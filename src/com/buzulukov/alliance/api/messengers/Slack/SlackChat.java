package com.buzulukov.alliance.api.messengers.Slack;

import com.buzulukov.alliance.api.messengers.Chat;
import com.buzulukov.alliance.api.messengers.Message;
import com.buzulukov.alliance.api.web.utils.WebUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;

public class SlackChat implements Chat {

    String id;
    String chatPhotoUri;
    String token;
    String title;
    String userId;
    LinkedList<Message> messages;
    boolean allMessagesLoaded;

    SlackChat(String token, String userId, String chatPhotoUri, String title, String id) {
        this.token = token;
        this.userId = userId;
        this.chatPhotoUri = chatPhotoUri;
        this.title = title;
        this.id = id;
        messages = new LinkedList<>();
        allMessagesLoaded = false;
    }

    @Override
    public String getChatPhotoUri() {
        return chatPhotoUri;
    }

    @Override
    public String getLibraryName() {
        return SlackMessenger.LIBRARY_NAME;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public LinkedList<Message> getMessages() {
        return messages;
    }

    @Override
    public Message getLastMessage() {
        return messages.getLast();
    }

    @Override
    public Message getFirstMessage() {
        return messages.getFirst();
    }

    @Override
    public boolean areAllMessagesLoaded() {
        return allMessagesLoaded;
    }

    @Override
    public void loadMessages() {
        SlackMessage firstMessage = (SlackMessage) messages.getFirst();

        String response = WebUtils.getResponse(SlackMessenger.METHOD_URI + "channels.history",
                "token=" + token, "channel=" + id, "count=100", "latest=" + firstMessage.timestamp);
        JsonObject responseObject = new JsonParser().parse(response).getAsJsonObject();
        JsonArray messagesArray = responseObject.get("messages").getAsJsonArray();

        for (int i = 0; i < messagesArray.size(); i++) {
            JsonObject messageObject = messagesArray.get(i).getAsJsonObject();
            SlackMessage message = SlackMessenger.createMessageFromJsonObject(messageObject, userId);
            messages.addFirst(message);
        }
        allMessagesLoaded = !responseObject.get("has_more").getAsBoolean();
    }

    @Override
    public void unloadMessages() {
        Message lastMessage = messages.getLast();
        messages.clear();
        messages.addLast(lastMessage);
        allMessagesLoaded = false;
    }

    @Override
    public void sendMessage(String text) {
        String encodedText = null;

        try {
            encodedText = URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        WebUtils.getResponse(SlackMessenger.METHOD_URI + "chat.postMessage",
                "token=" + token,
                "channel=" + id,
                "text=" + encodedText,
                "as_user=true");
    }

    @Override
    public int compareTo(Chat anotherChat) {
        return messages.getLast().compareTo(anotherChat.getLastMessage());
    }
}
