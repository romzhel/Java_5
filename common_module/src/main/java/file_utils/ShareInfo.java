package file_utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ShareInfo implements Serializable {
    public static final ShareInfo EMPTY = new ShareInfo("", "", (byte) 0);
    private String fileName;
    private String owner;
    private byte mainFlags;
    private List<UserShareInfo> userShareInfoList;
    private List<UserShareInfo> addedItems;
    private List<UserShareInfo> deletedItems;

    public ShareInfo() {
        userShareInfoList = new ArrayList<>();
        addedItems = new ArrayList<>();
        deletedItems = new ArrayList<>();
    }

    public ShareInfo(String fileName, String owner, byte mainFlags, List<UserShareInfo> userShareInfoList) {
        this.fileName = fileName;
        this.owner = owner;
        this.mainFlags = mainFlags;
        this.userShareInfoList = userShareInfoList;
        addedItems = new ArrayList<>();
        deletedItems = new ArrayList<>();
    }

    public ShareInfo(String fileName, String owner, byte mainFlags) {
        this.fileName = fileName;
        this.owner = owner;
        this.mainFlags = mainFlags;
        userShareInfoList = new ArrayList<>();
        addedItems = new ArrayList<>();
        deletedItems = new ArrayList<>();
    }

    public void addUserShareInfo(UserShareInfo userShareInfo) {
        userShareInfoList.add(userShareInfo);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public byte getMainFlags() {
        return mainFlags;
    }

    public void setMainFlags(byte mainFlags) {
        this.mainFlags = mainFlags;
    }

    public List<UserShareInfo> getUserShareInfoList() {
        return userShareInfoList;
    }

    public void setUserShareInfoList(List<UserShareInfo> userShareInfoList) {
        this.userShareInfoList = userShareInfoList;
    }

    public List<UserShareInfo> getAddedItems() {
        return addedItems;
    }

    public void setAddedItems(List<UserShareInfo> addedItems) {
        this.addedItems = addedItems;
    }

    public List<UserShareInfo> getDeletedItems() {
        return deletedItems;
    }

    public void setDeletedItems(List<UserShareInfo> deletedItems) {
        this.deletedItems = deletedItems;
    }

    @Override
    public String toString() {
        return "ShareInfo{" +
                "fileName='" + fileName + '\'' +
                ", owner='" + owner + '\'' +
                ", mainFlags=" + mainFlags +
                ", userShareInfoList=" + userShareInfoList +
                ", addedItems=" + addedItems +
                ", deletedItems=" + deletedItems +
                '}';
    }
}
