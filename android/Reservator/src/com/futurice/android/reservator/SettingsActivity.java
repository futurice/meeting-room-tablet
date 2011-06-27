package com.futurice.android.reservator;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.widget.EditText;

import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.Room;

public class SettingsActivity extends Activity {
	Editor editor;
	EditText serverAddressView;
	List<Room> rooms = new ArrayList<Room>();
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
	}

	@Override
	public void onResume(){
		super.onPostResume();
		ReservatorApplication app = (ReservatorApplication)getApplication();
		try {
			rooms = app.getDataProxy().getRooms();
		} catch (ReservatorException e) {

		}

		serverAddressView = (EditText) findViewById(R.id.serverAddressEdit);

		SharedPreferences settings = getSharedPreferences(getString(R.string.PREFERENCES_NAME), 0);
		serverAddressView.setText(settings.getString(getString(R.string.PREFERENCES_SERVER_ADDRESS), "10.4.2.214"));
	}

	@Override
	public void onPause(){
		// TODO: save button?
		SharedPreferences settings = getSharedPreferences(getString(R.string.PREFERENCES_NAME), 0);

		String serverAddress = serverAddressView.getText().toString().trim();

		editor = settings.edit();
		editor.putString(getString(R.string.PREFERENCES_SERVER_ADDRESS), serverAddress);
		editor.commit();

		// update proxy
		((ReservatorApplication)this.getApplication()).getDataProxy().setServer(serverAddress);

		super.onPause();
	}

}
