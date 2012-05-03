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

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
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

	/** Intent object used to start, stop, bind and unbind CVSService. */
	public static final Intent INTENT = new Intent("com.iwares.intent.action.CVS_DAEMON_SERVICE");

	/**
	 * Message handler used to handle request from CVSAgent.
	 *
	 * @author Eric.Tsai
	 *
	 */
	private class RequestHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			if (CVSService.this.onRequestMessage(msg))
				return;
			super.handleMessage(msg);
		}
	}

	/** Messenger used to communicate with CVSAgent. */
	private final Messenger mServiceMessenger = new Messenger(new RequestHandler());

	/* (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return mServiceMessenger.getBinder();
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null)
			startDaemon();
		return START_STICKY;
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		nativeOnCreate();
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		nativeOnDestroy();
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
		int result = 0;
		switch (msg.what) {
		case MSG_DAEMON_STATUS:
			result = isDaemonRunning() ? 1 : 0;
			replyMessage(msg.replyTo, msg.what, result);
			return true;
		case MSG_START_DAEMON:
			result = startDaemon() ? 1 : 0;
			replyMessage(msg.replyTo, msg.what, result);
			return true;
		case MSG_STOP_DAEMON:
			result = stopDaemon() ? 1 : 0;
			replyMessage(msg.replyTo, msg.what, result);
			return true;
		case MSG_SET_USER_PASSWORD:
			result = setUserPassword(
				((Bundle)msg.obj).getString("username"),
				((Bundle)msg.obj).getString("password")
				) ? 1 : 0;
			replyMessage(msg.replyTo, msg.what, result);
			return true;
		default:
			return false;
		}
	}

	/** Load libcvsdsrv.so for CVSDaemonService.*/
	static { System.loadLibrary("cvsdsrv"); }

	/** Integer value to hold the native CVSService object. */
	protected int mNativePtr = 0;

	/**
	 * Initial native CVSService object.
	 *
	 */
	private native void nativeOnCreate();

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
