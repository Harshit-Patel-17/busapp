package com.iitd.se.busapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private JSONArray buses;
    Timer timer;
    MyTimerTask timerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        try {
            buses = new JSONObject(getIntent().getExtras().getString("buses")).getJSONArray("buses");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        startTimerTask();
    }

    public void startTimerTask() {
        timer = new Timer();
        timerTask = new MyTimerTask();
        timer.schedule(timerTask, 1000, 1000);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/

        updateLocationsOnMap();
    }

    private void updateLocationsOnMap() {
        mMap.clear();
        ArrayList<Marker> markers = new ArrayList<Marker>();

        try {
            for (int i = 0; i < buses.length(); i++) {
                double lat = buses.getJSONObject(i).getDouble("latitude");
                double lng = buses.getJSONObject(i).getDouble("longitude");
                Log.e("LatLng", "" + lat + " " + lng);
                String title = buses.getJSONObject(i).getString("bus_num") + ", " + buses.getJSONObject(i).getString("registration_num");
                LatLng latLng = new LatLng(lat, lng);
                markers.add(mMap.addMarker(new MarkerOptions().position(latLng).title(title)));
            }

            if(!markers.isEmpty()) {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (Marker marker : markers) {
                    builder.include(marker.getPosition());
                }

                LatLngBounds bounds = builder.build();
                int padding = 100;
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                mMap.animateCamera(cu);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class UpdateBusLocation extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHelper httpHelper = new HttpHelper("http://" + getResources().getString(R.string.server_ip_port));
            try {
                for (int i = 0; i < buses.length(); i++) {
                    String bus_id = buses.getJSONObject(i).getString("id");
                    JSONObject bus = httpHelper.getJson("/buses/" + bus_id + "/latlng.json", "", "");
                    if(bus == null) {
                        Toast.makeText(getBaseContext(), "Connection problem...", Toast.LENGTH_LONG).show();
                        continue;
                    }
                    bus = bus.getJSONObject("bus");
                    buses.getJSONObject(i).put("latitude", bus.getString("latitude"));
                    buses.getJSONObject(i).put("longitude", bus.getString("longitude"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return (Void)null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            updateLocationsOnMap();
        }
    }

    public class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    UpdateBusLocation updateBusLocation = new UpdateBusLocation();
                    updateBusLocation.execute((Void)null);
                }
            });
        }
    }
}
