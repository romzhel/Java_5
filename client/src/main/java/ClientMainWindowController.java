import auth_service.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
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
    public ListView<FileInfo> lvServerFiles;
    public ListView<FileInfo> lvClientFiles;
    public Button btnDownload;
    public Button btnUpload;
    public Button btnClose;
    public Button btnLogin;
    public Button btnRegistration;
    public Button btnCreateFolder;
    public Button btnBrowseClient;
    public FlowPane fpServerNavigationPane;
    public FlowPane fpClientNavigationPane;
    private ClientHandler clientHandler;
    private ExecutorService executorService;
    private Socket socket;
    private NavigationPane clientNavigationPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            executorService = Executors.newSingleThreadExecutor();
            socket = new Socket("localhost", 8189);
            clientHandler = new ClientHandler(null, socket);
            clientHandler.setMessageListener(message -> {
                try {
                    Command.valueOf(message).execute(CmdParams.parse(clientHandler));
                } catch (Exception e) {
                    logger.error("command executing error {}", e.getMessage(), e);
                }
            });
            executorService.submit(clientHandler);
        } catch (Exception e) {
//            logger.fatal("Не удалось запустить процессы обмена с сервером {}", e.getMessage(), e);
        }

        initGui();
    }

    private void initGui() {
        NavigationPane navigationPane = new NavigationPane(fpServerNavigationPane, Paths.get("..."), Paths.get(""));
        navigationPane.addNavigationListener(path -> {
            logger.trace("request navigation to {}", path);
            try {
                Command.OUT_SEND_FILE_LIST_REQUEST.execute(CmdParams.parse(clientHandler, path));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Command.IN_USER_DATA.addCommandResultListener(objects -> {
            User user = (User) objects[0];
            Platform.runLater(() -> {
                clientHandler.setUser(user);
                btnLogin.setVisible(false);
                btnRegistration.setVisible(false);
            });
            navigationPane.setRelativePath(Paths.get(user.getNick()));
        });

        Callback<ListView<FileInfo>, ListCell<FileInfo>> listViewListCellCallback = new Callback<ListView<FileInfo>, ListCell<FileInfo>>() {
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
        };

        lvServerFiles.setCellFactory(listViewListCellCallback);
        lvServerFiles.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                fileFolderRequest();
            }
        });

        lvClientFiles.setCellFactory(listViewListCellCallback);


        Command.IN_FILES_LIST.addCommandResultListener(objects -> Platform.runLater(() -> {
            Path userPath = Paths.get(clientHandler.getUser().getNick());
            FilesInfo filesInfo = (FilesInfo) objects[0];
            lvServerFiles.getItems().clear();
            lvServerFiles.getItems().addAll(filesInfo.getFileList());
            navigationPane.setAddress(filesInfo.getFolder());
        }));

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

    @FXML
    private void fileFolderRequest() {
        FileInfo selectedFileInfo = lvServerFiles.getSelectionModel().getSelectedItem();
        if (selectedFileInfo == null) {
            throw new RuntimeException("Не выбран файл/папка");
        }

        try {
            if (selectedFileInfo.isFolder()) {
                logger.trace("выбрана для навигации папка {}", selectedFileInfo);
                Command.OUT_SEND_FILE_LIST_REQUEST.execute(CmdParams.parse(clientHandler, selectedFileInfo.getPath()));
            } else {
                logger.trace("выбран для загрузки файл {}", selectedFileInfo);
                Command.OUT_DOWNLOAD_REQUEST.execute(CmdParams.parse(clientHandler, selectedFileInfo.getPath()));
            }
        } catch (Exception e) {
            logger.error("Ошибка скачивания файла/навигации {}", e.getMessage());
            Dialogs.showMessageTS("Скачивание файла/навигация", "Ошибка:\n\n" + e.getMessage());
        }
    }

    public void delete() {
        FileInfo selectedFileInfo = lvServerFiles.getSelectionModel().getSelectedItem();
        if (selectedFileInfo == null) {
            throw new RuntimeException("Не выбран файл/папка");
        }

        logger.trace("выбран элемент для удаления {}", selectedFileInfo);
        try {
            Command.OUT_DELETE_ITEM.execute(CmdParams.parse(clientHandler, selectedFileInfo.getPath()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void switchClientBrowserView() {
        try {
            Stage stage = (Stage) btnBrowseClient.getScene().getWindow();
            if (stage.getWidth() < 1000) {
                stage.setWidth(1050);

                if (clientNavigationPane == null) {
                    Path defaultPath = FileInfoCollector.CLIENT_FOLDER;
                    clientNavigationPane = new NavigationPane(fpClientNavigationPane, Paths.get("..."), defaultPath);
                    clientNavigationPane.setAddress(Paths.get(""));

                    lvClientFiles.getItems().clear();
                    lvClientFiles.getItems().addAll(FileSystemRequester.getDetailedPathInfo(defaultPath, defaultPath).getFileList());
                    clientNavigationPane.addNavigationListener(path -> {
                        logger.trace("навигация по клиенту {}", path);
                        lvClientFiles.getItems().clear();
                        FilesInfo filesInfo = FileSystemRequester.getDetailedPathInfo(path, defaultPath);
                        logger.debug("получен список файлов {}", filesInfo);
                        lvClientFiles.getItems().addAll(filesInfo.getFileList());
                        clientNavigationPane.setAddress(filesInfo.getFolder());
                    });

                    lvClientFiles.setOnMouseClicked(event -> {
                        if (event.getClickCount() == 2) {
                            FileInfo fileInfo = lvClientFiles.getSelectionModel().getSelectedItem();
                            logger.debug("навигация по списку файлов, переход на {}", fileInfo.getPath());
                            if (fileInfo != null && fileInfo.isFolder()) {
                                lvClientFiles.getItems().clear();
                                FilesInfo filesInfo = FileSystemRequester.getDetailedPathInfo(fileInfo.getPath(), defaultPath);
                                logger.debug("получен список файлов {}", filesInfo);
                                lvClientFiles.getItems().addAll(filesInfo.getFileList());
                                clientNavigationPane.setAddress(fileInfo.getPath());
                            }

                        }
                    });

                    FolderWatcherService.getInstance().addChangeListener(FileSystemChangeListener.create()
                            .setMonitoredFolderPath(defaultPath)
                            .setRelativesPath(defaultPath)
                            .setChangeListener(changedFolder -> {
                                logger.debug("изменения в папке '{}', текущая папка '{}'", changedFolder, clientNavigationPane.getAddress());
                                if (clientNavigationPane.getAddress().equals(changedFolder)) {
                                    Platform.runLater(() -> {
                                        lvClientFiles.getItems().clear();
                                        lvClientFiles.getItems().addAll(FileSystemRequester.getDetailedPathInfo(changedFolder,
                                                defaultPath).getFileList());
                                    });
                                }
                            })
                    );
                }
            } else {
                stage.setWidth(605);


            }
        } catch (Exception e) {
            e.printStackTrace();
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
