package com.satir.antievakuator.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import static com.satir.antievakuator.data.Constants.FieldNameConstants.*;
import static com.satir.antievakuator.realm.models.EventStatus.DONATED;
import static com.satir.antievakuator.realm.models.EventStatus.NOT_ANSWERED;
import static com.satir.antievakuator.realm.models.EventStatus.NOT_DONATED;

import com.satir.antievakuator.R;
import com.satir.antievakuator.realm.Database;
import com.satir.antievakuator.realm.models.ReceivedEvent;
import com.squareup.picasso.Picasso;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReceivedEventActivity extends AppCompatActivity {
    private Database mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = new Database();
        initializeViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabase.closeRealm();
    }

    private void initializeViews(){
        setContentView(R.layout.activity_received_event);
        TextView headerTV = (TextView) findViewById(R.id.activity_received_event_header);
        TextView messageTV = (TextView) findViewById(R.id.activity_received_event_message);
        TextView carNumberTV = (TextView) findViewById(R.id.activity_received_event_car_number);
        ImageView photoIV = (ImageView) findViewById(R.id.activity_received_event_photo);
        ImageView mapIV = (ImageView) findViewById(R.id.activity_received_event_map);

        int eventId = getIntent().getIntExtra(RECEIVED_EVENT_ID, 0);
        ReceivedEvent event = mDatabase.getReceivedEvent(eventId);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String date = format.format(new Date(event.getDate()));
        String header = getString(R.string.header, date);
        headerTV.setText(header);
        messageTV.setText(event.getMessage());
        carNumberTV.setText(event.getCarNumber());
        Picasso.with(this).load(event.getUrlPhoto()).into(photoIV);
        Picasso.with(this).load(event.getUrlStaticMap()).into(mapIV);
        TextView donateStatusTV = (TextView) findViewById(R.id.donate_status_text_view);
        switch (event.getDonateStatus()){

            case NOT_ANSWERED:
                LinearLayout donateLayout = (LinearLayout) findViewById(R.id.donate_layout);
//                donateLayout.setVisibility(View.VISIBLE);
                break;

            case NOT_DONATED:
                donateStatusTV.setVisibility(View.VISIBLE);
                donateStatusTV.setText(R.string.not_donated);
                break;

            case DONATED:
                donateStatusTV.setVisibility(View.VISIBLE);
                donateStatusTV.setText(R.string.donated);
                break;
        }
    }

    public void onClickDonate(View view){
        //запускаем процедуру пожертвования
    }

    public void onClickRejectDonate(View view){
        //запускаем опрос причины отказа
    }

}
