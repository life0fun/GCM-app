package org.abarry.telo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

/**
 * Service that attempts to register with C2DM from Google
 * Is implemented as a service so that it's easy to call on boot.
 *
 */
public class TeloStartupService extends Service {

	//private long backoffTimeMs = 30000;
	//private long backoffTimeMs = 10000;
	
	@Override
	public IBinder onBind(Intent intent) {

		return null;

	}

	@Override
	public void onCreate() {
		super.onCreate();

		Toast.makeText(this, "Service Created", Toast.LENGTH_LONG).show();

	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();

	}

	@Override
	public void onStart(Intent intent, int startId) {

		super.onStart(intent, startId);

		Toast.makeText(this, "Service started, attempting registration...", Toast.LENGTH_LONG).show();
		
		// attempt a registration
		Log.w("C2DM", "start registration process");
		
		// set up the registration intent
		Intent intentC2dm = new Intent("com.google.android.c2dm.intent.REGISTER");
		intentC2dm.putExtra("app",
				PendingIntent.getBroadcast(this, 0, new Intent(), 0));

		// this must be the registered gmail account, the comment in the tutorial is wrong
		intentC2dm.putExtra("sender", "telebotphone@gmail.com");
		
		// fire the registration intent
		startService(intentC2dm);
		
		// commented out retry code since Android does a good job of caching failed
		// register calls
		/*
		Intent intentRetry = new Intent("org.abarry.telo.RETRY");
		
		PendingIntent pending = PendingIntent.getBroadcast(this, 0, intentRetry, 0);
		
		// setup for checking to make sure things worked
		AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + backoffTimeMs,
                pending);
		backoffTimeMs *= 2;
		*/
	}
}