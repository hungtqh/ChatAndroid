package com.chatandroid.chat.model;

public class GroupMessage {
    private String date, time, message, name, from, type;

    public GroupMessage() {
    }

    public GroupMessage(String date, String time, String message, String name, String from, String type) {
        this.date = date;
        this.time = time;
        this.message = message;
        this.name = name;
        this.from = from;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
