package com.satir.antievakuator.activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import static com.satir.antievakuator.data.Constants.FieldNameConstants.*;
import static com.satir.antievakuator.utils.ToastMaker.toastLongMessage;
import static com.satir.antievakuator.utils.ToastMaker.toastShortMessage;
import com.satir.antievakuator.AntievakuatorApplication;
import com.satir.antievakuator.data.DataArrays;
import com.satir.antievakuator.R;
import com.satir.antievakuator.data.SavedFields;
import com.satir.antievakuator.realm.Database;
import com.satir.antievakuator.realm.models.UserCar;
import com.satir.antievakuator.utils.Validator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import okhttp3.ResponseBody;


public class UserAccountActivity extends AppCompatActivity implements View.OnClickListener {
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM y", Locale.getDefault());
    private ArrayList<String> carBrands = new ArrayList<>(new DataArrays().getCarBrandsArray());
    private ArrayList<String> carModels;
    private SavedFields mSavedFields;
    private Database mDatabase;
    private Context mContext;
    private Validator mValidator;

    private EditText mNameET, mLastNameET, mCarNumberET, mCarRegionET, mCarCountryET, mEmailET, mOldPasswordET, mNewPasswordET;
    private AppCompatSpinner mCarBrandSpinner, mCarModelsSpinner;
    private Button mSaveButton, mDeleteButton;
    private TextView mChangeBirthdayTV;
    private DatePickerDialog mChangeBirthdayDialog;
    private RecyclerView mUserCarsRV;
    private UserCarsAdapter mAdapter;
    private Button mAddCarButton;
    private ArrayAdapter mCarModelsAdapter;

    private String mName, mLastName;
    private String mCarBrand = "", mCarModel="";
    private String mEmail = "";
    private String mNewPassword ="";
    private long mBirthday = 0;
    private List<UserCar> mUserCars;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mValidator = new Validator(this);
        mSavedFields = SavedFields.getInstance(this);
        mDatabase = new Database();
        initialViews();
        initializeUserInformation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabase.closeRealm();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.activity_user_account_save_changes_button:
                    if(!mNewPasswordET.getText().toString().trim().isEmpty()){
                        mNewPassword = mNewPasswordET.getText().toString().trim();
                    }
                    ArrayList<UserCar> cars = new ArrayList<>();
                    for(UserCar car : mUserCars){
                       cars.add(new UserCar(car.getCarNumber(), car.getBrand(), car.getModel()));
                    }
                    new UpdateAccountInfoAsyncTask().execute(cars);
                break;

