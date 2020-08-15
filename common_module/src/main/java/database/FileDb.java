package database;

import file_utils.FileInfo;
import file_utils.ShareInfo;
import file_utils.UserShareInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class FileDb {
    private static final Logger logger = LogManager.getLogger(FileDb.class);
    private Connection connection;
    private PreparedStatement getFileMainInfo;
    private PreparedStatement getFileUserList;


    public FileDb(Connection connection) {
        this.connection = connection;
    }

    public void init() throws Exception {
        getFileMainInfo = connection.prepareStatement("SELECT file_name, main_flags, nick as owner " +
                "FROM files JOIN users ON (users.id = files.owner_id) WHERE file_name LIKE ?;");
        getFileUserList = connection.prepareStatement("SELECT file_name, users.nick, files_access.flags, owner_id " +
                "FROM files_access JOIN files ON (files_access.file_id = files.id) JOIN users ON (files_access.user_id = users.id)" +
                "WHERE file_name LIKE ?;");
    }

    public ShareInfo getShareInfo(String path) throws Exception {
        logger.trace("получение всех данных по доступу к '{}'", path);
        getFileMainInfo.setString(1, path);
        ResultSet rs = getFileMainInfo.executeQuery();


        ShareInfo shareInfo = new ShareInfo();
        shareInfo.setFileName(path);
        shareInfo.setMainFlags(((byte) 0));
        if (rs.next()) {
            shareInfo.setOwner(rs.getString("owner"));
            shareInfo.setMainFlags(rs.getByte("main_flags"));
        }
        rs.close();

        getFileUserList.setString(1, path);
        rs = getFileUserList.executeQuery();

        while (rs.next()) {
            shareInfo.addUserShareInfo(new UserShareInfo("", (byte) 0));
        }
        rs.close();

        return shareInfo;
    }

    public void applyShareInfoChanges(FileInfo fileInfo) {
        logger.trace("Входные данные {}", fileInfo);
    }
}
