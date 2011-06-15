package com.futurice.android.reservator.model.dummy;

import java.util.ArrayList;
import java.util.List;

import com.futurice.android.reservator.model.AddressBookEntry;
import com.futurice.android.reservator.model.AddressBook;

public class DummyAddressBook implements AddressBook {

	@Override
	public List<AddressBookEntry> getEntries() {
		List<AddressBookEntry> entries = new ArrayList<AddressBookEntry>();

		entries.add(new AddressBookEntry("Oleg Grenrus", "oleg.grenrus@futurice.com"));
		entries.add(new AddressBookEntry("Vihtori Mäntylä", "vihtori.mantyla@futurice.com"));

		return entries;
	}

	@Override
	public String getEmailByName(String name) {
		for (AddressBookEntry entry : getEntries()) {
			if (entry.getName().equals(name)) {
				return entry.getEmail();
			}
		}
		return null;
	}

}
