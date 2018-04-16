package com.buzulukov.alliance.api.messengers;

import java.util.LinkedList;

public interface Chat extends Comparable<Chat> {

    String getChatPhotoUri();

    String getLibraryName();

    String getTitle();

    LinkedList<Message> getMessages();

    Message getLastMessage();

    Message getFirstMessage();

    boolean areAllMessagesLoaded();

    boolean update();

    void loadMessages();

    void unloadMessages();

    void sendMessage(String text);

    Chat EMPTY = new Chat() {

        @Override
        public String getChatPhotoUri() {
            return null;
        }

        @Override
        public String getLibraryName() {
            return null;
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public LinkedList<Message> getMessages() {
            return null;
        }

        @Override
        public Message getFirstMessage() {
            return null;
        }

        @Override
        public Message getLastMessage() {
            return null;
        }

        @Override
        public boolean areAllMessagesLoaded() {
            return false;
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
        public int compareTo(Chat o) {
            return 0;
        }

    };
}
