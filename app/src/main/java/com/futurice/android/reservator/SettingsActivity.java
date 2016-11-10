package com.futurice.android.reservator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.futurice.android.reservator.common.PreferenceManager;
import com.futurice.android.reservator.model.AddressBook;
import com.futurice.android.reservator.model.DataProxy;
import com.futurice.android.reservator.model.platformcalendar.PlatformCalendarDataProxy;
import com.futurice.android.reservator.model.platformcontacts.PlatformContactsAddressBook;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.view.SettingsRoomRowAdapter;

public class SettingsActivity extends ReservatorActivity {
    private final String GOOGLE_ACCOUNT_TYPE = "com.google";
    Spinner usedAccountView;
    Spinner roomNameView;
    ToggleButton addressBookOptionView;
    Spinner usedReservationAccount;
    DataProxy proxy;
    PreferenceManager settings;
    HashSet<String> unselectedRooms;
    ArrayList<String> roomNames;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        proxy = getResApplication().getDataProxy();

        try {
            roomNames = proxy.getRoomNames();
            roomNames.add(getString(R.string.lobbyRoomName));
        } catch (ReservatorException e) {
            Toast err = Toast.makeText(getResApplication(), e.getMessage(),
                Toast.LENGTH_LONG);
            err.show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        settings = PreferenceManager.getInstance(this);
        unselectedRooms = new HashSet<String>(settings.getUnselectedRooms());

        ListView l = (ListView) findViewById(R.id.roomListView);
        SettingsRoomRowAdapter roomListAdapter = new SettingsRoomRowAdapter(this, R.layout.settings_select_room_row, roomNames);
        l.setAdapter(roomListAdapter);


        // Set back the recorded settings
        usedAccountView = (Spinner) findViewById(R.id.usedAccountSpinner);
        String usedAccount = settings.getDefaultCalendarAccount();
        refreshGoogleAccountsSpinner();

        // Require weather reservation requires address book contacts or not
        addressBookOptionView = (ToggleButton) findViewById(R.id.usedAddressBookOption);
        addressBookOptionView.setChecked(settings.getAddressBookEnabled());

        usedReservationAccount = (Spinner) findViewById(R.id.defaultReservationAccount);
        String usedResAccount = settings.getDefaultUserName();
        if (addressBookOptionView.isChecked()) {
            usedReservationAccount.setVisibility(View.GONE);
            findViewById(R.id.defaultReservationAccountLabel).setVisibility(View.GONE);
        }
        refreshResAccountSpinner();

        @SuppressWarnings("unchecked")
        ArrayAdapter<String> usedAccountAdapter = (ArrayAdapter<String>) usedAccountView.getAdapter();
        int spinnerPosition = 0;
        if (usedAccountAdapter != null && usedAccountAdapter.getPosition(usedAccount) >= 0) {
            spinnerPosition = usedAccountAdapter.getPosition(usedAccount);
        }
        usedAccountView.setSelection(spinnerPosition);

        ArrayAdapter<String> usedResAccountAdapter = (ArrayAdapter<String>) usedReservationAccount.getAdapter();
        spinnerPosition = 0;
        if (usedResAccountAdapter != null && usedResAccountAdapter.getPosition(usedResAccount) >= 0) {
            spinnerPosition = usedResAccountAdapter.getPosition(usedResAccount);
        }
        usedReservationAccount.setSelection(spinnerPosition);


        roomNameView = (Spinner) findViewById(R.id.roomNameSpinner);
        String roomName = settings.getSelectedRoom();

        refreshRoomNamesSpinner();

        @SuppressWarnings("unchecked")
        ArrayAdapter<String> roomNameAdapter = (ArrayAdapter<String>) roomNameView.getAdapter();
        spinnerPosition = 0;
        if (roomNameAdapter != null) {
            spinnerPosition = roomNameAdapter.getPosition(roomName);
        }
        roomNameView.setSelection(spinnerPosition);


        // Setup button for removing log
        findViewById(R.id.removeUserDataButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // credentials
                settings.removeAllSettings();
                Toast.makeText(getApplicationContext(), "Removed credentials and reseted settings", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        // TODO: save button?
        // Save the settings
        Object selectedAccountName = usedAccountView.getSelectedItem();
        String selectedAccount = "";
        if (selectedAccountName != null) {
            selectedAccount = selectedAccountName.toString().trim();
        }

        Object selectedRoomName = roomNameView.getSelectedItem();
        String roomName = "";
        if (selectedRoomName != null) {
            roomName = selectedRoomName.toString().trim();
        }

        Object selectedResAccountName = usedReservationAccount.getSelectedItem();
        String selectedResAccount = null;
        if (selectedResAccountName != null) {
            selectedResAccount = selectedResAccountName.toString().trim();
        }


        settings.setDefaultCalendarAccount(selectedAccount);
        settings.setSelectedRoom(roomName);
        settings.setAddressBookEnabled(addressBookOptionView.isChecked());
        settings.setDefaultUserName(selectedResAccount);


        // Update proxies
        if (proxy instanceof PlatformCalendarDataProxy) {
            ((PlatformCalendarDataProxy) proxy).setAccount(selectedAccount);
        }

        AddressBook ab = getResApplication().getAddressBook();
        if (ab instanceof PlatformContactsAddressBook) {
            ((PlatformContactsAddressBook) ab).setAccount(selectedAccount);
        }
        Toast.makeText(getApplicationContext(), "Settings saved", Toast.LENGTH_SHORT).show();
    }

    private void refreshGoogleAccountsSpinner() {
        String selected = null;
        if (usedAccountView.getSelectedItem() != null) {
            selected = (String) usedAccountView.getSelectedItem();
        }

        ArrayAdapter<String> adapter;
        ArrayList<String> accounts = new ArrayList<String>();
        for (Account account : AccountManager.get(this).getAccountsByType(GOOGLE_ACCOUNT_TYPE)) {
            accounts.add(account.name);
        }

        adapter = new ArrayAdapter<String>(
            this, android.R.layout.simple_spinner_item, accounts);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        usedAccountView.setAdapter(adapter);
        if (selected != null && accounts.contains(selected)) {
            usedAccountView.setSelection(accounts.indexOf(selected));
        } else {
            usedAccountView.setSelection(0);
        }
    }

    private void refreshResAccountSpinner() {
        String selected = null;
        if (usedReservationAccount.getSelectedItem() != null) {
            selected = (String) usedReservationAccount.getSelectedItem();
        }

        ArrayAdapter<String> adapter;
        ArrayList<String> accounts = new ArrayList<String>();
        for (Account account : AccountManager.get(this).getAccountsByType(GOOGLE_ACCOUNT_TYPE)) {
            accounts.add(account.name);
        }

        adapter = new ArrayAdapter<String>(
            this, android.R.layout.simple_spinner_item, accounts);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        usedReservationAccount.setAdapter(adapter);
        if (selected != null && accounts.contains(selected)) {
            usedReservationAccount.setSelection(accounts.indexOf(selected));
        } else {
            usedReservationAccount.setSelection(0);
        }
    }

    private void refreshRoomNamesSpinner() {
        String selected = null;
        if (roomNameView.getSelectedItem() != null) {
            selected = (String) roomNameView.getSelectedItem();
        }
        ArrayAdapter<String> adapter;

        ArrayList<String> selectedRooms = new ArrayList<String>(roomNames);
        selectedRooms.removeAll(unselectedRooms);

        adapter = new ArrayAdapter<String>(
            this, android.R.layout.simple_spinner_item, selectedRooms);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roomNameView.setAdapter(adapter);
        if (selected != null && selectedRooms.contains(selected)) {
            roomNameView.setSelection(selectedRooms.indexOf(selected));
        }
    }

    public void roomRowClicked(final View view) {
        if (view instanceof CheckBox) {

            CheckBox c = (CheckBox) view;
            // checked = "not unselected". sorry!
            if (c.isChecked()) {
                unselectedRooms.remove(c.getText().toString());
            } else {
                unselectedRooms.add(c.getText().toString());
            }

            settings.setUnselectedRooms(unselectedRooms);

            refreshRoomNamesSpinner();
        }
    }

    public void onToggleClicked(final View view) {
        if (view instanceof ToggleButton) {
            Boolean checked = ((ToggleButton) addressBookOptionView).isChecked();

            if (checked) {
                usedReservationAccount.setVisibility(View.GONE);
                findViewById(R.id.defaultReservationAccountLabel).setVisibility(View.INVISIBLE);
            } else {
                usedReservationAccount.setVisibility(View.VISIBLE);
                findViewById(R.id.defaultReservationAccountLabel).setVisibility(View.VISIBLE);
            }
        }
    }
}
