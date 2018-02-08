package com.satir.antievakuator.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.google.android.gms.maps.model.LatLng;
import static com.satir.antievakuator.data.Constants.FieldNameConstants.*;

public class SavedFields {
    private static SharedPreferences sPreferences;
    private static SharedPreferences.Editor sEditor;
    private static SavedFields sSavedFields;

    public static SavedFields getInstance(Context context) {
        if (sSavedFields == null) {
            sSavedFields = new SavedFields(context);
        }
        return sSavedFields;
    }

    private SavedFields(Context context) {
        sPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sEditor = sPreferences.edit();
    }

    public LatLng getLastKnownLocation() {
        float latitude = sPreferences.getFloat(LAST_KNOWN_LATITUDE, -1000);
        float longitude = sPreferences.getFloat(LAST_KNOWN_LONGITUDE, -1000);
        if(latitude == -1000 || longitude == -1000){
            return null;
        }
        return new LatLng(latitude, longitude);
    }

    public void setLastKnownLocation(double latitude, double longitude) {
            sEditor.putFloat(LAST_KNOWN_LATITUDE, (float) latitude);
            sEditor.putFloat(LAST_KNOWN_LONGITUDE, (float) longitude);
            sEditor.commit();
    }

    public String getUserPhone(){
        return sPreferences.getString(USER_PHONE, "");
    }

    public void setUserPhone(String phone){
        if(!phone.equals("") && phone.length() != 10){
            return;
        }
        sEditor.putString(USER_PHONE, phone).commit();
    }

    public void setName(String name){
        sEditor.putString(USER_NAME, name).commit();
    }

    public String getName(){
        return sPreferences.getString(USER_NAME, "");
    }

    public void setLastName(String lastName){
        sEditor.putString(USER_LAST_NAME, lastName).commit();
    }

    public String getLastName(){
        return sPreferences.getString(USER_LAST_NAME, "");
    }

    public long getBirthday(){
        return sPreferences.getLong(BIRTHDAY, 0);
    }

    public void setBirthday(long birthday){
        sEditor.putLong(BIRTHDAY, birthday).commit();
    }

    public String getEmail(){
        return sPreferences.getString(EMAIL, "");
    }

    public void setEmail(String email){
        sEditor.putString(EMAIL, email).commit();
    }

    public String getUserPassword(){
        return sPreferences.getString(PASSWORD, getUserPhone());
    }

    public void setUserPassword(String password){
        sEditor.putString(PASSWORD, password).commit();
    }

    public void setShouldHintDefaultPassword(boolean visible){
        sEditor.putBoolean(HINT_DEFAULT_PASSWORD, visible).commit();
    }

    public boolean isShouldHintDefaultPassword(){
        return sPreferences.getBoolean(HINT_DEFAULT_PASSWORD, true);
    }

    public boolean isFCMTokenExist(){
        return sPreferences.getBoolean(FCM_TOKEN_EXIST, false);
    }

    public void setFCMTokenExist(boolean isExist){
        sEditor.putBoolean(FCM_TOKEN_EXIST, isExist).commit();
    }
}
