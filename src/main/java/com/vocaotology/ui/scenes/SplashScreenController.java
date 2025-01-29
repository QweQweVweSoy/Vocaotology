package com.vocaotology.ui.scenes;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.concurrent.Task;

public class SplashScreenController {
    
    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label statusLabel;

    // Method to bind progress and status
    public void bindProgress(Task<Void> loadingTask) {
        progressBar.progressProperty().bind(loadingTask.progressProperty());
        statusLabel.textProperty().bind(loadingTask.messageProperty());
    }

    // Additional methods for any interactions during splash loading
}
