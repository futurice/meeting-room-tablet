package com.futurice.android.reservator.view;

import java.util.Calendar;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.RoomInfo;
import com.futurice.android.reservator.common.Helpers;
import com.futurice.android.reservator.model.Reservation;
import com.futurice.android.reservator.model.Room;
import com.futurice.android.reservator.model.TimeSpan;
import com.futurice.android.reservator.model.fum3.FumAddressBookAdapter;
import com.futurice.android.reservator.model.rooms.RoomsInfo;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;

public class RoomReservationView extends FrameLayout implements
		OnClickListener, OnItemClickListener {

	View cancelButton, bookNowButton, reserveButton, calendarButton,
			bookingMode, normalMode, titleView;
	AutoCompleteTextView nameField;
	CustomTimeSpanPicker2 timePicker2;
	TextView roomNameView, roomInfoView, roomStatusView;

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
		titleView = findViewById(R.id.titleLayout);
		titleView.setOnClickListener(this);
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
		timePicker2 = (CustomTimeSpanPicker2) findViewById(R.id.timeSpanPicker2);
		roomNameView = (TextView) findViewById(R.id.roomNameLabel);
		roomInfoView = (TextView) findViewById(R.id.roomInfoLabel);
		roomStatusView = (TextView) findViewById(R.id.roomStatusLabel);
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
		if (v == calendarButton || v == titleView) {
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
				DateFormat.format("kk:mm", timePicker2.getStartTime()) + "-"
						+ DateFormat.format("kk:mm", timePicker2.getEndTime()),
				Toast.LENGTH_LONG);
		t.show();
	}

	protected void setNormalMode() {
		this.setBackgroundColor(getResources().getColor(R.color.Transparent));
		bookingMode.setVisibility(View.GONE);
		normalMode.setVisibility(View.VISIBLE);
	}

	protected void setReserveMode() {
		// refreshData(); // no need?
		this.setBackgroundColor(getResources().getColor(R.color.FutuLightGreen));
		reserveButton.setEnabled(false);
		bookingMode.setVisibility(View.VISIBLE);
		normalMode.setVisibility(View.GONE);
	}

	private void refreshData() {
		TimeSpan nextFreeTime = room.getNextFreeTime();

		timePicker2.reset();
		timePicker2.setMinimumTime(nextFreeTime.getStart());
		timePicker2.setMaximumTime(nextFreeTime.getEnd());
		timePicker2.setEndTimeRelatively(60); // let book the room for an hour

		RoomsInfo info = RoomsInfo.getRoomsInfo(room);
		roomNameView.setText(info.getRoomName());
		if (info.getRoomNumber() == 0) {
			roomInfoView.setText("for " + info.getRoomSize());
		} else {
			// U+2022 is a dot
			roomInfoView.setText(Integer.toString(info.getRoomNumber()) + " \u2022 for " + info.getRoomSize());
		}

		boolean bookable = false;
		if (room.isFree()) {
			int freeMinutes = room.minutesFreeFromNow();
			bookable = true;

			if (freeMinutes > 180) {
				roomStatusView.setText("Free");
			} else if (freeMinutes < 15) {
				roomStatusView.setText("Reserved");
				bookable = false;
			} else {
				roomStatusView.setText("Free for " + Helpers.humanizeTimeSpan(freeMinutes));
			}
		} else {
			roomStatusView.setText("Reserved");
		}

		if (bookable) {
			roomStatusView.setTextColor(getResources().getColor(R.color.StatusFreeColor));
			bookNowButton.setVisibility(View.VISIBLE);
		} else {
			roomStatusView.setTextColor(getResources().getColor(R.color.StatusReservedColor));
			bookNowButton.setVisibility(View.INVISIBLE);
		}
	}

	public void resetTimeSpan() {
		timePicker2.reset();
	}

	public void setMinTime(Calendar time){
		timePicker2.setMinimumTime(time);
	}

	public void setMaxTime(Calendar time){
		timePicker2.setMaximumTime(time);
	}

	public void setEndTimeRelatively(int minutes) {
		timePicker2.setEndTimeRelatively(minutes);
	}

	private void showRoomInCalendar() {
		Intent i = new Intent(getContext(), RoomInfo.class);
		i.putExtra(RoomInfo.ROOM_EMAIL_EXTRA, room.getEmail());
		getContext().startActivity(i);
	}
}
