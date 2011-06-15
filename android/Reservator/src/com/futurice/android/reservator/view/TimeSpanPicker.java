package com.futurice.android.reservator.view;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;


import com.futurice.android.reservator.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.view.View.OnClickListener;

public class TimeSpanPicker extends FrameLayout implements OnClickListener{
	private static final long TIME_STEP = 300000; //5min
	View plusButton, minusButton;
	TextView timeView, startLabel, endLabel;
	Date startTime;
	long timeSpan, endTime;
	public TimeSpanPicker(Context ctx){
		this(ctx, null);
	}
	
	public TimeSpanPicker(Context ctx, AttributeSet attrs){
		super(ctx, attrs);
		inflate(ctx, R.layout.time_picker, this);
		plusButton = findViewById(R.id.plusButton);
		plusButton.setOnClickListener(this);
		minusButton = findViewById(R.id.minusButton);
		minusButton.setOnClickListener(this);
		timeView = (TextView)findViewById(R.id.timeView);
		startLabel = (TextView)findViewById(R.id.startLabel);
		endLabel = (TextView)findViewById(R.id.endLabel);
	}

	@Override
	public void onClick(View v) {
		if(v == plusButton){
			alterTime(TIME_STEP);
		}else if(v == minusButton){
			alterTime(-TIME_STEP);
		}
	}

	public Date getEndTime(){
		return new Date(startTime.getTime() + timeSpan);
	}
	public Date getStartTime(){
		return startTime;
	}
	public void setMaxTime(Calendar time){
		endTime = time.getTimeInMillis();
	}
	/**
	 * Resets everything
	 * @param time
	 */
	public void setStartTime(Calendar time){
		startTime = time.getTime();
		endTime = time.getTimeInMillis();
		timeSpan = 0;
		updateTimeView();
	}
	private void alterTime(long delta){
		timeSpan += delta;
		if(timeSpan < 0){
			timeSpan = 0;
		}
		if(timeSpan > endTime - startTime.getTime()){
			timeSpan  = endTime - startTime.getTime();
		}
		updateTimeView();
	}
	private void updateTimeView(){
		
		timeView.setText(String.format("%dh %dm", timeSpan / 3600000, (timeSpan % 3600000) / 60000));
		
		SimpleDateFormat df = new SimpleDateFormat("hh:mm");
		endLabel.setText(df.format(new Date(timeSpan + startTime.getTime())));
		startLabel.setText(df.format(startTime));
		
	}
}
