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
 * @file	src/com/iwares/app/pocketcvs/IPAddressMonitor.java
 * @author	Eric.Tsai
 *
 */

package com.iwares.app.pocketcvs;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;

/**
 * IPAddressMonitor is used to monitor WiFi IP address status. If the IP address
 * is changed, a callback will be invoked.
 *
 * @author Eric.Tsai
 *
 */
public class IPAddressMonitor {

	/** Android OS Handler which the Runnable will be post to. */
	protected final Handler mHanlder = new Handler();

	/** Indicate whether the IP address monitor is running. */
	protected boolean mRunning = false;
	
	/** This Runnable will be executed every 500 MS if monitor is running. */
	protected final Runnable mRunnable = new Runnable() {
		public void run() {
			monitorIPAddress();
			if (!mRunning)
				return;
			mHanlder.postDelayed(mRunnable, 500);
		}
	};

	/**
	 * IPAddressMonitor callback interface.
	 *
	 * @author Eric.Tsai
	 *
	 */
	public interface Callback {
		/**
		 * This method will be called when the IP address is changed.
		 *
		 * @param ipAddress	The new IP address, this will be null if there is no
		 *					available networks.
		 *
		 */
		public abstract void onIPAddressChanged(String ipAddress);
	}

	/** Android WiFiManager service. */
	protected WifiManager mWiFiManager = null;

	/** Current callback interface of this IPAddressMonitor object. */
	protected Callback mCallback = null;

	/** Last IP address. */
	protected String mLastIPAddress = null;

	/**
	 * Constructor.
	 *
	 * @param context	Context.
	 * @param callback	Callback interface for the new CVSAgent.
	 *
	 */
	public IPAddressMonitor(Context context, Callback callback) {
		mWiFiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		mCallback = callback;
	}

	/**
	 * Start running the IPAddressMonitor.
	 *
	 */
	public void start() {
		if (mRunning)
			return;
		mHanlder.post(mRunnable);
		mRunning = true;
	}

	/**
	 * Stop running the IPAddressMonitor.
	 *
	 */
	public void stop() {
		mRunning = false;
		mHanlder.removeCallbacks(mRunnable);
	}

	/**
	 * Get IP address form the WiFiManager, compare with the last IP address, if
	 * they are different, invoke the callback.
	 *
	 */
	protected void monitorIPAddress() {
		String IPAddress = null;
		WifiInfo inf = mWiFiManager.getConnectionInfo();
		if (inf != null && inf.getIpAddress() != 0) {
			int iaddr = inf.getIpAddress();
			IPAddress =
				((iaddr >>  0) & 0xff) + "." + ((iaddr >>  8) & 0xff) + "." +
				((iaddr >> 16) & 0xff) + "." + ((iaddr >> 24) & 0xff)
				;
		}
		if (mLastIPAddress == IPAddress)
			return;
		if (mLastIPAddress != null && mLastIPAddress.equals(IPAddress))
			return;
		mCallback.onIPAddressChanged(IPAddress);
		mLastIPAddress = IPAddress;
	}

}
