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

    public static MessengersAdapter getInstance() {
        return instance;
    }

    private LinkedList<Messenger> uncheckedMessengers;
    private HashMap<String, Messenger> messengers;

    private MessengersAdapter() {
        uncheckedMessengers = new LinkedList<>();
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

    public void authorize(String messengerName, String loginType) {
        var messengersFactory = new MessengersFactory();
        var messenger = messengersFactory.getMessenger(messengerName);

        messenger.login(loginType);
        uncheckedMessengers.add(messenger);
    }

    public boolean updateMessengers() {
        boolean updated = false;

        for (var messenger : uncheckedMessengers) {
            if (messenger.isAuthorized()) {
                String accountName = messenger.getName() + " " + messenger.getAccountInfo();

                if (!messengers.containsKey(accountName)) {
                    messengers.put(accountName, messenger);
                    updated = true;
                }
            }
        }
        uncheckedMessengers.clear();
        return updated;
    }

    public boolean logout(String accountName) {
        if (messengers.containsKey(accountName)) {
            messengers.remove(accountName).logout();
            return true;
        }
        return false;
    }

}
