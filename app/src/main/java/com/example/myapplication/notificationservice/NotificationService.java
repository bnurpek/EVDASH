package com.example.myapplication.notificationservice;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.myapplication.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class NotificationService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        String title = message.getNotification().getTitle();
        String text = message.getNotification().getBody();
        final String ID = "HEADS_UP_NOTIFICATION";
        NotificationChannel channel = new NotificationChannel(ID, "Heads Up Notification", NotificationManager.IMPORTANCE_HIGH);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        Notification.Builder notification = new Notification.Builder(this, ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.evdas_logo)
                .setAutoCancel(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        NotificationManagerCompat.from(this).notify(1, notification.build());
    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d("TAG", "Refreshed token: " + token);
    }
}