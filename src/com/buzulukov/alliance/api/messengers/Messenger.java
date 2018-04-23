package com.buzulukov.alliance.api.messengers;

import java.util.LinkedList;

public interface Messenger {

    String getName();

    boolean login(String... params);

    void logout();

    boolean isAuthorized();

    String getAccountInfo();

    LinkedList<Chat> getChats();

    boolean updateChats();

}
