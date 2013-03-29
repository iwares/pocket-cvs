/*
 * Copyright (C) 2011 iWARES Solution Provider
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

/**
 * @file	src/com/iwares/app/pocketcvs/CVSService.java
 * @author	Eric.Tsai
 *
 */

package com.iwares.app.pocketcvs;

import java.lang.ref.WeakReference;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

/**
 * The CVS service.
 *
 * @author Eric.Tsai
 *
 */
public class CVSService extends Service {

	/** Repository path of CVS service. */
	public static final String REPOSITORY_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.cvsrepo";

	/** Temporary path of CVS service. */
	public static final String TEMPORARY_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.cvstemp";

	/** Intent action of the CVSService. */
	private static final String ACTION_CVS_DAEMON = "com.iwares.intent.action.CVS_DAEMON_SERVICE";

	/** Key of start up extra of CVSService intent. */
	private static final String KEY_RESTORE = "com.iwares.app.pocketcvs.RESOTRE";

	/** Name of the preferences file name. */
	private static final String SHARED_PREFENCES_NAME = "cvsdaemon.prefs";

	/** Key used to access daemon status (boolean) in cvsdaemon.prefs */
	private static final String KEY_DAEMON_STATUS = "Daemon Status";

	/**
	 * Start CVSService.
	 *
	 * @param context	Context.
	 * @param restore	If restore is true, The CVSService will stop if the saved
	 *					CVS daemon status is stopped.
	 * @return			If the service is being started or is already running, the
	 *					ComponentName of the actual service that was started is
	 *					returned; else if the service does not exist null is
	 *					returned.
	 */
	public static final ComponentName startService(Context context, boolean restore) {
		Intent intent = new Intent(ACTION_CVS_DAEMON)
			.putExtra(KEY_RESTORE, restore)
			;
		return context.startService(intent);
	}

	/**
	 * Stop CVSService.
	 *
	 * @param context	Context.
	 * @return			If there is a service matching the given Intent that is
	 *					already running, then it is stopped and true is returned;
	 *					else false is returned.
	 */
	public static final boolean stopService(Context context) {
		Intent intent = new Intent(ACTION_CVS_DAEMON);
		return context.stopService(intent);
	}

	/**
	 * Bind CVSService.
	 *
	 * @param context	Context
	 * @param conn		Receives information as the service is started and stopped.
	 * @param flags		BIND_NOT_FOREGROUND, BIND_ABOVE_CLIENT,
	 *					BIND_ALLOW_OOM_MANAGEMENT, or BIND_WAIVE_PRIORITY.
	 * @return			If you have successfully bound to the service, true is
	 *					returned; false is returned if the connection is not made
	 *					so you will not receive the service object.
	 */
	public static final boolean bindService(Context context, ServiceConnection conn, int flags) {
		Intent intent = new Intent(ACTION_CVS_DAEMON);
		return context.bindService(intent, conn, flags);
	}

	/**
	 * Message handler used to handle request from CVSAgent.
	 *
	 * @author Eric.Tsai
	 *
	 */
	private static class RequestHandler extends Handler {

		private final WeakReference<CVSService> mCVSServiceRef;

		public RequestHandler(CVSService cvsService) {
			mCVSServiceRef = new WeakReference<CVSService>(cvsService);
		}

		@Override
		public void handleMessage(Message msg) {
			CVSService cvsService = mCVSServiceRef.get();
			if (cvsService != null && cvsService.onRequestMessage(msg))
				return;
			super.handleMessage(msg);
		}

	}

	/** Messenger used to communicate with CVSAgent. */
	private final Messenger mServiceMessenger = new Messenger(new RequestHandler(this));

	/* (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return mServiceMessenger.getBinder();
	}

	/** Shared preferences */
	private SharedPreferences mPreferences = null;

	/* (non-Javadoc)
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (mPreferences.getBoolean(KEY_DAEMON_STATUS, false)) {
			Log.i("CVSService", "Saved CVS daemon status is 'started', start CVS daemon.");
			startDaemon();
		} else if (intent.getBooleanExtra(KEY_RESTORE, true)) {
			Log.i("CVSService", "Saved CVS daemon status is 'stopped', stop CVSService.");
			stopSelf();
		} else {
			Log.i("CVSService", "CVSService is ready for serve.");
		}
		return START_STICKY;
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		mPreferences = getSharedPreferences(SHARED_PREFENCES_NAME, MODE_PRIVATE);
		nativeOnCreate(REPOSITORY_PATH, TEMPORARY_PATH);
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		nativeOnDestroy();
		mPreferences = null;
		super.onDestroy();
	}

	/** Message ID used by CVS daemon status. */
	public static final int MSG_DAEMON_STATUS = 0x00001000;

