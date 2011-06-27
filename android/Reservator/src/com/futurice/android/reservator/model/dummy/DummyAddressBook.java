package com.futurice.android.reservator.model.dummy;

import java.util.Vector;

import com.futurice.android.reservator.model.AddressBookEntry;
import com.futurice.android.reservator.model.AddressBook;

public class DummyAddressBook extends AddressBook {
	@Override
	protected Vector<AddressBookEntry> fetchEntries() {
		Vector<AddressBookEntry> entries = new Vector<AddressBookEntry>();

		entries.add(new AddressBookEntry("Oleg Grenrus", "oleg.grenrus@futurice.com"));
		entries.add(new AddressBookEntry("Vihtori Mäntylä", "vihtori.mantyla@futurice.com"));

		return entries;
	}
}
