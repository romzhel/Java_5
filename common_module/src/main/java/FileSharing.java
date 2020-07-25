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
    private ScheduledExecutorService executorService;
    private ObservableList<File> fileList;
    private File shareFolder;

    public FileSharing(File shareFolder) {
        this.shareFolder = shareFolder;
        fileList = FXCollections.observableList(new ArrayList<>());
        executorService = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        if (!shareFolder.exists()) {
            shareFolder.mkdir();
        }
        executorService.scheduleAtFixedRate(() -> {
            File[] files = shareFolder.listFiles(pathname -> pathname.isFile());
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

    public void changeShareFolder(File newShareFolder) {
        if (newShareFolder == null || newShareFolder.isFile()) {
            throw new RuntimeException("Недопустимая папка: " + newShareFolder.toString());
        }

        stop();
        shareFolder = newShareFolder;
        start();
    }
}
