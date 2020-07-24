import javafx.application.Platform;
import javafx.scene.control.ListView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public enum Command {

    REQUEST_DOWNLOAD {
        void treat(ClientHandler clientHandler, Object... params) {
            DataOutputStream dos = clientHandler.getDataOutputStream();
            try {
                dos.writeUTF(RECEIVE_REQUEST_DOWNLOAD_AND_SEND.name());
                dos.writeUTF(params[0].toString());
            } catch (Exception e) {

            }
        }
    },
    RECEIVE_REQUEST_DOWNLOAD_AND_SEND {
        void treat(ClientHandler clientHandler, Object... params) {
            DataInputStream dis = clientHandler.getDataInputStream();

            try {
                File requestedFile = new File(dis.readUTF());
                if (requestedFile.exists()) {
                    new FileHandler().sendFile(requestedFile, clientHandler);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    },
    RECEIVE_FILE {
        void treat(ClientHandler clientHandler, Object... params) {
            File saveFolder = params[1] != null ? ((FileSharing) params[1]).getSharedFolder() : null;
            new FileHandler().receiveFile(saveFolder, clientHandler);
        }
    },
    SEND_FILE {
        void treat(ClientHandler clientHandler, Object... params) {

            if (params[0] instanceof File) {
                File uploadedFile = (File) params[0];
                if (uploadedFile.exists()) {
                    new FileHandler().sendFile(uploadedFile, clientHandler);
                }
            }
        }
    },
    SEND_FILES_LIST {
        void treat(ClientHandler clientHandler, Object... params) {
            DataOutputStream dos = clientHandler.getDataOutputStream();
            try {
                dos.writeUTF(FILES_LIST.name());
                dos.writeInt(params.length);
                for (int i = 0; i < params.length; i++) {
                    dos.writeUTF(params[i].toString());
                }
                dos.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    },
    FILES_LIST {
        void treat(ClientHandler clientHandler, Object... params) {
            DataInputStream dis = clientHandler.getDataInputStream();
            List<File> fileList = new ArrayList<>();
            try {
                int count = dis.readInt();
                for (int i = 0; i < count; i++) {
                    fileList.add(new File(dis.readUTF()));
                }

                if (params[0] instanceof ListView) {
                    Platform.runLater(() -> {
                        ListView<File> lvFileList = (ListView<File>) params[0];
                        lvFileList.getItems().clear();
                        lvFileList.getItems().addAll(fileList);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    },
    OK {
        void treat(ClientHandler clientHandler, Object... params) {
            System.out.println("/ok");
        }
    },
    EXIT {
        void treat(ClientHandler clientHandler, Object... params) {
            DataOutputStream dos = clientHandler.getDataOutputStream();
            try {
                dos.writeUTF(EXIT.name());
                dos.flush();
                clientHandler.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    Command() {
    }

    abstract void treat(ClientHandler clientHandler, Object... params);
}
