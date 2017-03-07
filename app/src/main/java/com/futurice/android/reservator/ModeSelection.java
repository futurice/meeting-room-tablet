package com.futurice.android.reservator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;


public class ModeSelection extends ReservatorActivity {

    static final int REQUEST_LOBBY = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.account_selection);
        selectMode();
    }

    @Override
    public void onResume() {
        super.onResume();
        selectMode();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void moveToLobby() {
        Intent i = new Intent(this, LobbyActivity.class);
        startActivityForResult(i, REQUEST_LOBBY);
    }

    public void selectMode() {
        final SharedPreferences preferences = getSharedPreferences(this.getString(R.string.PREFERENCES_NAME), Context.MODE_PRIVATE);
        final String selectedMode = preferences.getString(getString(R.string.modeForCalendar), "");

        if (selectedMode == "") {
            final String[] values = {getString(R.string.calendarTitel), getString(R.string.resourcesTitel)};

            // Build an alert dialog to select the account.
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.selectMode);
            builder.setItems(values, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    preferences.edit()
                            .putString(getString(R.string.modeForCalendar), values[which])
                            .apply();
                    moveToLobby();
                }
            });

            builder.show();

        } else {
            moveToLobby();
        }
    }
}
