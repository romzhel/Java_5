import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    @FXML
    private Button btnClose;

    private static final Logger logger = LogManager.getLogger(ServerMainWindowController.class);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.trace("инициализация окна оснастки сервера");
        try {
            CloudServer.getInstance().init();

            lblStatus.setText("Ожидание подключения клиентов");

            Command.IN_LOGIN_DATA_CHECK_AND_SEND_BACK_NICK.addCommandResultListener(objects -> lvClients.refresh());

            CloudServer.getInstance().getClientList().addListener((ListChangeListener<ClientHandler>) c -> {
                Platform.runLater(() -> {
                    lvClients.getItems().clear();
                    lvClients.getItems().addAll(c.getList());

                    if (lvClients.getItems().size() == 0) {
                        lblStatus.setText("Ожидание подключения клиентов");
                    } else {
                        lblStatus.setText(String.format("Подключено клиентов: %d", lvClients.getItems().size()));
                    }
                });
            });

            /*Command.IN_RECEIVE_FILE.addCommandResultListener(objects -> Platform.runLater(() -> {
                lvFiles.getItems().clear();
                lvFiles.getItems().addAll((File[]) objects[0]);
            }));*/
            //TODO
        } catch (Exception e) {
            Dialogs.showMessageTS("Ошибка инициализации сервера", e.getMessage());
        }

        lvClients.setCellFactory(param -> new ListCell<ClientHandler>() {
            @Override
            protected void updateItem(ClientHandler item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null && empty) {
                    setText(null);
                } else {
                    setText(String.format("%s [%s:%s]", item.getUser().getNick(),
                            item.getSocket().getInetAddress(), item.getSocket().getPort()));
                }
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
                            String info = item.isFile() ? String.valueOf(item.length()) :
                                    "папка, файлов: " + item.listFiles().length;
                            setText(String.format("%s [%s]\n", item.getName(), info));
                        }
                    }
                };
            }
        });

        btnClose.setOnAction(event -> Platform.exit());
    }
}
