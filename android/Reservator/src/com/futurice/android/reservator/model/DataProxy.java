package com.futurice.android.reservator.model;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

public abstract class DataProxy {
	public abstract void setCredentials(String user, String password);
	public abstract void deinit(); // TODO: do we need this?
	abstract public void reserve(Room room, TimeSpan timeSpan, String ownerEmail) throws ReservatorException;
	abstract public Vector<Room> getRooms() throws ReservatorException;
	
	private Set<DataUpdatedListener> listeners = new HashSet<DataUpdatedListener>();
	public void refreshRooms(){
		new Thread(){
			public void run(){
				try {
					notifyRoomsUpdated(getRooms());
				} catch (ReservatorException e) {
					//TODO errortoast or something, maybe tell listener about error?
					e.printStackTrace();
				}
			}
		}.start();
	}
	abstract protected Vector<Reservation> getRoomReservations(Room room) throws ReservatorException;
	public void refreshRoomReservations(final Room room){
		new Thread(){
			public void run(){
				try {
					notifyRoomReservationsUpdated(room, room.getReservations(true));
				} catch (ReservatorException e) {
					//TODO errortoast or something, maybe tell listener about error?
					e.printStackTrace();
				}
			}
		}.start();
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
	
	
}
