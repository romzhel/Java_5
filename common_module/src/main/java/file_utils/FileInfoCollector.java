package file_utils;

import auth_service.User;
import commands.CmdParams;
import commands.Command;
import database.DataBase;
import database.FileDb;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import processes.ClientHandler;
import processes.CloudServer;

import java.io.File;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

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
        Command.IN_SHARING_DATA.addCommandResultListener(objects -> {
            applyShareInfoChangesToDb(objects);
            try {
                CloudServer.getInstance().refreshClientsFileList(Paths.get(((ShareInfo) objects[0]).getFileName()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        FolderWatcherService.getInstance().addChangeListener(FileSystemChangeListener.create()
                .setChangeListener(this::applyItemDeletingFromDb)
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

    public FolderInfo getCompleteFilesInfo(ClientHandler clientHandler, Path folder) throws Exception {
        logger.trace("выполняется запрос по всем доступным файлам в папке '{}'", folder);
        Path selectedFolder = clientHandler.getSelectedFolder();
        String nick = clientHandler.getUser().getNick();
        FolderInfo folderInfo = null;

        if (folder.equals(UP_LEVEL) && clientHandler.getUser().equals(User.UNREGISTERED)) {
            folderInfo = FolderInfo.create()
                    .setFolder(folder)
                    .setFileList(treatFoldersFromDb(clientHandler, folder));
            logger.trace("доступные ресурсы в БД {}", folderInfo);
        } else if ((folder.equals(UP_LEVEL) || folder.toString().equals(nick)) && !clientHandler.getUser().equals(User.UNREGISTERED)) {
            folderInfo = FileSystemRequester.getDetailedPathInfo(MAIN_FOLDER.resolve(folder), MAIN_FOLDER);
            folderInfo.getFileList().addAll(treatFoldersFromDb(clientHandler, UP_LEVEL));
            logger.trace("доступные ресурсы в БД и на диске {}", folderInfo);
        } else if (folder.toString().startsWith(nick)) {
            folderInfo = FileSystemRequester.getDetailedPathInfo(MAIN_FOLDER.resolve(folder), MAIN_FOLDER);
            logger.trace("доступные ресурсы на диске {}", folderInfo);
        } else if (folder.getNameCount() == 1 || selectedFolder.getNameCount() < folder.getNameCount()) {
            folderInfo = FolderInfo.create()
                    .setFolder(folder)
                    .setFileList(treatFoldersFromDb(clientHandler, folder));
            logger.trace("доступные в корне папки в БД и файлы на диске {}", folderInfo);
        } else {
            folderInfo = FileSystemRequester.getDetailedPathInfo(MAIN_FOLDER.resolve(folder), MAIN_FOLDER);
            folderInfo.getFileList().addAll(treatFoldersFromDb(clientHandler, folder));
            logger.trace("доступные папки в БД и файлы на диске {}", folderInfo);
        }

        return folderInfo;
    }

    private List<FileInfo> treatFoldersFromDb(ClientHandler clientHandler, Path folder) throws Exception {
        return fileDb.getSharedFoldersForUser(clientHandler.getUser(), folder.toString()).stream()
                .filter(path -> !path.toString().startsWith(clientHandler.getUser().getNick()))
                .map(path -> folder.toString().isEmpty() ? path.getName(0) : path.subpath(0, folder.getNameCount() + 1))
                .distinct()
                .map(path -> FileInfo.create(path, 0L, Files.isDirectory(MAIN_FOLDER.resolve(path))))
                .collect(Collectors.toList());
    }

    public ShareInfo getShareInfo(String path) throws Exception {
        return fileDb.getSingleFolderShareInfo(path);
    }

    public void applyShareInfoChangesToDb(Object... objects) {
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

    public void applyItemDeletingFromDb(Path parentFolder, Path deletedItem) {
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
