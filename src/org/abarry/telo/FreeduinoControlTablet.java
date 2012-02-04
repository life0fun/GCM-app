package org.abarry.telo;

import org.abarry.telo.BaseActivity;
import org.abarry.telo.OutputController;

public class FreeduinoControlTablet extends BaseActivity {
	private OutputController mOutputController;

	protected void hideControls() {
		super.hideControls();
		mOutputController = null;
	}

	protected void showControls() {
		super.showControls();
		mOutputController = new OutputController(this, true);
		mOutputController.accessoryAttached();
	}
}
