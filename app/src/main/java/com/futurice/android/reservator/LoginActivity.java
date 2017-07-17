package com.futurice.android.reservator;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.futurice.android.reservator.common.PreferenceManager;
import com.futurice.android.reservator.model.AddressBook;
import com.futurice.android.reservator.model.AddressBookUpdatedListener;
import com.futurice.android.reservator.model.ReservatorException;

public class LoginActivity extends ReservatorActivity implements AddressBookUpdatedListener {

    private boolean addressBookOk = false;
    private boolean roomListOk = false;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (PreferenceManager.getInstance(this).getApplicationConfigured() == false)
        {
            showWizard();
            return;
        }

        setContentView(R.layout.login_activity);

        if (!havePermissions) {
            havePermissions = checkPermissions();
        }
        if (havePermissions) {
            // Check Google Calendar
            checkCalendarAndFetchEntries();
        }
    }

    private void checkCalendarAndFetchEntries() {
        if (getResApplication().getDataProxy().hasFatalError()) {
            showWizard();
            return;
        } else {
            roomListOk = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        AddressBook ab = this.getResApplication().getAddressBook();
        ab.addDataUpdatedListener(this);
        ab.refetchEntries();
        checkAndGo();
    }

    public void onPause() {
        super.onPause();

        AddressBook ab = this.getResApplication().getAddressBook();
        ab.removeDataUpdatedListener(this);
    }


    private void checkAndGo() {
        if (addressBookOk && roomListOk) {

            final Intent i = new Intent(this, LobbyActivity.class);
            startActivity(i);
        }
    }

    @Override
    public void addressBookUpdated() {
        addressBookOk = true;
        checkAndGo();
    }

    @Override
    public void addressBookUpdateFailed(ReservatorException e) {
        addressBookOk = false;
        // return to config
        showWizard();
    }

    private void showWizard()
    {
        final Intent i = new Intent(this, WizardActivity.class);
        startActivity(i);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST: {
                if (grantResults.length >= 3
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    havePermissions = true;
                    checkCalendarAndFetchEntries();
                } else {
                    finish();
                }
                return;
            }


        }
    }
}

