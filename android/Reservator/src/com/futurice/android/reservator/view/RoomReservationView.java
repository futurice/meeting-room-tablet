package com.futurice.android.reservator.view;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.RoomInfo;
import com.futurice.android.reservator.model.Reservation;
import com.futurice.android.reservator.model.Room;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.view.View.OnClickListener;

public class RoomReservationView extends FrameLayout implements OnClickListener, OnItemClickListener{

	View cancelButton, bookNowButton, reserveButton, calendarButton, bookingMode, normalMode;
	AutoCompleteTextView nameField;
	CustomTimeSpanPicker timePicker;
	
	private Room room;
	
	public RoomReservationView(Context context){
		this(context, null);
	}
	public RoomReservationView(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.lobby_reservation_row, this);
		cancelButton = findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(this);
		bookNowButton = findViewById(R.id.bookNowButton);
		bookNowButton.setOnClickListener(this);
		reserveButton = findViewById(R.id.reserveButton);
		reserveButton.setOnClickListener(this);
		calendarButton = findViewById(R.id.calendarButton);
		calendarButton.setOnClickListener(this);
		bookingMode = findViewById(R.id.bookingMode);
		normalMode = findViewById(R.id.normalMode);
		nameField = (AutoCompleteTextView)findViewById(R.id.autoCompleteTextView1);
		nameField.setOnItemClickListener(this);
		timePicker = (CustomTimeSpanPicker)findViewById(R.id.timeSpanPicker1);
	}
	
	public void setRoom(Room room){
		this.room = room;
		refreshData();
	}
	public Room getRoom(){
		return room;
	}
	
	@Override
	public void onClick(View v) {

		if(v == bookNowButton){
			setReserveMode();
		}
		if(v == cancelButton){
			setNormalMode();
		}
		if(v == reserveButton){
			makeReservation();	
			
		}if(v == calendarButton){
			showRoomInCalendar();
		}
	}
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		reserveButton.setEnabled(true);
		//Toast t = Toast.makeText(context, text, duration)
	}
	private void makeReservation(){
		Toast t = Toast.makeText(getContext(), DateFormat.format("kk:mm", timePicker.getStartTime()) + "-" + DateFormat.format("kk:mm",timePicker.getEndTime()), Toast.LENGTH_LONG);
		t.show();
	}
	private void setNormalMode(){
		bookingMode.setVisibility(View.GONE);
		normalMode.setVisibility(View.VISIBLE);
	}
	private void setReserveMode(){
		refreshData();
		reserveButton.setEnabled(false);
		bookingMode.setVisibility(View.VISIBLE);
		normalMode.setVisibility(View.GONE);
	}
	private void refreshData(){
		Reservation nextFreeTime = room.getNextFreeTime();
		timePicker.setMinTime(nextFreeTime.getBeginTime());
		timePicker.setMaxTime(nextFreeTime.getEndTime());
	}
	private void showRoomInCalendar(){
		Intent i = new Intent(getContext(), RoomInfo.class);
		i.putExtra(RoomInfo.ROOM_EMAIL_EXTRA, room.getEmail());
		getContext().startActivity(i);
	}
}
