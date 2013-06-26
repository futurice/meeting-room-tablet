package com.futurice.android.reservator.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import com.futurice.android.reservator.common.Helpers;

public class Room implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name, email;
	private Vector<Reservation> reservations;
	private int capacity = -1;

	public Room(String name, String email) {
		this.name = name;
		this.email = email;
		this.reservations = new Vector<Reservation>();
	}

	//public Vector<Reservation> getReservations(){
	//	return this.reservations;
	//}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public void setReservations(Vector<Reservation> reservations){
		this.reservations = reservations;
		Collections.sort(reservations);
	}

	@Override
	public String toString() {
		return name; // + " " + (isFree() ? "(free)" : "(reserved)");
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
		
		Reservation withinThreshold = null;
		
		for (Reservation r : reservations) {
			if (r.getEndTime().after(now)) {
				if (r.getStartTime().before(now)) {
					return r;
				}
				if (r.getStartTime().before(bookingThresholdEnd)) {
					withinThreshold = r;
				}
			}
		}

		return withinThreshold;
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
	 *
	 * Prerequisite: isFree
	 * @return
	 */
	public TimeSpan getNextFreeTime(){
		DateTime now = new DateTime();
		DateTime max = now.add(Calendar.DAY_OF_YEAR, 1).stripTime();


		for (Reservation r : reservations) {
			// bound nextFreeTime to
			if (r.getStartTime().after(max)) {
				return new TimeSpan(now, max);
			}
			if(r.getStartTime().after(now)){
				return new TimeSpan(now, r.getStartTime()); //TODO maybe there are many reservations after each other..
			}
		}

		return null;
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

	// Rooms that are free for this long to future are considered "free" (bookable)
	static private final int FREE_THRESHOLD_MINUTES = 180;
	// Rooms that are free for no more than this long to future are considered "reserved" (not-bookable)
	static private final int RESERVED_THRESHOLD_MINUTES = 30;
	
	public boolean isBookable() {
		if (this.isFree()) {
			int freeMinutes = this.minutesFreeFromNow();

			if (freeMinutes > FREE_THRESHOLD_MINUTES) {
				return true;
			} else if (freeMinutes < RESERVED_THRESHOLD_MINUTES) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}
	
	// Returns true if room is free for a long period
	public boolean isLongBookable() {
		if (this.isFree()) {
			int freeMinutes = this.minutesFreeFromNow();

			if (freeMinutes > FREE_THRESHOLD_MINUTES) {
				return true;
			} else if (freeMinutes < RESERVED_THRESHOLD_MINUTES) {
				return false;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	public String getStatusText() {
		if (this.isFree()) {
			int freeMinutes = this.minutesFreeFromNow();

			if (freeMinutes > FREE_THRESHOLD_MINUTES) {
				return "Free";
			} else if (freeMinutes < RESERVED_THRESHOLD_MINUTES) {
				return "Reserved";
			} else {
				return "Free for " + Helpers.humanizeTimeSpan(freeMinutes);
			}
		} else {
			return "Reserved";
		}
	}
}
