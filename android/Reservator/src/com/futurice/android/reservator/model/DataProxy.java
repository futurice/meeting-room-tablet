package com.futurice.android.reservator.model;

import java.util.List;

public interface DataProxy {
	public void init();
	public void setCredentials(String user, String password);
	public void deinit();
	
	public List<Room> getRooms() throws ReservatorException;
	public List<Reservation> getReservations() throws ReservatorException;
	public List<Reservation> getRoomReservations(Room room) throws ReservatorException;
	public boolean reserve(Reservation r) throws ReservatorException;
}
