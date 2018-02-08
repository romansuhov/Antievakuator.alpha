package com.satir.antievakuator.data;

import com.google.android.gms.maps.model.LatLng;

public class Constants{
//    public static final LatLngBounds SAINT_PETERSBURG = new LatLngBounds.Builder().include(new LatLng(59.5439672403491, 31.400772428125038)).include(new LatLng(60.617881760320365, 29.060684537500038)).build();
    public static final LatLng SAINT_PETERSBURG = new LatLng(59.93496183881133, 30.33472280949354);
    public static final int DEFAULT_MAP_ZOOM = 17;
    public static final String HTTPS_MAPS_GOOGLEAPIS_COM_MAPS_API_STATICMAP = "https://maps.googleapis.com/maps/api/staticmap?";

    public class FieldNameConstants {
        public static final String ID = "id";
        public static final String CAR_NUMBERS = "carNumbers";
        public static final String CAR_NUMBER = "carNumber";
        public static final String PHONE_NUMBER = "phoneNumber";
        public static final String URL_STATIC_MAP = "urlStaticMap";
        public static final String DATE = "date";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String PHOTO_URL = "photoUrl";
        public static final String MESSAGE = "message";
        public static final String LAST_KNOWN_LATITUDE = "lastKnownLatitude";
        public static final String LAST_KNOWN_LONGITUDE = "lastKnownLongitude";
        public static final String USER_PHONE = "userPhone";
        public static final String USER_NAME = "userName";
        public static final String USER_LAST_NAME = "userLastName";
        public static final String BIRTHDAY = "birthday";
        public static final String CAR_BRANDS = "carBrands";
        public static final String CAR_MODELS = "carModels";
        public static final String PASSWORD = "password";
        public static final String NEW_PASSWORD = "newPassword";
        public static final String EMAIL = "email";
        public static final String FCM_TOKEN = "FCMToken";
        public static final String RECEIVED_EVENT_ID = "receivedEventId";
        public static final String PHOTO = "photo";
        public static final String HINT_DEFAULT_PASSWORD = "hintDefaultPassword";
        public static final String FCM_TOKEN_EXIST = "FCMTokenExist";
    }

}


