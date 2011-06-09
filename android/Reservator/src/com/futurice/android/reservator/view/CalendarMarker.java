package com.futurice.android.reservator.view;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.model.Reservation;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class CalendarMarker extends LinearLayout implements OnTouchListener {
	private Reservation reservation = null;
	View reserveView = null;
	static String infoTextTemplate = null;
	TextView label = null;

	public CalendarMarker(Context context) {
		super(context);
		if(infoTextTemplate == null){
			infoTextTemplate = context.getString(R.string.reservationTextTemplate);
		}
		
	}

	public CalendarMarker(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setReservation(Reservation reservation, boolean isReserved) {
		if (isReserved) {
			this.setText(TextUtils.expandTemplate(infoTextTemplate, reservation.getSubject()).toString());
			this.setBackgroundColor(Color.argb(100, 255, 166, 201));
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

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int y = (int) event.getY();
		if (reserveView == null) {

			reserveView = new ReserveView(getContext());
			LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			params.setMargins(0, y, 0, 0);
			reserveView.setLayoutParams(params);
			reserveView.setBackgroundColor(Color.YELLOW);
			reserveView.setPadding(0, 0, 0, 0);
			addView(reserveView);
		} else {

			if (y < reserveView.getTop() + reserveView.getHeight() / 2) {
				LayoutParams lp = (LayoutParams) reserveView.getLayoutParams();
				lp.setMargins(0, y, 0, 0);
				reserveView.setLayoutParams(lp);

			} else if (y > reserveView.getBottom()) {
				reserveView.setPadding(0, 0, 0, y - reserveView.getBottom());
			}

		}
		return true;
	}
}
