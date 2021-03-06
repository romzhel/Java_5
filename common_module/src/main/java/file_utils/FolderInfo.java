package file_utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import processes.ClientHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FolderInfo {
    private static final Logger logger = LogManager.getLogger(FolderInfo.class);
    private Path folder;
    private List<FileInfo> fileList;


    private FolderInfo() {
        this.fileList = new ArrayList<>();
    }

    public static FolderInfo create() {
        return new FolderInfo();
    }

    public FolderInfo setFolder(Path folder) {
        this.folder = folder;
        return this;
    }

    public FolderInfo setFileList(FileInfo... fileList) {
        this.fileList.addAll(Arrays.asList(fileList));
        return this;
    }

    public FolderInfo setFileList(List<FileInfo> fileList) {
        this.fileList.addAll(fileList);
        return this;
    }

    public void sendTo(ClientHandler clientHandler) throws Exception {
        DataOutputStream dos = clientHandler.getDataOutputStream();
        dos.writeUTF(folder.toString());
        dos.writeInt(fileList.size());
        for (FileInfo fileInfo : fileList) {
            fileInfo.sendTo(clientHandler);
        }
    }

    public FolderInfo getFrom(ClientHandler clientHandler) throws Exception {
        DataInputStream dis = clientHandler.getDataInputStream();
        Path folder = Paths.get(dis.readUTF());
//        logger.trace("folder = {}", folder);
        List<FileInfo> list = new ArrayList<>();
        int count = dis.readInt();
//        logger.trace("count = {}", count);
        for (int i = 0; i < count; i++) {
            FileInfo fi = FileInfo.getFrom(clientHandler);
            list.add(fi);
//            logger.trace(fi);
        }

        return FolderInfo.create().setFolder(folder).setFileList(list);
    }

    public Path getFolder() {
        return folder;
    }

    public List<FileInfo> getFileList() {
        return fileList;
    }

    @Override
    public String toString() {
        return "file_utils.FilesInfo {" +
                "folder=" + folder +
                ", fileList=" + Arrays.toString(fileList.toArray()) +
                '}';
    }
}
