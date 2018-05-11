package com.buzulukov.alliance.controllers;

import com.buzulukov.alliance.App;
import com.buzulukov.alliance.api.messengers.Slack.SlackMessenger;
import com.buzulukov.alliance.api.web.utils.WebUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewAccountController {

    @FXML public ComboBox<String> accChoiceComboBox;

    public void initialize() {
        ObservableList<String> items = FXCollections.observableArrayList(App.MESSENGERS_ADAPTER.getMessengerNames());
        accChoiceComboBox.setItems(items);
        accChoiceComboBox.setValue(items.get(0));
    }


    public void onLoginClicked(ActionEvent actionEvent) {
        LoginFacade loginFacade = new LoginFacade();

        if (accChoiceComboBox.getValue().equals(App.MESSENGERS_ADAPTER.getMessengerNames()[0])) {
            loginFacade.loginVK();
        } else if (accChoiceComboBox.getValue().equals(App.MESSENGERS_ADAPTER.getMessengerNames()[1])) {
            loginFacade.loginSlack();
        }
    }

    public void onCancelClicked(ActionEvent actionEvent) {
        App.newAccountStage.close();
    }

    public void onUpdateClicked(ActionEvent actionEvent) {
        Platform.runLater(() -> MainController.getInstance().updateDialogsScreen(""));
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

        void loginSlack() {
            final String clientID = "323630498438.322130661072";
            final String clientSecret = "3e4839a8e9cb7b4bee4509871dcc8b36";
            final String[] accessScopes = {
                    "channels:history",
                    "channels:read",
                    "channels:write",
                    "chat:write:bot",
                    "chat:write:user",
                    "files:read",
                    "groups:history",
                    "groups:read",
                    "groups:write",
                    "im:history",
                    "im:read",
                    "im:write",
                    "users:read"
            };
            //String redirectUri = "https://slack.com";
            StringBuilder permissions = new StringBuilder();
            Arrays.stream(accessScopes).forEach(s -> permissions.append(s).append(","));
            String url = "https://slack.com/oauth/authorize?" +
                    "client_id=" + clientID +
                    "&scope=" + permissions + "users:read.email";
            //"&redirect_uri=" + redirectUri;

            Stage stage = new Stage();
            stage.setTitle("Authorization");
            stage.setMinWidth(650);
            stage.setMinHeight(600);
            stage.setWidth(650);
            stage.setHeight(600);

            WebView webView = new WebView();
            stage.setScene(new Scene(webView));
            stage.show();

            WebEngine webEngine = webView.getEngine();
            webEngine.load(url);
            webEngine.locationProperty().addListener(new ChangeListener<String>() {
                Pattern pattern = Pattern.compile("https://slack\\.com/\\?code=([0-9a-z]+\\.[0-9a-z]+\\.[0-9a-z]+)&state=");

                @Override
                public void changed(ObservableValue<? extends String> observable, String oldLocation, String newLocation) {
                    Matcher matcher = pattern.matcher(newLocation);
                    if (matcher.matches()) {
                        String code = matcher.group(1);

                        String response;
                        JsonObject responseObject;

                        response = WebUtils.getResponse(SlackMessenger.METHOD_URI + "oauth.access",
                                "client_id=" + clientID,
                                "client_secret=" + clientSecret,
                                "code=" + code);
                        responseObject = new JsonParser().parse(response).getAsJsonObject();
                        final String accessToken = responseObject.get("access_token").getAsString();

                        response = WebUtils.getResponse(SlackMessenger.METHOD_URI + "auth.test",
                                "token=" + accessToken);
                        responseObject = new JsonParser().parse(response).getAsJsonObject();
                        final String userId = responseObject.get("user_id").getAsString();

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
