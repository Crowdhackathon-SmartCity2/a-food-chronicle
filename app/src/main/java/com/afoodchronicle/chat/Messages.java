package com.afoodchronicle.chat;

/**
 * Created by Mayke on 15.03.2018.
 */

public class Messages
{
    private String message;
    private String type;
    private Long time;
    private Boolean seen;

    public Messages() {
    }

    public Messages(String message, String type, Long time, Boolean seen) {
        this.message = message;
        this.type = type;
        this.time = time;
        this.seen = seen;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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
