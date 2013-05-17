package com.futurice.android.reservator;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.content.DialogInterface;

public class LoginActivity extends ReservatorActivity {
	static final int REQUEST_LOBBY = 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_activity);
		
		// Check Google Calendar
		if (getResApplication().getDataProxy().hasFatalError()) {
			showFatalErrorDialog(
					getString(R.string.calendarError), 
					getString(R.string.noCalendarsError));
			return;
		} else {
			Intent i = new Intent(this, LobbyActivity.class);
			startActivityForResult(i, REQUEST_LOBBY);
		}
	}
	
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		finish();
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
}