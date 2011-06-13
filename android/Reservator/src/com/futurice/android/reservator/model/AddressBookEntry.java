package com.futurice.android.reservator.model;

public class AddressBookEntry {
	private String name, email;

	public AddressBookEntry(String name, String email) {
		this.name = name;
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}
}
