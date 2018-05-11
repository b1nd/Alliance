package com.buzulukov.alliance.api.messengers.Slack;

import com.buzulukov.alliance.api.messengers.Chat;
import com.buzulukov.alliance.api.messengers.Messenger;
import com.buzulukov.alliance.api.web.utils.WebUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Objects;

public class SlackMessenger implements Messenger, Serializable {

    public static final String LIBRARY_NAME = "Slack";
    public static final String METHOD_URI = "https://slack.com/api/";

    private String  userId;
    private String  accountInfo;
    private String  accessToken;
    private boolean isAuthorized;

    private transient LinkedList<Chat> chats;

    public SlackMessenger() {
    }

    @Override
    public String getName() {
        return LIBRARY_NAME;
    }

    @Override
    public boolean login(String... params) {
        if (!isAuthorized && params.length == 2) {
            accessToken = params[0];
            userId = params[1];
            isAuthorized = true;
        }
        return isAuthorized;
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
            var response = WebUtils.getResponse(METHOD_URI + "auth.test",
                    "token=" + accessToken);
            var responseObject = new JsonParser().parse(response).getAsJsonObject();
            accountInfo = responseObject.get("user").getAsString() + " " +
                    responseObject.get("team").getAsString();
            System.out.println(accountInfo);
        }
        return accountInfo;
    }

    @Override
    public LinkedList<Chat> getChats() {
        if (chats == null) {
            chats = new LinkedList<>();

            var response = WebUtils.getResponse(METHOD_URI + "users.info",
                    "token=" + accessToken,
                    "user=" + userId);

            var userObject = new JsonParser().parse(response).getAsJsonObject().get("user").getAsJsonObject();
            var photoUri = userObject.get("profile").getAsJsonObject().get("image_48").getAsString();

            response = WebUtils.getResponse(METHOD_URI + "channels.list",
                    "token=" + accessToken);

            var responseObject = new JsonParser().parse(response).getAsJsonObject();
            var channelsArray = responseObject.getAsJsonArray("channels");

            for (int i = 0; i < channelsArray.size(); i++) {
                var channelObject = channelsArray.get(i).getAsJsonObject();
                var chatTitle = channelObject.get("name").getAsString();
                var chatId = channelObject.get("id").getAsString();

                var chat = new SlackChat(accessToken, userId, photoUri, chatTitle, chatId);

                var channelResponse = WebUtils.getResponse(METHOD_URI + "channels.info",
                        "token=" + accessToken, "channel=" + chatId);
                var channelResponseObject = new JsonParser().parse(channelResponse).getAsJsonObject()
                        .get("channel").getAsJsonObject();

                if (!channelResponseObject.has("latest")) {
                    continue;
                }
                var latestMessageObject = channelResponseObject.get("latest").getAsJsonObject();

                SlackMessage message = createMessageFromJsonObject(latestMessageObject, userId);

                chat.messages.addLast(message);
                chats.addLast(chat);
            }
        }
        return chats;
    }

    @Override
    public boolean updateChats() {
        boolean updated = false;

        for (var chatIt : chats) {
            var chat = (SlackChat) chatIt;
            SlackMessage lastMessage = (SlackMessage) chat.getLastMessage();

            String response = WebUtils.getResponse(METHOD_URI + "channels.history",
                    "token=" + accessToken,
                    "channel=" + chat.id,
                    "count=1",
                    "oldest=" + lastMessage.timestamp);
            JsonObject responseObject = new JsonParser().parse(response).getAsJsonObject();
            JsonArray messages = responseObject.get("messages").getAsJsonArray();

            if (messages.size() == 0) {
                continue;
            }
            updated = true;
            JsonObject messageObject = messages.get(messages.size() - 1).getAsJsonObject();
            SlackMessage message = createMessageFromJsonObject(messageObject, userId);

            chat.messages.addLast(message);
        }

        return updated;
    }

    static SlackMessage createMessageFromJsonObject(JsonObject messageObject, String username) {
        String messageText = messageObject.get("text").getAsString();

        if (messageObject.has("attachments")) {
            String attachmentText;
            if(messageObject.get("attachments").isJsonObject()) {
                attachmentText = messageObject.get("attachments").getAsJsonObject().get("text").getAsString();
            } else {
                attachmentText = messageObject.get("attachments").getAsJsonArray().get(0)
                        .getAsJsonObject().get("text").getAsString();
            }
            messageText += "\n" + "Attachment: " + attachmentText;
        }

        String messageTimestamp = messageObject.get("ts").getAsString();
        String messageUsername = "System";
        boolean messageOutgoing = false;

        try {
            messageUsername = messageObject.get("user").getAsString();
            messageOutgoing = Objects.equals(messageUsername, username);
        } catch (NullPointerException e) {
            // Do nothing right now.
        }

        return new SlackMessage(messageText, messageTimestamp, messageUsername, messageOutgoing);
    }
}
