package com.example.collegeelectionsystem;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String CHANNEL_ID = "votify_channel";

    @Override public void onMessageReceived(RemoteMessage message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "Votify Updates",
                    NotificationManager.IMPORTANCE_DEFAULT);
            getSystemService(NotificationManager.class).createNotificationChannel(ch);
        }
        String title = message.getNotification()!=null? message.getNotification().getTitle() : "Votify";
        String body  = message.getNotification()!=null? message.getNotification().getBody()  : "New update";

        NotificationCompat.Builder nb = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title).setContentText(body).setAutoCancel(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        NotificationManagerCompat.from(this).notify((int)System.currentTimeMillis(), nb.build());
    }
}
