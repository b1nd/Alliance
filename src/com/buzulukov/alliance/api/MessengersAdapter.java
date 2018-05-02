package com.buzulukov.alliance.api;

import com.buzulukov.alliance.api.messengers.Chat;
import com.buzulukov.alliance.api.messengers.Messenger;

import java.io.*;
import java.util.Arrays;
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

    private HashMap<String, Messenger> messengers;

    private MessengersAdapter() {
        messengers = new HashMap<>();
    }

    public LinkedList<Chat> getChats(String... names) {
        var chats = new LinkedList<Chat>();

        if (names.length == 0) {
            for (var messenger : messengers.values()) {
                if (messenger.isAuthorized()) {
                    chats.addAll(messenger.getChats());
                }
            }
        } else {
            var namesList = new LinkedList<>(Arrays.asList(names));

            for (var messenger : messengers.values()) {
                if (messenger.isAuthorized() && namesList.contains(messenger.getName())) {
                    chats.addAll(messenger.getChats());
                }
            }
        }
        if (chats.isEmpty()) {
            chats.add(Chat.EMPTY);
        } else {
            Collections.sort(chats);
        }
        return chats;
    }

    public String[] getMessengerNames() {
        return MessengersFactory.MESSENGER_NAMES;
    }

    public boolean authorize(String messengerName, String... params) {
        var messengersFactory = new MessengersFactory();
        var messenger = messengersFactory.getMessenger(messengerName);

        if (messenger.login(params)) {
            if (messenger.isAuthorized()) {
                System.out.println("Here");
                String accountName = messenger.getName() + " " + messenger.getAccountInfo();

                if (!messengers.containsKey(accountName)) {
                    messengers.put(accountName, messenger);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean updateChats() {
        boolean updated = false;

        for (var messenger : messengers.values()) {
            if (messenger.updateChats()) {
                updated = true;
            }
        }
        return updated;
    }

    public boolean saveAccounts(String path) throws IOException {
        var nFile = new File(path);
        var isNewFile = nFile.createNewFile();

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(nFile))) {
            oos.writeObject(messengers);
        }
        return isNewFile;
    }

    public boolean loadAccounts(String path) throws IOException, ClassNotFoundException {
        boolean loaded = false;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
            Object ob = ois.readObject();

            if(ob instanceof HashMap) {
                messengers = (HashMap<String, Messenger>) ob;
                loaded = true;
            }
        }
        return loaded;
    }

    public boolean logout(String accountName) {
        if (messengers.containsKey(accountName)) {
            messengers.remove(accountName).logout();
            return true;
        }
        return false;
    }

}
