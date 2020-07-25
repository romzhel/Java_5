import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private FileSharing fileSharing;

    public ClientHandler(Socket socket, FileSharing fileSharing) throws Exception {
        this.socket = socket;
        this.fileSharing = fileSharing;
        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() throws RuntimeException {
        try {
            while (true) {
                if (dataInputStream.available() > 0) {
                    String received = dataInputStream.readUTF();
                    System.out.println(received);
                    Command.valueOf(received).execute(CommandParameters.parse(this, fileSharing));
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
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
    }

    public DataInputStream getDataInputStream() {
        return dataInputStream;
    }

    public DataOutputStream getDataOutputStream() {
        return dataOutputStream;
    }
}
