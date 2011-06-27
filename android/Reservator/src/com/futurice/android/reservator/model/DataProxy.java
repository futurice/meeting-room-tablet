package com.futurice.android.reservator.model;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import android.os.AsyncTask;

public abstract class DataProxy {
	public abstract void setCredentials(String user, String password);
	public abstract void setServer(String server);
	public abstract void deinit(); // TODO: do we need this?
	abstract public void reserve(Room room, TimeSpan timeSpan, String ownerEmail) throws ReservatorException;
	abstract public Vector<Room> getRooms() throws ReservatorException;
	abstract public Vector<Reservation> getRoomReservations(Room r) throws ReservatorException;

	private Set<DataUpdatedListener> listeners = new HashSet<DataUpdatedListener>();

	public void refreshRooms(){
		new RoomListRefreshTask().execute();
	}

	public void refreshRoomReservations(final Room room){
		new RoomReservationRefreshTask().execute(room);
	}

	public void addDataUpdatedListener(DataUpdatedListener listener){
		listeners.add(listener);
	}
	public void removeDataUpdatedListener(DataUpdatedListener listener){
		listeners.remove(listener);
	}
	private void notifyRoomsUpdated(Vector<Room> rooms){
		for(DataUpdatedListener l : listeners){
			l.roomListUpdated(rooms);
		}
	}
	private void notifyRoomReservationsUpdated(Room room, Vector<Reservation> reservations){
		for(DataUpdatedListener l : listeners){
			l.roomReservationsUpdated(room, reservations);
		}
	}

	private void notifyRefreshFailed(ReservatorException e){
		for(DataUpdatedListener l : listeners){
			l.refreshFailed(e);
		}
	}

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

	private class RoomReservationRefreshTask extends AsyncTask<Room, Void, Room>{
		ReservatorException e;
		@Override
		protected Room doInBackground(Room... rooms) {
			Room room = rooms[0];
			try {
				room.setReservations(getRoomReservations(room));
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
				notifyRoomReservationsUpdated(room, room.getReservations());
			}
		}
	}
}
