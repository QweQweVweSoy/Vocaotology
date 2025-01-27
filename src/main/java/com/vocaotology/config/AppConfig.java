package com.vocaotology.config;

public final class AppConfig {
    // Application metadata
    public static final String APP_NAME = "Vocaotology";
    public static final String APP_VERSION = "1.0.0";
    
    // Window configuration
    public static final int WINDOW_WIDTH = 1280;
    public static final int WINDOW_HEIGHT = 720;
    public static final String WINDOW_TITLE = APP_NAME;
    public static final boolean RESIZABLE = false;
    
    // Resource paths
    public static final String FXML_PATH = "/fxml/";
    public static final String IMAGES_PATH = "/assets/images/";
    public static final String FONTS_PATH = "/assets/fonts/";
    public static final String SOUNDS_PATH = "/assets/sounds/";
    public static final String DATA_PATH = "/data/";
    
    // File names
    public static final String DICTIONARY_FILE = "dictionary.txt";
    public static final String WORD_CLASS_FILE = "wordclass.txt";
    public static final String WORD_HISTORY_FILE = "wordHistory.txt";
    
    // Game settings
    public static final int DEFAULT_TIME_LIMIT = 180; // in seconds
    public static final int MIN_WORD_LENGTH = 3;
    public static final int MAX_WORD_LENGTH = 15;
    
    // Performance settings
    public static final int MAX_FPS = 60;
    public static final int BUFFER_SIZE = 4096;
    
    // CSS and styling
    public static final String STYLE_SHEET = "/styles/application.css";
    public static final String DEFAULT_FONT = "ToThePoint.ttf";
    
    private AppConfig() {
        // Prevent instantiation
        throw new UnsupportedOperationException("Configuration class");
    }
}