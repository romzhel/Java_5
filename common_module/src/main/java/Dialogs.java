import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class Dialogs {

    public static File selectFolder(Stage parentStage, String title) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(title);
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.dir") + "\\cloud_files"));

        return directoryChooser.showDialog(parentStage);
    }

    public static File selectAnyFile(Stage stage, String windowTitle, FileChooser.ExtensionFilter fileFilter, String fileName) {
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

        return fileName == null || fileName.contains("\\") ? fileChooser.showOpenDialog(stage) : fileChooser.showSaveDialog(stage);
    }

    public static File selectAnyFileTS(Stage stage, String windowTitle, FileChooser.ExtensionFilter fileFilter, String fileName) {
        if (!Thread.currentThread().getName().equals("JavaFX Application Thread")) {
            AtomicReference<File> result = new AtomicReference<>(null);
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

    public static void showMessage(String title, String message, double... size) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initModality(Modality.APPLICATION_MODAL);

        if (size.length > 0) alert.getDialogPane().setMinWidth(size[0]);
        if (size.length > 1) alert.getDialogPane().setMinHeight(size[1]);

        alert.showAndWait();
    }

    public static void showMessageTS(String title, String message, double... size) {
        if (!Thread.currentThread().getName().equals("JavaFX Application Thread")) {
            CountDownLatch inputWaiting = new CountDownLatch(1);

            Platform.runLater(() -> {
                showMessage(title, message, size);
                inputWaiting.countDown();
            });

            try {
                inputWaiting.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            showMessage(title, message, size);
        }
    }
}
