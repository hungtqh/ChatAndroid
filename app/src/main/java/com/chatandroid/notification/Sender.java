package com.chatandroid.notification;

import com.chatandroid.chat.model.NotificationDataModel;
import com.google.gson.annotations.SerializedName;


public class Sender {
    @SerializedName("data")
    private NotificationDataModel data;

    @SerializedName("to")
    public String to;

    public Sender() {
    }

    public Sender(String to, NotificationDataModel data) {
        this.data = data;
        this.to = to;
    }
}
