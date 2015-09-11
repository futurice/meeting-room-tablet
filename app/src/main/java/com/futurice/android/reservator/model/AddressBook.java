package com.futurice.android.reservator.model;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import android.os.AsyncTask;

public abstract class AddressBook {
    private Vector<AddressBookEntry> entries;
    private Set<AddressBookUpdatedListener> listeners = new HashSet<AddressBookUpdatedListener>();

    public AddressBook() {
    }

    protected abstract Vector<AddressBookEntry> fetchEntries() throws ReservatorException;

    public abstract void setCredentials(String username, String password);

    public Vector<AddressBookEntry> getEntries() throws ReservatorException {
        return entries;
    }

    public void prefetchEntries() {
        if (entries == null) {
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

    public String toString() {
        if (entries != null)
            return entries.toString();
        return "";
    }

    /**
     * Add a listener for this proxy. The listener will be notified after calls to refreshRooms and refreshRoomReservations finish or fail.
     *
     * @param listener
     */
    public void addDataUpdatedListener(AddressBookUpdatedListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a listener from this proxy.
     *
     * @param listener
     */
    public void removeDataUpdatedListener(AddressBookUpdatedListener listener) {
        listeners.remove(listener);
    }

    private void notifyEntriesUpdated() {
        synchronized (listeners) {
            for (AddressBookUpdatedListener l : listeners) {
                l.addressBookUpdated();
            }
        }
    }

    private void notifyAddressBookUpdateFailed(ReservatorException e) {
        synchronized (listeners) {
            for (AddressBookUpdatedListener l : listeners) {
                l.addressBookUpdateFailed(e);
            }
        }
    }

    public void refetchEntries() {
        entries = null;
        prefetchEntries();
    }

    private class PrefetchEntriesTask extends AsyncTask<Void, Void, Vector<AddressBookEntry>> {
        ReservatorException e = null;

        @Override
        protected Vector<AddressBookEntry> doInBackground(Void... params) {
            try {
                return fetchEntries();
            } catch (ReservatorException e) {
                this.e = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(Vector<AddressBookEntry> entries) {
            if (entries != null) {
                AddressBook.this.entries = entries;
                notifyEntriesUpdated();
            } else {
                notifyAddressBookUpdateFailed(e);
            }
        }
    }
}
