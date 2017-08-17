package com.localz.spotz.sdk.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.localz.spotz.sdk.Spotz;
import com.localz.spotz.sdk.app.MainActivity;
import com.localz.spotz.sdk.app.R;
import com.localz.spotz.sdk.app.model.SpotzMap;
import com.localz.spotz.sdk.models.Spot;

/**
 * The onReceive() method of this receiver will be called when device is no longer in the proximity of a spot.
 * The spot will be passed in the intent's extra.
 * The receiver need to be registered in the AndroidManifest.xml file with action: com.localz.spotz.sdk.app.SPOTZ_ON_SPOT_ENTER
 *
 * @author Localz
 */
public class OnExitedSpotBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = OnExitedSpotBroadcastReceiver.class.getSimpleName();

    public static final int NOTIFICATION_ID = 2;

    @Override
    public void onReceive(Context context, Intent intent) {
        Spot spot = (Spot) intent.getSerializableExtra(Spotz.EXTRA_SPOTZ);

        Log.d(TAG, "You have just exited a spot " + spot.name);

        new SpotzMap(context).remove(spot.spotId);

        Intent notificationIntent = new Intent(context.getPackageName() + MainActivity.SPOT_ENTERED_OR_EXITED);
        notificationIntent.setPackage(context.getPackageName());
        notificationIntent.putExtra("SPOT_ID", spot.spotId);
        notificationIntent.putExtra("EVENT", "SPOT_ENTER");
        notificationIntent.putExtra(OnShowNotificationBroadcastReceiver.NOTIFICATION_ID, NOTIFICATION_ID);
        notificationIntent.putExtra(OnShowNotificationBroadcastReceiver.NOTIFICATION_TITLE, "Exited a Spot");
        notificationIntent.putExtra(OnShowNotificationBroadcastReceiver.NOTIFICATION_TEXT, "You have just exited a spot \"" + spot.name + "\"");
        notificationIntent.putExtra(OnShowNotificationBroadcastReceiver.NOTIFICATION_ICON, R.drawable.ic_launcher);

        // send an ordered broadcast, so that if the app is in the foreground it will show spot data, otherwise it will be displayed as a notification
        context.sendOrderedBroadcast(notificationIntent, null);
    }
}