import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileHandler {
    private static final Logger logger = LogManager.getLogger(FileHandler.class);
    private static final int BUFFER_SIZE = 1024;

    public void sendFile(File file, ClientHandler clientHandler) throws Exception {
        if (!file.exists()) {
            throw new RuntimeException("Файл " + file + " не найден");
        }

        DataOutputStream dos = clientHandler.getDataOutputStream();
        dos.writeUTF(Command.IN_RECEIVE_FILE.name());
        dos.writeUTF(file.getName());
        dos.writeLong(file.length());

        logger.trace("file name sent {}", file.getName());

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

    public Path receiveFile(ClientHandler clientHandler, Path pathToSave) throws Exception {
        logger.trace("будет принят файл {}", pathToSave);
        DataInputStream dis = clientHandler.getDataInputStream();
        String fileName = dis.readUTF();
        long fileLength = dis.readLong();

        File fileToSave = pathToSave != null ? FileManager.MAIN_FOLDER.resolve(pathToSave).resolve(fileName).toFile() :
                Dialogs.selectAnyFileTS(null, "Выбор места сохранения", fileName);

        byte[] buffer = new byte[BUFFER_SIZE];

        if (fileToSave == null) {
            long cycles = fileLength / buffer.length + (fileLength % buffer.length > 0 ? 1 : 0);
            for (int i = 0; i < cycles; i++) {
                dis.read(buffer);
            }
            throw new RuntimeException("Не выбрано место сохранения файла");
        }

        logger.trace("файл будет сохранен {}", fileToSave);
        fileToSave.createNewFile();

        try (FileOutputStream fos = new FileOutputStream(fileToSave)) {
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

        return pathToSave.resolve(fileName);
    }

    public Path createFolder(ClientHandler clientHandler) throws Exception {
        DataInputStream dis = clientHandler.getDataInputStream();
        Path folderPath = Paths.get(clientHandler.getUser().getNick()).resolve(dis.readUTF());
        Path fullFolderPath = FileManager.MAIN_FOLDER.resolve(folderPath);
        if (fullFolderPath.toFile().exists()) {
            throw new RuntimeException("Такая папка уже существует");
        }

        fullFolderPath.toFile().mkdir();
        return folderPath;
    }
}
