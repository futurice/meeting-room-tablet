package com.futurice.android.reservator.view;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.model.DateTime;
import com.futurice.android.reservator.model.Reservation;
import com.futurice.android.reservator.model.TimeSpan;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

public class CalendarMarker extends FrameLayout{
	private Reservation reservation = null;
	private boolean reserved = false;
	private TimeSpan timeSpan;
	View content = null;
	private float proportionalTouchYCoordinate = 0.0f;
	OnTouchListener onTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			proportionalTouchYCoordinate = event.getY() / getHeight();
			return false;
		}
	};
	public CalendarMarker(Context context, TimeSpan timeSpan) {
		super(context);
		this.timeSpan = timeSpan;
		this.setPadding(0,0,0,0);
		this.setOnTouchListener(onTouchListener);
	}
	@Override
	protected void dispatchDraw(Canvas c){
		super.dispatchDraw(c);
	}
	public void setTimeSpan(TimeSpan span){
		this.timeSpan = span.clone();
	}
	public TimeSpan getTimeSpan(){
		return this.timeSpan;
	}
	@Override
	public void onLayout(boolean changed, int l, int t, int r, int b){
		super.onLayout(changed, l, t, r, b);
		if(content != null){
			content.layout(0, 0, r-l, b-t);
		}
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
				+ reservation.getStartTime().toGMTString() + "-"
				+ reservation.getEndTime().toGMTString();
	}
	public void setText(CharSequence text) {
		TextView label = new TextView(getContext());
		label.setTextColor(getResources().getColor(R.color.CalendarTextColor));
		label.setText(text);
		setContent(label);
	}
	public void setContent(View v){
		this.removeView(content);
		content = v;
		addView(v);
	}
	public DateTime getTouchedTime(){
		float length = proportionalTouchYCoordinate  * timeSpan.getLength();
		return new DateTime((long)(timeSpan.getStart().getTimeInMillis() + length));
	}
}
