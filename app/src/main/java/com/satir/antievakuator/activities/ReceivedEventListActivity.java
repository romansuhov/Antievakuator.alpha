package com.satir.antievakuator.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.satir.antievakuator.R;
import com.satir.antievakuator.realm.models.ReceivedEvent;
import com.satir.antievakuator.realm.Database;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.satir.antievakuator.data.Constants.FieldNameConstants.RECEIVED_EVENT_ID;

public class ReceivedEventListActivity extends AppCompatActivity {
    private RecyclerView mEventListRV;
    private RecyclerView.Adapter<EventHolder> mAdapter;
    private Context mContext;
    private Database mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mDatabase = new Database();
        initializeViews();
    }

    private void initializeViews(){
        setContentView(R.layout.activity_received_event_list);
        mAdapter = new EventAdapter(mDatabase.getReceivedEvents());
        mEventListRV = (RecyclerView) findViewById(R.id.activity_received_event_list_recycler_view);
        mEventListRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mEventListRV.setAdapter(mAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabase.closeRealm();
    }

    private class EventHolder extends RecyclerView.ViewHolder{
        private TextView dateTV;
        private TextView messageTV;
        private TextView carNumberTV;
        private ReceivedEvent mReceivedEvent;

        EventHolder(View itemView) {
            super(itemView);
            dateTV = (TextView) itemView.findViewById(R.id.item_received_event_date);
            messageTV = (TextView) itemView.findViewById(R.id.item_received_event_message);
            carNumberTV = (TextView) itemView.findViewById(R.id.item_received_event_car_number);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ReceivedEventActivity.class);
                    intent.putExtra(RECEIVED_EVENT_ID, mReceivedEvent.getId());
                    startActivity(intent);
                }
            });
        }

        void bindEvent(ReceivedEvent event){
            mReceivedEvent = event;
            SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
            dateTV.setText(format.format(new Date(mReceivedEvent.getDate())));
            messageTV.setText(mReceivedEvent.getMessage());
            carNumberTV.setText(mReceivedEvent.getCarNumber());
        }
    }

    private class EventAdapter extends RecyclerView.Adapter<EventHolder>{
        private List<ReceivedEvent> mEvents;

        EventAdapter(List<ReceivedEvent> events){
            mEvents = events;
        }

        @Override
        public EventHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.item_received_event, parent, false);
            return new EventHolder(view);
        }

        @Override
        public void onBindViewHolder(EventHolder holder, int position) {
            holder.bindEvent(mEvents.get(position));
        }

        @Override
        public int getItemCount() {
            return mEvents.size();
        }
    }
}
