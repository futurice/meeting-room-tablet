package com.futurice.android.reservator;

import android.accounts.AccountManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

import com.futurice.android.reservator.common.PreferenceManager;
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
            AccountManager.get(this),
            PlatformCalendarDataProxy.Mode.RESOURCES);

        String usedAccount = PreferenceManager.getInstance(this).getDefaultCalendarAccount();
        ((PlatformCalendarDataProxy) proxy).setAccount(usedAccount);
        googleAddressBook.setAccount(usedAccount);

        addressBook = googleAddressBook;

        handler = new Handler();
        clearCacheLater();
    }


    private void clearCacheLater() {
        handler.postDelayed(clearAddressCache, ADDRESS_CACHE_CLEAR_INTERVAL);
    }
}
