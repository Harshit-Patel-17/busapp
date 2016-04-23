package com.iitd.se.busapp;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by harshit on 21/4/16.
 */
public class SearchBusFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private Spinner stateSpinner, srcSpinner, dstSpinner;
    private JSONObject stateDistricts;
    private JSONArray busStops;
    private JSONObject buses;
    private Button searchBusesButton;
    private ArrayList<Map<String, String>> busStopNames;
    private ArrayList<String> busStopIds;
    private int srcBusStopId, dstBusStopId;

    public SearchBusFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static SearchBusFragment newInstance(int sectionNumber) {
        SearchBusFragment fragment = new SearchBusFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search_bus, container, false);
        stateSpinner = (Spinner) rootView.findViewById(R.id.spinnerStates);
        srcSpinner = (Spinner) rootView.findViewById(R.id.spinnerSrc);
        dstSpinner = (Spinner) rootView.findViewById(R.id.spinnerDst);
        searchBusesButton = (Button) rootView.findViewById(R.id.buttonSearchBuses);
        searchBusesButton.setEnabled(false);
        searchBusesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String src = busStopIds.get((int)srcSpinner.getSelectedItemId());
                String dst = busStopIds.get((int)dstSpinner.getSelectedItemId());
                SearchBuses searchBuses = new SearchBuses(src, dst);
                searchBuses.execute((Void)null);
            }
        });

        GetStateDistricts getStateDistricts = new GetStateDistricts();
        getStateDistricts.execute((Void)null);

        return rootView;
    }

    private void setStateSpinner() {
        JSONArray statesJson = stateDistricts.names();
        ArrayList<String> states = new ArrayList<String>();
        try {
            for (int i = 0; i < statesJson.length(); i++) {
                states.add(statesJson.getString(i));
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_state, R.id.textViewState, states);
            stateSpinner.setAdapter(adapter);
            stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    searchBusesButton.setEnabled(false);
                    String state = adapterView.getItemAtPosition(i).toString();
                    GetBusStops getBusStops = new GetBusStops(state);
                    getBusStops.execute((Void)null);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setSrcDstSpinners() {
        makeBusStopsNameAndIdLists();
        //ArrayAdapter<String> adapterSrc = new ArrayAdapter<String>(getContext(), R.layout.spinner_src, R.id.textViewSrc, busStopNames);
        //ArrayAdapter<String> adapterDst = new ArrayAdapter<String>(getContext(), R.layout.spinner_dst, R.id.textViewDst, busStopNames);
        SimpleAdapter adapterSrc = new SimpleAdapter(getActivity(), busStopNames, R.layout.spinner_src,
                new String[] {"text", "subtext"}, new int[] {R.id.textViewSrc, R.id.textViewSrcSub});
        SimpleAdapter adapterDst = new SimpleAdapter(getActivity(), busStopNames, R.layout.spinner_dst,
                new String[] {"text", "subtext"}, new int[] {R.id.textViewDst, R.id.textViewDstSub});
        srcSpinner.setAdapter(adapterSrc);
        dstSpinner.setAdapter(adapterDst);
        searchBusesButton.setEnabled(true);
    }

    private void makeBusStopsNameAndIdLists() {
        busStopNames = new ArrayList<Map<String, String>>();
        busStopIds = new ArrayList<String>();
        try {
            for(int i = 0; i < busStops.length(); i++) {
                Map<String, String> item = new HashMap<String, String>(2);
                item.put("text", busStops.getJSONObject(i).getString("name"));
                item.put("subtext", busStops.getJSONObject(i).getString("district"));
                busStopNames.add(item);
                //busStopNames.add(busStops.getJSONObject(i).getString("name") + "\n" + busStops.getJSONObject(i).getString("district"));
                busStopIds.add(busStops.getJSONObject(i).getString("id"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class GetStateDistricts extends AsyncTask<Void, Void, Void> {

        private JSONObject params;

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHelper httpHelper = new HttpHelper("http://" + getResources().getString(R.string.server_ip_port));
            try {
                stateDistricts = httpHelper.getJson("/bus_stops/state_district_data.json", "", "");
                if(stateDistricts != null)
                    stateDistricts = stateDistricts.getJSONObject("data");
                else
                    stateDistricts = null;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return (Void)null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(stateDistricts == null) {
                Toast.makeText(getActivity(), "Connection problem...", Toast.LENGTH_LONG).show();
            } else {
                setStateSpinner();
            }
        }
    }

    public class GetBusStops extends AsyncTask<Void, Void, Void> {

        private String state;

        public GetBusStops(String state) {
            this.state = state;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHelper httpHelper = new HttpHelper("http://" + getResources().getString(R.string.server_ip_port));
            try {
                JSONObject response = httpHelper.getJson("/bus_stops.json?state=" + state, "", "");
                if(response != null)
                    busStops = response.getJSONArray("bus_stops");
                else
                    busStops = null;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return (Void)null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(busStops == null) {
                Toast.makeText(getActivity(), "Connection problem...", Toast.LENGTH_LONG).show();
            } else {
                setSrcDstSpinners();
            }
        }
    }

    public class SearchBuses extends AsyncTask<Void, Void, Void> {

        private String src, dst;

        public SearchBuses(String src, String dst) {
            this.src = src;
            this.dst = dst;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHelper httpHelper = new HttpHelper("http://" + getResources().getString(R.string.server_ip_port));
            buses = httpHelper.getJson("/buses.json?src=" + src + "&dst=" + dst, "", "");
            return (Void)null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(buses == null) {
                Toast.makeText(getActivity(), "Connection problem...", Toast.LENGTH_LONG).show();
            } else {
                Intent intent = new Intent(getActivity(), SearchBusResult.class);
                intent.putExtra("buses", buses.toString());
                startActivity(intent);
            }
        }
    }
}
