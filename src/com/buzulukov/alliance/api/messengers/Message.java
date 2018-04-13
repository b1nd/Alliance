package com.buzulukov.alliance.api.messengers;

import java.util.Date;

public interface Message extends Comparable<Message> {

    String getText();

    Date getDate();

    boolean isOutgoing();

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
        public int compareTo(Message o) {
            return 0;
        }
    };

}
