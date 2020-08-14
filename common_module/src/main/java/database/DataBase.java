package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBase {
    private static DataBase instance;
    private Connection connection;

    private DataBase() throws Exception {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:./common_module/src/main/resources/cloud_db.db");
    }

    public static DataBase getInstance() throws Exception {
        if (instance == null) {
            instance = new DataBase();
        }
        return instance;
    }

    public void connect() {

    }

    public void disconnect() {
        try {
            connection.close();
        } catch (SQLException throwables) {

        }
    }

    public Connection getConnection() {
        return connection;
    }
}
