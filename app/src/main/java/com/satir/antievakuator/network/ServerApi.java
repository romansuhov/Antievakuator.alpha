package com.satir.antievakuator.network;

import org.json.JSONObject;
import java.util.Map;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Query;

import static com.satir.antievakuator.data.Constants.FieldNameConstants.*;

public interface ServerApi {
    @FormUrlEncoded
    @PUT("user/new")
    Call<ResponseBody> registration(@Field(PHONE_NUMBER) String phoneNumber, @Field(FCM_TOKEN) String FCMToken, @Field(CAR_NUMBERS) String[] carNumbers);

    @FormUrlEncoded
    @PUT("user/new")
    Call<ResponseBody> registration(@Field(PHONE_NUMBER) String phoneNumber, @Field(FCM_TOKEN) String FCMToken);

    @FormUrlEncoded
    @POST("user/token")
    Call<JSONObject> updateFCMToken(@Field(PHONE_NUMBER) String phoneNumber, @Field(FCM_TOKEN) String FCMToken);

    @Multipart()
    @POST("user/event")
    Call<ResponseBody> sendEvent(@Part("fields") RequestBody fields, @Part MultipartBody.Part file);

    @GET("car/model")
    Call<ResponseBody> getCarModels(@Query("brand") String brand);

    @FormUrlEncoded
    @POST("user/update")
    Call<ResponseBody> updateAccount(@FieldMap Map<String, String> fields);

    @DELETE("user/delete")
    Call<ResponseBody> deleteAccount(@Query(PHONE_NUMBER) String phoneNumber, @Query(PASSWORD) String password);
}
