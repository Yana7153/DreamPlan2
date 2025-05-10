package com.example.dreamplan.settings;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.dreamplan.R;



    public class NotificationHelper {
        private static final String CHANNEL_ID = "dreamplan_channel";
        private static final String CHANNEL_NAME = "DreamPlan Notifications";
        private static int notificationId = 0; // Use auto-incrementing ID

        public static void createNotificationChannel(Context context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_DEFAULT
                );
                NotificationManager manager = context.getSystemService(NotificationManager.class);
                manager.createNotificationChannel(channel);
            }
        }

        public static void showNotification(Context context, String title, String message) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManagerCompat manager = NotificationManagerCompat.from(context);

            // Use unique ID for each notification
            manager.notify(++notificationId, builder.build());
        }

        public static void cancelAllNotifications(Context context) {
            NotificationManagerCompat manager = NotificationManagerCompat.from(context);
            manager.cancelAll();
        }
    }

