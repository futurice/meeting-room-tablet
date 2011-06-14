package com.futurice.android.reservator.model;

import java.util.List;

public interface AddressBook {
	public List<AddressBookEntry> getEntries() throws ReservatorException;
}
