package com.futurice.android.reservator.model;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import android.os.AsyncTask;

public abstract class DataProxy {
	public abstract void setCredentials(String user, String password);
	public abstract void setServer(String server);
	abstract public void reserve(Room room, TimeSpan timeSpan, String owner, String ownerEmail) throws ReservatorException;

	/**
	 * Synchronously get a list of rooms this proxy is aware of. Listeners are not notified when done.
	 * @return the rooms
	 * @throws ReservatorException
	 */
	abstract public Vector<Room> getRooms() throws ReservatorException;
	
	/**
	 * Synchronously get a list of reservations mapped to a room. The reservations are not updated to the room.
	 * Listeners are not notified when done.
	 * @return reservations for the room
	 * @throws ReservatorException
	 */
	abstract public Vector<Reservation> getRoomReservations(Room r) throws ReservatorException;

	private Set<DataUpdatedListener> listeners = new HashSet<DataUpdatedListener>();

	/**
	 * Synchronously gets a room with its name. Listeners are not notified when done.
	 * @param roomName. The room name to look for
	 * @return the room matching roomName or null
	 * @throws ReservatorException
	 */
	public Room getRoomWithName(String roomName) throws ReservatorException{
		Vector<Room> rooms = getRooms();
		Iterator<Room> it = rooms.iterator();
		while(it.hasNext()){
			Room room = it.next();
			if (room.getName().equals(roomName)){
				return room;
			}
		}
		throw new ReservatorException("Can't find room " + roomName);
	}
	
	/**
	 * Returns an array of all room names
	 * @return array of all room names
	 * @throws ReservatorException
	 */
	public String[] getRoomNames() throws ReservatorException{
		Vector<Room> rooms = getRooms();
		String[] roomNames = new String[rooms.size()];
		int ind = 0;
		Iterator<Room> it = rooms.iterator();
		while(it.hasNext()){
			Room room = it.next();
			roomNames[ind] = room.getName();
			ind ++;
		}
		return roomNames;
	}
	
	/**
	 * Asynchronously request a room list refresh.
	 * Listener's roomListUpdated is called when done.
	 */
	public void refreshRooms(){
		new RoomListRefreshTask().execute();
	}
	
	/**
	 * Asynchronously request room's reservations and updates them to the room object.
	 * Listener's roomReservationsUpdated is called when done.
	 */
	public void refreshRoomReservations(Room room){
		new RoomReservationRefreshTask().execute(room);
	}

	/**
	 * Add a listener for this proxy. The listener will be notified after calls to refreshRooms and refreshRoomReservations finish or fail.
	 * @param listener
	 */
	public void addDataUpdatedListener(DataUpdatedListener listener){
		listeners.add(listener);
	}

	/**
	 * Remove a listener from this proxy.
	 * @param listener
	 */
	public void removeDataUpdatedListener(DataUpdatedListener listener){
		listeners.remove(listener);
	}
	private void notifyRoomsUpdated(Vector<Room> rooms){
		for(DataUpdatedListener l : listeners){
			l.roomListUpdated(rooms);
		}
	}
	private void notifyRoomReservationsUpdated(Room room){
		for(DataUpdatedListener l : listeners){
			l.roomReservationsUpdated(room);
		}
	}

	private void notifyRefreshFailed(ReservatorException e){
		for(DataUpdatedListener l : listeners){
			l.refreshFailed(e);
		}
	}
	/**
	 * Private inner class for asynchronously refreshing list of all the rooms.
	 * @author vman
	 */
	private class RoomListRefreshTask extends AsyncTask<Void, Void, Vector<Room>>{
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
		protected void onPostExecute(Vector<Room> rooms){
			if(rooms == null){
				notifyRefreshFailed(e);
			}else{
				notifyRoomsUpdated(rooms);
			}
		}
	}
	/**
	 * Private inner class for asynchronously refreshing room's reservation list.
	 * @author vman
	 */
	private class RoomReservationRefreshTask extends AsyncTask<Room, Void, Room>{
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
		protected void onPostExecute(Room room){
			if(room == null){
				notifyRefreshFailed(e);
			}else{
				notifyRoomReservationsUpdated(room);
			}
		}
	}
}
