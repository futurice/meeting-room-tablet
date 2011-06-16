package com.futurice.android.reservator.view;

import java.util.Calendar;
import java.util.GregorianCalendar;

import com.futurice.android.reservator.R;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.view.View.OnClickListener;

public class CustomTimePicker extends FrameLayout implements OnClickListener{
	
	private static final int TIME_STEP = 30, TIME_UNIT = Calendar.MINUTE;
	View plusButton, minusButton;
	TextView timeView, startLabel, endLabel;
	Calendar minTime, maxTime, time = new GregorianCalendar(1970,1,1,0,0,0);
	private OnTimeChangedListener listener = null;
	public CustomTimePicker(Context ctx){
		this(ctx, null);
	}
	
	public CustomTimePicker(Context ctx, AttributeSet attrs){
		super(ctx, attrs);
		inflate(ctx, R.layout.time_picker, this);
		plusButton = findViewById(R.id.plusButton);
		plusButton.setOnClickListener(this);
		minusButton = findViewById(R.id.minusButton);
		minusButton.setOnClickListener(this);
		timeView = (TextView)findViewById(R.id.timeView);
	}
	
	@Override
	public void onClick(View v) {
		if(v == plusButton){
			alterTime(TIME_UNIT, TIME_STEP);
		}else if(v == minusButton){
			alterTime(TIME_UNIT, -TIME_STEP);
		}
	}
	public Calendar getMaxTime(){
		return maxTime;
	}
	public Calendar getMinTime(){
		return minTime;
	}
	public void setMaxTime(Calendar time){
		this.maxTime = time;
		if(time.after(maxTime)){
			setTime(time);
		}
	}
	public void setMinTime(Calendar time){
		this.minTime = time;
		if(this.time.before(minTime)){
			setTime(time);
		}
	}
	public void setTime(Calendar time){
		this.time = (Calendar)time.clone();
		updateTimeView();
	}
	public Calendar getTime(){
		return this.time;
	}
	
	private void alterTime(int units, int value){
		time.add(units, value);
		if(time.after(maxTime)){
			time.setTime(maxTime.getTime());
		}else if(time.before(minTime)){
			time.setTime(minTime.getTime());
		}
		updateTimeView();
		if(listener != null){
			listener.onTimeChanged(this, time);
		}
	}
	private void updateTimeView(){
		setText(DateFormat.format("kk:mm", time));
	}
	private void setText(CharSequence txt){
		timeView.setText(txt);
	}
	public void setOnTimeChangedListener(OnTimeChangedListener listener){
		this.listener = listener;
	}
	public interface OnTimeChangedListener{
		public void onTimeChanged(CustomTimePicker v, Calendar newTime);
	}
}
