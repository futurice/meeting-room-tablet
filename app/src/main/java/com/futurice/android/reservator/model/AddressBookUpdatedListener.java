package com.futurice.android.reservator.model;


/**
 * Callbacks for AddressBookUpdatedListener updates and exceptions. All callbacks are in separate threads (non-ui).
 */
public interface AddressBookUpdatedListener {
    void addressBookUpdated();

    void addressBookUpdateFailed(ReservatorException e);
}
