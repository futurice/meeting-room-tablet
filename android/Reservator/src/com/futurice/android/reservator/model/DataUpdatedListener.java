package com.futurice.android.reservator.model;

import java.util.Vector;

/**
 * Callbacks for DataProxy updates and exceptions. All callbacks are in separate threads (non-ui).
 * @author vman
 *
 */
public interface DataUpdatedListener {
	public void roomListUpdated(Vector<Room> rooms);
	public void roomReservationsUpdated(Room room, Vector<Reservation> reservations);
	public void refreshFailed(ReservatorException ex);
}
