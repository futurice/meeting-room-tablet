package com.futurice.android.reservator.model.dummy;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import com.futurice.android.reservator.model.DataProxy;
import com.futurice.android.reservator.model.Reservation;
import com.futurice.android.reservator.model.Room;

public class DummyDataProxy implements DataProxy {
	List<Room> rooms = null;
	List<Reservation> reservations = null;

	@Override
	public void init() {
		this.rooms = new ArrayList<Room>();
		this.reservations = new ArrayList<Reservation>();

		for (int i = 0; i < 10; i++) {
			Room room = new Room("Room " + 400 + i, "futu.naut@futurice.com",
					null);
			generateReservationsForRoom(room);
			rooms.add(room);
		}
	}
	
	@Override
	public void deinit() {
		this.rooms = null;
		this.reservations = null;
	}

	@Override
	public List<Room> getRooms() {
		return rooms;
	}

	@Override
	public List<Reservation> getReservations() {
		return reservations;
	}

	@Override
	public boolean reserve(Reservation r) {
		reservations.add(r);
		r.setConfirmed(true);
		return true;
	}

	private void generateReservationsForRoom(Room room) {
		Random rand = new Random();
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 8);
		for (int i = 0; i < 10; i++) {
			long begin = cal.getTimeInMillis();
			long end = begin + 10 * 60 * 60 * 1000;

			while (begin < end) {
				int reservationLength = rand.nextInt(7200000) + 1;
				Calendar b = Calendar.getInstance();
				b.setTimeInMillis(begin);
				Calendar e = Calendar.getInstance();
				e.setTimeInMillis(begin + reservationLength > end ? end : begin + reservationLength);
				Reservation r = new Reservation(room, b, e);
				reservations.add(r);
				room.addReservation(r);
				begin += reservationLength + rand.nextInt(7200000) + 1;
			}
			cal.add(Calendar.DAY_OF_YEAR, 1);
		}
	}

	@Override
	public void setCredentials(String user, String password) {
		// Do nothing	
	}
}
