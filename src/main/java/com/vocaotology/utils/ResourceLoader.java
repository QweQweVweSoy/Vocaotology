package com.vocaotology.utils;

import com.vocaotology.config.AppConfig;

import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.text.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.application.Platform;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

public class ResourceLoader {
    private static ResourceLoader instance;
    
    // Cache for different resource types
    private final Map<String, Image> imageCache;
    private final Map<String, Font> fontCache;
    private final Map<String, Media> soundCache;
    private final Map<String, List<String>> textCache;
    private final Map<String, Parent> fxmlCache;
        private final Logger logger;
    private final ExecutorService executorService;
    
    private ResourceLoader() {
        imageCache = new HashMap<>();
        fontCache = new HashMap<>();
        soundCache = new HashMap<>();
        textCache = new HashMap<>();
        fxmlCache = new HashMap<>();
        executorService = Executors.newFixedThreadPool(3); // Pool for async loading
        
        // Initialize logger
        logger = Logger.getLogger(ResourceLoader.class.getName());
        setupLogger();
    }
    
    private void setupLogger() {
        try {
            // Create logs directory if it doesn't exist
            File logsDir = new File("logs");
            if (!logsDir.exists()) {
                logsDir.mkdir();
            }
            
            // Create log file with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            FileHandler fileHandler = new FileHandler("logs/resource_loader_" + timestamp + ".log", true);
            
            // Format the log messages
            SimpleFormatter formatter = new SimpleFormatter() {
                @Override
                public String format(LogRecord record) {
                    return String.format("[%s] %s: %s%n",
                            record.getLevel(),
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                            record.getMessage());
                }
            };
            
            fileHandler.setFormatter(formatter);
            logger.addHandler(fileHandler);
            logger.setLevel(Level.ALL);
            
        } catch (IOException e) {
            System.err.println("Failed to setup logger: " + e.getMessage());
        }
    }

    // Async image loading
    public CompletableFuture<Image> loadImageAsync(String imageName) {
        return CompletableFuture.supplyAsync(() -> {
            String path = AppConfig.IMAGES_PATH + imageName;
            try {
                if (imageCache.containsKey(imageName)) {
                    logger.info("Image loaded from cache: " + imageName);
                    return imageCache.get(imageName);
                }
                
                Image image = new Image(getClass().getResourceAsStream(path));
                Platform.runLater(() -> imageCache.put(imageName, image));
                logger.info("Image loaded successfully: " + imageName);
                return image;
            } catch (Exception e) {
                logger.severe("Failed to load image: " + path + " - " + e.getMessage());
                throw new CompletionException(e);
            }
        }, executorService);
    }

