package com.satir.antievakuator.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.satir.antievakuator.AntievakuatorApplication;
import com.satir.antievakuator.data.Constants;
import com.satir.antievakuator.data.SavedFields;
import com.satir.antievakuator.R;
import com.satir.antievakuator.dialogs.Registration;
import com.satir.antievakuator.utils.FileManager;
import com.satir.antievakuator.utils.StaticMapImageManager;
import com.shehabic.droppy.DroppyClickCallbackInterface;
import com.shehabic.droppy.DroppyMenuPopup;
import com.shehabic.droppy.animations.DroppyFadeInAnimation;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.photo.VKImageParameters;
import com.vk.sdk.api.photo.VKUploadImage;
import com.vk.sdk.dialogs.VKShareDialog;
import com.vk.sdk.dialogs.VKShareDialogBuilder;
import org.json.JSONObject;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import okhttp3.ResponseBody;

import static com.satir.antievakuator.data.Constants.DEFAULT_MAP_ZOOM;
import static com.satir.antievakuator.data.Constants.FieldNameConstants.*;
import static com.satir.antievakuator.utils.ToastMaker.toastShortMessage;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnCameraIdleListener{
    private static final int REQUEST_PHOTO = 1;
    private static final int CHOOSE_PHOTO = 2;
    private final Timer[] timer = new Timer[1];
    private GoogleMap mMap;
    private SavedFields mSavedFields;
    private Button mMakePhotoButton, mChoosePhotoButton;
    private LinearLayout mSelectPhotoLayout;
    private ImageView mCenterMapMarker;
    private GoogleApiClient mGoogleApiClient;
    private Uri eventPhotoUri;
    private Context mContext;
    private TextView mCurrentAddressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        initializeView();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mSavedFields = SavedFields.getInstance(this);
        eventPhotoUri = new FileManager(this).generateFile();
        timer[0] = new Timer();
    }

    @Override
    protected void onStart() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mGpsSwitchStateReceiver);
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        timer[0].cancel();
        super.onDestroy();
    }

    @Override
    protected void onStop() {

        super.onStop();
    }

    @Override
    protected void onResume() {
        invalidateOptionsMenu();
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        if (mSavedFields != null && !mSavedFields.getUserPhone().equals("")) {
            menu.removeItem(R.id.register);
        } else {
            menu.removeItem(R.id.user_account);
            if (mSavedFields != null && !mSavedFields.isFCMTokenExist()) {
                menu.findItem(R.id.register).setEnabled(false);
                menu.findItem(R.id.register).setTitle("Регистрация временно недоступна");
                final Timer timer = new Timer();
                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        if (mSavedFields != null && mSavedFields.isFCMTokenExist()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    menu.findItem(R.id.register).setEnabled(true);
                                    menu.findItem(R.id.register).setTitle("Регистрация");
                                }
                            });
                            timer.cancel();
                        }
                    }
                };
                timer.schedule(timerTask, new Date(), 5000);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.register:
                new Registration(this).showRegisterDialog();
                return false;

            case R.id.user_account:
                startActivity(new Intent(this, UserAccountActivity.class));
                return false;

            case R.id.event_list:
                startActivity(new Intent(this, ReceivedEventListActivity.class));
                return false;

            case R.id.menu_item_share:
                showPopupMenu(findViewById(R.id.menu_item_share));
                return super.onOptionsItemSelected(item);

            case R.id.about:
                startActivity(new Intent(this, InfoActivity.class));
                return false;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, CheckPermissionsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("finish", true);
        startActivity(intent);
    }

    private void showPopupMenu(View v) {
        DroppyMenuPopup.Builder droppyBuilder = new DroppyMenuPopup.Builder(this, v);
        DroppyMenuPopup droppyMenu = droppyBuilder.fromMenu(R.menu.menu_popup_share)
                .triggerOnAnchorClick(false)
                .setOnClick(new DroppyClickCallbackInterface() {
                    @Override
                    public void call(View v, int id) {
                        switch (id) {

                            case R.id.vk_button:
                                VKSdk.login((MainActivity) mContext, "photos", "wall", "offline");
                                break;

                            default:
                        }
                    }
                })
                .setPopupAnimation(new DroppyFadeInAnimation())
                .setXOffset(5)
                .setYOffset(5)
                .build();
        droppyMenu.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.make_photo_button:
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, eventPhotoUri);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                } else {
                    List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        grantUriPermission(packageName, eventPhotoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                }
                startActivityForResult(intent, REQUEST_PHOTO);
                break;
            case R.id.choose_photo_button:
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, CHOOSE_PHOTO);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                toastShortMessage("Успешно авторизовался");
                VKShareDialogBuilder builder = new VKShareDialogBuilder();
                //Наш пропагандистский текст втыкать сюда
                builder.setText("текст");
                //Нашу провокационную картинку-сюда
                builder.setAttachmentImages(new VKUploadImage[]{
                        new VKUploadImage(BitmapFactory.decodeResource(getResources(), R.drawable.vk_logo), VKImageParameters.pngImage())
                });
                //А сюда ссылку
                builder.setAttachmentLink("Антиэвакуатор",
                        "https://vk.com/public143696799");
                builder.setShareDialogListener(new VKShareDialog.VKShareDialogListener() {
                    @Override
                    public void onVkShareComplete(int postId) {
                        Toast.makeText(getApplicationContext(), R.string.shared_ok, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onVkShareCancel() {
                    }

                    @Override
                    public void onVkShareError(VKError error) {
                        toastShortMessage(error.errorMessage);
                    }
                });
                builder.show(getSupportFragmentManager(), "VKDialog");
            }

            @Override
            public void onError(VKError error) {
                toastShortMessage(error.errorMessage);
            }
        })) {
            if (resultCode == RESULT_OK) {
                if (requestCode == REQUEST_PHOTO) {
                    Intent intent = new Intent(this, EvakuatorEventActivity.class);
                    LatLng chosenPosition = mMap.getCameraPosition().target;
                    mSavedFields.setLastKnownLocation(chosenPosition.latitude, chosenPosition.longitude);
                    String urlStaticMap = StaticMapImageManager.createNewImageStringUrl(chosenPosition, getString(R.string.google_static_map_key));
                    intent.putExtra(URL_STATIC_MAP, urlStaticMap);
                    startActivity(intent);
                } else if(requestCode == CHOOSE_PHOTO) {
                    Intent intent = new Intent(this, EvakuatorEventActivity.class);
                    intent.setData(data.getData());
                    LatLng chosenPosition = mMap.getCameraPosition().target;
                    mSavedFields.setLastKnownLocation(chosenPosition.latitude, chosenPosition.longitude);
                    String urlStaticMap = StaticMapImageManager.createNewImageStringUrl(chosenPosition, getString(R.string.google_static_map_key));
                    intent.putExtra(URL_STATIC_MAP, urlStaticMap);
                    startActivity(intent);
                }
            }
        }

    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mSelectPhotoLayout.setVisibility(View.VISIBLE);
        mCenterMapMarker.setVisibility(View.VISIBLE);
        setCurrentLocation();
        mMap.setMyLocationEnabled(true);
        mMap.setOnCameraIdleListener(this);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //noinspection MissingPermission
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (lastLocation != null) {
            //noinspection MissingPermission
            mMap.setMyLocationEnabled(true);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), DEFAULT_MAP_ZOOM));
        } else {
            moveToLastKnownLocation();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        moveToLastKnownLocation();
    }

    private void setCurrentLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        registerReceiver(mGpsSwitchStateReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            //noinspection MissingPermission
            mMap.setMyLocationEnabled(true);
            if (mGoogleApiClient == null) {
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();
                mGoogleApiClient.connect();
            }
        } else {
            Toast.makeText(this, R.string.offer_location_enabled, Toast.LENGTH_LONG).show();
            moveToLastKnownLocation();
        }
    }

    private void moveToLastKnownLocation() {
        LatLng lastKnownLocation = mSavedFields.getLastKnownLocation();
        if (lastKnownLocation != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocation, DEFAULT_MAP_ZOOM));
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Constants.SAINT_PETERSBURG, 10));
        }
    }

    private void initializeView() {
        setContentView(R.layout.activity_main);
        mMakePhotoButton = (Button) findViewById(R.id.make_photo_button);
        mMakePhotoButton.setOnClickListener(this);
        mSelectPhotoLayout = (LinearLayout) findViewById(R.id.select_photo_layout);
        mChoosePhotoButton = (Button) findViewById(R.id.choose_photo_button);
        mChoosePhotoButton.setOnClickListener(this);
        mCenterMapMarker = (ImageView) findViewById(R.id.center_map_marker);
        mCurrentAddressText = (TextView) findViewById(R.id.current_address_text);
    }

    private BroadcastReceiver mGpsSwitchStateReceiver = new BroadcastReceiver() {

        @SuppressWarnings("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        TimerTask timerTask = new TimerTask() {
                            @Override
                            public void run() {
                                final Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                                if (lastLocation != null) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Log.d("MAP", lastLocation.toString());
                                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), DEFAULT_MAP_ZOOM));
                                        }
                                    });
                                    timer[0].cancel();
                                    timer[0] = new Timer();
                                }
                            }
                        };
                        timer[0].schedule(timerTask, new Date(), 1000);

                    }
                }
            }
    };

    @Override
    public void onCameraIdle() {
        new UpdateTextLocationAsyncTask().execute(mMap.getCameraPosition().target);
    }

    private class UpdateTextLocationAsyncTask extends AsyncTask<LatLng, Void, String> {

        @Override
        protected String doInBackground(LatLng... objects) {
            JSONObject json;
            String result = "";
            try {
                ResponseBody responseBody = AntievakuatorApplication.getYandexMapApi().getTextByCoords(objects[0].longitude + "," + objects[0].latitude, "house", "json", "1").execute().body();
                json = new JSONObject(responseBody.string());
            }
            catch(Exception e){
                return result;
            }
            try {
                JSONObject resultObject = (JSONObject) json.getJSONObject("response").getJSONObject("GeoObjectCollection").getJSONArray("featureMember").get(0);
                result = resultObject.getJSONObject("GeoObject").getString("name");
            }catch(Exception e){
                return result;
            }
            return result;
        }

        @Override
        protected void onPostExecute(final String address) {
            super.onPostExecute(address);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mCurrentAddressText.setText(address);
                }
            });
        }
    }
}
