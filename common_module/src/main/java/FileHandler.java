import java.io.*;

public class FileHandler {

    public void sendFile(File file, ClientHandler clientHandler) {
        DataOutputStream dos = clientHandler.getDataOutputStream();

        try {
            dos.writeUTF(Command.RECEIVE_FILE.name());
            dos.writeUTF(file.getName());
            dos.writeLong(file.length());

            byte[] buffer = new byte[1024];

            try (FileInputStream fis = new FileInputStream(file)) {
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

    public void receiveFile(File folder, ClientHandler clientHandler) {
        DataInputStream dis = clientHandler.getDataInputStream();
        long fileLength = 0;
        File fileToSave = null;

        try {
            String fileName = dis.readUTF();
            fileLength = dis.readLong();
            fileToSave = folder != null ? new File(folder + "\\" + fileName) :
                    Dialogs.selectAnyFileTS(null, "Выбор места сохранения", null, fileName);
            if (fileToSave == null) {
                return;
            }
            fileToSave.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }

        byte[] buffer = new byte[1024];

        try (FileOutputStream fos = new FileOutputStream(fileToSave)) {
            int bytesRead;
            long cycles = fileLength / buffer.length + (fileLength % buffer.length > 0 ? 1 : 0);
            for (int i = 0; i < cycles; i++) {
                bytesRead = dis.read(buffer);
                fos.write(buffer, 0, bytesRead);
            }

            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
