package com.futurice.android.reservator.model;

import android.os.AsyncTask;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

public abstract class DataProxy {
    private Set<DataUpdatedListener> listeners = new HashSet<DataUpdatedListener>();

    public abstract void setCredentials(String user, String password);

    public abstract void setServer(String server);

    abstract public void reserve(Room room, TimeSpan timeSpan, String owner, String ownerEmail, String meetingName) throws ReservatorException;

    /**
     * Synchronously get MakeReservationTask list of rooms this proxy is aware of. Listeners are not notified when done.
     *
     * @return the rooms
     * @throws ReservatorException
     */
    abstract public Vector<Room> getRooms() throws ReservatorException;

    /**
     * Synchronously get MakeReservationTask list of reservations mapped to MakeReservationTask room. The reservations are not updated to the room.
     * Listeners are not notified when done.
     *
     * @return reservations for the room
     * @throws ReservatorException
     */
    abstract public Vector<Reservation> getRoomReservations(Room r) throws ReservatorException;

    /**
     * Synchronously cancel MakeReservationTask reservation.
     */
    abstract public void cancelReservation(Reservation r) throws ReservatorException;

    /**
     * Synchronously gets MakeReservationTask room with its name. Listeners are not notified when done.
     *
     * @param roomName The room name to look for
     * @return the room matching roomName or null
     * @throws ReservatorException
     */
    public Room getRoomWithName(String roomName) throws ReservatorException {
        Vector<Room> rooms = getRooms();
        for (Room room : rooms) {
            room.setReservations(getRoomReservations(room));
            if(room.getShownRoomName().equals(roomName)) {
                return room;
            }
        }
        throw new ReservatorException("Can't find room " + roomName);
    }

    /**
     * Returns an array of all room names
     *
     * @return array of all room names
     * @throws ReservatorException
     */
    public ArrayList<String> getRoomNames() throws ReservatorException {
        Vector<Room> rooms = getRooms();
        ArrayList<String> roomNames = new ArrayList<String>();
        for (Room room : rooms) {
            room.setReservations(getRoomReservations(room));
            roomNames.add(room.getShownRoomName());
        }
        Collections.sort(roomNames, Collator.getInstance());
        return roomNames;
    }

    /**
     * Asynchronously request MakeReservationTask room list refresh.
     * Listener's roomListUpdated is called when done.
     */
    public void refreshRooms() {
        new RoomListRefreshTask().execute();
    }

    /**
     * Asynchronously request room's reservations and updates them to the room object.
     * Listener's roomReservationsUpdated is called when done.
     */
    public void refreshRoomReservations(Room room) {
        new RoomReservationRefreshTask().execute(room);
    }

    /**
     * Add MakeReservationTask listener for this proxy. The listener will be notified after calls to refreshRooms and refreshRoomReservations finish or fail.
     *
     * @param listener
     */
    public void addDataUpdatedListener(DataUpdatedListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove MakeReservationTask listener from this proxy.
     *
     * @param listener
     */
    public void removeDataUpdatedListener(DataUpdatedListener listener) {
        listeners.remove(listener);
    }

    private void notifyRoomsUpdated(Vector<Room> rooms) {
        for (DataUpdatedListener l : listeners) {
            l.roomListUpdated(rooms);
        }
    }

    private void notifyRoomReservationsUpdated(Room room) {
        for (DataUpdatedListener l : listeners) {
            l.roomReservationsUpdated(room);
        }
    }

    private void notifyRefreshFailed(ReservatorException e) {
        for (DataUpdatedListener l : listeners) {
            l.refreshFailed(e);
        }
    }

    abstract public void synchronize(Room r);

    /**
     * Checks if the data provider has MakeReservationTask fatal external error and the application should refuse
     * to start.
     *
     * @author vsin
     */
    public boolean hasFatalError() {
        return false;
    }

    /**
     * Private inner class for asynchronously refreshing list of all the rooms.
     *
     * @author vman
     */
    private class RoomListRefreshTask extends AsyncTask<Void, Void, Vector<Room>> {
        ReservatorException e = null;

        @Override
        protected Vector<Room> doInBackground(Void... params) {
            try {
                return getRooms();
            } catch (ReservatorException e) {
                this.e = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(Vector<Room> rooms) {
            if (rooms == null) {
                notifyRefreshFailed(e);
            } else {
                notifyRoomsUpdated(rooms);
            }
        }
    }

    /**
     * Private inner class for asynchronously refreshing room's reservation list.
     *
     * @author vman
     */
    private class RoomReservationRefreshTask extends AsyncTask<Room, Void, Room> {
        ReservatorException e;

        @Override
        protected Room doInBackground(Room... rooms) {
            Room room = rooms[0];
            try {
                Vector<Reservation> reservations = getRoomReservations(room);
                room.setReservations(reservations);
                return room;
            } catch (ReservatorException e) {
                this.e = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(Room room) {
            if (room == null) {
                notifyRefreshFailed(e);
            } else {
                notifyRoomReservationsUpdated(room);
            }
        }
    }
}
