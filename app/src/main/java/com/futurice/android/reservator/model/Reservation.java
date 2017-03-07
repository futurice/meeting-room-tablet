package com.futurice.android.reservator.model;

import java.io.Serializable;
import java.util.Vector;

public class Reservation implements Comparable<Reservation>, Serializable {
    private static final long serialVersionUID = 1L;

    final private String id;
    private TimeSpan timeSpan;
    private String subject;
    private boolean cancellable = false;
    private Vector<String> attendees;
    private long createdAt;

    public Reservation(String id, String subject, TimeSpan timeSpan) {
        this(id,subject, timeSpan,null,System.currentTimeMillis());
    }

    public Reservation(String id, String subject, TimeSpan timeSpan,Vector<String> attendees, long createdAt) {
        this.id = id;
        this.subject = subject;
        this.timeSpan = timeSpan;
        this.attendees = attendees;
        this.createdAt = createdAt;
    }

    public String getSubject() {
        return this.subject;
    }

    public TimeSpan getTimeSpan() {
        return timeSpan;
    }

    public DateTime getStartTime() {
        return timeSpan.getStart();
    }

    public DateTime getEndTime() {
        return timeSpan.getEnd();
    }

    public String getId() {
        return id;
    }

    public Vector<String> getAttendees (){
        return attendees;
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
        return (int) (this.getStartTime().getTimeInMillis() - another.getStartTime().getTimeInMillis());
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

    public void setTimeSpan(TimeSpan timeSpan) {
        this.timeSpan = timeSpan;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
