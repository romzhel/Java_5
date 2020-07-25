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

    private CloudServer() {
        filesSharing = new FileSharing(new File(DEFAULT_FOLDER));
        executorService = Executors.newFixedThreadPool(4);
        clientList = FXCollections.observableList(new ArrayList<>());
    }

    public static CloudServer getInstance() {
        if (instance == null) {
            instance = new CloudServer();
        }
        return instance;
    }

    public void init() {
        filesSharing.addFileListChangeListener(c -> {
            for (ClientHandler clientHandler : clientList) {
                try {
                    Command.SEND_FILES_LIST.execute(CommandParameters.parse(clientHandler, c.getList()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void start() {
        filesSharing.start();
        executorService.submit(() -> {
            try {
                serverSocket = new ServerSocket(8189);
                while (true) {
                    Socket socket = serverSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(socket, this);
                    clientList.add(clientHandler);
                    Command.SEND_FILES_LIST.execute(CommandParameters.parse(clientHandler, filesSharing.getFileList()));
                    executorService.submit(clientHandler);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        });
    }

    public void stop() {
        filesSharing.stop();
        try {
            serverSocket.close();
            executorService.shutdownNow();
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public FileSharing getFilesSharing() {
        return filesSharing;
    }

    public synchronized ObservableList<ClientHandler> getClientList() {
        return clientList;
    }
}
