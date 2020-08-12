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

import java.net.URL;
import java.util.ResourceBundle;


public class ServerMainWindowController implements Initializable {
    private static final Logger logger = LogManager.getLogger(ServerMainWindowController.class);
    @FXML
    private ListView<FileInfo> lvFiles;
    @FXML
    private Label lblStatus;
    @FXML
    private ListView<ClientHandler> lvClients;
    @FXML
    private Button btnClose;

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


        lvFiles.setCellFactory(new Callback<ListView<FileInfo>, ListCell<FileInfo>>() {
            @Override
            public ListCell<FileInfo> call(ListView<FileInfo> param) {
                return new ListCell<FileInfo>() {
                    @Override
                    protected void updateItem(FileInfo item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item == null || empty) {
                            setText(null);
                        } else {
                            String fileInfo = String.format("%s [%s]", item.getPath(),
                                    FileInfoCollector.formatSize(item.getLength()));
                            String folderInfo = String.format("%s [папок: %d, файлов: %d; %s]",
                                    item.getPath().getFileName(), item.getFoldersCount(), item.getFilesCount(),
                                    FileInfoCollector.formatSize(item.getLength()));
                            setText(item.isFolder() ? folderInfo : fileInfo);
                        }
                    }
                };
            }
        });

        try {
            lvFiles.getItems().addAll(FileSystemRequester.getDetailedPathInfo(FileInfoCollector.MAIN_FOLDER).getFileList());
            FolderWatcherService.getInstance().addChangeListener(changedFolder -> {
                Platform.runLater(() -> {
                    FilesInfo filesInfo = FileSystemRequester.getDetailedPathInfo(FileInfoCollector.MAIN_FOLDER);
                    lvFiles.getItems().clear();
                    lvFiles.getItems().addAll(filesInfo.getFileList());
                    logger.trace("refresh list view on server {}", filesInfo);
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        lvFiles.itemsProperty().addListener((observable, oldValue, newValue) -> {
            logger.trace("list view update {} -> {}", oldValue, newValue);
        });

        btnClose.setOnAction(event -> Platform.exit());
    }
}
