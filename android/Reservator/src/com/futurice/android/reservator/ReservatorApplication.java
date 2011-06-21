package com.futurice.android.reservator;

import com.futurice.android.reservator.model.AddressBook;
import com.futurice.android.reservator.model.DataProxy;
import com.futurice.android.reservator.model.dummy.DummyDataProxy;
import com.futurice.android.reservator.model.fum3.FumAddressBook;
import com.futurice.android.reservator.model.soap.SoapDataProxy;

import android.app.Application;

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
		proxy = new DummyDataProxy();
		//proxy = new SoapDataProxy("10.4.2.214"); // TODO: preference, prod: mail.futurice.com
		addressBook  = new FumAddressBook();
	}
}
