package com.futurice.android.reservator.model.fum3;

import java.util.List;

import com.futurice.android.reservator.model.AddressBook;
import com.futurice.android.reservator.model.AddressBookEntry;
import com.futurice.android.reservator.model.ReservatorException;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.widget.ArrayAdapter;

public class FumAddressBookAdapter extends ArrayAdapter<String>{
	static AddressBook fab;
	public FumAddressBookAdapter(Context ctx){
		super(ctx,android.R.layout.simple_spinner_dropdown_item);
		
		try {
			if(fab == null){
				fab = new FumAddressBook();
			}
			List<AddressBookEntry> entries = fab.getEntries();
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
