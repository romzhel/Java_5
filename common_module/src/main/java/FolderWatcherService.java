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
    private static FolderWatcherService instance;
    private Path monitoredRootFolder;
    private List<ChangeListener> changeListeners;
    private Map<WatchKey, Path> keyPathMap = new HashMap<>();
    private WatchService watchService;

    private FolderWatcherService() throws IOException {
        changeListeners = new ArrayList<>();
        watchService = FileSystems.getDefault().newWatchService();
    }

    public static FolderWatcherService getInstance() throws Exception {
        if (instance == null) {
            instance = new FolderWatcherService();
            instance.start();
        }
        return instance;
    }

    public FolderWatcherService addFolder(Path monitoredRootFolder) throws IOException {
        if (!Files.exists(monitoredRootFolder) || Files.isRegularFile(monitoredRootFolder)) {
            throw new RuntimeException("Недопустимая папка " + monitoredRootFolder);
        }

        registerDir(monitoredRootFolder, watchService);
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
        Thread thread = new Thread(this);
        thread.setName("FolderWatcherService");
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void run() {
        try {
//            WatchService watchService = FileSystems.getDefault().newWatchService();
//            registerDir(monitoredRootFolder, watchService);
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
//        logger.trace("добавлен мониторинг папки {}", path);
    }

    private void startListening(WatchService watchService) throws Exception {
        logger.info("Служба мониторинга изменений файловой системы запущена");
        while (true) {
            WatchKey queuedKey = watchService.take();
            for (WatchEvent<?> watchEvent : queuedKey.pollEvents()) {
                Path fullPath = keyPathMap.get(queuedKey).resolve((Path) watchEvent.context());
                registerDir(fullPath, watchService);
                logger.debug("обновлено {} в папке '{}'", fullPath,
                        FileInfoCollector.MAIN_FOLDER.relativize(keyPathMap.get(queuedKey)));
                Thread.sleep(500);
                changeListeners.forEach(action -> {
                    action.onChanged(FileInfoCollector.MAIN_FOLDER.relativize(keyPathMap.get(queuedKey)));
                });
            }
            if (!queuedKey.reset()) {
                keyPathMap.remove(queuedKey);
            }
            if (keyPathMap.isEmpty()) {
                logger.trace("автоматическая остановка мониторинга папок");
                break;
            }
        }
    }

    interface ChangeListener {
        void onChanged(Path changedFolder);
    }
}
