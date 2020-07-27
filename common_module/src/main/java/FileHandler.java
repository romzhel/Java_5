import java.io.*;

public class FileHandler {
    private static final int BUFFER_SIZE = 1024;

    public void sendFile(File file, ClientHandler clientHandler) throws Exception {
        DataOutputStream dos = clientHandler.getDataOutputStream();
        dos.writeUTF(Command.RECEIVE_FILE.name());
        dos.writeUTF(file.getName());
        dos.writeLong(file.length());

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            while (fis.available() > 0) {
                int bytesRead = fis.read(buffer);
                dos.write(buffer, 0, bytesRead);
            }
            dos.flush();
        } catch (Exception e) {
            throw e;
        }
    }

    public File receiveFile(ClientHandler clientHandler) throws Exception {
        DataInputStream dis = clientHandler.getDataInputStream();
        String fileName = dis.readUTF();
        long fileLength = dis.readLong();

        File fileToSave = clientHandler.getSelectedFolder() != null ?
                new File(clientHandler.getSelectedFolder() + "\\" + fileName) :
                Dialogs.selectAnyFileTS(null, "Выбор места сохранения", fileName);
        if (fileToSave == null) {
            throw new RuntimeException("Не выбрано место сохранения файла");
        }
        fileToSave.createNewFile();

        try (FileOutputStream fos = new FileOutputStream(fileToSave)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            long cycles = fileLength / buffer.length + (fileLength % buffer.length > 0 ? 1 : 0);
            for (int i = 0; i < cycles; i++) {
                bytesRead = dis.read(buffer);
                fos.write(buffer, 0, bytesRead);
            }

            fos.flush();
        } catch (Exception e) {
            throw e;
        }

        return fileToSave;
    }
}
