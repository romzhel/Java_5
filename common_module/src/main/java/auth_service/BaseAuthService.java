package auth_service;

import java.util.ArrayList;
import java.util.List;

public class BaseAuthService implements AuthService {
    private final List<Entry> entries;

    public BaseAuthService() {
        entries = new ArrayList<>();
        entries.add(new Entry("login1", "pass1", "nick1"));
        entries.add(new Entry("login2", "pass2", "nick2"));
        entries.add(new Entry("login3", "pass3", "nick3"));
    }

    @Override
    public void start() {
        System.out.println("Сервис аутентификации запущен");
    }

    @Override
    public void stop() {
        System.out.println("Сервис аутентификации остановлен");
    }

    @Override
    public User getNickByLoginPass(String login, String pass) {
        for (Entry o : entries) {
            if (o.login.equals(login) && o.pass.equals(pass)) return new User(0, o.nick);
        }
        return null;
    }

    @Override
    public void changeNick(String oldNick, String newNick) throws RuntimeException {
        for (Entry e : entries) {
            if (e.nick.equals(newNick)) {
                throw new RuntimeException("Ник " + newNick + " уже существует");
            }
        }

        for (Entry e : entries) {
            if (e.nick.equals(oldNick)) {
                e.nick = newNick;
                return;
            }
        }
        throw new RuntimeException("Не удалось сменить ник " + oldNick + " на " + newNick);
    }

    private static class Entry {
        private final String login;
        private final String pass;
        private String nick;

        public Entry(String login, String pass, String nick) {
            this.login = login;
            this.pass = pass;
            this.nick = nick;
        }
    }
}
