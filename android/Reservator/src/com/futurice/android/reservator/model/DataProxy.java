package com.futurice.android.reservator.model;

import java.util.List;

public interface DataProxy {
	public boolean init(String user, String password);
	public boolean deinit();
	public List<Room> getRooms();
	public List<Reservation> getReservations();
	public boolean reserve(Reservation r);
}
