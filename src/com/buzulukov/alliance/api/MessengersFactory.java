package com.buzulukov.alliance.api;

import com.buzulukov.alliance.api.messengers.Messenger;
import com.buzulukov.alliance.api.messengers.Slack.SlackMessenger;
import com.buzulukov.alliance.api.messengers.VK.VKMessenger;

/**
 * Messenger instances giver.
 */
class MessengersFactory {

    // All messenger names should be added here.
    static final String[] MESSENGER_NAMES = {
            VKMessenger.LIBRARY_NAME,
            SlackMessenger.LIBRARY_NAME
    };

    /**
     * Creates instance of messengerType messenger.
     * Only here you should add your messenger.
     *
     * @param messengerType Messenger name.
     * @return Super interface instance of messengerType messenger.
     * @throws NullPointerException     if messengerType is null.
     * @throws IllegalArgumentException if messengerType is not supported.
     */
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
