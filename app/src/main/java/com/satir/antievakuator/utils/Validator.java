package com.satir.antievakuator.utils;


import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.satir.antievakuator.R;
import com.satir.antievakuator.realm.models.CarNumber;

public class Validator {
    private Context mContext;

    public Validator(Context context){
        mContext = context;
    }

//    public boolean checkCarNumber(String carNumber, boolean showToast){
//        carNumber = carNumber.trim();
//        if(carNumber.length() == 0){
//            return false;
//        }
//        else if(carNumber.length() <11){
//            if(showToast) {
//                Toast.makeText(mContext, R.string.length_car_number_is_wrong, Toast.LENGTH_SHORT).show();
//            }
//            return false;
//        }else if(!carNumber.matches("[а-я]\\d{3}[а-я]{2}\\d{2,3}[a-z]{3}")){
//            if(showToast) {
//                Toast.makeText(mContext, R.string.wrong_car_number, Toast.LENGTH_SHORT).show();
//            }
//            return false;
//        }
//        return true;
//    }

    public String checkCar(String carNumber, String carRegion, String carCountry, boolean showToast){
    carNumber = checkCarNumber(carNumber, showToast);
        if(carNumber == null){
            return null;
        }
        carRegion = checkCarRegion(carRegion, showToast);
        if(carRegion == null){
            return null;
        }
        carCountry = checkCarCountry(carCountry, showToast);
        if(carCountry == null){
            return null;
        }
        return carNumber + carRegion + carCountry;
    }

    private String checkCarNumber(String carNumber, boolean showToast){
        carNumber = carNumber.toLowerCase();
        carNumber = carNumber.trim();
        char[] lat = {'a', 'b', 'e', 'k', 'm', 'h', 'o', 'p', 'c', 't', 'y', 'x'};
        char[] rus = {'а', 'в', 'е', 'к', 'м', 'н', 'о', 'р', 'с', 'т', 'у', 'х'};
        int index = 0;
        for(char c : lat){
            carNumber = carNumber.replaceAll(String.valueOf(c), String.valueOf(rus[index]));
            index++;
        }
        carNumber = carNumber.replaceAll("[^а-я0-9]", "");
        if(carNumber.length() == 6){
            return carNumber;
        }
        if(showToast) {
            Toast.makeText(mContext, R.string.car_number_is_wrong, Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    private String checkCarRegion(String carRegion, boolean showToast){
        carRegion = carRegion.trim();
        if(carRegion.length() > 0){
            return carRegion;
        }
        if(showToast) {
            Toast.makeText(mContext, R.string.car_region_is_wrong, Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    private String checkCarCountry(String carCountry, boolean showToast){
        carCountry = carCountry.trim();
        if(carCountry.length() >= 2){
            return carCountry;
        }
        if(showToast) {
            Toast.makeText(mContext, R.string.car_country_is_wrong, Toast.LENGTH_SHORT).show();
        }
        return null;
    }

//    public boolean checkPhoneNumber(String phoneNumber, boolean showToast){
//        phoneNumber = phoneNumber.trim();
//        if(phoneNumber.length() == 0){
//            return false;
//        }
//        else if(phoneNumber.length() != 10){
//            if(showToast) {
//                Toast.makeText(mContext, R.string.length_phone_number_is_wrong, Toast.LENGTH_SHORT).show();
//            }
//            return false;
//        }
//        return true;
//    }

    public String checkPhoneNumber(String phoneNumber, boolean showToast){
        phoneNumber = phoneNumber.trim();
        phoneNumber = phoneNumber.replaceAll("[^0-9]", "");
        if(phoneNumber.length() < 10){
            if(showToast) {
                Toast.makeText(mContext, R.string.error_phone_number, Toast.LENGTH_SHORT).show();
            }
            return null;
        }
        return phoneNumber.substring(phoneNumber.length()-10);
    }

    public boolean checkEmail(String email, boolean showToast){
        email = email.trim();
        if(email.length() == 0){
            return false;
        }
        else{
            boolean isValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
            if(!isValid && showToast){
                Looper.prepare();
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, R.string.wrong_email, Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return isValid;
        }
    }
}
