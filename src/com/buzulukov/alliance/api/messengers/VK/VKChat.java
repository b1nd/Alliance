package com.buzulukov.alliance.api.messengers.VK;

import com.buzulukov.alliance.api.messengers.Chat;
import com.buzulukov.alliance.api.messengers.Message;

import java.util.LinkedList;

public class VKChat implements Chat {

    String chatPhotoUri;
    String title;
    int id;
    LinkedList<Message> messages;
    boolean allMessagesLoaded;

    VKChat(String chatPhotoUri, String title, int id) {
        this.chatPhotoUri = chatPhotoUri;
        this.title = title;
        this.id = id;
        messages = new LinkedList<>();
        allMessagesLoaded = false;
    }

    VKChat(String title, int id) {
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
    public boolean update() {
        return false;
    }

    @Override
    public void loadMessages() {

    }

    @Override
    public void unloadMessages() {

    }

    @Override
    public void sendMessage(String text) {

    }

    @Override
    public int compareTo(Chat anotherChat) {
        return messages.getLast().compareTo(anotherChat.getLastMessage());
    }
}
