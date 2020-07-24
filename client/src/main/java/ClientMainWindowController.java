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

        btnDownload.setOnAction(event -> {
            File selectedFile = lvFiles.getSelectionModel().getSelectedItem();
            if (selectedFile != null) {
                Command.REQUEST_DOWNLOAD.treat(clientHandler, selectedFile);
            }
        });

        btnUpload.setOnAction(event -> {
            File uploadedFile = Dialogs.selectAnyFileTS(null, "Выбор файла для загруки на сервер",
                    null, null).get(0);
            Command.SEND_FILE.treat(clientHandler, uploadedFile);
        });

        try {
            socket = new Socket("localhost", 8189);
            clientHandler = new ClientHandler(socket, null, lvFiles, null);
            executorService = Executors.newSingleThreadExecutor();
            executorService.submit(clientHandler);
        } catch (Exception e) {

        }

    }

    public void close() {
        clientHandler.close();
        executorService.shutdown();
    }
}
