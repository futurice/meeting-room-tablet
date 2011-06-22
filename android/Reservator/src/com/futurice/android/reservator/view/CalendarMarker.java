package com.futurice.android.reservator.view;


import com.futurice.android.reservator.R;
import com.futurice.android.reservator.model.Reservation;
import com.futurice.android.reservator.model.TimeSpan;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CalendarMarker extends LinearLayout {
	private Reservation reservation = null;
	private boolean reserved = false;
	private TimeSpan timeSpan;
	TextView label = null;
	public CalendarMarker(Context context, TimeSpan timeSpan) {
		super(context);
		this.timeSpan = timeSpan;
		this.setPadding(0,0,0,0);
	}

	public void setTimeSpan(TimeSpan span){
		this.timeSpan = span.clone();
	}
	public TimeSpan getTimeSpan(){
		return this.timeSpan;
	}
	
	public boolean isReserved(){
		return this.reserved;
	}
	public void setReserved(boolean isReserved) {
		this.reserved = isReserved;
		if (isReserved) {
			this.setBackgroundResource(R.color.CalendarMarkerReservedColor);
		} else {
			this.setBackgroundResource(R.color.CalendarMarkerFreeColor);
		}
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
