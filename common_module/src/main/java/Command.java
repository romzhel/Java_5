import auth_service.User;
import org.apache.logging.log4j.LogManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public enum Command {

    OUT_DOWNLOAD_REQUEST {
        void execute(CmdParams cmdParams) throws Exception {
            if (cmdParams.getStringParams().size() != 1) {
                throw new RuntimeException("Неверное количество параметров");
            }

            LogManager.getLogger(OUT_DOWNLOAD_REQUEST.name()).trace(cmdParams.getStringParams().get(0));
            DataOutputStream dos = cmdParams.getClientHandler().getDataOutputStream();
            dos.writeUTF(IN_DOWNLOAD_REQUEST_AND_SEND_FILE.name());
            dos.writeUTF(cmdParams.getStringParams().get(0));
        }
    },
    IN_DOWNLOAD_REQUEST_AND_SEND_FILE {
        void execute(CmdParams cmdParams) throws Exception {
            DataInputStream dis = cmdParams.getClientHandler().getDataInputStream();
            String requestedFileName = dis.readUTF();
            File requestedFile = FileInfoCollector.MAIN_FOLDER
                    .resolve(requestedFileName)
                    .toFile();
            LogManager.getLogger(IN_DOWNLOAD_REQUEST_AND_SEND_FILE.name()).trace(requestedFile);
            new FileHandler().sendFile(requestedFile, cmdParams.getClientHandler());
        }
    },
    IN_RECEIVE_FILE {
        void execute(CmdParams cmdParams) throws Exception {
            LogManager.getLogger(IN_RECEIVE_FILE.name()).trace(cmdParams);

            Path selectedFolder = cmdParams.getClientHandler().getSelectedFolder();
            Path folderToSave = cmdParams.getCloudServer() != null ?
                    selectedFolder == FileInfoCollector.UP_LEVEL ? Paths.get(cmdParams.getClientHandler().getUser().getNick())
                            .resolve(selectedFolder) : selectedFolder
                    : null;

            Path filePath = new FileHandler().receiveFile(cmdParams.getClientHandler(), folderToSave);
            LogManager.getLogger(IN_RECEIVE_FILE.name()).trace("received file = {}", filePath);
            FileInfoCollector fileInfoCollector = cmdParams.getFileInfoCollector();
            if (fileInfoCollector != null) {
                fileInfoCollector.addNewFile(filePath, cmdParams.getClientHandler());
                commandResultListeners.forEach(action -> action.send(filePath));
            }
            //TODO добавление в базу + рассылка всем задействованным

        }
    },
    OUT_SEND_FILE {
        void execute(CmdParams cmdParams) throws Exception {
            if (cmdParams.getStringParams().size() != 1) {
                throw new RuntimeException("Неверное количество параметров");
            }

            LogManager.getLogger(OUT_SEND_FILE.name()).trace(cmdParams);
            File file = new File(cmdParams.getStringParams().get(0));
            if (file.exists()) {
                new FileHandler().sendFile(file, cmdParams.getClientHandler());
            }
        }
    },
    OUT_SEND_FILE_LIST_REQUEST {
        void execute(CmdParams cmdParams) throws Exception {
            if (cmdParams.getStringParams().size() != 1) {
                throw new RuntimeException("Неверное количество параметров");
            }

            LogManager.getLogger(OUT_SEND_FILE_LIST_REQUEST.name()).trace(cmdParams.getStringParams().get(0));
            DataOutputStream dos = cmdParams.getClientHandler().getDataOutputStream();
            dos.writeUTF(IN_FILE_LIST_REQUEST.name());
            dos.writeUTF(cmdParams.getStringParams().get(0));
            dos.flush();
        }
    },
    IN_FILE_LIST_REQUEST {
        void execute(CmdParams cmdParams) throws Exception {
            DataInputStream dis = cmdParams.getClientHandler().getDataInputStream();
            String requestedFolder = dis.readUTF();
            LogManager.getLogger(IN_FILE_LIST_REQUEST.name()).trace(requestedFolder);

            OUT_SEND_FILE_LIST.execute(CmdParams.parse(cmdParams.getClientHandler(), requestedFolder));
        }
    },
    OUT_SEND_FILE_LIST {
        void execute(CmdParams cmdParams) throws Exception {
            LogManager.getLogger(OUT_SEND_FILE_LIST.name()).trace(cmdParams);
            DataOutputStream dos = cmdParams.getClientHandler().getDataOutputStream();
            dos.writeUTF(IN_FILES_LIST.name());
            FilesInfo filesInfo = cmdParams.getFileInfoCollector().getFilesInfo(cmdParams.getClientHandler(),
                    Paths.get(cmdParams.getStringParams().get(0)));
            LogManager.getLogger(OUT_SEND_FILE_LIST.name()).trace(filesInfo);
            filesInfo.sendTo(cmdParams.getClientHandler());
            dos.flush();
            cmdParams.getClientHandler().setSelectedFolder(filesInfo.getFolder());
        }
    },
    IN_FILES_LIST {
        void execute(CmdParams cmdParams) throws Exception {
            FilesInfo filesInfo = FilesInfo.create().getFrom(cmdParams.getClientHandler());
            cmdParams.getClientHandler().setSelectedFolder(filesInfo.getFolder());
            LogManager.getLogger(IN_FILES_LIST.name()).trace(filesInfo);

            commandResultListeners.forEach(action -> action.send(filesInfo));
        }
    },
    OK {
        void execute(CmdParams cmdParams) throws Exception {
            LogManager.getLogger(OK.name()).trace(cmdParams);
            System.out.println("/ok");
        }
    },
    OUT_SEND_EXIT {
        void execute(CmdParams cmdParams) throws Exception {
            LogManager.getLogger(OUT_SEND_EXIT.name()).trace(cmdParams);
            DataOutputStream dos = cmdParams.getClientHandler().getDataOutputStream();
            dos.writeUTF(IN_EXIT.name());
            dos.flush();
            cmdParams.getClientHandler().close();
        }
    },
    IN_EXIT {
        void execute(CmdParams cmdParams) throws Exception {
            LogManager.getLogger(IN_EXIT.name()).trace(cmdParams);
            cmdParams.getClientHandler().close();
        }
    },
    OUT_SEND_LOGIN_DATA {
        void execute(CmdParams cmdParams) throws Exception {
            LogManager.getLogger(OUT_SEND_LOGIN_DATA.name()).trace(cmdParams);
            DataOutputStream dos = cmdParams.getClientHandler().getDataOutputStream();
            dos.writeUTF(IN_LOGIN_DATA_CHECK_AND_SEND_BACK_NICK.name());
            dos.writeUTF(cmdParams.getStringParams().get(0));
            dos.writeUTF(cmdParams.getStringParams().get(1));
        }
    },
    IN_LOGIN_DATA_CHECK_AND_SEND_BACK_NICK {
        void execute(CmdParams cmdParams) throws Exception {
            DataInputStream dis = cmdParams.getClientHandler().getDataInputStream();
            DataOutputStream dos = cmdParams.getClientHandler().getDataOutputStream();
            User user = null;
            try {
                String login = dis.readUTF();
                String pass = dis.readUTF();
                LogManager.getLogger(IN_LOGIN_DATA_CHECK_AND_SEND_BACK_NICK.name()).trace("input data = " + login + "," + pass);
                user = cmdParams.getClientHandler().getServer().getAuthService().getNickByLoginPass(login, pass);
                LogManager.getLogger(IN_LOGIN_DATA_CHECK_AND_SEND_BACK_NICK.name()).trace(user);
                cmdParams.getClientHandler().setUser(user);
                dos.writeUTF(IN_USER_DATA.name());
                dos.writeInt(user.getId());
                dos.writeUTF(user.getNick());

                for (ResultListener rs : commandResultListeners) {
                    rs.send(user);
                }
            } catch (Exception e) {
                LogManager.getLogger(IN_LOGIN_DATA_CHECK_AND_SEND_BACK_NICK.name()).trace(e.getMessage());
                OUT_SEND_ERROR.execute(CmdParams.parse(cmdParams.getClientHandler(), new String[]{e.getMessage()}));
            }
        }
    },
    IN_USER_DATA {
        void execute(CmdParams cmdParams) throws Exception {
            DataInputStream dis = cmdParams.getClientHandler().getDataInputStream();
            User user = new User(dis.readInt(), dis.readUTF());
            LogManager.getLogger(IN_USER_DATA.name()).trace(user);
            for (ResultListener rs : commandResultListeners) {
                rs.send(user);
            }
        }
    },
    OUT_SEND_ERROR {
        void execute(CmdParams cmdParams) throws Exception {
            LogManager.getLogger(OUT_SEND_ERROR.name()).trace(cmdParams);
            DataOutputStream dos = cmdParams.getClientHandler().getDataOutputStream();
            dos.writeUTF(IN_ERROR.name());
            dos.writeUTF(cmdParams.getStringParams().get(0));
            dos.flush();
        }
    },
    IN_ERROR {
        void execute(CmdParams cmdParams) throws Exception {
            DataInputStream dis = cmdParams.getClientHandler().getDataInputStream();
            String errorDetails = dis.readUTF();
            Dialogs.showMessageTS("Ошибка", errorDetails);
            LogManager.getLogger(IN_ERROR.name()).trace(errorDetails);
            dis.skip(dis.available());
        }
    },
    OUT_CREATE_FOLDER {
        void execute(CmdParams cmdParams) throws Exception {
            LogManager.getLogger(OUT_CREATE_FOLDER.name()).trace(cmdParams);
            DataOutputStream dos = cmdParams.getClientHandler().getDataOutputStream();
            dos.writeUTF(IN_CREATE_FOLDER.name());
            dos.writeUTF(cmdParams.getStringParams().get(0));
            dos.flush();
        }
    },
    IN_CREATE_FOLDER {
        void execute(CmdParams cmdParams) throws Exception {
            LogManager.getLogger(IN_CREATE_FOLDER.name()).trace(cmdParams);
            FileInfoCollector fileInfoCollector = cmdParams.getFileInfoCollector();
            if (fileInfoCollector != null) {
                Path folderPath = new FileHandler().createFolder(cmdParams.getClientHandler());
                fileInfoCollector.addNewFile(folderPath, cmdParams.getClientHandler());
                LogManager.getLogger(IN_CREATE_FOLDER.name()).trace("папка {} добавлена", folderPath);
                commandResultListeners.forEach(action -> action.send(folderPath));
            }
        }
    };

    List<ResultListener> commandResultListeners;

    Command() {
        commandResultListeners = new ArrayList<>();
    }

    abstract void execute(CmdParams commandParameters) throws Exception;

    public void addCommandResultListener(ResultListener commandResult) {
        if (commandResult != null) {
            commandResultListeners.add(commandResult);
        }
    }

    interface ResultListener {
        void send(Object... objects);
    }
}
