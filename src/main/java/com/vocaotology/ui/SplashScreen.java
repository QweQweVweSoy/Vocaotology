package com.vocaotology.ui;

import javafx.scene.Scene;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.geometry.Pos;
public class SplashScreen {
    private final ProgressBar progressBar;
    private final Stage splashStage;
    private final Label statusLabel;

    public SplashScreen(Image logo, String initialStatus) {
        splashStage = new Stage(StageStyle.UNDECORATED);
        progressBar = new ProgressBar(0);
        statusLabel = new Label(initialStatus);

        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);
        root.getChildren().addAll(new ImageView(logo), progressBar, statusLabel);

        Scene scene = new Scene(root);
        splashStage.setScene(scene);
    }

    public void start(Stage mainStage, Task<Void> loadingTask) {
        splashStage.show();
        progressBar.progressProperty().bind(loadingTask.progressProperty());
        statusLabel.textProperty().bind(loadingTask.messageProperty());

        loadingTask.setOnSucceeded(e -> {
            splashStage.hide();
            mainStage.show();
        });

        new Thread(loadingTask).start();
    }
}
