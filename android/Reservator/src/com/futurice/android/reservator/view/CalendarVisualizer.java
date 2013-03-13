package com.futurice.android.reservator.view;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Canvas.VertexMode;
import android.graphics.Paint.Align;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.model.DateTime;
import com.futurice.android.reservator.model.Reservation;
import com.futurice.android.reservator.model.TimeSpan;

public class CalendarVisualizer extends HorizontalScrollView implements ReservatorVisualizer,
		OnTouchListener {
	private Paint markerPaint, textPaint, weekTextPaint, gridPaint;
	private int dayStartTime = 60 * 8, dayEndTime = 60 * 20; // minutes from
																// midnight
	private DateTime firstDayToShow;
	private int daysToShow = 10;
	private int dayWidth = 200;
	private int timeLabelWidth = 100;
	private Reservation[] reservations;
	TimeSpan touchedTimeSpan;
	DateTime touchedTime;
	Shader reservationShader, leftEdgeShader, rightEdgeShader;
	int textColor, weekTextColor, gridColor;
	int weekStartDay = Calendar.MONDAY;
	String dayLabels[], weekLabels[];
	private SimpleDateFormat dayLabelFormatter, weekLabelFormatter;
	private Paint fadingEdgePaint;
	private RectF calendarAreaRect, timeLabelRect, headerRect;
	private FrameLayout contentFrame;
	public CalendarVisualizer(Context context) {
		this(context, null);
	}

	public CalendarVisualizer(Context context, AttributeSet attrs) {
		super(context, attrs);
		 firstDayToShow = new DateTime().stripTime();
		 //forces scroll view to have scrollable content area
		contentFrame = new FrameLayout(getContext());
		contentFrame.setClickable(true);
		contentFrame.setOnTouchListener(this);
		this.addView(contentFrame, LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);


		this.textColor = getResources().getColor(R.color.CalendarTextColor);
		this.weekTextColor = getResources().getColor(R.color.CalendarWeekTextColor);
		this.gridColor = getResources().getColor(R.color.CalendarBorderColor);

		this.textPaint = new Paint();
		textPaint.setColor(textColor);
		textPaint.setAntiAlias(true);

		this.weekTextPaint = new Paint();
		weekTextPaint.setColor(weekTextColor);
		weekTextPaint.setAntiAlias(true);

		this.gridPaint = new Paint();
		gridPaint.setColor(gridColor);

		this.markerPaint = new Paint();
		this.reservationShader = new LinearGradient(0, 0, 1, 1, getResources().getColor(R.color.CalendarMarkerReservedColor), getResources().getColor(R.color.CalendarMarkerReservedColor), TileMode.REPEAT);
		markerPaint.setShader(reservationShader);

		this.fadingEdgePaint = new Paint();
		this.leftEdgeShader = new LinearGradient(0, 0, 16, 0, Color.argb(128, 128, 128, 128), Color.argb(0, 0,0,0), TileMode.CLAMP);
		this.rightEdgeShader = new LinearGradient(0, 0, 16, 0, Color.argb(0, 0, 0, 0), Color.argb(128, 128, 128, 128), TileMode.CLAMP);

		setHorizontalFadingEdgeEnabled(false);
		this.setBackgroundColor(Color.TRANSPARENT);

		dayLabelFormatter = new SimpleDateFormat(getResources().getString(R.string.dateLabelFormat));
		String weekLabelFormat = getResources().getString(R.string.weekLabelFormat);
		weekLabelFormatter = new SimpleDateFormat(weekLabelFormat);

	}

	@Override
	public synchronized void setReservations(List<Reservation> reservationList) {
		long start = System.currentTimeMillis();
		this.reservations = new Reservation[reservationList.size()];
		reservationList.toArray(this.reservations);
		Arrays.sort(this.reservations);
		generateDayHeaderLabels();
		contentFrame.setPadding(Math.max(getWidth(), daysToShow * dayWidth + timeLabelWidth), 0,0,0);
		Log.d("Performance", "Set reservations done in " + (System.currentTimeMillis() - start) + "ms");
	}
	private void generateDayHeaderLabels(){
		if (reservations.length > 0) {
			dayLabels = new String[Math.max(
					getDaysFromStart(reservations[reservations.length - 1]
							.getEndTime()), daysToShow)];
			weekLabels = new String[dayLabels.length];
		} else {
			dayLabels = new String[daysToShow];
			weekLabels = new String[daysToShow];
		}

		DateTime day = getFirstDayToShow();
		for(int i = 0; i < dayLabels.length; i++){
			dayLabels[i] = dayLabelFormatter.format(day.getTime());
			weekLabels[i] = day.get(Calendar.DAY_OF_WEEK) == weekStartDay ? weekLabelFormatter.format(day.getTime()) : null;
			day = day.add(Calendar.DAY_OF_YEAR, 1);
		}
	}

	private void drawTimeLabels(Canvas c, RectF area){
		float width = area.width();
		float height = area.height();
		Align originalAlign = textPaint.getTextAlign();



		c.save();
		c.translate(getScrollX(), 0);
		//c.clipRect(area); no clipRect used. the first label goes few pixels above the top
		c.translate(area.left, area.top);
		textPaint.setTextAlign(Align.RIGHT);
		float normalTextSize = textPaint.getTextSize();
		float smallTextSize = normalTextSize * 0.642f;
		textPaint.setTextSize(smallTextSize);
		float padding = width / 8;
		float x = width - padding;
		final String minuteStr = "00";
		float minutesWidth = textPaint.measureText(minuteStr); //minutes are drawn separately with smaller font
		for(int minutes = dayStartTime; minutes < dayEndTime; minutes += 60){
			float timeY = getProportionalY(0,minutes) * height;
			textPaint.setTextSize(smallTextSize);
			c.drawText(minuteStr, x,  timeY + smallTextSize, textPaint);
			textPaint.setTextSize(normalTextSize);
			String hoursStr = Integer.toString(minutes / 60);
			c.drawText(hoursStr, x-minutesWidth,  timeY + smallTextSize, textPaint);
			c.drawLine(x + (width - x) / 3, timeY, width, timeY, gridPaint);
		}
		textPaint.setTextAlign(originalAlign);
		c.restore();
	}
	private void drawDayHeaders(Canvas c, RectF area){
		c.save();
		c.clipRect(area.left + getScrollX(), area.top,  area.right + getScrollX(), area.bottom);
		c.translate(area.left, area.top);
		float textSize = area.height() / 3;
		textPaint.setTextSize(textSize);
		float dayLabelY = area.height() - textSize / 2;
		float weekLabelY = dayLabelY - textSize;
		for(int i = 0; i < dayLabels.length; i++) {
			float x = i * dayWidth + textSize / 2;
			if(weekLabels[i] != null){
				c.drawText(weekLabels[i], x, weekLabelY, weekTextPaint);
			}
			c.drawText(dayLabels[i], x, dayLabelY, textPaint);
		}
		c.restore();
	}
	private void drawCalendarReservations(Canvas c, RectF area){
		c.save();
		c.clipRect(area.left + getScrollX(), area.top, area.right + getScrollX(), area.bottom);
		c.translate(area.left, area.top);
		int height = (int)area.height();
		if (reservations.length > 0) {
			float[] points = new float[reservations.length * 8];
			short[] indices = new short[reservations.length * 6];
			for (int i = 0; i < reservations.length; i++) {
				int j = 8 * i;
				//order of points is top-left, top-right, bottom-left, bottom-right
				points[j] = getXForTime(reservations[i].getStartTime());
				points[j + 1] = getProportionalY(reservations[i].getStartTime())*height;
				points[j + 2] = getXForTime(reservations[i].getStartTime()) + dayWidth;
				points[j + 3] = points[j + 1];
				points[j + 4] = points[j];
				points[j + 5] = getProportionalEndY(reservations[i].getEndTime())*height;
				points[j + 6] = points[j + 2];
				points[j + 7] = points[j + 5];
				j += 8;
				//top-left * 2, top-right, bottom-left, bottom-right * 2
				// *2 makes reservation connecting triangles zero area
				int p = 6 * i;
				short vi = (short)(4 * i); //each reservation needs 4 vertices
				indices[p] = vi;
				indices[p+1] = vi;
				indices[p+2] = (short) (vi+1);
				indices[p+3] = (short) (vi+2);
				indices[p+4] = (short) (vi+3);
				indices[p+5] = (short) (vi+3);
			}
			c.drawVertices(VertexMode.TRIANGLE_STRIP, points.length, points, 0,
					points, 0, null, 0, indices, 0, indices.length, markerPaint);
		
		}
		c.restore();
	}
	private void drawCalendarLines(Canvas c, RectF area){
		float height = area.height();
		c.save();
		c.clipRect(area.left + getScrollX(), area.top, area.right + getScrollX(), area.bottom);
		c.translate(area.left, area.top);


		for(int i = 0; i < dayLabels.length; i++) {
			c.drawLine(i * dayWidth, 0, i * dayWidth, height, gridPaint);
		}
		for (int minutes = dayStartTime; minutes < dayEndTime; minutes += 60) {
			float y = getProportionalY(0, minutes) * height;
			c.drawLine(0, y, contentFrame.getWidth(), y, gridPaint);
		}

		c.restore();
	}
	private void drawReservationSubjects(Canvas c, RectF area){
		float textHeight = textPaint.getTextSize();
		int padding = 4;
		float height = area.height();
		c.save();
		c.clipRect(area.left + getScrollX(), area.top, area.right + getScrollX(), area.bottom);
		c.translate(area.left, area.top);


		for(Reservation r : reservations){
			c.drawText(r.getSubject(), getXForTime(r.getStartTime()) + padding,getProportionalY(r.getStartTime())*height + textHeight + padding, textPaint);
		}
		c.restore();
	}
	private void drawFadingEdges(Canvas c, RectF area){
		c.save();
		//c.clipRect(area.left + getScrollX(), area.top,  area.right + getScrollX(), area.bottom);
		c.translate(area.left,area.top);
		if(getParent() instanceof View){
			c.translate(getScrollX(), 0);
			if(getScrollX() > 0){
				fadingEdgePaint.setShader(leftEdgeShader);
				c.drawRect(0, 0, 16, area.height(), fadingEdgePaint);
			}
			if(getScrollX() + getWidth() < contentFrame.getWidth()){
				c.translate(area.width()-16, 0);
				fadingEdgePaint.setShader(rightEdgeShader);
				c.drawRect(0, 0, 16, area.height(), fadingEdgePaint);
			}
		}
		c.restore();
	}
	
	private void drawCurrentTimeIndicators(Canvas c, RectF area) {
		c.save();
		c.clipRect(area.left + getScrollX(), area.top, area.right + getScrollX(), area.bottom);
		c.translate(area.left, area.top);
		int height = (int) area.height();
		
		DateTime now = new DateTime();
		
		int startX = getXForTime(now);
		int endX = startX + dayWidth;
		int currentY = (int) getProportionalY(now) * height;

		Paint fillPaint = new Paint();
		fillPaint.setARGB(128, 192, 192, 192); // #C0C0C0 = semialpha grey
		
		// the rectangle
		c.drawRect(startX, 0, endX, currentY, fillPaint);
		
		Paint linePaint = new Paint();
		linePaint.setColor(Color.RED);
		
		// the red line
		c.drawLine(startX, currentY, endX, currentY, linePaint);
		
		c.restore();
	}
	
	@Override
	protected void onDraw(Canvas c) {
		long start = System.currentTimeMillis();
		int headerHeight = Math.min(getHeight(), getWidth()) / 12;
		timeLabelRect = new RectF(0, headerHeight, timeLabelWidth, getHeight());
		calendarAreaRect = new RectF(timeLabelWidth, headerHeight, getWidth(), getHeight());
		headerRect = new RectF(timeLabelWidth, 0, getWidth(), headerHeight);

		drawDayHeaders(c, headerRect);
		drawCalendarLines(c, calendarAreaRect);
		drawCalendarReservations(c, calendarAreaRect);
		drawReservationSubjects(c, calendarAreaRect);
		drawFadingEdges(c, calendarAreaRect);
		drawTimeLabels(c, timeLabelRect);
		drawCurrentTimeIndicators(c, calendarAreaRect);
		
		Log.d("Performance", "Drew CalendarVisualizer in " + (System.currentTimeMillis() - start) + "ms");
	}

	private int getDaysFromStart(DateTime day) {
		return (int)(day.getTimeInMillis() - getFirstDayToShow().getTimeInMillis()) / (60*60*24*1000);
		//return day.subtract(getFirstDayToShow(), Calendar.DAY_OF_YEAR);
	}

	private float getProportionalEndY(DateTime time){
		int hours = time.get(Calendar.HOUR_OF_DAY);
		return getProportionalY(hours ==  0 ? 24 : hours , time.get(Calendar.MINUTE));
	}
	private float getProportionalY(DateTime time){
		return getProportionalY(time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE));
	}
	private float getProportionalY(int hours, int minutes) {
		return (minutes + hours * 60 - dayStartTime) / (float) (dayEndTime - dayStartTime);
	}

	@Override
	public boolean onTouch(View v, MotionEvent e) {
		//TODO This causes a small slow down in scrolling animation when ACTION_UP occurs:/
		if (e.getAction() == MotionEvent.ACTION_UP) {
			touchedTime = getTimeForCoordinates(e.getX(), e.getY());
			Reservation colliding = getReservationForTime(touchedTime);
			if (colliding != null) {
				return true; //cancel onClick if reserved marker is touched
			}
			DateTime start;
			Reservation before = findReservationBefore(touchedTime);
			if(before == null || touchedTime.stripTime().after(before.getEndTime())){
				start = touchedTime.setTime(dayStartTime / 60, dayStartTime % 60, 0);
			}else{
				start = before.getEndTime();
			}
			DateTime end;
			Reservation after = findReservationAfter(touchedTime);
			if(after == null || after.getStartTime().stripTime().after(touchedTime)){
				end = touchedTime.setTime(dayEndTime / 60, dayEndTime % 60, 0);
			}else{
				end = after.getStartTime();
			}
			touchedTimeSpan = new TimeSpan(start, end);
			Log.d("CalendarVisualize", "Calendar visualizer touched time: "
					+ touchedTime.toGMTString() + "\n timespan: "
					+ touchedTimeSpan.getStart().toGMTString() + "-"
					+ touchedTimeSpan.getEnd().toGMTString());
			invalidate();
		}
		return false; // do not interfere with onClick logic
	}

	private Reservation getReservationForTime(DateTime time) {
		for (int i = 0; i < reservations.length; i++) {
			if (reservations[i].getStartTime().before(time)) {
				if (reservations[i].getEndTime().after(time)) {
					return reservations[i];
				}
			} else {
				return null;
			}
		}
		return null;
	}

	private Reservation findReservationBefore(DateTime time) {
		Reservation latest = null;
		for (int i = 0; i < reservations.length; i++) {
			if (reservations[i].getEndTime().before(time)) {
				latest = reservations[i];
			} else {
				return latest;
			}
		}
		return null;
	}

	private Reservation findReservationAfter(DateTime time) {
		for (int i = 0; i < reservations.length; i++) {
			if (reservations[i].getStartTime().after(time)) {
				return reservations[i];
			}
		}
		return null;
	}

	private DateTime getTimeForCoordinates(float x, float y) {
		int minutes = dayStartTime + (int) ( (y - calendarAreaRect.top) / calendarAreaRect.height() * (dayEndTime - dayStartTime));
		DateTime absoluteDays = firstDayToShow.add(Calendar.DAY_OF_YEAR, (int) ((x - calendarAreaRect.left) / dayWidth))
				.setTime(minutes / 60, minutes % 60, 0);
		return absoluteDays;
	}

	public DateTime getSelectedTime() {
		return touchedTime;
	}

	public TimeSpan getSelectedTimeSpan() {
		return touchedTimeSpan;
	}

	private DateTime getFirstDayToShow() {
		if (reservations.length > 0
				&& reservations[0].getStartTime().before(firstDayToShow)) {
			return reservations[0].getStartTime();
		} else {
			return firstDayToShow; // TODO some logic here now it's today by default
		}
	}

	public int getXForTime(DateTime day) {
		return getDaysFromStart(day) * dayWidth;
	}

	@Override
	public void setOnClickListener(final OnClickListener l){
		//ScrollView does not produce onClick events, so bind the contentFrame's onClick to fake this ones onClick..
		contentFrame.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				l.onClick(CalendarVisualizer.this);
			}
		});
	}

}
