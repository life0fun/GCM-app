
package com.colorcloud.gcm;

import static com.colorcloud.gcm.GcmApp.SENDER_ID;
import static com.colorcloud.gcm.MainActivity.displayMessage;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.colorcloud.gcm.GcmApp.GCMLog;
import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

/**
 * {@link IntentService} responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService {

    @SuppressWarnings("hiding")
    private static final String TAG = "GCM_Intent";
    
    GcmApp mApp;

    public GCMIntentService() {
        super(SENDER_ID);
        mApp = (GcmApp)getApplication();
    }

    @Override
    protected void onRegistered(Context context, String registrationId) {
        GCMLog.i(TAG, "onRegistered : Device registered: regId = " + registrationId);
        displayMessage(context, getString(R.string.gcm_registered));
        ServerUtils.registerGcmIdToAppServer(context, registrationId);
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        GCMLog.i(TAG, "onUnregistered : Device unregistered");
        displayMessage(context, getString(R.string.gcm_unregistered));
        if (GCMRegistrar.isRegisteredOnServer(context)) {
            ServerUtils.unregisterGcmIdFromAppServer(context, registrationId);
        } else {
            // This callback results from the call to unregister made on
            // ServerUtils when the registration to the server failed.
            GCMLog.i(TAG, "Ignoring unregister callback");
        }
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        final String title = intent.getStringExtra("title");
        final String score = intent.getStringExtra("score");
        GCMLog.i(TAG, "onMessage : Received message : " + title + " " + score);
        String message = getString(R.string.gcm_message);
        GcmService.postMessage(GcmService.MSG_GCM_MESSAGE, 0, title + " " +  score);
        //displayMessage(context, message);
        // notifies user
    }

    @Override
    protected void onDeletedMessages(Context context, int total) {
        GCMLog.i(TAG, "onDeletedMessages : Received deleted messages notification");
        String message = getString(R.string.gcm_deleted, total);
        displayMessage(context, message);
    }

    @Override
    public void onError(Context context, String errorId) {
        GCMLog.i(TAG, "Received error: " + errorId);
        displayMessage(context, getString(R.string.gcm_error, errorId));
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        GCMLog.i(TAG, "onRecoverableError : Received recoverable error: " + errorId);
        displayMessage(context, getString(R.string.gcm_recoverable_error, errorId));
        return super.onRecoverableError(context, errorId);
    }
}
