package com.iwares.app.pocketcvs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MediaReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("MediaReceiver", "Media mounted, trying to restore CVS service.");
		CVSService.startService(context, true);
	}

}
