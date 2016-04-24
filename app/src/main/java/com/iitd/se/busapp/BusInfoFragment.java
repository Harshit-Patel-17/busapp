package com.iitd.se.busapp;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class BusInfoFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;

    public BusInfoFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_bus_info, container, false);
        SupportMapFragment mapFragment = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.fragmentMap));
        mapFragment.getMapAsync(this);
        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    public void displayBusAndBusStopsOnMap(JSONObject bus) {
        mMap.clear();

        BitmapDescriptor bus_icon = BitmapDescriptorFactory.fromResource(R.drawable.bus);
        BitmapDescriptor bus_stop_icon = BitmapDescriptorFactory.fromResource(R.drawable.bus_stop2);

        ArrayList<Marker> markers = new ArrayList<Marker>();
        try {
            double lat = bus.getDouble("latitude");
            double lng = bus.getDouble("longitude");
            LatLng latLng = new LatLng(lat, lng);
            markers.add(mMap.addMarker(new MarkerOptions().position(latLng).title("").icon(bus_icon)));
            JSONArray busStops = bus.getJSONArray("route");
            for(int i = 0; i < busStops.length(); i++) {
                lat = busStops.getJSONObject(i).getDouble("latitude");
                lng = busStops.getJSONObject(i).getDouble("longitude");
                latLng = new LatLng(lat, lng);
                markers.add(mMap.addMarker(new MarkerOptions().position(latLng).title(busStops.getJSONObject(i).getString("name")).icon(bus_stop_icon)));
            }

            if(!markers.isEmpty()) {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (Marker marker : markers) {
                    builder.include(marker.getPosition());
                }

                LatLngBounds bounds = builder.build();
                int padding = 50;
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                mMap.animateCamera(cu);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
