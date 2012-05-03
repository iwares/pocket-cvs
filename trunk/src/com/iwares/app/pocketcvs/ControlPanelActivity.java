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
 * @file	src/com/iwares/app/pocketcvs/ControlPanelActivity.java
 * @author	Eric.Tsai
 *
 */

package com.iwares.app.pocketcvs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Control panel of the Pocket CVS application.
 *
 * @author Eric.Tsai
 *
 */
public class ControlPanelActivity extends Activity implements CVSAgent.Callback, IPAddressMonitor.Callback,
	View.OnClickListener, DialogInterface.OnClickListener {

	/** CVS agent used to communicate with the CVS service. */
	private CVSAgent mCVSAgent;

	/** IP address monitor to monitor WiFi IP address. */
	private IPAddressMonitor mIPAddressMonitor;

	/** Indicate whether allow the activity to be finished currently. */
	private boolean mAllowExit = false;

	/** Indicate whether the CVS daemon is running. */
	private boolean mIsDaemonRunning = false;

	/** Status image widget. */
	private ImageView mStatusImage;

	/** Status text widget. */
	private TextView mStatusText;

	/** Status description text widget. */
	private TextView mDescriptionText;

	/** Start/Stop button widget. */
	private Button mStartStopButton;

	/** Progress widget. */
	private ProgressBar mProgressRing;

	/** Current WiFi IP address */
	private String mIPAddress = null;

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startService(CVSService.INTENT);
		bindService(CVSService.INTENT, mCVSAgent = new CVSAgent(this), 0);
		mIPAddressMonitor = new IPAddressMonitor(this, this);
		setContentView(R.layout.acvs_control_panel);
		mStatusImage = (ImageView)findViewById(R.id.StatusImage);
		mStatusText = (TextView)findViewById(R.id.StatusText);
		mDescriptionText = (TextView)findViewById(R.id.DescriptionText);
		mStartStopButton = (Button)findViewById(R.id.StartStopButton);
		mProgressRing = (ProgressBar)findViewById(R.id.ProgressRing);
		mStartStopButton.setOnClickListener(this);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mIPAddressMonitor.start();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		mIPAddressMonitor.stop();
		super.onPause();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	public void onDestroy() {
		unbindService(mCVSAgent);
		if (!mIsDaemonRunning)
			stopService(CVSService.INTENT);
		super.onDestroy();
	}

	/**
	 * Called when user decide to finish this activity.
	 */
	public void onUserFinishActivity() {
		if (!mAllowExit)
			return;
		finish();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onKeyUp(int, android.view.KeyEvent)
	 */
	public boolean onKeyUp(int keyCode, KeyEvent evt) {
		if (keyCode != KeyEvent.KEYCODE_BACK)
			return false;
		onUserFinishActivity();
		return true;
	}

	public void updateUIStatus() {
		if (mIsDaemonRunning) {
			String description = getString(R.string.description_running);
			if (mIPAddress == null)
				description += getString(R.string.no_network_available);
			else
				description += mIPAddress + ":" + "/sdcard/.cvsrepo";
			mStatusImage.setBackgroundResource(R.drawable.service_is_running);
			mStatusText.setText(R.string.status_running);
			mDescriptionText.setText(description);
			mStartStopButton.setText(R.string.button_stop);
		} else {
			mStatusImage.setBackgroundResource(R.drawable.service_is_stopped);
			mStatusText.setText(R.string.status_stopped);
			mDescriptionText.setText(R.string.description_stopped);
			mStartStopButton.setText(R.string.button_start);
		}
	}

	/* (non-Javadoc)
	 * @see com.iwares.app.pocketcvs.CVSAgent.Callback#onDaemonStatusChanged(int)
	 */
	public void onDaemonStatusChanged(int newStatus) {
		switch (newStatus) {
		case CVSAgent.STATUS_STOPPED:
			mIsDaemonRunning = false;
			break;
		case CVSAgent.STATUS_RUNNING:
			mIsDaemonRunning = true;
			break;
		default:
			break;
		}
		updateUIStatus();
		mStartStopButton.setVisibility(View.VISIBLE);
		mProgressRing.setVisibility(View.GONE);
		mAllowExit = true;
	}

	/* (non-Javadoc)
	 * @see com.iwares.app.pocketcvs.CVSAgent.Callback#onUserPasswordChanged(boolean)
	 */
	public void onUserPasswordChanged(final boolean changed) {
		Log.d("ControlPanelActivity", "onUserPasswordChanged(" + changed + ")");
		int msgID = changed ? R.string.password_changed : R.string.password_not_changed;
		Toast.makeText(this, msgID, Toast.LENGTH_SHORT).show();
	}

	/* (non-Javadoc)
	 * @see com.iwares.app.pocketcvs.IPAddressMonitor.Callback#onIPAddressChanged(java.lang.String)
	 */
	public void onIPAddressChanged(String ipAddress) {
		mIPAddress = ipAddress;
		if (!mAllowExit)
			return;
		updateUIStatus();
	}

	/* (non-Javadoc)
	 * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
	 */
	public void onClick(DialogInterface dialog, int which) {
		String username = ((EditText)((AlertDialog)dialog).findViewById(R.id.UserName))
			.getText().toString();
		String password = ((EditText)((AlertDialog)dialog).findViewById(R.id.Password))
			.getText().toString();
		// TODO: Check validation of user name and password.
		mCVSAgent.requstSetUserPassword(username, password);
	}

	/**
	 * Called when user press the start/stop button or menu item.
	 */
	public void onUserStartStopDaemon() {
		mStartStopButton.setVisibility(View.GONE);
		mProgressRing.setVisibility(View.VISIBLE);
		if (mIsDaemonRunning)
			mCVSAgent.requstStopDaemon();
		else
			mCVSAgent.requstStartDaemon();
		mAllowExit = false;
	}

	/**
	 * Called when user select the password menu item.
	 */
	public void onUserSetUserPassword() {
		new AlertDialog.Builder(this)
		.setTitle(R.string.password_dialog_title)
		.setView(View.inflate(this, R.layout.user_password_dialog, null))
		.setPositiveButton(R.string.button_confirm, this)
		.setNegativeButton(R.string.button_cancel, null)
		.show();
	}

	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.StartStopButton:
			onUserStartStopDaemon();
			break;
		default:
			break;
		}
	}

	/** Start/Stop menu item ID. */
	protected static final int MENU_STARTSTOP = 0x00000100;

	/** Password menu item ID. */
	protected static final int MENU_PASSWORD = 0x00000101;

	/** Quit menu item ID */
	protected static final int MENU_QUIT = 0x00000102;

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_STARTSTOP, 1, R.string.menu_start).setIcon(R.drawable.ic_menu_start);
		menu.add(0, MENU_PASSWORD, 1, R.string.menu_password).setIcon(R.drawable.ic_menu_password);
		menu.add(0, MENU_QUIT, 1, R.string.menu_quit).setIcon(R.drawable.ic_menu_quit);
		return true;
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (!mAllowExit) {
			menu.getItem(0).setEnabled(false);
			menu.getItem(2).setEnabled(false);
		} else {
			menu.getItem(0).setEnabled(true);
			menu.getItem(2).setEnabled(true);
		}
		if (mIsDaemonRunning) {
			menu.getItem(0).setTitle(R.string.menu_stop);
			menu.getItem(0).setIcon(R.drawable.ic_menu_stop);
		} else {
			menu.getItem(0).setTitle(R.string.menu_start);
			menu.getItem(0).setIcon(R.drawable.ic_menu_start);
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_STARTSTOP:
			onUserStartStopDaemon();
			break;
		case MENU_PASSWORD:
			onUserSetUserPassword();
			break;
		case MENU_QUIT:
			onUserFinishActivity();
			break;
		default:
			return false;
		}
		return true;
	}

}
