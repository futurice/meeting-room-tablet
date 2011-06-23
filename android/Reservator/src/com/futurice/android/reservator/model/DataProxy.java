package com.futurice.android.reservator.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class DataProxy {
	public abstract void setCredentials(String user, String password);
	public abstract void deinit(); // TODO: do we need this?
	abstract public void reserve(Room room, TimeSpan timeSpan, String ownerEmail) throws ReservatorException;
	abstract public List<Room> getRooms() throws ReservatorException;
	
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
	abstract protected List<Reservation> getRoomReservations(Room room) throws ReservatorException;
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
	private void notifyRoomsUpdated(List<Room> rooms){
		for(DataUpdatedListener l : listeners){
			l.roomListUpdated(rooms);
		}
	}
	private void notifyRoomReservationsUpdated(Room room, List<Reservation> reservations){
		for(DataUpdatedListener l : listeners){
			l.roomReservationsUpdated(room, reservations);
		}
	}
	
	
}
