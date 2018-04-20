package com.buzulukov.alliance;

import com.buzulukov.alliance.api.MessengersAdapter;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.IOException;

public class App extends Application {

    public static final MessengersAdapter MESSENGERS_ADAPTER = MessengersAdapter.getInstance();

    public static Stage mainStage;
    public static Stage settingsStage;
    public static Parent mainRoot;
    public static Parent settingsRoot;

    @Override
    public void start(Stage mainStage) throws Exception {
        mainRoot = FXMLLoader.load(App.class.getResource("layouts/main.fxml"));
        mainRoot.getStylesheets().addAll(
                "com/buzulukov/alliance/styles/chat.css",
                "com/buzulukov/alliance/styles/dialogs.css");
        App.mainStage = mainStage;
        mainStage.setScene(new Scene(mainRoot));
        mainStage.setTitle("Alliance");

        initializeSettings();

        mainStage.show();
    }

    private void initializeSettings() throws IOException {
        settingsRoot = FXMLLoader.load(App.class.getResource("layouts/settings.fxml"));
        settingsRoot.setLayoutX(-250);

        Scene settingsScene = new Scene(settingsRoot);
        settingsScene.setFill(Color.TRANSPARENT);
        settingsScene.getStylesheets().addAll(
                "com/buzulukov/alliance/styles/settings.css");

        settingsStage = new Stage();
        settingsStage.setScene(settingsScene);
        settingsStage.initStyle(StageStyle.TRANSPARENT);
        settingsStage.setHeight(500);
        settingsStage.setWidth(250);
        settingsStage.setResizable(false);
        settingsStage.focusedProperty().addListener((focused, was, now) -> {
            if (!now) {
                settingsStage.close();
                settingsRoot.setLayoutX(-250);
            }
            settingsStage.setY(mainStage.getY());
            settingsStage.setX(mainStage.getX() + 7);
        });
    }
}
