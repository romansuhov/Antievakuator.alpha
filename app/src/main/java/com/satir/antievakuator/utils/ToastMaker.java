package com.satir.antievakuator.utils;


import android.content.Context;
import android.widget.Toast;

public class ToastMaker {
    public static Context sApplicationContext;

    public static void toastShortMessage(String message){
        Toast.makeText(sApplicationContext, message, Toast.LENGTH_SHORT).show();
    }

    public static void toastLongMessage(String message){
        Toast.makeText(sApplicationContext, message, Toast.LENGTH_LONG).show();
    }
}
