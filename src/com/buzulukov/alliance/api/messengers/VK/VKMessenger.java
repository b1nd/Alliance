package com.buzulukov.alliance.api.messengers.VK;

import com.buzulukov.alliance.api.messengers.Chat;
import com.buzulukov.alliance.api.messengers.Messenger;
import com.buzulukov.alliance.web.utils.WebUtils;
import com.google.gson.JsonParser;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VKMessenger implements Messenger {

    private static final String API_LIBRARY_NAME = "VK";
    private static final String API_VERSION = "5.71";

    private static final String REDIRECT_URI = "https://oauth.vk.com/blank.html";
    private static final int CLIENT_ID = 6338916;
    private static final String[] ACCESS_SCOPES = {
            "offline",
            "messages"
    };
    private static final Pattern LOGIN_PATTERN = Pattern.compile(
            "https://oauth.vk.com/blank.html#access_token=([0-9a-z]+)&expires_in=[0-9]+&user_id=([0-9]+)");

    private String accessToken;
    private String accountInfo;
    private int userId;
    private boolean isAuthorized;

    private LinkedList<Chat> chats;

    private Random random;

    private JsonParser jsonParser;

    public VKMessenger() {
        jsonParser = new JsonParser();
        random = new Random();
    }

    @Override
    public String getName() {
        return API_LIBRARY_NAME;
    }

    @Override
    public void login(String loginType) {
        var chooseLogin = new VKLoginFacade();

        if (loginType == null) {
            throw new NullPointerException("Login type can't be null");
        } else if (loginType.equalsIgnoreCase("desktop")) {
            chooseLogin.loginJavafx();
        } else {
            throw new IllegalArgumentException("No supported loginType");
        }
    }

    @Override
    public void logout() {

    }

    @Override
    public boolean isAuthorized() {
        return isAuthorized;
    }

    @Override
    public String getAccountInfo() {
        return accountInfo;
    }

    @Override
    public LinkedList<Chat> getChats() {
        return chats;
    }

    private class VKLoginFacade {
        private String url;

        VKLoginFacade() {
            StringBuilder scopes = new StringBuilder();
            Arrays.stream(ACCESS_SCOPES).forEach(s -> scopes.append(s).append(","));

            url = WebUtils.getUrl("https://oauth.vk.com/authorize",
                    "client_id=" + CLIENT_ID,
                    "display=page",
                    "redirect_uri=" + REDIRECT_URI,
                    "scopes=" + scopes,
                    "response_type=token",
                    "v=" + API_VERSION);
        }

        void loginJavafx() {
            Stage stage = new Stage();
            stage.setWidth(672);
            stage.setHeight(388);

            WebView webView = new WebView();
            stage.setScene(new Scene(webView));
            stage.show();

            WebEngine webEngine = webView.getEngine();
            webEngine.load(url);
            webEngine.locationProperty().addListener((observable, oldLocation, newLocation) -> {
                if (newLocation.contains(REDIRECT_URI)) {
                    Matcher matcher = LOGIN_PATTERN.matcher(newLocation);

                    if (matcher.matches()) {
                        accessToken = matcher.group(1);
                        userId = Integer.parseInt(matcher.group(2));
                        isAuthorized = true;
                        //saveAccountData();
                        stage.close();
                    }
                }
            });
        }
    }
}