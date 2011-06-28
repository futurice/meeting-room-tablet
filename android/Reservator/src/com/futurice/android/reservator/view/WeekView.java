package com.futurice.android.reservator.view;

import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.model.Reservation;
import com.futurice.android.reservator.model.Room;
import com.futurice.android.reservator.model.TimeSpan;

public class WeekView extends RelativeLayout implements OnClickListener {

	public static final int NUMBER_OF_DAYS_TO_SHOW = 10;

	private CalendarView calendarView = null;
	private FrameLayout calendarFrame = null;
	public WeekView(Context context) {
		this(context, null);
	}

	private OnFreeTimeClickListener onFreeTimeClickListener = null;

	public WeekView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public WeekView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void refreshData(Room room) {
		calendarFrame = (FrameLayout)findViewById(R.id.frameLayout1);
		calendarFrame.removeAllViews();
		calendarView = new CalendarView(getContext());

		Calendar today = Calendar.getInstance();
		today.set(Calendar.HOUR_OF_DAY, 8);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MILLISECOND, 0);

		Calendar day = (Calendar)today.clone();
		for (int i = 0; i < NUMBER_OF_DAYS_TO_SHOW; i++) {
			// Skip weekend days
			if (day.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
				day.add(Calendar.DAY_OF_YEAR, 2);
			} else if (day.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
				day.add(Calendar.DAY_OF_WEEK, 1);
			}
			Calendar endOfDay = (Calendar) day.clone();
			endOfDay.set(Calendar.HOUR_OF_DAY, 18);

			calendarView.addDay((Calendar) day.clone());

			List<Reservation> daysReservations = room.getReservationsForDay(day);

			if (daysReservations.isEmpty()) {
				addFreeMarker(day, endOfDay);
			} else {
				Reservation first = daysReservations.get(0);
				if (first.getStartTime().after(day)) {
					addFreeMarker(day, first.getStartTime());
				}
				for (int j = 0; j < daysReservations.size(); j++) {
					Reservation current = daysReservations.get(j);
					addReservedMarker(current);
					if (j < daysReservations.size() - 1) {
						Reservation next = daysReservations.get(j + 1);
						if(next.getStartTime().after(current.getEndTime())){
							addFreeMarker(current.getEndTime(), next.getStartTime());
						}
					}
				}
				Reservation last = daysReservations.get(daysReservations.size() - 1);
				if(last.getEndTime().before(endOfDay)){
					addFreeMarker(last.getEndTime(), endOfDay);
				}
			}
			day.add(Calendar.DAY_OF_YEAR, 1);
		}
		addDisabledMarker(today, Calendar.getInstance());
		calendarFrame.addView(calendarView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

	}

	public static interface OnFreeTimeClickListener {
		abstract void onFreeTimeClick(View v, TimeSpan timeSpan, Calendar clickTime);
	}

	public void setOnFreeTimeClickListener(OnFreeTimeClickListener onFreeTimeClickListener) {
		this.onFreeTimeClickListener  = onFreeTimeClickListener;
	}

	@Override
	public void onClick(final View v) {

		if (v instanceof CalendarMarker) {
			final CalendarMarker marker = (CalendarMarker) v;
			if (marker.isReserved()) {
				return;
			} else {
				if (onFreeTimeClickListener != null) {
					onFreeTimeClickListener.onFreeTimeClick(v, marker.getTimeSpan(), marker.getTouchedTime());
				}
			}
		}
	}

	private void addFreeMarker(Calendar startTime, Calendar endTime) {
		if(startTime.after(endTime)){
			throw new IllegalArgumentException("starTime must be before endTime");
		}
		CalendarMarker marker = calendarView.addMarker(startTime, endTime);
		marker.setOnClickListener(this);
		marker.setReserved(false);
	}
	private void addDisabledMarker(Calendar startTime, Calendar endTime){
		if(startTime.after(endTime)){
			throw new IllegalArgumentException("starTime must be before endTime");
		}
		CalendarMarker marker = calendarView.addMarker(startTime, endTime);
		marker.setClickable(true); //blocks clicks from views it covers
		marker.setReserved(true);
		marker.setBackgroundColor(getResources().getColor(R.color.CalendarDisabledColor));
	}
	private void addReservedMarker(Reservation r) {
		CalendarMarker marker = calendarView.addMarker(r.getStartTime(),
				r.getEndTime());
		marker.setText(r.getSubject());
		marker.setReserved(true);
	}

}
