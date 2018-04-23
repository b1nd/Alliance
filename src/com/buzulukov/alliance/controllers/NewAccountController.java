package com.buzulukov.alliance.controllers;

import com.buzulukov.alliance.App;
import com.buzulukov.alliance.web.utils.WebUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewAccountController {

    @FXML
    public ComboBox<String> accChoiceComboBox;

    public void initialize() {
        ObservableList<String> items = FXCollections.observableArrayList(App.MESSENGERS_ADAPTER.getMessengerNames());
        accChoiceComboBox.setItems(items);
        accChoiceComboBox.setValue(items.get(0));
    }


    public void onLoginClicked(ActionEvent actionEvent) {
        LoginFacade loginFacade = new LoginFacade();

        if (accChoiceComboBox.getValue().equals(App.MESSENGERS_ADAPTER.getMessengerNames()[0])) {
            loginFacade.loginVK();
        }
    }

    public void onCancelClicked(ActionEvent actionEvent) {
        App.newAccountStage.close();
    }

    public void onUpdateClicked(ActionEvent actionEvent) {
        Platform.runLater(() -> MainController.getInstance().updateDialogsScreen());
        App.newAccountStage.close();
    }

    private class LoginFacade {

        void loginVK() {
            final String API_VERSION = "5.71";
            final int CLIENT_ID = 6447450;
            final String REDIRECT_URI = "https://oauth.vk.com/blank.html";
            final Pattern LOGIN_PATTERN = Pattern.compile(
                    "https://oauth.vk.com/blank.html#access_token=([0-9a-z]+)&expires_in=[0-9]+&user_id=([0-9]+)");
            final String[] ACCESS_SCOPES = {
                    "offline",
                    "messages"
            };
            StringBuilder scopes = new StringBuilder();

            for (int i = 0; i < ACCESS_SCOPES.length - 1; ++i) {
                scopes.append(ACCESS_SCOPES[i]).append(",");
            }
            scopes.append(ACCESS_SCOPES[ACCESS_SCOPES.length - 1]);

            final String LOGIN_URL = WebUtils.getUrl("https://oauth.vk.com/authorize",
                    "client_id=" + CLIENT_ID,
                    "display=page",
                    "redirect_uri=" + REDIRECT_URI,
                    "scope=" + scopes,
                    "response_type=token",
                    "v=" + API_VERSION);

            Stage stage = new Stage();
            stage.setWidth(672);
            stage.setHeight(388);

            WebView webView = new WebView();
            stage.setScene(new Scene(webView));
            stage.show();

            WebEngine webEngine = webView.getEngine();
            webEngine.load(LOGIN_URL);
            webEngine.locationProperty().addListener((observable, oldLocation, newLocation) -> {
                if (newLocation.contains(REDIRECT_URI)) {
                    Matcher matcher = LOGIN_PATTERN.matcher(newLocation);

                    if (matcher.matches()) {
                        String accessToken = matcher.group(1);
                        String userId = matcher.group(2);

                        if (App.MESSENGERS_ADAPTER.authorize(accChoiceComboBox.getValue(), accessToken, userId)) {
                            java.net.CookieHandler.setDefault(new java.net.CookieManager());
                            stage.close();
                        }
                    }
                }
            });
        }
    }
}
