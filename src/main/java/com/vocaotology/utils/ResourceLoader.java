package com.vocaotology.utils;

import com.vocaotology.config.AppConfig;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.text.Font;
import javafx.scene.Parent;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ResourceLoader implements AutoCloseable {
    private static volatile ResourceLoader instance;
    private static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private static final int CACHE_INITIAL_CAPACITY = 100;
    
    private final Map<String, Image> imageCache;
    private final Map<String, Font> fontCache;
    private final Map<String, Media> soundCache;
    private final Map<String, List<String>> textCache;
    private final Map<String, Parent> fxmlCache;
    private final Logger logger;
    private final ExecutorService executorService;
    
    private ResourceLoader() {
        // Use ConcurrentHashMap for thread-safety without explicit synchronization
        imageCache = new ConcurrentHashMap<>(CACHE_INITIAL_CAPACITY);
        fontCache = new ConcurrentHashMap<>(CACHE_INITIAL_CAPACITY);
        soundCache = new ConcurrentHashMap<>(CACHE_INITIAL_CAPACITY);
        textCache = new ConcurrentHashMap<>(CACHE_INITIAL_CAPACITY);
        fxmlCache = new ConcurrentHashMap<>(CACHE_INITIAL_CAPACITY);
        
        // Use virtual threads for better resource utilization in Java 21+
        executorService = Executors.newCachedThreadPool();
        logger = setupLogger();
    }
    
    public static ResourceLoader getInstance() {
        if (instance == null) {
            synchronized (ResourceLoader.class) {
                if (instance == null) {
                    instance = new ResourceLoader();
                }
            }
        }
        return instance;
    }
    
    private Logger setupLogger() {
        Logger log = Logger.getLogger(ResourceLoader.class.getName());
        try {
            File logsDir = new File("logs");
            logsDir.mkdirs();
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            FileHandler fileHandler = new FileHandler(
                "logs/resource_loader_" + timestamp + ".log",
                true
            );
            
            fileHandler.setFormatter(new SimpleFormatter() {
                @Override
                public String format(LogRecord record) {
                    return String.format("[%s] %s: %s%n",
                        record.getLevel(),
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        record.getMessage()
                    );
                }
            });
            
            log.addHandler(fileHandler);
            log.setLevel(Level.INFO);
            
        } catch (IOException e) {
            System.err.println("Failed to setup logger: " + e.getMessage());
        }
        return log;
    }
    
    public CompletableFuture<Image> loadImageAsync(String imageName) {
        return CompletableFuture.supplyAsync(() -> {
            Image cached = imageCache.get(imageName);
            if (cached != null) {
                logger.fine("Image loaded from cache: " + imageName);
                return cached;
            }
            
            String path = AppConfig.IMAGES_PATH + imageName;
            try (InputStream is = getClass().getResourceAsStream(path)) {
                if (is == null) {
                    throw new IOException("Resource not found: " + path);
                }
                
                Image image = new Image(is);
                imageCache.put(imageName, image);
                logger.info("Image loaded successfully: " + imageName);
                return image;
            } catch (Exception e) {
                logger.severe("Failed to load image: " + path + " - " + e.getMessage());
                throw new CompletionException(e);
            }
        }, executorService);
    }
    
    public CompletableFuture<List<String>> loadTextFileAsync(String fileName) {
        return CompletableFuture.supplyAsync(() -> {
            List<String> cached = textCache.get(fileName);
            if (cached != null) {
                logger.fine("Text file loaded from cache: " + fileName);
                return cached;
            }
            
            String path = AppConfig.DATA_PATH + fileName;
            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    Objects.requireNonNull(getClass().getResourceAsStream(path)),
                    StandardCharsets.UTF_8
                )
            )) {
                List<String> lines = reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .toList();
                
                textCache.put(fileName, lines);
                logger.info("Text file loaded successfully: " + fileName);
                return lines;
            } catch (Exception e) {
                logger.severe("Failed to load text file: " + path + " - " + e.getMessage());
                throw new CompletionException(e);
            }
        }, executorService);
    }
    
    public CompletableFuture<Parent> loadFXMLAsync(String fxmlName) {
        return CompletableFuture.supplyAsync(() -> {
            Parent cached = fxmlCache.get(fxmlName);
            if (cached != null) {
                logger.fine("FXML loaded from cache: " + fxmlName);
                return cached;
            }
            
            String path = AppConfig.FXML_PATH + fxmlName;
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
                Parent root = loader.load();
                fxmlCache.put(fxmlName, root);
                return root;
            } catch (Exception e) {
                logger.severe("Failed to load FXML: " + path + " - " + e.getMessage());
                throw new CompletionException(e);
            }
        }, executorService);
    }
    
    public Task<Void> loadResourcesWithProgress(List<String> resources) {
        return new Task<>() {
            @Override
            protected Void call() {
                int total = resources.size();
                AtomicInteger completed = new AtomicInteger(0);
                
                List<CompletableFuture<?>> futures = new ArrayList<>();
                
                for (String resource : resources) {
                    CompletableFuture<?> future = switch (getResourceType(resource)) {
                        case IMAGE -> loadImageAsync(resource);
                        case TEXT -> loadTextFileAsync(resource);
                        case FXML -> loadFXMLAsync(resource);
                        default -> CompletableFuture.completedFuture(null);
                    };
                    
                    future.thenRun(() -> {
                        updateProgress(completed.incrementAndGet(), total);
                        updateMessage("Loaded: " + resource);
                    }).exceptionally(e -> {
                        logger.warning("Failed to load resource: " + resource + " - " + e.getMessage());
                        return null;
                    });
                    
                    futures.add(future);
                }
                
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                return null;
            }
        };
    }
    
    private enum ResourceType {
        IMAGE, TEXT, FXML, UNKNOWN
    }
    
    private ResourceType getResourceType(String resource) {
        String lowerCaseResource = resource.toLowerCase();
        if (lowerCaseResource.endsWith(".png") || lowerCaseResource.endsWith(".jpg")) {
            return ResourceType.IMAGE;
        } else if (lowerCaseResource.endsWith(".txt")) {
            return ResourceType.TEXT;
        } else if (lowerCaseResource.endsWith(".fxml")) {
            return ResourceType.FXML;
        } else {
            return ResourceType.UNKNOWN;
        }
    }
    
    public void clearCache(ResourceType type) {
        switch (type) {
            case IMAGE -> imageCache.clear();
            case TEXT -> textCache.clear();
            case FXML -> fxmlCache.clear();
            default -> clearAllCaches();
        }
        logger.info("Cleared cache for type: " + type);
    }
    
    public void clearAllCaches() {
        imageCache.clear();
        fontCache.clear();
        soundCache.clear();
        textCache.clear();
        fxmlCache.clear();
        logger.info("All caches cleared");
    }
    
    @Override
    public void close() {
        try {
            executorService.shutdown();
            if (!executorService.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
            clearAllCaches();
            logger.info("ResourceLoader shutdown completed");
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            logger.severe("ResourceLoader shutdown interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}