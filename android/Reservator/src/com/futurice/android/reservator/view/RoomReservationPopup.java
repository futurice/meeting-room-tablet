package com.futurice.android.reservator.view;


import java.util.Calendar;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.model.Room;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.PopupWindow;

public class RoomReservationPopup extends PopupWindow{
	RoomReservationView reservationView;
	private RoomReservationPopup(View contentView, int width, int height){
		super(contentView, width, height, true);
		reservationView  = (RoomReservationView)contentView.findViewById(R.id.roomReservationView1);
		contentView.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		contentView.findViewById(R.id.reserveButton).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onClick(v);
				dismiss();
			}
		});
	}

	public void setData(Room r, Calendar minTime, Calendar maxTime){
		setRoom(r);
		setTimeRange(minTime, maxTime);
		reservationView.setReserveMode();
	}
	
	private void setRoom(Room r){
		this.reservationView.setRoom(r);
	}
	private void setTimeRange(Calendar min, Calendar max){
		this.reservationView.setMinTime(min);
		this.reservationView.setMaxTime(max);
	}
	public static PopupWindow create(View parent, Room room, Calendar minTime, Calendar maxTime){
		View root = parent.getRootView();
		View contentView = View.inflate(parent.getContext(), R.layout.reservation_popup, null);
		RoomReservationPopup popup = new RoomReservationPopup(contentView, root.getWidth(), root.getHeight());
		popup.setData(room, minTime, maxTime);
		return popup;
	}
}
