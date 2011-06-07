package com.futurice.android.reservator.model;

import java.util.Calendar;



public class Reservation implements Comparable<Reservation> {
	private Calendar beginTime, endTime;
	private Room room;
	private boolean confirmed = false;
	public Reservation(Room room, Calendar beginTime, Calendar endTime){
		this.room = room;
		this.beginTime = beginTime;
		this.endTime = endTime;
	}
	
	public Calendar getBeginTime(){
		return beginTime;
	}
	public Calendar getEndTime(){
		return endTime;
	}
	public Room getRoom(){
		return room;
	}
	public boolean isConfirmed(){
		return confirmed;
	}
	public void setConfirmed(boolean confirmed){
		this.confirmed = confirmed;
	}

	@Override
	public int compareTo(Reservation another) {
		return (int)(this.beginTime.getTimeInMillis() - another.beginTime.getTimeInMillis());
	}
}
