package com.futurice.android.reservator.model;

import java.util.List;

/**
 * Callbacks for DataProxy updates and exceptions. All callbacks are in separate threads (non-ui).
 * @author vman
 *
 */
public interface DataUpdatedListener {
	public void roomListUpdated(List<Room> rooms);
	public void roomReservationsUpdated(Room room, List<Reservation> reservations);
	public void refreshFailed(ReservatorException ex);
}
