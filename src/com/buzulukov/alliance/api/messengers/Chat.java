package com.buzulukov.alliance.api.messengers;

import java.util.LinkedList;

/**
 * All messenger chats must implement this interface.
 * All methods must be implemented properly.
 * Comparator on your own need.
 */
public interface Chat extends Comparable<Chat> {

    /**
     * Gives photo uri of chat.
     *
     * @return Chat photo uri.
     */
    String getChatPhotoUri();

    /**
     * Gives name of chat messenger.
     *
     * @return Chat messenger name.
     */
    String getLibraryName();

    /**
     * Gives title of chat.
     *
     * @return Chat title.
     */
    String getTitle();

    /**
     * Gives LinkedList of all messages in this chat.
     *
     * @return LinkedList<Message> of all chat messages.
     */
    LinkedList<Message> getMessages();

    /**
     * Gives last message of chat.
     *
     * @return Last chat message.
     */
    Message getLastMessage();

    /**
     * Gives first message of chat.
     *
     * @return first chat message.
     */
    Message getFirstMessage();

    /**
     * If all messages for this chat are loaded.
     *
     * @return All messengers for this chat are loaded.
     */
    boolean areAllMessagesLoaded();

    /**
     * Load more up messages for chat.
     */
    void loadMessages();

    /**
     * Unload all chat messages except last.
     */
    void unloadMessages();

    /**
     * Sends message to this chat.
     *
     * @param text Messages text.
     */
    void sendMessage(String text);

    /**
     * Stub for empty chat.
     */
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
