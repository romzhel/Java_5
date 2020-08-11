import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Collectors;

public class FileSystemRequester extends SimpleFileVisitor<Path> {
    private static FileSystemRequester instance;
    private long size;
    private int folderCount;
    private int filesCount;

    private FileSystemRequester() {
    }

    public static FilesInfo getFullPathInfo(Path path) throws IOException {
        return FilesInfo.create()
                .setFolder(FileInfoCollector.MAIN_FOLDER.relativize(path))
                .setFileList(Files.walk(path).map(FileSystemRequester::getPathInfo).collect(Collectors.toList()));
    }

    public static FileInfo getPathInfo(Path path) {
        if (instance == null) {
            instance = new FileSystemRequester();
        }

        instance.size = 0;
        instance.folderCount = -1;
        instance.filesCount = 0;

        try {
            Files.walkFileTree(path, instance);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return FileInfo.create(FileInfoCollector.MAIN_FOLDER.relativize(path), instance.size, Files.isDirectory(path))
                .setFilesCount(instance.filesCount)
                .setFoldersCount(instance.folderCount);
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (attrs.isRegularFile()) {
            filesCount++;
        }
        size += attrs.size();
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        folderCount++;
        return FileVisitResult.CONTINUE;
    }
}
