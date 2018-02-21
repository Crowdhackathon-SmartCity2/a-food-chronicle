package com.afoodchronicle;

public class User {

    private String id;
    private String firstName;
    private String lastName;
    private String photoUrl;
    private String birthday;
    private String description;
    private String text;

    public User(String birthday, String description) {
        this.birthday = birthday;
        this.description = description;
    }

    public User(String firstName, String lastName, String photoUrl, String birthday, String description) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.photoUrl = photoUrl;
        this.birthday = birthday;
        this.description = description;
    }

    public User(String text, String lastName, String photoUrl) {
        this.lastName = lastName;
        this.photoUrl = photoUrl;
        this.text = text;
    }

    public User() {
    }

    public User(String firstName, String lastName, String photoUrl, String id) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.photoUrl = photoUrl;
        this.id = id;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getText() {
        return text;
    }
    public void setText(String text){
        this.text = text;
    }
}