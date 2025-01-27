module com.vocaotology {
    requires javafx.controls;
    requires javafx.web;
    requires javafx.fxml;
    requires javafx.media;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires java.desktop;
    requires java.logging;

    opens com.vocaotology to javafx.fxml;
     opens com.vocaotology.config to javafx.fxml;
     opens com.vocaotology.ui to javafx.fxml;
     opens com.vocaotology.game to javafx.fxml;
     opens com.vocaotology.core to javafx.fxml;
     opens com.vocaotology.data to javafx.fxml;
     opens com.vocaotology.utils to javafx.fxml;

    exports com.vocaotology;
    //exports com.vocaotology.ui;
    //exports com.vocaotology.data;	
    //exports com.vocaotology.game;
    exports com.vocaotology.core;
    exports com.vocaotology.utils;
    exports com.vocaotology.config;
}

