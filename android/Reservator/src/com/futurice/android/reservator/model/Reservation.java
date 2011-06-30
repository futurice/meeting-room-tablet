package com.futurice.android.reservator.model;

import java.io.Serializable;

public class Reservation implements Comparable<Reservation>, Serializable {
	private static final long serialVersionUID = 1L;
	private TimeSpan timeSpan;
	private String subject;

	public Reservation(Room room, String subject, TimeSpan timeSpan){
		this.subject = subject;
		this.timeSpan = timeSpan;
	}

	public String getSubject(){
		return this.subject;
	}
	public DateTime getStartTime(){
		return timeSpan.getStart();
	}
	public DateTime getEndTime(){
		return timeSpan.getEnd();
	}

	@Override
	public int compareTo(Reservation another) {
		return (int)(this.getStartTime().getTimeInMillis() - another.getStartTime().getTimeInMillis());
	}

	public void setSubject(String text) {
		this.subject = text;

	}
}
