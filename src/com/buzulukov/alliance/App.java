package com.buzulukov.alliance;

import com.buzulukov.alliance.api.MessengersAdapter;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    public static final MessengersAdapter MESSENGERS_ADAPTER = MessengersAdapter.getInstance();
    private Stage  stage;
    private Parent mainRoot;

    @Override
    public void start(Stage stage) throws Exception {
        mainRoot = FXMLLoader.load(App.class.getResource("layouts/main.fxml"));
        //mainRoot.getStylesheets().add("com/buzulukov/alliance/styles/chat.css");
        this.stage = stage;
        stage.setScene(new Scene(mainRoot));
        stage.show();
    }
}
