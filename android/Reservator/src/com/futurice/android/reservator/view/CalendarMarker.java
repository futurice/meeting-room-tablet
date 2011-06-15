package com.futurice.android.reservator.view;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.model.Reservation;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.MotionEvent;
import android.view.View;

public class CalendarMarker extends LinearLayout {
	private Reservation reservation = null;
	View reserveView = null;
	static String infoTextTemplate = null;
	TextView label = null;
	boolean reservationModeEnabled = false;
	public CalendarMarker(Context context) {
		super(context);
		if(infoTextTemplate == null){
			infoTextTemplate = context.getString(R.string.reservationTextTemplate);
		}
	}

	public void setReservation(Reservation reservation, boolean isReserved) {
		if (isReserved) {
			this.setText(TextUtils.expandTemplate(infoTextTemplate, reservation.getSubject()).toString());
			this.setBackgroundColor(Color.argb(65, 251, 142, 99));
		} else {
			this.setBackgroundColor(Color.TRANSPARENT);
		}
		this.reservation = reservation;
	}

	public Reservation getReservation() {
		return this.reservation;
	}

	@Override
	public String toString() {
		return reservation.getRoom().getName() + ": "
				+ reservation.getBeginTime().getTime().toGMTString() + "-"
				+ reservation.getEndTime().getTime().toGMTString();
	}

	public void setText(String text) {
		if (text == null || text.length() == 0) {
			this.removeView(label);
		} else {
			if (label == null) {
				label = new TextView(this.getContext());
				label.setLayoutParams(new LayoutParams(
						LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
				addView(label);
			}
			label.setText(text);
		}
	}
}
