package com.futurice.android.reservator.view;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.model.Reservation;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.Room;

public class WeekView extends RelativeLayout {

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
		calendar = (CalendarView)findViewById(R.id.calendarView1); //for some reason findViewById(R.id.calendarView1) did not work
	}
	public void setRoom(Room room) {
		for(int i = 0; i < getChildCount(); i++){
			if(getChildAt(i) instanceof CalendarView){
				calendar = (CalendarView)getChildAt(i);
			}
		}
		currentRoom = room;
		Calendar day = Calendar.getInstance();
		day.set(Calendar.HOUR_OF_DAY, 8);
		day.set(Calendar.MINUTE, 0);
		day.set(Calendar.SECOND, 0);

		for (int i = 0; i < NUMBER_OF_DAYS_TO_SHOW; i++) {
			Calendar endOfDay = (Calendar) day.clone();
			endOfDay.set(Calendar.HOUR_OF_DAY, 18);
			calendar.addDay((Calendar) day.clone());
			List<Reservation> daysReservations;
			try {
				daysReservations = getReservationsForDay(
						currentRoom.getReservations(true), day);
			} catch (ReservatorException e) {
				// TODO: XXX
				Log.e("DataProxy", "getReservations", e);
				return;
			}

			if (daysReservations.isEmpty()) {
				calendar.addMarker(day, endOfDay).setReservation(
						new Reservation(currentRoom, null, day, endOfDay),
						false);
			} else {
				Reservation first = daysReservations.get(0);
				if (first.getBeginTime().after(day)) {
					calendar.addMarker(day, first.getBeginTime()).setReservation(new Reservation(currentRoom, null, day, first.getBeginTime()), false);
				}
				for (int j = 0; j < daysReservations.size(); j++) {
					Reservation current = daysReservations.get(j);
					calendar.addMarker(current.getBeginTime(),
							current.getEndTime()).setReservation(current, true);

					if (j < daysReservations.size() - 1) {
						Reservation next = daysReservations.get(j + 1);
						Reservation free = new Reservation(currentRoom, null,
								current.getEndTime(), next.getBeginTime());
						calendar.addMarker(current.getEndTime(),
								next.getBeginTime())
								.setReservation(free, false);
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
}
