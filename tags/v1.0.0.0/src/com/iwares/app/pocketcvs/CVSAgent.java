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
 * @file	src/com/iwares/app/pocketcvs/CVSAgent.java
 * @author	Eric.Tsai
 *
 */

package com.iwares.app.pocketcvs;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

/**
 * CVSAgent is used to communicate with the CVSService.
 *
 * @author Eric.Tsai
 *
 */
public class CVSAgent extends Handler implements ServiceConnection {

	/** CVS Daemon status - stopped. */
	public static final int STATUS_STOPPED = 0;

	/** CVS Daemon status - running. */
	public static final int STATUS_RUNNING = 1;

	/**
	 * CVSAgent callback interface.
	 *
	 * @author Eric.Tsai
	 *
	 */
	public interface Callback {
		/**
		 * Called when the CVSAgent detected that the status of CVS daemon is
		 * changed.
		 *
		 * @param newStatus		The new CVS daemon status.
		 *
		 */
		public abstract void onDaemonStatusChanged(int newStatus);
		/**
		 * Called when the change user name password request returned.
		 *
		 * @param changed		Whether the user name and password is changed.
		 *
		 */
		public abstract void onUserPasswordChanged(boolean changed);
	}

	/** Current callback interface of this CVSAgent object. */
	private Callback mCallback;

	/**
	 * Constructor.
	 *
	 * @param callback	Callback interface for the new CVSAgent.
	 *
	 */
	public CVSAgent(Callback callback) {
		if (callback == null)
			throw new NullPointerException();
		mCallback = callback;
	}

	/** Messenger used to communicate with the CVSService. */
	private Messenger mService, mMessenger;

	/** Current CVS daemon status. */
	public int mStatus = STATUS_STOPPED;

	/**
	 * Post message to the CVSService.
	 *
	 * @param what	Message ID.
	 * @param arg1	Argument 1.
	 * @param arg2	Argument 2.
	 * @param obj	Object.
	 *
	 * @return		Return true if the message is sent successfully, otherwise
	 * 				return false.
	 */
	protected boolean postMessage(int what, int arg1, int arg2, Object obj) {
		Message msg = Message.obtain(null, what, arg1, arg2, obj);
		msg.replyTo = mMessenger;
		try { mService.send(msg); } catch (Exception e) { return false; }
		return true;
	}

	/* (non-Javadoc)
	 * @see android.content.ServiceConnection#onServiceConnected(
	 * android.content.ComponentName, android.os.IBinder)
	 */
	public void onServiceConnected(ComponentName name, IBinder service) {
		mMessenger = new Messenger(this);
		mService = new Messenger(service);
		postMessage(CVSService.MSG_DAEMON_STATUS, 0, 0, null);
	}

	/* (non-Javadoc)
	 * @see android.content.ServiceConnection#onServiceDisconnected(
	 * android.content.ComponentName)
	 */
	public void onServiceDisconnected(ComponentName name) {
		// Do nothing.
	}

	/**
	 * Request the CVSService to start the CVS daemon thread.
	 *
	 * @return	Return true if the request is post successfully, otherwise
	 * 			return false.
	 */
	public boolean requstStartDaemon() {
		return postMessage(CVSService.MSG_START_DAEMON, 0, 0, null);
	}

	/**
	 * Request the CVSService to stop the CVS daemon thread.
	 *
	 * @return	Return true if the request is post successfully, otherwise
	 * 			return false.
	 */
	public boolean requstStopDaemon() {
		return postMessage(CVSService.MSG_STOP_DAEMON, 0, 0, null);
	}

	/**
	 * Request the CVSService to change user name and password.
	 * @param username	New user name.
	 * @param password	New password.
	 *
	 * @return	Return true if the request is post successfully, otherwise
	 * 			return false.
	 */
	public boolean requstSetUserPassword(String username, String password) {
		Bundle bundle = new Bundle();
		bundle.putString("username", username);
		bundle.putString("password", password);
		return postMessage(CVSService.MSG_SET_USER_PASSWORD, 0, 0, (Object)bundle);
	}

	/* (non-Javadoc)
	 * @see android.os.Handler#handleMessage(android.os.Message)
	 */
	@Override
	public void handleMessage(Message msg) {
		Log.d("CVSAgent", msg.toString());
		switch (msg.what) {
		case CVSService.MSG_DAEMON_STATUS:
			mStatus = msg.arg1 == 1 ? STATUS_RUNNING : STATUS_STOPPED;
			mCallback.onDaemonStatusChanged(mStatus);
			break;
		case CVSService.MSG_START_DAEMON:
			mStatus = msg.arg1 == 1 ? STATUS_RUNNING : mStatus;
			mCallback.onDaemonStatusChanged(mStatus);
			break;
		case CVSService.MSG_STOP_DAEMON:
			mStatus = msg.arg1 == 1 ? STATUS_STOPPED : mStatus;
			mCallback.onDaemonStatusChanged(mStatus);
			break;
		case CVSService.MSG_SET_USER_PASSWORD:
			mCallback.onUserPasswordChanged(msg.arg1 == 1);
			break;
		default:
			super.handleMessage(msg);
			break;
		}
	}

}
