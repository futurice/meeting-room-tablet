package com.futurice.android.reservator;

import com.futurice.android.reservator.model.AddressBook;
import com.futurice.android.reservator.model.CachedDataProxy;
import com.futurice.android.reservator.model.Room;
import com.futurice.android.reservator.model.dummy.DummyDataProxy;
import com.futurice.android.reservator.model.DataProxy;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.fum3.FumAddressBook;
import com.futurice.android.reservator.model.soap.SoapDataProxy;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class ReservatorApplication extends Application {
	private DataProxy proxy;
	private AddressBook addressBook;
	private Activity runningActivity;

	public DataProxy getDataProxy(){
		return proxy;
	}

	public AddressBook getAddressBook() {
		return addressBook;
	}
	
	@Override
	public void onCreate(){
		String serverAddress = getSettingValue(R.string.PREFERENCES_SERVER_ADDRESS, "mail.futurice.com");// TODO: change to mail.futurice.com before delivery
		proxy = new SoapDataProxy(serverAddress);
		// proxy = new DummyDataProxy();
		proxy = new CachedDataProxy(proxy);
		addressBook  = new FumAddressBook();
		try {
			addressBook.prefetchEntries();
		} catch (ReservatorException e) {
			// TODO: DIE!
		}
	}
	
	public String getSettingValue(int settingNameId, String defaultValue){
		SharedPreferences settings = getSharedPreferences(getString(R.string.PREFERENCES_NAME), 0);
		return settings.getString(getString(settingNameId), defaultValue);
	}
	
	public String getFavouriteRoomName(){
		return this.getSettingValue(R.string.PREFERENCES_ROOM_NAME, getString(R.string.lobbyRoomName));
	}
}
