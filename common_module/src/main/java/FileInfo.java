import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileInfo {
    private Path path;
    private long length;
    private boolean isFolder;

    public FileInfo(Path path, long length, boolean isFolder) {
        this.path = path;
        this.length = length;
        this.isFolder = isFolder;
    }

    public static FileInfo create(Path path, long length, boolean isFolder) {
        return new FileInfo(path, length, isFolder);
    }

    public void sendTo(ClientHandler clientHandler) throws Exception {
        DataOutputStream dos = clientHandler.getDataOutputStream();
        dos.writeUTF(path.toString());
        dos.writeLong(length);
        dos.writeBoolean(isFolder);
    }

    public static FileInfo getFrom(ClientHandler clientHandler) throws Exception {
        DataInputStream dis = clientHandler.getDataInputStream();
        return new FileInfo(Paths.get(dis.readUTF()), dis.readLong(), dis.readBoolean());
    }

    public Path getPath() {
        return path;
    }

    public long getLength() {
        return length;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public void setFolder(boolean folder) {
        isFolder = folder;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "path=" + path +
                ", length=" + length +
                ", isFolder=" + isFolder +
                '}';
    }
}
