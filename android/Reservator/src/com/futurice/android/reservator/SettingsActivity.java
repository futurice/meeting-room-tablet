package com.futurice.android.reservator;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
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
		serverAddressView.setText(settings.getString(getString(R.string.PREFERENCES_SERVER_ADDRESS), "10.4.2.214"));
		
		findViewById(R.id.removeUserDataButton).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				editor.remove(getString(R.string.PREFERENCES_USERNAME))
					.remove(getString(R.string.PREFERENCES_PASSWORD));
				Toast.makeText(SettingsActivity.this, "Removed!", Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public void onPause(){
		// TODO: save button?
		String serverAddress = serverAddressView.getText().toString().trim();
		editor.putString(getString(R.string.PREFERENCES_SERVER_ADDRESS), serverAddress);
		editor.commit();

		// update proxy
		((ReservatorApplication)this.getApplication()).getDataProxy().setServer(serverAddress);

		super.onPause();
	}

}