	/** Message ID used by start daemon request and response. */
	public static final int MSG_START_DAEMON = 0x00001001;

	/** Message ID used by stop daemon request and response. */
	public static final int MSG_STOP_DAEMON = 0x00001002;

	/** Message ID used by change user name password request and response. */
	public static final int MSG_SET_USER_PASSWORD = 0x00001003;

	/**
	 * Reply message to the specified messenger.
	 *
	 * @param messenger	Messenger to reply.
	 * @param what		Message ID.
	 * @param arg1		Message argument.
	 *
	 * @return			Return true if the message is replied successfully,
	 * 					otherwise return false.
	 *
	 */
	protected boolean replyMessage(Messenger messenger, int what, int arg1) {
		Message msg = Message.obtain(null, what, arg1, 0, null);
		try { messenger.send(msg); } catch (Exception e) { return false; }
		return true;
	}

	/**
	 * Called when the CVSService received a message from CVSAgent.
	 *
	 * @param msg	Received message object.
	 *
	 * @return		Return true if the message is processed, otherwise return
	 * 				false.
	 *
	 */
	protected boolean onRequestMessage(Message msg) {
		Log.d("CVSService", msg.toString());
		boolean result = false;
		switch (msg.what) {
		case MSG_DAEMON_STATUS:
			result = isDaemonRunning();
			saveDaemonStatus(result);
			replyMessage(msg.replyTo, msg.what, result ? 1 : 0);
			return true;
		case MSG_START_DAEMON:
			result = startDaemon();
			saveDaemonStatus(isDaemonRunning());
			replyMessage(msg.replyTo, msg.what, result ? 1 : 0);
			return true;
		case MSG_STOP_DAEMON:
			result = stopDaemon();
			saveDaemonStatus(isDaemonRunning());
			replyMessage(msg.replyTo, msg.what, result ? 1 : 0);
			return true;
		case MSG_SET_USER_PASSWORD:
			result = setUserPassword(
				((Bundle)msg.obj).getString("username"),
				((Bundle)msg.obj).getString("password")
				);
			replyMessage(msg.replyTo, msg.what, result ? 1 : 0);
			return true;
		default:
			return false;
		}
	}

	/** Save current CVS daemon status to shared preferences. */
	protected void saveDaemonStatus(boolean isRunning) {
		boolean status = mPreferences.getBoolean(KEY_DAEMON_STATUS, false);
		if (status == isRunning)
			return;
		mPreferences.edit().putBoolean(KEY_DAEMON_STATUS, isRunning).commit();
	}

	/** Load libcvsdsrv.so for CVSDaemonService.*/
	static { System.loadLibrary("cvsdsrv"); }

	/** Integer value to hold the native CVSService object. */
	protected int mNativePtr = 0;

	/**
	 * Initial native CVSService object.
	 *
	 */
	private native boolean nativeOnCreate(String repoPath, String tempPath);

	/**
	 * Native method to change user name and password.
	 * @param username	New user name.
	 * @param password	New password.
	 *
	 * @return	Return true if success, otherwise return false.
	 *
	 */
	private native boolean setUserPassword(String username, String password);

	/**
	 * Native method to start the CVS daemon thread.
	 *
	 * @return	Return true if success, otherwise return false.
	 *
	 */
	private native boolean startDaemon();

	/**
	 * Native method to get the running status of the CVS daemon thread.
	 *
	 * @return	Return true if the CVS daemon thread is running, otherwise
	 *			return false.
	 *
	 */
	private native boolean isDaemonRunning();

	/**
	 * Native method to stop the CVS daemon thread.
	 *
	 * @return	Return true if success, otherwise return false.
	 *
	 */
	private native boolean stopDaemon();

	/**
	 * Delete native CVSService object.
	 *
	 */
	private native void nativeOnDestroy();

}
