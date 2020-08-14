import auth_service.User;
import commands.Command;
import javafx.application.Application;
import javafx.application.Platform;
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
        Scene scene = new Scene(root, 590, 400);
        primaryStage.setScene(scene);
        String titleTemplate = "Клиент файлообменника (пользователь: %s)";
        primaryStage.setTitle(String.format(titleTemplate, User.UNREGISTERED.getNick()));
        Command.IN_USER_DATA.addCommandResultListener(objects -> {
            User user = (User) objects[0];
            Platform.runLater(() -> {
                primaryStage.setTitle(String.format(titleTemplate, user.getNick()));
            });
        });
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> ((ClientMainWindowController) fxmlLoader.getController()).close());
    }
}
