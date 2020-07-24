import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FileSharing {
    private ScheduledExecutorService executorService;
    private ObservableList<File> fileList;
    private File sharedFolder;

    public void setShareFolder(File shareFolder) {
        this.sharedFolder = shareFolder;
        fileList = FXCollections.observableList(new ArrayList<>());
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> {
            File[] files = shareFolder.listFiles(pathname -> pathname.isFile());
            if (files.length != fileList.size()) {
                fileList.clear();
                fileList.addAll(files);
            }
        }, 3, 1, TimeUnit.SECONDS);
    }

    public void close() {
        executorService.shutdown();
    }

    public ObservableList<File> getFileList() {
        return fileList;
    }

    public File getSharedFolder() {
        return sharedFolder;
    }
}
