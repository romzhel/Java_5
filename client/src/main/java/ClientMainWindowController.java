import auth_service.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.FlowPane;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientMainWindowController implements Initializable {
    private static final Logger logger = LogManager.getLogger(ClientMainWindowController.class);
    private ClientHandler clientHandler;
    private ExecutorService executorService;
    private Socket socket;

    @FXML
    private ListView<FileInfo> lvFiles;
    @FXML
    private Button btnDownload;
    @FXML
    private Button btnUpload;
    @FXML
    private Button btnClose;
    @FXML
    private Button btnLogin;
    @FXML
    private Button btnRegistration;
    @FXML
    private Button btnCreateFolder;
    @FXML
    private FlowPane fpNavigationPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initGui();

        try {
            executorService = Executors.newSingleThreadExecutor();
            socket = new Socket("localhost", 8189);
            clientHandler = new ClientHandler(null, socket);
            clientHandler.setMessageListener(message -> {
                try {
                    Command.valueOf(message).execute(CmdParams.parse(clientHandler));
                } catch (Exception e) {

                }
            });
            executorService.submit(clientHandler);
//            Command.OUT_SEND_FILE_LIST_REQUEST.execute(CmdParams.parse(clientHandler, ));
        } catch (Exception e) {

        }
    }

    private void initGui() {
        Command.IN_USER_DATA.addCommandResultListener(objects -> {
            User user = (User) objects[0];
            Platform.runLater(() -> {
                clientHandler.setUser(user);
                btnLogin.setVisible(false);
                btnRegistration.setVisible(false);
            });
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
                            String details = item.isFolder() ? "папка" : FileInfoCollector.formatSize(item.getLength());
                            setText(String.format("%s [%s]", item.getPath().getFileName().toString(), details));
                        }
                    }
                };
            }
        });

        lvFiles.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                FileInfo selectedFileInfo = lvFiles.getSelectionModel().getSelectedItem();
                Path selectedPathForRequest = clientHandler.getSelectedFolder().resolve(selectedFileInfo.getPath());
                logger.trace("выбран элемент {}, папка = {}", selectedFileInfo, selectedFileInfo.isFolder());
                if (selectedFileInfo.isFolder()) {
                    try {
                        Command.OUT_SEND_FILE_LIST_REQUEST.execute(CmdParams.parse(
                                clientHandler, selectedFileInfo.getPath()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    downloadFile();
                }
            }
        });

        NavigationPane navigationPane = new NavigationPane(fpNavigationPane, Paths.get("..."));
        navigationPane.addNavigationListener(path -> {
            logger.trace("request navigation to {}", path);
            Path fullPath = Paths.get(clientHandler.getUser().getNick()).resolve(path);
            try {
                Command.OUT_SEND_FILE_LIST_REQUEST.execute(CmdParams.parse(clientHandler, fullPath));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Command.IN_FILES_LIST.addCommandResultListener(objects -> Platform.runLater(() -> {
            Path userPath = Paths.get(clientHandler.getUser().getNick());
            FilesInfo filesInfo = (FilesInfo) objects[0];
            lvFiles.getItems().clear();
            lvFiles.getItems().addAll(filesInfo.getFileList());
            navigationPane.setAddress(userPath.relativize(filesInfo.getFolder()));
        }));

        btnDownload.setOnAction(event -> {
            if (lvFiles.getSelectionModel().getSelectedItem() != null) {
                downloadFile();
            } else {
                Dialogs.showMessage("Загрузка файла на сервер", "Выберите файл в списке");
            }
        });

        btnUpload.setOnAction(event -> {
            File uploadedFile = Dialogs.selectAnyFileTS(null, "Выбор файла для загрузки на сервер", null);
            if (uploadedFile == null || !uploadedFile.exists()) {
                return;
            }
            try {
                Command.OUT_SEND_FILE.execute(CmdParams.parse(clientHandler, uploadedFile.toPath()));
            } catch (Exception e) {
                Dialogs.showMessageTS("Загрузка файла на сервер", "Ошибка:\n\n" + e.getMessage());
            }
        });

        btnCreateFolder.setOnAction(event -> {
            String folderName = Dialogs.TextInputDialog("Добавление папки", "Введите название папки", "");
            if (!folderName.isEmpty()) {
                try {
                    Command.OUT_CREATE_FOLDER.execute(CmdParams.parse(clientHandler, folderName));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        btnLogin.setOnAction(event -> {
            try {
                Command.OUT_SEND_LOGIN_DATA.execute(CmdParams.parse(clientHandler, Dialogs.getLoginData()));
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        });

        btnClose.setOnAction(event -> {
            close();
        });
    }

    private void downloadFile() {
        FileInfo selectedFileInfo = lvFiles.getSelectionModel().getSelectedItem();
        try {
            if (selectedFileInfo.isFolder()) {
                logger.trace("выбран элемент {}, папка = {}", selectedFileInfo, selectedFileInfo.isFolder());
                Command.OUT_SEND_FILE_LIST_REQUEST.execute(CmdParams.parse(clientHandler, selectedFileInfo.getPath()));
            } else {
                logger.trace("выбран для загрузки файл {}", selectedFileInfo);
            }
            Command.OUT_DOWNLOAD_REQUEST.execute(CmdParams.parse(clientHandler, selectedFileInfo.getPath()));
        } catch (Exception e) {
            Dialogs.showMessageTS("Скачивание файла/навигация", "Ошибка:\n\n" + e.getMessage());
        }
    }

    public void close() {
        try {
            Command.OUT_SEND_EXIT.execute(CmdParams.parse(clientHandler));
        } catch (Exception e) {

        }
        executorService.shutdownNow();
        Platform.exit();
    }
}
