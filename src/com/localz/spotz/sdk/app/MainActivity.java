package com.localz.spotz.sdk.app;

import android.animation.AnimatorSet;
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
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.localz.spotz.sdk.Spotz;
import com.localz.spotz.sdk.app.model.SpotzMap;
import com.localz.spotz.sdk.app.widgets.CustomAnimation;
import com.localz.spotz.sdk.listeners.InitializationListenerAdapter;
import com.localz.spotz.sdk.models.Spot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends Activity {

    public static final String TAG = MainActivity.class.getSimpleName();

    public static final String SPOT_ENTERED_OR_EXITED = ".SPOT_ENTERED_OR_EXITED";

    private static final int REQUEST_ENABLE_BT = 13;

    private static final long FOREGROUND_INTERVAL = 2000;
    private static final long FOREGROUND_DURATION = 1000;

    private static final long BACKGROUND_INTERVAL = 60000;
    private static final long BACKGROUND_DURATION = 5000;

    // This BroadcastReceiver is to be notified when a device enters or exits a spot.
    // It is used to refresh status on the screen.
    private OnEnterOrExitBroadcastReceiver enterOrExitSpotBroadcastReceiver;
    // This BroadcastReceiver is to be notified of "distance-to-spot" change when a device is within a "ranging" spot.
    private OnSpotDistanceUpdatedBroadcastReceiver spotDistanceUpdatedBroadcastReceiver;

    // Tracks ids of the spots that device is in
    private SpotzMap inSpotMap;
    private TextView statusText;
    private Button startStop;

    private SpotListAdapter spotListAdapter;
    private List<Spot> spotList = new ArrayList<>();

    private SpotComparator spotComparator = new SpotComparator();

    private AnimatorSet animatorSet;

    private boolean scanning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inSpotMap = new SpotzMap(this);

        statusText = (TextView) findViewById(R.id.activity_status_text);
        startStop = (Button) findViewById(R.id.start_stop);
        ListView activeSpotsListView = (ListView) findViewById(R.id.spots_list_view);
        spotListAdapter = new SpotListAdapter();
        activeSpotsListView.setAdapter(spotListAdapter);

        // displays spot details
        activeSpotsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Spot spot = null;
                if (position < spotListAdapter.getCount()) {
                    spot = (Spot) spotListAdapter.getItem(position);
                }
                if (spot != null) {
                    Intent intent = new Intent(MainActivity.this, SpotDataActivity.class);
                    intent.putExtra(Spotz.EXTRA_SPOTZ, spot);
                    startActivity(intent);
                }
            }
        });

        // Show either "Start Scanning" or "Stop Scanning" depending on the status

        startStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (scanning || Spotz.getInstance().isScanningForSpotz(MainActivity.this)) {
                    stopScanning();
                    startStop.setText(getString(R.string.start_scanning));
                    inSpotMap.clear();
                } else {
                    boolean isInitialised = Spotz.getInstance().isInitialized(MainActivity.this);
                    if (!isInitialised) {
                        startStop.setVisibility(View.INVISIBLE);
                        inSpotMap.clear();
                        // Initialise Spotz
                        initialiseSpotzSdk(true);
                    } else {
                        startActiveScanning();
                        startStop.setText(getString(R.string.stop_scanning));
                    }
                }
                adjustUI();
            }
        });
    }

    /**
     * Initialise Spotz SDK
     *
     * @param startScanning if "true" - start scanning after initialisation
     */
    private void initialiseSpotzSdk(final boolean startScanning) {
        // Let's initialize the spotz sdk so we can start receiving callbacks for any spots we find!
        statusText.setText("Initialising");
        statusText.setVisibility(View.VISIBLE);
        startStop.setVisibility(View.INVISIBLE);
        Spotz.getInstance().initialize(this,
                "your-application-id",          // Your application ID goes here
                "your-application-key",         // Your application key goes here
                new InitializationListenerAdapter() {
                    @Override
                    public void onInitialized() {

                        if (startScanning) {
                            startActiveScanning();
                        } else {
                            startStop.setText(getString(R.string.start_scanning));
                        }
                        startStop.setVisibility(View.VISIBLE);
                        adjustUI();
                    }

                    @Override
                    public void onError(Throwable exception) {
                        Log.e(TAG, "Exception while registering device", exception);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                startStop.setVisibility(View.VISIBLE);
                                createErrorDialogInitialising();
                            }
                        });
                    }
                }, false);
    }

    /**
     * Start "active" (foreground) scanning.
     * This will scan for beacons and report results every 5 seconds.
     */
    private void startActiveScanning() {
        if (Spotz.getInstance().isInitialized(MainActivity.this)) {
            Log.d(TAG, "startActiveScanning");
            Spotz.getInstance().startForegroundScanning(MainActivity.this, FOREGROUND_INTERVAL, FOREGROUND_DURATION);
            animatorSet = CustomAnimation.startWaveAnimation(findViewById(R.id.wave));
        }
    }

    /**
     * Start "passive" (background) scanning.
     * This will scan for beacons and report results every 60 seconds.
     */
    private void startPassiveScanning() {
        if (Spotz.getInstance().isInitialized(MainActivity.this)) {
            Log.d(TAG, "startPassiveScanning");
            scanning = false;
            Spotz.getInstance().startBackgroundScanning(MainActivity.this, BACKGROUND_INTERVAL, BACKGROUND_DURATION);
        }
    }

    /**
     * Stop scanning.
     */
    private void stopScanning() {
        if (Spotz.getInstance().isInitialized(MainActivity.this)) {
            Log.d(TAG, "stopScanning");
            scanning = false;
            Spotz.getInstance().stopScanningForSpotz(MainActivity.this);
            CustomAnimation.stopWaveAnimation(animatorSet);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        adjustUI();

        // Register a receiver to update UI when a device enters or exits a spot
        if (enterOrExitSpotBroadcastReceiver == null) {
            enterOrExitSpotBroadcastReceiver = new OnEnterOrExitBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter(getPackageName() + SPOT_ENTERED_OR_EXITED);
            intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
            registerReceiver(enterOrExitSpotBroadcastReceiver, intentFilter);
        }

        // Register a receiver to update UI when a device gets closer or further away from a "ranging" spot
        if (spotDistanceUpdatedBroadcastReceiver == null) {
            spotDistanceUpdatedBroadcastReceiver = new OnSpotDistanceUpdatedBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter(getPackageName() + Spotz.BROADCAST_DISTANCE);
            registerReceiver(spotDistanceUpdatedBroadcastReceiver, intentFilter);
        }
        // output debug info
        Spotz.getInstance().setDebug(true);

        boolean isScanningForSpotz = Spotz.getInstance().isScanningForSpotz(MainActivity.this);
        if (isScanningForSpotz) {
            // if was already scanning (in the background) then continue to do so but in the foreground
            startStop.setText(getString(R.string.stop_scanning));
            startActiveScanning();
        } else {
            new SpotzMap(this).clear();
            startStop.setText(getString(R.string.start_scanning));
            scanning = false;
            init();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // If this activity is destroyed (sent to background) we want to unregister receiver.
        // Note: notifications about spot enter and exit will still be delivered to
        // OnEnteredSpotBroadcastReceiver and OnExitedSpotBroadcastReceiver.
        if (enterOrExitSpotBroadcastReceiver != null) {
            try {
                unregisterReceiver(enterOrExitSpotBroadcastReceiver);
                enterOrExitSpotBroadcastReceiver = null;
            } catch (Exception e) {
            }
        }

        // If this activity is destroyed (sent to background) we want to unregister receiver.
        if (spotDistanceUpdatedBroadcastReceiver != null) {
            try {
                unregisterReceiver(spotDistanceUpdatedBroadcastReceiver);
                spotDistanceUpdatedBroadcastReceiver = null;
            } catch (Exception e) {
            }
        }
        CustomAnimation.stopWaveAnimation(animatorSet);
        if (scanning || Spotz.getInstance().isScanningForSpotz(MainActivity.this)) {
            // if was already scanning (in the foreground) then continue to do so but in the background
            startPassiveScanning();
        }
    }

    /**
     * Refresh UI elements.
     */
    private void adjustUI() {
        inSpotMap = new SpotzMap(this);
        spotList.clear();

        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!inSpotMap.isEmpty()) {
                    spotList.addAll(inSpotMap.values());
                    Collections.sort(spotList, spotComparator);
                    statusText.setVisibility(View.VISIBLE);
                    statusText.setText("Scanning");
                    setInRange();
                } else {
                    if (scanning || Spotz.getInstance().isScanningForSpotz(MainActivity.this)) {
                        statusText.setText("Scanning");
                        statusText.setVisibility(View.VISIBLE);
                        setOutOfRange();
                    } else if (Spotz.getInstance().isInitialized(MainActivity.this)) {
                        statusText.setText("Initialised");
                        statusText.setVisibility(View.VISIBLE);
                    } else {
                        statusText.setText("Not initialised");
                        statusText.setVisibility(View.VISIBLE);
                    }
                }
                spotListAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Set animation.
     */
    private void setInRange() {
        TransitionDrawable transition = (TransitionDrawable) findViewById(R.id.wave).getBackground();
        transition.startTransition(400);
    }

    /**
     * Set animation.
     */
    private void setOutOfRange() {
        TransitionDrawable transition = (TransitionDrawable) findViewById(R.id.wave).getBackground();
        transition.resetTransition();
    }

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
    // (OnEnteredSpotBroadcastReceiver and OnExitedSpotBroadcastReceiver) will notify this receiver to adjust UI.
    public class OnEnterOrExitBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            adjustUI();
            abortBroadcast();
        }
    }

    /**
     * Receive notifications when a device gets closer or further away from a "ranging" spot.
     */
    public class OnSpotDistanceUpdatedBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Spot spot = (Spot) intent.getSerializableExtra(Spotz.EXTRA_SPOTZ);
            Log.d(TAG, "Spot distance updated " + spot.name);
            new SpotzMap(context).put(spot.spotId, spot);
            adjustUI();
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
                        if (!Spotz.getInstance().isInitialized(MainActivity.this)) {
                            MainActivity.this.initialiseSpotzSdk(false);
                        } else {
                            adjustUI();
                        }
                    }
                });
                builder.show();
            } else if (!Spotz.getInstance().isInitialized(MainActivity.this)) {
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
