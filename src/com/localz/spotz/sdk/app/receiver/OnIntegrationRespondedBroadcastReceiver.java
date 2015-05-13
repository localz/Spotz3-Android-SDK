package com.localz.spotz.sdk.app.receiver;

import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.localz.spotz.sdk.Spotz;
import com.localz.spotz.sdk.app.MainActivity;
import com.localz.spotz.sdk.app.R;

/**
 * Optional!!!!!!!!. Only used if there is an integration configured for the application which require response.
 */
public class OnIntegrationRespondedBroadcastReceiver extends BroadcastReceiver {

	@Override
    public void onReceive(Context context, Intent intent) {
        String integrationResponse = (String) intent.getSerializableExtra(Spotz.EXTRA_INTEGRATION);

        Log.d(MainActivity.TAG, "Integration Response: " + integrationResponse);

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
                        /*Toast toast = Toast.makeText(this.mainActivity, responseMessage, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.TOP | Gravity.CENTER_VERTICAL, 10, 10);
                        toast.show();
                        */
                        Notification.Builder mBuilder =
                                new Notification.Builder(context)
                                        .setSmallIcon(R.drawable.ic_launcher)
                                        .setContentTitle("Spotz Integration Response")
                                        .setContentText(responseMessage);

                        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        mNotificationManager.notify(2, mBuilder.build());


                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
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
            Log.d(MainActivity.TAG, "httpGetWebhookResponseString: " + httpGetWebhookResponseString);
            JSONObject httpGetWebhookResponseObject = new JSONObject(
                    httpGetWebhookResponseString);
            JSONObject ratesDictionary = httpGetWebhookResponseObject
                    .getJSONObject("rates");
            Log.d(MainActivity.TAG, "ratesDictionary: " + ratesDictionary);
            baseCurrency = (String) httpGetWebhookResponseObject
                    .get("base");
            Log.d(MainActivity.TAG, "baseCurrency: " + baseCurrency);
            Double ratesEURValue = (Double) ratesDictionary
                    .get("EUR");
            Log.d(MainActivity.TAG, "ratesEURValue: " + ratesEURValue);
            euroExchangeRage = "" + ratesEURValue;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if ((baseCurrency != null) && (euroExchangeRage != null)) {
            returnMessage = "Exchange rate from " + baseCurrency + " to Euro is: " + euroExchangeRage;
        }
        return returnMessage;
    }
}