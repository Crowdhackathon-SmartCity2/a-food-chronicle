package com.afoodchronicle.firebase;

public class FirebaseUser {

    private String id;
    private String firstName;
    private String lastName;
    private String photoUrl;
    private String birthday;
    private String description;
    private String text;
    private String age;
    private String thumbPhotoUrl;
    private String deviceToken;


    public FirebaseUser(String firstName, String lastName, String photoUrl, String thumbPhotoUrl, String birthday, String description, String age, String deviceToken) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.photoUrl = photoUrl;
        this.birthday = birthday;
        this.description = description;
        this.age = age;
        this.thumbPhotoUrl = thumbPhotoUrl;
        this.deviceToken = deviceToken;
    }

    public FirebaseUser() {
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getThumbPhotoUrl() {
        return thumbPhotoUrl;
    }

    public void setThumbPhotoUrl(String thumbPhotoUrl) {
        this.thumbPhotoUrl = thumbPhotoUrl;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
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