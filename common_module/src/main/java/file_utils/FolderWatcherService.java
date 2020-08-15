package file_utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class FolderWatcherService implements Runnable {
    private static final Logger logger = LogManager.getLogger(FolderWatcherService.class);
    private static FolderWatcherService instance;
    private List<FileSystemChangeListener> changeListeners;
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

    /*public file_utils.FolderWatcherService addFolder(Path monitoredRootFolder) throws IOException {
        if (!Files.exists(monitoredRootFolder) || Files.isRegularFile(monitoredRootFolder)) {
            throw new RuntimeException("Недопустимая папка " + monitoredRootFolder);
        }

        registerDir(monitoredRootFolder);
        return this;
    }*/

    public FolderWatcherService addChangeListener(FileSystemChangeListener changeListener) throws Exception {
        if (!Files.exists(changeListener.getMonitoredFolderPath()) || Files.isRegularFile(changeListener.getMonitoredFolderPath())) {
            throw new RuntimeException("Недопустимая папка " + changeListener.getMonitoredFolderPath());
        }

        changeListeners.add(changeListener);
        registerDir(changeListener.getMonitoredFolderPath());
        return this;
    }

    public void removeChangeListener(FileSystemChangeListener changeListener) {
        changeListeners.remove(changeListener);
        unregisterDir(changeListener.getMonitoredFolderPath());
    }

    public void start() throws Exception {
        Thread thread = new Thread(this);
        thread.setName("file_utils.FolderWatcherService");
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void run() {
        try {
            startListening(watchService);
        } catch (Exception e) {
            logger.error("ошибка {}", e.getMessage(), e);
        }
    }

    private void registerDir(Path path) throws IOException {
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
                registerDir(f.toPath());
            }
        }
        logger.trace("добавлен мониторинг папки {}", path);
    }

    private void unregisterDir(Path path) {
        for (Map.Entry<WatchKey, Path> entry : keyPathMap.entrySet()) {
            if (path.equals(entry.getValue())) {
                entry.getKey().cancel();

                try {
                    Files.walk(path)
                            .filter(Files::isDirectory)
                            .forEach(this::unregisterDir);
                } catch (IOException e) {
                    logger.error("ошибка отмены мониторинга папки {}", e.getMessage(), e);
                }

                logger.trace("мониторинг папки {} был отменен", path);
                break;
            }
        }
    }

    private void startListening(WatchService watchService) throws Exception {
        logger.info("Служба мониторинга изменений файловой системы запущена");
        while (true) {
            WatchKey queuedKey = watchService.take();
            for (WatchEvent<?> watchEvent : queuedKey.pollEvents()) {
                Path fullPath = keyPathMap.get(queuedKey).resolve((Path) watchEvent.context());
                registerDir(fullPath);
                logger.debug("обновлено '{}'", fullPath);
                Thread.sleep(500);
                Path changedFolder = keyPathMap.get(queuedKey);
                changeListeners.forEach(listener -> {
                    if (changedFolder.startsWith(listener.getMonitoredFolderPath()) &&
                            (listener.getEventTypes() == null || Arrays.asList(listener.getEventTypes()).contains(watchEvent.kind()))) {
                        Path changedRelativePath = listener.getRelativesPath().relativize(changedFolder);
                        logger.debug("относительный путь '{}' = '{}'", listener.getMonitoredFolderPath(), changedRelativePath);
                        listener.getChangeListener().onChanged(changedRelativePath, listener.getRelativesPath().relativize(fullPath));
                    }
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
}
