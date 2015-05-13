package com.localz.spotz.sdk.app;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Set;

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
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.localz.spotz.sdk.Spotz;
import com.localz.spotz.sdk.Spotz.ScanMode;
import com.localz.spotz.sdk.app.model.SpotzMap;
import com.localz.spotz.sdk.app.widgets.CustomAnimation;
import com.localz.spotz.sdk.listeners.InitializationListenerAdapter;
import com.localz.spotz.sdk.listeners.RangingListener;
import com.localz.spotz.sdk.models.Spot;

public class MainActivity extends Activity {
	public static final String TAG = MainActivity.class.getSimpleName();
	public static final String SPOT_ENTERED_OR_EXITED = "SPOT_ENTERED_OR_EXITED";
	private static final int REQUEST_BLUETOOTH = 100;

	// This BroadcastReceiver is to be notified when device either enter or exit
	// spot.
	// It is used to refresh status on the screen.
	private OnEnterOrExitBroadcastReceiver enterOrExitSpotBroadcastReceiver;
	
	// Tracks spot ids of the spots that device is in
	private SpotzMap inSpotMap;
	TextView nameOfSpotText;
	TextView rangingDistanceTextView;
	TextView startStop;

	DecimalFormat df = new DecimalFormat("0.00");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		inSpotMap = new SpotzMap(this);

		rangingDistanceTextView = (TextView) findViewById(R.id.activity_spot_ranging_distance);
		nameOfSpotText = (TextView) findViewById(R.id.activity_range_text);
		startStop = (TextView) findViewById(R.id.start_stop);

		enterOrExitSpotBroadcastReceiver = new OnEnterOrExitBroadcastReceiver();
		registerReceiver(enterOrExitSpotBroadcastReceiver, new IntentFilter(
				getPackageName() + SPOT_ENTERED_OR_EXITED));
		final Spotz spotz = Spotz.getInstance();

		// Show either "Start Scanning" or "Stop Scanning" depending on the
		// status
		boolean isScanningForSpotz = spotz
				.isScanningForSpotz(MainActivity.this);
		if (isScanningForSpotz) {
			startStop.setText(getString(R.string.stop_scanning));
		} else {
			startStop.setText(getString(R.string.start_scanning));
		}

