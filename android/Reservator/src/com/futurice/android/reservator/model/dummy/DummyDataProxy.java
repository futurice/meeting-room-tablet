package com.futurice.android.reservator.model.dummy;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;


import com.futurice.android.reservator.model.DataProxy;
import com.futurice.android.reservator.model.Reservation;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.Room;

public class DummyDataProxy implements DataProxy {
	List<Room> rooms = null;
	List<Reservation> reservations = null;

	@Override
	public void init() {
		this.rooms = new ArrayList<Room>();
		this.reservations = new ArrayList<Reservation>();

		for (int i = 0; i < 10; i++) {
			Room room = new Room("Room " + 400 + i, "futu.naut"+i+"@futurice.com", this);
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
	public List<Reservation> getReservations() throws ReservatorException {
		throw new ReservatorException("not implemented"); // need to fix getRoomReservation to keep data consistent
		// return reservations;
	}

	@Override
	public boolean reserve(Reservation r) {
		reservations.add(r);
		r.setConfirmed(true);
		return true;
	}

	@Override
	public void setCredentials(String user, String password) {
		// Do nothing
	}

	@Override
	public List<Reservation> getRoomReservations(Room room) {
		List<Reservation> ret = new ArrayList<Reservation>();
		Random rand = new Random();
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 8);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);

		int reservationCount = rand.nextInt(5) + 5;
		for (int i = 0; i < reservationCount; i++) {

			long begin = cal.getTimeInMillis();
			long end = begin + 10 * 60 * 60 * 1000;

			begin += (rand.nextInt(4)+1)*1800000;

			while (begin < end) {
				int reservationLength = (rand.nextInt(4)+1)*1800000;
				Calendar b = Calendar.getInstance();
				b.setTimeInMillis(begin);
				Calendar e = Calendar.getInstance();
				e.setTimeInMillis(begin + reservationLength > end ? end : begin + reservationLength);

				Reservation r = new Reservation(room, "foobar!", b, e);
				ret.add(r);
				begin += reservationLength + (rand.nextInt(6))*1800000;
			}
			cal.add(Calendar.DAY_OF_YEAR, 1);
		}

		return ret;
	}
}
