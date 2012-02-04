package org.abarry.telo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Activity called when the user clicks on the message that indicates a message was received.
 * Doesn't do much.
 *
 */
public class MessageReceivedActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		setContentView(R.layout.activity_result);
		/*
		if (extras != null) {
			String message = extras.getString("payload");
			if (message != null && message.length() > 0) {
				TextView view = (TextView) findViewById(R.id.result);
				view.setText(message);
			}
		}*/

		TextView view = (TextView) findViewById(R.id.result);
		view.setText("received notification");
		
		super.onCreate(savedInstanceState);
	}

}