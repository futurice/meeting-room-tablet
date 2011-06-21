package com.futurice.android.reservator.model;

import java.util.List;

public abstract class AddressBook {
	private List<AddressBookEntry> entries;

	protected abstract List<AddressBookEntry> fetchEntries() throws ReservatorException;

	public List<AddressBookEntry> getEntries() throws ReservatorException {
		prefetchEntries();
		return entries;
	}

	public void prefetchEntries() throws ReservatorException {
		if (entries == null) {
			entries = fetchEntries();
		}
	}

	public String getEmailByName(String name) {
		if (entries == null) {
			return null; // no entries, no win
		}

		for (AddressBookEntry entry : entries) {
			if (entry.getName().equals(name)) {
				return entry.getEmail();
			}
		}
		return null;
	}
}
