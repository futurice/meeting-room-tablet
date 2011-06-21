package com.futurice.android.reservator.view;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import com.futurice.android.reservator.R;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Html;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.View.OnClickListener;

public class CalendarView extends LinearLayout implements OnClickListener {
	private static final int THICK_DELIM = 3, THIN_DELIM = 1,
			BOTTOM_PADDING = 65;
	private SimpleDateFormat dayLabelFormatter = new SimpleDateFormat("E d.M.");
	private LinearLayout scrollView;
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

			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LayoutParams.FILL_PARENT, 1, 1);
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
		LayoutParams lp = new LayoutParams(width, LayoutParams.FILL_PARENT);
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

		final int dayOfWeek = day.get(Calendar.DAY_OF_WEEK);
		if (dayOfWeek == Calendar.MONDAY) {
			((TextView) column.findViewById(R.id.weekLabel)).setText("Week "
					+ day.get(Calendar.WEEK_OF_YEAR));
			addVerticalDelimeter(THICK_DELIM, scrollView);
		}
		((TextView) column.findViewById(R.id.dayLabel))
				.setText(dayLabelFormatter.format(day.getTime()));
		container.addView(column, LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		scrollView.addView(container, 200, LayoutParams.MATCH_PARENT);

		columns.put(getDayIdentifier(day), column);

		addVerticalDelimeter(THIN_DELIM, scrollView);
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
				(endTime.getTimeInMillis() - begin.getTimeInMillis()));
		v.setLayoutParams(lp);
		return v;
	}

	private int getDayIdentifier(Calendar day) {
		return day.get(Calendar.YEAR) * 1000 + day.get(Calendar.DAY_OF_YEAR);
	}

	@Override
	public void onClick(final View v) {

		if (v instanceof CalendarMarker) {
			final CalendarMarker marker = (CalendarMarker) v;
			if (marker.isReserved()) {
				return;
			} else {
				Dialog d = new RoomReservationPopup(getContext(), marker);
				d.show();
				d.setOnCancelListener(new OnCancelListener() {

					@Override
					public void onCancel(DialogInterface dialog) {
						((WeekView) getParent()).setRoom(marker
								.getReservation().getRoom());
					}
				});
			}

		}
	}
	public void clear(){
		scrollView.removeAllViews();
	}
}
