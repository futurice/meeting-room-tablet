package com.futurice.android.reservator.model;

import java.util.List;

public interface DataProxy {
	public void setCredentials(String user, String password);
	public void deinit(); // TODO: do we need this?

	public List<Room> getRooms() throws ReservatorException;
	public List<Reservation> getRoomReservations(Room room) throws ReservatorException;
	public void reserve(Room room, TimeSpan timeSpan, String ownerEmail) throws ReservatorException;
}
