package com.futurice.android.reservator;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.futurice.android.reservator.model.AddressBook;
import com.futurice.android.reservator.model.DataProxy;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.platformcalendar.PlatformCalendarDataProxy;
import com.futurice.android.reservator.model.platformcontacts.PlatformContactsAddressBook;
import com.futurice.android.reservator.view.SettingsRoomRowAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SettingsActivity extends ReservatorActivity {
    Spinner usedAccountView;
    Spinner roomNameView;
    ToggleButton addressBookOptionView;
    Spinner usedReservationAccount;
    DataProxy proxy;
    SharedPreferences settings;
    HashSet<String> unselectedRooms;
    ArrayList<String> roomNames;
    private  Spinner languageSpinner;
    private Spinner meetingDesignationView;
    private Spinner reservationViewSpinner;

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
        settings = getSharedPreferences(getString(R.string.PREFERENCES_NAME), Context.MODE_PRIVATE);
        unselectedRooms = new HashSet<String>(settings.getStringSet(getString(R.string.PREFERENCES_UNSELECTED_ROOMS), new HashSet<String>()));

        ListView l = (ListView) findViewById(R.id.roomListView);
        SettingsRoomRowAdapter roomListAdapter = new SettingsRoomRowAdapter(this, R.layout.settings_select_room_row, roomNames);
        l.setAdapter(roomListAdapter);


        // Set back the recorded settings
        usedAccountView = (Spinner) findViewById(R.id.usedAccountSpinner);
        String usedAccount = settings.getString(
            getString(R.string.PREFERENCES_ACCOUNT),
            getString(R.string.allAccountsMagicWord));
        refreshGoogleAccountsSpinner();

        // Require weather reservation requires address book contacts or not
        addressBookOptionView = (ToggleButton) findViewById(R.id.usedAddressBookOption);
        addressBookOptionView.setChecked(settings.getBoolean("addressBookOption", false));

        usedReservationAccount = (Spinner) findViewById(R.id.defaultReservationAccount);
        String usedResAccount = settings.getString(
            getString(R.string.accountForServation),
            "");
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
        String roomName = settings.getString(getString(R.string.PREFERENCES_ROOM_NAME), "");

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
                Editor editor = settings.edit();
                Map<String, ?> keys = settings.getAll();
                for (Map.Entry<String, ?> entry : keys.entrySet()) {
                    editor.remove(entry.getKey());
                }
                editor.apply();
                Toast.makeText(getApplicationContext(), getString(R.string.removeUserData), Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        Locale currentLocal = this.getResources().getConfiguration().locale;
        languageSpinner = (Spinner) findViewById(R.id.languageSpinner);
        refreshLanguageSpinner(currentLocal);

        meetingDesignationView = (Spinner) findViewById(R.id.meetingDesignationSpinner);
        String designation = settings.getString("meetingDesignation","");
        refreshMeetingTitleSpinner();

        @SuppressWarnings("unchecked")
        ArrayAdapter<String> meetingDesignationAdapter = (ArrayAdapter<String>) meetingDesignationView.getAdapter();
        spinnerPosition = 0;
        if (meetingDesignationAdapter != null) {
            spinnerPosition = meetingDesignationAdapter.getPosition(designation);
        }
        meetingDesignationView.setSelection(spinnerPosition);

        reservationViewSpinner = (Spinner) findViewById(R.id.reservationViewSpinner);
        String resView = settings.getString("reservationView","");
        refreshReservationSpinner();

        @SuppressWarnings("unchecked")
        ArrayAdapter<String> resViewAdapter = (ArrayAdapter<String>) reservationViewSpinner.getAdapter();
        spinnerPosition = 0;
        if (resViewAdapter != null) {
            spinnerPosition = resViewAdapter.getPosition(resView);
        }
        reservationViewSpinner.setSelection(spinnerPosition);

        findViewById(R.id.saveSettings).setOnClickListener(getSaveClickListener());
    }

    @Override
    public void onPause() {
        super.onPause();
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
        String selectedResAccount = "";
        if (selectedResAccountName != null) {
            selectedResAccount = selectedResAccountName.toString().trim();
        }

        Object selectedResView = reservationViewSpinner.getSelectedItem();
        String selectedReservationView = "";
        if (selectedResView != null){
            selectedReservationView = selectedResView.toString().trim();
        }

        Object selectedLocal = languageSpinner.getSelectedItem();
        String selectedLocalLanguage = "";
        if (selectedResView != null){
            selectedLocalLanguage = selectedLocal.toString().trim();
        }

        Object selectedMeetingTitle = meetingDesignationView.getSelectedItem();
        String selectedmeetingTitelDesignation = "";
        if (selectedResView != null){
            selectedmeetingTitelDesignation = selectedMeetingTitle.toString().trim();
        }

        Editor editor = settings.edit();
        editor.putString(getString(R.string.PREFERENCES_ACCOUNT), selectedAccount);

        if(selectedAccount.equals(getString(R.string.allAccountsMagicWord))) {
            editor.putString(getString(R.string.PREFERENCES_ACCOUNT_TYPE), "");
        } else {
            editor.putString(getString(R.string.PREFERENCES_ACCOUNT_TYPE), selectedAccount.substring(selectedAccount.indexOf("@"), selectedAccount.length()));
        }
        editor.putString(getString(R.string.PREFERENCES_ROOM_NAME), roomName);
        editor.putBoolean("addressBookOption", addressBookOptionView.isChecked());
        editor.putString(getString(R.string.accountForServation), selectedResAccount);
        editor.putString("reservationView",selectedReservationView);
        editor.putString("local",selectedLocalLanguage);
        editor.putString("meetingDesignation",selectedmeetingTitelDesignation);

        editor.apply();

        // Update proxies
        if (proxy instanceof PlatformCalendarDataProxy) {
            if (selectedAccount.equals(getString(R.string.allAccountsMagicWord))) {
                ((PlatformCalendarDataProxy) proxy).setAccount(null);
            } else {
                ((PlatformCalendarDataProxy) proxy).setAccount(selectedAccount);
            }
        }

        AddressBook ab = getResApplication().getAddressBook();
        if (ab instanceof PlatformContactsAddressBook) {
            if (selectedAccount.equals(getString(R.string.allAccountsMagicWord))) {
                ((PlatformContactsAddressBook) ab).setAccount(null);
            } else {
                ((PlatformContactsAddressBook) ab).setAccount(selectedAccount);
            }
        }
        Toast.makeText(getApplicationContext(), getString(R.string.settingsSaved), Toast.LENGTH_SHORT).show();
    }

    private void refreshGoogleAccountsSpinner() {
        String selected = null;
        if (usedAccountView.getSelectedItem() != null) {
            selected = (String) usedAccountView.getSelectedItem();
        }

        ArrayAdapter<String> adapter;
        ArrayList<String> accounts = new ArrayList<String>();
        accounts.add(getString(R.string.allAccountsMagicWord));
        for (Account account : AccountManager.get(this).getAccounts()) {
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
        for (Account account : AccountManager.get(this).getAccounts()) {
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
            Editor editor = settings.edit();

            CheckBox c = (CheckBox) view;
            // checked = "not unselected". sorry!
            if (c.isChecked()) {
                unselectedRooms.remove(c.getText().toString());
            } else {
                unselectedRooms.add(c.getText().toString());
            }

            // Create MakeReservationTask new HashSet, because...
            // http://stackoverflow.com/questions/14034803/misbehavior-when-trying-to-store-a-string-set-using-sharedpreferences
            editor.putStringSet(getString(R.string.PREFERENCES_UNSELECTED_ROOMS), new HashSet<String>(unselectedRooms));
            editor.commit();

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

    private void refreshLanguageSpinner(Locale currentLocal) {
        List<String> laguages = new ArrayList<>();
        laguages.add(getString(R.string.language_De));
        laguages.add(getString(R.string.language_En));

        ArrayAdapter<String> laguageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, laguages);
        laguageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(laguageAdapter);

        String currentLanguage = currentLocal.getLanguage();
        if (currentLanguage.equals("en")) {
            languageSpinner.setSelection(1);
        }

        final int selectedItem = languageSpinner.getSelectedItemPosition();

        languageSpinner.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int itemPosition, long l) {
                if (itemPosition == selectedItem) {
                    return;
                } else {
                    if (itemPosition == 0) {
                        setLocale("de");
                    } else {
                        setLocale("en");
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setLocale(String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        this.getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        this.setContentView(R.layout.settings_activity);
        onResume();
    }

    private void refreshMeetingTitleSpinner() {
        List<String> views = new ArrayList<>();
        views.add(getString(R.string.meetingTitlePersonName));
        views.add(getString(R.string.meetingTitleMeetingName));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, views);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        meetingDesignationView.setAdapter(adapter);

        meetingDesignationView.setSelection(0);
    }

    private void refreshReservationSpinner() {
        List<String> views = new ArrayList<>();
        views.add(getString(R.string.reserv_view_spinner_default));
        views.add(getString(R.string.reserv_view_spinner_change));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, views);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reservationViewSpinner.setAdapter(adapter);

        reservationViewSpinner.setSelection(0);
    }

    private OnClickListener getSaveClickListener(){
        return new OnClickListener() {
            @Override
            public void onClick(View view) {
                onPause();
                finish();
            }
        };
    }
}