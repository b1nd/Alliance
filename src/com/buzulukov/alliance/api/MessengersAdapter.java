package com.buzulukov.alliance.api;

import com.buzulukov.alliance.api.messengers.Chat;
import com.buzulukov.alliance.api.messengers.Messenger;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

public class MessengersAdapter {

    private static MessengersAdapter instance;

    static {
        instance = new MessengersAdapter();
    }

    public MessengersAdapter getInstance() {
        return instance;
    }

    private HashMap<String, Messenger> messengers;

    private MessengersAdapter() {
        messengers = new HashMap<>();
    }

    public LinkedList<Chat> getChats() {
        var chats = new LinkedList<Chat>();

        for (var messenger : messengers.values()) {
            if (messenger.isAuthorized()) {
                chats.addAll(messenger.getChats());
            }
        }
        Collections.sort(chats);
        return chats;
    }

    public String[] getMessengerNames() {
        return MessengersFactory.MESSENGER_NAMES;
    }

    public boolean authorize(String messengerName) {
        var messengersFactory = new MessengersFactory();
        var messenger = messengersFactory.getMessenger(messengerName);

        messenger.login();

        if (messenger.isAuthorized()) {
            String accountName = messengerName + " " + messenger.getAccountInfo();

            if (!messengers.containsKey(accountName)) {
                messengers.put(accountName, messenger);
                return true;
            }
        }
        return false;
    }

    public boolean logout(String accountName) {
        if (messengers.containsKey(accountName)) {
            messengers.remove(accountName).logout();
            return true;
        }
        return false;
    }

}
