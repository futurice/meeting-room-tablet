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
	private Paint markerPaint, textPaint, gridPaint;
	private int dayStartTime = 60 * 8, dayEndTime = 60 * 18; // minutes from
																// midnight
	private DateTime firstDayToShow = new DateTime();
	private int daysToShow = 10;
	private int dayWidth = 200;
	private int timeLabelWidth = 100;
	private Reservation[] reservations;
	TimeSpan touchedTimeSpan;
	DateTime touchedTime;
	Shader reservationShader, leftEdgeShader, rightEdgeShader;
	int textColor, gridColor;
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
		contentFrame = new FrameLayout(getContext());
		this.addView(contentFrame, LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
		this.textColor = getResources().getColor(R.color.CalendarTextColor);
		this.gridColor = getResources().getColor(R.color.CalendarBorderColor);
		this.textPaint = new Paint();
		textPaint.setColor(textColor);
		this.gridPaint = new Paint();
		gridPaint.setColor(gridColor);
		this.markerPaint = new Paint();
		this.reservationShader = new LinearGradient(0, 0, 1, 1, getResources().getColor(R.color.CalendarMarkerReservedColor), getResources().getColor(R.color.CalendarMarkerReservedColor), TileMode.REPEAT);
		markerPaint.setShader(reservationShader);
		
		this.fadingEdgePaint = new Paint();
		this.leftEdgeShader = new LinearGradient(0, 0, 8, 0, Color.argb(128, 64, 64, 64), Color.argb(0, 0,0,0), TileMode.CLAMP);
		this.rightEdgeShader = new LinearGradient(0, 0, 8, 0, Color.argb(0, 0, 0, 0), Color.argb(128, 64, 64, 64), TileMode.CLAMP);
		
		setHorizontalFadingEdgeEnabled(false);
		this.setBackgroundColor(Color.TRANSPARENT);
		this.setOnTouchListener(this);
		dayLabelFormatter = new SimpleDateFormat(getResources().getString(R.string.dateLabelFormat));
		String weekLabelFormat = getResources().getString(R.string.weekLabelFormat);
		weekLabelFormatter = new SimpleDateFormat(weekLabelFormat);
		
	}

	@Override
	public synchronized void setReservations(List<Reservation> reservationList) {
		int listSize = reservationList.size();;
		this.reservations = new Reservation[listSize];
		reservationList.toArray(this.reservations);
		Arrays.sort(this.reservations);
		generateDayHeaderLabels();
		contentFrame.setPadding(Math.max(getWidth(), daysToShow * dayWidth + timeLabelWidth), 0,0,0);
	}
	private void generateDayHeaderLabels(){
		if(reservations.length > 0){
		dayLabels = new String[Math.max(getDaysFromStart(reservations[reservations.length - 1].getEndTime()), daysToShow)];
		weekLabels = new String[dayLabels.length];
		}else{
			dayLabels = new String[daysToShow];
			weekLabels = new String[daysToShow];
		}
		
		DateTime day = getFirstDayToShow();
		for(int i = 0; i < dayLabels.length; i++){
			Log.d("CalendarVisualizer", day.toGMTString());
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
		float minutesWidth = textPaint.measureText(":00"); //minutes are drawn separately with smaller font
		for(int minutes = dayStartTime; minutes < dayEndTime; minutes += 60){
			float timeY = getProportionalY(0,minutes) * height;
			textPaint.setTextSize(smallTextSize);
			c.drawText(":00", x,  timeY + smallTextSize, textPaint);
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
				c.drawText(weekLabels[i], x, weekLabelY, textPaint);
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
			float[] points = new float[reservations.length * 12];
			int j = 0;
			for (int i = 0; i < reservations.length; i++) {
				points[j] = getXForTime(reservations[i].getStartTime()) + dayWidth;
				points[j + 1] = getProportionalY(reservations[i].getStartTime())*height;
				points[j + 2] = points[j]; // double to make invisible triangle
				points[j + 3] = points[j + 1];
				points[j + 4] = getXForTime(reservations[i].getStartTime());
				points[j + 5] = points[j + 1];
				points[j + 6] = points[j];
				points[j + 7] = getProportionalY(reservations[i].getEndTime())*height;
				points[j + 8] = points[j + 4];
				points[j + 9] = points[j + 7];
				points[j + 10] = points[j + 8];// double to make invisible
												// triangle
				points[j + 11] = points[j + 9];
				j += 12;
			}
			c.drawVertices(VertexMode.TRIANGLE_STRIP, points.length, points, 0,
					points, 0, null, 0, null, 0, 0, markerPaint);
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
				c.drawRect(0, 0, 8, area.height(), fadingEdgePaint);
			}
			if(getScrollX() + getWidth() < contentFrame.getWidth()){
				c.translate(area.width()-8, 0);
				fadingEdgePaint.setShader(rightEdgeShader);
				c.drawRect(0, 0, 8, area.height(), fadingEdgePaint);
			}
		}
		c.restore();
	}
	@Override
	protected void onDraw(Canvas c) {
		int headerHeight = Math.min(getHeight(), getWidth()) / 12;
		timeLabelRect = new RectF(0, headerHeight, timeLabelWidth, getHeight());
		calendarAreaRect = new RectF(timeLabelWidth, headerHeight, getWidth(), getHeight());
		headerRect = new RectF(timeLabelWidth, 0, getWidth(), headerHeight);
		textPaint.setAntiAlias(true);
		//dayWidth = getWidth() / daysToShow;
		//day and week labels
//		c.save();
//		c.translate(timeLabelWidth, 0);
//		c.translate(parentScroll, 0);
//		c.clipRect(0,0,getWidth() - timeLabelWidth, headerHeight);
//		c.translate(-parentScroll, 0);
		drawDayHeaders(c, headerRect);
//		//c.restore();
//		
//		//calendar markers and the grid
//		c.save();
//		c.translate(parentScroll, 0);
//		c.clipRect(calendarAreaRect);
//		c.translate(-parentScroll, 0);
//		c.translate(timeLabelWidth, headerHeight);
		drawCalendarLines(c, calendarAreaRect);//getWidth() - timeLabelWidth, getHeight() - headerHeight);
		drawCalendarReservations(c, calendarAreaRect);//getWidth() - timeLabelWidth, getHeight() - headerHeight);
		drawReservationSubjects(c, calendarAreaRect);//getWidth() - timeLabelWidth, getHeight() - headerHeight);
		drawFadingEdges(c, calendarAreaRect);//((View)getParent()).getWidth() - (int)calendarAreaRect.left, (int)calendarAreaRect.height());
		//		c.restore();
		
		//timeLabels on the left
		//c.save();
		//c.translate(0, headerHeight);
		drawTimeLabels(c, timeLabelRect);//timeLabelWidth, getHeight() - headerHeight);
		//c.restore();
		
		
	}

	private int getDaysFromStart(DateTime day) {
		int days = day.get(Calendar.DAY_OF_YEAR)
				- getFirstDayToShow().get(Calendar.DAY_OF_YEAR);
		return days;

	}
	private float getProportionalY(DateTime time){
		return getProportionalY(time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE));
	}
	private float getProportionalY(int hours, int minutes) {
		return (minutes + hours * 60 - dayStartTime) / (float) (dayEndTime - dayStartTime);
	}

	@Override
	public boolean onTouch(View v, MotionEvent e) {
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
				start = before.getStartTime();
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
		return firstDayToShow.add(Calendar.DAY_OF_YEAR, (int) ((x - calendarAreaRect.left) / dayWidth))
				.setTime(minutes / 60, minutes % 60, 0);

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
			return firstDayToShow; // TODO some logic here
		}
	}

	public int getXForTime(DateTime day) {
		return getDaysFromStart(day) * dayWidth;

	}
	/*@Override
	public void onMeasure(int widthSpec, int heightSpec) {
		int width = Math.max(MeasureSpec.getSize(widthSpec), daysToShow	* dayWidth + timeLabelWidth);
		setMeasuredDimension(width, MeasureSpec.getSize(heightSpec));
	}*/
}
