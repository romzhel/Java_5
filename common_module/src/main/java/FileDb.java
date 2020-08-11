import auth_service.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class FileDb {
    private Connection connection;
    private PreparedStatement saveMainInfo;
    private PreparedStatement saveAccessInfo;
    private PreparedStatement changeNick;

    private PreparedStatement getFileList;
    private static final Logger logger = LogManager.getLogger(FileDb.class);


    public FileDb(Connection connection) {
        this.connection = connection;
    }

    public void init() throws Exception {
        saveMainInfo = connection.prepareStatement("INSERT INTO files (file_name, owner_id) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS);
        saveAccessInfo = connection.prepareStatement("INSERT INTO files_access (file_id, user_id, rights) VALUES (?, ?, ?);");
        getFileList = connection.prepareStatement("SELECT file_name, owner_id, user_id, rights FROM files JOIN files_access " +
                "ON (files.id = files_access.file_id) WHERE file_name LIKE ? AND user_id = ?");

    }

    public void saveNewFile(String fileName, int userId) throws Exception {
        connection.setAutoCommit(false);

        saveMainInfo.setString(1, fileName);
        saveMainInfo.setInt(2, userId);
        saveMainInfo.executeUpdate();

        int key;
        ResultSet rs = saveMainInfo.getGeneratedKeys();
        if (rs.next()) {
            key = rs.getInt(1);
        } else {
            connection.rollback();
            connection.setAutoCommit(true);
            rs.close();
            throw new RuntimeException("Ошибка сохранения в базу данных основных сведений о файле");
        }

        saveAccessInfo.setInt(1, key);
        saveAccessInfo.setInt(2, userId);
        saveAccessInfo.setInt(3, 0);
        int res = saveAccessInfo.executeUpdate();

        if (res != 1) {
            connection.rollback();
            throw new RuntimeException("Ошибка сохранения в базу данных сведений о доступе к файлу");
        }
        connection.setAutoCommit(true);
        logger.trace("данные о файле {} сохранены в БД", fileName);
    }

    public List<Path> getFiles(User user, Path folder) throws Exception {
        logger.trace("будет сделан запрос в БД: user = {}, folder = {}", user, folder);
        String searchText = folder.equals(FileInfoCollector.UP_LEVEL) ? "%" : folder.toString() + "\\%";
        logger.trace("search text = ", searchText);
        getFileList.setString(1, searchText);
        getFileList.setInt(2, user.getId());

        ResultSet rs = getFileList.executeQuery();

        List<Path> result = new ArrayList<>();
        while (rs.next()) {
            result.add(Paths.get(rs.getString("file_name")));
        }
        logger.trace("результат запроса из ДБ по файлам {}", result.size());

        return result;
    }
}
