import javafx.scene.control.Hyperlink;
import javafx.scene.layout.FlowPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class NavigationPane {
    private static final Logger logger = LogManager.getLogger(NavigationPane.class);
    private List<NavigationListener> navigationListeners;
    private FlowPane flowPane;
    private Path rootPath;
    private Path relativePath;
    private Path address;

    public NavigationPane(FlowPane flowPane, Path rootPath, Path relativePath) {
        navigationListeners = new ArrayList<>();
        this.flowPane = flowPane;
        this.rootPath = rootPath;
        this.relativePath = relativePath;
        address = Paths.get("");
    }

    public void setAddress(Path path) {
        logger.trace("получен path = '{}'", path);
        path = relativePath.relativize(path);
        logger.trace("обработанный c '{}' path = '{}'", relativePath, path);
        flowPane.getChildren().clear();
        path = rootPath.resolve(path);
        for (int i = 0; i < path.getNameCount(); i++) {
            Hyperlink hyperlink = new Hyperlink(path.getName(i).toString());
            Path navPath = Paths.get("");
            for (int j = 1; j <= i; j++) {
                navPath = navPath.resolve(path.getName(j));
            }
            hyperlink.setUserData(navPath);
            logger.trace("ссылка '{}' = '{}'", i, navPath);
            flowPane.getChildren().addAll(hyperlink, new Hyperlink("\\"));
            hyperlink.setOnAction(event -> {
                navigationListeners.forEach(action -> {
                    Path navigatePath = relativePath.resolve((Path) hyperlink.getUserData());
                    action.navigate(navigatePath);
                    address = navigatePath;
                    logger.debug("адрес = '{}'", address);
                });
            });
        }
    }

    public Path getAddress() {
        return address;
    }

    public void setRelativePath(Path relativePath) {
        this.relativePath = relativePath;
    }

    public void addNavigationListener(NavigationListener navigationListener) {
        navigationListeners.add(navigationListener);
    }

    interface NavigationListener {
        void navigate(Path path);
    }
}
