package com.colorcloud.gcm;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/** This Activity does nothing but receive USB_DEVICE_ATTACHED events from the
 * USB service and springboards to the main activity and the USB service
 */
public final class UsbAccessoryActivity extends Activity {

	static final String TAG = "UsbAccessoryActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// startup the service that will manage the USB device
		Intent i = new Intent(this, UsbFromC2DMService.class);
		i.putExtra("payload", "startup");
		i.putExtra("startup", true);
		startService(i);
		
		// startup the GUI for Telo
		Intent intent = new Intent(this, MainActivity.class);
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
