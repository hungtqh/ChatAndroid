package com.chatandroid.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;

import com.chatandroid.R;
import com.chatandroid.chat.activity.ChatActivity;
import com.chatandroid.chat.activity.ProfileViewActivity;
import com.chatandroid.utils.AppPreference;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.concurrent.Executor;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String sent = remoteMessage.getData().get("sent"); // receiver
        String user = remoteMessage.getData().get("user"); // sender
        String intent = remoteMessage.getData().get("intent");

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        AppPreference preference = new AppPreference(MyFirebaseMessaging.this);
        if (preference.getCurrentChattingUser() == null || !preference.getCurrentChattingUser().equals(user)) { // sender
            if (firebaseUser != null) {
                if (firebaseUser.getUid() != null && sent.equals(firebaseUser.getUid())) { // receiver
                    sendNotification(remoteMessage, intent);
                }
            }
        }
    }

    private void sendNotification(RemoteMessage remoteMessage, String intentName) {
        String channelId = getString(R.string.default_notification_channel_id);
        String user = remoteMessage.getData().get("user"); // sender
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String device = remoteMessage.getData().get("device"); // receiver

        int j = Integer.parseInt(user.replaceAll("[\\D]", "")); // replace sender ID  to all digits
        Intent intent = new Intent();

        if (intentName.equals("ChatActivity")) {
            intent = new Intent(this, ChatActivity.class);
        } else if (intentName.equals("ProfileViewActivity")) {
            intent = new Intent(this, ProfileViewActivity.class);
        }

        Bundle bundle = new Bundle();
        bundle.putString("receiver_uid", user);
        bundle.putString("receiver_token", device);
        intent.putExtras(bundle);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSound)
                .setContentIntent(pendingIntent);
        NotificationManager noti = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int i = 0; // notification id
        if (j > 0) { // assign id with sender id
            i = j;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    getString(R.string.chat_notifications),
                    NotificationManager.IMPORTANCE_HIGH);
            noti.createNotificationChannel(channel);
        }

        noti.notify(i, builder.build());

    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        updateToken();
    }

    // update token if receiver logged in at another device
    private void updateToken() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnSuccessListener((Executor) this, instanceIdResult -> {
                        String updatedToken = instanceIdResult.getToken();
                        reference.child("device_token").setValue(updatedToken);
                    });
        }

    }
}
