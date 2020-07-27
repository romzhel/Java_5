package auth_service;

public interface AuthService {
    void start() throws Exception;

    User getNickByLoginPass(String login, String pass) throws Exception;

    void changeNick(String oldNick, String newNick) throws Exception;

    void stop();
}
