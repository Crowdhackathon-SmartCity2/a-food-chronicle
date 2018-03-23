package com.afoodchronicle.chat.fragments;


public class Requests {
    private String userName, description, thumbPhotoUrl;

    public Requests() {
    }

    public Requests(String userName, String description, String thumbPhotoUrl) {
        this.userName = userName;
        this.description = description;
        this.thumbPhotoUrl = thumbPhotoUrl;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThumbPhotoUrl() {
        return thumbPhotoUrl;
    }

    public void setThumbPhotoUrl(String thumbPhotoUrl) {
        this.thumbPhotoUrl = thumbPhotoUrl;
    }
}
