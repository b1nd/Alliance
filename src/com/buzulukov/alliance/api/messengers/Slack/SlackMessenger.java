package com.buzulukov.alliance.api.messengers.Slack;

import com.buzulukov.alliance.api.messengers.Chat;
import com.buzulukov.alliance.api.messengers.Messenger;

import java.util.LinkedList;

public class SlackMessenger implements Messenger {

    public static final String LIBRARY_NAME = "Slack";

    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean login(String... params) {
        return false;
    }

    @Override
    public void logout() {

    }

    @Override
    public boolean isAuthorized() {
        return false;
    }

    @Override
    public String getAccountInfo() {
        return null;
    }

    @Override
    public LinkedList<Chat> getChats() {
        return null;
    }

    @Override
    public boolean updateChats() {
        return false;
    }
}
