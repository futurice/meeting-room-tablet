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
import android.widget.EditText;
import android.widget.Toast;

import com.futurice.android.reservator.model.Room;

public class SettingsActivity extends Activity {
	Editor editor;
	EditText serverAddressView;
	EditText roomNameView;
	List<Room> rooms = new ArrayList<Room>();
	SharedPreferences settings;
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_activity);
	}

	@Override
	public void onResume(){
		super.onResume();
		settings = getSharedPreferences(getString(R.string.PREFERENCES_NAME), 0);
		editor = settings.edit();

		serverAddressView = (EditText) findViewById(R.id.serverAddressEdit);
		serverAddressView.setText(settings.getString(getString(R.string.PREFERENCES_SERVER_ADDRESS), "mail.futurice.com"));
		roomNameView = (EditText) findViewById(R.id.roomNameEdit);
		roomNameView.setText(settings.getString(getString(R.string.PREFERENCES_ROOM_NAME), ""));		
		
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
				}else{
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
		String serverAddress = serverAddressView.getText().toString().trim();
		String roomName = roomNameView.getText().toString().trim();
		editor.putString(getString(R.string.PREFERENCES_SERVER_ADDRESS), serverAddress);
		editor.putString(getString(R.string.PREFERENCES_ROOM_NAME), roomName);
		editor.commit();

		// update proxy
		((ReservatorApplication)this.getApplication()).getDataProxy().setServer(serverAddress);

		super.onPause();
	}

}
