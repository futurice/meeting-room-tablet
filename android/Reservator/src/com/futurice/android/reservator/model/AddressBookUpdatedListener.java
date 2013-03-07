package com.futurice.android.reservator.model;


/**
 * Callbacks for DataProxy updates and exceptions. All callbacks are in separate threads (non-ui).
 * @author vman
 *
 */
public interface AddressBookUpdatedListener {
	public void addressBookUpdated();
}
