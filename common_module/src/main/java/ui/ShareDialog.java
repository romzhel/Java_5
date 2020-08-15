package ui;

import file_utils.ShareInfo;
import file_utils.UserShareInfo;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Paths;

public class ShareDialog {
    private static final Logger logger = LogManager.getLogger(ShareDialog.class);
    public CheckBox cbxAllRead;
    public CheckBox cbxAllWrite;
    public CheckBox cbxRegRead;
    public CheckBox cbxRegWrite;
    public CheckBox cbxUserWrite;
    public TextField tfAddUser;
    public ListView<UserShareInfo> lvUsersShare;
    public Button btnAddUser;
    public Button btnDeleteUser;
    public Button btnOk;
    public Button btnCancel;
    private String TITLE_TEMPLATE = "Настройка доступа для %s";
    private Stage stage;
    private byte mainFlags;
    private boolean isCancelled;

    public ShareDialog() throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/share_dialog.fxml"));
        fxmlLoader.setController(this);
        AnchorPane root = fxmlLoader.load();

        stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Настройка доступа");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
    }

    public ShareInfo getShareInfo(ShareInfo shareInfo) {
        logger.info("входные данные {}", shareInfo);
        stage.setTitle(String.format(TITLE_TEMPLATE, Paths.get(shareInfo.getFileName()).getFileName()));
        mainFlags = shareInfo.getMainFlags();
        addChBoxListener(cbxAllRead, cbxAllWrite, (byte) 1, (byte) 2);
        addChBoxListener(cbxRegRead, cbxRegWrite, (byte) 4, (byte) 8);
        addChBoxSelectListener(cbxAllRead, (byte) 1);
        addChBoxSelectListener(cbxAllWrite, (byte) 2);
        addChBoxSelectListener(cbxRegRead, (byte) 4);
        addChBoxSelectListener(cbxRegWrite, (byte) 8);

        lvUsersShare.setCellFactory(new Callback<ListView<UserShareInfo>, ListCell<UserShareInfo>>() {
            @Override
            public ListCell<UserShareInfo> call(ListView<UserShareInfo> param) {
                return new ListCell<UserShareInfo>() {
                    @Override
                    protected void updateItem(UserShareInfo item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item == null || empty) {
                            setText(null);
                        } else {
                            setText(String.format("%s [-%s%s-]", item.getUser(),
                                    (item.getFlags() & 1) > 0 ? "r" : "-",
                                    (item.getFlags() & 2) > 0 ? "w" : "-"));
                        }
                    }
                };
            }
        });

        lvUsersShare.getItems().addAll(shareInfo.getUserShareInfoList());

        btnAddUser.setOnAction(event -> {
            if (!tfAddUser.getText().isEmpty()) {
                for (UserShareInfo usi : shareInfo.getUserShareInfoList()) {
                    if (usi.getUser().equals(tfAddUser.getText())) {
                        usi.setFlags((byte) (cbxUserWrite.isSelected() ? 3 : 1));

                        tfAddUser.setText("");
                        cbxUserWrite.setSelected(false);
                        return;
                    }
                }

                UserShareInfo usi = new UserShareInfo(tfAddUser.getText(), (byte) (cbxUserWrite.isSelected() ? 3 : 1));
                tfAddUser.setText("");
                cbxUserWrite.setSelected(false);

                shareInfo.addUserShareInfo(usi);
                shareInfo.getAddedItems().add(usi);
                lvUsersShare.getItems().add(usi);
            }
        });

        btnDeleteUser.setOnAction(event -> {
            UserShareInfo usi = lvUsersShare.getSelectionModel().getSelectedItem();
            if (usi != null) {
                lvUsersShare.getItems().remove(usi);
                shareInfo.getDeletedItems().add(usi);
            }
        });

        stage.setOnCloseRequest(event -> {
            event.consume();
            isCancelled = true;
            stage.close();
        });

        btnCancel.setOnAction(event -> {
            isCancelled = true;
            stage.close();
        });

        btnOk.setOnAction(event -> stage.close());

        stage.showAndWait();

        if (isCancelled) {
            throw new RuntimeException("Отмена изменения параметров общего доступа");
        }

        shareInfo.setMainFlags(mainFlags);
        return shareInfo;
    }

    private void addChBoxListener(CheckBox cbxMain, CheckBox cbxSecondary, byte flagMain, byte flagSecondary) {
        cbxMain.selectedProperty().addListener((observable, oldValue, newValue) -> {
            cbxSecondary.setDisable(!newValue);
            if (!newValue) {
                cbxSecondary.setSelected(false);
            }
        });
        cbxMain.setSelected((mainFlags & flagMain) > 0);
        cbxSecondary.setSelected((mainFlags & flagSecondary) > 0);
    }

    private void addChBoxSelectListener(CheckBox checkBox, byte flag) {
        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            mainFlags = (byte) (newValue ? mainFlags | flag : mainFlags & ~flag);
        });
    }
}
