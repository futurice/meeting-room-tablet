package com.futurice.android.reservator.model;

import java.util.List;


import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.widget.ArrayAdapter;

public class AddressBookAdapter extends ArrayAdapter<String>{
	public AddressBookAdapter(Context ctx, AddressBook addressBook){
		super(ctx,android.R.layout.simple_spinner_dropdown_item);

		try {
			List<AddressBookEntry> entries = addressBook.getEntries();
			for(AddressBookEntry abe : entries){
				this.add(abe.getName());
			}
		} catch (ReservatorException e) {
			Builder alertBuilder = new AlertDialog.Builder(getContext());
			alertBuilder.setTitle("Error!");
			alertBuilder.setMessage(e.getMessage());
			alertBuilder.show();
		}
	}
}
