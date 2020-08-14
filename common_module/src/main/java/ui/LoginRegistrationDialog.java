package ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class LoginRegistrationDialog {
    public TextField tfNick;
    public TextField tfLogin;
    public TextField tfPassword;
    public TextField tfPasswordConfirm;
    public Label lblNick;
    public Label lblLogin;
    public Label lblPass;
    public Label lblPassConf;
    public Button btnOk;
    public Button btnCancel;
    private Stage stage;
    private boolean isCancelled;

    public LoginRegistrationDialog() throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/login_registration_dialog.fxml"));
        fxmlLoader.setController(this);
        AnchorPane root = fxmlLoader.load();

        stage = new Stage();
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);

        stage.setOnCloseRequest(event -> {
            event.consume();
            isCancelled = true;
            stage.close();
        });
        btnCancel.setOnAction(event -> {
            isCancelled = true;
            stage.close();
        });
        tfPassword.setOnAction(event -> checkData(tfLogin, tfPassword));
        tfPasswordConfirm.setOnAction(event -> checkData(tfNick, tfLogin, tfPassword, tfPasswordConfirm));
        btnOk.setOnAction(event -> checkData(tfLogin, tfPassword));
    }

    public String[] getLoginData() throws Exception {
        stage.setTitle("Ввод учётных данных");
        double[] positionsY = new double[]{8, 8, 39, 39};
        Node[] visibleNode = new Node[]{lblLogin, tfLogin, lblPass, tfPassword};
        Node[] unVisibleNode = new Node[]{lblNick, tfNick, lblPassConf, tfPasswordConfirm};

        setVisible(false, unVisibleNode);
        for (int i = 0; i < visibleNode.length; i++) {
            visibleNode[i].setLayoutY(positionsY[i]);
        }

        stage.setHeight(160);
        btnOk.requestFocus();
        stage.showAndWait();

        if (isCancelled) {
            throw new RuntimeException("Отмена");
        }

        checkData(tfLogin, tfPassword);
        return new String[]{tfLogin.getText(), tfPassword.getText()};
    }

    public String[] getRegistrationData() throws Exception {
        stage.setTitle("Регистрация");
        btnOk.requestFocus();
        stage.showAndWait();

        if (isCancelled) {
            throw new RuntimeException("Отмена");
        }

        checkData(tfNick, tfLogin, tfPassword, tfPasswordConfirm);
        return new String[]{tfNick.getText(), tfLogin.getText(), tfPassword.getText()};
    }

    private void checkData(TextField... textFields) {
        for (TextField tf : textFields) {
            if (tf.getText().trim().length() < 1) {
                Dialogs.showMessageTS("Ошибка ввода", "Не допускаются пустые значения");
                return;
            }
        }

        if (textFields.length == 4 && !tfPassword.getText().equals(tfPasswordConfirm.getText())) {
            Dialogs.showMessageTS("Ошибка ввода", "Пароли не совпадают");
            return;
        }

        stage.close();
    }

    private void setVisible(boolean visible, Node... nodes) {
        for (Node node : nodes) {
            node.setVisible(visible);
        }
    }
}
