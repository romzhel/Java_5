import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileSharingServer {
    private static FileSharingServer instance;
    private FileSharing filesSharing;
    private ObservableList<ClientHandler> clientList;
    private ServerSocket serverSocket;
    private ExecutorService executorService;

    private FileSharingServer() {
        filesSharing = new FileSharing();
        executorService = Executors.newCachedThreadPool();
        clientList = FXCollections.observableList(new ArrayList<>());
    }

    public static FileSharingServer getInstance() {
        if (instance == null) {
            instance = new FileSharingServer();
        }

        return instance;
    }

    public void init(File filesFolder) {
        filesSharing.setShareFolder(filesFolder);
        filesSharing.getFileList().addListener((ListChangeListener<File>) c -> {
            for (ClientHandler clientHandler : clientList) {
                Command.SEND_FILES_LIST.treat(clientHandler, c.getList().toArray());
            }
        });
    }

    public void start() {
        executorService.submit(() -> {
            try {
                serverSocket = new ServerSocket(8189);
                System.out.println("Сервер запущен");
                while (true) {
                    Socket socket = serverSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(socket, clientList, null, filesSharing);
                    clientList.add(clientHandler);
                    Command.SEND_FILES_LIST.treat(clientHandler, filesSharing.getFileList().toArray());
                    executorService.submit(clientHandler);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void stop() {
        filesSharing.close();
        try {
            serverSocket.close();
            executorService.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public FileSharing getFilesSharing() {
        return filesSharing;
    }

    public ObservableList<ClientHandler> getClientList() {
        return clientList;
    }
}
