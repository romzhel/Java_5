import javafx.application.Platform;
import javafx.scene.control.ListView;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public enum Command {

    REQUEST_DOWNLOAD("") {
        void treat(ClientHandler clientHandler, Object... params) {
            DataOutputStream dos = clientHandler.getDataOutputStream();
            try {
                dos.writeUTF(RECEIVE_REQUEST_DOWNLOAD_AND_SEND.name());
                System.out.println(RECEIVE_REQUEST_DOWNLOAD_AND_SEND.name());
                dos.writeUTF(params[0].toString());
                System.out.println(params[0].toString());
            } catch (Exception e){

            }
        }
    },
    RECEIVE_REQUEST_DOWNLOAD_AND_SEND("") {
        void treat(ClientHandler clientHandler, Object... params) {
            DataInputStream dis = clientHandler.getDataInputStream();
            DataOutputStream dos = clientHandler.getDataOutputStream();

            try {
                File requestedFile = new File(dis.readUTF());
                if (requestedFile.exists()) {
                    dos.writeUTF(RECEIVE_FILE.name());
                    System.out.println(RECEIVE_FILE.name());
                    dos.writeUTF(requestedFile.getName());
                    System.out.println(requestedFile.getName());
                    dos.writeLong(requestedFile.length());
                    System.out.println(requestedFile.length());

                    byte[] buffer = new byte[1024];

                    try (FileInputStream fis = new FileInputStream(requestedFile)) {
                        while (fis.available() > 0) {
                            int bytesRead = fis.read(buffer);
                            dos.write(buffer, 0, bytesRead);
                        }
                        dos.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    },
    RECEIVE_FILE("") {
        void treat(ClientHandler clientHandler, Object... params) {
            DataInputStream dis = clientHandler.getDataInputStream();
            String fileName = "";
            long fileLength = 0;
            File newFile = null;

            try {
                fileName = dis.readUTF();
                fileLength = dis.readLong();
                newFile = params[1] != null ? new File (((FileSharing)params[1]).getSharedFolder() + "\\" + fileName) :
                        Dialogs.selectAnyFileTS(null, "Выбор места сохранения", null, fileName).get(0);
                newFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }

            byte[] buffer = new byte[1024];

            try(FileOutputStream fos = new FileOutputStream(newFile)) {
                int bytesRead;
                do {
                    bytesRead = dis.read(buffer);
                    fos.write(buffer, 0, bytesRead);
                } while (dis.available() > 0 && fileLength - bytesRead > 0);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    },
    SEND_FILE("") {
        void treat(ClientHandler clientHandler, Object... params) {
            DataOutputStream dos = clientHandler.getDataOutputStream();

            if (params[0] instanceof File) {
                File uploadedFile = (File) params[0];
                if (uploadedFile.exists()) {
                    try {
                        dos.writeUTF(RECEIVE_FILE.name());
                        System.out.println(RECEIVE_FILE.name());
                        dos.writeUTF(uploadedFile.getName());
                        System.out.println(uploadedFile.getName());
                        dos.writeLong(uploadedFile.length());
                        System.out.println(uploadedFile.length());

                        byte[] buffer = new byte[1024];

                        try (FileInputStream fis = new FileInputStream(uploadedFile)) {
                            while (fis.available() > 0) {
                                int bytesRead = fis.read(buffer);
                                dos.write(buffer, 0, bytesRead);
                            }
                            dos.flush();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    },
    SEND_FILES_LIST("") {
        void treat(ClientHandler clientHandler, Object... params) {
            DataOutputStream dos = clientHandler.getDataOutputStream();
            try {
                dos.writeUTF(FILES_LIST.name());
                dos.writeInt(params.length);
                for (int i = 0; i < params.length; i++) {
                    dos.writeUTF(params[i].toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    },
    FILES_LIST("") {
        void treat(ClientHandler clientHandler, Object... params) {
            DataInputStream dis = clientHandler.getDataInputStream();
            List<File> fileList = new ArrayList<>();
            try {
                int count = dis.readInt();
                for (int i = 0; i < count; i++) {
                    fileList.add(new File(dis.readUTF()));
                }

                if (params[0] instanceof ListView) {
                    Platform.runLater(()-> {
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
    OK("/ok") {
        void treat(ClientHandler clientHandler, Object... params) {
            System.out.println("/ok");
        }
    };

    String message;

    Command(String message) {
        this.message = message;
    }

    abstract void treat(ClientHandler clientHandler, Object... params);

    public String getMessage() {
        return message;
    }


}
