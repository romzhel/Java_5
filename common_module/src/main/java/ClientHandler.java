import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private CloudServer cloudServer;
    private MessageListener messageListener;
    private CloseListener closeListener;

    public ClientHandler(Socket socket, CloudServer cloudServer) throws Exception {
        this.socket = socket;
        this.cloudServer = cloudServer;
        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() throws RuntimeException {
        try {
            while (true) {
                if (dataInputStream.available() > 0) {
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

    public interface MessageListener {
        void send(String message);
    }

    public interface CloseListener {
        void send();
    }
}
