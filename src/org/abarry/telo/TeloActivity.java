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
 * Activity for the telo robot. Displays the GUI and allows you
 * to name the robot, see your registration key, and force a registration
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
		// fire an intent to start the registration service
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
		Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
		Log.d("C2DM RegId", string);

	}
	
	/**
	 * Saves the phone name in a shared preference
	 * (called when "Save Name" button is pressed)
	 * @param view
	 */
	public void saveName(View view) {
		// get the phone name out of the textbot
		EditText textbox = (EditText) findViewById(R.id.phoneName);
		SharedPreferences prefs = getSharedPreferences("org.abarry.telo", MODE_WORLD_WRITEABLE );
		Editor edit = prefs.edit();
		String thisName = textbox.getText().toString();
		
		// check to make sure that the name 
		if (thisName.length() > 0)
		{
			// put the phone's name into the shared preferences so we'll remember it
			// in other places and between runs
			edit.putString("phoneName", thisName);
			edit.commit();
			
			// display a notification that we saved it
			Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
		} else {
			// display a notification that the user must enter a name
			Toast.makeText(this, "Enter a name", Toast.LENGTH_SHORT).show();
		}
	}
}