package com.futurice.android.reservator.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class Room {
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
		Calendar now = Calendar.getInstance();
		for (Reservation r : reservations) {
			if (r.getBeginTime().before(now) && r.getEndTime().after(now)) {
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
	public int minutesFreeFrom(Calendar from) {
		for (Reservation r : reservations) {
			if (r.getBeginTime().after(from)) {
				return (int) (r.getBeginTime().getTimeInMillis() - from.getTimeInMillis()) / 60000;
			}
		}

		return Integer.MAX_VALUE;
	}

	public int minutesFreeFromNow() {
		return minutesFreeFrom(Calendar.getInstance());
	}


	/**
	 * Time in minutes room is reserved
	 * Precondition: room is reserved
	 *
	 * @param from
	 * @return
	 */
	public int reservedForFrom(Calendar from) {
		Calendar to = (Calendar) from.clone();
		for (Reservation r : reservations) {
			if (r.getBeginTime().before(to) && r.getEndTime().after(to)) {
				to = (Calendar) r.getEndTime().clone();
				to.add(Calendar.MINUTE, 5);
			}
		}

		return (int) (to.getTimeInMillis() - from.getTimeInMillis()) / 60000;
	}

	public int reservedForFromNow() {
		return reservedForFrom(Calendar.getInstance());
	}

	/**
	 *
	 * Prerequisite: isFree
	 * @return
	 */
	public TimeSpan getNextFreeTime(){
		Calendar now = Calendar.getInstance();
		Calendar max = Calendar.getInstance();

		max.roll(Calendar.DAY_OF_WEEK, 1);
		max.set(Calendar.HOUR_OF_DAY, 0);
		max.set(Calendar.MINUTE, 0);
		max.set(Calendar.SECOND, 0);
		max.set(Calendar.MILLISECOND, 0);

		for (Reservation r : reservations) {
			// bound nextFreeTime to
			if (r.getBeginTime().after(max)) {
				return new TimeSpan(now, max);
			}
			if(r.getBeginTime().after(now)){
				return new TimeSpan(now, r.getBeginTime()); //TODO maybe there are many reservations after each other..
			}
		}

		return null;
	}


	public List<Reservation> getReservationsForDay(Calendar day) {
		List<Reservation> daysReservations = new ArrayList<Reservation>();
		for (Reservation r : reservations) {
			if (r.getBeginTime().get(Calendar.DAY_OF_YEAR) == day
					.get(Calendar.DAY_OF_YEAR)
					&& r.getBeginTime().get(Calendar.YEAR) == day
							.get(Calendar.YEAR)) {
				daysReservations.add(r);
			}
		}
		return daysReservations;
	}

	public boolean equals(Room room) {
		return email.equals(room.getEmail());
	}
}
