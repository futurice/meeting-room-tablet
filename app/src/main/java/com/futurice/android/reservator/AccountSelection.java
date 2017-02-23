package com.futurice.android.reservator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.content.Intent;
import android.accounts.Account;
import android.accounts.AccountManager;

import java.util.List;
import java.util.ArrayList;


public class AccountSelection extends ReservatorActivity {
    static final int REQUEST_LOBBY = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.account_selection);
        selectAccount();
    }

    @Override
    public void onResume() {
        super.onResume();
        selectAccount();
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
        for (Account account : AccountManager.get(this).getAccounts()) {
            accountsList.add(account.name);
        }
        return accountsList.toArray(new String[accountsList.size()]);
    }

    public void selectAccount() {
        final SharedPreferences preferences = getSharedPreferences(this.getString(R.string.PREFERENCES_NAME), Context.MODE_PRIVATE);
        final String selectedAccount = preferences.getString(getString(R.string.accountForServation), "");
        boolean addressBookOption = preferences.getBoolean("addressBookOption", false);

        if (selectedAccount == "" && !addressBookOption) {
            final String[] values = fetchAccounts();

            // Only one Google account available so the selection isn't needed.
            if (values.length == 1) {
                preferences.edit()
                        .putString(getString(R.string.accountForServation), values[0])
                        .apply();
                moveToLobby();
            } else {

                // Build an alert dialog to select the account.
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.selectAccount);
                builder.setItems(values, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        preferences.edit()
                                .putString(getString(R.string.accountForServation), values[which])
                                .apply();
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
