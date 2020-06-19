package com.chatandroid.chat.model;

public class UserState {
    private String date;
    private String status;
    private String time;

    public UserState(String date, String status, String time) {
        this.date = date;
        this.status = status;
        this.time = time;
    }

    public UserState() {
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
