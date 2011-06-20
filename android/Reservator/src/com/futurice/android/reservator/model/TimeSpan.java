package com.futurice.android.reservator.model;

import java.util.Calendar;

public class TimeSpan {
	Calendar start, end;
	public TimeSpan(Calendar start, Calendar end){
		this.start = start;
		this.end = end;
	}
	public TimeSpan(Calendar start, int units, int count){
		if(start == null){
			start = Calendar.getInstance();
		}
		this.start = start;
		this.end = (Calendar)start.clone();
		this.end.add(units, count);
	}
	public Calendar getStart(){
		return start;
	}
	public void setStart(Calendar start){
		this.start = start;
	}
	public Calendar getEnd() {
		return end;
	}
	public void setEnd(Calendar end) {
		this.end = end;
	}
	public long getLength(){
		return end.getTimeInMillis() - start.getTimeInMillis();
	}
	@Override
	public TimeSpan clone(){
		return new TimeSpan(start, end);
	}
}
