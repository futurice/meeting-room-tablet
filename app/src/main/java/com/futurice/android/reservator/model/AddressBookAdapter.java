package com.futurice.android.reservator.model;

import java.util.List;

import android.content.Context;
import android.widget.ArrayAdapter;

public class AddressBookAdapter extends ArrayAdapter<String> {
    public AddressBookAdapter(Context ctx, AddressBook addressBook)
        throws ReservatorException {
        super(ctx, android.R.layout.simple_spinner_dropdown_item);

        // TODO Add listeners to handle null
        List<AddressBookEntry> entries = addressBook.getEntries();
        if (entries != null) {
            for (AddressBookEntry abe : entries) {
                this.add(abe.getName());
            }
        }

    }
}
