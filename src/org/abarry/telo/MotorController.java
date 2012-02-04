package org.abarry.telo;

public class MotorController {
	private int mMotorNumber;
	private FreeduinoControlActivity mActivity;

	class MotorValueUpdater {
		private final byte mCommandTarget;

		MotorValueUpdater() {
			mCommandTarget = (byte) (mMotorNumber - 1);
		}

		public void onPositionChange(double value) {
			int v = (int) (255 * value);
			if (mActivity != null) {
				mActivity.sendCommand(FreeduinoControlActivity.MOTOR_SERVO_COMMAND,
						mCommandTarget, (byte) v);
			}
		}
	}

	public MotorController(FreeduinoControlActivity activity, int number) {
		mActivity = activity;
		mMotorNumber = number;
	}

}
