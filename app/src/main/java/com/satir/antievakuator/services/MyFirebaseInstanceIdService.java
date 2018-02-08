package com.satir.antievakuator.services;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.satir.antievakuator.AntievakuatorApplication;
import com.satir.antievakuator.data.SavedFields;

import java.io.IOException;

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        final String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        SavedFields savedFields = SavedFields.getInstance(getApplicationContext());
        String userPhoneNumber = savedFields.getUserPhone();
        savedFields.setFCMTokenExist(true);
        if (!userPhoneNumber.equals("")) {
            try {
                AntievakuatorApplication.getApi().updateFCMToken(userPhoneNumber, refreshedToken).execute();
            } catch (IOException e) {
                //ignore
            }
        }
    }
}
