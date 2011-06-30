package com.futurice.android.reservator.model;

import java.io.Serializable;

public class TimeSpan implements Serializable {
	private static final long serialVersionUID = 1L;
	private DateTime start, end;

	public TimeSpan(DateTime start, DateTime end){
		this.start = start;
		this.end = end;
		if(start.after(end)){
			throw new IllegalArgumentException("No negative time allowed -- " + start.toGMTString() + " - " + end.toGMTString());
		}
	}
	public TimeSpan(DateTime start, int units, int count){
		if(count < 0){
			throw new IllegalArgumentException("No negative time allowed -- count: " + count);
		}
		if(start == null){
			start = new DateTime();
		}
		this.start = start;
		this.end = start.later(units, count);
		if(this.start.after(this.end)){
			throw new IllegalArgumentException("No negative time allowed");
		}
	}
	public final DateTime getStart(){
		return start;
	}
	public void setStart(DateTime start){

		this.start = start;
		if(start.after(end)){
			throw new IllegalArgumentException("No negative time allowed -- setStart");
		}
	}
	public final DateTime getEnd() {
		return end;
	}
	public void setEnd(DateTime end) {
		this.end = end;
		if(start.after(end)){
			throw new IllegalArgumentException("No negative time allowed -- setEnd");
		}
	}
	public long getLength(){
		long startMillis = start.getTimeInMillis();
		long endMillis = end.getTimeInMillis();
		return endMillis - startMillis;
	}

	@Override
	public TimeSpan clone(){
		return new TimeSpan(start, end);
	}
}
