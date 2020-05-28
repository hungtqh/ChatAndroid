package com.chatandroid.chats.model;

public class User {


    public String username;
    public String name;
    public String uid;
    public String firstname;
    public String lastname;
    public String device_token;
    public String status;


    public User(){

    }

    public User(String username, String uid, String firstname, String lastname, String status, String device_token) {
        this.username = username;
        this.uid = uid;
        this.firstname = firstname;
        this.lastname = lastname;
        this.device_token = device_token;
        this.status = status;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDevice_token() {
        return device_token;
    }

    public void setDevice_token(String device_token) {
        this.device_token = device_token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return this.firstname + " " + this.lastname;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

}
