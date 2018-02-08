package com.satir.antievakuator.camera;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import com.satir.antievakuator.activities.EvakuatorEventActivity;
import com.satir.antievakuator.utils.FileManager;
import com.satir.antievakuator.R;
import com.satir.antievakuator.utils.Validator;
import java.util.ArrayList;
import static com.satir.antievakuator.data.Constants.FieldNameConstants.CAR_NUMBERS;
import static com.satir.antievakuator.data.Constants.FieldNameConstants.LATITUDE;
import static com.satir.antievakuator.data.Constants.FieldNameConstants.LONGITUDE;
import static com.satir.antievakuator.data.Constants.FieldNameConstants.URL_STATIC_MAP;
import static com.satir.antievakuator.utils.ToastMaker.toastLongMessage;

public class CameraActivity extends AppCompatActivity {

    private byte[] pictureData;
    ImageButton capture;
    private Button mFinishButton, mRecaptureButton;
    private LinearLayout mButtons;
    private EditText mCarInputET;
    CameraPreview preview;
    Camera mCamera;
    FrameLayout mFrame;
    Context mContext;
    private Uri mUri;
    private boolean shouldAddCallback;

    private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            showConfirm();
            pictureData = data;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        if(savedInstanceState != null && savedInstanceState.containsKey("pictureData")) {
            pictureData = savedInstanceState.getByteArray("pictureData");
        }
        else{
            shouldAddCallback = true;
        }
        mCamera = openCamera();
        if (mCamera == null) {
            toastLongMessage("Opening camera failed");
            return;
        }
        initializeView();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putByteArray("pictureData", pictureData);
    }

    @Override
    protected void onPause() {
        if (mCamera != null) {
            if (pictureData == null) {
                mFrame.removeView(preview);
                preview.cleanPreview();
                preview = null;
            }
            mCamera.stopPreview();
            mCamera.lock();
            mCamera.release();
            mCamera = null;
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (mCamera == null) {
            mCamera = openCamera();
            if (pictureData == null) {
                preview = new CameraPreview(this, mCamera, shouldAddCallback);
                mFrame.addView(preview, 0);
            }
        }
        super.onResume();
    }


    private Camera openCamera() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
            return null;

        Camera cam = null;
        if (Camera.getNumberOfCameras() > 0) {
            try {
                cam = Camera.open(0);
            } catch (Exception exc) {
            }
        }
        return cam;
    }

    private void showConfirm() {
        capture.setVisibility(View.INVISIBLE);
        mButtons.setVisibility(View.VISIBLE);
    }

    private void hideConfirm() {
        mButtons.setVisibility(View.INVISIBLE);
        capture.setVisibility(View.VISIBLE);
    }

    private void initializeView(){
        setContentView(R.layout.activity_camera);
        mButtons = (LinearLayout) findViewById(R.id.camera_activity_input_car_number_layout);
        mCarInputET = (EditText) findViewById(R.id.camera_activity_input_car_number_edit_text);
        mFinishButton = (Button) findViewById(R.id.camera_activity_finish);
        mFinishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent oldIntent = getIntent();
                Intent intent = new Intent(mContext, EvakuatorEventActivity.class);
                intent.putExtra(LATITUDE, oldIntent.getDoubleExtra(LATITUDE, -1000));
                intent.putExtra(LONGITUDE, oldIntent.getDoubleExtra(LONGITUDE, -1000));
                intent.putExtra(URL_STATIC_MAP, oldIntent.getStringExtra(URL_STATIC_MAP));
                FileManager fileManager = new FileManager(mContext);
                mUri = fileManager.generateFile();
                try {
                    fileManager.savePhotoInFile(pictureData, mUri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Validator validator = new Validator(mContext);
                String[] inputCarNumbers = mCarInputET.getText().toString().split(" ");
                ArrayList<String> validCarNumbers = new ArrayList<>();
//                for(String carNumber : inputCarNumbers){
//                    if(validator.checkCarNumber(carNumber, true)){
//                        validCarNumbers.add(carNumber);
//                    }
//                }
                if(!validCarNumbers.isEmpty()){
                    intent.putExtra(CAR_NUMBERS, validCarNumbers);
                }
                intent.setData(mUri);
                mCamera.release();
                mCamera = null;
                startActivity(intent);
            }
        });
        mRecaptureButton = (Button) findViewById(R.id.camera_activity_recapture);
        mRecaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideConfirm();
                mCamera.startPreview();
                pictureData = null;
            }
        });
        preview = new CameraPreview(this, mCamera, shouldAddCallback);
        mFrame = (FrameLayout) findViewById(R.id.camera_activity_layout);
        mFrame.addView(preview, 0);


        capture = (ImageButton) findViewById(R.id.camera_activity_capture);
        capture.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCamera.takePicture(null, null, null, mPictureCallback);
                    }
                }
        );
    }
}
