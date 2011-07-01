package com.futurice.android.reservator;


import java.util.ArrayList;
import java.util.List;

import com.futurice.android.reservator.model.AddressBook;
import com.futurice.android.reservator.model.CachedDataProxy;
import com.futurice.android.reservator.model.DataProxy;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.fum3.FumAddressBook;
import com.futurice.android.reservator.model.soap.SoapDataProxy;

import android.app.Application;
import android.content.SharedPreferences;

public class ReservatorApplication extends Application {
	private DataProxy proxy;
	private AddressBook addressBook;

	public DataProxy getDataProxy(){
		return proxy;
	}

	public AddressBook getAddressBook() {
		return addressBook;
	}

	@Override
	public void onCreate(){
		//proxy = new DummyDataProxy();
		SharedPreferences settings = getSharedPreferences(getString(R.string.PREFERENCES_NAME), 0);
		proxy = new SoapDataProxy(settings.getString(getString(R.string.PREFERENCES_SERVER_ADDRESS), "127.0.0.1")); // TODO: change to mail.futurice.com before delivery
		// proxy = new CachedDataProxy(proxy);
		addressBook  = new FumAddressBook();
		try {
			addressBook.prefetchEntries();
		} catch (ReservatorException e) {
			// TODO: DIE!
		}
	}
}
