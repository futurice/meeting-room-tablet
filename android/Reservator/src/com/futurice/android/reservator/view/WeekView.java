package com.futurice.android.reservator.view;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.model.Reservation;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.Room;

public class WeekView extends RelativeLayout implements OnClickListener {

	public static final int NUMBER_OF_DAYS_TO_SHOW = 10;
	private Room currentRoom = null;
	private CalendarView calendar = null;

	public WeekView(Context context) {
		this(context, null);

	}

	public WeekView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);

	}

	public WeekView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		calendar = (CalendarView) findViewById(R.id.calendarView1); // for some
																	// reason
																	// findViewById(R.id.calendarView1)
																	// did not
																	// work
	}

	public void setRoom(Room room) {
		for (int i = 0; i < getChildCount(); i++) {
			if (getChildAt(i) instanceof CalendarView) {
				calendar = (CalendarView) getChildAt(i);
			}
		}
		calendar.clear();
		currentRoom = room;
		Calendar day = Calendar.getInstance();
		day.set(Calendar.HOUR_OF_DAY, 8);
		day.set(Calendar.MINUTE, 0);
		day.set(Calendar.SECOND, 0);
		day.set(Calendar.MILLISECOND, 0);

		List<Reservation> reservations;
		try {
			reservations = currentRoom.getReservations(true);
		} catch (ReservatorException e) {
			// TODO: XXX
			Log.e("DataProxy", "getReservations", e);
			return;
		}

		for (int i = 0; i < NUMBER_OF_DAYS_TO_SHOW; i++) {
			// Skip weekend days
			if (day.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
				day.add(Calendar.DAY_OF_YEAR, 2);
			} else if (day.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
				day.add(Calendar.DAY_OF_WEEK, 1);
			}
			Calendar endOfDay = (Calendar) day.clone();
			endOfDay.set(Calendar.HOUR_OF_DAY, 18);
			calendar.addDay((Calendar) day.clone());
			List<Reservation> daysReservations;

			daysReservations = getReservationsForDay(reservations, day);

			if (daysReservations.isEmpty()) {
				addFreeMarker(day, endOfDay);
			} else {
				Reservation first = daysReservations.get(0);
				if (first.getBeginTime().after(day)) {
					addFreeMarker(day, first.getBeginTime());
				}
				for (int j = 0; j < daysReservations.size(); j++) {
					Reservation current = daysReservations.get(j);
					addReseredMarker(current);

					if (j < daysReservations.size() - 1) {
						
						Reservation next = daysReservations.get(j + 1);
						if(next.getBeginTime().after(current.getEndTime())){
							addFreeMarker(current.getEndTime(), next.getBeginTime());
						}
					}
				}
			}

			day.add(Calendar.DAY_OF_YEAR, 1);
		}
		calendar.requestLayout();
	}

	private List<Reservation> getReservationsForDay(
			List<Reservation> reservations, Calendar day) {
		List<Reservation> daysReservations = new ArrayList<Reservation>();
		for (Reservation r : reservations) {
			if (r.getBeginTime().get(Calendar.DAY_OF_YEAR) == day
					.get(Calendar.DAY_OF_YEAR)
					&& r.getBeginTime().get(Calendar.YEAR) == day
							.get(Calendar.YEAR)) {
				daysReservations.add(r);
			}
		}
		return daysReservations;
	}

	@Override
	public void onClick(final View v) {

		if (v instanceof CalendarMarker) {
			final CalendarMarker marker = (CalendarMarker) v;
			if (marker.isReserved()) {
				return;
			} else {
				Dialog d = new RoomReservationPopup(getContext(), marker.getTimeSpan(), currentRoom);
				d.show();
				d.setOnCancelListener(new OnCancelListener() {

					@Override
					public void onCancel(DialogInterface dialog) {
						refreshData();
					}
				});
			}

		}
	}

	public void refreshData(){
		setRoom(currentRoom);
	}
	private void addFreeMarker(Calendar startTime, Calendar endTime) {
		CalendarMarker marker = calendar.addMarker(startTime, endTime);
		marker.setOnClickListener(this);
		marker.setReserved(false);
	}

	private void addReseredMarker(Reservation r) {
		CalendarMarker marker = calendar.addMarker(r.getBeginTime(),
				r.getEndTime());
		marker.setText(r.getSubject());
		marker.setReserved(true);
	}
}
