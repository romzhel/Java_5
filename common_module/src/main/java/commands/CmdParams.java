package commands;

import file_utils.FileInfoCollector;
import file_utils.FolderInfo;
import file_utils.ShareInfo;
import processes.ClientHandler;
import processes.CloudServer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CmdParams {
    private ClientHandler clientHandler;
    private CloudServer cloudServer;
    private FileInfoCollector fileInfoCollector;
    private List<String> stringParams;
    private FolderInfo filesInfo;
    private ShareInfo shareInfo;

    private CmdParams() {
        stringParams = new ArrayList<>();
    }

    public static CmdParams parse(Object... objects) {
        CmdParams result = new CmdParams();
        for (Object object : objects) {
            if (object instanceof ClientHandler) {
                result.clientHandler = (ClientHandler) object;
                result.cloudServer = result.clientHandler.getServer();
                result.fileInfoCollector = result.cloudServer != null ? result.cloudServer.getFileInfoCollector() : null;
            } else if (object instanceof String) {
                result.stringParams.add(object.toString());
            } else if (object instanceof List) {
                result.stringParams.addAll((List<String>) object);
            } else if (object instanceof String[]) {
                result.stringParams = Arrays.asList((String[]) object);
            } else if (object instanceof FolderInfo) {
                result.filesInfo = (FolderInfo) object;
            } else if (object instanceof Path) {
                result.stringParams.add(object.toString());
            } else if (object instanceof ShareInfo) {
                result.shareInfo = (ShareInfo) object;
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

    public FileInfoCollector getFileInfoCollector() {
        return fileInfoCollector;
    }

    public List<String> getStringParams() {
        return stringParams;
    }

    public FolderInfo getFilesInfo() {
        return filesInfo;
    }

    public ShareInfo getShareInfo() {
        return shareInfo;
    }

    @Override
    public String toString() {
        return "commands.CmdParams{" +
                "clientHandler=" + clientHandler +
                ", stringParams=" + stringParams +
                ", filesInfo=" + filesInfo +
                '}';
    }
}
