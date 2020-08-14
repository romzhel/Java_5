package auth_service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;

public class SqliteAuthService implements AuthService {
    private static final Logger logger = LogManager.getLogger(SqliteAuthService.class);
    private Connection connection;
    private PreparedStatement getNick;
    private PreparedStatement registerNick;

    public SqliteAuthService(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void start() throws Exception {
        getNick = connection.prepareStatement("SELECT id, nick FROM users WHERE login = ? AND password = ?;");
        registerNick = connection.prepareStatement("INSERT INTO users (nick, login, password) VALUES (?,?,?);",
                PreparedStatement.RETURN_GENERATED_KEYS);
        logger.info("Сервер авторизации запущен");
    }

    @Override
    public User getNickByLoginPass(String login, String password) throws Exception {
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
    public User registerNick(String... params) throws Exception {
        logger.info("регистрация {}", Arrays.toString(params));
        registerNick.setString(1, params[0]);
        registerNick.setString(2, params[1]);
        registerNick.setString(3, params[2]);
        registerNick.execute();

        ResultSet rs = registerNick.getGeneratedKeys();
        if (rs.next()) {
            return new User(rs.getInt(1), params[0]);
        } else {
            throw new RuntimeException("Не удалось добавить пользователя с ником '" + params[0] + "'");
        }
    }

    @Override
    public void stop() {
        AutoCloseable[] objs = new AutoCloseable[]{getNick, registerNick, connection};
        for (AutoCloseable obj : objs) {
            try {
                obj.close();
            } catch (Exception e) {
            }
        }
        logger.trace("Сервис авторизации остановлен");
    }
}
