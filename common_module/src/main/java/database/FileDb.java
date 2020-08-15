package database;

import file_utils.ShareInfo;
import file_utils.UserShareInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class FileDb {
    private static final Logger logger = LogManager.getLogger(FileDb.class);
    private Connection connection;
    private PreparedStatement getFileMainInfo;
    private PreparedStatement getFileUserList;
    private PreparedStatement addFileMainInfo;
    private PreparedStatement updateFileMainInfo;
    private PreparedStatement deleteFileMainInfo;
    private PreparedStatement addFileInfo;
    private PreparedStatement updateFileInfo;
    private PreparedStatement deleteFileInfo;


    public FileDb(Connection connection) {
        this.connection = connection;
    }

    public void init() throws Exception {
        getFileMainInfo = connection.prepareStatement("SELECT file_name, main_flags, nick as owner " +
                "FROM files JOIN users ON (users.id = files.owner_id) WHERE file_name LIKE ?;");
        getFileUserList = connection.prepareStatement("SELECT nick, flags " +
                "FROM files_access JOIN files ON (files_access.file_id = files.id) JOIN users ON (files_access.user_id = users.id) " +
                "WHERE file_name LIKE ?;");
        addFileMainInfo = connection.prepareStatement("INSERT INTO files (file_name, owner_id, main_flags) " +
                "VALUES (?, (SELECT id FROM users WHERE nick = ?), ?);");
        updateFileMainInfo = connection.prepareStatement("UPDATE files SET main_flags = ? WHERE file_name LIKE ?;");
        deleteFileMainInfo = connection.prepareStatement("DELETE FROM files WHERE file_name LIKE ?;");
        addFileInfo = connection.prepareStatement("INSERT INTO files_access (file_id, user_id, flags) " +
                "VALUES ((SELECT id FROM files WHERE file_name LIKE ?), (SELECT id FROM users WHERE nick = ?), ?);");
        updateFileInfo = connection.prepareStatement("UPDATE files_access SET flags = ? " +
                "WHERE file_id = (SELECT id FROM files WHERE file_name LIKE ?) AND user_id = (SELECT id FROM users WHERE nick = ?); ");
        deleteFileInfo = connection.prepareStatement("DELETE FROM files_access WHERE file_id = (SELECT id FROM files WHERE file_name LIKE ?) " +
                "AND user_id = (SELECT id FROM users WHERE nick = ?) AND flags = ?;");
    }

    public ShareInfo getShareInfo(String path) throws Exception {
        logger.trace("получение всех данных по доступу к '{}'", path);
        getFileMainInfo.setString(1, path);
        ResultSet rs = getFileMainInfo.executeQuery();

        ShareInfo shareInfo = new ShareInfo();
        shareInfo.setFileName(path);
        shareInfo.setOwner(Paths.get(path).getName(0).toString());
        shareInfo.setMainFlags(((byte) 0));
        if (rs.next()) {
            shareInfo.setOwner(rs.getString("owner"));
            shareInfo.setMainFlags(rs.getByte("main_flags"));
        }
        rs.close();

        getFileUserList.setString(1, path);
        rs = getFileUserList.executeQuery();

        while (rs.next()) {
            shareInfo.addUserShareInfo(new UserShareInfo(rs.getString("nick"), rs.getByte("flags")));
        }
        rs.close();

        return shareInfo;
    }

    public boolean addFileMainInfo(ShareInfo shareInfo) throws Exception {
        logger.trace("добавление {}", shareInfo);
        addFileMainInfo.setString(1, shareInfo.getFileName());
        addFileMainInfo.setString(2, shareInfo.getOwner());
        addFileMainInfo.setInt(3, shareInfo.getMainFlags());

        return addFileMainInfo.executeUpdate() > 0;
    }

    public boolean updateFileMainInfo(ShareInfo shareInfo) throws Exception {
        logger.trace("изменение {}", shareInfo);
        updateFileMainInfo.setInt(1, shareInfo.getMainFlags());
        updateFileMainInfo.setString(2, shareInfo.getFileName());

        return updateFileMainInfo.executeUpdate() > 0;
    }

    public boolean deleteFileMainInfo(String path) throws Exception {
        logger.trace("удаление {}", path);
        deleteFileInfo.setString(1, path);

        return deleteFileMainInfo.executeUpdate() > 0;
    }

    public boolean addFileInfo(String path, UserShareInfo info) throws Exception {
        logger.trace("добавление {} для {}", info, path);
        addFileInfo.setString(1, path);
        addFileInfo.setString(2, info.getUser());
        addFileInfo.setInt(3, info.getFlags());

        return addFileInfo.executeUpdate() > 0;
    }

    public boolean updateFileInfo(String path, UserShareInfo info) throws Exception {
        logger.trace("изменение {} для {}", info, path);
        updateFileInfo.setInt(1, info.getFlags());
        updateFileInfo.setString(2, path);
        updateFileInfo.setString(3, info.getUser());

        return updateFileInfo.executeUpdate() > 0;
    }

    public boolean deleteFileInfo(String path, UserShareInfo info) throws Exception {
        logger.trace("удаление {} для {}", path, info);
        deleteFileInfo.setString(1, path);
        deleteFileInfo.setString(2, info.getUser());
        deleteFileInfo.setInt(3, info.getFlags());

        return deleteFileInfo.executeUpdate() > 0;
    }
}