            case R.id.activity_user_account_delete_account_button:
                if(mOldPasswordET.getText().toString().trim().length() == 0){
                    Toast.makeText(this, R.string.need_password_for_delete_account, Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!mOldPasswordET.getText().toString().trim().equals(mSavedFields.getUserPassword())){
                    mOldPasswordET.setText("");
                    Toast.makeText(this, R.string.wrong_password, Toast.LENGTH_SHORT).show();
                    return;
                }
                new DeleteAccountAsyncTask().execute(mOldPasswordET.getText().toString().trim());
                break;

            case R.id.activity_user_account_change_birthday_text_view:
                mChangeBirthdayTV.setEnabled(false);
                mChangeBirthdayDialog.show();
                break;

            case R.id.activity_user_account_add_car_button:
                String carNumber = mValidator.checkCar(mCarNumberET.getText().toString(), mCarRegionET.getText().toString(), mCarCountryET.getText().toString(), true);
                if(carNumber != null){
                    UserCar newCar = new UserCar(carNumber, mCarBrand, mCarModel);
                    for(UserCar car : mUserCars){
                        if(car.getCarNumber().equals(mCarNumberET.getText().toString().trim())){
                            break;
                        }
                    }
                    mUserCars.add(newCar);
                    mAdapter.notifyDataSetChanged();
                    mUserCarsRV.invalidate();
                    mCarNumberET.getText().clear();
                    mCarRegionET.getText().clear();
                    mCarCountryET.setText("rus");
                    mCarBrandSpinner.setSelection(0);
                    mCarModelsSpinner.setSelection(0);
                }
                break;
        }
    }

    private void initialViews(){
        setContentView(R.layout.activity_user_account);
        mNameET = (EditText) findViewById(R.id.activity_user_account_name);
        mNameET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                mName = s.toString().trim();
            }
        });
        mLastNameET = (EditText) findViewById(R.id.activity_user_account_last_name);
        mLastNameET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                mLastName = s.toString().trim();
            }
        });
        mChangeBirthdayTV = (TextView) findViewById(R.id.activity_user_account_change_birthday_text_view);
        mChangeBirthdayTV.setOnClickListener(this);
        mCarNumberET = (EditText) findViewById(R.id.activity_user_account_input_car_number);
        mCarRegionET = (EditText) findViewById(R.id.activity_user_account_input_car_region);
        mCarCountryET = (EditText) findViewById(R.id.activity_user_account_input_car_country);
        mAddCarButton = (Button) findViewById(R.id.activity_user_account_add_car_button);
        mAddCarButton.setOnClickListener(this);
        mEmailET = (EditText) findViewById(R.id.activity_user_account_email);
        mEmailET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                mEmail = s.toString().trim();
            }
        });
        mOldPasswordET = (EditText) findViewById(R.id.activity_user_account_old_password);
        mOldPasswordET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().trim().length() == 0){
                    mNewPasswordET.setEnabled(false);
                    return;
                }
                else if(s.toString().trim().equals(mSavedFields.getUserPassword())){
                    mNewPasswordET.setEnabled(true);
                }
                else{
                    mNewPasswordET.setEnabled(false);
                }
            }
        });
        mNewPasswordET = (EditText) findViewById(R.id.activity_user_account_new_password);
        TextView hintDefaultPassword = (TextView) findViewById(R.id.activity_user_account_hint_default_password);
        if(!mSavedFields.isShouldHintDefaultPassword()){
            hintDefaultPassword.setVisibility(View.GONE);
        }
        mSaveButton = (Button) findViewById(R.id.activity_user_account_save_changes_button);
        mSaveButton.setOnClickListener(this);
        mDeleteButton = (Button) findViewById(R.id.activity_user_account_delete_account_button);
        mDeleteButton.setOnClickListener(this);
        mUserCarsRV = (RecyclerView) findViewById(R.id.activity_user_account_user_cars_recycler_view);
        carModels = new ArrayList<>();
        carModels.add(0, "Не выбрано");
        mCarBrandSpinner = (AppCompatSpinner) findViewById(R.id.activity_user_account_spinner_car_brand);
        ArrayAdapter carBrandAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, carBrands);
        carBrandAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCarBrandSpinner.setAdapter(carBrandAdapter);
        mCarBrandSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String brand = parent.getSelectedItem().toString();
                if(brand.equals("Не выбрано")){
                    mCarBrand = "";
                    return;
                }
                mCarBrand = brand;
                new GetCarModelsAsyncTask().execute(mCarBrand);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        mCarModelsSpinner = (AppCompatSpinner) findViewById(R.id.activity_user_account_spinner_car_models);
        mCarModelsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, carModels);
        mCarModelsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCarModelsSpinner.setAdapter(mCarModelsAdapter);
        mCarModelsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String model = parent.getSelectedItem().toString();
                if(model.equals("Не выбрано")){
                    mCarModel = "";
                }else{
                    mCarModel = model;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        Calendar newCalendar = Calendar.getInstance();
        mChangeBirthdayDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newCal = Calendar.getInstance();
                newCal.set(year, monthOfYear, dayOfMonth);
                mBirthday = newCal.getTimeInMillis();
                mChangeBirthdayTV.setText(dateFormat.format(newCal.getTime()));
                mChangeBirthdayTV.setEnabled(true);
            }
        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
    }

    private void initializeUserInformation(){
        mUserCarsRV.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mUserCars = mDatabase.getUserCars();
        mAdapter = new UserCarsAdapter();
        mUserCarsRV.setAdapter(mAdapter);
        long birthday;
        mName = mSavedFields.getName();
        mLastName = mSavedFields.getLastName();
        mEmail = mSavedFields.getEmail();
        mNameET.setText(mName);
        mLastNameET.setText(mLastName);
        mEmailET.setText(mEmail);
        if(mSavedFields.getBirthday() > 0){
            birthday = mSavedFields.getBirthday();
            mChangeBirthdayTV.setText(dateFormat.format(new Date(birthday)));
        }
    }

    private class UserCarHolder extends RecyclerView.ViewHolder{
        private TextView mCarTV;
        private ImageButton mDeleteCarButton;
        private UserCar mUserCar;

        public UserCarHolder(View itemView) {
            super(itemView);
            mCarTV = (TextView) itemView.findViewById(R.id.item_user_car_TV);
            mDeleteCarButton = (ImageButton) itemView.findViewById(R.id.item_user_car_delete_car);
            mDeleteCarButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mUserCars.remove(mUserCar);
                    mAdapter.notifyDataSetChanged();
                    mUserCarsRV.invalidate();
                }
            });
        }

        void bindUserCar(UserCar car){
            mUserCar = car;
            StringBuilder carBuilder = new StringBuilder();
            if(mUserCar.getBrand() != null){
                carBuilder.append(mUserCar.getBrand() + " ");
            }
            if(mUserCar.getModel() != null){
                carBuilder.append(mUserCar.getModel() + " ");
            }
            carBuilder.append(mUserCar.getCarNumber());
            mCarTV.setText(carBuilder.toString());
        }
    }

    private class UserCarsAdapter extends RecyclerView.Adapter<UserCarHolder>{

        @Override
        public UserCarHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.item_user_car, parent, false);
            return new UserCarHolder(view);
        }

        @Override
        public void onBindViewHolder(UserCarHolder holder, int position) {
            holder.bindUserCar(mUserCars.get(position));
        }


        @Override
        public int getItemCount() {
            return mUserCars.size();
        }

    }


    private class UpdateAccountInfoAsyncTask extends AsyncTask<List<UserCar>, Void, Boolean> {
        private ProgressDialog mProgressDialog;
        private boolean shouldShowError = true;

        @Override
        protected Boolean doInBackground(List<UserCar>... objects) {
            Map<String, String> fields = new HashMap<>();
            fields.put(PHONE_NUMBER, mSavedFields.getUserPhone());
            if(!mName.isEmpty()) {
                fields.put(USER_NAME, mName);
            }
            if(!mLastName.isEmpty()) {
                fields.put(USER_LAST_NAME, mLastName);
            }
            if(!mEmail.isEmpty() && mValidator.checkEmail(mEmail, true)) {
                fields.put(EMAIL, mEmail);
            }
            else{
                mEmail = "";
            }
            if(mBirthday > 0) {
                fields.put(BIRTHDAY, String.valueOf(mBirthday));
            }
            if(!mNewPassword.isEmpty()) {
                fields.put(NEW_PASSWORD, mNewPassword);
            }
            JSONArray jsonCarNumbers = new JSONArray();
            JSONArray jsonCarBrands = new JSONArray();
            JSONArray jsonCarModels = new JSONArray();
            ArrayList<UserCar> list = new ArrayList(objects[0]);
            for(UserCar car : list){
                jsonCarNumbers.put(car.getCarNumber());
                jsonCarBrands.put(car.getBrand());
                jsonCarModels.put(car.getModel());
            }
            if(!list.isEmpty()) {
                fields.put(CAR_NUMBERS, jsonCarNumbers.toString());
                fields.put(CAR_BRANDS, jsonCarModels.toString());
                fields.put(CAR_MODELS, jsonCarBrands.toString());
            }
            try {
                JSONObject json = new JSONObject(AntievakuatorApplication.getApi().updateAccount(fields).execute().body().string());
                return json.getBoolean("result");
            }catch (IOException e){
                return false;
            } catch (JSONException e) {
                return false;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mSaveButton.setEnabled(false);
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setMessage(getResources().getString(R.string.update_account_info));
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();
        }

        @Override
        protected void onPostExecute(Boolean isSuccess) {
            super.onPostExecute(isSuccess);
            mProgressDialog.dismiss();
            mSaveButton.setEnabled(true);
            if (isSuccess) {
                //сохраняем данные в настройках и закрываем активити
                mSavedFields.setName(mName);
                mSavedFields.setLastName(mLastName);
                mSavedFields.setBirthday(mBirthday);
                List<UserCar> userCars = new ArrayList<>();
                for(UserCar car : mUserCars){
                    UserCar newCar = new UserCar(car.getCarNumber(), car.getBrand(), car.getModel());
                    userCars.add(newCar);
                }
                mDatabase.updateCarsInfo(userCars);
                mSavedFields.setEmail(mEmail);
                if(!mNewPassword.isEmpty()){
                    mSavedFields.setUserPassword(mNewPassword);
                    mSavedFields.setShouldHintDefaultPassword(false);
                }
                Toast.makeText(getApplication(), R.string.account_info_was_changes, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                if(shouldShowError){
                    toastLongMessage(getString(R.string.account_info_update_error));
                }
            }
        }
    }

    private class DeleteAccountAsyncTask extends AsyncTask<String, Void, Boolean> {
        private ProgressDialog mProgressDialog;
        private boolean shouldShowError = true;

        @Override
        protected Boolean doInBackground(String... objects) {
            try {
                JSONObject json = new JSONObject(AntievakuatorApplication.getApi().deleteAccount(mSavedFields.getUserPhone(), objects[0]).execute().body().string());
                return json.getBoolean("result");
            } catch (JSONException | IOException e) {
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDeleteButton.setEnabled(false);
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setMessage(getResources().getString(R.string.update_account_info));
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();
        }

        @Override
        protected void onPostExecute(Boolean isSuccess) {
            super.onPostExecute(isSuccess);
            mProgressDialog.dismiss();
            mDeleteButton.setEnabled(true);
            if(isSuccess == null){
                toastShortMessage("Exception");
                return;
            }
            if (isSuccess) {
                mSavedFields.setUserPhone("");
                mSavedFields.setName("");
                mSavedFields.setLastName("");
                mSavedFields.setBirthday(0);
                mDatabase.deleteAllCars();
                mSavedFields.setEmail("");
                mSavedFields.setUserPassword("");
                Toast.makeText(getApplication(), R.string.account_was_deleted, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                if(shouldShowError) {
                    toastLongMessage(getString(R.string.account_delete_error));
                }
            }
        }
    }

    private class GetCarModelsAsyncTask extends AsyncTask<String, Void, List<String>> {
//        private ProgressDialog mProgressDialog;
        private ArrayList carModels = new ArrayList<>();

        @Override
        protected List<String> doInBackground(String... objects) {
            try {
                ResponseBody responseBody = AntievakuatorApplication.getApi().getCarModels(objects[0]).execute().body();
                JSONObject json = new JSONObject(responseBody.string());
                String cars = json.getString("data");
                String[] carsArray = cars.split(",");
                carModels = new ArrayList<>(Arrays.asList(carsArray));
            }catch(IOException e){
                return carModels;
            } catch (JSONException e) {
                return carModels;
            }
            return carModels;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            mProgressDialog = new ProgressDialog(mContext);
//            mProgressDialog.setMessage(getResources().getString(R.string.update_account_info));
//            mProgressDialog.setCancelable(false);
//            mProgressDialog.setCanceledOnTouchOutside(false);
//            mProgressDialog.show();
        }

        @Override
        protected void onPostExecute(List<String> models) {
            super.onPostExecute(models);
//            mProgressDialog.dismiss();
            if (models != null) {
                models.add(0, "Не выбрано");
                carModels = (ArrayList<String>) models;
                mCarModelsAdapter.clear();
                mCarModelsAdapter.addAll(carModels);
                mCarModelsAdapter.notifyDataSetChanged();
                }
            }
    }
}
