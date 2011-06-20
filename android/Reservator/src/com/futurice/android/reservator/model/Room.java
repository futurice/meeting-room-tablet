package com.futurice.android.reservator.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class Room {
	private String name, email;
	private List<Reservation> reservations;
	private DataProxy dataProxy;

	public Room(String name, String email, DataProxy dataProxy) {
		this.name = name;
		this.email = email;
		this.dataProxy = dataProxy;
		this.reservations = new ArrayList<Reservation>();
	}

	public List<Reservation> getReservations(boolean forceRefresh) throws ReservatorException {
		if (forceRefresh || this.reservations == null) {
			reservations = dataProxy.getRoomReservations(this);
		}
		if (reservations == null) {
			reservations = new ArrayList<Reservation>();
		}
		Collections.sort(reservations);
		return this.reservations;
	}

	public void addReservation(Reservation r) {
		// TODO: tell data proxy
		// should this take start, begin time and reservator's name as parameters?

		this.reservations.add(r);
		Collections.sort(this.reservations);
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
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

	public Reservation getNextFreeTime(){
		Calendar now = Calendar.getInstance();
		for (Reservation r : reservations) {
			if(r.getBeginTime().after(now)){
				return new Reservation(this, "free", now, r.getBeginTime()); //TODO maybe there are many reservations after each other..
			}
		}
		return null;
	}
}
