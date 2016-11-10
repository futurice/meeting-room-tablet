package com.futurice.android.reservator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.content.Intent;
import android.accounts.Account;
import android.accounts.AccountManager;

import com.futurice.android.reservator.common.PreferenceManager;

import java.util.List;
import java.util.ArrayList;


public class AccountSelection extends ReservatorActivity {
    static final int REQUEST_LOBBY = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.account_selection);
        selectGoogleAccount();
    }

    @Override
    public void onResume() {
        super.onResume();
        selectGoogleAccount();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void moveToLobby() {
        Intent i = new Intent(this, LobbyActivity.class);
        startActivityForResult(i, REQUEST_LOBBY);
    }

    public String[] fetchAccounts() {
        List<String> accountsList = new ArrayList<String>();
        for (Account account : AccountManager.get(this).getAccountsByType(getString(R.string.googleAccountType))) {
            accountsList.add(account.name);
        }
        return accountsList.toArray(new String[accountsList.size()]);
    }

    public void selectGoogleAccount() {

        final String selectedAccount = PreferenceManager.getInstance(this).getDefaultUserName();
        boolean addressBookOption = PreferenceManager.getInstance(this).getAddressBookEnabled();

        if (selectedAccount == null && !addressBookOption) {
            final String[] values = fetchAccounts();

            // Only one Google account available so the selection isn't needed.
            if (values.length == 1) {
                PreferenceManager.getInstance(this).setDefaultUserName(values[0]);
                moveToLobby();
            } else {

                // Build an alert dialog to select the account.
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.selectGoogleAccount);
                builder.setItems(values, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        PreferenceManager.getInstance(AccountSelection.this).setDefaultUserName(values[which]);
                        moveToLobby();
                    }
                });

                builder.show();
            }
        } else {
            moveToLobby();
        }
    }
}
