package com.futurice.android.reservator.model;

import java.util.List;

public interface DataProxy {
	public void setCredentials(String user, String password);
	public void deinit(); // TODO: do we need this?

	public List<Room> getRooms() throws ReservatorException;
	public List<Reservation> getReservations() throws ReservatorException;
	public List<Reservation> getRoomReservations(Room room) throws ReservatorException;
	public boolean reserve(Reservation r) throws ReservatorException;
}
