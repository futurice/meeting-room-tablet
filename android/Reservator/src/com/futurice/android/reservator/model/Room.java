package com.futurice.android.reservator.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
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
		Date now = new Date();
		for (Reservation r : reservations) {
			if (r.getBeginTime().before(now)) {
				if (r.getEndTime().after(now)) {
					return false;
				}
			} else {
				return true;
			}
		}
		return true;
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
