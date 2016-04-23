package com.iitd.se.busapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by harshit on 21/4/16.
 */
public class NearestBusStopFragment extends Fragment implements OnMapReadyCallback{
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private GoogleMap mMap;
    private Location myLocation, nearestLocation;
    private String nearestLocationName;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private JSONObject busStop;

    public NearestBusStopFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static NearestBusStopFragment newInstance(int sectionNumber) {
        NearestBusStopFragment fragment = new NearestBusStopFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_nearest_bus_stop, container, false);
        SupportMapFragment mapFragment = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapNearestBusStop));
        mapFragment.getMapAsync(this);
        //Initialize location manager
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();
        nearestLocation = new Location("");

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    private void displayBusStop() {
        mMap.clear();
        LatLng latLng = new LatLng(nearestLocation.getLatitude(), nearestLocation.getLongitude());
        mMap.addMarker(new MarkerOptions().position(latLng).title(nearestLocationName));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
    }

    public class GetNearestBusStop extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHelper httpHelper = new HttpHelper("http://" + getResources().getString(R.string.server_ip_port));
            try {
                busStop = httpHelper.getJson("/bus_stops/closest.json?lat=" + myLocation.getLatitude() + "&lng=" + myLocation.getLongitude(), "", "");
                if(busStop != null)
                    busStop = busStop.getJSONObject("bus_stop");
                else
                    busStop = null;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return (Void)null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(busStop == null) {
                Toast.makeText(getActivity(), "Connection problem...", Toast.LENGTH_LONG).show();
            } else {
                try {
                    nearestLocation.setLatitude(busStop.getDouble("latitude"));
                    nearestLocation.setLongitude(busStop.getDouble("longitude"));
                    nearestLocationName = busStop.getString("name");
                    displayBusStop();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                myLocation = location;
                GetNearestBusStop getNearestBusStop = new GetNearestBusStop();
                getNearestBusStop.execute((Void)null);
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            String statusString = "";
            switch (i) {
                case android.location.LocationProvider.AVAILABLE:
                    statusString = "available";
                case android.location.LocationProvider.OUT_OF_SERVICE:
                    statusString = "out of service";
                case android.location.LocationProvider.TEMPORARILY_UNAVAILABLE:
                    statusString = "temporarily unavailable";
            }
            Toast.makeText(getContext(),
                    s + " " + statusString,
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String s) {
            Toast.makeText(getContext(),
                    "Provider: " + s + " enabled",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderDisabled(String s) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }
}

