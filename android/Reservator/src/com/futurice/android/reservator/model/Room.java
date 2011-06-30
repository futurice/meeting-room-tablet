package com.futurice.android.reservator.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class Room implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name, email;
	private Vector<Reservation> reservations;

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
				return (int) (r.getStartTime().getTimeInMillis() - from.getTimeInMillis()) / 60000;
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
				to = to.later(Calendar.MINUTE, 5);
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
		DateTime max = now.later(Calendar.DAY_OF_YEAR, 1).stripTime();


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


	public List<Reservation> getReservationsForDay(DateTime day) {
		List<Reservation> daysReservations = new ArrayList<Reservation>();
		for (Reservation r : reservations) {
			if (r.getStartTime().sameDay(day)) {
				daysReservations.add(r);
			}
		}
		return daysReservations;
	}

	public boolean equals(Room room) {
		return email.equals(room.getEmail());
	}
}
