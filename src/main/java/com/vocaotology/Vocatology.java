package com.vocaotology;
import com.vocaotology.config.*;
import javafx.application.Application;
import javafx.stage.Stage;
public class Vocatology extends Application{
    @Override
    public void start(Stage primaryStage){
        primaryStage.setTitle(AppConfig.WINDOW_TITLE);

    }
    public static void main(String [] args){
        launch(args);
    }
}