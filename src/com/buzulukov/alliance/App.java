package com.buzulukov.alliance;

import com.buzulukov.alliance.api.MessengersAdapter;
import com.buzulukov.alliance.controllers.AccountsController;
import com.buzulukov.alliance.controllers.MainController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class App extends Application {

    public static final MessengersAdapter MESSENGERS_ADAPTER = MessengersAdapter.getInstance();

    public static Stage accountsStage;
    public static Stage mainStage;
    public static Stage settingsStage;
    public static Stage newAccountStage;
    public static Parent accountsRoot;
    public static Parent mainRoot;
    public static Parent settingsRoot;
    public static Parent newAccountRoot;

    @Override
    public void start(Stage mainStage) throws Exception {
        mainRoot = FXMLLoader.load(App.class.getResource("layouts/main.fxml"));
        mainRoot.getStylesheets().addAll(
                "com/buzulukov/alliance/styles/chat.css",
                "com/buzulukov/alliance/styles/dialogs.css");
        App.mainStage = mainStage;
        mainStage.setScene(new Scene(mainRoot));
        mainStage.setTitle("Alliance");
        mainStage.getIcons().add(new Image("com/buzulukov/alliance/resources/logo-main.png"));

        initializeSettings();

        loadAccounts("accounts.data");

        mainStage.setOnCloseRequest(event -> {
            try {
                MESSENGERS_ADAPTER.saveAccounts("accounts.data");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        mainStage.show();
    }

    private void loadAccounts(String path) throws ClassNotFoundException {
        try {
            MESSENGERS_ADAPTER.loadAccounts(path);
            Platform.runLater(() -> MainController.getInstance().updateDialogsScreen(""));
        } catch (IOException e) {
            System.out.println("No saved accounts found");
        }
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

    public static void showNewAccountWindow() {
        if (newAccountStage == null) {
            try {
                newAccountRoot = FXMLLoader.load(App.class.getResource("layouts/newAccount.fxml"));
                newAccountRoot.getStylesheets().add("com/buzulukov/alliance/styles/dialogs.css");
            } catch (IOException e) {
                e.printStackTrace();
            }
            newAccountStage = new Stage();
            newAccountStage.setWidth(245);
            newAccountStage.setHeight(130);
            newAccountStage.setResizable(false);
            newAccountStage.setScene(new Scene(newAccountRoot));
            newAccountStage.initModality(Modality.WINDOW_MODAL);
            newAccountStage.initOwner(mainStage);
            newAccountStage.initStyle(StageStyle.TRANSPARENT);
        }
        newAccountStage.setX(mainStage.getX() + (mainStage.getWidth() - newAccountStage.getWidth()) / 2);
        newAccountStage.setY(mainStage.getY() + (mainStage.getHeight() - newAccountStage.getHeight()) / 2);
        newAccountStage.show();
    }

    public static void showAccountsWindow() {
        if (accountsStage == null) {
            try {
                accountsRoot = FXMLLoader.load(App.class.getResource("layouts/accounts.fxml"));
                accountsRoot.getStylesheets().add("com/buzulukov/alliance/styles/dialogs.css");
            } catch (IOException e) {
                e.printStackTrace();
            }
            accountsStage = new Stage();
            accountsStage.setWidth(350);
            accountsStage.setHeight(400);
            accountsStage.setResizable(false);
            accountsStage.setScene(new Scene(accountsRoot));
            accountsStage.initModality(Modality.WINDOW_MODAL);
            accountsStage.initOwner(mainStage);
            accountsStage.initStyle(StageStyle.TRANSPARENT);
        }
        AccountsController.getInstance().updateAccountsScreen();
        accountsStage.setX(mainStage.getX() + (mainStage.getWidth() - accountsStage.getWidth()) / 2);
        accountsStage.setY(mainStage.getY() + (mainStage.getHeight() - accountsStage.getHeight()) / 2);
        accountsStage.show();
    }
}
