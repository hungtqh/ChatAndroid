package com.chatandroid.chat.model;

public class Contacts {
    public String firstname, lastname, status, image, username;

    public Contacts()
    {

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Contacts(String firstname, String lastname, String username, String status, String image) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.status = status;
        this.image = image;
        this.username = username;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
