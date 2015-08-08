package com.localz.spotz.sdk.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.localz.spotz.sdk.Spotz;
import com.localz.spotz.sdk.app.model.SpotzMap;
import com.localz.spotz.sdk.app.widgets.CustomAnimation;
import com.localz.spotz.sdk.listeners.InitializationListenerAdapter;
//import com.localz.spotz.sdk.listeners.RangingListener;
import com.localz.spotz.sdk.models.Spot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class MainActivity extends Activity {
    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String SPOT_ENTERED_OR_EXITED = ".SPOT_ENTERED_OR_EXITED";
    private static final int REQUEST_BLUETOOTH = 100;

    private static final int REQUEST_ENABLE_BT = 13;

    private final long activeInterval = 20000;
    private final long activeDuration = 1000;

    private final long passiveInterval = 60000;
    private final long passiveDuration = 1000;

    // This BroadcastReceiver is to be notified when device either enter or exit
    // spot.
    // It is used to refresh status on the screen.
    private OnEnterOrExitBroadcastReceiver enterOrExitSpotBroadcastReceiver;


    // Tracks spot ids of the spots that device is in
    private SpotzMap inSpotMap;
    TextView nameOfSpotText;
//    TextView rangingDistanceTextView;
    TextView startStop;

    private ListView activeSpotsListView;
    private SpotListAdapter spotListAdapter;
    private List<Spot> spotList = new ArrayList<Spot>();

    private boolean scanning;

    DecimalFormat df = new DecimalFormat("0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inSpotMap = new SpotzMap(this);

//        rangingDistanceTextView = (TextView) findViewById(R.id.activity_spot_ranging_distance);
        nameOfSpotText = (TextView) findViewById(R.id.activity_range_text);
        startStop = (TextView) findViewById(R.id.start_stop);
        activeSpotsListView = (ListView) findViewById(R.id.spots_list_view);
        spotListAdapter = new SpotListAdapter();
        activeSpotsListView.setAdapter(spotListAdapter);

        final Spotz spotz = Spotz.getInstance();

        // Show either "Start Scanning" or "Stop Scanning" depending on the
        // status

        startStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (scanning || spotz.isScanningForSpotz(MainActivity.this)) {
//                    spotz.stopScanningForSpotz(MainActivity.this);
//                    nameOfSpotText.setText(getString(R.string.message_initialized));
                    stopScanning();
                    startStop.setText(getString(R.string.start_scanning));
                    inSpotMap.clear();
                } else {
                    boolean isInitialised = spotz.isInitialized(MainActivity.this);
                    if (!isInitialised) {
//                        nameOfSpotText.setText(getString(R.string.message_initializing));

                        startStop.setVisibility(View.INVISIBLE);
                        inSpotMap.clear();
                        // Initialise Spotz
                        initialiseSpotzSdk(true);
                    } else {
                        // setOutOfRange(null);
                        startActiveScanning();
//                        Spotz.getInstance().startScanningForSpotz(MainActivity.this, activeInterval, activeDuration);
                        startStop.setText(getString(R.string.stop_scanning));
                    }
                }
                adjustUI();
            }
        });
    }

    private void initialiseSpotzSdk(final boolean startScanning) {
        // Let's initialize the spotz sdk so we can start receiving callbacks
        // for any spotz we find!
        Spotz.getInstance().initialize(this,
//                "3TA3JdAAON7FO51a",          // Your application ID goes here
                "YYPNOQwDSelIip7X",          // Your application ID goes here
                "UIr7fcmS1OurDfUXFssPuOOr49f370xsOSuB8sDq",              // Your client key goes here
//                "pKrsYoGHT0cc2mF6Qal9uWMmmRelPpLgamSBiEtz",              // Your client key goes here
                new InitializationListenerAdapter() {
                    @Override
                    public void onInitialized() {

//                        if (getString(R.string.message_initializing)
//                                .equalsIgnoreCase(nameOfSpotText.getText().toString())) {
//                            Spotz.getInstance().startScanningForSpotz(
//                                    MainActivity.this, activeInterval, activeDuration);
//                        }
                        if (startScanning) {
                            startActiveScanning();
                            startStop.setVisibility(View.VISIBLE);
                            startStop.setText(getString(R.string.stop_scanning));
                            CustomAnimation.startWaveAnimation(findViewById(R.id.wave));
                        }
                        adjustUI();
                    }

                    @Override
                    public void onError(Throwable exception) {
                        Log.e(TAG, "Exception while registering device",
                                exception);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                startStop.setVisibility(View.VISIBLE);
                                createErrorDialogInitialising();
                            }
                        });
                    }
                }, startScanning);
    }

    private void startActiveScanning() {
        if (Spotz.getInstance().isInitialized(MainActivity.this)) {
            Log.d(TAG, "startActiveScanning");
            Spotz.getInstance().stopScanningForSpotz(MainActivity.this);
            rangingHandler.removeCallbacks(rangingRunnable);
            rangeIfRequired();
        }
    }

    private void startPassiveScanning() {
        if (Spotz.getInstance().isInitialized(MainActivity.this)) {
            Log.d(TAG, "startPassiveScanning");
            rangingHandler.removeCallbacks(rangingRunnable);
            scanning = false;
            Spotz.getInstance().startScanningForSpotz(MainActivity.this, Spotz.ScanMode.EAGER);
        }
    }

    private void stopScanning() {
        if (Spotz.getInstance().isInitialized(MainActivity.this)) {
            if (scanning) {
                Log.d(TAG, "stopScanning");
                rangingHandler.removeCallbacks(rangingRunnable);
                scanning = false;
                Spotz.getInstance().stopScanningForSpotz(MainActivity.this);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        rangingRunnable.run();

        adjustUI();

        if (enterOrExitSpotBroadcastReceiver == null) {
            enterOrExitSpotBroadcastReceiver = new OnEnterOrExitBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter(getPackageName() + SPOT_ENTERED_OR_EXITED);
            intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
            registerReceiver(enterOrExitSpotBroadcastReceiver, intentFilter);
        }
        boolean isScanningForSpotz = Spotz.getInstance().isScanningForSpotz(MainActivity.this);
        if (isScanningForSpotz) {
            startStop.setText(getString(R.string.stop_scanning));
            Spotz.getInstance().stopScanningForSpotz(this);
            rangeIfRequired();
        } else {
            new SpotzMap(this).clear();
            startStop.setText(getString(R.string.start_scanning));
            scanning = false;
            if (!Spotz.getInstance().isInitialized(MainActivity.this)) {
                init();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        rangingHandler.removeCallbacks(rangingRunnable);

        if (enterOrExitSpotBroadcastReceiver != null) {
            try {
                unregisterReceiver(enterOrExitSpotBroadcastReceiver);
                enterOrExitSpotBroadcastReceiver = null;
            } catch (Exception e) {
            }
        }
        if (scanning || Spotz.getInstance().isScanningForSpotz(MainActivity.this)) {
            startPassiveScanning();
        }
    }

    private SpotComparator spotComparator = new SpotComparator();

    private void adjustUI() {
        inSpotMap = new SpotzMap(this);
        spotList.clear();

        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!inSpotMap.isEmpty()) {
                    spotList.addAll(inSpotMap.values());
                    Collections.sort(spotList, spotComparator);
                    nameOfSpotText.setVisibility(View.VISIBLE);
                    nameOfSpotText.setText("Scanning");
                    setInRange(null);
//            rangingDistanceTextView.setVisibility(View.GONE);
                } else {
                    if (Spotz.getInstance().isScanningForSpotz(MainActivity.this)) {
//            rangingDistanceTextView.setVisibility(View.VISIBLE);
                        nameOfSpotText.setText("Out of range");
                        nameOfSpotText.setVisibility(View.VISIBLE);
                    } else if (Spotz.getInstance().isInitialized(MainActivity.this)) {
                        nameOfSpotText.setText("Initialised");
                        nameOfSpotText.setVisibility(View.VISIBLE);
                    } else {
                        nameOfSpotText.setText("Not initialised");
                        nameOfSpotText.setVisibility(View.VISIBLE);
                    }
                    setOutOfRange(null);
                }
                spotListAdapter.notifyDataSetChanged();
//                if ((inSpotMap != null) && !inSpotMap.isEmpty()) {
//                    Spot spot = inSpotMap.get(inSpotMap.keySet().toArray()[0]);
//                    setInRange(spot);
//                    if ((spot.enteredBeacon != null)
//                            && (spot.enteredBeacon.distance > 0)) {
////                        rangingDistanceTextView.setVisibility(View.VISIBLE);
////                        rangingDistanceTextView.setText(MainActivity.this
////                                .getString(R.string.message_ranging_distance)
////                                + " \n"
////                                + df.format(spot.enteredBeacon.distance)
////                                + " meters");
//                    } else {
//                        rangingDistanceTextView.setVisibility(View.INVISIBLE);
//                    }
//                } else {
//                    setOutOfRange(null);
//                    rangingDistanceTextView.setVisibility(View.INVISIBLE);
//                }

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // If this activity is destroyed we want to unregister receiver.
        // Note: notifications about spot enter and exit are delivered to
        // OnEnteredSpotBroadcastReceiver
        // and OnEnteredSpotBroadcastReceiver broadcast receivers.
        try {
            if (enterOrExitSpotBroadcastReceiver != null) {
                unregisterReceiver(enterOrExitSpotBroadcastReceiver);
                enterOrExitSpotBroadcastReceiver = null;
            }
        } catch (Exception e){

        }
    }

    private void setInRange(final Spot spot) {
        TransitionDrawable transition = (TransitionDrawable) findViewById(
                R.id.wave).getBackground();
        transition.resetTransition();
        transition.startTransition(400);
//        nameOfSpotText.setText(getString(R.string.message_in_range) + " "
//                + spot.name);
//
//        View spotDataButton = findViewById(R.id.activity_spot_data_text);
//        spotDataButton.setVisibility(View.VISIBLE);
//        spotDataButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this,
//                        SpotDataActivity.class);
//                intent.putExtra(Spotz.EXTRA_SPOTZ, spot);
//                startActivity(intent);
//            }
//        });
//
//        if (spot.enteredBeacon != null) {
////            TextView serialText = (TextView) findViewById(R.id.activity_serial_text);
////            serialText.setVisibility(View.VISIBLE);
////            serialText.setText(spot.enteredBeacon.beaconId);
//        }
//
//        if (inSpotMap.isEmpty()) {
//            TransitionDrawable transition = (TransitionDrawable) findViewById(
//                    R.id.wave).getBackground();
//            transition.resetTransition();
//            transition.startTransition(400);
//        }
//
//        inSpotMap.put(spot.spotId, spot);
    }

    private void setOutOfRange(final Spot spot) {
        TransitionDrawable transition = (TransitionDrawable) findViewById(
                R.id.wave).getBackground();
        transition.resetTransition();
        transition.reverseTransition(400);
//        if (spot != null) {
//            inSpotMap.remove(spot.spotId);
//        }
//
//        if (inSpotMap.isEmpty()) {
//
//            nameOfSpotText.setText(R.string.message_not_in_range);
//
//            TextView serialText = (TextView) findViewById(R.id.activity_serial_text);
//            serialText.setVisibility(View.GONE);
//
//            TransitionDrawable transition = (TransitionDrawable) findViewById(
//                    R.id.wave).getBackground();
//            transition.resetTransition();
//            transition.reverseTransition(400);
//
//            findViewById(R.id.activity_spot_data_text).setVisibility(
//                    View.INVISIBLE);
//
//        } else {
//            setInRange((Spot) inSpotMap.values().toArray()[0]);
//        }
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == REQUEST_BLUETOOTH) {
//            BluetoothAdapter bluetoothAdapter = BluetoothAdapter
//                    .getDefaultAdapter();
//            // For this example app, let's try to ensure bluetooth is switched
//            // on
//            if (bluetoothAdapter.isEnabled()) {
//                initialiseSpotzSdk(false);
//            } else {
//                showBluetoothNotEnabledDialog();
//            }
//        }
//    }

//    private void showBluetoothNotEnabledDialog() {
//        new AlertDialog.Builder(this)
//                .setTitle("Bluetooth not enabled")
//                .setMessage(R.string.message_bluetooth_not_enabled)
//                .setCancelable(false)
//                .setPositiveButton("Enable",
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(
//                                    DialogInterface dialogInterface, int i) {
//                                Intent intentOpenBluetoothSettings = new Intent();
//                                intentOpenBluetoothSettings
//                                        .setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
//                                startActivityForResult(
//                                        intentOpenBluetoothSettings,
//                                        REQUEST_BLUETOOTH);
//                                dialogInterface.dismiss();
//                            }
//                        })
//                .setNegativeButton("Close",
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(
//                                    DialogInterface dialogInterface, int i) {
//                                dialogInterface.dismiss();
//                                finish();
//                            }
//                        }).show();
//    }
//
    private void createErrorDialogInitialising() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Unable to initialize")
                .setMessage(R.string.message_initialize_error)
                .setPositiveButton("Close",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(
                                    DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                finish();
                            }
                        }).show();
    }

    // When activity is running, especially on foreground,
    // and either enter or exit spot, broadcast receivers
    // (OnEnteredSpotBroadcastReceiver
    // and OnExitedSpotBroadcastReceiver) will notify this receiver to adjust
    // UI.
    public class OnEnterOrExitBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            adjustUI();
            abortBroadcast();
        }
    }

    Handler rangingHandler = new Handler();
    Runnable rangingRunnable = new Runnable() {
        public void run() {
            rangeIfRequired();
        }
    };

    public void rangeIfRequired() {
        // check shared preferences
        if (Spotz.getInstance().isInitialized(MainActivity.this)) {
            scanning = true;
            Spotz.getInstance().quickScan(MainActivity.this, 1000);
//            if (Spotz.getInstance().isAnySpotzToRange(MainActivity.this)) {
//                clearRangingDistances();
//                Spotz.getInstance().range(MainActivity.this,
//                        new RangingListener() {
//
//                            @Override
//                            public void onRangeIterationCompleted(
//                                    HashMap<String, Double> spotIdsAndDistances) {
//                                // spotIdsAndDistances is <spotId, distance>
//                                // pair, which shows spotId and distance to the
//                                // closest beacon in the spot
//                                // Populated
//                                Set<String> spotIds = spotIdsAndDistances
//                                        .keySet();
//                                for (String spotId : spotIds) {
//                                    Spot spotOfRangingBeacon = inSpotMap
//                                            .get(spotId);
//                                    if (spotOfRangingBeacon != null) {
//                                        spotOfRangingBeacon.closestBeaconDistance = spotIdsAndDistances
//                                                .get(spotId);
//                                        // this will write to storage
//                                        inSpotMap.put(spotId,
//                                                spotOfRangingBeacon);
//                                    }
//                                }
//                                adjustUI();
//                            }
//                        });
//            } else {
//                adjustUI();
//            }
            rangingHandler.postDelayed(rangingRunnable, 5000);
        }
    }

    private void clearRangingDistances() {
        Set<String> spotIds = inSpotMap.keySet();
        for (String spotId : spotIds) {
            Spot spot = inSpotMap.get(spotId);
            spot.enteredBeacon.distance = 0;
            inSpotMap.put(spotId, spot);
        }
    }

    private void init() {
        Log.d(TAG, "init");
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Bluetooth not enabled, select 'OK' to enable Bluetooth, 'Cancel' to continue");
                builder.setCancelable(false);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.this.initialiseSpotzSdk(false);
                    }
                });
            } else {
                initialiseSpotzSdk(false);
            }
        } else {
            onBluetoothFailed();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onActivityResult " + resultCode);
        }
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    initialiseSpotzSdk(false);
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    onBluetoothFailed();
                }
        }
    }

    private void onBluetoothFailed() {
        Log.d(TAG, "onBluetoothFailed");
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Failed to initialise Bluetooth, select 'OK' to continue without Bluetooth, 'Cancel' to quit");
        builder.setCancelable(false);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.this.initialiseSpotzSdk(false);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.this.finish();
            }
        });
        builder.show();
    }

    public class SpotListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return spotList.size();
        }

        @Override
        public Object getItem(int i) {
            return spotList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.listview_spot_item, viewGroup, false);
            }
            Spot spot = (Spot) getItem(i);
            TextView spotName = (TextView) view.findViewById(R.id.spot_name);
            TextView spotId = (TextView) view.findViewById(R.id.spot_id);
            TextView trigger = (TextView) view.findViewById(R.id.trigger);
            spotName.setText("Spot name: " + spot.name);
            spotId.setText("Spot id: " + spot.spotId);
            trigger.setVisibility(View.VISIBLE);
            if (spot.enteredBeacon != null) {
                if (spot.enteredBeacon.distance > 0) {
                    trigger.setText("Beacon: " + spot.enteredBeacon.beaconId + ", distance: " + spot.enteredBeacon.distance);
                } else {
                    trigger.setText("Beacon: " + spot.enteredBeacon.beaconId);
                }
            } else if (spot.enteredGeofence != null) {
                trigger.setText("Geofence: " + spot.enteredGeofence.geofenceId);
            } else {
                trigger.setVisibility(View.GONE);
            }
            return view;
        }
    }

    private class SpotComparator implements Comparator<Spot> {

        @Override
        public int compare(Spot spot1, Spot spot2) {
            return spot1.name.compareTo(spot2.name);
        }
    }
}
