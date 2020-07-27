import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class LoginDialog {

    public LoginDialog(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/loginWindow.fxml"));

        VBox root = fxmlLoader.load();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Авторизация");
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();
    }
}
