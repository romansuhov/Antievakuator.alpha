package com.satir.antievakuator;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.multidex.MultiDexApplication;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import com.satir.antievakuator.network.NetworkUtils;
import com.satir.antievakuator.network.ServerApi;
import com.satir.antievakuator.network.YandexMapApi;
import com.satir.antievakuator.realm.Migration;
import com.satir.antievakuator.utils.ToastMaker;
import com.vk.sdk.VKSdk;
import com.yandex.metrica.YandexMetrica;
import java.util.concurrent.TimeUnit;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//При рефакторинге я стараюсь располагать методы в следующем порядке:
//    -методы жизненного цикла
//        -колбэки кликов
//        -колбэки
//        -кастом методы
//        -внутренние классы

public class AntievakuatorApplication extends MultiDexApplication {
    final static String[] PERMISSIONS = new String[] {
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_NETWORK_STATE
    };
    public static final String YANDEX_METRICA_API_KEY = "11178acb-96ef-4aee-87f3-d10e1e58e87e";

    private static ServerApi serverApi;
    private static YandexMapApi yandexMapApi;

    @Override
    public void onCreate() {
        super.onCreate();
        createRealm();
        createRetrofit();
        createYandexMetrica();
        initializeSocialNetworks();
        ToastMaker.sApplicationContext = getApplicationContext();
    }

    public static boolean checkPermissions(Activity activity){
        boolean granted = true;
        for (String permission:PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(activity, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                granted = false;
                break;
            }
        }
        if (!granted) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS, 0);
        }
        return granted;
    }

    public static ServerApi getApi() {
        return serverApi;
    }

    public static YandexMapApi getYandexMapApi(){
        return yandexMapApi;
    }


    private void createRealm(){
        Realm.init(this);
        RealmConfiguration realmConfig = new RealmConfiguration.Builder().schemaVersion(0).migration(new Migration()).build();
        Realm.setDefaultConfiguration(realmConfig);
    }

    private void createRetrofit(){
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).writeTimeout(60, TimeUnit.SECONDS). readTimeout(60, TimeUnit.SECONDS).connectTimeout(10, TimeUnit.SECONDS).build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(NetworkUtils.SERVER_URL) //Базовая часть адреса
                .client(client)
                .addConverterFactory(GsonConverterFactory.create()) //Конвертер, необходимый для преобразования JSON'а в объекты
                .build();
        serverApi = retrofit.create(ServerApi.class);
        Retrofit retrofitYandex = new Retrofit.Builder()
                .baseUrl(NetworkUtils.SERVER_YANDEX_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        yandexMapApi = retrofitYandex.create(YandexMapApi.class);
    }

    private void createYandexMetrica(){
        // Инициализация AppMetrica SDK
        YandexMetrica.activate(getApplicationContext(), YANDEX_METRICA_API_KEY);
        // Отслеживание активности пользователей
        YandexMetrica.enableActivityAutoTracking(this);
    }

    private void initializeSocialNetworks(){
        VKSdk.initialize(this);
    }
}

