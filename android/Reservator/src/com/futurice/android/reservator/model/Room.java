package com.futurice.android.reservator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class Room {
	private String name, email;
	private List<Reservation> reservations;

	public Room(String name, String email, List<Reservation> reservations) {
		this.name = name;
		this.email = email;
		if (reservations == null) {
			this.reservations = new ArrayList<Reservation>();
		} else {
			this.reservations = reservations;
		}
	}
	
	public List<Reservation> getReservations(boolean forceRefresh) throws ReservatorException {
		return this.reservations;
	}

	public void addReservation(Reservation r) {
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
		return name + " " + (isFree() ? "(free)" : "(reserved)");
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
}
