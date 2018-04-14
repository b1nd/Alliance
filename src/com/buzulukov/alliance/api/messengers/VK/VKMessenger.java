package com.buzulukov.alliance.api.messengers.VK;

import com.buzulukov.alliance.api.messengers.Chat;
import com.buzulukov.alliance.api.messengers.Messenger;
import com.buzulukov.alliance.web.utils.WebUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VKMessenger implements Messenger, Serializable {

    public static final String LIBRARY_NAME = "VK";

    private static final String API_VERSION = "5.71";

    private static final String REDIRECT_URI = "https://oauth.vk.com/blank.html";
    private static final String METHOD_URI = "https://api.vk.com/method/";
    private static final int CLIENT_ID = 6447450;
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
        return LIBRARY_NAME;
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
        isAuthorized = false;
    }

    @Override
    public boolean isAuthorized() {
        return isAuthorized;
    }

    @Override
    public String getAccountInfo() {
        if (accountInfo == null) {
            String response = WebUtils.getResponse(METHOD_URI + "account.getProfileInfo",
                    "access_token=" + accessToken,
                    "v=" + API_VERSION);
            var responseObject = jsonParser.parse(response).getAsJsonObject().get("response").getAsJsonObject();
            accountInfo = responseObject.get("first_name").getAsString() + " " +
                    responseObject.get("last_name").getAsString();
        }
        return accountInfo;
    }

    @Override
    public LinkedList<Chat> getChats() {
        if (chats == null) {
            chats = new LinkedList<>();
            var userIds = new StringBuilder();
            var response = WebUtils.getResponse(METHOD_URI + "messages.getDialogs",
                    "access_token=" + accessToken,
                    "v=" + API_VERSION,
                    "count=200");
            var responseObject = getJsonObjectFromResponse(response);
            var itemsArray = responseObject.get("items").getAsJsonArray();

            for (int i = 0; i < itemsArray.size(); ++i) {
                var messageObject = itemsArray
                        .get(i).getAsJsonObject()
                        .get("message").getAsJsonObject();

                //Пропуск сообщений из сообществ
                if (messageObject.get("user_id").getAsInt() < 0) {
                    continue;
                }
                var chatTitle = messageObject.get("title").getAsString();
                int chatId = 0;

                if (messageObject.has("chat_id")) {
                    chatId = messageObject.get("chat_id").getAsInt();
                } else {
                    if (userIds.length() != 0) {
                        userIds.append(",");
                    }
                    userIds.append(messageObject.get("user_id").getAsString());
                }
                VKChat chat = new VKChat(chatTitle, chatId);
                VKMessage message = createMessageFromJsonObject(messageObject);
                chat.messages.addLast(message);
                chats.addLast(chat);
            }

            if (userIds.length() != 0) {
                response = WebUtils.getResponse(METHOD_URI + "users.get",
                        "access_token=" + accessToken,
                        "v=" + API_VERSION,
                        "user_ids=" + userIds.toString());

                var responseArray = getJsonArrayFromResponse(response);
                int i = 0;

                for (Chat chat : chats) {
                    if (chat.getTitle().isEmpty()) {
                        JsonObject userObject = responseArray.get(i).getAsJsonObject();
                        String firstName = userObject.get("first_name").getAsString();
                        String lastName = userObject.get("last_name").getAsString();
                        ((VKChat) chat).title = firstName + " " + lastName;
                        ++i;
                    }
                }
            }
        }
        Collections.sort(chats);
        return chats;
    }

    @Override
    public boolean updateChats() {
        var lastMessage = (VKMessage) chats.getFirst().getLastMessage();
        var response = WebUtils.getResponse(METHOD_URI + "messages.getDialogs",
                "access_token=" + accessToken,
                "v=" + API_VERSION,
                "offset=-1",
                "count=1",
                "start_message_id=" + lastMessage.id);
        var responseObject = getJsonObjectFromResponse(response);
        var itemsArray = responseObject.get("items").getAsJsonArray();

        if (itemsArray.size() == 0) {
            return false;
        }
        var chatObject = itemsArray.get(itemsArray.size() - 1).getAsJsonObject();
        var messageObject = chatObject.get("message").getAsJsonObject();

        // Skip messages from groups.
        if (messageObject.get("user_id").getAsInt() < 0) {
            lastMessage.id = messageObject.get("id").getAsInt();
            return false;
        }
        var chatTitle = messageObject.get("title").getAsString();
        int chatId = 0;
        int userId = 0;

        if (messageObject.has("chat_id")) {
            chatId = messageObject.get("chat_id").getAsInt();
        } else {
            userId = messageObject.get("user_id").getAsInt();
        }
        VKChat chat;

        // chatID == 0 -> message from user.
        // userId == 0 -> message from conference.
        if (chatId == 0) {
            response = WebUtils.getResponse(METHOD_URI + "users.get",
                    "user_ids=" + userId,
                    "v=" + API_VERSION);
            var userObject = getJsonArrayFromResponse(response).get(0).getAsJsonObject();
            var firstName = userObject.get("first_name").getAsString();
            var lastName = userObject.get("last_name").getAsString();
            chat = new VKChat(firstName + " " + lastName, chatId);
        } else {
            chat = new VKChat(chatTitle, chatId);
        }
        VKMessage message = createMessageFromJsonObject(messageObject);
        chat.getMessages().addLast(message);

        for (var chatIt : chats) {
            if (chatIt.getTitle().equals(chat.getTitle())) {
                if (chatId != 0) {
                    if (((VKChat) chatIt).id == chat.id) {
                        chatIt.getMessages().addLast(message);
                        return true;
                    }
                } else {
                    if(((VKMessage)chatIt.getLastMessage()).userId == message.userId) {
                        chatIt.getMessages().addLast(message);
                        return true;
                    }
                }
            }
        }
        chats.addFirst(chat);
        return true;
    }

    private VKMessage createMessageFromJsonObject(JsonObject messageObject) {
        String messageText = messageObject.get("body").getAsString();

        if (messageText.isEmpty()) {
            if (messageObject.has("attachments")) {
                messageText = "[Attachment]";
            } else if (messageObject.has("fwd_messages")) {
                messageText = "[Forwarded message]";
            } else if (messageObject.has("action")) {
                messageText = "[Action]";
            }
        }
        Date messageDate = new Date(messageObject.get("date").getAsLong() * 1000);
        int messageId = messageObject.get("id").getAsInt();
        int messageUserId = messageObject.get("user_id").getAsInt();
        boolean messageOutgoing = (messageObject.get("out").getAsInt() == 1);

        return new VKMessage(messageText, messageDate, messageId, messageUserId, messageOutgoing);
    }

    private JsonObject getJsonObjectFromResponse(String response) {
        return jsonParser.parse(response).getAsJsonObject().get("response").getAsJsonObject();
    }

    private JsonArray getJsonArrayFromResponse(String response) {
        return jsonParser.parse(response).getAsJsonObject().get("response").getAsJsonArray();
    }

    private class VKLoginFacade {
        private String url;

        VKLoginFacade() {
            StringBuilder scopes = new StringBuilder();
            for (int i = 0; i < ACCESS_SCOPES.length - 1; ++i) {
                scopes.append(ACCESS_SCOPES[i]).append(",");
            }
            scopes.append(ACCESS_SCOPES[ACCESS_SCOPES.length - 1]);

            url = WebUtils.getUrl("https://oauth.vk.com/authorize",
                    "client_id=" + CLIENT_ID,
                    "display=page",
                    "redirect_uri=" + REDIRECT_URI,
                    "scope=" + scopes,
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

                        stage.close();
                    }
                }
            });
        }
    }
}