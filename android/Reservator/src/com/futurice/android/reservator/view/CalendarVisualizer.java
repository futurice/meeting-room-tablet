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
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.model.DateTime;
import com.futurice.android.reservator.model.Reservation;
import com.futurice.android.reservator.model.Room;
import com.futurice.android.reservator.model.TimeSpan;

public class CalendarVisualizer extends View implements ReservatorVisualizer,
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
	private RectF calendarAreaRect;
	public CalendarVisualizer(Context context) {
		this(context, null);
	}

	public CalendarVisualizer(Context context, AttributeSet attrs) {
		super(context, attrs);
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
		invalidate();
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

	
	private void drawTimeLabels(Canvas c, int width, int height){
		Align originalAlign = textPaint.getTextAlign();
		c.save();
		if(getParent() instanceof View){
			int dx = ((View)getParent()).getScrollX();
			if(dx != 0){
				c.translate(dx, 0);
			}
		}
		
		textPaint.setTextAlign(Align.RIGHT);
		float normalTextSize = textPaint.getTextSize();
		float smallTextSize = normalTextSize * 0.642f;
		textPaint.setTextSize(smallTextSize);
		int padding = width / 8;
		int x = width - padding;
		float minutesWidth = textPaint.measureText(":00");
		for(int minutes = dayStartTime; minutes < dayEndTime; minutes += 60){
			float timeY = getProportionalY(0,minutes) * height;
			textPaint.setTextSize(smallTextSize);
			c.drawText(":00", x,  timeY + smallTextSize, textPaint);
			textPaint.setTextSize(normalTextSize);
			String hoursStr = Integer.toString(minutes / 60);
			c.drawText(hoursStr, x-minutesWidth,  timeY + smallTextSize, textPaint);
			c.drawLine(x + (width - x) / 3, timeY, width, timeY, gridPaint);
		}
		c.restore();
		textPaint.setTextAlign(originalAlign);
	}
	private void drawDayHeaders(Canvas c, int width, int height){
		int textSize = height / 3;
		textPaint.setTextSize(textSize);
		int dayLabelY = height - textSize / 2;
		int weekLabelY = dayLabelY - textSize;
		for(int i = 0; i < dayLabels.length; i++) {
			int x = i * dayWidth + textSize / 2;
			if(weekLabels[i] != null){
				c.drawText(weekLabels[i], x, weekLabelY, textPaint);
			}
			c.drawText(dayLabels[i], x, dayLabelY, textPaint);
		}
	}
	private void drawCalendarReservations(Canvas c, int width, int height){
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
	}
	private void drawCalendarLines(Canvas c, int width, int height){
		for(int i = 0; i < dayLabels.length; i++) {
			c.drawLine(i * dayWidth, 0, i * dayWidth, height, gridPaint);
		}
		for (int minutes = dayStartTime; minutes < dayEndTime; minutes += 60) {
			float y = getProportionalY(0, minutes) * height;
			c.drawLine(0, y, getWidth(), y, gridPaint);
		}
	}
	private void drawReservationSubjects(Canvas c, int width, int height){
		float textHeight = textPaint.getTextSize();
		int padding = 4;
		for(Reservation r : reservations){
			c.drawText(r.getSubject(), getXForTime(r.getStartTime()) + padding,getProportionalY(r.getStartTime())*height + textHeight + padding, textPaint);
		}
	}
	private void drawFadingEdges(Canvas c, int width, int height){
		if(getParent() instanceof View){
			View parent = (View)getParent();
			int scrollX = parent.getScrollX();
			Rect drawingRect = new Rect();
			getDrawingRect(drawingRect);
			c.translate(scrollX, 0);
			if(scrollX > 0){
				fadingEdgePaint.setShader(leftEdgeShader);
				c.drawRect(0, 0, 8, height, fadingEdgePaint);
			}
			if(scrollX + parent.getWidth() < getWidth()){ //A bit ugly mayhaps
				c.translate(width-8, 0);
				fadingEdgePaint.setShader(rightEdgeShader);
				c.drawRect(0, 0, 8, height, fadingEdgePaint);
			}
		}
		//TODO right edge too
	}
	@Override
	protected void onDraw(Canvas c) {
		
		int parentScroll = ((View)(getParent())).getScrollX();
		int headerHeight = Math.min(getHeight(), getWidth()) / 10;
		calendarAreaRect = new RectF(timeLabelWidth, headerHeight, getWidth(), getHeight() - 20); //getright is bad
		
		textPaint.setAntiAlias(true);
		//dayWidth = getWidth() / daysToShow;
		
		//day and week labels
		c.save();
		c.translate(timeLabelWidth, 0);
		c.translate(parentScroll, 0);
		c.clipRect(0,0,getWidth() - timeLabelWidth, headerHeight);
		c.translate(-parentScroll, 0);
		drawDayHeaders(c, getWidth() - timeLabelWidth, headerHeight);
		c.restore();
		
		
		//calendar markers and the grid
		c.save();
		c.translate(parentScroll, 0);
		c.clipRect(calendarAreaRect);
		c.translate(-parentScroll, 0);
		c.translate(timeLabelWidth, headerHeight);
		drawCalendarReservations(c, getWidth() - timeLabelWidth, getHeight() - headerHeight);
		drawReservationSubjects(c, getWidth() - timeLabelWidth, getHeight() - headerHeight);
		drawCalendarLines(c, getWidth() - timeLabelWidth, getHeight() - headerHeight);
		c.restore();
		
		//timeLabels on the left
		c.save();
		c.translate(0, headerHeight);
		drawTimeLabels(c, timeLabelWidth, getHeight() - headerHeight);
		c.restore();
		
		c.save();
		c.translate(calendarAreaRect.left, calendarAreaRect.top);
		drawFadingEdges(c, ((View)getParent()).getWidth() - (int)calendarAreaRect.left, (int)calendarAreaRect.height());
		c.restore();
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
	@Override
	public void onMeasure(int widthSpec, int heightSpec) {
		int width = Math.max(MeasureSpec.getSize(widthSpec), daysToShow	* dayWidth + timeLabelWidth);
		setMeasuredDimension(width, MeasureSpec.getSize(heightSpec));
	}
}
