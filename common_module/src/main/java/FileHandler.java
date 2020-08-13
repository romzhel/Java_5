import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileHandler {
    private static final Logger logger = LogManager.getLogger(FileHandler.class);
    private static final int BUFFER_SIZE = 1024;

    public void sendFile(ClientHandler clientHandler, Path fileForSendPath, Path fileSaveLocation) throws Exception {
        if (fileForSendPath.getRoot() == null) {
            fileForSendPath = FileInfoCollector.MAIN_FOLDER.resolve(fileForSendPath);
        }

        if (Files.notExists(fileForSendPath)) {
            logger.warn("файл '{}' для отправки не найден", fileForSendPath);
            throw new RuntimeException("Файл " + fileForSendPath + " не найден");
        }

        DataOutputStream dos = clientHandler.getDataOutputStream();
        dos.writeUTF(Command.IN_RECEIVE_FILE.name());
        dos.writeUTF(fileSaveLocation.toString());
        dos.writeLong(Files.size(fileForSendPath));

        logger.trace("sending file '{}' for saving in {} ...", fileForSendPath, fileSaveLocation);

        try (FileInputStream fis = new FileInputStream(fileForSendPath.toFile())) {
            byte[] buffer = new byte[BUFFER_SIZE];
            while (fis.available() > 0) {
                int bytesRead = fis.read(buffer);
                dos.write(buffer, 0, bytesRead);
            }
            dos.flush();
            logger.trace("file was sent to client");
        } catch (Exception e) {
            throw e;
        }
    }

    public Path receiveFile(ClientHandler clientHandler) throws Exception {
        DataInputStream dis = clientHandler.getDataInputStream();
        Path fileSaveLocationPath = Paths.get(dis.readUTF());
        long fileLength = dis.readLong();

//        logger.debug("received data: save location {}", fileSaveLocationPath);

        if (fileSaveLocationPath.getRoot() == null) {
            fileSaveLocationPath = FileInfoCollector.MAIN_FOLDER.resolve(fileSaveLocationPath);
        }

//        logger.trace("receiving file to '{}' ...", fileSaveLocationPath);
        Files.createFile(fileSaveLocationPath);

        byte[] buffer = new byte[BUFFER_SIZE];
        try (FileOutputStream fos = new FileOutputStream(fileSaveLocationPath.toFile())) {
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
        return fileSaveLocationPath.startsWith(FileInfoCollector.MAIN_FOLDER) ?
                FileInfoCollector.MAIN_FOLDER.relativize(fileSaveLocationPath) : fileSaveLocationPath;
    }

    public Path createFolder(ClientHandler clientHandler) throws Exception {
        DataInputStream dis = clientHandler.getDataInputStream();
        Path folderPath = clientHandler.getSelectedFolder().resolve(dis.readUTF());
        Path fullFolderPath = FileInfoCollector.MAIN_FOLDER.resolve(folderPath);
        if (fullFolderPath.toFile().exists()) {
            throw new RuntimeException("Такая папка уже существует");
        }

        Files.createDirectory(fullFolderPath);
        return folderPath;
    }

    public Path deleteItem(ClientHandler clientHandler) throws Exception {
        DataInputStream dis = clientHandler.getDataInputStream();
        Path fullItemPath = FileInfoCollector.MAIN_FOLDER.resolve(dis.readUTF());
        Files.deleteIfExists(fullItemPath);

        return fullItemPath;
    }
}
