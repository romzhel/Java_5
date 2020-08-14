package auth_service;

public interface AuthService {
    void start() throws Exception;

    User getNickByLoginPass(String login, String pass) throws Exception;

    User registerNick(String... params) throws Exception;

    void stop();
}
