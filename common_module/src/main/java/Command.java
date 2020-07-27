import auth_service.User;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public enum Command {

    DOWNLOAD_REQUEST {
        void execute(CommandParameters cmdParams) throws Exception {
            DataOutputStream dos = cmdParams.getClientHandler().getDataOutputStream();
            dos.writeUTF(RECEIVE_DOWNLOAD_REQUEST_AND_SEND_FILE.name());
            dos.writeUTF(cmdParams.getFile().toString());
        }
    },
    RECEIVE_DOWNLOAD_REQUEST_AND_SEND_FILE {
        void execute(CommandParameters cmdParams) throws Exception {
            DataInputStream dis = cmdParams.getClientHandler().getDataInputStream();
            File requestedFile = new File(dis.readUTF());
            if (requestedFile.exists()) {
                new FileHandler().sendFile(requestedFile, cmdParams.getClientHandler());
            }
        }
    },
    RECEIVE_FILE {
        void execute(CommandParameters cmdParams) throws Exception {
            File folder = cmdParams.getFileSharing() != null ? cmdParams.getFileSharing().getShareFolder() : null;
            new FileHandler().receiveFile(folder, cmdParams.getClientHandler());
        }
    },
    SEND_FILE {
        void execute(CommandParameters cmdParams) throws Exception {
            if (cmdParams.getFile().exists()) {
                new FileHandler().sendFile(cmdParams.getFile(), cmdParams.getClientHandler());
            }
        }
    },
    SEND_FILES_LIST {
        void execute(CommandParameters cmdParams) throws Exception {
            DataOutputStream dos = cmdParams.getClientHandler().getDataOutputStream();
            dos.writeUTF(FILES_LIST.name());
            dos.writeInt(cmdParams.getFileArray().length);
            for (int i = 0; i < cmdParams.getFileArray().length; i++) {
                dos.writeUTF(cmdParams.getFileArray()[i].toString());
            }
            dos.flush();
        }
    },
    FILES_LIST {
        void execute(CommandParameters cmdParams) throws Exception {
            DataInputStream dis = cmdParams.getClientHandler().getDataInputStream();
            List<File> fileList = new ArrayList<>();
            int count = dis.readInt();
            for (int i = 0; i < count; i++) {
                fileList.add(new File(dis.readUTF()));
            }

            if (commandResult != null) {
                commandResult.send(fileList.toArray());
            }
        }
    },
    OK {
        void execute(CommandParameters cmdParams) throws Exception {
            System.out.println("/ok");
        }
    },
    SEND_EXIT {
        void execute(CommandParameters cmdParams) throws Exception {
            DataOutputStream dos = cmdParams.getClientHandler().getDataOutputStream();
            dos.writeUTF(EXIT.name());
            dos.flush();
            cmdParams.getClientHandler().close();
        }
    },
    EXIT {
        void execute(CommandParameters cmdParams) throws Exception {
            DataOutputStream dos = cmdParams.getClientHandler().getDataOutputStream();
            cmdParams.getClientHandler().close();
        }
    },
    SEND_LOGIN_DATA {
        void execute(CommandParameters cmdParams) throws Exception {
            DataOutputStream dos = cmdParams.getClientHandler().getDataOutputStream();
            dos.writeUTF(LOGIN_DATA.name());
            dos.writeUTF(cmdParams.getStringParams()[0]);
            dos.writeUTF(cmdParams.getStringParams()[1]);
        }
    },
    LOGIN_DATA {
        void execute(CommandParameters cmdParams) throws Exception {
            DataInputStream dis = cmdParams.getClientHandler().getDataInputStream();
            DataOutputStream dos = cmdParams.getClientHandler().getDataOutputStream();
            User user = null;
            try {
                user = cmdParams.getCloudServer().getAuthService().getNickByLoginPass(dis.readUTF(), dis.readUTF());
                System.out.println(user);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                SEND_ERROR.execute(CommandParameters.parse(cmdParams.getClientHandler(), e.getMessage()));
            }
            cmdParams.getClientHandler().setUser(user);
            dos.writeUTF(USER_DATA.name());
            dos.writeInt(user.getId());
            dos.writeUTF(user.getNick());

            if (commandResult != null) {
                commandResult.send(user);
            }
        }
    },
    USER_DATA {//receive on client

        void execute(CommandParameters cmdParams) throws Exception {
            DataInputStream dis = cmdParams.getClientHandler().getDataInputStream();
            User user = new User(dis.readInt(), dis.readUTF());
            if (commandResult != null) {
                commandResult.send(user);
            }
        }
    },
    SEND_ERROR {
        void execute(CommandParameters cmdParams) throws Exception {
            DataOutputStream dos = cmdParams.getClientHandler().getDataOutputStream();

        }
    },
    ERROR {
        void execute(CommandParameters cmdParams) throws Exception {
            DataOutputStream dos = cmdParams.getClientHandler().getDataOutputStream();

        }
    };

    CommandResult commandResult;

    Command() {
        commandResult = null;
    }

    abstract void execute(CommandParameters commandParameters) throws Exception;

    public void setCommandResult(CommandResult commandResult) {
        this.commandResult = commandResult;
    }
}
