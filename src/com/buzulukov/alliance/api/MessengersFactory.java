package com.buzulukov.alliance.api;

import com.buzulukov.alliance.api.messengers.Messenger;
import com.buzulukov.alliance.api.messengers.Slack.SlackMessenger;
import com.buzulukov.alliance.api.messengers.VK.VKMessenger;

public class MessengersFactory {

    public static final String[] MESSENGER_NAMES = {
            VKMessenger.LIBRARY_NAME,
            SlackMessenger.LIBRARY_NAME
    };

    Messenger getMessenger(String messengerType) throws NullPointerException, IllegalArgumentException {
        if (messengerType == null) {
            throw new NullPointerException("Messenger type name is null.");
        } else if (messengerType.equalsIgnoreCase(MESSENGER_NAMES[0])) {
            return new VKMessenger();
        } else if (messengerType.equalsIgnoreCase(MESSENGER_NAMES[1])) {
            return new SlackMessenger();
        }
        throw new IllegalArgumentException("No supported messenger.");
    }

}
