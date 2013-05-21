package com.futurice.android.reservator;

import android.app.AlertDialog;
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
import android.content.DialogInterface;

import com.futurice.android.reservator.model.AddressBook;
import com.futurice.android.reservator.model.AddressBookUpdatedListener;
import com.futurice.android.reservator.model.ReservatorException;

public class LoginActivity extends ReservatorActivity implements OnClickListener, 
	AddressBookUpdatedListener {
	
	MenuItem settingsMenu;
	private String fumUsername;
	private String fumPassword;
	
	private ProgressDialog pd;
	private boolean addressBookOk = false;
	private boolean roomListOk = false;
	private SharedPreferences preferences;
	private Editor editor;
	
	static final int REQUEST_LOBBY = 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_activity);
		
		if (pd != null) {
			pd.dismiss();
		}
		
		preferences = getSharedPreferences(this.getString(R.string.PREFERENCES_NAME), Context.MODE_PRIVATE);
		editor = preferences.edit();
		
		// Check Google Calendar
		if (getResApplication().getDataProxy().hasFatalError()) {
			showFatalErrorDialog(
					getString(R.string.calendarError), 
					getString(R.string.noCalendarsError));
			return;
		} else {
			roomListOk = true;
		}
		
		// Login to FUM
		((Button) findViewById(R.id.loginButton)).setOnClickListener(this);
		
		if (preferences.contains(getString(R.string.PREFERENCES_FUM_USERNAME)) && 
				preferences.contains(getString(R.string.PREFERENCES_FUM_PASSWORD))) {
				loginFum(preferences.getString(getString(R.string.PREFERENCES_FUM_USERNAME), null),
						preferences.getString(getString(R.string.PREFERENCES_FUM_PASSWORD), null));
			// do nothing, activity is changed after a successful login
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		
		AddressBook ab = this.getResApplication().getAddressBook();
		ab.addDataUpdatedListener(this);
		checkAndGo();
	}
	
	public void onPause() {
		super.onPause();
		
		AddressBook ab = this.getResApplication().getAddressBook();
		ab.removeDataUpdatedListener(this);
	}
	
	@Override
	public void onClick(View v) {
		v.setEnabled(false);
		loginFum(((TextView)findViewById(R.id.fumUsername)).getText().toString(),
				((TextView)findViewById(R.id.fumPassword)).getText().toString());
		v.setEnabled(true);
	}
	
	private void loginFum(String fumUsername, String fumPassword) {
		((TextView) findViewById(R.id.fumUsername)).setText(fumUsername);
		pd = ProgressDialog.show(this, "Logging in...", null, true, true);
		updateProgressDialogMessage();
		
		this.fumUsername = fumUsername;
		this.fumPassword = fumPassword;
		
		AddressBook fumAb = this.getResApplication().getFumAddressBook();
		fumAb.setCredentials(fumUsername, fumPassword);
		
		AddressBook ab = this.getResApplication().getAddressBook();
		ab.refetchEntries();
	}
	
	private void updateProgressDialogMessage() {
		if (pd == null)
			return;
		
		String s = "";
		
		if (roomListOk)
			s += "Google Calendar ok\n";
		else
			s += "Google Calendar pending...\n";
		
		if (addressBookOk)
			s += "FUM login ok\n";
		else
			s += "FUM login pending...\n";
		
		pd.setMessage(s);
	}
	
	private void checkAndGo() {
		if (addressBookOk && roomListOk) {
			// FUM
			editor.putString(getString(R.string.PREFERENCES_FUM_USERNAME), fumUsername);
			editor.putString(getString(R.string.PREFERENCES_FUM_PASSWORD), fumPassword);
			
			editor.apply();
			if (pd != null)
				pd.dismiss();
			
			Intent i = new Intent(this, LobbyActivity.class);
			startActivityForResult(i, REQUEST_LOBBY);
		}
	}

	@Override
	public void addressBookUpdated() {
		addressBookOk = true;
		updateProgressDialogMessage();
		checkAndGo();
	}

	@Override
	public void addressBookUpdateFailed(ReservatorException e) {
		addressBookOk = false;
		
		if (pd != null)
			pd.dismiss();
		Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
		setContentView(R.layout.login_activity);
		((Button) findViewById(R.id.loginButton)).setOnClickListener(this);
	}
	
	public void showFatalErrorDialog(String title, String errorMsg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setMessage(errorMsg)
			.setTitle(title)
			.setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					LoginActivity.this.finish();
				}
			});
		
		builder.create().show();
	}
	
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		finish();
	}
}