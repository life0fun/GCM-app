package org.abarry.telo;

public class ServoController {
	private final int mServoNumber;
	private final byte mCommandTarget;
	private FreeduinoControlActivity mActivity;

	public ServoController(FreeduinoControlActivity activity, int servoNumber) {
		mActivity = activity;
		mServoNumber = servoNumber;
		mCommandTarget = (byte) (servoNumber - 1 + 0x10);
	}

	public void onPositionChange(double value) {
		byte v = (byte) (value * 255);
		mActivity.sendCommand(FreeduinoControlActivity.MOTOR_SERVO_COMMAND,
				mCommandTarget, v);
	}

}
