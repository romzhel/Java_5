package file_utils;

import auth_service.User;
import commands.CmdParams;
import commands.Command;
import database.DataBase;
import database.FileDb;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import processes.ClientHandler;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;

public class FileInfoCollector {
    public static final Path MAIN_FOLDER = Paths.get(System.getProperty("user.dir"), "cloud_files");
    public static final Path CLIENT_FOLDER = Paths.get(System.getProperty("user.dir"), "client_files");
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

    public void start() throws Exception {
        if (!MAIN_FOLDER.toFile().exists()) {
            MAIN_FOLDER.toFile().mkdir();
        }

        Command.IN_LOGIN_DATA_CHECK_AND_SEND_BACK_NICK.addCommandResultListener(this::createFolder);
        Command.IN_RECEIVE_REGISTRATION_DATA.addCommandResultListener(this::createFolder);
        Command.IN_SHARING_DATA.addCommandResultListener(this::applyShareInfoChanges);

        FolderWatcherService.getInstance().addChangeListener(FileSystemChangeListener.create()
                .setChangeListener(this::applyItemDeleting)
                .setMonitoredFolderPath(MAIN_FOLDER)
                .setRelativesPath(MAIN_FOLDER)
                .setEventTypes(new WatchEvent.Kind[]{StandardWatchEventKinds.ENTRY_DELETE}));
    }

    private void createFolder(Object... objects) {
        File userFolder = MAIN_FOLDER.resolve(((User) objects[0]).getNick()).toFile();
        if (!userFolder.exists()) {
            userFolder.mkdir();
        }
    }

    public FolderInfo getFilesInfo(ClientHandler clientHandler, Path folder) throws Exception {
        return FileSystemRequester.getDetailedPathInfo(MAIN_FOLDER.resolve(folder), MAIN_FOLDER);
    }

    public ShareInfo getShareInfo(String path) throws Exception {
        return fileDb.getShareInfo(path);
    }

    public void applyShareInfoChanges(Object... objects) {
        ShareInfo shareInfo = (ShareInfo) objects[0];
        logger.trace("синхронизация с БД {}", shareInfo);
        String path = shareInfo.getFileName();

        try {
            if (!fileDb.updateFileMainInfo(shareInfo)) {
                fileDb.addFileMainInfo(shareInfo);
            }

            for (UserShareInfo usi : shareInfo.getAddedItems()) {
                if (!fileDb.updateFileInfo(path, usi)) {
                    fileDb.addFileInfo(path, usi);
                }
            }

            for (UserShareInfo usi : shareInfo.getDeletedItems()) {
                fileDb.deleteFileInfo(path, usi);
            }
        } catch (Exception e) {
            logger.error("ошибка работы БД {}", e.getMessage(), e);
            try {
                Command.OUT_SEND_ERROR.execute(CmdParams.parse(e.getMessage()));
            } catch (Exception exception) {
                logger.error("ошибка отправки сообщения об ошибке {}", e.getMessage(), e);
            }
        }
    }

    public void applyItemDeleting(Path parentFolder, Path deletedItem) {
        logger.trace("обнаружено удаление объекта {}", deletedItem);
        try {
            fileDb.deleteFileMainInfo(deletedItem.toString());
        } catch (Exception e) {
            logger.error("ошибка работы БД {}", e.getMessage(), e);
            try {
                Command.OUT_SEND_ERROR.execute(CmdParams.parse(e.getMessage()));
            } catch (Exception exception) {
            }
        }
    }
}
