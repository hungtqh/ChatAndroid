package com.chatandroid.chat.model;

public class User {

    private String username;
    private String uid;
    private String email;
    private String firstname;
    private String lastname;
    private String name;
    private String gender;
    public String image;
    private String device_token;
    private String location;
    private String phonenumber;

    private UserState userState;

    public User(String username, String email, String uid, String firstname, String lastname, String gender, String image, String device_token, String location, String phonenumber, UserState userState) {
        this.username = username;
        this.uid = uid;
        this.firstname = firstname;
        this.lastname = lastname;
        this.gender = gender;
        this.image = image;
        this.device_token = device_token;
        this.location = location;
        this.phonenumber = phonenumber;
        this.userState = userState;
        this.email = email;
    }

    public User() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getName() {
        return this.firstname + " " + this.lastname;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDevice_token() {
        return device_token;
    }

    public void setDevice_token(String device_token) {
        this.device_token = device_token;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public UserState getUserState() {
        return userState;
    }

    public void setUserState(UserState userState) {
        this.userState = userState;
    }
}
