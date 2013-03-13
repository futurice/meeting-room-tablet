package com.futurice.android.reservator;

import java.util.Vector;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.MenuItem;
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
	DataUpdatedListener {
	
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
			if (pd != null)
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
		((TextView) findViewById(R.id.username)).setText(username);
		pd = ProgressDialog.show(this, "Logging in...", null, true, true);
		
		this.username = username;
		this.password = password;
		DataProxy dataProxy = this.getResApplication().getDataProxy();
		dataProxy.setCredentials(username, password);
		dataProxy.refreshRooms(); // checks the credentials with room query
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		finish();
	}

	@Override
	public void roomListUpdated(Vector<Room> rooms) {
		SharedPreferences preferences = getSharedPreferences(
				this.getString(R.string.PREFERENCES_NAME), 0);
		if (username == null || password == null) {
			refreshFailed(new ReservatorException("Failed to find current username or password for login"));
		} else {
			Editor editor = preferences.edit();
			editor.putString(getString(R.string.PREFERENCES_USERNAME), username);
			editor.putString(getString(R.string.PREFERENCES_PASSWORD), password);
			editor.commit();
			if (pd != null)
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
		if (pd != null)
			pd.dismiss();
		Toast.makeText(this, ex.getMessage(),
				Toast.LENGTH_LONG).show();
		setContentView(R.layout.login_activity);
		((Button) findViewById(R.id.loginButton)).setOnClickListener(this);
	}

}
