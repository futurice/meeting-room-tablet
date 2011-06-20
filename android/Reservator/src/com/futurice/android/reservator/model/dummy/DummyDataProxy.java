package com.futurice.android.reservator.model.dummy;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


import com.futurice.android.reservator.model.DataProxy;
import com.futurice.android.reservator.model.Reservation;
import com.futurice.android.reservator.model.Room;

public class DummyDataProxy implements DataProxy {
	List<Room> rooms = null;
	Map<String,List<Reservation>> reservations;

	public DummyDataProxy() {
		this.rooms = new ArrayList<Room>();
		this.reservations = new HashMap<String,List<Reservation>>();

		rooms.add(new Room("Room-Panorama-401", "401-Panorama@futu.com", this));
		rooms.add(new Room("Room-Pilotti-402", "402-Pilotti@futu.com", this));
		rooms.add(new Room("Room-Vauhtimato-403", "403-vauhtimato@futu.com", this));
		rooms.add(new Room("Room-Space Shot-404", "404-Spaceshot@futu.com", this));
		rooms.add(new Room("Project-Regatta-405", "405-Regatta@futu.com", this));
		rooms.add(new Room("Project-Kino-406", "406-Kino@futu.com", this));
		rooms.add(new Room("Project-Vekkula-407", "407-Vekkula@futu.com", this));
		rooms.add(new Room("Project-Metkula-408", "408-Metkula@futu.com", this));

		rooms.add(new Room("Project-Verstas-506", "506-Verstas@futu.com", this));
		rooms.add(new Room("Room-Kenkäkauppa-503", "503-Kenkakauppa@futu.com", this));
		rooms.add(new Room("Room-Koivumetsä-504", "504-Koivumetsa@futu.com", this));
		rooms.add(new Room("Room-Merineukkari-502", "502-Merineukkari@futu.com", this));
		rooms.add(new Room("Room-Pikku Neukkari-501", "501-pikkuneukkari@futu.com", this));

		Collections.sort(rooms, new Comparator<Room>() {
			@Override
			public int compare(Room room1, Room room2) {
				return room1.getEmail().compareTo(room2.getEmail());
			}
		});
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
	public boolean reserve(Reservation r) {
		return true;
	}

	@Override
	public void setCredentials(String user, String password) {
		// Do nothing
	}

	@Override
	public List<Reservation> getRoomReservations(Room room) {
		try {
			Thread.sleep((int)(Math.random()*10));
		} catch (InterruptedException e1) {}

		List<Reservation> ret = reservations.get(room.getEmail());
		if (ret != null) return ret; // found in cache

		ret = new ArrayList<Reservation>();
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

		reservations.put(room.getEmail(), ret); // put into the cache

		return ret;
	}
}
