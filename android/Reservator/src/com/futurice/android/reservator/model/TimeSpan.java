package com.futurice.android.reservator.model;

public class TimeSpan {
	private DateTime start, end;

	public TimeSpan(DateTime start, DateTime end){
		this.start = start;
		this.end = end;
		if(start.after(end)){
			throw new IllegalArgumentException("No negative time allowed");
		}
	}
	public TimeSpan(DateTime start, int units, int count){
		if(count < 0){
			throw new IllegalArgumentException("No negative time allowed");
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
			throw new IllegalArgumentException("No negative time allowed");
		}
	}
	public final DateTime getEnd() {
		return end;
	}
	public void setEnd(DateTime end) {
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
		return new TimeSpan(start, end);
	}
}
