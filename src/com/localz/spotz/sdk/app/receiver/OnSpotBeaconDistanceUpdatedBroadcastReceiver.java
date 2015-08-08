package com.localz.spotz.sdk.app.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
public class OnSpotBeaconDistanceUpdatedBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = OnSpotBeaconDistanceUpdatedBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Spot spot = (Spot) intent.getSerializableExtra(Spotz.EXTRA_SPOTZ);

        Log.d(TAG, "You have just exited spotz " + spot.name);
        Intent notificationIntent = new Intent(context, MainActivity.class);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0, notificationIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        Notification.Builder mBuilder =
                new Notification.Builder(context)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Exited Spotz")
                        .setContentText("You have just exited spotz " + spot.name)
                        .setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(("exit" + spot.spotId).hashCode(), mBuilder.build());

        new SpotzMap(context).remove(spot.spotId);

        // Notify activity
        Intent notifyActivityIntent = new Intent(context.getPackageName() + MainActivity.SPOT_ENTERED_OR_EXITED);
        notifyActivityIntent.setPackage(context.getPackageName());
        context.sendBroadcast(new Intent(context.getPackageName() + MainActivity.SPOT_ENTERED_OR_EXITED));
    }
}