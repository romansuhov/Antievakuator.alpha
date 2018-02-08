package com.satir.antievakuator.dialogs;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.google.firebase.iid.FirebaseInstanceId;
import com.satir.antievakuator.activities.UserAccountActivity;
import com.satir.antievakuator.AntievakuatorApplication;
import com.satir.antievakuator.R;
import com.satir.antievakuator.data.SavedFields;
import com.satir.antievakuator.realm.Database;
import com.satir.antievakuator.utils.Validator;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.ResponseBody;
import static com.satir.antievakuator.utils.ToastMaker.toastLongMessage;

public class Registration {
    private AlertDialog mDialog;
    private Context mContext;
    private SavedFields mSavedFields;
    private Database mDatabase;

    private String mPhone;
    private String mCarNumber;

    public Registration(Context context){
        mContext = context;
        mDatabase = new Database();
        mSavedFields = SavedFields.getInstance(mContext);
    }

    public void showRegisterDialog(){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View v = inflater.inflate(R.layout.dialog_registration, new LinearLayout(mContext));
        final EditText phoneEditText = (EditText) v.findViewById(R.id.dialog_registration_phone_edit_text);
        final EditText carNumberEditText = (EditText) v.findViewById(R.id.dialog_registration_input_car_number);
        final EditText carRegionEditText = (EditText) v.findViewById(R.id.dialog_registration_input_car_region);
        carNumberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() == 6){
                    carRegionEditText.requestFocus();
                }
            }
        });
        final EditText carCountryEditText = (EditText) v.findViewById(R.id.dialog_registration_input_car_country);
        final CheckBox haveCar = (CheckBox) v.findViewById(R.id.dialog_registration_have_car_checkbox);
        final LinearLayout carNumberLayout = (LinearLayout) v.findViewById(R.id.dialog_registration_input_car_layout);
        haveCar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    carNumberLayout.setVisibility(View.VISIBLE);
                }
                else{
                    carNumberLayout.setVisibility(View.GONE);
                }
            }
        });
        dialogBuilder.setTitle(R.string.register_menu_item);
        dialogBuilder.setView(v);
        dialogBuilder.setPositiveButton(mContext.getResources().getString(R.string.OK), null);
        dialogBuilder.setNegativeButton(mContext.getResources().getString(R.string.cancel), null);
        mDialog = dialogBuilder.create();
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Validator validator = new Validator(mContext);
                String phone = validator.checkPhoneNumber(phoneEditText.getText().toString(), true);
                if(phone == null){
                    return;
                }
                if(carNumberLayout.getVisibility() == View.VISIBLE){
                    String car = validator.checkCar(carNumberEditText.getText().toString(), carRegionEditText.getText().toString(), carCountryEditText.getText().toString(), true);
                    if(car == null){
                        return;
                    }
                    mCarNumber = car;
                }
                mPhone = phone;
                    new RegisterAsyncTask().execute();
            }
        });
    }

    private class RegisterAsyncTask extends AsyncTask<Void, Void, Boolean>{
        private ProgressDialog mProgressDialog;
        private String mError;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setMessage(mContext.getResources().getString(R.string.send_register_data));
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result;
            JSONObject json;
            ResponseBody body;
            try {
                String FCMToken = FirebaseInstanceId.getInstance().getToken();
                if(mCarNumber != null){
                    body = AntievakuatorApplication.getApi().registration(mPhone, FCMToken, new String[]{mCarNumber}).execute().body();
                }
                else {
                    body = AntievakuatorApplication.getApi().registration(mPhone, FCMToken).execute().body();
                }
                String response = body.string();
                json = new JSONObject(response);
                result = json.getBoolean("result");
                if(!result){
                    mError = json.getString("error");
                }
               return result;
            } catch (IOException e) {
                return null;
            } catch (JSONException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Boolean isAccept) {
            super.onPostExecute(isAccept);
            mProgressDialog.dismiss();
            if(isAccept == null){
                toastLongMessage(mContext.getString(R.string.failed_send));
                return;
            }
            if(isAccept){
                Toast.makeText(mContext, R.string.success_register, Toast.LENGTH_SHORT).show();
                mDialog.dismiss();
                mSavedFields.setUserPhone(mPhone);
                mSavedFields.setUserPassword("7" + mPhone);
                if(mCarNumber != null) {
                   mDatabase.createNewUserCar(mCarNumber, null, null);
                    mDatabase.closeRealm();
                }
                mContext.startActivity(new Intent(mContext, UserAccountActivity.class));
            }
            else{
                toastLongMessage(mContext.getString(R.string.error) + " " + mError);
            }
        }
    }
}
