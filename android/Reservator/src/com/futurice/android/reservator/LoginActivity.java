package com.futurice.android.reservator;

import java.util.Vector;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.futurice.android.reservator.model.DataProxy;
import com.futurice.android.reservator.model.DataUpdatedListener;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.Room;

public class LoginActivity extends ReservatorActivity implements OnClickListener, 
	OnMenuItemClickListener, DataUpdatedListener {
	
	MenuItem settingsMenu;
	private String username;
	private String password;
	private ProgressDialog pd;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_activity);
		((Button) findViewById(R.id.loginButton)).setOnClickListener(this);
		
		SharedPreferences preferences = getSharedPreferences(
				this.getString(R.string.PREFERENCES_NAME), Context.MODE_PRIVATE);
		
		if (preferences.contains("username")
				&& preferences.contains("password")) {
				login(preferences.getString(getString(R.string.PREFERENCES_USERNAME), null),
						preferences.getString(getString(R.string.PREFERENCES_PASSWORD), null));
			// do nothing, activity is changed after a successful login
		} else {
			pd.dismiss();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		DataProxy dataProxy = this.getResApplication().getDataProxy();
		dataProxy.addDataUpdatedListener(this);
	}
	
	public void onPause() {
		super.onPause();
		DataProxy dataProxy = this.getResApplication().getDataProxy();
		dataProxy.removeDataUpdatedListener(this);
	}
	
	@Override
	public void onClick(View v) {
		v.setEnabled(false);
		TextView currentPassword = (TextView) findViewById(R.id.password);
		TextView currentUsername = (TextView) findViewById(R.id.username);
		login(currentUsername.getText().toString(), currentPassword.getText().toString());
		v.setEnabled(true);
	}
	
	private void login(String username, String password) {
		pd = ProgressDialog.show(this, "Loading", "Logging in with the saved credentials", true, true);

		this.username = username;
		this.password = password;
		DataProxy dataProxy = this.getResApplication().getDataProxy();
		dataProxy.setCredentials(username, password);
		dataProxy.refreshRooms(); // checks the credentials with room query
	}
	
/*
	private boolean login(String username, String password) {
		DataProxy dataProxy = this.getResApplication().getDataProxy();
		try {
			dataProxy.setCredentials(username, password);
			dataProxy.getRooms(); // checks the credentials with room query
			SharedPreferences preferences = getSharedPreferences(
					this.getString(R.string.PREFERENCES_NAME), 0);
			Editor editor = preferences.edit();
			editor.putString("username", username);
			editor.putString("password", password);
			editor.commit();

			Intent i = new Intent(this, LobbyActivity.class);
			startActivityForResult(i, 0);
			return true;
		} catch (ReservatorException ex) {
			Toast err = Toast.makeText(this, ex.getMessage(),
					Toast.LENGTH_LONG);
			err.show();
			return false;
		}

	}*/

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// One cannot do anything from the settings menu without logging in first => disabling the menu for now...
/*		settingsMenu = menu.add("Settings").setOnMenuItemClickListener(this);
		settingsMenu.setIcon(android.R.drawable.ic_menu_preferences);*/

		return true;
	}


	@Override
	public boolean onMenuItemClick(MenuItem item) {
		if (item == settingsMenu) {
			Intent i = new Intent(this, SettingsActivity.class);
			startActivity(i);
		}
		return true;
	}

	@Override
	public void roomListUpdated(Vector<Room> rooms) {
		SharedPreferences preferences = getSharedPreferences(
				this.getString(R.string.PREFERENCES_NAME), 0);
		if (username == null || password == null) {
			refreshFailed(new ReservatorException("Failed to find current username or password for login"));
		} else {
			Editor editor = preferences.edit();
			editor.putString("username", username);
			editor.putString("password", password);
			editor.commit();
			pd.dismiss();
			
			DataProxy dataProxy = this.getResApplication().getDataProxy();
			dataProxy.removeDataUpdatedListener(this);

			Intent i = new Intent(this, LobbyActivity.class);
			startActivityForResult(i, 0);
		}
	}

	@Override
	public void roomReservationsUpdated(Room room) {
		// No operation
	}

	@Override
	public void refreshFailed(ReservatorException ex) {
		pd.dismiss();
		Toast.makeText(this, ex.getMessage(),
				Toast.LENGTH_LONG).show();
		setContentView(R.layout.login_activity);
		((Button) findViewById(R.id.loginButton)).setOnClickListener(this);
	}

}
