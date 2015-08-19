package com.localz.spotz.sdk.app.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import com.localz.spotz.sdk.app.MainActivity;
import com.localz.spotz.sdk.app.R;

public class OnShowNotificationBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = OnShowNotificationBroadcastReceiver.class.getSimpleName();

    public static final String NOTIFICATION_TITLE = "NOTIFICATION_TITLE";
    public static final String NOTIFICATION_TEXT = "NOTIFICATION_TEXT";
    public static final String NOTIFICATION_ID = "NOTIFICATION_ID";
    public static final String NOTIFICATION_ICON = "NOTIFICATION_ICON";

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra(NOTIFICATION_TITLE);
        String text = intent.getStringExtra(NOTIFICATION_TEXT);
        int id = intent.getIntExtra(NOTIFICATION_ID, 1);
        int icon = intent.getIntExtra(NOTIFICATION_ICON, R.drawable.ic_launcher);

//        if (BuildConfig.DEBUG) {
        Log.d(TAG, "onReceive title=" + title);
//        }

        if (title != null && text != null) {
            Intent notificationIntent = new Intent(context, MainActivity.class);
//            notificationIntent.putExtra(SpotzListActivity.SPOT_ID, intent.getStringExtra(SpotzListActivity.SPOT_ID));
//            notificationIntent.putExtra(SpotzListActivity.EVENT, intent.getStringExtra(SpotzListActivity.EVENT));
//            notificationIntent.putExtra(SpotzListActivity.ACTION, intent.getStringExtra(SpotzListActivity.ACTION));
//            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);


            PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            Notification.Builder mBuilder =
                    new Notification.Builder(context)
                            .setSmallIcon(icon)
                            .setContentTitle(title)
                            .setContentText(text)
                            .setAutoCancel(true)
                            .setVibrate(new long[]{1000, 1000})
                            .setLights(Color.RED, 3000, 3000)
                            .setSound(alarmSound)
                            .setContentIntent(resultPendingIntent);

            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(id, mBuilder.build());
        }
    }
}