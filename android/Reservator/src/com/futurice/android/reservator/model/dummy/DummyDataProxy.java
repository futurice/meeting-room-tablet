package com.futurice.android.reservator.model.dummy;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Vector;


import com.futurice.android.reservator.model.DataProxy;
import com.futurice.android.reservator.model.Reservation;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.Room;
import com.futurice.android.reservator.model.TimeSpan;

public class DummyDataProxy extends DataProxy {
	Vector<Room> rooms = null;
	Map<String,Vector<Reservation>> reservations;
	public DummyDataProxy() {
		this.rooms = new Vector<Room>();
		this.reservations = Collections.synchronizedMap(new HashMap<String,Vector<Reservation>>());

		rooms.add(new Room("Room-Panorama-401", "401-Panorama@futu.com"));
		rooms.add(new Room("Room-Pilotti-402", "402-Pilotti@futu.com"));
		rooms.add(new Room("Room-Vauhtimato-403", "403-vauhtimato@futu.com"));
		rooms.add(new Room("Room-Space Shot-404", "404-Spaceshot@futu.com"));
		rooms.add(new Room("Project-Regatta-405", "405-Regatta@futu.com"));
		rooms.add(new Room("Project-Kino-406", "406-Kino@futu.com"));
		rooms.add(new Room("Project-Vekkula-407", "407-Vekkula@futu.com"));
		rooms.add(new Room("Project-Metkula-408", "408-Metkula@futu.com"));

		rooms.add(new Room("Project-Verstas-506", "506-Verstas@futu.com"));
		rooms.add(new Room("Room-Kenk�kauppa-503", "503-Kenkakauppa@futu.com"));
		rooms.add(new Room("Room-Koivumets�-504", "504-Koivumetsa@futu.com"));
		rooms.add(new Room("Room-Merineukkari-502", "502-Merineukkari@futu.com"));
		rooms.add(new Room("Room-Pikku Neukkari-501", "501-pikkuneukkari@futu.com"));

		Collections.sort(rooms, new Comparator<Room>() {
			@Override
			public int compare(Room room1, Room room2) {
				return room1.getEmail().compareTo(room2.getEmail());
			}
		});
	}

	@Override
	public Vector<Room> getRooms() {
		return rooms;
	}

	@Override
	public void reserve(Room room, TimeSpan timeSpan, String ownerEmail) throws ReservatorException {
		// TODO: check for availability

		Reservation reservation = new Reservation(room, "reserved with FutuReservator5000", timeSpan);
		Vector<Reservation> roomReservations = reservations.get(room.getEmail());
		if (roomReservations == null) {
			throw new ReservatorException("unknown room");
		}
		roomReservations.add(reservation);
	}

	@Override
	public void setCredentials(String user, String password) {
		// Do nothing
	}

	@Override
	public void setServer(String server) {
		// Do nothing
	}

	@Override
	public Vector<Reservation> getRoomReservations(Room room) {
		try {
			Thread.sleep((int)(Math.random()*10));
		} catch (InterruptedException e1) {}

		Vector<Reservation> ret = reservations.get(room.getEmail());
		if (ret != null) return ret; // found in cache

		ret = new Vector<Reservation>();
		Random rand = new Random();
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 8);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

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

				Reservation r = new Reservation(room, "foobar!", new TimeSpan(b, e));
				ret.add(r);
				begin += reservationLength + (rand.nextInt(6))*1800000;
			}
			cal.add(Calendar.DAY_OF_YEAR, 1);
		}

		reservations.put(room.getEmail(), ret); // put into the cache

		return ret;
	}
}
