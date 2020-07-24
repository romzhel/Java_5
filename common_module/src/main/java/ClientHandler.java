import javafx.collections.ObservableList;
import javafx.scene.control.ListView;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private ObservableList<ClientHandler> clientList;
    private ListView<File> lvFileList;
    private FileSharing fileSharing;

    public ClientHandler(Socket socket, ObservableList<ClientHandler> clientList, ListView<File> lvFileList,
                         FileSharing fileSharing) throws Exception {
        this.socket = socket;
        this.clientList = clientList;
        this.lvFileList = lvFileList;
        this.fileSharing = fileSharing;
        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() throws RuntimeException {
        try {
            Command command;
            while (true) {
                if (dataInputStream.available() > 0) {
                    String received = dataInputStream.readUTF();
                    System.out.println(received);
                    Command.valueOf(received).treat(this, lvFileList, fileSharing);
                }
            }
        } catch (Exception e) {

        }
    }

    public void close() {
        Closeable[] closeable = new Closeable[]{dataInputStream, dataOutputStream, socket};
        for (Closeable instance:closeable) {
            try {
                instance.close();
            } catch (Exception e) {

            }
        }
        if (clientList != null) {
            clientList.remove(this);
        }
    }

    public DataInputStream getDataInputStream() {
        return dataInputStream;
    }

    public DataOutputStream getDataOutputStream() {
        return dataOutputStream;
    }
}
