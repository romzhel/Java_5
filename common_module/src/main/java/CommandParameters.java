import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CommandParameters {
    private ClientHandler clientHandler;
    private List<File> fileList;
    private CloudServer cloudServer;
    private FileSharing fileSharing;
    private String[] stringParams;

    private CommandParameters() {
        fileList = new ArrayList<>();
    }

    public static CommandParameters parse(Object... objects) {
        CommandParameters result = new CommandParameters();
        for (Object object : objects) {
            if (object instanceof ClientHandler) {
                result.clientHandler = (ClientHandler) object;
            } else if (object instanceof File) {
                result.fileList.add((File) object);
            } else if (object instanceof CloudServer) {
                result.cloudServer = (CloudServer) object;
            } else if (object instanceof List) {
                result.fileList.addAll((List<File>) object);
            } else if (object instanceof FileSharing) {
                result.fileSharing = (FileSharing) object;
            } else if (object instanceof String[]) {
                result.stringParams = (String[]) object;
            }
        }

        return result;
    }

    public ClientHandler getClientHandler() {
        return clientHandler;
    }

    public File[] getFileArray() {
        return fileList.toArray(new File[]{});
    }

    public File getFile() {
        return fileList.get(0);
    }

    public CloudServer getCloudServer() {
        return cloudServer;
    }

    public FileSharing getFileSharing() {
        return fileSharing;
    }

    public String[] getStringParams() {
        return stringParams;
    }
}
