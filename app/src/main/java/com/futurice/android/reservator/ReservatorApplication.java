package com.futurice.android.reservator;

import android.accounts.AccountManager;
import android.app.Application;
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
    private DataProxy proxy = null;
    private AddressBook addressBook = null;
    private Handler handler = null;

    public DataProxy getDataProxy() {
        if(proxy == null) resetDataProxy();
        return proxy;
    }

    public void resetDataProxy()
    {
        PlatformCalendarDataProxy.Mode mode = PreferenceManager.getInstance(this).getCalendarMode();
        if(mode==null)
        {
            mode = PlatformCalendarDataProxy.Mode.CALENDARS;
        }

        proxy = new PlatformCalendarDataProxy(
                getContentResolver(),
                AccountManager.get(this),
                mode);

        String usedAccount = PreferenceManager.getInstance(this).getDefaultCalendarAccount();
        ((PlatformCalendarDataProxy) proxy).setAccount(usedAccount);

        PlatformContactsAddressBook googleAddressBook = new PlatformContactsAddressBook(getContentResolver());
        googleAddressBook.setAccount(usedAccount);

        addressBook = googleAddressBook;

        handler = new Handler();
        clearCacheLater();
    }

    public AddressBook getAddressBook() {
        return addressBook;
    }

    @Override
    public void onCreate() {
        resetDataProxy();
    }

    private void clearCacheLater() {
        handler.postDelayed(clearAddressCache, ADDRESS_CACHE_CLEAR_INTERVAL);
    }
}
