package org.abarry.telo;

import android.content.res.Resources;
import android.view.View;

public abstract class AccessoryController {

	public AccessoryController() {
	}


	void accessoryAttached() {
		onAccesssoryAttached();
	}

	abstract protected void onAccesssoryAttached();

}
