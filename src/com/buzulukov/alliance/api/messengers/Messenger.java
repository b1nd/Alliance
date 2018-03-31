package com.buzulukov.alliance.api.messengers;

import java.util.LinkedList;

public interface Messenger {

    String getName();

    void login();

    void logout();

    boolean isAuthorized();

    String getAccountInfo();

    LinkedList<Chat> getChats();

}
