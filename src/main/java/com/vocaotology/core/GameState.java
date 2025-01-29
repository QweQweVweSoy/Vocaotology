package com.vocaotology.core;
public enum GameState {
    LOADING("Loading game resources..."),
    MAIN_MENU("Main Menu"),
    SETTINGS("Settings"),
    TUTORIAL("How to Play"),
    GAME_SETUP("Game Setup"),
    PLAYING("In Game"),
    PAUSED("Game Paused"),
    GAME_OVER("Game Over"),
    HIGH_SCORES("High Scores"),
    STATS("Statistics"),
    EXIT("Exiting...");

    private final String description;

    GameState(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPlayingState() {
        return this == PLAYING || this == PAUSED;
    }

    public boolean isMenuState() {
        return this == MAIN_MENU || this == SETTINGS || this == TUTORIAL;
    }

    public boolean allowsPause() {
        return this == PLAYING;
    }
}