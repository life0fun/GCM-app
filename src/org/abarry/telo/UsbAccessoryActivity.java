package org.abarry.telo;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/* This Activity does nothing but receive USB_DEVICE_ATTACHED events from the
 * USB service and springboards to the main activity and the USB service
 */
public final class UsbAccessoryActivity extends Activity {

	static final String TAG = "UsbAccessoryActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent i = new Intent(this, UsbFromC2DMService.class);
		i.putExtra("payload", "startup");
		startService(i);
		
		Intent intent = new Intent(this, TeloActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		try {
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Log.e(TAG, "unable to start FreeduinoControl activity", e);
		}
		finish();
	}
}
