package com.satir.antievakuator.network;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;


public interface YandexMapApi {

    @GET("1.x")
    Call<ResponseBody> getTextByCoords(@Query("geocode") String geocode, @Query("kind") String house, @Query("format") String json, @Query("results") String result);

}
