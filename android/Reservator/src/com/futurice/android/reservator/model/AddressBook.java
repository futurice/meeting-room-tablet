package com.futurice.android.reservator.model;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import android.os.AsyncTask;

public abstract class AddressBook {
	private Vector<AddressBookEntry> entries;
	private Set<AddressBookUpdatedListener> listeners = new HashSet<AddressBookUpdatedListener>();

	protected abstract Vector<AddressBookEntry> fetchEntries() throws ReservatorException;

	public Vector<AddressBookEntry> getEntries() throws ReservatorException {
		prefetchEntries();
		return entries;
	}

	public void prefetchEntries() throws ReservatorException {
		if (entries == null) {
			// entries = fetchEntries();
			new PrefetchEntriesTask().execute();
		}
	}

	public AddressBookEntry getEntryByName(String name) {
		if (entries == null) {
			return null; // no entries, no win
		}

		for (AddressBookEntry entry : entries) {
			if (entry.getName().equals(name)) {
				return entry;
			}
		}
		return null;
	}
	
	
	/**
	 * Add a listener for this proxy. The listener will be notified after calls to refreshRooms and refreshRoomReservations finish or fail.
	 * @param listener
	 */
	public void addDataUpdatedListener(AddressBookUpdatedListener listener){
		listeners.add(listener);
	}

	/**
	 * Remove a listener from this proxy.
	 * @param listener
	 */
	public void removeDataUpdatedListener(AddressBookUpdatedListener listener){
		listeners.remove(listener);
	}
	
	private void notifyEntriesUpdated(){
		for(AddressBookUpdatedListener l : listeners){
			l.addressBookUpdated();
		}
	}
	
	private class PrefetchEntriesTask extends AsyncTask<Void, Void, Vector<AddressBookEntry>> {

		@Override
		protected Vector<AddressBookEntry> doInBackground(Void... params) {
			try {
				return fetchEntries();
			} catch (ReservatorException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		@Override
		protected void onPostExecute(Vector<AddressBookEntry> entries){
			AddressBook.this.entries = entries;
			notifyEntriesUpdated();
		}
	}
	
	
}
