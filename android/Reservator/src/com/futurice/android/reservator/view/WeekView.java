package com.futurice.android.reservator.view;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.ReservatorApplication;
import com.futurice.android.reservator.model.Reservation;
import com.futurice.android.reservator.model.Room;
import com.futurice.android.reservator.model.TimeSpan;

public class WeekView extends RelativeLayout implements OnClickListener {

	public static final int NUMBER_OF_DAYS_TO_SHOW = 10;
	private Room currentRoom = null;
	private CalendarView calendar = null;
	private FrameLayout calendarFrame = null;
	public WeekView(Context context) {
		this(context, null);
	}

	public WeekView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public WeekView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setRoom(Room room) {
		calendarFrame = (FrameLayout)findViewById(R.id.frameLayout1);
		calendarFrame.removeAllViews();
		calendar = new CalendarView(getContext());


		currentRoom = room;
		Calendar today = Calendar.getInstance();
		today.set(Calendar.HOUR_OF_DAY, 8);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MILLISECOND, 0);
		
		Vector<Reservation> reservations = currentRoom.getReservations();
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
				Reservation last = daysReservations.get(daysReservations.size() - 1);
				if(last.getEndTime().before(endOfDay)){
					addFreeMarker(last.getEndTime(), endOfDay);
				}
			}
			day.add(Calendar.DAY_OF_YEAR, 1);
		}
		addDisabledMarker(today, Calendar.getInstance());
		calendarFrame.addView(calendar, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
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

				RoomReservationPopup d;

				Calendar start = marker.getTimeSpan().getStart();
				Calendar end = marker.getTimeSpan().getEnd();

				// if time span is less than hour, select it all
				if (marker.getTimeSpan().getLength() <= 60*60000) {
					d = new RoomReservationPopup(getContext(),marker.getTimeSpan(), marker.getTimeSpan(), currentRoom);
				} else {
					Calendar touch = marker.getTouchedTime();
					Calendar now = Calendar.getInstance();

					touch.set(Calendar.MINUTE, 0);
					touch.set(Calendar.SECOND, 0);
					touch.set(Calendar.MILLISECOND, 0);

					if (touch.before(start)) {
						touch = start;
					}
					if (touch.before(now) && now.before(end)) {
						touch = now;
					}

					TimeSpan presetTimeSpan = new TimeSpan(touch, Calendar.HOUR, 1);
					Calendar touchend = presetTimeSpan.getEnd();

					// quantize end to 15min steps
					touchend.set(Calendar.MINUTE, (touchend.get(Calendar.MINUTE) / 15) * 15);

					if (touchend.after(end)) {
						presetTimeSpan.setEnd((Calendar)end.clone()); // TODO: i really dislike this cloning
					}

					d = new RoomReservationPopup(getContext(),marker.getTimeSpan(), presetTimeSpan, currentRoom);
				}

				d.show();
				d.setOnCancelListener(new OnCancelListener() {

					@Override
					public void onCancel(DialogInterface dialog) {
						((ReservatorApplication)getContext().getApplicationContext()).getDataProxy().refreshRoomReservations(currentRoom);
					}
				});
			}

		}
	}

	public void refreshData(){
		post(new Runnable() {
			@Override
			public void run() {
				setRoom(currentRoom);
			}
		});
		
	}
	private void addFreeMarker(Calendar startTime, Calendar endTime) {
		if(startTime.after(endTime)){
			throw new IllegalArgumentException("starTime must be before endTime");
		}
		CalendarMarker marker = calendar.addMarker(startTime, endTime);
		marker.setOnClickListener(this);
		marker.setReserved(false);
	}
	private void addDisabledMarker(Calendar startTime, Calendar endTime){
		if(startTime.after(endTime)){
			throw new IllegalArgumentException("starTime must be before endTime");
		}
		CalendarMarker marker = calendar.addMarker(startTime, endTime);
		marker.setClickable(true); //blocks clicks from views it covers
		marker.setReserved(true);
		marker.setBackgroundColor(getResources().getColor(R.color.CalendarDisabledColor));
	}
	private void addReseredMarker(Reservation r) {
		CalendarMarker marker = calendar.addMarker(r.getBeginTime(),
				r.getEndTime());
		marker.setText(r.getSubject());
		marker.setReserved(true);
	}
}
