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
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.localz.spotz.sdk.Spotz;
import com.localz.spotz.sdk.app.widgets.CustomAnimation;
import com.localz.spotz.sdk.listeners.InitializationListenerAdapter;
import com.localz.spotz.sdk.models.Spot;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends Activity {
    public static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_BLUETOOTH = 100;

    private OnEnteredSpotBroadcastReceiver enteredSpotBroadcastReceiver;
    private OnExitedSpotBroadcastReceiver exitedSpotBroadcastReceiver;
    private OnIntegrationRespondedBroadcastReceiver integrationRespondedBroadcastReceiver;
    // Tracks spot ids of the spots that device is in
    HashMap<String, Spot> inSpotMap = new HashMap<String, Spot>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Register BLE scan receivers
        enteredSpotBroadcastReceiver = new OnEnteredSpotBroadcastReceiver();
        exitedSpotBroadcastReceiver = new OnExitedSpotBroadcastReceiver();
        integrationRespondedBroadcastReceiver = new OnIntegrationRespondedBroadcastReceiver();
        registerReceiver(enteredSpotBroadcastReceiver,
                new IntentFilter(getPackageName() + ".SPOTZ_ON_SPOT_ENTER"));
        registerReceiver(exitedSpotBroadcastReceiver,
                new IntentFilter(getPackageName() + ".SPOTZ_ON_SPOT_EXIT"));
        registerReceiver(integrationRespondedBroadcastReceiver, new IntentFilter(getPackageName() + ".SPOTZ_ON_INTEGRATION_RESPONDED"));

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
                "3tzlSb16Yfi6uh3UfjJDnygSaMOHPFYIs1Xm5pgQ", // Your application ID goes here
                "4l1Jdi2C4Tz6XE3PD5uqehjeUuPIilD3oyx5S5FZ", // Your client key goes here
                null,
                null,
                new InitializationListenerAdapter() {
                    @Override
                    public void onInitialized() {

                        TextView rangeText = (TextView) findViewById(R.id.activity_range_text);

                        if (getString(R.string.message_initializing).equalsIgnoreCase(rangeText.getText().toString())) {
                            setOutOfRange(null);
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
            Spot spot = (Spot) intent.getSerializableExtra(Spotz.EXTRA_SPOTZ);
            setOutOfRange(spot);
        }
    }

    /**
     * Optional!!!!!!!!. Only used if there is an integration configured for the application which require response.
     */
    public class OnIntegrationRespondedBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String integrationResponse = (String) intent.getSerializableExtra(Spotz.EXTRA_INTEGRATION);

            Log.d(TAG, "Integration Response: " + integrationResponse);

            /*
               The response will have the following format:
               {
                 "date": "2015-01-07T20:34:00.594Z",
                 "548662128f658a08006d684d": {
                 "httpGetWebhook": {
                        "base": "USD",
                        "date": "2015-01-07",
                        "rates": {
                           "AUD": 1.2391,
                           "EUR": 0.8452
                        }
                 }
               }

               where data - is the timestamp when actual response occurred
               548662128f658a08006d684d - is spot id
               httpGetWebhook - is the name of the integration, as shown in the Spotz Web Dashboard.
               The value of "httpGetWebhook" is the JSON string of the response from the integration.
               extractInfoFromIntegrationResponse - is method specific to parsing response from the
               3rd party API.
             */

            try {
                JSONObject rootObject = new JSONObject(integrationResponse);
                Iterator keys = rootObject.keys();
                if (keys != null) {
                    while (keys.hasNext()) {
                        String key = (String) keys.next();
                        if (key.equalsIgnoreCase("date")) {
                            // at this stage we not interested in the date.
                            continue;
                        } else {
                            // each spot that had integration triggered for
                            JSONObject integrationResponseObject = rootObject
                                    .getJSONObject(key);
                            String responseMessage = extractInfoFromIntegrationResponse(integrationResponseObject);

                            // In this example, we just show Toast with the response to user
                            Toast toast = Toast.makeText(MainActivity.this, responseMessage, Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.TOP | Gravity.CENTER_VERTICAL, 10, 10);
                            toast.show();

                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * Extracting data from integration response.
     * This method is specific to the integration and its response. Hence you need to write it yourself.
     * This example is specific to parsing response from http://api.fixer.io/
     *
     * @param integrationResponseObject - json of the response from 3rd party server.
     * @return formatted message
     */
    private String extractInfoFromIntegrationResponse(JSONObject integrationResponseObject) {

        String baseCurrency = null;
        String euroExchangeRage = null;
        String returnMessage = "Data not received or corrupted";

        try {
            // HTTP GET WEBHOOK
            String httpGetWebhookResponseString = integrationResponseObject
                    .getString("httpGetWebhook");
            Log.d(TAG, "httpGetWebhookResponseString: " + httpGetWebhookResponseString);
            JSONObject httpGetWebhookResponseObject = new JSONObject(
                    httpGetWebhookResponseString);
            JSONObject ratesDictionary = httpGetWebhookResponseObject
                    .getJSONObject("rates");
            Log.d(TAG, "ratesDictionary: " + ratesDictionary);
            baseCurrency = (String) httpGetWebhookResponseObject
                    .get("base");
            Log.d(TAG, "baseCurrency: " + baseCurrency);
            Double ratesEURValue = (Double) ratesDictionary
                    .get("EUR");
            Log.d(TAG, "ratesEURValue: " + ratesEURValue);
            euroExchangeRage = "" + ratesEURValue;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if ((baseCurrency != null) && (euroExchangeRage != null)) {
            returnMessage = "Exchange rate from " + baseCurrency + " to Euro is: " + euroExchangeRage;
        }
        return returnMessage;
    }

    private void setInRange(final Spot spot) {
        TextView rangeText = (TextView) findViewById(R.id.activity_range_text);
        rangeText.setText(getString(R.string.message_in_range) + " " + spot.name);

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

        if (spot.enteredBeacon != null) {
            TextView serialText = (TextView) findViewById(R.id.activity_serial_text);
            serialText.setVisibility(View.VISIBLE);
            serialText.setText(spot.enteredBeacon.serial);
        }

        if (inSpotMap.isEmpty()) {
            TransitionDrawable transition = (TransitionDrawable) findViewById(R.id.wave).getBackground();
            transition.resetTransition();
            transition.startTransition(400);
        }

        inSpotMap.put(spot.id, spot);
    }

    private void setOutOfRange(final Spot spot) {
        if (spot != null) {
            inSpotMap.remove(spot.id);
        }

        if (inSpotMap.isEmpty()) {

            TextView rangeText = (TextView) findViewById(R.id.activity_range_text);
            rangeText.setText(R.string.message_not_in_range);

            TextView serialText = (TextView) findViewById(R.id.activity_serial_text);
            serialText.setVisibility(View.GONE);

            TransitionDrawable transition = (TransitionDrawable) findViewById(R.id.wave).getBackground();
            transition.resetTransition();
            transition.reverseTransition(400);

            findViewById(R.id.activity_spot_data_text).setVisibility(View.INVISIBLE);


        } else {
            setInRange((Spot) inSpotMap.values().toArray()[0]);
        }
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
