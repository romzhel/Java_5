import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;


public class ServerMainWindowController implements Initializable {
    @FXML
    private ListView<File> lvFiles;
    @FXML
    private Label lblStatus;
    @FXML
    private ListView<ClientHandler> lvClients;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lblStatus.setText("Ожидание подключения клиентов");
        FileSharingServer.getInstance().getClientList().addListener((ListChangeListener<ClientHandler>) c -> {
            Platform.runLater(()-> {
                lvClients.getItems().clear();
                lvClients.getItems().addAll(c.getList());

                if (lvClients.getItems().size() == 0) {
                    lblStatus.setText("Ожидание подключения клиентов");
                } else {
                    lblStatus.setText(String.format("Подключено клиентов: %d", lvClients.getItems().size()));
                }
            });
        });

        FileSharingServer.getInstance().getFilesSharing().getFileList().addListener((ListChangeListener<File>) c -> {
            if (c.getList().size() > 0) {
                Platform.runLater(()-> {
                    lvFiles.getItems().clear();
                    lvFiles.getItems().addAll(c.getList());
                });
            }
        });

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
    }
}
