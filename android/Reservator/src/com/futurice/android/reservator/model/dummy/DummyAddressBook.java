package com.futurice.android.reservator.model.dummy;

import java.util.ArrayList;
import java.util.List;

import com.futurice.android.reservator.model.AddressBookEntry;
import com.futurice.android.reservator.model.AddressBook;

public class DummyAddressBook extends AddressBook {
	@Override
	protected List<AddressBookEntry> fetchEntries() {
		List<AddressBookEntry> entries = new ArrayList<AddressBookEntry>();

		entries.add(new AddressBookEntry("Oleg Grenrus", "oleg.grenrus@futurice.com"));
		entries.add(new AddressBookEntry("Vihtori Mäntylä", "vihtori.mantyla@futurice.com"));

		return entries;
	}
}
