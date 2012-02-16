package org.abarry.telo;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

/**
 * Class that manages the USB device, including registering, closing, and
 * communication.
 *
 * Is a service so you can have other applications running in the foreground but
 * still manage USB commands in the background.
 *
 * @author abarry
 *
 */
public class UsbFromC2DMService extends Service implements Runnable {
	private static final String TAG = "UsbFromC2DMService";

	private static final String ACTION_USB_PERMISSION = "org.abarry.telo.action.USB_PERMISSION";


	private UsbManager mUsbManager;
	private PendingIntent mPermissionIntent;
	private boolean mPermissionRequestPending;

	UsbAccessory mAccessory;
	ParcelFileDescriptor mFileDescriptor;
	FileInputStream mInputStream;
	FileOutputStream mOutputStream;

	// Freediuno dependent constants
	public static final byte MOTOR_SERVO_COMMAND = 2;
	private static final int MESSAGE_SWITCH = 1;


	protected class SwitchMsg {
		private byte sw;
		private byte state;

		public SwitchMsg(byte sw, byte state) {
			this.sw = sw;
			this.state = state;
		}

		public byte getSw() {
			return sw;
		}

		public byte getState() {
			return state;
		}
	}

	/** receives notifications about when a device is disconnected
	 * this is important so we can close the device and kill ourselves
	 * so that next time a USB device shows up we'll start from scratch
	 * and register the device
	 *
	 * Also has code for opening a device, but that does not seem to run (?)
	 */
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Toast("received broadcast");
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					//UsbAccessory accessory = UsbManager.getAccessory(intent);
					UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
					if (intent.getBooleanExtra(
							UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						openAccessory(accessory);
					} else {
						Log.d(TAG, "permission denied for accessory "
								+ accessory);
						Toast("permission denied for accessory");
					}
					mPermissionRequestPending = false;
				}
			} else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
				//UsbAccessory accessory = UsbManager.getAccessory(intent);
				UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
				if (accessory != null && accessory.equals(mAccessory)) {
					closeAccessory();
				}
			}
		}
	};

	/** Called when the service is first created. */
	public void onCreate() {
		super.onCreate();

		//mUsbManager = UsbManager.getInstance(this);
		mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

		// register ourselves to get info when the USB device is disconnected
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
				ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		registerReceiver(mUsbReceiver, filter);

		// check for a existing input and output streams
		if (mInputStream != null && mOutputStream != null) {
			return;
		}

		// load the list of USB devices
		UsbAccessory[] accessories = mUsbManager.getAccessoryList();
		UsbAccessory accessory = (accessories == null ? null : accessories[0]);
		if (accessory != null) {

			// check to see if we can open the device
			if (mUsbManager.hasPermission(accessory)) {
				// open the USB device
				openAccessory(accessory);
			} else {
				// ask for permission to open the device
				synchronized (mUsbReceiver) {
					if (!mPermissionRequestPending) {
						mUsbManager.requestPermission(accessory,
								mPermissionIntent);
						mPermissionRequestPending = true;
					}
				}
			}
		} else {
			Log.d(TAG, "mAccessory is null");
		}

	}


	/**
	 * Run whenever there is a C2DM push notification and also
	 * on the startup of the service.
	 *
	 * Calls the code that parses the payload and sends a command
	 * to the USB device
	 */
	public int onStartCommand (Intent intent, int flags, int startId)
	{
		// fire off a sendCommand based on the data in intent

		Log.d(TAG, "in onStartCommand");
		Toast("in onStartCommand");

		// get the payload string
		Bundle extras = intent.getExtras();
		String payload = extras.getString("payload", "");
		boolean startup = extras.getBoolean("startup", false);

		// check to see if this isn't a USB call but is a
		// "we just started call"
		if (startup != true)
		{
			// call the USB parsing/command function
			doUsb(payload);
		}

		return 0;

	}

	/**
	 * Function that parses the push notification command and sends a USB event.
	 * If you are changing this code, this is likely the function you're interested
	 * in changing.
	 *
	 * @param payload
	 */
	private void doUsb(String payload) {
		// throw a toast saying that we're firing a USB event
		CharSequence text = "USB Command: \"" + payload + "\" payload length: " + payload.length();
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(this, text, duration);
		toast.show();

		/*// check for the "Forward" button being pressed
		if (payload.equals("f"))
		{
			// sending a -1 will make the LED bright on pin 3
			sendCommand((byte) 2, (byte) 0, (byte) -1);

		} else {

			// sending a 78 will dim the LED on pin 3
			sendCommand((byte) 2, (byte) 0, (byte) 78);
		}

		// ------------- TODO: more sendCommand calls here ----------- //

		 */

		//if (payload.equals("f"))
		//{
			//byte value = 'f';
		int Value = 0;
		byte ValueSent = 0;
		if (payload.length() > 1)
		{
			Value = Integer.getInteger(payload.substring(1)); // take the numeric part
			ValueSent = 1;
		}

		if (Value < 0) Value = 0;
		else if (Value > 255) Value = 255;

			sendCommand((byte) payload.charAt(0), ValueSent, (byte) Value);
		//}
		//else sendCommand((byte) 3, (byte) 1, (byte) 0);

	}


	public void onDestroy() {
		unregisterReceiver(mUsbReceiver);
		super.onDestroy();
	}

	/**
	 * Open the USB accessory
	 * Note that this calls the Runnable here to fire off
	 * the thread that actually opens the device
	 *
	 * @param accessory the device to open
	 */
	private void openAccessory(UsbAccessory accessory) {
		mFileDescriptor = mUsbManager.openAccessory(accessory);
		if (mFileDescriptor != null) {
			mAccessory = accessory;
			FileDescriptor fd = mFileDescriptor.getFileDescriptor();
			mInputStream = new FileInputStream(fd);
			mOutputStream = new FileOutputStream(fd);
			Thread thread = new Thread(null, this, "DemoKit");
			thread.start();
			Log.d(TAG, "accessory opened");
			Toast("accessory opened");
		} else {
			Log.d(TAG, "accessory open fail");
			Toast("accessory open fail");
		}
	}

	private void closeAccessory() {

		try {
			if (mFileDescriptor != null) {
				mFileDescriptor.close();
			}
		} catch (IOException e) {
		} finally {
			mFileDescriptor = null;
			mAccessory = null;
		}

		// kill ourselves (stop this service) so that next time we
		// get a new USB device we start the service from scratch
		stopSelf();
	}

	/**
	 * Thread that actually registeres the USB device.  Taken directly from the
	 * Google sample code
	 */
	public void run() {
		int ret = 0;
		byte[] buffer = new byte[16384];
		int i;

		while (ret >= 0) {
			try {
				ret = mInputStream.read(buffer);
			} catch (IOException e) {
				break;
			}

			i = 0;
			while (i < ret) {
				int len = ret - i;

				switch (buffer[i]) {
				case 0x1:
					if (len >= 3) {
						Message m = Message.obtain(mHandler, MESSAGE_SWITCH);
						m.obj = new SwitchMsg(buffer[i + 1], buffer[i + 2]);
						mHandler.sendMessage(m);
					}
					i += 3;
					break;



				default:
					Log.d(TAG, "unknown msg: " + buffer[i]);
					Toast("unknown msg: " + buffer[i]);
					i = len;
					break;
				}
			}

		}
	}

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_SWITCH:
				SwitchMsg o = (SwitchMsg) msg.obj;
				handleSwitchMessage(o);
				break;

			}
		}
	};

	/** Helper function for toasts
	 *
	 * @param toastStr string to toast
	 */
	private void Toast(CharSequence toastStr)
	{
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(this, toastStr, duration);
		toast.show();
	}

	/**
	 * Sends commands to the USB port.
	 *
	 * @param command
	 * @param target
	 * @param value
	 */
	public void sendCommand(byte command, byte target, int value) {

		CharSequence text = "USB Command: " + command + " target: " + target + " value: " + value;
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(this, text, duration);
		toast.show();

		byte[] buffer = new byte[3];
		if (value > 255)
			value = 255;

		buffer[0] = command;
		buffer[1] = target;
		buffer[2] = (byte) value;
		if (mOutputStream != null && buffer[1] != -1) {
			try {
				mOutputStream.write(buffer);
			} catch (IOException e) {
				Log.e(TAG, "write failed", e);
			}
		}
	}

protected void handleSwitchMessage(SwitchMsg o) {
}

@Override
public IBinder onBind(Intent intent) {
	// TODO Auto-generated method stub
	return null;
}

}
