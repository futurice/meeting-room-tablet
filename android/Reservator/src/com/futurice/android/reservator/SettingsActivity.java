package com.futurice.android.reservator;

import java.util.ArrayList;
import java.util.List;

import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.Room;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class SettingsActivity extends Activity implements OnItemSelectedListener{
	Editor editor;
	Spinner roomSelector;
	List<Room> rooms = new ArrayList<Room>();
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
	}
	
	@Override
	public void onPostResume(){
		super.onPostResume();
		ReservatorApplication app = (ReservatorApplication)getApplication();
		try {
			rooms = app.getDataProxy().getRooms();
		} catch (ReservatorException e) {
			
		}
		roomSelector = (Spinner)findViewById(R.id.defaultRoomSpinner); 
		ArrayAdapter<Room> roomAdapter = new ArrayAdapter<Room>(this, android.R.layout.simple_dropdown_item_1line, rooms);
		roomSelector.setAdapter(roomAdapter);
		
		
		SharedPreferences settings = getSharedPreferences(getString(R.string.PREFERENCES_NAME), 0);
		if(settings.contains(getString(R.string.PREFERENCES_SHOW_ROOM))){
			String showRoom = settings.getString(getString(R.string.PREFERENCES_SHOW_ROOM), "");
			for(Room r : rooms){
			if(r.getEmail().equals(showRoom))
				roomSelector.setSelection(rooms.indexOf(r));
		}
		}
		editor = settings.edit();
		roomSelector.setOnItemSelectedListener(this);
	}
	
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		Room selectedRoom = (Room)parent.getItemAtPosition(position);
		editor.putString(getString(R.string.PREFERENCES_SHOW_ROOM), selectedRoom.getEmail());
	}
	@Override
	public void onPause(){
		editor.commit();
		super.onPause();
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		
	}
	
}
