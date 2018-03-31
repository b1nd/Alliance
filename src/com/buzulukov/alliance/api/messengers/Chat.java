package com.buzulukov.alliance.api.messengers;

import java.util.LinkedList;

public interface Chat extends Comparable<Chat> {

    String getLibraryName();

    String getTitle();

    LinkedList<Message> getMessages();

    Message getLastMessage();

    boolean areAllMessagesLoaded();

    boolean update();

    void loadMessages();

    void unloadMessages();

    void sendMessage(String text);

}
