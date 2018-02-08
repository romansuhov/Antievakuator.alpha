package com.satir.antievakuator.services;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.satir.antievakuator.Notifications;
import com.satir.antievakuator.realm.Database;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService{


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        int id = 0;
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ

        if (remoteMessage.getData().size() > 0) {
            final Map<String, String> dataMap = remoteMessage.getData();
            Database database = new Database();
            id = database.createNewReceivedEvent(dataMap);
            database.closeRealm();
        }

            Notifications.createNotificationReceivedEvent(getApplicationContext(), id, "Новое событие!", "Нажмите для подробностей");
    }

}
