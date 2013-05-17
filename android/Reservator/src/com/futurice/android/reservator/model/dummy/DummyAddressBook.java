package com.futurice.android.reservator.model.dummy;

import java.util.Vector;

import com.futurice.android.reservator.model.AddressBook;
import com.futurice.android.reservator.model.AddressBookEntry;

public class DummyAddressBook extends AddressBook {
	
	public DummyAddressBook() {
		super();
	}
	
	@Override
	protected Vector<AddressBookEntry> fetchEntries() {
		Vector<AddressBookEntry> entries = new Vector<AddressBookEntry>();

		entries.add(new AddressBookEntry("Oleg Grenrus", "oleg.grenrus@futurice.com"));
		entries.add(new AddressBookEntry("Vihtori M�ntyl�", "vihtori.mantyla@futurice.com"));

		return entries;
	}

	@Override
	public void setCredentials(String username, String password) {
		// NOOP
	}
}
