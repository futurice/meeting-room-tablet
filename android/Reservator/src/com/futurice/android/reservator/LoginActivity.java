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

import com.futurice.android.reservator.model.AddressBook;
import com.futurice.android.reservator.model.AddressBookUpdatedListener;
import com.futurice.android.reservator.model.DataProxy;
import com.futurice.android.reservator.model.DataUpdatedListener;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.Room;

public class LoginActivity extends ReservatorActivity implements OnClickListener, 
	DataUpdatedListener, AddressBookUpdatedListener {
	
	MenuItem settingsMenu;
	private String username;
	private String password;
	private String fumUsername;
	private String fumPassword;
	
	private ProgressDialog pd;
	private boolean roomListOk = false;
	private boolean addressBookOk = false;
	private SharedPreferences preferences;
	private Editor editor;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_activity);
		((Button) findViewById(R.id.loginButton)).setOnClickListener(this);
		
		preferences = getSharedPreferences(this.getString(R.string.PREFERENCES_NAME), Context.MODE_PRIVATE);
		editor = preferences.edit();
		
		if (preferences.contains(getString(R.string.PREFERENCES_USERNAME))
				&& preferences.contains(getString(R.string.PREFERENCES_PASSWORD))
				&& preferences.contains(getString(R.string.PREFERENCES_FUM_USERNAME))
				&& preferences.contains(getString(R.string.PREFERENCES_FUM_PASSWORD))) {
				login(preferences.getString(getString(R.string.PREFERENCES_USERNAME), null),
						preferences.getString(getString(R.string.PREFERENCES_PASSWORD), null),
						preferences.getString(getString(R.string.PREFERENCES_FUM_USERNAME), null),
						preferences.getString(getString(R.string.PREFERENCES_FUM_PASSWORD), null)
						);
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
		AddressBook ab = this.getResApplication().getAddressBook();
		ab.addDataUpdatedListener(this);
	}
	
	public void onPause() {
		super.onPause();
		DataProxy dataProxy = this.getResApplication().getDataProxy();
		dataProxy.removeDataUpdatedListener(this);
		AddressBook ab = this.getResApplication().getAddressBook();
		ab.removeDataUpdatedListener(this);
	}
	
	@Override
	public void onClick(View v) {
		v.setEnabled(false);
		TextView currentPassword = (TextView) findViewById(R.id.password);
		TextView currentUsername = (TextView) findViewById(R.id.username);
		login(currentUsername.getText().toString(), 
				currentPassword.getText().toString(),
				((TextView)findViewById(R.id.fumUsername)).getText().toString(),
				((TextView)findViewById(R.id.fumPassword)).getText().toString()
				);
		v.setEnabled(true);
	}
	
	private void login(String username, String password, String fumUsername, String fumPassword) {
		((TextView) findViewById(R.id.username)).setText(username);
		pd = ProgressDialog.show(this, "Logging in...", null, true, true);
		updateProgressDialogMessage();
		
		this.username = username;
		this.password = password;
		
		this.fumUsername = fumUsername;
		this.fumPassword = fumPassword;
		
		DataProxy dataProxy = this.getResApplication().getDataProxy();
		dataProxy.setCredentials(username, password);
		dataProxy.refreshRooms(); // checks the credentials with room query
		
		AddressBook ab = this.getResApplication().getAddressBook();
		ab.setCredentials(fumUsername, fumPassword);
		ab.prefetchEntries();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		finish();
	}

	@Override
	public void roomListUpdated(Vector<Room> rooms) {
		if (username == null || password == null) {
			refreshFailed(new ReservatorException("Failed to find current exchange username or password for login"));
		} else {
			roomListOk = true;
			updateProgressDialogMessage();
			checkAndGo();
		}
	}
	
	private void updateProgressDialogMessage() {
		if (pd == null)
			return;
		
		String s = "";
		if (roomListOk)
			s += "Exchange login ok\n";
		else
			s += "Exchange login pending...\n";
		
		if (addressBookOk)
			s += "FUM login ok\n";
		else
			s += "FUM login pending...\n";
		
		pd.setMessage(s);
	}
	
	private void checkAndGo() {
		if (roomListOk && addressBookOk) {
			editor.putString(getString(R.string.PREFERENCES_USERNAME), username);
			editor.putString(getString(R.string.PREFERENCES_PASSWORD), password);
			
			// FUM
			editor.putString(getString(R.string.PREFERENCES_FUM_USERNAME), fumUsername);
			editor.putString(getString(R.string.PREFERENCES_FUM_PASSWORD), fumPassword);
			
			editor.apply();
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
		Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
		roomListOk = addressBookOk = false;
		setContentView(R.layout.login_activity);
		((Button) findViewById(R.id.loginButton)).setOnClickListener(this);
	}

	@Override
	public void addressBookUpdated() {
		addressBookOk = true;
		updateProgressDialogMessage();
		checkAndGo();
	}

	@Override
	public void addressBookUpdateFailed(ReservatorException e) {
		refreshFailed(e);
	}

}
