package org.abarry.telo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Gets the startup intent and calls the service that will register with Google
 * This exists as glue between the "hey we just finished booting" code and the
 * registration code 
 *
 */
public class TeloStartupIntentReceiver extends BroadcastReceiver{
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent serviceIntent = new Intent();
		serviceIntent.setAction("org.abarry.telo.TeloStartupService");
		
		// start up the service that does registration
		context.startService(serviceIntent);
	}
}