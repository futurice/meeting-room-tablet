package com.futurice.android.reservator.view;

import java.util.Calendar;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.RoomInfo;
import com.futurice.android.reservator.model.Reservation;
import com.futurice.android.reservator.model.Room;
import com.futurice.android.reservator.model.fum3.FumAddressBookAdapter;

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
import android.view.inputmethod.InputMethodManager;

public class RoomReservationView extends FrameLayout implements
		OnClickListener, OnItemClickListener {

	View cancelButton, bookNowButton, reserveButton, calendarButton,
			bookingMode, normalMode;
	AutoCompleteTextView nameField;
	CustomTimeSpanPicker timePicker;
	private Calendar maxTime, minTime;
	
	
	private Room room;

	public RoomReservationView(Context context) {
		this(context, null);
	}
	
	private OnFocusChangeListener userNameFocusChangeListener = new OnFocusChangeListener() {

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus) {
				reserveButton.setEnabled(false);
			}

		}
	};

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
		nameField = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView1);
		nameField.setOnItemClickListener(this);
		nameField.setAdapter(new FumAddressBookAdapter(context));
		nameField.setOnFocusChangeListener(userNameFocusChangeListener);
		timePicker = (CustomTimeSpanPicker) findViewById(R.id.timeSpanPicker1);
	}

	public void setRoom(Room room) {
		this.room = room;
		refreshData();
	}

	public Room getRoom() {
		return room;
	}

	@Override
	public void onClick(View v) {

		if (v == bookNowButton) {
			setReserveMode();
		}
		if (v == cancelButton) {
			setNormalMode();
		}
		if (v == reserveButton) {
			makeReservation();

		}
		if (v == calendarButton) {
			showRoomInCalendar();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		reserveButton.setEnabled(true);
		nameField.setSelected(false);
		InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(nameField.getRootView().getWindowToken(), 0);
		// Toast t = Toast.makeText(context, text, duration)
	}

	private void makeReservation() {
		Toast t = Toast.makeText(getContext(),
				DateFormat.format("kk:mm", timePicker.getStartTime()) + "-"
						+ DateFormat.format("kk:mm", timePicker.getEndTime()),
				Toast.LENGTH_LONG);
		t.show();
	}

	protected void setNormalMode() {
		bookingMode.setVisibility(View.GONE);
		normalMode.setVisibility(View.VISIBLE);
	}

	protected void setReserveMode() {
		refreshData();
		reserveButton.setEnabled(false);
		bookingMode.setVisibility(View.VISIBLE);
		normalMode.setVisibility(View.GONE);
	}

	private void refreshData() {
		if(maxTime == null || minTime == null){
			Reservation nextFreeTime = room.getNextFreeTime();
			timePicker.setMinTime(nextFreeTime.getBeginTime());
			timePicker.setMaxTime(nextFreeTime.getEndTime());
		}else{
			timePicker.setMinTime(minTime);
			timePicker.setMaxTime(maxTime);
		}
	}
	public void setMinTime(Calendar time){
		this.minTime = time;
	}
	public void setMaxTime(Calendar time){
		this.maxTime = time;
	}

	private void showRoomInCalendar() {
		Intent i = new Intent(getContext(), RoomInfo.class);
		i.putExtra(RoomInfo.ROOM_EMAIL_EXTRA, room.getEmail());
		getContext().startActivity(i);
	}
}
