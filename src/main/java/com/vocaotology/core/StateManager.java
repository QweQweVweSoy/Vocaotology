package com.vocaotology.core;

import java.util.Stack;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import com.vocaotology.core.GameState;

public class StateManager {
    private static StateManager instance;
    private final Stack<GameState> stateStack;
    private final ObjectProperty<GameState> currentState;
    
    private StateManager() {
        stateStack = new Stack<>();
        currentState = new SimpleObjectProperty<>(GameState.LOADING);
        stateStack.push(GameState.LOADING);
    }
    
    public static StateManager getInstance() {
        if (instance == null) {
            instance = new StateManager();
        }
        return instance;
    }
    
    public void changeState(GameState newState) {
        if (currentState.get() != newState) {
            stateStack.push(newState);
            currentState.set(newState);
            onStateChanged(newState);
        }
    }
    
    public void revertToPreviousState() {
        if (stateStack.size() > 1) {
            stateStack.pop(); // Remove current state
            GameState previousState = stateStack.peek();
            currentState.set(previousState);
            onStateChanged(previousState);
        }
    }
    
    public GameState getCurrentState() {
        return currentState.get();
    }
    
    public ObjectProperty<GameState> currentStateProperty() {
        return currentState;
    }
    
    private void onStateChanged(GameState newState) {
        // Override this method in a subclass or add listeners
        // to handle state transition logic
        System.out.println("State changed to: " + newState.getDescription());
    }
    
    public boolean canTransitionTo(GameState newState) {
        GameState current = getCurrentState();
        
        switch (current) {
            case LOADING:
                return newState == GameState.MAIN_MENU;
                
            case MAIN_MENU:
                return newState != GameState.LOADING;
                
            case PLAYING:
                return newState == GameState.PAUSED || 
                       newState == GameState.GAME_OVER || 
                       newState == GameState.MAIN_MENU;
                
            case PAUSED:
                return newState == GameState.PLAYING || 
                       newState == GameState.MAIN_MENU;
                
            // Handle all other states
            case GAME_OVER:
            case SETTINGS:
            case TUTORIAL:
            case GAME_SETUP:
            case HIGH_SCORES:
            case STATS:
            case EXIT:
                return true;
                
            default:
                return false;
        }
    }
}