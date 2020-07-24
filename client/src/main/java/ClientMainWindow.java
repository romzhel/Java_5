import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class ClientMainWindow extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/clientWindow.fxml"));

        AnchorPane root = fxmlLoader.load();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Клиент файлообменника");
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> ((ClientMainWindowController) fxmlLoader.getController()).close());
    }
}
