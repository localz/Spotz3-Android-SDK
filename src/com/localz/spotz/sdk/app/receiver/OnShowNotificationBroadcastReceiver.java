package com.localz.spotz.sdk.app.receiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.localz.spotz.sdk.app.MainActivity;
import com.localz.spotz.sdk.app.R;

/**
 * Displays Spot data in the notification bar when the app is running in the background.
 */
public class OnShowNotificationBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = OnShowNotificationBroadcastReceiver.class.getSimpleName();

    static final String NOTIFICATION_TITLE = "NOTIFICATION_TITLE";
    static final String NOTIFICATION_TEXT = "NOTIFICATION_TEXT";
    static final String NOTIFICATION_ID = "NOTIFICATION_ID";
    static final String NOTIFICATION_ICON = "NOTIFICATION_ICON";

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra(NOTIFICATION_TITLE);
        String text = intent.getStringExtra(NOTIFICATION_TEXT);
        int id = intent.getIntExtra(NOTIFICATION_ID, 1);
        int icon = intent.getIntExtra(NOTIFICATION_ICON, R.drawable.ic_launcher);

        Log.d(TAG, "onReceive title=" + title);

        if (title != null && text != null) {
            Intent notificationIntent = new Intent(context, MainActivity.class);

            PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(icon)
                            .setContentTitle(title)
                            .setContentText(text)
                            .setAutoCancel(true)
                            .setVibrate(new long[]{1000})
                            .setLights(Color.YELLOW, 1000, 3000)
                            .setSound(alarmSound)
                            .setContentIntent(resultPendingIntent);

            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(id, mBuilder.build());
        }
    }
}