		startStop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (spotz.isScanningForSpotz(MainActivity.this)) {
					spotz.stopScanningForSpotz(MainActivity.this);
					startStop.setText(getString(R.string.start_scanning));
					inSpotMap.clear();
					adjustUI();
				} else {
					boolean isInitialised = spotz
							.isInitialized(MainActivity.this);
					if (!isInitialised) {
						nameOfSpotText
								.setText(getString(R.string.message_initializing));

						startStop.setVisibility(View.INVISIBLE);
						inSpotMap.clear();
						// Initialise Spotz
						initialiseSpotzSdk();
					} else {
						// setOutOfRange(null);
						Spotz.getInstance().startScanningForSpotz(
								MainActivity.this, Spotz.ScanMode.EAGER);
						startStop.setText(getString(R.string.stop_scanning));
					}

				}
			}
		});
	}

	private void initialiseSpotzSdk() {
		// Let's initialize the spotz sdk so we can start receiving callbacks
		// for any spotz we find!
		Spotz.getInstance().initialize(this,
				"your-application-id",          // Your application ID goes here
		        "your-client-key",              // Your client key goes here
				null, null, new InitializationListenerAdapter() {
					@Override
					public void onInitialized() {

						if (getString(R.string.message_initializing)
								.equalsIgnoreCase(
										nameOfSpotText.getText().toString())) {
							Spotz.getInstance().startScanningForSpotz(
									MainActivity.this, Spotz.ScanMode.EAGER);
						}

						startStop.setVisibility(View.VISIBLE);
						startStop.setText(getString(R.string.stop_scanning));
						CustomAnimation
								.startWaveAnimation(findViewById(R.id.wave));
					}

					@Override
					public void onError(Exception exception) {
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
				});
	}

	@Override
	protected void onResume() {
		super.onResume();
		rangingRunnable.run();

		adjustUI();
	}

	@Override
	protected void onPause() {
		super.onPause();
		rangingHandler.removeCallbacks(rangingRunnable);
	}

	private void adjustUI() {
		inSpotMap = new SpotzMap(this);

		MainActivity.this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if ((inSpotMap != null) && !inSpotMap.isEmpty()) {
					Spot spot = inSpotMap.get(inSpotMap.keySet().toArray()[0]);
					setInRange(spot);
					if ((spot.closestBeaconDistance != null)
							&& (spot.closestBeaconDistance > 0)) {
						rangingDistanceTextView.setVisibility(View.VISIBLE);
						rangingDistanceTextView.setText(MainActivity.this
								.getString(R.string.message_ranging_distance)
								+ " \n"
								+ df.format(spot.closestBeaconDistance)
								+ " meters");
					} else {
						rangingDistanceTextView.setVisibility(View.INVISIBLE);
					}
				} else {
					setOutOfRange(null);
					rangingDistanceTextView.setVisibility(View.INVISIBLE);
				}

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
		unregisterReceiver(enterOrExitSpotBroadcastReceiver);
	}

	private void setInRange(final Spot spot) {
		nameOfSpotText.setText(getString(R.string.message_in_range) + " "
				+ spot.name);

		View spotDataButton = findViewById(R.id.activity_spot_data_text);
		spotDataButton.setVisibility(View.VISIBLE);
		spotDataButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this,
						SpotDataActivity.class);
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
			TransitionDrawable transition = (TransitionDrawable) findViewById(
					R.id.wave).getBackground();
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

			nameOfSpotText.setText(R.string.message_not_in_range);

			TextView serialText = (TextView) findViewById(R.id.activity_serial_text);
			serialText.setVisibility(View.GONE);

			TransitionDrawable transition = (TransitionDrawable) findViewById(
					R.id.wave).getBackground();
			transition.resetTransition();
			transition.reverseTransition(400);

			findViewById(R.id.activity_spot_data_text).setVisibility(
					View.INVISIBLE);

		} else {
			setInRange((Spot) inSpotMap.values().toArray()[0]);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_BLUETOOTH) {
			BluetoothAdapter bluetoothAdapter = BluetoothAdapter
					.getDefaultAdapter();
			// For this example app, let's try to ensure bluetooth is switched
			// on
			if (bluetoothAdapter.isEnabled()) {
				initialiseSpotzSdk();
			} else {
				showBluetoothNotEnabledDialog();
			}
		}
	}

	private void showBluetoothNotEnabledDialog() {
		new AlertDialog.Builder(this)
				.setTitle("Bluetooth not enabled")
				.setMessage(R.string.message_bluetooth_not_enabled)
				.setCancelable(false)
				.setPositiveButton("Enable",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(
									DialogInterface dialogInterface, int i) {
								Intent intentOpenBluetoothSettings = new Intent();
								intentOpenBluetoothSettings
										.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
								startActivityForResult(
										intentOpenBluetoothSettings,
										REQUEST_BLUETOOTH);
								dialogInterface.dismiss();
							}
						})
				.setNegativeButton("Close",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(
									DialogInterface dialogInterface, int i) {
								dialogInterface.dismiss();
								finish();
							}
						}).show();
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
	// (OnEnteredSpotBroadcastReceiver
	// and OnExitedSpotBroadcastReceiver) will notify this receiver to adjust
	// UI.
	public class OnEnterOrExitBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			adjustUI();
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
		if (Spotz.getInstance().isScanningForSpotz(MainActivity.this)) {
			if (Spotz.getInstance().isAnySpotzToRange(MainActivity.this)) {
				clearRangingDistances();
				Spotz.getInstance().range(MainActivity.this,
						new RangingListener() {

							@Override
							public void onRangeIterationCompleted(
									HashMap<String, Double> spotIdsAndDistances) {
								// spotIdsAndDistances is <spotId, distance>
								// pair, which shows spotId and distance to the
								// closest beacon in the spot
								// Populated
								Set<String> spotIds = spotIdsAndDistances
										.keySet();
								for (String spotId : spotIds) {
									Spot spotOfRangingBeacon = inSpotMap
											.get(spotId);
									if (spotOfRangingBeacon != null) {
										spotOfRangingBeacon.closestBeaconDistance = spotIdsAndDistances
												.get(spotId);
										// this will write to storage
										inSpotMap.put(spotId,
												spotOfRangingBeacon);
									}
								}
								adjustUI();
							}
						});
			} else {
				adjustUI();
			}
		}
		rangingHandler.postDelayed(rangingRunnable, 1000);
	}

	private void clearRangingDistances() {
		Set<String> spotIds = inSpotMap.keySet();
		for (String spotId : spotIds) {
			Spot spot = inSpotMap.get(spotId);
			spot.closestBeaconDistance = null;
			inSpotMap.put(spotId, spot);
		}
	}

}
