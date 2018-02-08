package com.satir.antievakuator.realm.models;


import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class UserCar extends RealmObject{
    @PrimaryKey
    private String carNumber;
    private String brand;
    private String model;

    public UserCar(String carNumber, String brand, String model){
        this.carNumber = carNumber;
        this.brand = brand;
        this.model = model;
    }

    public UserCar(){}

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
