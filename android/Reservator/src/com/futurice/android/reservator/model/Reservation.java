package com.futurice.android.reservator.model;

import java.io.Serializable;

public class Reservation implements Comparable<Reservation>, Serializable {
	private static final long serialVersionUID = 1L;

	final private String id;
	final private TimeSpan timeSpan;
	final private String subject;
	final long EQUAL_THRESHOLD = 2000;

	public Reservation(String id, String subject, TimeSpan timeSpan){
		this.id = id;
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

	public String getId() {
		return id;
	}

	public boolean equals(Reservation other) {
		return id.equals(other.id)
			|| ( Math.abs(getStartTime().getTimeInMillis() - other.getStartTime().getTimeInMillis()) < EQUAL_THRESHOLD
					&& Math.abs(getEndTime().getTimeInMillis() - other.getEndTime().getTimeInMillis()) < EQUAL_THRESHOLD);
	}

	@Override
	public int compareTo(Reservation another) {
		return (int)(this.getStartTime().getTimeInMillis() - another.getStartTime().getTimeInMillis());
	}
	
	@Override
	public String toString() {
		return subject + ": " + timeSpan; 
	}
}
