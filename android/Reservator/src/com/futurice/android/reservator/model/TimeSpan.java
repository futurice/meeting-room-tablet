package com.futurice.android.reservator.model;

import java.util.Calendar;

public class TimeSpan {
	private Calendar start, end;

	public TimeSpan(Calendar start, Calendar end){
		this.start = (Calendar)start.clone();
		this.end = (Calendar)end.clone();
		if(start.after(end)){
			throw new IllegalArgumentException("No negative time allowed");
		}
	}
	public TimeSpan(Calendar start, int units, int count){
		if(count < 0){
			throw new IllegalArgumentException("No negative time allowed");
		}
		if(start == null){
			start = Calendar.getInstance();
		}
		this.start = start;
		this.end = (Calendar)start.clone();
		this.end.add(units, count);
		if(this.start.after(this.end)){
			throw new IllegalArgumentException("No negative time allowed");
		}
	}
	public final Calendar getStart(){
		return start;
	}
	public void setStart(Calendar start){

		this.start = start;
		if(start.after(end)){
			throw new IllegalArgumentException("No negative time allowed");
		}
	}
	public final Calendar getEnd() {
		return end;
	}
	public void setEnd(Calendar end) {
		this.end = end;
		if(start.after(end)){
			throw new IllegalArgumentException("No negative time allowed");
		}
	}
	public long getLength(){
		long startMillis = start.getTimeInMillis();
		long endMillis = end.getTimeInMillis();
		return endMillis - startMillis;
	}
	@Override
	public TimeSpan clone(){
		return new TimeSpan((Calendar)start.clone(), (Calendar)end.clone());
	}
}
