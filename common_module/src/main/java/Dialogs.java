import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.*;

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

    public static File selectAnyFileTS(Stage stage, String windowTitle, String fileName) {
        if (!Thread.currentThread().getName().equals("JavaFX Application Thread")) {
            AtomicReference<File> result = new AtomicReference<>(null);
            CountDownLatch inputWaiting = new CountDownLatch(1);

            Platform.runLater(() -> {
                result.set(selectAnyFile(stage, windowTitle, new FileChooser.ExtensionFilter("Все файлы", "*.*"), fileName));
                inputWaiting.countDown();
            });

            try {
                inputWaiting.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return result.get();
        } else {
            return selectAnyFile(stage, windowTitle, new FileChooser.ExtensionFilter("Все файлы", "*.*"), fileName);
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

    public static String[] getLoginData() throws Exception {
        VBox root = new VBox();
        root.setPadding(new Insets(25));
        root.setSpacing(10);

        TextField tfLogin = new TextField();
        tfLogin.setPromptText("Введите логин");
        PasswordField tfPassword = new PasswordField();
        tfPassword.setPromptText("Введите пароль");

        HBox buttonsArea = new HBox();
        buttonsArea.setSpacing(26);
        buttonsArea.setAlignment(Pos.CENTER);
        Button btnOk = new Button("OK");
        btnOk.setPrefWidth(75);
        Button btnCancel = new Button("Отмена");
        btnCancel.setPrefWidth(75);
        buttonsArea.getChildren().addAll(btnOk, btnCancel);

        Stage stage = new Stage();
        Scene scene = new Scene(root, 280, 150);
        stage.setScene(scene);
        stage.setTitle("Авторизация");
        stage.initStyle(StageStyle.UTILITY);
        stage.initModality(Modality.APPLICATION_MODAL);
        root.getChildren().addAll(tfLogin, tfPassword, buttonsArea);
        btnCancel.requestFocus();

        final boolean[] isCancelled = {false};

        stage.setOnCloseRequest(event -> {
            event.consume();
            isCancelled[0] = true;
            stage.close();
        });

        btnOk.setOnAction(event -> {
            boolean hasInputError = false;
            if (tfLogin.getText().length() < 3) {
                showMessageTS("Ошибка ввода", "Логин должен быть не менее 3 символов");
                hasInputError = true;
            } else if (tfPassword.getText().isEmpty()) {
                showMessageTS("Ошибка ввода", "Пароль не может быть пустым");
                hasInputError = true;
            }
            if (!hasInputError) {
                stage.close();
            }
        });

        btnCancel.setOnAction(event -> {
            isCancelled[0] = true;
            stage.close();
        });
        stage.showAndWait();

        if (isCancelled[0]) {
            throw new RuntimeException("Отмена операции");
        }

        return new String[]{tfLogin.getText(), tfPassword.getText()};
    }

    public static String[] getRegistrationData() throws Exception {
        VBox root = new VBox();
        root.setPadding(new Insets(25));
        root.setSpacing(10);

        TextField tfName = new TextField();
        tfName.setPromptText("Введите имя");
        TextField tfLogin = new TextField();
        tfLogin.setPromptText("Введите логин");
        PasswordField tfPassword = new PasswordField();
        tfPassword.setPromptText("Введите пароль");
        PasswordField tfPasswordConfirmation = new PasswordField();
        tfPasswordConfirmation.setPromptText("Введите пароль");

        HBox buttonsArea = new HBox();
        buttonsArea.setSpacing(26);
        buttonsArea.setAlignment(Pos.CENTER);
        Button btnOk = new Button("OK");
        btnOk.setPrefWidth(75);
        Button btnCancel = new Button("Отмена");
        btnCancel.setPrefWidth(75);
        buttonsArea.getChildren().addAll(btnOk, btnCancel);

        Stage stage = new Stage();
        Scene scene = new Scene(root, 280, 150);
        stage.setScene(scene);
        stage.setTitle("Авторизация");
        stage.initStyle(StageStyle.UTILITY);
        stage.initModality(Modality.APPLICATION_MODAL);
        root.getChildren().addAll(tfName, tfLogin, tfPassword, tfPasswordConfirmation, buttonsArea);
        btnCancel.requestFocus();
        stage.showAndWait();

        if (tfName.getText().length() < 3) {
            throw new RuntimeException("Имя должно быть не менее 3 символов");
        }
        if (!tfPassword.getText().matches(tfPasswordConfirmation.getText())) {
            throw new RuntimeException("Пароли не совпадают");
        }

        return new String[]{tfLogin.getText(), tfPassword.getText()};
    }
}
