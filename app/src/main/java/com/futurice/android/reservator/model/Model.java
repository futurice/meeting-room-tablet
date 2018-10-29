package com.futurice.android.reservator.model;

import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Handler;

import com.futurice.android.reservator.common.PreferenceManager;
import com.futurice.android.reservator.model.platformcalendar.PlatformCalendarDataProxy;
import com.futurice.android.reservator.model.platformcontacts.PlatformContactsAddressBook;

public class Model {
    private final long ADDRESS_CACHE_CLEAR_INTERVAL = 6 * 60 * 60 * 1000; // Once every six hours

    private ContentResolver contentResolver;
    private AccountManager accountManager;
    private Context context;

    private Handler handler = null;
    private DataProxy proxy = null;
    private AddressBook addressBook = null;
    private Room favoriteRoom = null;

    public Model(ContentResolver contentResolver, AccountManager accountManager, Context context) {
        this.contentResolver = contentResolver;
        this.accountManager = accountManager;
        this.context = context;
    }

    public void resetDataProxy() {
        PlatformCalendarDataProxy.Mode mode = PreferenceManager.getInstance(context).getCalendarMode();
        if(mode==null)
        {
            mode = PlatformCalendarDataProxy.Mode.CALENDARS;
        }

        proxy = new PlatformCalendarDataProxy(
                this.contentResolver,
                this.accountManager,
                mode);

        String usedAccount = PreferenceManager.getInstance(context).getDefaultCalendarAccount();
        ((PlatformCalendarDataProxy) proxy).setAccount(usedAccount);

        PlatformContactsAddressBook platformAddressBook = new PlatformContactsAddressBook(contentResolver);
        platformAddressBook.setAccount(usedAccount);

        this.addressBook = platformAddressBook;

        this.handler = new Handler();
        this.clearCacheLater();
    }

    Runnable clearAddressCache = new Runnable() {
        @Override
        public void run() {
            getAddressBook().refetchEntries();
            clearCacheLater();
        }
    };

    private void clearCacheLater() {
        handler.postDelayed(clearAddressCache, ADDRESS_CACHE_CLEAR_INTERVAL);
    }


    public DataProxy getDataProxy() {
        if(this.proxy == null)
            resetDataProxy();
        return this.proxy;
    }

    public AddressBook getAddressBook() {
        return this.addressBook;
    }

    public Room getFavoriteRoom() {
        String roomName = PreferenceManager.getInstance(this.context).getSelectedRoom();
        if (roomName != null) {
            if (this.favoriteRoom != null && this.favoriteRoom.getName() == roomName)
                return this.favoriteRoom;
            try {
                this.favoriteRoom = this.getDataProxy().getRoomWithName(roomName);
                } catch (ReservatorException ex) {
                return null;
                }
            return this.favoriteRoom;
        }
        return null;
    }

}
