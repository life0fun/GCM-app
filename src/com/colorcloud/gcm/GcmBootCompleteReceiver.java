package com.colorcloud.gcm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Gets the startup intent and calls the service that will register with Google
 * This exists as glue between the "hey we just finished booting" code and the
 * registration code 
 *
 */
public class GcmBootCompleteReceiver extends BroadcastReceiver{
	@Override
	public void onReceive(Context context, Intent intent) {
		 String action = intent.getAction();
		 Intent serviceIntent = new Intent(context, GcmService.class);
		 serviceIntent.setAction(action);   // put in action and extras
		 serviceIntent.putExtras(intent);
		 context.startService(serviceIntent);  // start the connection service 
	}
}