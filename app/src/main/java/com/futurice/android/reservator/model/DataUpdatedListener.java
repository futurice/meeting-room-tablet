package com.futurice.android.reservator.model;

import java.util.Vector;

/**
 * Callbacks for DataProxy updates and exceptions. All callbacks are in separate threads (non-ui).
 *
 * @author vman
 */
public interface DataUpdatedListener {
    void roomListUpdated(Vector<Room> rooms);

    void roomReservationsUpdated(Room room);

    void refreshFailed(ReservatorException ex);
}
