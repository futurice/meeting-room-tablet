package com.futurice.android.reservator.model.platformcontacts;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.futurice.android.reservator.model.AddressBook;
import com.futurice.android.reservator.model.AddressBookEntry;
import com.futurice.android.reservator.model.ReservatorException;

import java.util.Vector;

public class PlatformContactsAddressBook extends AddressBook {
    private final String GOOGLE_ACCOUNT_TYPE = "com.google";
    private String account = null;
    private ContentResolver resolver;

    public PlatformContactsAddressBook(ContentResolver resolver) {
        super();
        this.resolver = resolver;
    }

    /**
     * This is a heavy query, and should be called rarely.
     */
    @Override
    protected Vector<AddressBookEntry> fetchEntries() throws ReservatorException {
        Vector<AddressBookEntry> entries = new Vector<AddressBookEntry>();

        String[] mProjection = {
                ContactsContract.RawContacts._ID,
                ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY};

        String mSelectionClause =
                ContactsContract.RawContacts.ACCOUNT_TYPE + " = ? AND " +
                        ContactsContract.RawContacts.DELETED + " = 0";

        String[] mSelectionArgs;
        if (account != null) {
            mSelectionClause = mSelectionClause + " AND " + ContactsContract.RawContacts.ACCOUNT_NAME + " = ?";
            mSelectionArgs = new String[]{GOOGLE_ACCOUNT_TYPE, account};
        } else {
            mSelectionArgs = new String[]{GOOGLE_ACCOUNT_TYPE};
        }

        String mSortOrder = null;

        Cursor result = resolver.query(
                ContactsContract.RawContacts.CONTENT_URI,
                mProjection,
                mSelectionClause,
                mSelectionArgs,
                mSortOrder);

        if (result != null) {
            if (result.getCount() > 0) {
                result.moveToFirst();
                do {
                    long contactId = result.getLong(0);
                    String contactName = result.getString(1);
                    String contactEmail = fetchContactEmail(contactId);

                    if (contactEmail != null) {
                        entries.add(new AddressBookEntry(contactName, contactEmail));
                    }
                } while (result.moveToNext());
            }
            result.close();
        }

        return entries;
    }

    private String fetchContactEmail(long rawContactId) {
        String[] mProjection = {ContactsContract.CommonDataKinds.Email.ADDRESS};
        String mSelectionClause = ContactsContract.CommonDataKinds.Email.RAW_CONTACT_ID + " = " + rawContactId;
        String[] mSelectionArgs = {};
        String mSortOrder = null;

        Cursor result = resolver.query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                mProjection,
                mSelectionClause,
                mSelectionArgs,
                mSortOrder);

        if (result != null) {
            if (result.getCount() > 0) {
                result.moveToFirst();
                String email = result.getString(0);
                if (!email.isEmpty()) {
                    result.close();
                    return email;
                }
            }
            result.close();
        }
        return null;
    }

    @Override
    public void setCredentials(String username, String password) {
        // No-op
    }

    public String getAccount() {
        return this.account;
    }

    public void setAccount(String account) {
        String oldAccount = this.account;
        this.account = account;

        // Has the account changed?
        if (!(account == null ? oldAccount == null : account.equals(oldAccount))) {
            refetchEntries();
        }
    }
}
