package com.futurice.android.reservator.view;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.ReservatorApplication;
import com.futurice.android.reservator.RoomActivity;
import com.futurice.android.reservator.common.Helpers;
import com.futurice.android.reservator.model.AddressBookAdapter;
import com.futurice.android.reservator.model.DateTime;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.Room;
import com.futurice.android.reservator.model.TimeSpan;
import com.futurice.android.reservator.model.rooms.RoomsInfo;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;

public class LobbyReservationRowView extends FrameLayout implements
		OnClickListener, OnItemClickListener {

	View cancelButton, bookNowButton, reserveButton, calendarButton,
			bookingMode, normalMode, titleView;
	AutoCompleteTextView nameField;
	CustomTimeSpanPicker2 timePicker2;
	TextView roomNameView, roomInfoView, roomStatusView;
	ReservatorApplication application;

	Callback onReserveCallback = null;

	private Room room;

	public LobbyReservationRowView(Context context) {
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


	public LobbyReservationRowView(Context context, AttributeSet attrs) {
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
		timePicker2 = (CustomTimeSpanPicker2) findViewById(R.id.timeSpanPicker2);
		roomNameView = (TextView) findViewById(R.id.roomNameLabel);
		roomInfoView = (TextView) findViewById(R.id.roomInfoLabel);
		roomStatusView = (TextView) findViewById(R.id.roomStatusLabel);

		application = (ReservatorApplication) this.getContext().getApplicationContext();
		nameField.setOnFocusChangeListener(userNameFocusChangeListener);
		nameField.setOnItemClickListener(this);
		nameField.setOnClickListener(this);
		if(nameField.getAdapter() == null){
			nameField.setAdapter(new AddressBookAdapter(this.getContext(), application.getAddressBook()));
		}

	}

	public void setRoom(Room room) {
		this.room = room;

		// Room stuff
		RoomsInfo info = RoomsInfo.getRoomsInfo(room);
		roomNameView.setText(info.getRoomName());
		if (info.getRoomNumber() == 0) {
			roomInfoView.setText("for " + info.getRoomSize());
		} else {
			// U+2022 is a dot
			roomInfoView.setText(Integer.toString(info.getRoomNumber()) + " \u2022 for " + info.getRoomSize());
		}

		// Reservation stuff
		TimeSpan nextFreeTime = room.getNextFreeTime();

		timePicker2.reset();
		if (nextFreeTime != null) {
			timePicker2.setMinimumTime(nextFreeTime.getStart());
			timePicker2.setMaximumTime(nextFreeTime.getEnd());
		} else {
			timePicker2.setMinimumTime(new DateTime());
		}
		timePicker2.setEndTimeRelatively(60); // let book the room for an hour

		boolean bookable = false;
		if (room.isFree()) {
			int freeMinutes = room.minutesFreeFromNow();
			bookable = true;

			if (freeMinutes > 180) {
				roomStatusView.setText("Free");
			} else if (freeMinutes < 30) {
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

	public Room getRoom() {
		return room;
	}

	@Override
	public void onClick(View v) {

		if (v == bookNowButton) {
			setReserveMode();
		}
		else if (v == cancelButton) {
			setNormalMode();
		}
		else if (v == reserveButton) {
			reserveButton.setEnabled(false);
			makeReservation();
		}
		else if (v == calendarButton || v == titleView) {
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
		String email = application.getAddressBook().getEmailByName(nameField.getText().toString());

		try {
			if (email == null) {
				throw new ReservatorException("No such user, try again");
			}
			application.getDataProxy().reserve(room, timePicker2.getTimeSpan(), email);
		} catch (ReservatorException e) {
			Builder alertBuilder = new AlertDialog.Builder(getContext());
			alertBuilder.setTitle("Failed to put reservation")
				.setMessage(e.getMessage());
			alertBuilder.setOnCancelListener(new OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						if (onReserveCallback != null) {
							onReserveCallback.call(LobbyReservationRowView.this);
						}
					}
				});

		alertBuilder.show();

				return;
		}

		if (onReserveCallback != null) {
			onReserveCallback.call(this);
		}

	}

	protected void setNormalMode() {
		this.setBackgroundColor(getResources().getColor(R.color.Transparent));
		bookingMode.setVisibility(View.GONE);
		normalMode.setVisibility(View.VISIBLE);
	}

	protected void setReserveMode() {
		this.setBackgroundColor(getResources().getColor(R.color.ReserveBackground));
		reserveButton.setEnabled(false);
		bookingMode.setVisibility(View.VISIBLE);
		normalMode.setVisibility(View.GONE);
	}

	public void resetTimeSpan() {
		timePicker2.reset();
	}

	public void setMinTime(DateTime time){
		timePicker2.setMinimumTime(time);
	}

	public void setMaxTime(DateTime time){
		timePicker2.setMaximumTime(time);
	}

	public void setEndTimeRelatively(int minutes) {
		timePicker2.setEndTimeRelatively(minutes);
	}

	public void setOnReserveCallback(Callback onReserveCallback) {
		this.onReserveCallback = onReserveCallback;
	}

	private void showRoomInCalendar() {
		Intent i = new Intent(getContext(), RoomActivity.class);
		i.putExtra(RoomActivity.ROOM_EXTRA, room);
		getContext().startActivity(i);
	}
}
