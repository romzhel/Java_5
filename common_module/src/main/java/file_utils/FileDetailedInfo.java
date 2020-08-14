package file_utils;

import auth_service.User;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class FileDetailedInfo {
    private FileInfo fileInfo;
    private User owner;
    private Map<User, Integer> usersAndRights;

    private FileDetailedInfo() {
        fileInfo = FileInfo.create(Paths.get(""), 0, false);
        usersAndRights = new HashMap<>();
    }

    public static FileDetailedInfo create() {
        return new FileDetailedInfo();
    }
}
