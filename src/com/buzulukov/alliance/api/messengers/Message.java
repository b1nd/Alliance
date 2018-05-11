package com.buzulukov.alliance.api.messengers;

import java.util.Date;
import java.util.LinkedList;

/**
 * All messenger Messages must implement this interface.
 * All methods must be implemented properly.
 * Comparator on your own need.
 */
public interface Message extends Comparable<Message> {

    /**
     * Gives text of message.
     *
     * @return Message text,
     */
    String getText();

    /**
     * Gives date of message.
     *
     * @return Message date.
     */
    Date getDate();

    /**
     * If message is outgoing.
     *
     * @return Is message outgoing.
     */
    boolean isOutgoing();

    /**
     * Gives all images attached to this message.
     *
     * @return image urls.
     */
    LinkedList<String> getImagesUrl();

    /**
     * Stub for empty Message.
     */
    Message EMPTY = new Message() {

        @Override
        public String getText() {
            return null;
        }

        @Override
        public Date getDate() {
            return null;
        }

        @Override
        public boolean isOutgoing() {
            return false;
        }

        @Override
        public LinkedList<String> getImagesUrl() {
            return null;
        }

        @Override
        public int compareTo(Message o) {
            return 0;
        }
    };

}
