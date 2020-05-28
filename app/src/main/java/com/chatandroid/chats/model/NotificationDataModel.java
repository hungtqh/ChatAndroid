package com.chatandroid.chats.model;


public class NotificationDataModel {

    private String user;
    private int icon;
    private String title;
    private String body;
    private String sented;

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    private String device;

    public NotificationDataModel() {
    }

    public NotificationDataModel(String user, int icon, String title, String body, String sented, String device) {
        this.user = user;
        this.icon = icon;
        this.device = device;
        this.title = title;
        this.body = body;
        this.sented = sented;
    }


    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }


    public void setBody(String body) {
        this.body = body;
    }

    public String getSented() {
        return sented;
    }

    public void setSented(String sented) {
        this.sented = sented;
    }
}
