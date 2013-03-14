package com.futurice.android.reservator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.futurice.android.reservator.model.DataProxy;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.view.SettingsRoomRowAdapter;

public class SettingsActivity extends ReservatorActivity {
	EditText serverAddressView;
	Spinner roomNameView;
	DataProxy proxy;

	SharedPreferences settings;
	HashSet<String> unselectedRooms;
	ArrayList<String> roomNames;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_activity);
	    proxy = getResApplication().getDataProxy();
	    
	    try {
	    	roomNames = proxy.getRoomNames();
	    	roomNames.add(getString(R.string.lobbyRoomName));
	    } catch (ReservatorException e) {
			Toast err = Toast.makeText(getResApplication(), e.getMessage(),
					Toast.LENGTH_LONG);
			err.show();
	    }
	}
	
	@Override
	public void onResume(){
		super.onResume();
		settings = getSharedPreferences(getString(R.string.PREFERENCES_NAME), Context.MODE_PRIVATE);
		unselectedRooms = new HashSet<String>(settings.getStringSet(getString(R.string.PREFERENCES_UNSELECTED_ROOMS), new HashSet<String>()));
		
	    ListView l = (ListView) findViewById(R.id.roomListView);
	    SettingsRoomRowAdapter roomListAdapter = new SettingsRoomRowAdapter(this, R.layout.settings_select_room_row, roomNames);
	    l.setAdapter(roomListAdapter);
		
	    // Set back the recorded settings
		serverAddressView = (EditText) findViewById(R.id.serverAddressEdit);
		serverAddressView.setText(settings.getString(getString(R.string.PREFERENCES_SERVER_ADDRESS), "mail.futurice.com"));
		roomNameView = (Spinner) findViewById(R.id.roomNameSpinner);
		String roomName = settings.getString(getString(R.string.PREFERENCES_ROOM_NAME), "");

		refreshRoomNamesSpinner();
		
		@SuppressWarnings("unchecked")
		ArrayAdapter<String> adapter = (ArrayAdapter<String>) roomNameView.getAdapter();
		int spinnerPosition = 0;
		if (adapter != null){
			spinnerPosition = adapter.getPosition(roomName);
		}
		roomNameView.setSelection(spinnerPosition);
		
		// Setup button for removing log
		findViewById(R.id.removeUserDataButton).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// credentials
				Editor editor = settings.edit();
				Map<String, ?> keys = settings.getAll();
				for (Map.Entry<String, ?> entry : keys.entrySet()) {
					editor.remove(entry.getKey());
				}
				editor.apply();
				Toast.makeText(getApplicationContext(), "Removed credentials and reseted settings", Toast.LENGTH_SHORT).show();
				finish();
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
		super.onPause();
		// TODO: save button?
		// Save the settings
		String serverAddress = serverAddressView.getText().toString().trim();
		Object selectedRoomName = roomNameView.getSelectedItem();
		String roomName = "";
		if (selectedRoomName != null){
			roomName = selectedRoomName.toString().trim();
		}
		Editor editor = settings.edit();
		editor.putString(getString(R.string.PREFERENCES_SERVER_ADDRESS), serverAddress);
		editor.putString(getString(R.string.PREFERENCES_ROOM_NAME), roomName);
		
		editor.apply();

		// Update proxy
		proxy.setServer(serverAddress);
		Toast.makeText(getApplicationContext(), "Settings saved", Toast.LENGTH_SHORT).show();
	}

	private void refreshRoomNamesSpinner() {
		// Populates the select box with list of rooms
	    Spinner spinner = (Spinner) findViewById(R.id.roomNameSpinner);
		
	    String selected = null;
	    if (roomNameView.getSelectedItem() != null) {
	    	selected = (String) roomNameView.getSelectedItem();
	    }
	    
	    ArrayAdapter<String> adapter;

	    ArrayList<String> selectedRooms = new ArrayList<String>(roomNames);
	    selectedRooms.removeAll(unselectedRooms);
	    
	    adapter = new ArrayAdapter<String>(
	            this, android.R.layout.simple_spinner_item, selectedRooms);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spinner.setAdapter(adapter);
		if (selected != null && selectedRooms.contains(selected)) {
			spinner.setSelection(selectedRooms.indexOf(selected));
		}
	}
	
	public void roomRowClicked(final View view) {
		if (view instanceof CheckBox) {
			Editor editor = settings.edit();

			CheckBox c = (CheckBox)view;
			// checked = "not unselected". sorry!
			if (c.isChecked()) {
				unselectedRooms.remove(c.getText().toString());
			} else {
				unselectedRooms.add(c.getText().toString());
			}

			// Create a new HashSet, because...
			// http://stackoverflow.com/questions/14034803/misbehavior-when-trying-to-store-a-string-set-using-sharedpreferences
			editor.putStringSet(getString(R.string.PREFERENCES_UNSELECTED_ROOMS), new HashSet<String>(unselectedRooms));
			editor.commit();
			
			refreshRoomNamesSpinner();
		}
	}
}
