package com.futurice.android.reservator.model;

import java.util.Calendar;

public class TimeSpan {
	Calendar start, end;
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
}
