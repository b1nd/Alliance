package com.buzulukov.alliance.api;

import com.buzulukov.alliance.api.messengers.Chat;
import com.buzulukov.alliance.api.messengers.Messenger;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

/**
 * Shell or adapter over all implemented messengers.
 */
public class MessengersAdapter {

    private static MessengersAdapter instance;

    static {
        instance = new MessengersAdapter();
    }

    public static MessengersAdapter getInstance() {
        return instance;
    }

    // Here all messengers are located.
    private HashMap<String, Messenger> messengers;

    private MessengersAdapter() {
        // Prevent class instancing. Only Singleton.
        // Can't support similar accounts in messengers right now.
        messengers = new HashMap<>();
    }

    /**
     * Give all chats of super Chat interface from all authorised messengers filtered by regex.
     *
     * @param regex Filter for library name or chat title or chat last message.
     * @return LinkedList<Chat> of all filtered chats in authorised messengers.
     */
    public LinkedList<Chat> getChats(String regex) {
        var chats = new LinkedList<Chat>();

        if (regex.isEmpty()) {
            for (var messenger : messengers.values()) {
                if (messenger.isAuthorized()) {
                    chats.addAll(messenger.getChats());
                }
            }
        } else {
            for (var messenger : messengers.values()) {
                if (messenger.isAuthorized()) {
                    for (var chat : messenger.getChats()) {
                        if (chat.getLibraryName().matches(".*" + regex + ".*") ||
                                chat.getTitle().matches(".*" + regex + ".*") ||
                                chat.getLastMessage().getText().matches(".*" + regex + ".*"))
                            chats.addLast(chat);
                    }
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

    /**
     * Give names of all available messengers.
     *
     * @return Messenger names.
     */
    public String[] getMessengerNames() {
        return MessengersFactory.MESSENGER_NAMES;
    }

    /**
     * Gives all account names of authorised messengers.
     *
     * @return Set of account names.
     */
    public Set<String> getAccountNames() {
        return messengers.keySet();
    }

    /**
     * Authorize in new messenger.
     *
     * @param messengerName Type of messenger.
     * @param params        Necessary params for authorization in
     * @return Is authorised.
     */
    public boolean authorize(String messengerName, String... params) {
        var messengersFactory = new MessengersFactory();
        var messenger = messengersFactory.getMessenger(messengerName);

        if (messenger.login(params)) {
            if (messenger.isAuthorized()) {
                String accountName = messenger.getName() + " " + messenger.getAccountInfo();

                if (!messengers.containsKey(accountName)) {
                    messengers.put(accountName, messenger);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Update chats for all authorised messengers.
     *
     * @return Are there new chats.
     */
    public boolean updateChats() {
        boolean updated = false;

        for (var messenger : messengers.values()) {
            if (messenger.updateChats()) {
                updated = true;
            }
        }
        return updated;
    }

    /**
     * Serialize HashMap<String, Messenger> object to path.
     *
     * @param path Serialization path.
     * @return Is object serialized properly.
     * @throws IOException if path is not available.
     */
    public boolean saveAccounts(String path) throws IOException {
        var nFile = new File(path);
        var isNewFile = nFile.createNewFile();

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(nFile))) {
            oos.writeObject(messengers);
        }
        return isNewFile;
    }

    /**
     * Deserialize HashMap<String, Messenger> object from path.
     *
     * @param path Object path.
     * @return Is object deserialized properly.
     * @throws IOException            if path is wrong.
     * @throws ClassNotFoundException if class was not loaded.
     */
    public boolean loadAccounts(String path) throws IOException, ClassNotFoundException {
        boolean loaded = false;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
            Object ob = ois.readObject();

            if (ob instanceof HashMap) {
                messengers = (HashMap<String, Messenger>) ob;
                loaded = true;
            }
        }
        return loaded;
    }

    /**
     * Logout from authorised messenger with requested accountName.
     *
     * @param accountName Authorised messenger account name.
     * @return Did it logout.
     */
    public boolean logout(String accountName) {
        if (messengers.containsKey(accountName)) {
            messengers.remove(accountName).logout();
            return true;
        }
        return false;
    }

}
