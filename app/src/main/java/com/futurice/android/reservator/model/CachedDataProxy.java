package com.futurice.android.reservator.model;

import android.util.Log;

import com.futurice.android.reservator.common.CacheMap;

import java.util.Vector;

public class CachedDataProxy extends DataProxy {
    private static final long CACHE_RESERVATION_FOR = 60 * 1000; // 1 minute

    private final DataProxy dataProxy;
    private final CacheMap<String, Vector<Reservation>> reservationCache;
    private Vector<Room> rooms;

    public CachedDataProxy(DataProxy dataProxy) {
        this.dataProxy = dataProxy;
        this.reservationCache = new CacheMap<String, Vector<Reservation>>();
        this.rooms = null;
    }


    @Override
    public Vector<Reservation> getRoomReservations(Room r)
        throws ReservatorException {
        Vector<Reservation> reservations = reservationCache.get(r.getEmail());
        if (reservations == null) {
            Log.d("CACHE", "getRoomReservations -- " + r.getEmail());
            reservations = dataProxy.getRoomReservations(r);
        }
        reservationCache.put(r.getEmail(), reservations, CACHE_RESERVATION_FOR);
        return reservations;
    }

    @Override
    public Vector<Room> getRooms() throws ReservatorException {
        // TODO: do not cache forever
        if (this.rooms == null) {
            Log.d("CACHE", "getRooms");
            this.rooms = dataProxy.getRooms();
        }
        return this.rooms;
    }

    @Override
    public void reserve(Room room, TimeSpan timeSpan, String owner, String ownerEmail, String meetingName)
        throws ReservatorException {
        reservationCache.remove(room.getEmail());
        dataProxy.reserve(room, timeSpan, owner, ownerEmail, meetingName);
    }

    @Override
    public void setCredentials(String user, String password) {
        dataProxy.setCredentials(user, password);
    }

    @Override
    public void setServer(String server) {
        dataProxy.setServer(server);
        clearCache();
    }

    /**
     * Force Refreshing the room, clearing the cached data for that room if it exists.
     *
     * @param room
     */
    public void forceRefreshRoomReservations(Room room) {
        this.reservationCache.remove(room.getEmail());
        this.refreshRoomReservations(room);
    }

    public void clearCache() {
        this.rooms = null;
        this.reservationCache.clear();
    }

    @Override
    public void cancelReservation(Reservation r) throws ReservatorException {
        dataProxy.cancelReservation(r);
    }

    @Override
    public void synchronize(Room r) {

    }
}
