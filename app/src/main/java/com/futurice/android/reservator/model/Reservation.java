package com.futurice.android.reservator.model;

import java.io.Serializable;

public class Reservation implements Comparable<Reservation>, Serializable {
	private static final long serialVersionUID = 1L;

	final private String id;
	final private TimeSpan timeSpan;
	final private String subject;
	private boolean cancellable = false;

	public Reservation(String id, String subject, TimeSpan timeSpan){
		this.id = id;
		this.subject = subject;
		this.timeSpan = timeSpan;
	}

	public String getSubject(){
		return this.subject;
	}

	public TimeSpan getTimeSpan(){
		return timeSpan;
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
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Reservation) {
			return equals((Reservation) other);
		}
		return super.equals(other);
	}
	
	public boolean equals(Reservation other) {
		return id.equals(other.id);
	}

	@Override
	public int compareTo(Reservation another) {
		return (int)(this.getStartTime().getTimeInMillis() - another.getStartTime().getTimeInMillis());
	}
	
	@Override
	public String toString() {
		return "Reservation<" + id + "," + hashCode() + "> " + subject + ": " + timeSpan; 
	}
	
	public void setIsCancellable(final boolean value) {
		this.cancellable = value;
	}
	
	public boolean isCancellable() {
		return this.cancellable;
	}
}
