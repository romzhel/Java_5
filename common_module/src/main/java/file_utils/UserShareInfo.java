package file_utils;

import java.io.Serializable;

public class UserShareInfo implements Serializable {
    private String user;
    private byte flags;

    public UserShareInfo(String user, byte flags) {
        this.user = user;
        this.flags = flags;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public byte getFlags() {
        return flags;
    }

    public void setFlags(byte flags) {
        this.flags = flags;
    }

    @Override
    public String toString() {
        return "UserShareInfo{" +
                "user='" + user + '\'' +
                ", flags=" + flags +
                '}';
    }
}
