package com.buzulukov.alliance.api.messengers.Slack;

import com.buzulukov.alliance.api.messengers.Message;

import java.util.Date;
import java.util.LinkedList;

public class SlackMessage implements Message {

    public String timestamp;
    private String text;
    private String username;
    private Date date;
    private boolean outgoing;
    private LinkedList<String> imagesUrl;

    public SlackMessage(String text, String timestamp, String username, boolean outgoing) {
        this.text = text;
        this.timestamp = timestamp;
        this.date = new Date((long) Double.parseDouble(timestamp) * 1000);
        this.username = username;
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
