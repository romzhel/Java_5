import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FileSharing {
    public static final String UP_LEVEL = "UP_LEVEL";
    private ScheduledExecutorService executorService;
    private ObservableList<File> fileList;
    private File shareFolder;
    private FileDb fileDb;

    public FileSharing(File shareFolder) throws Exception {
        this.shareFolder = shareFolder;
        fileList = FXCollections.observableList(new ArrayList<>());
        executorService = Executors.newSingleThreadScheduledExecutor();
        fileDb = new FileDb(DataBase.getInstance().getConnection());
        fileDb.init();
    }

    public void start() {
        if (!shareFolder.exists()) {
            shareFolder.mkdir();
        }
        executorService.scheduleAtFixedRate(() -> {
            File[] files = shareFolder.listFiles();
            if (files.length != fileList.size()) {
                fileList.clear();
                fileList.addAll(files);
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void addFileListChangeListener(ListChangeListener<File> listChangeListener) {
        if (listChangeListener != null) {
            fileList.addListener(listChangeListener);
        }
    }

    public void stop() {
        try {
            executorService.shutdownNow();
            executorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (Exception e) {

        }
    }

    public List<File> getFileList() {
        return fileList;
    }

    public File getShareFolder() {
        return shareFolder;
    }

    public File[] getFileList(ClientHandler clientHandler, String folder) {
        if (clientHandler.getUser().getId() != 0) {
            if (folder.equals(UP_LEVEL)) {
                File userFolder = new File(shareFolder.getPath() + "\\" + clientHandler.getUser().getNick());
                if (!userFolder.exists()) {
                    userFolder.mkdir();
                }
                clientHandler.setSelectedFolder(userFolder);
                return userFolder.listFiles();
            }
        }

        return new File[]{};
    }

    public void addNewFile(File file, ClientHandler clientHandler) throws Exception {
        fileDb.saveNewFile(file.toString(), clientHandler.getUser().getId());
    }
}
