import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FolderWatcherService implements Runnable {
    private static final Logger logger = LogManager.getLogger(FolderWatcherService.class);
    private Path monitoredRootFolder;
    private List<ChangeListener> changeListeners;
    private Map<WatchKey, Path> keyPathMap = new HashMap<>();

    private FolderWatcherService() {
        changeListeners = new ArrayList<>();
    }

    public static FolderWatcherService create() {
        return new FolderWatcherService();
    }

    public FolderWatcherService setFolder(Path monitoredRootFolder) {
        this.monitoredRootFolder = monitoredRootFolder;
        return this;
    }

    public FolderWatcherService addChangeListener(ChangeListener changeListener) {
        changeListeners.add(changeListener);
        return this;
    }

    public void removeChangeListener(ChangeListener changeListener) {
        changeListeners.remove(changeListener);
    }

    public void start() throws Exception {
        if (!monitoredRootFolder.toFile().exists() || monitoredRootFolder.toFile().isFile()) {
            throw new RuntimeException("Недопустимая папка " + monitoredRootFolder);
        }

        Thread thread = new Thread(this);
        thread.setName("FolderWatcherService");
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void run() {
        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            registerDir(monitoredRootFolder, watchService);
            startListening(watchService);
        } catch (Exception e) {
            logger.error("ошибка {}", e.getMessage(), e);
        }
    }

    private void registerDir(Path path, WatchService watchService) throws IOException {
        if (!Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            return;
        }

        WatchKey key = path.register(watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
//                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE);
        keyPathMap.put(key, path);

        for (File f : path.toFile().listFiles()) {
            if (f.isDirectory()) {
                registerDir(f.toPath(), watchService);
            }
        }
    }

    private void startListening(WatchService watchService) throws Exception {
        logger.info("Служба мониторинга изменений папки {} запущена", monitoredRootFolder);
        while (true) {
            WatchKey queuedKey = watchService.take();
            for (WatchEvent<?> watchEvent : queuedKey.pollEvents()) {
                Path fullPath = keyPathMap.get(queuedKey).resolve((Path) watchEvent.context());

                if (watchEvent.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                }

                if (watchEvent.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                    registerDir(fullPath, watchService);
                    changeListeners.forEach(action -> action.onChanged(keyPathMap.get(queuedKey)));
                }
            }
            if (!queuedKey.reset()) {
                keyPathMap.remove(queuedKey);
            }
            if (keyPathMap.isEmpty()) {
                break;
            }
        }
    }

    interface ChangeListener {
        void onChanged(Path changedFolder);
    }
}
