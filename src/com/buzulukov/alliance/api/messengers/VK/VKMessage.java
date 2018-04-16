package com.buzulukov.alliance.api.messengers.VK;

import com.buzulukov.alliance.api.messengers.Message;

import java.util.Date;
import java.util.LinkedList;

public class VKMessage implements Message {

    int     id;
    int     userId;
    Date    date;
    String  text;
    boolean outgoing;
    LinkedList<String> imagesUrl;

    VKMessage(String text, Date date, int id, int userId, boolean outgoing) {
        this.text = text;
        this.date = date;
        this.id = id;
        this.userId = userId;
        this.outgoing = outgoing;
        imagesUrl = new LinkedList<>();
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public Date getDate() {
        return date;
    }

    @Override
    public boolean isOutgoing() {
        return outgoing;
    }

    @Override
    public LinkedList<String> getImagesUrl() {
        return imagesUrl;
    }

    @Override
    public int compareTo(Message anotherMessage) {
        return -date.compareTo(anotherMessage.getDate());
    }

}
