package org.abarry.telo;

import android.content.res.Resources;
import android.view.View;

public abstract class AccessoryController {

	protected FreeduinoControlActivity mHostActivity;

	public AccessoryController(FreeduinoControlActivity activity) {
		mHostActivity = activity;
	}

	protected View findViewById(int id) {
		return mHostActivity.findViewById(id);
	}

	protected Resources getResources() {
		return mHostActivity.getResources();
	}

	void accessoryAttached() {
		onAccesssoryAttached();
	}

	abstract protected void onAccesssoryAttached();

}
