package com.satir.antievakuator;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import com.satir.antievakuator.activities.ReceivedEventActivity;

import static com.satir.antievakuator.data.Constants.FieldNameConstants.RECEIVED_EVENT_ID;

public class Notifications {
    private static final int REQUEST_CODE_NOTIFICATION = 0;

    public static void createNotificationReceivedEvent(Context context, int id, String title, String text) {
        Intent notificationIntent = new Intent(context, ReceivedEventActivity.class);
        notificationIntent.putExtra(RECEIVED_EVENT_ID, id);

        PendingIntent contentIntent = PendingIntent.getActivity(context, REQUEST_CODE_NOTIFICATION,
                notificationIntent, PendingIntent.FLAG_ONE_SHOT);

        int sound = Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentIntent(contentIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(text)
                .setDefaults(sound)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis());

        Notification notification = builder.build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify("Notification", 0, notification);
    }
}
