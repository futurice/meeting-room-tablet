package com.futurice.android.reservator.model.dummy;

import java.util.Vector;

import android.content.Context;

import com.futurice.android.reservator.model.AddressBook;
import com.futurice.android.reservator.model.AddressBookEntry;

public class DummyAddressBook extends AddressBook {
	
	public DummyAddressBook(Context c) {
		super(c);
	}
	
	@Override
	protected Vector<AddressBookEntry> fetchEntries() {
		Vector<AddressBookEntry> entries = new Vector<AddressBookEntry>();

		entries.add(new AddressBookEntry("Oleg Grenrus", "oleg.grenrus@futurice.com"));
		entries.add(new AddressBookEntry("Vihtori Mäntylä", "vihtori.mantyla@futurice.com"));

		return entries;
	}
}
