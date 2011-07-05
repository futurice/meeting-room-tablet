package com.futurice.android.reservator.view;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.model.Room;
import com.futurice.android.reservator.model.TimeSpan;

import android.app.Dialog;
import android.content.Context;
import android.view.View;

public class RoomReservationPopup extends Dialog {
	LobbyReservationRowView reservationView;
	CalendarMarker marker;

	public RoomReservationPopup(Context context, TimeSpan timeLimits, TimeSpan presetTime, Room room) {
		super(context, R.style.Theme_Transparent);
		setCancelable(true);

		setContentView(R.layout.reservation_popup);
		findViewById(R.id.relativeLayout1).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				cancel();
			}
		});
		reservationView = (LobbyReservationRowView)findViewById(R.id.roomReservationView1);
		reservationView.setClickable(true);
		reservationView.setRoom(room);
		reservationView.resetTimeSpan();
		reservationView.setMinTime(timeLimits.getStart());
		reservationView.setMaxTime(timeLimits.getEnd());
		reservationView.timePicker2.setStartTime(presetTime.getStart());
		reservationView.timePicker2.setEndTime(presetTime.getEnd());
		reservationView.setEndTimeRelatively(60);

		reservationView.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				cancel();
			}
		});

		reservationView.findViewById(R.id.reserveButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				reservationView.onClick(v);
				cancel();
			}
		});

		reservationView.setReserveMode();
	}

	public void setOnReserveCallback(Callback callback) {
		reservationView.setOnReserveCallback(callback);
	}
}
