package file_utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Collectors;

public class FileSystemRequester extends SimpleFileVisitor<Path> {
    private static final Logger logger = LogManager.getLogger(FileSystemRequester.class);
    private static FileSystemRequester instance;
    private Path path;
    private long size;
    private int folderCount;
    private int filesCount;

    private FileSystemRequester() {
    }

    public static FolderInfo getDetailedPathInfo(Path fullPath, Path relativePath) {
        logger.debug("запрошен состав папки '{}' с относительным путём '{}'", fullPath, relativePath);
        if (fullPath.getRoot() == null) {
            fullPath = relativePath.resolve(fullPath);
        }
        logger.debug("поиск файлов в '{}'", fullPath);
        try {
            Path finalFullPath = fullPath;
            return FolderInfo.create()
                    .setFolder(relativePath.relativize(fullPath))
                    .setFileList(Files.walk(fullPath)
                            .filter(path1 -> path1.getParent().equals(finalFullPath))
                            .map(path -> getPathInfo(path, relativePath))
                            .sorted((o1, o2) -> (o1.isFolder() ? 0 : 1) - (o2.isFolder() ? 0 : 1))
                            .collect(Collectors.toList()));
        } catch (IOException e) {
            e.printStackTrace();
            return FolderInfo.create();
        }
    }

    public static FileInfo getPathInfo(Path path, Path relativePath) {
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
        return FileInfo.create(!relativePath.equals(Paths.get("")) ? relativePath.relativize(path) : path,
                instance.size, Files.isDirectory(path))
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
