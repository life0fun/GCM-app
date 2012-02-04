package org.abarry.telo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Activity for when the user clicks on the "registration successful" notification.
 * Doesn't do much.
 * 
 * Note: if you did display the payload, it wouldn't update since you're only creating the text
 * in onCreate and not dealing with a reloaded activity.
 *
 */
public class RegistrationResultActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_result);
		//Bundle extras = getIntent().getExtras();
		//if (extras != null) {
		//	String registrationId = extras.getString("registration_id");
		//	if (registrationId != null && registrationId.length() > 0) {
				TextView view = (TextView) findViewById(R.id.result);
				//view.setText(registrationId);
				view.setText("registration succesful.");
		//	}
		//}

		super.onCreate(savedInstanceState);
	}
}
