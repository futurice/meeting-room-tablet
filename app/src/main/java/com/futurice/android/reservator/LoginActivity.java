package com.futurice.android.reservator;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import android.content.DialogInterface;

import com.futurice.android.reservator.model.AddressBook;
import com.futurice.android.reservator.model.AddressBookUpdatedListener;
import com.futurice.android.reservator.model.ReservatorException;

public class LoginActivity extends ReservatorActivity implements AddressBookUpdatedListener {

    static final int REQUEST_LOBBY = 0;
    MenuItem settingsMenu;
    private ProgressDialog pd;
    private boolean addressBookOk = false;
    private boolean roomListOk = false;
    private SharedPreferences preferences;
    private Editor editor;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        if (pd != null) {
            pd.dismiss();
        }

        preferences = getSharedPreferences(this.getString(R.string.PREFERENCES_NAME), Context.MODE_PRIVATE);
        editor = preferences.edit();

        // Check Google Calendar
        if (getResApplication().getDataProxy().hasFatalError()) {
            showFatalErrorDialog(
                getString(R.string.calendarError),
                getString(R.string.noCalendarsError));
            return;
        } else {
            roomListOk = true;
        }

        AddressBook ab = this.getResApplication().getAddressBook();
        ab.refetchEntries();
    }

    @Override
    public void onResume() {
        super.onResume();

        AddressBook ab = this.getResApplication().getAddressBook();
        ab.addDataUpdatedListener(this);
        checkAndGo();
    }

    public void onPause() {
        super.onPause();

        AddressBook ab = this.getResApplication().getAddressBook();
        ab.removeDataUpdatedListener(this);
    }

    private void updateProgressDialogMessage() {
        if (pd == null)
            return;

        String s = "";

        if (roomListOk)
            s += getString(R.string.calendarOk)+"\n";
        else
            s += getString(R.string.calendarPending)+"\n";

        if (addressBookOk)
            s += getString(R.string.contactsOk)+"\n";
        else
            s += getString(R.string.contactsPending)+"\n";

        pd.setMessage(s);
    }

    private void checkAndGo() {
        if (addressBookOk && roomListOk) {
            editor.apply();
            if (pd != null)
                pd.dismiss();

            Intent i = new Intent(this, AccountSelection.class);
            startActivityForResult(i, REQUEST_LOBBY);
        }
    }

    @Override
    public void addressBookUpdated() {
        addressBookOk = true;
        updateProgressDialogMessage();
        checkAndGo();
    }

    @Override
    public void addressBookUpdateFailed(ReservatorException e) {
        addressBookOk = false;

        if (pd != null)
            pd.dismiss();
        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        setContentView(R.layout.login_activity);
    }

    public void showFatalErrorDialog(String title, String errorMsg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(errorMsg)
            .setTitle(title)
            .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    LoginActivity.this.finish();
                }
            });

        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        finish();
    }
}