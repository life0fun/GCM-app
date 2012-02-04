package org.abarry.telo;

import android.view.ViewGroup;
import org.abarry.telo.AccessoryController;
import org.abarry.telo.MotorController;
import org.abarry.telo.FreeduinoControlActivity;
import org.abarry.telo.R;
import org.abarry.telo.ServoController;

public class OutputController extends AccessoryController {

	private boolean mVertical;

	OutputController(FreeduinoControlActivity hostActivity, boolean vertical) {
		super(hostActivity);
		mVertical = vertical;
	}

	protected void onAccesssoryAttached() {
		//setupServoController(1, R.id.pan_Servo);
		//setupServoController(2, R.id.tilt_Servo);
		//setupMotorController(1, R.id.Motor);
	}

	private void setupServoController(int servoIndex, int viewId) {
		ServoController sc = new ServoController(mHostActivity, servoIndex);
		//sc.attachToView((ViewGroup) findViewById(viewId));
	}

	private void setupMotorController(int index, int viewId) {
		MotorController Motor = new MotorController(mHostActivity, index);
		//Motor.attachToView((ViewGroup) findViewById(viewId));
	}
}
