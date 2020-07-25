import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

import java.io.File;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientMainWindowController implements Initializable {
    private ClientHandler clientHandler;
    private ExecutorService executorService;
    private Socket socket;

    @FXML
    private ListView<File> lvFiles;
    @FXML
    private Button btnDownload;
    @FXML
    private Button btnUpload;
    @FXML
    private Button btnClose;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lvFiles.setCellFactory(new Callback<ListView<File>, ListCell<File>>() {
            @Override
            public ListCell<File> call(ListView<File> param) {
                return new ListCell<File>() {
                    @Override
                    protected void updateItem(File item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item == null || empty) {
                            setText(null);
                        } else {
                            setText(item.getName());
                        }
                    }
                };
            }
        });

        Command.FILES_LIST.setCommandResult(objects -> Platform.runLater(() -> {
            lvFiles.getItems().clear();
            for (Object obj:objects) {
                lvFiles.getItems().add((File)obj);
            }
        }));

        btnDownload.setOnAction(event -> {
            File selectedFile = lvFiles.getSelectionModel().getSelectedItem();
            if (selectedFile != null) {
                try {
                    Command.DOWNLOAD_REQUEST.execute(CommandParameters.parse(clientHandler, selectedFile));
                } catch (Exception e) {
                    Dialogs.showMessageTS("Скачивание файла с сервера", "Ошибка:\n\n" + e.getMessage());
                }
            } else {
                Dialogs.showMessage("Загрузка файла на сервер", "Выберите файл в списке");
            }
        });

        btnUpload.setOnAction(event -> {
            File uploadedFile = Dialogs.selectAnyFileTS(null, "Выбор файла для загруки на сервер",
                    null, null);
            if (uploadedFile == null || !uploadedFile.exists()) {
                return;
            }
            try {
                Command.SEND_FILE.execute(CommandParameters.parse(clientHandler, uploadedFile));
            } catch (Exception e) {
                Dialogs.showMessageTS("Загрузка файла на сервер", "Ошибка:\n\n" + e.getMessage());
            }
        });

        btnClose.setOnAction(event -> {
            close();
        });

        try {
            socket = new Socket("localhost", 8189);
            clientHandler = new ClientHandler(socket, null);
            executorService = Executors.newSingleThreadExecutor();
            executorService.submit(clientHandler);
        } catch (Exception e) {

        }

    }

    public void close() {
        try {
            Command.SEND_EXIT.execute(CommandParameters.parse(clientHandler));
        } catch (Exception e) {
            e.printStackTrace();
        }
        executorService.shutdown();
        Platform.exit();
    }
}
