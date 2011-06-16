package com.futurice.android.reservator.view;

import java.util.Calendar;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.view.CustomTimePicker.OnTimeChangedListener;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class CustomTimeSpanPicker extends FrameLayout implements OnTimeChangedListener{

	CustomTimePicker start, end;
	
	public CustomTimeSpanPicker(Context context){
		this(context, null);
	}
	public CustomTimeSpanPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.timespan_picker, this);
		start = (CustomTimePicker)findViewById(R.id.startTimePicker);
		start.setOnTimeChangedListener(this);
		end = (CustomTimePicker)findViewById(R.id.endTimePicker);
		end.setOnTimeChangedListener(this);
	}
	public void setMinTime(Calendar time){
		start.setMinTime(time);
		end.setMinTime(time);
	}
	public void setMaxTime(Calendar time){
		start.setMaxTime(time);
		end.setMaxTime(time);
	}
	public Calendar getStartTime(){
		return start.getTime();
	}
	public Calendar getEndTime(){
		return end.getTime();
	}
	@Override
	public void onTimeChanged(CustomTimePicker v, Calendar newTime) {
		if(v == end){
			start.setMaxTime(newTime);
		}else if(v == start){
			end.setMinTime(newTime);
		}
	}
}
