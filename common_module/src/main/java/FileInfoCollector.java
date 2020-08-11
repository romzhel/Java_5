import auth_service.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class FileInfoCollector {
    public static final Path MAIN_FOLDER = Paths.get(System.getProperty("user.dir"), "cloud_files");
    public static final Path UP_LEVEL = Paths.get("");
    private static final Logger logger = LogManager.getLogger(FileInfoCollector.class);
    private FileDb fileDb;

    public FileInfoCollector() throws Exception {
        fileDb = new FileDb(DataBase.getInstance().getConnection());
        fileDb.init();
    }

    public static String formatSize(long v) {
        if (v < 1024) {
            return v + " B";
        }
        int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
        return String.format("%.1f %sB", (double) v / (1L << (z * 10)), " KMGTPE".charAt(z));
    }

    public void start() {
        if (!MAIN_FOLDER.toFile().exists()) {
            MAIN_FOLDER.toFile().mkdir();
        }

        Command.IN_LOGIN_DATA_CHECK_AND_SEND_BACK_NICK.addCommandResultListener(objects -> {
            File userFolder = MAIN_FOLDER.resolve(((User) objects[0]).getNick()).toFile();
            if (!userFolder.exists()) {
                userFolder.mkdir();
            }
        });
    }

    public FilesInfo getFilesInfo(ClientHandler clientHandler, Path folder) throws Exception {//сокращенное название папки
        return FilesInfo.create()
                .setFolder(folder)
                .setFileList(fileDb.getFiles(clientHandler.getUser(), folder)
                        .stream()
                        .map(path -> MAIN_FOLDER.resolve(path).toFile())
                        .filter(file -> {
//                            logger.trace("file {}, exists {}", file, file.exists());
//                            logger.trace("file parent {}, selected folder {}", file.toPath().getParent(), MAIN_FOLDER.resolve(clientHandler.getSelectedFolder()));
                            return file.exists() /*&& file.toPath().getParent().equals(MAIN_FOLDER.resolve(clientHandler.getSelectedFolder()))*/;
                        })
                        .map(file -> FileInfo.create(file.toPath().subpath(MAIN_FOLDER.getNameCount(), file.toPath().getNameCount()),
                                file.length(), file.isDirectory()))
                        .collect(Collectors.toList()));
    }

    public void addNewFile(Path path, ClientHandler clientHandler) throws Exception {
        logger.trace("добавление файла {}", path);
        fileDb.saveNewFile(path.toString(), clientHandler.getUser().getId());
    }

    public void getFolderInfo(Path folderPath) {
        long size = 0;
        int filesCount = 0;
        int folderCount = 0;


    }
}
