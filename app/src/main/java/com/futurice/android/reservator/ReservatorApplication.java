package com.futurice.android.reservator;

import android.accounts.AccountManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

import com.futurice.android.reservator.model.AddressBook;
import com.futurice.android.reservator.model.DataProxy;
import com.futurice.android.reservator.model.platformcalendar.PlatformCalendarDataProxy;
import com.futurice.android.reservator.model.platformcontacts.PlatformContactsAddressBook;

public class ReservatorApplication extends Application {
    private final long ADDRESS_CACHE_CLEAR_INTERVAL = 6 * 60 * 60 * 1000; // Once every six hours
    Runnable clearAddressCache = new Runnable() {
        @Override
        public void run() {
            getAddressBook().refetchEntries();
            clearCacheLater();
        }
    };
    private DataProxy proxy;
    private AddressBook addressBook;
    private Handler handler;

    public DataProxy getDataProxy() {
        return proxy;
    }

    public AddressBook getAddressBook() {
        return addressBook;
    }

    @Override
    public void onCreate() {
        PlatformContactsAddressBook googleAddressBook = new PlatformContactsAddressBook(getContentResolver());
        proxy = new PlatformCalendarDataProxy(
                getContentResolver(),
                AccountManager.get(this), this);

        String usedAccount = getSharedPreferences(getString(R.string.PREFERENCES_NAME), Context.MODE_PRIVATE).getString(
                getString(R.string.PREFERENCES_ACCOUNT),
                getString(R.string.allAccountsMagicWord));

        if (usedAccount.equals(getString(R.string.allAccountsMagicWord))) {
            ((PlatformCalendarDataProxy) proxy).setAccount(null);
            googleAddressBook.setAccount(null);
        } else {
            ((PlatformCalendarDataProxy) proxy).setAccount(usedAccount);
            googleAddressBook.setAccount(usedAccount);
        }

        addressBook = googleAddressBook;

        handler = new Handler();
        clearCacheLater();
    }

    public String getSettingValue(int settingNameId, String defaultValue) {
        SharedPreferences settings = getSharedPreferences(getString(R.string.PREFERENCES_NAME), 0);
        return settings.getString(getString(settingNameId), defaultValue);
    }

    public Boolean getBooleanSettingsValue(String settingName, Boolean defaultValue) {
        SharedPreferences settings = getSharedPreferences(getString(R.string.PREFERENCES_NAME), 0);
        return settings.getBoolean(settingName, defaultValue);
    }

    public String getFavouriteRoomName() {
        return this.getSettingValue(R.string.PREFERENCES_ROOM_NAME, getString(R.string.lobbyRoomName));
    }

    private void clearCacheLater() {
        handler.postDelayed(clearAddressCache, ADDRESS_CACHE_CLEAR_INTERVAL);
    }
}
