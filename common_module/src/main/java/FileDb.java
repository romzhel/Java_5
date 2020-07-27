import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class FileDb {
    private Connection connection;
    private PreparedStatement saveMainInfo;
    private PreparedStatement saveAccessInfo;
    private PreparedStatement changeNick;


    public FileDb(Connection connection) {
        this.connection = connection;
    }

    public void init() throws Exception {
        saveMainInfo = connection.prepareStatement("INSERT INTO files (file_name, owner_id) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS);
        saveAccessInfo = connection.prepareStatement("INSERT INTO files_access (file_id, user_id, rights) VALUES (?, ?, ?);");
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
        saveAccessInfo.setInt(3, 255);
        int res = saveAccessInfo.executeUpdate();

        if (res != 1) {
            connection.rollback();
            throw new RuntimeException("Ошибка сохранения в базу данных сведений о доступе к файлу");
        }
        connection.setAutoCommit(true);
    }
}