    // Async text file loading
    public CompletableFuture<List<String>> loadTextFileAsync(String fileName) {
        return CompletableFuture.supplyAsync(() -> {
            String path = AppConfig.DATA_PATH + fileName;
            List<String> lines = new ArrayList<>();
            
            try (InputStream is = getClass().getResourceAsStream(path);
                 BufferedReader reader = new BufferedReader(
                     new InputStreamReader(is, StandardCharsets.UTF_8))) {
                
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        lines.add(line.trim());
                    }
                }
                logger.info("Text file loaded successfully: " + fileName);
                Platform.runLater(() -> textCache.put(fileName, lines));
                return lines;
            } catch (Exception e) {
                logger.severe("Failed to load text file: " + path + " - " + e.getMessage());
                throw new CompletionException(e);
            }
        }, executorService);
    }

    // Batch loading of resources
    public CompletableFuture<Void> loadResourcesBatch(List<String> imageNames, List<String> textFiles) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        
        // Queue up all image loads
        for (String imageName : imageNames) {
            futures.add(loadImageAsync(imageName));
        }
        
        // Queue up all text file loads
        for (String textFile : textFiles) {
            futures.add(loadTextFileAsync(textFile));
        }
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .whenComplete((result, throwable) -> {
                if (throwable != null) {
                    logger.severe("Batch loading failed: " + throwable.getMessage());
                } else {
                    logger.info("Batch loading completed successfully");
                }
            });
    }

    // Example usage of loading with progress tracking
    public Task<Void> loadResourcesWithProgress(List<String> resources) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                int total = resources.size();
                int completed = 0;
                
                for (String resource : resources) {
                    try {
                        if (resource.endsWith(".png") || resource.endsWith(".jpg")) {
                            loadImageAsync(resource).get(); // Wait for completion
                        } else if (resource.endsWith(".txt")) {
                            loadTextFileAsync(resource).get(); // Wait for completion
                        }
                        
                        completed++;
                        updateProgress(completed, total);
                        updateMessage("Loading: " + resource);
                    } catch (Exception e) {
                        logger.warning("Failed to load resource: " + resource + " - " + e.getMessage());
                    }
                }
                return null;
            }
        };
    }

    // Resource cleanup
    public void shutdown() {
        try {
            executorService.shutdown();
            if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
            logger.info("ResourceLoader shutdown completed");
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            logger.severe("ResourceLoader shutdown interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }



    
    public static ResourceLoader getInstance() {
        if (instance == null) {
            instance = new ResourceLoader();
        }
        return instance;
    }
    
    // Image loading
    public Image loadImage(String imageName) {
        return imageCache.computeIfAbsent(imageName, key -> {
            String path = AppConfig.IMAGES_PATH + key;
            try {
                return new Image(getClass().getResourceAsStream(path));
            } catch (Exception e) {
                System.err.println("Failed to load image: " + path);
                e.printStackTrace();
                return null;
            }
        });
    }
    
    // Font loading
    public Font loadFont(String fontName, double size) {
        String cacheKey = fontName + "_" + size;
        return fontCache.computeIfAbsent(cacheKey, key -> {
            String path = AppConfig.FONTS_PATH + fontName;
            try {
                InputStream is = getClass().getResourceAsStream(path);
                return Font.loadFont(is, size);
            } catch (Exception e) {
                System.err.println("Failed to load font: " + path);
                e.printStackTrace();
                return null;
            }
        });
    }
    
    // Sound loading
    public Media loadSound(String soundName) {
        return soundCache.computeIfAbsent(soundName, key -> {
            String path = AppConfig.SOUNDS_PATH + key;
            try {
                return new Media(getClass().getResource(path).toExternalForm());
            } catch (Exception e) {
                System.err.println("Failed to load sound: " + path);
                e.printStackTrace();
                return null;
            }
        });
    }
    
    // Text file loading (for dictionary, word lists, etc.)
    public List<String> loadTextFile(String fileName) {
        return textCache.computeIfAbsent(fileName, key -> {
            String path = AppConfig.DATA_PATH + key;
            List<String> lines = new ArrayList<>();
            
            try (InputStream is = getClass().getResourceAsStream(path);
                 BufferedReader reader = new BufferedReader(
                     new InputStreamReader(is, StandardCharsets.UTF_8))) {
                
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        lines.add(line.trim());
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to load text file: " + path);
                e.printStackTrace();
            }
            
            return lines;
        });
    }
    
    // Clear specific cache
    public void clearImageCache() {
        imageCache.clear();
    }
    
    public void clearFontCache() {
        fontCache.clear();
    }
    
    public void clearSoundCache() {
        soundCache.clear();
    }
    
    public void clearTextCache() {
        textCache.clear();
    }
    
    // Clear all caches
    public void clearAllCaches() {
        clearImageCache();
        clearFontCache();
        clearSoundCache();
        clearTextCache();
    }
    
    // Check if resource exists
    public boolean resourceExists(String path) {
        return getClass().getResource(path) != null;
    }
    
    // Get resource as stream
    public InputStream getResourceAsStream(String path) {
        return getClass().getResourceAsStream(path);
    }

    // Add FXML loading method
    public Parent loadFXML(String fxmlName) {
        return fxmlCache.computeIfAbsent(fxmlName, key -> {
            String path = AppConfig.FXML_PATH + key;
            try {
                return FXMLLoader.load(getClass().getResource(path));
            } catch (Exception e) {
                logger.severe("Failed to load FXML: " + path + " - " + e.getMessage());
                return null;
            }
        });
    }
    
    // Async FXML loading
    public CompletableFuture<Parent> loadFXMLAsync(String fxmlName) {
        return CompletableFuture.supplyAsync(() -> {
            String path = AppConfig.FXML_PATH + fxmlName;
            try {
                Parent root = FXMLLoader.load(getClass().getResource(path));
                Platform.runLater(() -> fxmlCache.put(fxmlName, root));
                return root;
            } catch (Exception e) {
                logger.severe("Failed to load FXML: " + path + " - " + e.getMessage());
                throw new CompletionException(e);
            }
        }, executorService);
    }

}