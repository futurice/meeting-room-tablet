package com.futurice.android.reservator.model.dummy;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;


import com.futurice.android.reservator.model.DataProxy;
import com.futurice.android.reservator.model.Reservation;
import com.futurice.android.reservator.model.Room;

public class DummyDataProxy implements DataProxy{
	List<Room> rooms = null;
	List<Reservation> reservations = null;
	@Override
	public boolean init(String user, String password) {
		this.rooms = new ArrayList<Room>();
		this.reservations = new ArrayList<Reservation>();
		
		for(int i = 0; i < 10; i++){
			Room room = new Room("Room " + 400+i, "futu.naut@futurice.com", null);
			generateReservationsForRoom(room);
			rooms.add(room);
		}
		
		
		return true;
	}

	@Override
	public boolean deinit() {
		this.rooms = null;
		this.reservations = null;
		return true;
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
	
	private void generateReservationsForRoom(Room room){
		Random rand = new Random();
		long begin = new Date().getTime() - 60*60*24*2*1000;
		long end = new Date().getTime() + 60*60*24*2*1000;

		while(begin < end){
			int reservationLength = rand.nextInt(7200000) + 1;
			Calendar b = Calendar.getInstance();
			b.setTimeInMillis(begin);
			Calendar e = Calendar.getInstance();
			e.setTimeInMillis(begin + reservationLength);
			Reservation r = new Reservation(room, b, e);
			reservations.add(r);
			room.addReservation(r);
			begin += reservationLength + rand.nextInt(7200000) + 1;
		}
	}
}
