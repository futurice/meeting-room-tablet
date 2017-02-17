package com.futurice.android.reservator.controller;

import android.content.Context;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.ReservatorApplication;
import com.futurice.android.reservator.model.AddressBookEntry;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.Room;
import com.futurice.android.reservator.model.TimeSpan;


public class MakeReservations {

    public void doReservation(ReservatorApplication application, String name,
                              Room room, TimeSpan timeSpan, String meetingTitle) throws ReservatorException {
        Context context = application.getApplicationContext();
        AddressBookEntry entry = application.getAddressBook().getEntryByName(name);
        Boolean addressBookOption = application.getBooleanSettingsValue("addressBookOption", false);

        if (entry == null && addressBookOption) {
            throw new ReservatorException(context.getString(R.string.faildUser));
        }

        if (entry != null) {
            application.getDataProxy().reserve(room, timeSpan, entry.getName(), entry.getEmail(),entry.getName());
        } else {
            // Address book option is off so reserve the room with the selected account in settings.
            String accountEmail = application.getSettingValue(R.string.accountForServation, "");

            if (accountEmail.equals("")) {
                throw new ReservatorException(context.getString(R.string.faildCheckSettings));
            }

            String title = meetingTitle;

            if (title.equals("")) {
                title = application.getString(R.string.defaultTitleForReservation);
            }

            application.getDataProxy().reserve(room, timeSpan, name, accountEmail,title);
        }
    }

}
