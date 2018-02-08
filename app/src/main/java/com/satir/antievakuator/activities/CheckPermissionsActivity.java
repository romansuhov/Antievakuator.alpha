package com.satir.antievakuator.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.satir.antievakuator.AntievakuatorApplication;
import com.satir.antievakuator.realm.Database;
import com.satir.antievakuator.utils.ToastMaker;

import java.util.HashMap;
import java.util.Map;

import static com.satir.antievakuator.data.Constants.FieldNameConstants.CAR_NUMBER;
import static com.satir.antievakuator.data.Constants.FieldNameConstants.DATE;
import static com.satir.antievakuator.data.Constants.FieldNameConstants.MESSAGE;
import static com.satir.antievakuator.data.Constants.FieldNameConstants.PHONE_NUMBER;
import static com.satir.antievakuator.data.Constants.FieldNameConstants.PHOTO_URL;
import static com.satir.antievakuator.data.Constants.FieldNameConstants.RECEIVED_EVENT_ID;
import static com.satir.antievakuator.data.Constants.FieldNameConstants.URL_STATIC_MAP;

public class CheckPermissionsActivity extends AppCompatActivity {
    private Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent oldIntent = getIntent();
        mIntent = new Intent(this, MainActivity.class);

        if(oldIntent.hasExtra(RECEIVED_EVENT_ID)){
            mIntent = new Intent(this, ReceivedEventActivity.class);
            mIntent.putExtra(RECEIVED_EVENT_ID, oldIntent.getIntExtra(RECEIVED_EVENT_ID, 0));
        }
        else {
            Bundle data = oldIntent.getExtras();
            if (data != null && data.containsKey(CAR_NUMBER)) {
                mIntent = new Intent(this, ReceivedEventActivity.class);
                Map<String, String> dataMap = new HashMap<>();
                dataMap.put(CAR_NUMBER, data.getString(CAR_NUMBER));
                dataMap.put(MESSAGE, data.getString(MESSAGE));
                dataMap.put(PHONE_NUMBER, data.getString(PHONE_NUMBER));
                dataMap.put(PHOTO_URL, data.getString(PHOTO_URL));
                dataMap.put(URL_STATIC_MAP, data.getString(URL_STATIC_MAP));
                dataMap.put(DATE, data.getString(DATE));
                Database database = new Database();
                int id = database.createNewReceivedEvent(dataMap);
                database.closeRealm();
                mIntent.putExtra(RECEIVED_EVENT_ID, id);
            }
            if (getIntent().getBooleanExtra("finish", false)) {
                finish();
            } else {
                boolean granted = AntievakuatorApplication.checkPermissions(this);
                if (granted) {
                    startActivity(mIntent);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for(int result : grantResults){
            if(result == PackageManager.PERMISSION_DENIED){
                ToastMaker.toastLongMessage("Приложение не сможет корректно работать без разрешений");
                finish();
            }
        }
        startActivity(mIntent);
    }
}
