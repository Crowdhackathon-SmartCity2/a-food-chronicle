package com.afoodchronicle.chat;

public class Messages
{
    private String messages;
    private String type;
    private Long time;
    private Boolean seen;
    private String from;

    public Messages() {
    }

    public Messages(String message, String type, Long time, Boolean seen, String from) {
        this.messages = message;
        this.type = type;
        this.time = time;
        this.seen = seen;
        this.from = from;
    }

    public String getMessages() {
        return messages;
    }

    public void setMessages(String messages) {
        this.messages = messages;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Boolean getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }
}
