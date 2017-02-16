package com.futurice.android.reservator.model;

import android.content.Context;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.common.Helpers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

public class Room implements Serializable {
    // Rooms that are free for no more than this long to future are considered "reserved" (not-bookable)
    static public final int RESERVED_THRESHOLD_MINUTES = 15;
    private static final long serialVersionUID = 1L;
    // Rooms that are free for this long to future are considered "free"
    static private final int FREE_THRESHOLD_MINUTES = 180;
    private String name, email;
    private Vector<Reservation> reservations;
    private String shownRoomName;
    private int capacity = -1;

    public Room(String name, String email) {
        this.name = name;
        this.email = email;
        this.reservations = new Vector<Reservation>();
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public void setReservations(Vector<Reservation> reservations) {
        this.reservations = reservations;
        Collections.sort(reservations);
        setShownRoomName();
    }

    public String getShownRoomName(){
        return shownRoomName;
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean isFree() {
        DateTime now = new DateTime();
        for (Reservation r : reservations) {
            if (r.getStartTime().before(now) && r.getEndTime().after(now)) {
                return false;
            }
        }

        return true;
    }

    public Reservation getCurrentReservation() {
        DateTime now = new DateTime();
        DateTime bookingThresholdEnd = now.add(Calendar.MINUTE, RESERVED_THRESHOLD_MINUTES);

        for (Reservation r : reservations) {
            if (r.getEndTime().after(now) && r.getStartTime().before(bookingThresholdEnd)) {
                return r;
            }
        }

        return null;
    }

    /**
     * Time in minutes the room is free
     * Precondition: room is free
     *
     * @param from
     * @return Integer.MAX_VALUE if no reservations in future
     */
    public int minutesFreeFrom(DateTime from) {
        for (Reservation r : reservations) {
             if (r.getStartTime().after(from)) {
                return (int) ((r.getStartTime().getTimeInMillis() - from.getTimeInMillis()) / 60000);
            }
        }

        return Integer.MAX_VALUE;
    }

    public int minutesFreeFromNow() {
        return minutesFreeFrom(new DateTime());
    }

    /**
     * Time in minutes room is reserved
     * Precondition: room is reserved
     *
     * @param from
     * @return
     */
    public int reservedForFrom(DateTime from) {
        DateTime to = from;

        for (Reservation r : reservations) {
            if (r.getStartTime().before(to) && r.getEndTime().after(to)) {
                to = r.getEndTime();
                to = to.add(Calendar.MINUTE, 5);
            }
        }

        return (int) (to.getTimeInMillis() - from.getTimeInMillis()) / 60000;
    }

    public int reservedForFromNow() {
        return reservedForFrom(new DateTime());
    }

    /**
     * Prerequisite: isFree
     *
     * @return
     */
    public TimeSpan getNextFreeTime() {
        DateTime now = new DateTime();
        DateTime max = now.add(Calendar.DAY_OF_YEAR, 1).stripTime();


        for (Reservation r : reservations) {
            // bound nextFreeTime to
            if (r.getStartTime().after(max)) {
                return new TimeSpan(now, max);
            }
            if (r.getStartTime().after(now)) {
                return new TimeSpan(now, r.getStartTime()); //TODO maybe there are many reservations after each other..
            }
        }

        return null;
    }

    /**
     * Prerequisites: None
     *
     * @param length Required slot length in minutes.
     * @return Next free slot today of at least the required length, if any.
     */
    public TimeSpan getNextFreeSlot(int length) {
        DateTime now = new DateTime();
        DateTime max = now.add(Calendar.DAY_OF_YEAR, 1).stripTime();

        int roundedMin = now.roundTime(now.get(Calendar.MINUTE)) - now.get(Calendar.MINUTE);
        DateTime dateTime = now.add(Calendar.MINUTE, length + roundedMin);
        TimeSpan candidateSlot = new TimeSpan(now, dateTime);

        for (Reservation r : reservations) {
            if (r.getStartTime().afterOrEqual(candidateSlot.getEnd())) return candidateSlot;
            if (r.getTimeSpan().intersects(candidateSlot)) {
                if (r.getEndTime().afterOrEqual(max)) return null;
                candidateSlot = new TimeSpan(r.getEndTime(), r.getEndTime().add(Calendar.MINUTE, length));
            }
        }

        return candidateSlot;
    }

    public TimeSpan getNextFreeSlot() {
        return getNextFreeSlot(RESERVED_THRESHOLD_MINUTES);
    }

    public List<Reservation> getReservationsForTimeSpan(TimeSpan ts) {
        List<Reservation> daysReservations = new ArrayList<Reservation>();
        for (Reservation r : reservations) {
            if (r.getTimeSpan().intersects(ts)) {
                daysReservations.add(r);
            }
        }
        return daysReservations;
    }

    @Override
    public int hashCode() {
        return email.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Room) {
            return equals((Room) other);
        }
        return super.equals(other);
    }

    public boolean equals(Room room) {
        return email.equals(room.getEmail());
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    /**
     * @param threshold Minimum duration of reservation.
     * @return If room can be immediately booked with threshold long reservation.
     */
    public boolean isBookable(final int threshold) {
        if (this.isFree()) {
            if (this.minutesFreeFromNow() < threshold) {
                return false;
            }
            return true;
        }
        return false;
    }

    public boolean isBookable() {
        return isBookable(RESERVED_THRESHOLD_MINUTES);
    }

    /**
     * Prerequisite: isFree
     *
     * @return true if room is continuously free now and for the rest of the day
     */
    public boolean isFreeRestOfDay() {
        DateTime now = new DateTime();
        DateTime max = now.add(Calendar.DAY_OF_YEAR, 1).stripTime();

        TimeSpan restOfDay = new TimeSpan(now, max);

        for (Reservation r : reservations) {
            if (r.getTimeSpan().intersects(restOfDay)) return false;
            if (r.getStartTime().after(max)) return true;
        }

        return true;
    }

    public String getStatusText(Context context) {
        if (this.isFree()) {
            int freeMinutes = this.minutesFreeFromNow();

            if (freeMinutes > FREE_THRESHOLD_MINUTES) {
                return context.getString(R.string.free);
            } else if (freeMinutes < RESERVED_THRESHOLD_MINUTES) {
                return context.getString(R.string.defaultTitleForReservation);
            } else {
                return String.format("%s %s", context.getString(R.string.freeFor), Helpers.humanizeTimeSpan(freeMinutes, context));
            }
        } else {
            return context.getString(R.string.defaultTitleForReservation);
        }
    }

    private long getTimeDifference(int hours, int minutes, Date lastTimeConnected) {
        DateTime reservEndTime = new DateTime().setTime(hours,minutes,0);
        return reservEndTime.subtract(new DateTime(lastTimeConnected.getTime()),Calendar.MILLISECOND);

    }

    public long getTimeDifferenceHour(Date lastTimeConnected) {
        int hours = getNextFreeSlot().getStart().get(Calendar.HOUR_OF_DAY);
        int minutes = getNextFreeSlot().getStart().get(Calendar.MINUTE);

        return (TimeUnit.MILLISECONDS.toHours(getTimeDifference(hours, minutes,lastTimeConnected)) % 24);
    }

    public long getTimeDifferenceMinute(Date lastTimeConnected) {
        int hours = getNextFreeSlot().getStart().get(Calendar.HOUR_OF_DAY);
        int minutes = getNextFreeSlot().getStart().get(Calendar.MINUTE);

        long timeDifference = getTimeDifference(hours, minutes,lastTimeConnected);
        timeDifference -= TimeUnit.HOURS.toMillis(getTimeDifferenceHour(lastTimeConnected));

        return TimeUnit.MILLISECONDS.toMinutes(timeDifference) % 60;
    }

    private void setShownRoomName() {
        if (!reservations.isEmpty()) {
            Vector<String> attendees = reservations.get(0).getAttendees();
            String[] nameList;

            for (Object attendee : attendees) {
                String name = attendee.toString();
                nameList = name.split(" ");

                if (nameList.length < 2) {
                    if (!nameList[0].contains("@")) {
                        shownRoomName = name;
                        break;
                    }
                }
            }
        }

        if (isShownNameSetToName()){
            shownRoomName =  name;
        }
    }

    private boolean isShownNameSetToName() {
        if (shownRoomName == null || name.split(" ").length == 2){
            if (shownRoomName == null|| name.split(" ")[0].equals("10") || name.split(" ")[1].contains("Ecke")){
                return true;
            }
        }
        return false;
    }
}
