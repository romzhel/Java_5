package file_utils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;

public class FileSystemChangeListener {
    private FileChangeListener changeListener;
    private Path monitoredFolderPath;
    private Path relativesPath;
    private WatchEvent.Kind<?>[] eventTypes;

    private FileSystemChangeListener() {
        this.relativesPath = Paths.get("");
    }

    public static FileSystemChangeListener create() {
        return new FileSystemChangeListener();
    }

    public FileChangeListener getChangeListener() {
        return changeListener;
    }

    public FileSystemChangeListener setChangeListener(FileChangeListener changeListener) {
        this.changeListener = changeListener;
        return this;
    }

    public Path getMonitoredFolderPath() {
        return monitoredFolderPath;
    }

    public FileSystemChangeListener setMonitoredFolderPath(Path monitoredFolderPath) {
        this.monitoredFolderPath = monitoredFolderPath;
        return this;
    }

    public Path getRelativesPath() {
        return relativesPath;
    }

    public FileSystemChangeListener setRelativesPath(Path relativesPath) {
        this.relativesPath = relativesPath;
        return this;
    }

    public WatchEvent.Kind<?>[] getEventTypes() {
        return eventTypes;
    }

    public FileSystemChangeListener setEventTypes(WatchEvent.Kind<?>[] eventTypes) {
        this.eventTypes = eventTypes;
        return this;
    }

    public interface FileChangeListener {
        void onChanged(Path changedFolder, Path changedItem);
    }
}
