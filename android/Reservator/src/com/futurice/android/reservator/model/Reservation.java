package com.futurice.android.reservator.model;

import java.util.Calendar;

public class Reservation implements Comparable<Reservation> {
	private TimeSpan timeSpan;
	private Room room;
	private String subject;
	private boolean confirmed = false;

	public Reservation(Room room, String subject, TimeSpan timeSpan){
		this.room = room;
		this.subject = subject;
		this.timeSpan = timeSpan;
	}

	public String getSubject(){
		return this.subject;
	}
	public Calendar getStartTime(){
		return timeSpan.getStart();
	}
	public Calendar getEndTime(){
		return timeSpan.getEnd();
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
		return (int)(this.getStartTime().getTimeInMillis() - another.getStartTime().getTimeInMillis());
	}

	public void setSubject(String text) {
		this.subject = text;

	}
}
