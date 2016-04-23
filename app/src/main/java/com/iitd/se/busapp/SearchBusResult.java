package com.iitd.se.busapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SearchBusResult extends AppCompatActivity {

    private ListView searchBusResultListView;
    private JSONArray buses;
    private Button dispOnMapButton;

    private enum SeatAvail {Empty, Stand, Full}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_bus_result);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        searchBusResultListView = (ListView) findViewById(R.id.listViewSearchBusResult);
        dispOnMapButton = (Button) findViewById(R.id.buttonDispOnMap);
        dispOnMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayResultOnMap();
            }
        });

        try {
            buses = new JSONObject(getIntent().getExtras().getString("buses")).getJSONArray("buses");
            populateResult();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void displayResultOnMap() {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("buses", getIntent().getExtras().getString("buses"));
        startActivity(intent);
    }

    private void populateResult() {
        searchBusResultListView.setAdapter(new MyBaseAdapter(buses, this));
    }

    public class MyBaseAdapter extends BaseAdapter {

        JSONArray mBuses;
        Context mContext;

        public MyBaseAdapter(JSONArray buses, Context context) {
            mBuses = buses;
            mContext = context;
        }

        @Override
        public int getCount() {
            return mBuses.length();
        }

        @Override
        public Object getItem(int i) {
            JSONObject item = null;
            try {
                item = mBuses.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return item;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = inflater.inflate(R.layout.listview_search_buses, viewGroup, false);
            TextView busNumTextView = (TextView) row.findViewById(R.id.textViewBusNum);
            TextView regNumTextView = (TextView) row.findViewById(R.id.textViewRegNum);
            ImageView seatAvailImageView = (ImageView) row.findViewById(R.id.imageViewSeatAvail);

            try {
                JSONObject item = mBuses.getJSONObject(i);
                busNumTextView.setText(item.getString("bus_num"));
                regNumTextView.setText(item.getString("registration_num"));
                switch(SeatAvail.values()[item.getInt("seat_avail")]) {
                    case Empty:
                        seatAvailImageView.setImageResource(R.drawable.empty_normal);
                        break;
                    case Stand:
                        seatAvailImageView.setImageResource(R.drawable.stand_normal);
                        break;
                    case Full:
                        seatAvailImageView.setImageResource(R.drawable.full_normal);
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return row;
        }
    }
}
