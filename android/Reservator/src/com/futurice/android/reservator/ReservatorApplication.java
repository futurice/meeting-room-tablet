package com.futurice.android.reservator;

import android.accounts.AccountManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.futurice.android.reservator.model.AddressBook;
import com.futurice.android.reservator.model.DataProxy;
import com.futurice.android.reservator.model.fum3.FumAddressBook;
import com.futurice.android.reservator.model.platformcalendar.PlatformCalendarDataProxy;

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
		addressBook  = new FumAddressBook(this);
		
		proxy = new PlatformCalendarDataProxy(
				getContentResolver(),
				AccountManager.get(this),
				getString(R.string.calendarAccountGlob));
		
		String usedAccount = getSharedPreferences(getString(R.string.PREFERENCES_NAME), Context.MODE_PRIVATE).getString(
	    		getString(R.string.PREFERENCES_GOOGLE_ACCOUNT), 
	    		getString(R.string.allAccountsMagicWord));
	    
		if (usedAccount.equals(getString(R.string.allAccountsMagicWord))) {
			((PlatformCalendarDataProxy) proxy).setAccount(null);
		} else {
			((PlatformCalendarDataProxy) proxy).setAccount(usedAccount);
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
