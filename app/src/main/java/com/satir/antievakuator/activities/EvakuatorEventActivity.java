package com.satir.antievakuator.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.satir.antievakuator.AntievakuatorApplication;
import com.satir.antievakuator.data.SavedFields;
import com.satir.antievakuator.R;
import com.satir.antievakuator.network.NetworkUtils;
import com.satir.antievakuator.utils.FileManager;
import com.satir.antievakuator.utils.PictureUtils;
import com.satir.antievakuator.utils.Validator;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import static com.satir.antievakuator.data.Constants.FieldNameConstants.*;
import static com.satir.antievakuator.utils.ToastMaker.toastLongMessage;

public class EvakuatorEventActivity extends AppCompatActivity {
    private static final int REQUEST_PHOTO = 0;

    private ArrayList<String> mCarNumbers;
    private String mPhoneNumber;
    private String mUrlStaticMap;
    private Uri mUriPhoto;
    private SavedFields mSavedFields;
    private String mMessage;
    private Validator mValidator;

    private TextView mCarNumbersTV;
    private EditText mInputCarNumberET, mInputCarRegionET, mInputCarCountryET;
    private EditText mInputPhoneET;
    private EditText mMessageET;
    private ImageView mPhotoImageView;
    private Button mSendEventButton;
    private Context mContext;
    private TextView mErrorCarNumberTV, mErrorPhoneNumberTV, mErrorNoneCarsTV;
    private ScrollView mScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mValidator = new Validator(this);
        mCarNumbers = new ArrayList<>();
        initializeView();
        Intent intent = getIntent();
        mUrlStaticMap = intent.getStringExtra(URL_STATIC_MAP);
        mUriPhoto = intent.getData();
        FileManager manager = new FileManager(mContext);
        if(mUriPhoto != null) {
            manager.copyPhoto(mUriPhoto);
        }
        mUriPhoto = manager.generateFile();
        mPhotoImageView.setVisibility(View.VISIBLE);
        PictureUtils.setPic(mUriPhoto, mPhotoImageView, this);
    }

    public void onClickSendEvakuatorEvent(View view) {
        if (!NetworkUtils.isOnline(this)) {
            Toast.makeText(this, R.string.not_have_internet, Toast.LENGTH_SHORT).show();
            return;
        }
        if(!checkInputData()){
            return;
        }
        view.setEnabled(false);
        SendEvakuatorEventAsyncTask asyncTask = new SendEvakuatorEventAsyncTask();
        asyncTask.execute();
    }

    public void onClickRemadePhoto(View view) {
        Intent intent =new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mUriPhoto);
        startActivityForResult(intent, REQUEST_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_PHOTO){
            if(resultCode == RESULT_OK){
                PictureUtils.setPic(mUriPhoto, mPhotoImageView, this);
            }
        }
    }

    public void onClickAddCar(View view){
        addCarInList(mInputCarNumberET.getText().toString(), mInputCarRegionET.getText().toString(), mInputCarCountryET.getText().toString());
    }

    private void addCarInList(String carNumber, String carRegion, String carCountry){
        //Если номер валидный и ещё не добавлен в список
        String car = mValidator.checkCar(carNumber, carRegion, carCountry, true);
        if(car != null){
            mErrorCarNumberTV.setVisibility(View.GONE);
            if(!mCarNumbers.contains(car)){
                mCarNumbers.add(car);
                mErrorNoneCarsTV.setVisibility(View.GONE);
            }
            mInputCarNumberET.getText().clear();
            mInputCarRegionET.getText().clear();
            mInputCarCountryET.setText("rus");
            String carNumbers="";
            for(String number : mCarNumbers){
                carNumbers = carNumbers + number + "\n";
            }
            mCarNumbersTV.setText(carNumbers);
        }
        else{
            mErrorCarNumberTV.setVisibility(View.VISIBLE);
        }
    }

    private void initializeView(){
        setContentView(R.layout.activity_evakuator_event);
        mScrollView = (ScrollView) findViewById(R.id.activity_evakuator_event_scroll_view);
        mInputCarRegionET = (EditText) findViewById(R.id.activity_evakuator_event_input_car_region_edit_text);
        mInputCarNumberET = (EditText) findViewById(R.id.activity_evakuator_event_input_car_number_edit_text);
        mInputCarNumberET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() == 6){
                    mInputCarRegionET.requestFocus();
                }
            }
        });

        mInputCarCountryET = (EditText) findViewById(R.id.activity_evakuator_event_input_car_country_edit_text);
        mCarNumbersTV = (TextView) findViewById(R.id.activity_evakuator_event_cars_list_text_view);
        mMessageET = (EditText) findViewById(R.id.activity_evacuator_event_input_text_edit_text);
        mPhotoImageView = (ImageView) findViewById(R.id.activity_evacuator_event_photo_image_view);
        mSendEventButton = (Button) findViewById(R.id.activity_evakuator_event_send_event);
        mSavedFields = SavedFields.getInstance(this);
        mErrorCarNumberTV = (TextView) findViewById(R.id.activity_evakuator_event_input_car_number_error_text);
        mErrorNoneCarsTV = (TextView) findViewById(R.id.activity_evakuator_event_none_cars_error_text);
        mPhoneNumber = mSavedFields.getUserPhone();
        if (mPhoneNumber.equals("")) {
            LinearLayout inputPhoneLayout = (LinearLayout) findViewById(R.id.activity_evacuator_event_input_phone_number_layout);
            mInputPhoneET = (EditText) findViewById(R.id.activity_evacuator_event_input_phone_number_edit_text);
            inputPhoneLayout.setVisibility(View.VISIBLE);
            mErrorPhoneNumberTV = (TextView) findViewById(R.id.activity_evakuator_event_input_phone_number_error_text);
        }
    }

    private boolean checkInputData() {
        mErrorCarNumberTV.setVisibility(View.GONE);
        mErrorNoneCarsTV.setVisibility(View.GONE);
        boolean dataIsOk = true;
        //обновление списка авто
        if(mCarNumbers.size() == 0){
            mErrorNoneCarsTV.setVisibility(View.VISIBLE);
            dataIsOk = false;
        }
        //телефонный номер
        String phoneNumber;
        if (mInputPhoneET != null) {
            phoneNumber = mValidator.checkPhoneNumber(mInputPhoneET.getText().toString(), false);
            if (phoneNumber != null) {
                mPhoneNumber = phoneNumber;
                mErrorPhoneNumberTV.setVisibility(View.GONE);
            }else{
                mErrorPhoneNumberTV.setVisibility(View.VISIBLE);
                dataIsOk = false;
                mScrollView.scrollTo(0, 0);
            }
        }
        mMessage = mMessageET.getText().toString();
        return dataIsOk;
    }

    private class SendEvakuatorEventAsyncTask extends AsyncTask<Void, Void, JSONObject> {
        private ProgressDialog mProgressDialog;

        @Override
        protected JSONObject doInBackground(Void... objects) {
            File file;
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                file = new File(mContext.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.separator + "evakuatorPhoto.jpg");
            }
            else {
                file = new File(mUriPhoto.toString().substring(5));
            }
            MediaType mediaType = MediaType.parse("image/jpg");
            RequestBody requestFile = RequestBody.create(mediaType, file);
            MultipartBody.Part body = MultipartBody.Part.createFormData(PHOTO, file.getName(), requestFile);
            JSONObject json = new JSONObject();
            try {
                json.put(URL_STATIC_MAP, mUrlStaticMap);
                json.put(CAR_NUMBERS, mCarNumbers);
                json.put(PHONE_NUMBER, mPhoneNumber);
                json.put(MESSAGE, mMessage);
                json.put(DATE, System.currentTimeMillis());
            }catch (JSONException e){
                return null;
            }
                RequestBody jsonPart = NetworkUtils.createPartFromString(json.toString());
            try {
                return new JSONObject(AntievakuatorApplication.getApi().sendEvent(jsonPart, body).execute().body().string());
            }catch(IOException e){
                return null;
            } catch (JSONException e) {
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setMessage(getResources().getString(R.string.send_evakuator_event));
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            super.onPostExecute(json);
            mProgressDialog.dismiss();
            mSendEventButton.setEnabled(true);
            if (json == null) {
                    toastLongMessage(getString(R.string.failed_send));
            } else {
                boolean userExist;
                int quantityUser=0;
                try {
                    userExist = json.has("data");
                if(userExist){
                    quantityUser = json.getInt("data");
                }
                } catch (JSONException e) {
                    toastLongMessage("Не удалось распарсить ответ");
                    return;
                }
                String message;
                if(userExist){
                    message = getString(R.string.accept_send_evakuator_event_info_user_exist, String.valueOf(quantityUser));
                }
                else{
                    message = getString(R.string.accept_send_evakuator_event_info_user_not_exist);
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext).setTitle(R.string.accept_send_event).setMessage(message).setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                builder.create().show();
            }
        }
    }

}
