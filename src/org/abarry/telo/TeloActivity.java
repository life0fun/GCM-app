package org.abarry.telo;

import android.app.Activity;
import android.os.Bundle;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Activity for the telo robot.
 *
 */
public class TeloActivity extends Activity {

	public final static String AUTH = "authentication";
	public final static String REGISTERED = "registered";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// fill in the name box
		SharedPreferences prefs = getSharedPreferences("org.abarry.telo", MODE_WORLD_WRITEABLE );
		String phoneName = prefs.getString("phoneName", "default name");
		
		setContentView(R.layout.main);
		
		EditText textbox = (EditText) findViewById(R.id.phoneName);
		textbox.setText(phoneName);
		
	}

	/**
	 *  Called when you press the register button.
	 *  Starts the register service.
	 *  
	 *  @param view
	 */
	public void register(View view) {
		/*Log.w("C2DM", "start registration process");
		Intent intent = new Intent("com.google.android.c2dm.intent.REGISTER");
		intent.putExtra("app",
				PendingIntent.getBroadcast(this, 0, new Intent(), 0));

		// this must be the registered gmail account, the comment in the tutorial is wrong
		intent.putExtra("sender", "telebotphone@gmail.com");
		startService(intent);
		*/
		
		Intent serviceIntent = new Intent();
		serviceIntent.setAction("org.abarry.telo.TeloStartupService");
		startService(serviceIntent);
		
	}

	/**
	 * Displays a toast with the registration ID
	 * 
	 * @param view
	 */
	public void showRegistrationId(View view) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		String string = prefs.getString(AUTH, "n/a");
		Toast.makeText(this, string, Toast.LENGTH_LONG).show();
		Log.d("C2DM RegId", string);

	}
	
	/**
	 * Saves the phone name in a shared preference
	 * @param view
	 */
	public void saveName(View view) {
		// get the phone name out of the textbot
		EditText textbox = (EditText) findViewById(R.id.phoneName);
		SharedPreferences prefs = getSharedPreferences("org.abarry.telo", MODE_WORLD_WRITEABLE );
		Editor edit = prefs.edit();
		String thisName = textbox.getText().toString();
		
		if (thisName.length() > 0)
		{
			edit.putString("phoneName", thisName);
			edit.commit();
			
			Toast.makeText(this, "Saved", Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(this, "Enter a name", Toast.LENGTH_LONG).show();
		}
	}
}