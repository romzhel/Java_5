package auth_service;

public class User {
    private int id;
    private String nick;

    public User(int id, String name) {
        this.id = id;
        this.nick = name;
    }

    public static User parse(String[] params) {
        if (params.length != 2 || !params[0].matches("\\d+")) {
            throw new RuntimeException("Некорректные параметры");
        }
        return new User(Integer.parseInt(params[0]), params[1]);
    }

    public static User UNREGISTERED() {
        return new User(0, "Неизвестный");
    }

    public String[] convertToStringArray() {
        return new String[]{String.valueOf(id), nick};
    }

    public int getId() {
        return id;
    }

    public String getNick() {
        return nick;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + nick + '\'' +
                '}';
    }
}
