package com.futurice.android.reservator.model;

import java.util.List;

public interface AddressBookProxy {
	public List<AddressBookEntry> getEntries() throws ReservatorException;
}
