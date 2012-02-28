package org.abarry.telo;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.util.Log;
import android.widget.Toast;

/**
 * To use C2DM you must register your device against Google's servers.  This is the class that
 * deals with the callback for that registratoin (ie when it works, for example).
 * Gets the information in the callback such as the device key (which a server needs when sending
 * a notification.
 *
 * Then sends the key to our server which will write it down
 * in the MySQL database.
 *
 */
public class C2DMRegistrationReceiver extends BroadcastReceiver {

	// this is the URL of the register page that prints OK to the page when it
	// records to registration ID and phone data
	private static final String TELOBOT_REGISTER_URL = "http://9thsense.com/telo/register.php";


	private static final String C2DM_RETRY = "com.google.android.c2dm.intent.RETRY";

	private long backoffTimeMs = 30000;

	/**
	 * Called on a broadcast.  Checks to make sure it is the REGISTRATION broadcast
	 * and then sends the ID to the server.  If there is a failure, schedules a retry
	 * with exponential backoff.
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Log.v("C2DM", "Registration Receiver called");

		// check to see if this is the right broadcast for us
		if ("com.google.android.c2dm.intent.REGISTRATION".equals(action)) {
			Log.v("C2DM", "Received registration ID");
			final String registrationId = intent
					.getStringExtra("registration_id");
			String error = intent.getStringExtra("error");

			// check to see if there was an issue getting the ID
			if ("SERVICE_NOT_AVAILABLE".equals(error)) {

				// schedule a retry
		        Log.d("org.abarry.telo", "Scheduling registration retry, backoff = " + backoffTimeMs);
		        Intent retryIntent = new Intent(C2DM_RETRY);
		        PendingIntent retryPIntent = PendingIntent.getBroadcast(context,
		                0 /*requestCode*/, retryIntent, 0 /*flags*/);

		        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		        am.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + backoffTimeMs,
		                retryPIntent);

		        // Next retry should wait longer (exponential backoff)
		        backoffTimeMs *= 2;
		        return;
		    }


			Log.d("C2DM", "dmControl: registrationId = " + registrationId
					+ ", error = " + error);

			// get phone ID
			String deviceId = Secure.getString(context.getContentResolver(),
					Secure.ANDROID_ID);

			// create a notification that we got the ID
			// this is safe to comment out.
			Toast.makeText(context, "Registration successful", Toast.LENGTH_SHORT).show();

			// get the phone name
			SharedPreferences prefs = context.getSharedPreferences("org.abarry.telo",
					Context.MODE_WORLD_WRITEABLE );
			String phoneName = prefs.getString("phoneName", "default name");

			// send the ID to the server
			sendRegistrationIdToServer(deviceId, phoneName, registrationId);


			saveRegistrationId(context, registrationId);




		/*
		} else if ("org.abarry.telo.RETRY".equals(action)) {
			Log.w("C2DM", "RETRY Receiver called");
			checkForRetry(context, intent);
		*/
		}
	}

	/**
	 * Checks the internal preferences to see if there is an ID.  If not,
	 * attempts a retry.  Currently unused since Android seems to do a pretty
	 * good job of caching attempts to register when the internet connection
	 * is down.
	 */
	/*
	private void checkForRetry(Context context, Intent intent)
	{
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
//		Editor edit = prefs.edit();

		boolean regOk = prefs.getBoolean(TeloActivity.REGISTERED, false);

		if (regOk != true)
		{
			// need to retry

			// restart the service to make it retry
			Intent serviceIntent = new Intent();
			serviceIntent.setAction("org.abarry.telo.TeloStartupService");
			context.startService(serviceIntent);

		}
	}
	*/

	/**
	 * save the registration ID to a preference (not currently used)
	 */
	private void saveRegistrationId(Context context, String registrationId) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		Editor edit = prefs.edit();
		edit.putString(TeloActivity.AUTH, registrationId);
		edit.putBoolean(TeloActivity.REGISTERED, true);
		edit.commit();
	}


	/**
	 * Sends the registration ID to the server, with the URL defined in
	 * TELOBOT_REGISTER_URL
	 *
	 * @param deviceId unique device ID
	 * @param registrationId registration ID from Google
	 * @param phoneName name of the phone to display to the user on the website
	 * @return returns true on success (server said OK)  If this is true, there is a very high probability that everything is working well
	 */
	public void sendRegistrationIdToServer(String deviceId, String phoneName,
			String registrationId) {
		Log.d("C2DM", "Sending registration ID to my application server");
		final HttpClient client = new DefaultHttpClient();
		final HttpPost post = new HttpPost(TELOBOT_REGISTER_URL);

		// set up the POST data
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
		// Get the deviceID
		nameValuePairs.add(new BasicNameValuePair("deviceid", deviceId));
		nameValuePairs.add(new BasicNameValuePair("phonename", phoneName));
		nameValuePairs.add(new BasicNameValuePair("registrationid",
				registrationId));

		// encode the POST data
		try {
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		} catch (UnsupportedEncodingException e1) {
			//e1.printStackTrace();
		}

		// execute the HTML POST call to hit the server
		new Thread(new Runnable() {
		    public void run() {
		    	try {
					HttpResponse response = client.execute(post);
				} catch (ClientProtocolException e) {
					//e.printStackTrace();
				} catch (IOException e) {
					//e.printStackTrace();
				}
		    }
		  }).start();

	}
}
