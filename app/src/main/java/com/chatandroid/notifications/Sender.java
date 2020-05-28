package com.chatandroid.notifications;

import com.chatandroid.chats.model.NotificationDataModel;
import com.chatandroid.chats.model.NotificationModel;
import com.google.gson.annotations.SerializedName;


public class Sender {

    @SerializedName("notification")
    private NotificationModel notification;

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
