package com.futurice.android.reservator;


import java.util.List;

import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.Room;
import com.futurice.android.reservator.view.WeekView;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;

public class RoomInfo extends Activity implements OnItemSelectedListener, OnMenuItemClickListener{
	/** Called when the activity is first created. */
	public static final String ROOM_EMAIL_EXTRA = "roomEmail";
	
	WeekView weekView;
	List<Room> rooms;
	ArrayAdapter<Room> roomAdapter;
	TextView roomNameLabel;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.room_browse_view);
		this.weekView = (WeekView)findViewById(R.id.weekView1);
		this.roomNameLabel = (TextView)findViewById(R.id.roomNameLabel);
	}
	@Override
	public void onResume(){
		super.onResume();
		try {
			rooms = ((ReservatorApplication)getApplication()).getDataProxy().getRooms();
			
			SharedPreferences settings = getSharedPreferences(getString(R.string.PREFERENCES_NAME), 0);
			String roomEmail = getIntent().getStringExtra(ROOM_EMAIL_EXTRA);
			if(roomEmail != null)
				roomEmail = settings.getString(getString(R.string.PREFERENCES_SHOW_ROOM), "");
			if(roomEmail != null){
				for(Room r : rooms){
					if(r.getEmail().equals(roomEmail)){
						setRoom(r);
						break;
					}
				}
			}
			else if(!rooms.isEmpty()){
				setRoom(rooms.get(0));
			}
			
		} catch (ReservatorException e) {
			// TODO: XXX
			Log.e("DataProxy", "getRooms", e);
		}
	}
	private void setRoom(Room r){
		roomNameLabel.setText(r.getName());
		weekView.setRoom(r);
	}
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		Room selectedRoom = (Room)parent.getItemAtPosition(position);
		weekView.setRoom(selectedRoom);
	}
	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    menu.add("Settings").setOnMenuItemClickListener(this);
		return true;
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		Intent i = new Intent(this, SettingsActivity.class);
		startActivity(i);
		return true;
	}
}