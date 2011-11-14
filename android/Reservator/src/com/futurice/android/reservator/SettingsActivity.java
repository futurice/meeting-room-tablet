package com.futurice.android.reservator;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.futurice.android.reservator.model.DataProxy;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.Room;

public class SettingsActivity extends Activity {
	Editor editor;
	EditText serverAddressView;
	Spinner roomNameView;
	ReservatorApplication app;
	DataProxy proxy;

	SharedPreferences settings;
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_activity);

	    app = ((ReservatorApplication) getApplication());
	    proxy = app.getDataProxy();
	    
		// Populates the select box with list of rooms
	    Spinner spinner = (Spinner) findViewById(R.id.roomNameSpinner);
	    ArrayAdapter<String> adapter;
	    String[] roomNames;
	    try {
	    	roomNames = proxy.getRoomNames();
		    adapter = new ArrayAdapter<String>(
		            this, android.R.layout.simple_spinner_item, roomNames);
	    } catch (ReservatorException e) {
			Toast err = Toast.makeText(app, e.getMessage(),
					Toast.LENGTH_LONG);
			err.show();
	    	return;
	    }
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spinner.setAdapter(adapter);
	}

	@Override
	public void onResume(){
		super.onResume();
		settings = getSharedPreferences(getString(R.string.PREFERENCES_NAME), 0);
		editor = settings.edit();

		// Set back the recorded settings
		serverAddressView = (EditText) findViewById(R.id.serverAddressEdit);
		serverAddressView.setText(settings.getString(getString(R.string.PREFERENCES_SERVER_ADDRESS), "mail.futurice.com"));
		roomNameView = (Spinner) findViewById(R.id.roomNameSpinner);
		String roomName = settings.getString(getString(R.string.PREFERENCES_ROOM_NAME), "");
		ArrayAdapter<String> adapter = (ArrayAdapter<String>) roomNameView.getAdapter();
		int spinnerPosition = adapter.getPosition(roomName);
		roomNameView.setSelection(spinnerPosition);
		
		// Setup button for removing log
		findViewById(R.id.removeUserDataButton).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				editor.remove(getString(R.string.PREFERENCES_USERNAME))
					.remove(getString(R.string.PREFERENCES_PASSWORD));
				Toast.makeText(SettingsActivity.this, "Removed!", Toast.LENGTH_SHORT).show();
			}
		});
		Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.doNotModify));
		builder.setMessage(getString(R.string.onlyItTeamShouldModify));
		builder.setCancelable(false);
		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(which == AlertDialog.BUTTON_NEGATIVE){
					dialog.dismiss();
					finish();
				} else {
					dialog.dismiss();
				}
				
			}
		}; 
		builder.setPositiveButton(getString(R.string.continueBtn), listener);
		builder.setNegativeButton(getString(R.string.goBackBtn), listener);
		builder.show();
	}

	@Override
	public void onPause(){
		// TODO: save button?
		// Save the settings
		String serverAddress = serverAddressView.getText().toString().trim();
		String roomName = roomNameView.getSelectedItem().toString().trim();
		editor.putString(getString(R.string.PREFERENCES_SERVER_ADDRESS), serverAddress);
		editor.putString(getString(R.string.PREFERENCES_ROOM_NAME), roomName);
		editor.commit();

		// Update proxy
		proxy.setServer(serverAddress);

		super.onPause();
	}

}
