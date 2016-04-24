package com.iitd.se.busapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.SupportMapFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class BusInfo extends AppCompatActivity {

    private TextView busNumTextView, regNumTextView, seatAvailTextView, nextStopTextView;
    private int busId;
    private JSONObject bus;
    private enum SeatAvail {Empty, Stand, Full}
    BusInfoFragment busInfoFragment;
    Timer timer;
    MyTimerTask myTimerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_info);
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

        busId = getIntent().getExtras().getInt("busId");

        busNumTextView = (TextView) findViewById(R.id.textViewBusNumber);
        regNumTextView = (TextView) findViewById(R.id.textViewRegNumber);
        seatAvailTextView = (TextView) findViewById(R.id.textViewSeatAvail);
        nextStopTextView = (TextView) findViewById(R.id.textViewNextStop);
        busInfoFragment = (BusInfoFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentBusOnMap);
        //busInfoFragment = new BusInfoFragment();
        //setFragment(busInfoFragment);
    }

    private void setFragment(Fragment frag) {
        FragmentManager fm = getSupportFragmentManager();
        if(fm.findFragmentById(R.id.fragmentBusOnMap) == null) {
            fm.beginTransaction().add(R.id.fragmentBusOnMap, frag).commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        timer = new Timer();
        myTimerTask = new MyTimerTask();
        timer.schedule(myTimerTask, 1000, 5000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void updateBusInfo() {
        try {
            busNumTextView.setText(bus.getString("bus_num"));
            regNumTextView.setText(bus.getString("registration_num"));
            seatAvailTextView.setText(getSeatAvailText());
            nextStopTextView.setText(findNextBusStopName());
            busInfoFragment.displayBusAndBusStopsOnMap(bus);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String getSeatAvailText() {
        String seatAvailText = "";
        try {
            switch(SeatAvail.values()[bus.getInt("seat_avail")]) {
                case Empty:
                    seatAvailText = "Seats available";
                    break;
                case Stand:
                    seatAvailText = "Space to stand only";
                    break;
                case Full:
                    seatAvailText = "No space available";
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return seatAvailText;
    }

    private String findNextBusStopName() {
        String nextBusStop = "";
        try {
            int busStopId = bus.getInt("bus_stop_id");
            JSONArray busStops = bus.getJSONArray("route");
            for(int i = 0; i < busStops.length(); i++) {
                if(busStopId == busStops.getJSONObject(i).getInt("id")) {
                    nextBusStop = busStops.getJSONObject(i).getString("name");
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return nextBusStop;
    }

    public class GetBusTask extends AsyncTask<Void, Void, Boolean> {

        private int mBusId;
        private String toast;

        public GetBusTask(int busId) {
            mBusId = busId;
            toast = null;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            HttpHelper httpHelper = new HttpHelper("http://" + getResources().getString(R.string.server_ip_port));
            bus = httpHelper.getJson("/buses/" + mBusId + ".json", "", "");
            if(bus == null) {
                toast = "Connection problem...";
                return false;
            }
            try {
                bus = bus.getJSONObject("bus");
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {

            if(success) {
                updateBusInfo();
            } else {
                Toast.makeText(getBaseContext(), "Connection problem...", Toast.LENGTH_LONG).show();
            }
        }
    }

    public class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            GetBusTask getBusTask = new GetBusTask(busId);
            getBusTask.execute((Void)null);
        }
    }
}
