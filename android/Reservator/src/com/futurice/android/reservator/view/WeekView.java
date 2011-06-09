package com.futurice.android.reservator.view;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.R.color;
import com.futurice.android.reservator.model.Reservation;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.Room;

public class WeekView extends RelativeLayout {

	public static final int NUMBER_OF_DAYS_TO_SHOW = 10;
	private boolean skipWeekend = true;
	private Room currentRoom = null;
	private LinearLayout container = null;

	public WeekView(Context context) {
		super(context);
		inflate(context, R.layout.calendar_view, this);
		container = (LinearLayout) findViewById(R.id.container);

	}

	public WeekView(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.calendar_view, this);
		container = (LinearLayout) findViewById(R.id.container);

	}

	public WeekView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		inflate(context, R.layout.calendar_view, this);
		container = (LinearLayout) findViewById(R.id.container);
	}

	public void setRoom(Room room) {
		currentRoom = room;
		container.removeAllViews();

		Calendar day = Calendar.getInstance();
		day.set(Calendar.HOUR_OF_DAY, 8);
		day.set(Calendar.MINUTE, 0);
		day.set(Calendar.SECOND, 0);

		container.addView(getHorizontalDelimiter(false));
		for (int i = 0; i < NUMBER_OF_DAYS_TO_SHOW; i++) {
			Calendar endOfDay = (Calendar) day.clone();
			endOfDay.set(Calendar.HOUR_OF_DAY, 18);
			LinearLayout column = new LinearLayout(getContext());
			column.setOrientation(LinearLayout.VERTICAL);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(200,
					LayoutParams.FILL_PARENT, 0.2f);
			column.setLayoutParams(lp);

			

			View dayLabel = getColumnHeader(day);
			column.addView(dayLabel);

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
				CalendarMarker freeMarker = getViewForTimeSpan(day, endOfDay);
				column.addView(freeMarker);
			} else {
				Reservation first = daysReservations.get(0);
				if (first.getBeginTime().after(day)) {
					CalendarMarker leadingView = getViewForTimeSpan(day,
							first.getBeginTime());
					column.addView(leadingView);
				}
				for (int j = 0; j < daysReservations.size(); j++) {
					Reservation current = daysReservations.get(j);
					CalendarMarker reservedView = getViewForTimeSpan(
							current.getBeginTime(), current.getEndTime());
					reservedView.setReservation(current, true);
					column.addView(reservedView);

					if (j < daysReservations.size() - 1) {
						Reservation next = daysReservations.get(j + 1);
						CalendarMarker freeView = getViewForTimeSpan(
								current.getEndTime(), next.getBeginTime());
						column.addView(freeView);
					}
				}
			}
			if (day.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
				container.addView(getHorizontalDelimiter(true));
			} else {
				container.addView(getHorizontalDelimiter(false));
			}
			if (skipWeekend && day.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
				day.add(Calendar.DAY_OF_YEAR, 3);

			} else {
				day.add(Calendar.DAY_OF_YEAR, 1);
			}
			container.addView(column);
		}
	}

	private View getColumnHeader(Calendar day){
		//Datestamp
		TextView tv = new TextView(getContext());
		tv.setTextAppearance(getContext(), R.style.CalendarFont);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, 1, 3600000f);
		SimpleDateFormat f = new SimpleDateFormat(getContext().getString(
				R.string.dateLabelFormat));
		String dayText = f.format(day.getTime());
		tv.setText(dayText);
		tv.setGravity(Gravity.CENTER);
		tv.setLayoutParams(lp);
		return tv;
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

	private CalendarMarker getViewForTimeSpan(Calendar begin, Calendar end) {
		CalendarMarker v = new CalendarMarker(this.getContext());
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, 1,
				(float) (end.getTimeInMillis() - begin.getTimeInMillis()));
		v.setReservation(new Reservation(currentRoom, null, begin, end), false);
		v.setLayoutParams(lp);
		return v;
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		LinearLayout hourLabelContainer = (LinearLayout) findViewById(R.id.linearLayout1);
		if (container != null && container.getWidth() > 0
				&& container.getHeight() > 0) {
			Paint p = new Paint();
			p.setColor(color.CalendarBorderColor);
			canvas.save();
			canvas.translate(0, hourLabelContainer.getTop());
			int right = ((HorizontalScrollView) container.getParent())
					.getRight();
			final int x = 0;
			for (int i = 0; i < hourLabelContainer.getChildCount(); i++) {
				View v = hourLabelContainer.getChildAt(i);
				canvas.drawLine(x, v.getBottom(), right, v.getBottom(), p);
			}
			canvas.restore();
		}
		super.dispatchDraw(canvas);
	}

	private View getHorizontalDelimiter(boolean thick) {
		View v = new View(getContext());
		v.setLayoutParams(new LinearLayout.LayoutParams(thick ? 3 : 1,
				LinearLayout.LayoutParams.FILL_PARENT));
		v.setBackgroundColor(Color.LTGRAY);
		return v;
	}
}
