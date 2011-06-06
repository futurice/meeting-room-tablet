package com.futurice.android.reservator;


import java.util.List;

import com.futurice.android.reservator.model.Room;
import com.futurice.android.reservator.view.WeekView;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class RoomInfo extends Activity implements OnItemSelectedListener{
	/** Called when the activity is first created. */
	WeekView weekView;
	List<Room> rooms;
	ArrayAdapter<Room> roomAdapter;
	Spinner roomSelector;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.room_browse_view);
		this.weekView = (WeekView)findViewById(R.id.weekView1);
		rooms = ((ReservatorApplication)getApplication()).getDataProxy().getRooms();
		roomSelector = (Spinner)findViewById(R.id.spinner1); 
		roomAdapter = new ArrayAdapter<Room>(this, android.R.layout.simple_dropdown_item_1line, rooms);
		roomSelector.setAdapter(roomAdapter);
		roomSelector.setOnItemSelectedListener(this);
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
}