package com.futurice.android.reservator.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateTime implements Serializable {
	private static final long serialVersionUID = 1L;
	private Calendar cal;

	public DateTime() {
		cal = Calendar.getInstance();
	}

	public DateTime(Calendar cal) {
		this.cal = (Calendar) cal.clone();
	}

	private DateTime(Calendar cal, boolean clone) {
		if (clone) {
			this.cal = (Calendar) cal.clone();
		} else {
			this.cal = cal;
		}
	}

	public DateTime(Date date) {
		this.cal = Calendar.getInstance();
		this.cal.setTime(date);
	}

	public DateTime(long milliseconds) {
		this.cal = Calendar.getInstance();
		this.cal.setTimeInMillis(milliseconds);
	}

	public boolean after(DateTime other) {
		return cal.after(other.cal);
	}

	public boolean before(DateTime other) {
		return cal.before(other.cal);
	}

	public long getTimeInMillis() {
		return cal.getTimeInMillis();
	}

	public DateTime stripTime() {
		Calendar n = (Calendar) cal.clone();
		n.set(Calendar.HOUR, 0);
		n.set(Calendar.HOUR_OF_DAY, 0);
		n.set(Calendar.MINUTE, 0);
		n.set(Calendar.SECOND, 0);
		n.set(Calendar.MILLISECOND, 0);
		return new DateTime(n, false);
	}

	public DateTime stripMinutes() {
		Calendar n = (Calendar) cal.clone();
		n.set(Calendar.MINUTE, 0);
		n.set(Calendar.SECOND, 0);
		n.set(Calendar.MILLISECOND, 0);
		return new DateTime(n, false);
	}

	public boolean sameDay(DateTime other) {
		return this.cal.get(Calendar.DAY_OF_YEAR) == other.cal.get(Calendar.DAY_OF_YEAR)
				&& this.cal.get(Calendar.YEAR) == other.cal.get(Calendar.YEAR);
	}

	public int get(int field) {
		return cal.get(field);
	}

	public DateTime set(int field, int value) {
		Calendar n = (Calendar) cal.clone();
		n.set(field, value);
		return new DateTime(n, false);
	}

	public DateTime add(int field, int value) {
		Calendar n = (Calendar) cal.clone();
		n.add(field, value);
		return new DateTime(n, false);
	}

	public String toGMTString() {
		return cal.getTime().toGMTString();
	}

	public Date getTime() {
		return cal.getTime();
	}

	public DateTime setTime(int hours, int minutes, int seconds) {
		Calendar n = (Calendar) cal.clone();
		n.set(Calendar.HOUR_OF_DAY, hours);
		n.set(Calendar.MINUTE, minutes);
		n.set(Calendar.SECOND, seconds);
		n.set(Calendar.MILLISECOND, 0);
		return new DateTime(n, false);
	}
	public int subtract(DateTime o, int unit){
		switch(unit){
		default:
			throw new IllegalArgumentException("Not implemented with unit " + unit);
		}
	}
}
