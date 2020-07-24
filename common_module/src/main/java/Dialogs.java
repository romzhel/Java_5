import javafx.application.Platform;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class Dialogs {

    public static File selectFolder(Stage parentStage, String title) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(title);
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.dir")));

        return directoryChooser.showDialog(parentStage);
    }

    public static List<File> selectAnyFile(Stage stage, String windowTitle, FileChooser.ExtensionFilter fileFilter, String fileName) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(windowTitle);
        if (fileFilter != null) fileChooser.getExtensionFilters().add(fileFilter);
        if (fileName != null) {
            File temp = new File(fileName);
            if (fileName.contains("\\") && temp.exists()) {
                fileChooser.setInitialDirectory(temp);
            } else {
                fileChooser.setInitialFileName(fileName);
            }
        }

        return fileName == null || fileName.contains("\\") ? fileChooser.showOpenMultipleDialog(stage) :
                Arrays.asList(fileChooser.showSaveDialog(stage));
    }

    public static List<File> selectAnyFileTS(Stage stage, String windowTitle, FileChooser.ExtensionFilter fileFilter, String fileName) {
        if (!Thread.currentThread().getName().equals("JavaFX Application Thread")) {
            AtomicReference<List<File>> result = new AtomicReference<>(null);
            CountDownLatch inputWaiting = new CountDownLatch(1);

            Platform.runLater(() -> {
                result.set(selectAnyFile(stage, windowTitle, fileFilter, fileName));
                inputWaiting.countDown();
            });

            try {
                inputWaiting.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return result.get();
        } else {
            return selectAnyFile(stage, windowTitle, fileFilter, fileName);
        }
    }
}
