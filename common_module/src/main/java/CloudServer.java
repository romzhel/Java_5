import auth_service.AuthService;
import auth_service.SqliteAuthService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CloudServer {
    private static final Logger logger = LogManager.getLogger(CloudServer.class);
    private static CloudServer instance;
    private FileInfoCollector fileInfoCollector;
    private ObservableList<ClientHandler> clientList;
    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private AuthService authService;
    private FolderWatcherService folderWatcherService;

    private CloudServer() throws Exception {
        fileInfoCollector = new FileInfoCollector();
        executorService = Executors.newFixedThreadPool(4);
        clientList = FXCollections.observableList(new ArrayList<>());
        authService = new SqliteAuthService(DataBase.getInstance().getConnection());
    }

    public static CloudServer getInstance() throws Exception {
        if (instance == null) {
            instance = new CloudServer();
        }
        return instance;
    }

    public void init() {
//        Command.IN_RECEIVE_FILE.addCommandResultListener(this::refreshClients);
//        Command.IN_CREATE_FOLDER.addCommandResultListener(this::refreshClients);
        try {
            FolderWatcherService.getInstance().addChangeListener(this::refreshClientsFileList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshClientsFileList(Object... objects) {
        logger.trace("аргументы обновления клиентов {}", objects);
        for (ClientHandler clientHandler : clientList) {
            Path currentFolder = Paths.get(clientHandler.getUser().getNick()).resolve(clientHandler.getSelectedFolder());
            if (((Path) objects[0]).equals(currentFolder)) {
                try {
                    logger.trace("обновление клиента {} папки {}", clientHandler, clientHandler.getSelectedFolder());
                    Command.OUT_SEND_FILE_LIST.execute(CmdParams.parse(clientHandler, clientHandler.getSelectedFolder()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void start() {
        logger.trace("запуск сервера");
        executorService.submit(() -> {
            try {
                fileInfoCollector.start();
//                folderWatcherService.start();
                authService.start();
                serverSocket = new ServerSocket(8189);
                logger.trace("сервер запущен, ожидание подключения клиентов");
                while (true) {
                    Socket socket = serverSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(this, socket);
                    initClientHandler(clientHandler);
                    executorService.submit(clientHandler);
                }
            } catch (Exception e) {
                Dialogs.showMessageTS("Ошибка инициализации сервера", e.getMessage() + "\n\n" + e.toString());
                e.printStackTrace();
                stop();
                Platform.exit();
            }
        });
    }

    private void initClientHandler(ClientHandler clientHandler) throws Exception {
        clientList.add(clientHandler);
//        Command.OUT_SEND_FILE_LIST.execute(CmdParams.parse(clientHandler, FileInfoCollector.UP_LEVEL));
        clientHandler.setMessageListener(message -> {
            try {
                Command.valueOf(message).execute(CmdParams.parse(clientHandler));
            } catch (Exception e) {
                try {
                    Command.OUT_SEND_ERROR.execute(CmdParams.parse(clientHandler, e.getMessage()));
                } catch (Exception exception) {

                }
            }
        });
        clientHandler.setCloseListener(() -> clientList.remove(clientHandler));
        Command.IN_LOGIN_DATA_CHECK_AND_SEND_BACK_NICK.addCommandResultListener(objects -> {
            try {
                Command.OUT_SEND_FILE_LIST.execute(CmdParams.parse(clientHandler, FileInfoCollector.UP_LEVEL));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void stop() {
        try {
            DataBase.getInstance().disconnect();
        } catch (Exception e) {
        }
        try {
            serverSocket.close();

        } catch (Exception e) {
        }
        try {
            executorService.shutdownNow();
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (Exception e) {
        }
    }

    public FileInfoCollector getFileInfoCollector() {
        return fileInfoCollector;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public synchronized ObservableList<ClientHandler> getClientList() {
        return clientList;
    }

    public FolderWatcherService getFolderWatcherService() {
        return folderWatcherService;
    }
}
