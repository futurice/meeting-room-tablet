package com.futurice.android.reservator.view;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.model.Reservation;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.AutoCompleteTextView;

public class RoomReservationPopup extends Dialog{
	RoomReservationView reservationView;
	CalendarMarker marker;
	
	protected RoomReservationPopup(Context context, CalendarMarker marker) {
		super(context, R.style.Theme_Transparent);
		setCancelable(true);
		this.marker = marker;
		
		setContentView(R.layout.reservation_popup);
		reservationView = (RoomReservationView)findViewById(R.id.roomReservationView1);
		Reservation r = marker.getReservation();
		reservationView.setRoom(r.getRoom());
		reservationView.setMinTime(r.getBeginTime());
		reservationView.setMaxTime(r.getEndTime());
		reservationView.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				CalendarMarker m = RoomReservationPopup.this.marker;
				m.setReservation(m.getReservation(), false);
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

	
}
