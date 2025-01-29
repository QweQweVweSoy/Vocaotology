package com.vocaotology.config;
public final class GameConfig {
    // Scoring configuration
    public static final int BASE_WORD_SCORE = 10;
    public static final int LETTER_MULTIPLIER = 2;
    public static final int SPECIAL_WORD_MULTIPLIER = 3;
    
    // Difficulty settings
    public enum Difficulty {
        EASY(1.0f),
        MEDIUM(1.5f),
        HARD(2.0f);
        
        private final float multiplier;
        
        Difficulty(float multiplier) {
            this.multiplier = multiplier;
        }
        
        public float getMultiplier() {
            return multiplier;
        }
    }
    
    // Game modes
    public enum GameMode {
        TIMED,      // Play against the clock
        ENDLESS,    // Play until mistake
        CHALLENGE   // Special challenge mode
    }
    
    // Time settings (in seconds)
    public static final int EASY_TIME_LIMIT = 240;
    public static final int MEDIUM_TIME_LIMIT = 180;
    public static final int HARD_TIME_LIMIT = 120;
    
    // Achievement thresholds
    public static final int BRONZE_SCORE = 1000;
    public static final int SILVER_SCORE = 5000;
    public static final int GOLD_SCORE = 10000;
    
    private GameConfig() {
        // Prevent instantiation
        throw new UnsupportedOperationException("Configuration class");
    }
}