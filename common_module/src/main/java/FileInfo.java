import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileInfo {
    private Path path;
    private long length;
    private boolean isFolder;
    private int filesCount;
    private int foldersCount;

    public FileInfo(Path path, long length, boolean isFolder) {
        this.path = path;
        this.length = length;
        this.isFolder = isFolder;
    }

    public static FileInfo create(Path path, long length, boolean isFolder) {
        return new FileInfo(path, length, isFolder);
    }

    public static FileInfo getFrom(ClientHandler clientHandler) throws Exception {
        DataInputStream dis = clientHandler.getDataInputStream();
        return new FileInfo(Paths.get(dis.readUTF()), dis.readLong(), dis.readBoolean());
    }

    public void sendTo(ClientHandler clientHandler) throws Exception {
        DataOutputStream dos = clientHandler.getDataOutputStream();
        dos.writeUTF(path.toString());
        dos.writeLong(length);
        dos.writeBoolean(isFolder);
    }

    public Path getPath() {
        return path;
    }

    public FileInfo setPath(Path path) {
        this.path = path;
        return this;
    }

    public long getLength() {
        return length;
    }

    public FileInfo setLength(long length) {
        this.length = length;
        return this;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public void setFolder(boolean folder) {
        isFolder = folder;
    }

    public int getFilesCount() {
        return filesCount;
    }

    public FileInfo setFilesCount(int filesCount) {
        this.filesCount = filesCount;
        return this;
    }

    public int getFoldersCount() {
        return foldersCount;
    }

    public FileInfo setFoldersCount(int foldersCount) {
        this.foldersCount = foldersCount;
        return this;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "path=" + path +
                ", length=" + length +
                ", isFolder=" + isFolder +
                ", filesCount=" + filesCount +
                ", foldersCount=" + foldersCount +
                '}';
    }
}
