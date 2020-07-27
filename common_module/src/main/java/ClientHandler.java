import auth_service.User;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private MessageListener messageListener;
    private CloseListener closeListener;
    private User user;
    private File selectedFolder;

    public ClientHandler(Socket socket) throws Exception {
        this.socket = socket;
        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
        user = User.UNREGISTERED();
    }

    @Override
    public void run() throws RuntimeException {
        try {
            while (true) {
                if (dataInputStream.available() > 0 && messageListener != null) {
                    messageListener.send(dataInputStream.readUTF());
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            close();
        }
    }

    public void close() {
        Closeable[] closeable = new Closeable[]{dataInputStream, dataOutputStream, socket};
        for (Closeable instance : closeable) {
            try {
                instance.close();
            } catch (Exception e) {

            }
        }
        if (closeListener != null) {
            closeListener.send();
        }
    }

    public DataInputStream getDataInputStream() {
        return dataInputStream;
    }

    public DataOutputStream getDataOutputStream() {
        return dataOutputStream;
    }

    public Socket getSocket() {
        return socket;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public interface MessageListener {
        void send(String message);
    }

    public interface CloseListener {
        void send();
    }

    public File getSelectedFolder() {
        return selectedFolder;
    }

    public void setSelectedFolder(File selectedFolder) {
        this.selectedFolder = selectedFolder;
    }

    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public void setCloseListener(CloseListener closeListener) {
        this.closeListener = closeListener;
    }
}
