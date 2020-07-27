package auth_service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SqliteAuthService implements AuthService {
    private static final Logger LOGGER = LogManager.getLogger(SqliteAuthService.class);
    private Connection connection;
    private PreparedStatement getNick;
    private PreparedStatement checkNick;
    private PreparedStatement changeNick;

    public SqliteAuthService(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void start() throws Exception {
        getNick = connection.prepareStatement("SELECT id, nick FROM users WHERE login = ? AND password = ?");
        checkNick = connection.prepareStatement("SELECT * FROM users WHERE nick = ?");
        changeNick = connection.prepareStatement("UPDATE users SET nick = ? WHERE nick = ?");
        LOGGER.info("Сервер авторизации запущен");
    }

    @Override
    public User getNickByLoginPass(String login, String password) throws Exception {
        System.out.println(login + " " + password);
        getNick.setString(1, login);
        getNick.setString(2, password);

        try (ResultSet rs = getNick.executeQuery()) {
            if (rs.next()) {
                return new User(rs.getInt("id"), rs.getString("nick"));
            } else {
                throw new RuntimeException("Пользователь не найден");
            }
        }
    }

    @Override
    public void changeNick(String oldNick, String newNick) throws Exception {
        checkNick.setString(1, newNick);

        try (ResultSet rs = checkNick.executeQuery()) {
            if (rs.next()) {
                throw new RuntimeException("Ник " + newNick + " уже существует");
            }
        }

        changeNick.setString(1, newNick);
        changeNick.setString(2, oldNick);

        if (changeNick.executeUpdate() < 1) {
            throw new RuntimeException("Не удалось сменить ник " + oldNick + " на " + newNick);
        }
    }

    @Override
    public void stop() {
        try {
            changeNick.close();
        } catch (Exception e) {
        }
        try {
            getNick.close();
        } catch (Exception e) {
        }
        try {
            connection.close();
        } catch (Exception e) {
        }
        LOGGER.trace("Сервиса авторизации остановлен");
    }
}
