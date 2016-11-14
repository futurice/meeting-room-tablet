package com.futurice.android.reservator;

import java.util.ArrayList;
import java.util.HashSet;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.futurice.android.reservator.common.PreferenceManager;
import com.futurice.android.reservator.model.AddressBook;
import com.futurice.android.reservator.model.DataProxy;
import com.futurice.android.reservator.model.platformcalendar.PlatformCalendarDataProxy;
import com.futurice.android.reservator.model.platformcontacts.PlatformContactsAddressBook;

public class SettingsActivity extends ReservatorActivity {
    private final String GOOGLE_ACCOUNT_TYPE = "com.google";
    Spinner usedAccountView;
    Spinner roomNameView;
    ToggleButton addressBookOptionView;
    Spinner usedReservationAccount;
    ToggleButton resourceCalendarOptionView;
    LinearLayout roomsListLinearLayout;


    DataProxy proxy;
    PreferenceManager preferences;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        preferences = PreferenceManager.getInstance(this);

        resourceCalendarOptionView = (ToggleButton) findViewById(R.id.roomResourceCalendarOption);
        roomsListLinearLayout = (LinearLayout) findViewById(R.id.roomsLinearLayout);
        usedAccountView = (Spinner) findViewById(R.id.usedAccountSpinner);
        addressBookOptionView = (ToggleButton) findViewById(R.id.usedAddressBookOption);
        usedReservationAccount = (Spinner) findViewById(R.id.defaultReservationAccount);
        roomNameView = (Spinner) findViewById(R.id.roomNameSpinner);

        findViewById(R.id.removeUserDataButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // credentials
                preferences.removeAllSettings();
                Toast.makeText(getApplicationContext(), "Removed credentials and reseted settings", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        reloadSettings();

    }

    @Override
    public void onPause() {
        super.onPause();
        save();
    }


    private void save()
    {
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

        HashSet<String> unselectedRooms = new HashSet<String>();
        for (int i=0; i<roomsListLinearLayout.getChildCount(); i++)
        {
            CheckBox cb = (CheckBox) roomsListLinearLayout.getChildAt(i);
            if(cb==null) continue;

            if(cb.isChecked()==false){
                unselectedRooms.add(cb.getText().toString());
            }
        }

        PlatformCalendarDataProxy.Mode m = PlatformCalendarDataProxy.Mode.CALENDARS;
        if(resourceCalendarOptionView.isChecked()) m = PlatformCalendarDataProxy.Mode.RESOURCES;

        Object selectedResAccountName = usedReservationAccount.getSelectedItem();
        String selectedResAccount = null;
        if (selectedResAccountName != null && !addressBookOptionView.isChecked()) {
            selectedResAccount = selectedResAccountName.toString().trim();
        }


        preferences.setDefaultCalendarAccount(selectedAccount);
        preferences.setSelectedRoom(roomName);
        preferences.setAddressBookEnabled(addressBookOptionView.isChecked());
        preferences.setDefaultUserName(selectedResAccount);
        preferences.setUnselectedRooms(unselectedRooms);
        preferences.setCalendarMode(m);


        // Update proxies
        getResApplication().resetDataProxy();
        Toast.makeText(getApplicationContext(), "Settings saved", Toast.LENGTH_SHORT).show();

        System.out.println("Settings saved");

    }

    private void reloadSettings()
    {
        proxy = getResApplication().getDataProxy();

        refreshGoogleAccountsSpinner();
        refreshResAccountSpinner();
        refreshDefaultRoomSpinner();
        refreshRoomSelectionNamesList();
        refreshAddressBookToggle();
        refreshResourceCalendarToggle();
    }



    private void refreshResourceCalendarToggle()
    {
        resourceCalendarOptionView.setOnCheckedChangeListener(null);
        resourceCalendarOptionView.setChecked(preferences.getCalendarMode()== PlatformCalendarDataProxy.Mode.RESOURCES);
        resourceCalendarOptionView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                save();
                reloadSettings();
            }
        });
    }

    private void refreshGoogleAccountsSpinner() {

        usedAccountView.setOnItemSelectedListener(null);

        String selected = null;
        if (usedAccountView.getSelectedItem() != null) {
            selected = (String) usedAccountView.getSelectedItem();
        }
        else {
            selected = preferences.getDefaultCalendarAccount();
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

        usedAccountView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(!usedAccountView.getSelectedItem().toString().trim().equals(preferences.getDefaultCalendarAccount()))
                {
                    save();
                    reloadSettings();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


    }

    private void refreshAddressBookToggle()
    {
        // Require weather reservation requires address book contacts or not
        addressBookOptionView.setChecked(preferences.getAddressBookEnabled());

        if (addressBookOptionView.isChecked()) {
            usedReservationAccount.setVisibility(View.GONE);
            findViewById(R.id.defaultReservationAccountLabel).setVisibility(View.GONE);
        }

        addressBookOptionView.setOnCheckedChangeListener(null);
        addressBookOptionView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                save();
                reloadSettings();
            }
        });

    }

    private void refreshResAccountSpinner() {
        String selected = null;
        if (usedReservationAccount.getSelectedItem() != null) {
            selected = (String) usedReservationAccount.getSelectedItem();
        }
        else {
            selected = preferences.getDefaultUserName();
        }

        ArrayAdapter<String> adapter;
        ArrayList<String> accounts = new ArrayList<String>();
        for (Account account : AccountManager.get(this).getAccountsByType(GOOGLE_ACCOUNT_TYPE)) {
            accounts.add(account.name);
        }

        adapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, accounts);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        usedReservationAccount.setAdapter(adapter);
        if (selected != null && accounts.contains(selected)) {
            usedReservationAccount.setSelection(accounts.indexOf(selected));
        } else {
            usedReservationAccount.setSelection(0);
        }


        // hide when address book is required
        if (preferences.getAddressBookEnabled())
        {
            usedReservationAccount.setVisibility(View.GONE);
            findViewById(R.id.defaultReservationAccountLabel).setVisibility(View.GONE);
        }
        else {
            usedReservationAccount.setVisibility(View.VISIBLE);
            findViewById(R.id.defaultReservationAccountLabel).setVisibility(View.VISIBLE);
        }

    }

    private void refreshDefaultRoomSpinner() {
        String selected = null;
        if (roomNameView.getSelectedItem() != null) {
            selected = (String) roomNameView.getSelectedItem();
        } else {
            selected = preferences.getSelectedRoom();
        }

        ArrayList<String> roomNames = proxy.getRoomNames();
        HashSet<String> unselectedRooms = preferences.getUnselectedRooms();

        ArrayList<String> selectedRooms = new ArrayList<String>(roomNames);
        selectedRooms.removeAll(unselectedRooms);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
            this, android.R.layout.simple_spinner_item, selectedRooms);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roomNameView.setAdapter(adapter);
        if (selected != null && selectedRooms.contains(selected)) {
            roomNameView.setSelection(selectedRooms.indexOf(selected));
        }

    }


    public void refreshRoomSelectionNamesList()
    {

        ArrayList<String> roomNames = proxy.getRoomNames();
        HashSet<String> unselectedRooms = preferences.getUnselectedRooms();

        roomsListLinearLayout.removeAllViews();
        for(String roomName: roomNames)
        {
            CheckBox cb = new CheckBox(this);
            cb.setText(roomName);
            cb.setTextColor(ContextCompat.getColor(this,android.R.color.background_dark));
            cb.setChecked(!unselectedRooms.contains(roomName));
            roomsListLinearLayout.addView(cb);
        }

    }


}
