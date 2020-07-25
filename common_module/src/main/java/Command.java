import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public enum Command {

    DOWNLOAD_REQUEST {
        void execute(CommandParameters cmdParams) {
            DataOutputStream dos = cmdParams.getClientHandler().getDataOutputStream();
            try {
                dos.writeUTF(RECEIVE_DOWNLOAD_REQUEST_AND_SEND_FILE.name());
                dos.writeUTF(cmdParams.getFile().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    },
    RECEIVE_DOWNLOAD_REQUEST_AND_SEND_FILE {
        void execute(CommandParameters cmdParams) {
            DataInputStream dis = cmdParams.getClientHandler().getDataInputStream();
            try {
                File requestedFile = new File(dis.readUTF());
                if (requestedFile.exists()) {
                    new FileHandler().sendFile(requestedFile, cmdParams.getClientHandler());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    },
    RECEIVE_FILE {
        void execute(CommandParameters cmdParams) {
            File folder = cmdParams.getFileSharing() != null ? cmdParams.getFileSharing().getShareFolder() : null;
            new FileHandler().receiveFile(folder, cmdParams.getClientHandler());
        }
    },
    SEND_FILE {
        void execute(CommandParameters cmdParams) {
            if (cmdParams.getFile().exists()) {
                new FileHandler().sendFile(cmdParams.getFile(), cmdParams.getClientHandler());
            }
        }
    },
    SEND_FILES_LIST {
        void execute(CommandParameters cmdParams) {
            DataOutputStream dos = cmdParams.getClientHandler().getDataOutputStream();
            try {
                dos.writeUTF(FILES_LIST.name());
                dos.writeInt(cmdParams.getFileArray().length);
                for (int i = 0; i < cmdParams.getFileArray().length; i++) {
                    dos.writeUTF(cmdParams.getFileArray()[i].toString());
                }
                dos.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    },
    FILES_LIST {
        void execute(CommandParameters cmdParams) {
            DataInputStream dis = cmdParams.getClientHandler().getDataInputStream();
            List<File> fileList = new ArrayList<>();
            try {
                int count = dis.readInt();
                for (int i = 0; i < count; i++) {
                    fileList.add(new File(dis.readUTF()));
                }

                if (commandResult != null) {
                    commandResult.send(fileList.toArray());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    },
    OK {
        void execute(CommandParameters cmdParams) {
            System.out.println("/ok");
        }
    },
    EXIT {
        void execute(CommandParameters cmdParams) {
            DataOutputStream dos = cmdParams.getClientHandler().getDataOutputStream();
            try {
                dos.writeUTF(EXIT.name());
                dos.flush();
                cmdParams.getClientHandler().close();
                if (commandResult != null) {
                    commandResult.send(cmdParams.getClientHandler());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    CommandResult commandResult;

    Command() {
        commandResult = null;
    }

    abstract void execute(CommandParameters commandParameters);

    public void setCommandResult(CommandResult commandResult) {
        this.commandResult = commandResult;
    }
}
