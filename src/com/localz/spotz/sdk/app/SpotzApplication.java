package com.localz.spotz.sdk.app;

import android.annotation.TargetApi;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;

/**
 * Created by aleksey on 15/08/2017.
 */

public class SpotzApplication extends Application {

    public static final String NOTIFICATION_CHANNEL_ID = "spotz_enter_exit_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // The id of the channel.
        String id = NOTIFICATION_CHANNEL_ID;
        // The user-visible name of the channel.
        CharSequence name = "Spotz notifications";
        // The user-visible description of the channel.
        String description = "Spotz notifications";
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel mChannel = new NotificationChannel(id, name, importance);
        // Configure the notification channel.
        mChannel.setDescription(description);
        mChannel.enableLights(true);
        // Sets the notification light color for notifications posted to this
        // channel, if the device supports this feature.
        mChannel.setLightColor(Color.YELLOW);
        mChannel.enableVibration(true);
        mChannel.setVibrationPattern(new long[]{100, 200});
        mNotificationManager.createNotificationChannel(mChannel);
    }
}
