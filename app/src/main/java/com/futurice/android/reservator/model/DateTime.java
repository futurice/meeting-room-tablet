package com.futurice.android.reservator.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

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

    public boolean afterOrEqual(DateTime other) {
        return cal.compareTo(other.cal) >= 0;
    }

    public boolean beforeOrEqual(DateTime other) {
        return cal.compareTo(other.cal) <= 0;
    }

    public int compareTo(DateTime other) {
        return cal.compareTo(other.cal);
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

    public long subtract(DateTime o, int unit) {
        switch (unit) {
            case Calendar.DAY_OF_YEAR:
                return (this.getTimeInMillis() - o.getTimeInMillis()) / (1000 * 60 * 60 * 24);
            case Calendar.MILLISECOND:
                return this.getTimeInMillis() - o.getTimeInMillis();
            default:
                throw new IllegalArgumentException("Not implemented with unit " + unit);
        }
    }

    public boolean equals(long o) {
        return this.getTimeInMillis() == o;
    }

    @Override
    public String toString() {
        return cal.getTime().toString();
        //return toGMTString();
    }

    public Date getDate(int value, boolean doRoundTime){
        if (doRoundTime){
            return new Date(this.get(Calendar.YEAR),this.get(Calendar.MONTH),this.get(Calendar.DAY_OF_YEAR),this.get(Calendar.HOUR)+value,roundTime(this.get(Calendar.MINUTE)));
        } else {
            return new Date(this.get(Calendar.YEAR),this.get(Calendar.MONTH),this.get(Calendar.DAY_OF_YEAR),this.get(Calendar.HOUR)+value,this.get(Calendar.MINUTE));
        }
    }

    public int roundTime(int min) {
        int counter = min/15;
        min= min%15;
        if(min==0 && counter==0){
            return 0;
        }

        if (counter == 0 || (counter == 1 && min == 0)){
            return 15;
        }

        if (counter >= 1 && min < 15){
            if (min == 0){
                return counter*15;
            } else {
                counter = counter + 1;
                return counter*15;
            }
        }
        return 0;
    }

}
