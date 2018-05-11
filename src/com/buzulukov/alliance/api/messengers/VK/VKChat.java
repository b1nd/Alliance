package com.buzulukov.alliance.api.messengers.VK;

import com.buzulukov.alliance.api.messengers.Chat;
import com.buzulukov.alliance.api.messengers.Message;
import com.buzulukov.alliance.api.web.utils.WebUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.Random;

public class VKChat implements Chat {

    String  accessToken;
    String  chatPhotoUri;
    String  title;
    int     id;
    boolean allMessagesLoaded;
    LinkedList<Message> messages;

    VKChat(String accessToken, String chatPhotoUri, String title, int id) {
        this.accessToken = accessToken;
        this.chatPhotoUri = chatPhotoUri;
        this.title = title;
        this.id = id;
        messages = new LinkedList<>();
        allMessagesLoaded = false;
    }

    VKChat(String accessToken, String title, int id) {
        this.accessToken = accessToken;
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
        return VKMessenger.LIBRARY_NAME;
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
        VKMessage firstMessage = (VKMessage) messages.getFirst();
        int peerId;

        if (id != 0) {
            peerId = 2000000000 + id;
        } else {
            peerId = firstMessage.userId;
        }

        String response = WebUtils.getResponse(VKMessenger.METHOD_URI + "messages.getHistory",
                "access_token=" + accessToken,
                "v=" + VKMessenger.API_VERSION,
                "offset=1",
                "count=100",
                "peer_id=" + peerId,
                "start_message_id=" + firstMessage.id);

        JsonObject responseObject = VKMessenger.getJsonObjectFromResponse(response);
        JsonArray itemsArray = responseObject.get("items").getAsJsonArray();

        for (int i = 0; i < itemsArray.size(); i++) {
            JsonObject messageObject = itemsArray.get(i).getAsJsonObject();
            VKMessage message = VKMessenger.createMessageFromJsonObject(messageObject);
            messages.addFirst(message);
        }
        if (itemsArray.size() == 0) {
            allMessagesLoaded = true;
        }
        System.out.println("Number of messages loaded = " + messages.size());
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
        int peerId;

        if (id != 0) {
            peerId = 2000000000 + id;
        } else {
            peerId = ((VKMessage) getLastMessage()).userId;
        }
        String encodedText = null;

        try {
            encodedText = URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        WebUtils.getResponse(VKMessenger.METHOD_URI + "messages.send",
                "access_token=" + accessToken,
                "v=" + VKMessenger.API_VERSION,
                "random_id=" + new Random().nextInt(Integer.MAX_VALUE),
                "peer_id=" + peerId,
                "message=" + encodedText);
    }

    @Override
    public int compareTo(Chat anotherChat) {
        return messages.getLast().compareTo(anotherChat.getLastMessage());
    }
}
