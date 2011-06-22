package com.futurice.android.reservator.view;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.model.TimeSpan;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Html;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CalendarView extends RelativeLayout{
	private int idRunner;
	private static final int THICK_DELIM = 3, THIN_DELIM = 1,
			BOTTOM_PADDING = 65;
	private SimpleDateFormat dayLabelFormatter = new SimpleDateFormat("E d.M.");
	private RelativeLayout scrollView;
	Map<Integer, ViewGroup> columns = new HashMap<Integer, ViewGroup>();
	Paint gridPaint;

	private View leftmostColumn, leftmostHeader, rightmostColumn, rightmostHeader;
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
		inflate(getContext(),R.layout.calendar_view, this);
		hourColumn = (LinearLayout)findViewById(R.id.hourColumn);
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
		scrollView = (RelativeLayout) findViewById(R.id.relativeLayout1);
		clear();
	}

	@Override
	protected void dispatchDraw(Canvas c) {
		int topLeftHeight = findViewById(R.id.topLeftEmptyBox).getHeight();
		ViewGroup.LayoutParams lp = leftmostHeader.getLayoutParams();
		if(lp.height != topLeftHeight){
			lp.height = topLeftHeight;
			leftmostHeader.setLayoutParams(lp);
		}
		c.save();
		c.translate(0, hourColumn.getTop());
		for (int i = 0; i < hourColumn.getChildCount(); i++) {
			int y = hourColumn.getChildAt(i).getTop();
			c.drawLine(0, y, getWidth(), y, gridPaint);
		}
		c.restore();
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
		LinearLayout column = new LinearLayout(getContext());
		column.setBackgroundColor(Color.BLACK);
		column.setOrientation(LinearLayout.VERTICAL);
		column.setWeightSum(endHour.getTimeInMillis()/60000 - startHour.getTimeInMillis()/60000);
		LayoutParams columnLayoutParams = new LayoutParams(200, LayoutParams.WRAP_CONTENT);
		columnLayoutParams.addRule(RelativeLayout.ALIGN_TOP, rightmostColumn.getId());
		columnLayoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, rightmostColumn.getId());
		columnLayoutParams.addRule(RelativeLayout.RIGHT_OF, rightmostColumn.getId());
		rightmostColumn = column;
		column.setId(++idRunner);
		scrollView.addView(column, columnLayoutParams);
		columns.put(getDayIdentifier(day), column);

		LinearLayout columnHeader = (LinearLayout)inflate(getContext(), R.layout.day_column_header, null);
		LayoutParams headerLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		headerLayoutParams.addRule(RelativeLayout.ALIGN_TOP, rightmostHeader.getId());
		headerLayoutParams.addRule(ALIGN_BOTTOM, rightmostHeader.getId());
		headerLayoutParams.addRule(ALIGN_LEFT, rightmostColumn.getId());
		headerLayoutParams.addRule(ALIGN_RIGHT, rightmostColumn.getId());
		columnHeader.setId(++idRunner);
		scrollView.addView(columnHeader, headerLayoutParams);
		rightmostHeader = scrollView;
		
		
		final int dayOfWeek = day.get(Calendar.DAY_OF_WEEK);
		if (dayOfWeek == Calendar.MONDAY) {
			((TextView) columnHeader.findViewById(R.id.weekLabel)).setText("Week "
					+ day.get(Calendar.WEEK_OF_YEAR));
			//add vertical delim?
		}
		((TextView) columnHeader.findViewById(R.id.dayLabel))
				.setText(dayLabelFormatter.format(day.getTime()));
		
		
		
		//addVerticalDelimeter(THIN_DELIM, scrollView);
	}

	public CalendarMarker addMarker(Calendar begin, Calendar end) {
		CalendarMarker marker = getViewForTimeSpan(begin, end);
		if (begin.get(Calendar.HOUR_OF_DAY) < endHour.get(Calendar.HOUR_OF_DAY)) {
			columns.get(getDayIdentifier(begin)).addView(marker);
		}
		return marker;
	}

	private CalendarMarker getViewForTimeSpan(Calendar begin, Calendar end) {
		if (end.get(Calendar.HOUR_OF_DAY) >= endHour.get(Calendar.HOUR_OF_DAY)){
			end.set(Calendar.HOUR_OF_DAY, endHour.get(Calendar.HOUR_OF_DAY));
			end.set(Calendar.MINUTE, endHour.get(Calendar.MINUTE));
		}
		CalendarMarker v = new CalendarMarker(this.getContext(), new TimeSpan(begin, end));
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT,
				(end.getTimeInMillis()/60000 - begin.getTimeInMillis()/60000));
		v.setLayoutParams(lp);
		if(begin.get(Calendar.HOUR_OF_DAY) >= endHour.get(Calendar.HOUR_OF_DAY)){
			v.setVisibility(GONE);
		}
		return v;
	}
	
	//hash function for matching columns and specific days
	private int getDayIdentifier(Calendar day) {
		return day.get(Calendar.YEAR) * 1000 + day.get(Calendar.DAY_OF_YEAR);
	}

	public void clear(){
		scrollView.removeAllViews();
		idRunner = 42;
		leftmostHeader = new View(getContext());
		leftmostHeader.setId(++idRunner);
		LayoutParams lp = new LayoutParams(10, LayoutParams.WRAP_CONTENT);
		lp.addRule(ALIGN_PARENT_TOP);
		lp.addRule(ALIGN_PARENT_LEFT);
		scrollView.addView(leftmostHeader, lp);
		
		leftmostColumn = new View(getContext());
		leftmostColumn.setId(++idRunner);
		lp = new LayoutParams(10, 1);
		lp.addRule(BELOW, leftmostHeader.getId());
		lp.addRule(ALIGN_PARENT_LEFT);
		lp.addRule(ALIGN_PARENT_BOTTOM);
		scrollView.addView(leftmostColumn, lp);
		
		rightmostHeader = leftmostHeader;
		rightmostColumn = leftmostColumn;
		rightmostColumn.setBackgroundColor(Color.MAGENTA);//setVisibility(INVISIBLE);
		rightmostHeader.setBackgroundColor(Color.CYAN);//Visibility(INVISIBLE);
		
	}
}
