package com.futurice.android.reservator.model;

import java.io.Serializable;

public class Reservation implements Comparable<Reservation>, Serializable {
	private static final long serialVersionUID = 1L;

	final private String id;
	final private TimeSpan timeSpan;
	final private String subject;

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
		return id.equals(other.id);
	}

	@Override
	public int compareTo(Reservation another) {
		return (int)(this.getStartTime().getTimeInMillis() - another.getStartTime().getTimeInMillis());
	}
}
