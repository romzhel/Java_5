import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CmdParams {
    private ClientHandler clientHandler;
    private CloudServer cloudServer;
    private FileInfoCollector fileInfoCollector;
    private List<String> stringParams;
    private FilesInfo filesInfo;

    private CmdParams() {
        stringParams = new ArrayList<>();
    }

    public static CmdParams parse(Object... objects) {
        CmdParams result = new CmdParams();
        for (Object object : objects) {
            if (object instanceof ClientHandler) {
                result.clientHandler = (ClientHandler) object;
                result.cloudServer = result.clientHandler.getServer();
                result.fileInfoCollector = result.cloudServer != null ? result.cloudServer.getFilesSharing() : null;
            } else if (object instanceof String) {
                result.stringParams.add(object.toString());
            } else if (object instanceof List) {
                result.stringParams.addAll((List<String>) object);
            } else if (object instanceof String[]) {
                result.stringParams = Arrays.asList((String[]) object);
            } else if (object instanceof FilesInfo) {
                result.filesInfo = (FilesInfo) object;
            } else if (object instanceof Path) {
                result.stringParams.add(object.toString());
            } else {
                throw new RuntimeException("Неизвестный тип параметра команды " + object + "\n\n");
            }
        }

        return result;
    }

    public ClientHandler getClientHandler() {
        return clientHandler;
    }

    public CloudServer getCloudServer() {
        return cloudServer;
    }

    public FileInfoCollector getFileSharing() {
        return fileInfoCollector;
    }

    public List<String> getStringParams() {
        return stringParams;
    }

    public FilesInfo getFilesInfo() {
        return filesInfo;
    }

    @Override
    public String toString() {
        return "CmdParams{" +
                "clientHandler=" + clientHandler +
                ", cloudServer=" + cloudServer +
                ", fileSharing=" + fileInfoCollector +
                ", stringParams=" + stringParams +
                ", filesInfo=" + filesInfo +
                '}';
    }
}
