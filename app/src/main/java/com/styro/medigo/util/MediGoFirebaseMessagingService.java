package com.styro.medigo.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import com.styro.medigo.R;
import com.styro.medigo.activity.MainActivity;
import com.styro.medigo.activity.NotificationsActivity;

public class MediGoFirebaseMessagingService extends FirebaseMessagingService {

    private static final int NOTIFICATION_ID_BASE = 1000;

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        // Save the refreshed token locally
        SessionManager sessionManager = new SessionManager(this);
        sessionManager.setFcmToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Create notification channels for Android O and above
        createNotificationChannels();

        String title = "MediGo";
        String body = "";
        String channelId = Constants.CHANNEL_GENERAL;

        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
        }

        if (remoteMessage.getData().size() > 0) {
            if (remoteMessage.getData().containsKey("title")) {
                title = remoteMessage.getData().get("title");
            }
            if (remoteMessage.getData().containsKey("body")) {
                body = remoteMessage.getData().get("body");
            }
            if (remoteMessage.getData().containsKey("channel")) {
                channelId = remoteMessage.getData().get("channel");
            }
        }

        showNotification(title, body, channelId);
    }

    private void showNotification(String title, String body, String channelId) {
        Intent intent = new Intent(this, NotificationsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationId = (int) System.currentTimeMillis();
        manager.notify(notificationId, builder.build());
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationChannel ordersChannel = new NotificationChannel(
                    com.styro.medigo.util.Constants.CHANNEL_ORDERS,
                    "Order Updates",
                    NotificationManager.IMPORTANCE_HIGH
            );
            ordersChannel.setDescription("Notifications about your medicine orders");
            manager.createNotificationChannel(ordersChannel);

            NotificationChannel promotionsChannel = new NotificationChannel(
                    com.styro.medigo.util.Constants.CHANNEL_PROMOTIONS,
                    "Promotions",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            promotionsChannel.setDescription("Promotional offers and discounts");
            manager.createNotificationChannel(promotionsChannel);

            NotificationChannel generalChannel = new NotificationChannel(
                    com.styro.medigo.util.Constants.CHANNEL_GENERAL,
                    "General",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            generalChannel.setDescription("General app notifications");
            manager.createNotificationChannel(generalChannel);
        }
    }

}