package com.buzulukov.alliance.api.messengers;

import java.util.Date;

public interface Message extends Comparable<Message> {

    String getText();

    Date getDate();

    boolean isOutgoing();

}
