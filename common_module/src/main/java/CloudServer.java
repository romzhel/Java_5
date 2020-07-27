import auth_service.AuthService;
import auth_service.SqliteAuthService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CloudServer {
    private static final String DEFAULT_FOLDER = System.getProperty("user.dir") + "\\cloud_files";
    private static CloudServer instance;
    private FileSharing filesSharing;
    private ObservableList<ClientHandler> clientList;
    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private AuthService authService;

    private CloudServer() throws Exception {
        filesSharing = new FileSharing(new File(DEFAULT_FOLDER));
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
        /*filesSharing.addFileListChangeListener(c -> {
            for (ClientHandler clientHandler : clientList) {
                try {
                    Command.SEND_FILES_LIST.execute(CommandParameters.parse(clientHandler, c.getList()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });*/
        Command.RECEIVE_FILE.addCommandResultListener(objects -> {
            for (ClientHandler clientHandler : clientList) {
                if ((objects[0]).toString().startsWith(clientHandler.getSelectedFolder().toString())) {
                    try {
                        Command.SEND_FILES_LIST.execute(CommandParameters.parse(clientHandler,
                                clientHandler.getSelectedFolder().listFiles()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void start() {
        filesSharing.start();
        executorService.submit(() -> {
            try {
                authService.start();
                serverSocket = new ServerSocket(8189);
                while (true) {
                    Socket socket = serverSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(socket);
                    clientList.add(clientHandler);
//                    Command.SEND_FILES_LIST.execute(CommandParameters.parse(clientHandler, filesSharing.getFileList()));
                    clientHandler.setMessageListener(message -> {
                        try {
                            Command.valueOf(message).execute(CommandParameters.parse(clientHandler, filesSharing, this));
                        } catch (Exception e) {
                            try {
                                Command.SEND_ERROR.execute(CommandParameters.parse(clientHandler, new String[]{e.getMessage()}));
                            } catch (Exception exception) {
                                exception.printStackTrace();
                            }
                        }
                    });
                    clientHandler.setCloseListener(() -> clientList.remove(clientHandler));
                    Command.LOGIN_DATA.addCommandResultListener(objects -> {
                        try {
                            Command.SEND_FILES_LIST.execute(CommandParameters.parse(clientHandler,
                                    filesSharing.getFileList(clientHandler, FileSharing.UP_LEVEL)));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    executorService.submit(clientHandler);
                }
            } catch (Exception e) {
                Dialogs.showMessageTS("Ошибка инициализации сервера", e.getMessage());
                stop();
                Platform.exit();
            }
        });
    }

    public void stop() {
        filesSharing.stop();
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

    public FileSharing getFilesSharing() {
        return filesSharing;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public synchronized ObservableList<ClientHandler> getClientList() {
        return clientList;
    }
}
