package com.futurice.android.reservator.view;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.model.Reservation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Html;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.View.OnClickListener;

public class CalendarView extends LinearLayout implements OnClickListener {
	private static final int THICK_DELIM = 3, THIN_DELIM = 1,
			BOTTOM_PADDING = 65;
	private SimpleDateFormat dayLabelFormatter = new SimpleDateFormat("E M.d.");
	private LinearLayout scrollView;
	private boolean skipWeekend = true;
	Map<Integer, ViewGroup> columns = new HashMap<Integer, ViewGroup>();
	Paint gridPaint;

	Calendar startHour = new GregorianCalendar(2000, 1, 1, 8, 0);
	Calendar endHour = new GregorianCalendar(2000, 1, 1, 18, 0);

	LinearLayout hourColumn;

	public CalendarView(Context context) {
		this(context, null);
	}

	public CalendarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		gridPaint = new Paint();
		gridPaint.setColor(Color.argb(255, 209, 211, 212));

		hourColumn = new LinearLayout(getContext());
		hourColumn.setPadding(0, 0, 0, BOTTOM_PADDING);
		hourColumn.setOrientation(LinearLayout.VERTICAL);
		hourColumn.addView(new TextView(getContext()));
		hourColumn.addView(new TextView(getContext()));

		for (int i = startHour.get(Calendar.HOUR_OF_DAY); i < endHour
				.get(Calendar.HOUR_OF_DAY); i++) {
			TextView tv = new TextView(getContext());
			tv.setText(Html.fromHtml(i + "<small>00</small>"));
			tv.setGravity(Gravity.TOP | Gravity.RIGHT);

			LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, 1, 1);
			lp.weight = 1;
			hourColumn.addView(tv, lp);
		}
		addView(hourColumn, LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT);
		addVerticalDelimeter(THIN_DELIM, this);
		inflate(context, R.layout.calendar_view, this);
		scrollView = (LinearLayout) findViewById(R.id.linearLayout1);
	}

	@Override
	protected void dispatchDraw(Canvas c) {
		for (int i = 2; i < hourColumn.getChildCount(); i++) {
			int y = hourColumn.getChildAt(i).getTop();
			c.drawLine(0, y, getWidth(), y, gridPaint);
		}
		super.dispatchDraw(c);
	}

	private void addVerticalDelimeter(int width, LinearLayout parent) {
		View v = new View(getContext());
		LayoutParams lp = new LinearLayout.LayoutParams(width,
				LayoutParams.FILL_PARENT);
		lp.bottomMargin = BOTTOM_PADDING;
		v.setLayoutParams(lp);
		v.setBackgroundColor(gridPaint.getColor());
		parent.addView(v);
	}

	public void addDay(Calendar day) {
		RelativeLayout container = new RelativeLayout(getContext());

		LinearLayout column = (LinearLayout) inflate(getContext(),
				R.layout.day_column, null);
		column.setPadding(0, 0, 0, BOTTOM_PADDING);
		column.setWeightSum(endHour.getTimeInMillis()
				- startHour.getTimeInMillis());
		column.setOrientation(LinearLayout.VERTICAL);
		if (day.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
			((TextView) column.findViewById(R.id.weekLabel)).setText("Week "
					+ day.get(Calendar.WEEK_OF_YEAR));
		}
		((TextView) column.findViewById(R.id.dayLabel))
				.setText(dayLabelFormatter.format(day.getTime()));
		container.addView(column, LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		scrollView.addView(container, 200, LayoutParams.MATCH_PARENT);

		columns.put(getDayIdentifier(day), column);

		addVerticalDelimeter(isLastDayOfWeek(day) ? THICK_DELIM : THIN_DELIM,
				scrollView);
	}

	public void setSkipWeekend(boolean skip) {
		this.skipWeekend = skip;
	}

	private boolean isLastDayOfWeek(Calendar day) {
		int dayOfWeek = day.get(Calendar.DAY_OF_WEEK);

		if (skipWeekend) {
			return dayOfWeek == Calendar.FRIDAY;
		} else {
			return dayOfWeek == Calendar.SUNDAY;
		}
	}

	public CalendarMarker addMarker(Calendar begin, Calendar end) {
		CalendarMarker marker = getViewForTimeSpan(begin, end);
		if (begin.get(Calendar.HOUR_OF_DAY) < endHour.get(Calendar.HOUR_OF_DAY)) {
			columns.get(getDayIdentifier(begin)).addView(marker);
			marker.setOnClickListener(this);
		}

		return marker;
	}

	private CalendarMarker getViewForTimeSpan(Calendar begin, Calendar end) {
		Calendar endTime = end;
		if (end.get(Calendar.HOUR_OF_DAY) < endHour.get(Calendar.HOUR_OF_DAY)) {
			endTime = new GregorianCalendar(end.get(Calendar.YEAR),
					end.get(Calendar.MONTH), end.get(Calendar.DAY_OF_MONTH),
					end.get(Calendar.HOUR_OF_DAY), end.get(Calendar.MINUTE));

		}
		CalendarMarker v = new CalendarMarker(this.getContext());
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, 1,
				(float) (endTime.getTimeInMillis() - begin.getTimeInMillis()));
		v.setLayoutParams(lp);
		return v;
	}

	private int getDayIdentifier(Calendar day) {
		return day.get(Calendar.YEAR) * 1000 + day.get(Calendar.DAY_OF_YEAR);
	}

	@Override
	public void onClick(final View v) {
		v.setBackgroundColor(Color.GREEN);
		Reservation reservation = ((CalendarMarker)v).getReservation();  
		final PopupWindow w = RoomReservationPopup.create(this, reservation.getRoom(), reservation.getBeginTime(), reservation.getEndTime());
		w.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss() {
				v.setBackgroundColor(Color.WHITE);
			}
		});
		w.showAtLocation(CalendarView.this, Gravity.CENTER, 0, 0);

	}
}
