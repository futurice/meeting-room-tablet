package com.futurice.android.reservator.model;

import java.util.Vector;
import java.util.Set;
import java.util.HashSet;

/**
 * Just an AddressBook that can utilize multiple sources.
 * @author vsin 
 */
public class CombinedAddressBook extends AddressBook {
	private Set<AddressBook> sources = new HashSet<AddressBook>();
	
	public CombinedAddressBook() {}
	public void addSource(AddressBook ab) {
		sources.add(ab);
	}

	/**
	 * Return a value only if all sources were successful.
	 */
	@Override
	protected Vector<AddressBookEntry> fetchEntries()
			throws ReservatorException {
		Vector<AddressBookEntry> entries = new Vector<AddressBookEntry>();  
		for (AddressBook ab : sources) {
			Vector<AddressBookEntry> subResult = ab.fetchEntries();
			if (subResult == null) return null;
			entries.addAll(subResult);
		}
		return entries;
	}

	@Override
	public void setCredentials(String username, String password) {}
}
