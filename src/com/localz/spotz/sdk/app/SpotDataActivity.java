package com.localz.spotz.sdk.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.localz.spotz.sdk.Spotz;
import com.localz.spotz.sdk.models.Beacon;
import com.localz.spotz.sdk.models.Geofence;
import com.localz.spotz.sdk.models.Spot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Display Spot data including Beacon details.
 */
public class SpotDataActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spot_data);

        Spot spot = (Spot) getIntent().getSerializableExtra(Spotz.EXTRA_SPOTZ);

        ListView spotDataList = findViewById(R.id.spot_data_list);
        spotDataList.setAdapter(new SpotDataAdapter(spot));
    }

    public class SpotDataAdapter extends BaseAdapter {
        private static final int TYPE_HEADING = 0;
        private static final int TYPE_SPOT_DATA = 1;
        private static final int TYPE_BEACON = 2;
        private static final int TYPE_GEOFENCE = 3;
        private static final int TYPE_PAYLOAD = 4;
        private static final int TYPE_COUNT = TYPE_PAYLOAD + 1;

        private final List<Object> dataList;
        private final Map<Integer, Integer> typeMap;

        public SpotDataAdapter(Spot spot) {
            dataList = new ArrayList<>();

            typeMap = new HashMap<>();
            int i = 0;
            typeMap.put(i++, TYPE_HEADING);
            dataList.add("Spot info");
            typeMap.put(i++, TYPE_SPOT_DATA);
            dataList.add(spot);
            typeMap.put(i++, TYPE_HEADING);
            dataList.add("Beacons");
            if (spot.beacons != null) {
                for (Beacon beacon : spot.beacons) {
                    typeMap.put(i++, TYPE_BEACON);
                    dataList.add(beacon);
                }
            }
            if (spot.enteredBeacon != null) {
                typeMap.put(i++, TYPE_BEACON);
                dataList.add(spot.enteredBeacon);
            }
            typeMap.put(i++, TYPE_HEADING);
            dataList.add("Geofence");
            if (spot.enteredGeofence != null) {
                typeMap.put(i++, TYPE_GEOFENCE);
                dataList.add(spot.enteredGeofence);
            }
            typeMap.put(i++, TYPE_HEADING);
            dataList.add("Attributes");
            if (spot.attributes != null) {
                for (Map.Entry<String, Object> entry : spot.attributes.entrySet()) {
                    typeMap.put(i++, TYPE_PAYLOAD);
                    dataList.add(entry);
                }
            }
        }

        @Override
        public int getViewTypeCount() {
            return TYPE_COUNT;
        }

        @Override
        public int getItemViewType(int position) {
            return typeMap.get(position);
        }

        @Override
        public int getCount() {
            return dataList.size();
        }

        @Override
        public Object getItem(int position) {
            return dataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            View view = convertView;
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.listview_spot_data_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.spotzDataLayout = view.findViewById(R.id.spotz_data);
                viewHolder.id = view.findViewById(R.id.spotz_id);
                viewHolder.name = view.findViewById(R.id.spotz_name);
                viewHolder.heading = view.findViewById(R.id.heading);
                viewHolder.beaconLayout = view.findViewById(R.id.beacon);
                viewHolder.uuid = view.findViewById(R.id.uuid);
                viewHolder.major = view.findViewById(R.id.major);
                viewHolder.minor = view.findViewById(R.id.minor);
                viewHolder.geofenceLayout = view.findViewById(R.id.geofence);
                viewHolder.geofenceId = view.findViewById(R.id.geofence_id);
                viewHolder.geofenceCoordinates = view.findViewById(R.id.geofence_coordinates);
                viewHolder.payloadLayout = view.findViewById(R.id.payload);
                viewHolder.key = view.findViewById(R.id.key);
                viewHolder.value = view.findViewById(R.id.value);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            viewHolder.heading.setVisibility(View.GONE);
            viewHolder.spotzDataLayout.setVisibility(View.GONE);
            viewHolder.geofenceLayout.setVisibility(View.GONE);
            viewHolder.beaconLayout.setVisibility(View.GONE);
            viewHolder.payloadLayout.setVisibility(View.GONE);

            switch (getItemViewType(position)) {
                case TYPE_HEADING:
                    String heading = (String) getItem(position);
                    viewHolder.heading.setText(heading);
                    viewHolder.heading.setVisibility(View.VISIBLE);
                    break;
                case TYPE_SPOT_DATA:
                    Spot spot = (Spot) getItem(position);
                    viewHolder.id.setText(spot.spotId);
                    viewHolder.name.setText(spot.name);
                    viewHolder.spotzDataLayout.setVisibility(View.VISIBLE);
                    break;
                case TYPE_BEACON:
                    Beacon beacon = (Beacon) getItem(position);
                    viewHolder.uuid.setText(beacon.uuid);
                    viewHolder.major.setText("" + beacon.major);
                    viewHolder.minor.setText("" + beacon.minor);
                    viewHolder.beaconLayout.setVisibility(View.VISIBLE);
                    break;
                case TYPE_GEOFENCE:
                    Geofence geofence = (Geofence) getItem(position);
                    viewHolder.geofenceId.setText(geofence.geofenceId);
                    viewHolder.geofenceCoordinates.setText(geofence.latitude + "\n" + geofence.longitude);
                    viewHolder.geofenceLayout.setVisibility(View.VISIBLE);
                    break;
                case TYPE_PAYLOAD:
                    Map.Entry<String, Object> payload = (Map.Entry<String, Object>) getItem(position);
                    viewHolder.key.setText(payload.getKey());
                    viewHolder.value.setText(payload.getValue().toString());
                    viewHolder.payloadLayout.setVisibility(View.VISIBLE);
                    break;
            }

            return view;
        }
    }

    private class ViewHolder {
        public View spotzDataLayout;
        public TextView id;
        public TextView name;
        public TextView heading;
        public View beaconLayout;
        public TextView uuid;
        public TextView major;
        public TextView minor;
        public View geofenceLayout;
        public TextView geofenceId;
        public TextView geofenceCoordinates;
        public View payloadLayout;
        public TextView key;
        public TextView value;
    }
}
