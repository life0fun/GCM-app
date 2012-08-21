package com.colorcloud.gcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.colorcloud.gcm.GcmApp.GCMLog;
import com.google.android.gcm.GCMRegistrar;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;


import static com.colorcloud.gcm.GcmApp.SENDER_ID;
import static com.colorcloud.gcm.GcmApp.SERVER_URL;
import static com.colorcloud.gcm.MainActivity.displayMessage;

/**
 * Helper class used to communicate with the demo server.
 */
public final class ServerUtils {

    private static final String TAG = "GCM_ServUtil";
    
    private static final int MAX_ATTEMPTS = 5;
    private static final int BACKOFF_MILLI_SECONDS = 2000;
    private static final Random random = new Random();

    /**
     * register device to GCM and get registration id
     */
    static String getGcmRegistrationId(final Context context) {
        checkNotNull(SERVER_URL, "SERVER_URL");
        checkNotNull(SENDER_ID, "SENDER_ID");
        // Make sure the device has the proper dependencies.
        GCMRegistrar.checkDevice(context);
        // Make sure the manifest was properly set - comment out this line
        // while developing the app, then uncomment it when it's ready.
        GCMRegistrar.checkManifest(context);
        
        final String regId = GCMRegistrar.getRegistrationId(context);  // final, wont be null ?
        if (regId.equals("")) {
            GCMLog.d(TAG, "getGcmRegistrationId : not yet registered, start registering with project Id ");
            // Automatically registers application to GCM
            GCMRegistrar.register(context, SENDER_ID);   // get the regId thru GCM intent.
        } else {
            // already registered
            if (GCMRegistrar.isRegisteredOnServer(context)) {
                // Skips registration.
                GCMLog.d(TAG, "getGcmRegistrationId : registered id to my server :" + regId);
                //mDisplay.append(getString(R.string.already_registered) + "\n");
            } else {
                // Try to register again, but not in the UI thread.
                // It's also necessary to cancel the thread onDestroy(),
                GCMLog.d(TAG, "getGcmRegistrationId : register gcm id to App server :" + regId);
                boolean registered = ServerUtils.registerGcmIdToAppServer(context, regId);
                if (!registered) {
                    // GCMRegistrar.unregister(context);
                }
            }
            GcmService.postMessage(GcmService.MSG_GCM_REGID, 0, regId);
        }
        return regId;
    }
    
    /**
     * Register this account/device pair within the server.
     *
     * @return whether the registration succeeded or not.
     */
    static boolean registerGcmIdToAppServer(final Context context, final String regId) {
        GCMLog.i(TAG, "registering device (regId = " + regId + ")");
        GcmService.postMessage(GcmService.MSG_GCM_REGID, 0, regId);
        
        String serverUrl = SERVER_URL + "/register";
        Map<String, String> params = new HashMap<String, String>();
        params.put("regId", regId);
        long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
        // Once GCM returns a registration id, we need to register it in the
        // remote server. As the server might be down, we will retry it a couple times.
 
        /*
        for (int i = 1; i <= MAX_ATTEMPTS; i++) {
            Log.d(TAG, "Attempt #" + i + " to register");
            try {
                displayMessage(context, context.getString(
                        R.string.server_registering, i, MAX_ATTEMPTS));
                post(serverUrl, params);
                GCMRegistrar.setRegisteredOnServer(context, true);
                String message = context.getString(R.string.server_registered);
                CommonUtils.displayMessage(context, message);
                return true;
            } catch (IOException e) {
                // Here we are simplifying and retrying on any error; in a real
                // application, it should retry only on unrecoverable errors
                // (like HTTP error code 503).
                Log.e(TAG, "Failed to register on attempt " + i, e);
                if (i == MAX_ATTEMPTS) {
                    break;
                }
                try {
                    Log.d(TAG, "Sleeping for " + backoff + " ms before retry");
                    Thread.sleep(backoff);
                } catch (InterruptedException e1) {
                    // Activity finished before we complete - exit.
                    Log.d(TAG, "Thread interrupted: abort remaining retries!");
                    Thread.currentThread().interrupt();
                    return false;
                }
                // increase backoff exponentially
                backoff *= 2;
            }
        }
        */
        String message = context.getString(R.string.server_register_error, MAX_ATTEMPTS);
        displayMessage(context, message);
        return false;
    }

    /**
     * Unregister this account/device pair within the server.
     */
    static void unregisterGcmIdFromAppServer(final Context context, final String regId) {
        Log.i(TAG, "unregistering device (regId = " + regId + ")");
        GcmService.postMessage(GcmService.MSG_GCM_REGID, 0, "");
        
        String serverUrl = SERVER_URL + "/unregister";
        Map<String, String> params = new HashMap<String, String>();
        params.put("regId", regId);
        try {
            post(serverUrl, params);
            GCMRegistrar.setRegisteredOnServer(context, false);
            String message = context.getString(R.string.server_unregistered);
            displayMessage(context, message);
        } catch (IOException e) {
            // At this point the device is unregistered from GCM, but still
            // registered in the server.
            // We could try to unregister again, but it is not necessary:
            // if the server tries to send a message to the device, it will get
            // a "NotRegistered" error message and should unregister the device.
            String message = context.getString(R.string.server_unregister_error,
                    e.getMessage());
            displayMessage(context, message);
        }
    }

    /**
     * Issue a POST request to the server.
     *
     * @param endpoint POST address.
     * @param params request parameters.
     *
     * @throws IOException propagated from POST.
     */
    private static void post(String endpoint, Map<String, String> params)
            throws IOException {
        URL url;
        try {
            url = new URL(endpoint);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("invalid url: " + endpoint);
        }
        StringBuilder bodyBuilder = new StringBuilder();
        Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
        // constructs the POST body using the parameters
        while (iterator.hasNext()) {
            Entry<String, String> param = iterator.next();
            bodyBuilder.append(param.getKey()).append('=')
                    .append(param.getValue());
            if (iterator.hasNext()) {
                bodyBuilder.append('&');
            }
        }
        String body = bodyBuilder.toString();
        Log.v(TAG, "Posting '" + body + "' to " + url);
        byte[] bytes = body.getBytes();
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setFixedLengthStreamingMode(bytes.length);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded;charset=UTF-8");
            // post the request
            OutputStream out = conn.getOutputStream();
            out.write(bytes);
            out.close();
            // handle the response
            int status = conn.getResponseCode();
            if (status != 200) {
              throw new IOException("Post failed with error code " + status);
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
    
    private static void checkNotNull(Object reference, String name) {
        if (reference == null) {
            throw new NullPointerException(" Please set the constant and recompile the app : " + name);
        }
    }
    
    
    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    public static void showNotification(Context context, String message) {
        int icon = R.drawable.ic_stat_gcm;
        long when = System.currentTimeMillis();
        
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        Notification notification = new Notification(icon, message, when);
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // pendingIntent that will start a new activity.
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT);

        CharSequence contentText = message;
        notification.setLatestEventInfo(context, "gcm", contentText, contentIntent);
        notificationManager.notify(1, notification);
        GCMLog.d(TAG, "generateNotification: " + message);
        String title = context.getString(R.string.app_name);
        
//        Notification notification = new Notification.Builder(context)
//            .setContentTitle(title)
//            .setContentText(message)
//            .setSmallIcon(icon)
//            .setDeleteIntent(intent).
//            .build();
    }
}
