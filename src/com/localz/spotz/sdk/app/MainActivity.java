package com.localz.spotz.sdk.app;

import android.Manifest;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.localz.sdk.core.util.LocalzEnvironment;
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

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String SPOT_ENTERED_OR_EXITED = ".SPOT_ENTERED_OR_EXITED";

    private static final int REQUEST_ENABLE_BT = 13;
    private static final int REQUEST_ACCESS_LOCATION = 14;

    // This BroadcastReceiver is to be notified when a device enters or exits a spot.
    // It is used to refresh status on the screen.
    private OnEnterOrExitBroadcastReceiver enterOrExitSpotBroadcastReceiver;
    // This BroadcastReceiver is to be notified of "distance-to-spot" change when a device is within a "ranging" spot.
    private OnSpotDistanceUpdatedBroadcastReceiver spotDistanceUpdatedBroadcastReceiver;
    private OnClosestSpotUpdatedBroadcastReceiver closestSpotUpdatedBroadcastReceiver;

    // Tracks ids of the spots that device is in
    private SpotzMap inSpotMap;
    private String closestSpotId;
    private TextView statusText;
    private Button startStop;

    private SpotListAdapter spotListAdapter;
    private List<Spot> spotList = new ArrayList<>();

    private SpotComparator spotComparator = new SpotComparator();

    private AnimatorSet animatorSet;
    private AlertDialog alertDialog;

    // track if the SDK has been initialised
    // (it's good practice to initialise Spotz SDK once per app start)
    private boolean initialised;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        initialised = false;

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
                if (Spotz.getInstance().isScanningForSpotz(getApplicationContext())) {
                    stopScanning();
                    inSpotMap.clear();
                } else {
                    if (!initialised) {
                        startStop.setVisibility(View.INVISIBLE);
                        inSpotMap.clear();
                        // Initialise Spotz
                        initialiseSpotzSdk(true);
                    } else {
                        startActiveScanning();
                    }
                }
                adjustUI();
            }
        });
        // output debug info, or not
        Spotz.getInstance().setDebug(getApplicationContext(), true);
    }

    /**
     * Initialise Spotz SDK
     *
     * @param startScanning if "true" - start scanning after initialisation
     */
    private void initialiseSpotzSdk(final boolean startScanning) {
        Log.d(TAG, "initialiseSpotzSdk");
        if (!initialised) {
            // Let's initialize the spotz sdk so we can start receiving callbacks for any spots we find!
            statusText.setText("Initialising");
            statusText.setVisibility(View.VISIBLE);
            startStop.setVisibility(View.INVISIBLE);

            Spotz.getInstance().enableSmoothing(getApplicationContext(), true);
            Spotz.getInstance().initialize(getApplicationContext(),
                    "vwSkqKuL6OMsUYXROgOCzCfcE2BqoOiLOcO46000", // Your application ID goes here
                    "wmczlaWfgT58Hs6P0VQojx2gLOYAfTiDoP7WGO1y", // Your client key goes here
                    LocalzEnvironment.DEV,
                    new InitializationListenerAdapter() {
                        @Override
                        public void onInitialized() {
                            initialised = true;
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
        } else if (startScanning) {
            startActiveScanning();
        }
    }

    /**
     * Start "active" (foreground) scanning.
     * This will scan for beacons and report results every 2 seconds.
     */
    private void startActiveScanning() {
        if (Spotz.getInstance().isInitialized(getApplicationContext())) {
            Log.d(TAG, "startActiveScanning");
            Spotz.getInstance().startForegroundScanning(getApplicationContext());
            startStop.setText(getString(R.string.stop_scanning));
            startStop.setVisibility(View.VISIBLE);
            animatorSet = CustomAnimation.startWaveAnimation(findViewById(R.id.wave));
        }
    }

    /**
     * Start "passive" (background) scanning.
     * This will scan for beacons and report results every 60 seconds.
     */
    private void startPassiveScanning() {
        if (Spotz.getInstance().isInitialized(getApplicationContext())) {
            Log.d(TAG, "startPassiveScanning");
            Spotz.getInstance().startBackgroundScanning(getApplicationContext());
        }
    }

    /**
     * Stop scanning.
     */
    private void stopScanning() {
        if (Spotz.getInstance().isInitialized(getApplicationContext())) {
            Log.d(TAG, "stopScanning");
            Spotz.getInstance().stopScanningForSpotz(getApplicationContext());
            startStop.setText(getString(R.string.start_scanning));
            startStop.setVisibility(View.VISIBLE);
            CustomAnimation.stopWaveAnimation(animatorSet);
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
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

        // Register a receiver to update UI when the closest spot is identified
        if (closestSpotUpdatedBroadcastReceiver == null) {
            closestSpotUpdatedBroadcastReceiver = new OnClosestSpotUpdatedBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter(getPackageName() + Spotz.BROADCAST_CLOSEST_BEACON);
            registerReceiver(closestSpotUpdatedBroadcastReceiver, intentFilter);
        }

        init();
    }

    private void init() {
        Log.d(TAG, "init");
        closeAlertDialog();
        if (hasPermissions()) {
            if (Spotz.getInstance().isScanningForSpotz(getApplicationContext())) {
                initialiseSpotzSdk(true);
            } else {
                new SpotzMap(this).clear();
                startStop.setText(getString(R.string.start_scanning));
                initBle();
            }
        } else {
            tryRequestPermissions();
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

        // If this activity is destroyed (sent to background) we want to unregister receiver.
        if (closestSpotUpdatedBroadcastReceiver != null) {
            try {
                unregisterReceiver(closestSpotUpdatedBroadcastReceiver);
                closestSpotUpdatedBroadcastReceiver = null;
            } catch (Exception e) {
            }
        }
        CustomAnimation.stopWaveAnimation(animatorSet);
        if (Spotz.getInstance().isScanningForSpotz(getApplicationContext())) {
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
                    if (Spotz.getInstance().isScanningForSpotz(getApplicationContext())) {
                        statusText.setText("Scanning");
                        statusText.setVisibility(View.VISIBLE);
                        setOutOfRange();
                    } else if (Spotz.getInstance().isInitialized(getApplicationContext())) {
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
        alertDialog = new AlertDialog.Builder(MainActivity.this)
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

    /**
     * Receive notifications when the closest spot is identified.
     */
    public class OnClosestSpotUpdatedBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Spot spot = (Spot) intent.getSerializableExtra(Spotz.EXTRA_SPOTZ);
            if (spot != null) {
                closestSpotId = spot.spotId;
            } else {
                closestSpotId = null;
            }
            adjustUI();
        }
    }

    private void initBle() {
        Log.d(TAG, "initBle");
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Bluetooth not enabled, select 'OK' to enable Bluetooth, 'Cancel' to continue");
                builder.setCancelable(false);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        closeAlertDialog();
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        closeAlertDialog();
                        initialiseSpotzSdk(false);
                    }
                });
                alertDialog = builder.create();
                alertDialog.show();
            } else {
                initialiseSpotzSdk(false);
            }
        } else {
            onBluetoothFailed();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled
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
                closeAlertDialog();
                initialiseSpotzSdk(false);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                closeAlertDialog();
                finish();
            }
        });
        alertDialog = builder.create();
        alertDialog.show();
    }

    private void closeAlertDialog() {
        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
    }

    /**
     * Check permissions if running Android 6.x
     *
     * @return true or false
     */
    private boolean hasPermissions() {
        return ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * If running Android 6 or up, a user may disable 'Location' permission for the app, so we need check if it is enabled and ask a user to enable it if it's disabled.
     */
    private void tryRequestPermissions() {
        // Should we show an explanation?
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)) {

            // Show an explanation to the user *asynchronously* -- don't block this thread waiting for the user's response!
            // After the user sees the explanation, try again to request the permission.

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Spotz SDK requires access to Location service. Is that OK?");
            builder.setCancelable(false);
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    closeAlertDialog();
                    requestPermissions();
                }
            });
            builder.setNegativeButton("Nope", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    closeAlertDialog();
                    finish();
                }
            });
            alertDialog = builder.create();
            alertDialog.show();

        } else {
            // No explanation needed, we can request the permission.
            requestPermissions();
        }
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_ACCESS_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult");
        if (requestCode == REQUEST_ACCESS_LOCATION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, yay!
            } else {
                // permission denied, boo!
            }
//            init();
        }
    }

    private class SpotListAdapter extends BaseAdapter {

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
            TextView trigger = (TextView) view.findViewById(R.id.trigger);
            spotName.setText("Spot name: " + spot.name);
            if (closestSpotId != null && spot.spotId.equals(closestSpotId)) {
                spotName.setText("Spot name: " + spot.name + " (* closest)");
            }
            trigger.setVisibility(View.VISIBLE);
            if (spot.enteredBeacon != null) {
                StringBuilder builder = new StringBuilder();
                builder.append("Beacon").append("\nid: ").append(spot.enteredBeacon.beaconId);
                builder.append("\nrssi: ").append(spot.enteredBeacon.rssi);
                builder.append(" txPower: ").append(spot.enteredBeacon.txPower);
                builder.append("\ndistance: ").append(spot.enteredBeacon.distance);
                trigger.setText(builder.toString());
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
            if (closestSpotId != null) {
                if (closestSpotId.equals(spot1.spotId)) {
                    return -1;
                } else if (closestSpotId.equals(spot2.spotId)) {
                    return 1;
                }
            }
            return spot1.name.compareTo(spot2.name);
        }
    }
}
