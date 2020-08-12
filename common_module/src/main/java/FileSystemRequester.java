import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Collectors;

public class FileSystemRequester extends SimpleFileVisitor<Path> {
    private static FileSystemRequester instance;
    private Path path;
    private long size;
    private int folderCount;
    private int filesCount;

    private FileSystemRequester() {
    }

    public static FilesInfo getDetailedPathInfo(Path fullPath) {
        System.out.println("fullPath = " + fullPath);
        try {
            return FilesInfo.create()
                    .setFolder(FileInfoCollector.MAIN_FOLDER.relativize(fullPath))
                    .setFileList(Files.walk(fullPath)
                            .filter(path1 -> path1.getParent().equals(fullPath))
                            .map(FileSystemRequester::getPathInfo).collect(Collectors.toList()));
        } catch (IOException e) {
            e.printStackTrace();
            return FilesInfo.create();
        }
    }

    public static FileInfo getPathInfo(Path path) {
        if (instance == null) {
            instance = new FileSystemRequester();
        }

        instance.path = path;
        instance.size = 0;
        instance.folderCount = 0;
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
        if (!dir.equals(path)) {
            folderCount++;
        }
        return FileVisitResult.CONTINUE;
    }
}
