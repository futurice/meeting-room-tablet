package com.futurice.android.reservator.view.wizard;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.ReservatorApplication;
import com.futurice.android.reservator.WizardActivity;
import com.futurice.android.reservator.common.PreferenceManager;
import com.futurice.android.reservator.model.DataProxy;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.platformcalendar.PlatformCalendarDataProxy;
import com.github.paolorotolo.appintro.ISlidePolicy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by shoj on 10/11/2016.
 */

public final class WizardDefaultRoomSelectionFragment extends android.support.v4.app.Fragment {

    RadioGroup roomRadioGroup = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.wizard_account_selection, container, false);

        roomRadioGroup = (RadioGroup) view.findViewById(R.id.wizard_accounts_radiogroup);
        TextView title = (TextView) view.findViewById(R.id.wizard_accounts_title);
        title.setText(R.string.defaultRoomSelectionTitle);


        roomRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                String roomName = ((RadioButton)group.findViewById(checkedId)).getText().toString();
                PreferenceManager preferences = PreferenceManager.getInstance(getActivity());
                preferences.setSelectedRoom(roomName);
            }
        });

        return view;
    }

    public void reloadRooms()
    {

        PreferenceManager preferences = PreferenceManager.getInstance(getActivity());
        ReservatorApplication application = ((ReservatorApplication) getActivity().getApplication());

        roomRadioGroup.removeAllViews();
        DataProxy proxy = application.getDataProxy();

        ArrayList<String> roomNames = proxy.getRoomNames();

        HashSet<String> unselectedRooms = preferences.getUnselectedRooms();

        for(String roomName: roomNames)
        {
            if(unselectedRooms.contains(roomName)) {
                continue;
            }

            RadioButton roomRadioButton = new RadioButton(getActivity());
            roomRadioButton.setText(roomName);
            roomRadioGroup.addView(roomRadioButton);
        }


    }

    public String[] getAvailableAccounts()
    {
        List<String> accountsList = new ArrayList<String>();
        for (Account account : AccountManager.get(getActivity()).getAccountsByType(getString(R.string.googleAccountType))) {
            accountsList.add(account.name);
        }
        return accountsList.toArray(new String[accountsList.size()]);
    }


}
