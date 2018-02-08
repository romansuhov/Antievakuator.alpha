package com.satir.antievakuator.realm.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ReceivedEvent extends RealmObject{
    @PrimaryKey
    private int id;
    private String urlStaticMap;
    private String urlPhoto;
    private String phoneNumber;
    private String carNumber;
    private String message;
    private long date;
    private int donateStatus;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrlStaticMap() {
        return urlStaticMap;
    }

    public void setUrlStaticMap(String urlStaticMap) {
        this.urlStaticMap = urlStaticMap;
    }

    public String getUrlPhoto() {
        return urlPhoto;
    }

    public void setUrlPhoto(String urlPhoto) {
        this.urlPhoto = urlPhoto;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getDonateStatus() {
        return donateStatus;
    }

    public void setDonateStatus(int status) {
        if(status>EventStatus.NOT_DONATED){
            return;
        }
        this.donateStatus = status;
    }

}
