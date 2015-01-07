package com.localz.spotz.sdk.app;

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
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.localz.spotz.sdk.app.widgets.CustomAnimation;
import com.localz.spotz.sdk.Spotz;
import com.localz.spotz.sdk.listeners.InitializationListenerAdapter;
import com.localz.spotz.sdk.models.Spot;

public class MainActivity extends Activity {
    public static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_BLUETOOTH = 100;

    private OnEnteredSpotBroadcastReceiver enteredSpotBroadcastReceiver;
    private OnExitedSpotBroadcastReceiver exitedSpotBroadcastReceiver;
    private boolean isInRange = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Register BLE scan receivers
        enteredSpotBroadcastReceiver = new OnEnteredSpotBroadcastReceiver();
        exitedSpotBroadcastReceiver = new OnExitedSpotBroadcastReceiver();
        registerReceiver(enteredSpotBroadcastReceiver,
                new IntentFilter(getPackageName() + ".SPOTZ_ON_SPOT_ENTER"));
        registerReceiver(exitedSpotBroadcastReceiver,
                new IntentFilter(getPackageName() + ".SPOTZ_ON_SPOT_EXIT"));

        // Check if device has ble
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            // For this example app, let's try to ensure bluetooth is switched on
            if (bluetoothAdapter.isEnabled()) {
                initialiseSpotzSdk();
            } else {
                showBluetoothNotEnabledDialog();
            }
        } else {
            // Tell the user this device is not supported
            Toast.makeText(this, "Bluetooth not found on your device. You won't be able to use any bluetooth features",
                    Toast.LENGTH_SHORT).show();
            TextView rangeText = (TextView) findViewById(R.id.activity_range_text);
            rangeText.setText(R.string.message_device_not_supported);
        }
    }

    private void initialiseSpotzSdk() {
        // Let's initialize the spotz sdk so we can start receiving callbacks for any spotz we find!
        Spotz.getInstance().initialize(this,
                "your-application-id", // Your application ID goes here
                "your-client-key", // Your client key goes here

                new InitializationListenerAdapter() {
                    @Override
                    public void onInitialized() {

                        TextView rangeText = (TextView) findViewById(R.id.activity_range_text);

                        if (getString(R.string.message_initializing).equalsIgnoreCase(rangeText.getText().toString())) {
                            setOutOfRange();
                        }

                        CustomAnimation.startWaveAnimation(findViewById(R.id.wave));
                    }

                    @Override
                    public void onError(Exception exception) {
                        Log.e(TAG, "Exception while registering device", exception);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                createErrorDialogInitialising();
                            }
                        });
                    }
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Spotz.getInstance().isInitialized()) {
            Spotz.getInstance().startScanningForSpotz(this,
                    Spotz.ScanMode.EAGER);
        }
    }


    @Override
    protected void onPause() {
        // If this activity is paused we want to stop scanning for beacons
        // Depending on usecase, it might be desirable to leave scanning when application
        // is in background.
        if (Spotz.getInstance().isInitialized()) {
            Spotz.getInstance().stopScanningForSpotz(this);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // If this activity is destroyed we want to unregister receivers
        unregisterReceiver(exitedSpotBroadcastReceiver);
        unregisterReceiver(enteredSpotBroadcastReceiver);
    }

    public class OnEnteredSpotBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Spot spot = (Spot) intent.getSerializableExtra(Spotz.EXTRA_SPOTZ);

            setInRange(spot);
        }
    }

    public class OnExitedSpotBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            setOutOfRange();
        }
    }

    private void setInRange(final Spot spot) {
        TextView rangeText = (TextView) findViewById(R.id.activity_range_text);
        rangeText.setText(getString(R.string.message_in_range) + " " + spot.name);

        if (spot.enteredBeacon != null) {
            TextView serialText = (TextView) findViewById(R.id.activity_serial_text);
            serialText.setVisibility(View.VISIBLE);
            serialText.setText(spot.enteredBeacon.serial);
        }

        if (!isInRange) {
            TransitionDrawable transition = (TransitionDrawable) findViewById(R.id.wave).getBackground();
            transition.resetTransition();
            transition.startTransition(400);

            View spotDataButton = findViewById(R.id.activity_spot_data_text);
            spotDataButton.setVisibility(View.VISIBLE);
            spotDataButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, SpotDataActivity.class);
                    intent.putExtra(Spotz.EXTRA_SPOTZ, spot);
                    startActivity(intent);
                }
            });
        }

        isInRange = true;
    }

    private void setOutOfRange() {
        TextView rangeText = (TextView) findViewById(R.id.activity_range_text);
        rangeText.setText(R.string.message_not_in_range);

        TextView serialText = (TextView) findViewById(R.id.activity_serial_text);
        serialText.setVisibility(View.GONE);

        if (isInRange) {
            TransitionDrawable transition = (TransitionDrawable) findViewById(R.id.wave).getBackground();
            transition.resetTransition();
            transition.reverseTransition(400);

            findViewById(R.id.activity_spot_data_text).setVisibility(View.INVISIBLE);
        }

        isInRange = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_BLUETOOTH) {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            // For this example app, let's try to ensure bluetooth is switched on
            if (bluetoothAdapter.isEnabled()) {
                initialiseSpotzSdk();
            } else {
                showBluetoothNotEnabledDialog();
            }
        }
    }

    private void showBluetoothNotEnabledDialog() {
        new AlertDialog.Builder(this).setTitle("Bluetooth not enabled")
                .setMessage(R.string.message_bluetooth_not_enabled)
                .setCancelable(false)
                .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intentOpenBluetoothSettings = new Intent();
                        intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                        startActivityForResult(intentOpenBluetoothSettings, REQUEST_BLUETOOTH);
                        dialogInterface.dismiss();
                    }
                }).setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        finish();
                    }
                }
        ).show();
    }

    private void createErrorDialogInitialising() {
        new AlertDialog.Builder(MainActivity.this).setTitle("Unable to initialize")
                .setMessage(R.string.message_initialize_error)
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        finish();
                    }
                }).show();
    }
}
