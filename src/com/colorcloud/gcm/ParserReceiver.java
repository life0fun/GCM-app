package com.colorcloud.gcm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.colorcloud.gcm.GcmApp.GCMLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * If you send a push via the client SDK or the REST API, you can also specify an Intent to be fired 
 * when the push notification is received (not the time the user taps the push notification). 
 * This will allow your app to perform custom handling for the notification, and can be used whether or not 
 * you have chosen to display a system tray message.
 * To implement custom notification handling, set the "action" entry in your push notification data dictionary 
 * to the Intent action which you want to fire, as described in the REST API docs. 
 *
 * ParsePush push = new ParsePush();
 * push.setChannels(channels);
 * push.setExpirationTimeInterval(86400);
 * push.setData(new JSONObject("{\"action\": \"com.colorcloud.gcm.locationchange\", \"msg\": \"hello from parser\" });
 * {"action":"com.colorcloud.gcm.locationchange","msg":"hello from parser"}
 * push.sendInBackground();
 */

public class ParserReceiver extends BroadcastReceiver {
    private static final String TAG = "GCM_ParseRcvr";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive : " + intent.toString());
        try {
            String action = intent.getAction();
            String channel = intent.getExtras().getString("com.parse.Channel");
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            Log.d(TAG, "onReceive : action " + action + " on channel " + channel + " with extras:");
        
            Iterator<String> itr = json.keys();
            while (itr.hasNext()) {
                String key = (String) itr.next();
                GCMLog.d(TAG, " onReceive: data dict :" + key + " => " + json.getString(key));
            }
            String msg = json.getString("msg");
            GCMLog.d(TAG, "onReceive : msg : " + msg);
            GcmService.postMessage(GcmService.MSG_GCM_MESSAGE, 0, msg);  // post a msg to service to process
            
        } catch (JSONException e) {
            Log.d(TAG, "JSONException: " + e.getMessage());
        }
    }
}