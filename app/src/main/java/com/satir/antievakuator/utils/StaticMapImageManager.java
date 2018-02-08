package com.satir.antievakuator.utils;

import com.google.android.gms.maps.model.LatLng;
import com.satir.antievakuator.R;

import static com.satir.antievakuator.data.Constants.DEFAULT_MAP_ZOOM;
import static com.satir.antievakuator.data.Constants.HTTPS_MAPS_GOOGLEAPIS_COM_MAPS_API_STATICMAP;



public class StaticMapImageManager {


    public static String createNewImageStringUrl(LatLng chosenPosition, String key){
        StringBuilder builder = new StringBuilder(HTTPS_MAPS_GOOGLEAPIS_COM_MAPS_API_STATICMAP)
                .append("size=500x500&")
                .append("scale=2&")
                .append("zoom=" + DEFAULT_MAP_ZOOM + "&")
                .append("format=jpg&")
                .append("markers=" + chosenPosition.latitude +"," + chosenPosition.longitude + "&")
                .append("key=" + key);
        return builder.toString();
    }
}
