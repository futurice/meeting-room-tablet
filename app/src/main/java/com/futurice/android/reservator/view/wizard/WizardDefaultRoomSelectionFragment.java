package com.futurice.android.reservator.view.wizard;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.ReservatorApplication;
import com.futurice.android.reservator.common.PreferenceManager;
import com.futurice.android.reservator.model.DataProxy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by shoj on 10/11/2016.
 */

public final class WizardDefaultRoomSelectionFragment extends android.support.v4.app.Fragment {

    @BindView(R.id.wizard_accounts_radiogroup)
    RadioGroup roomRadioGroup;
    @BindView(R.id.wizard_accounts_title)
    TextView title;

    Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wizard_account_selection, container, false);
        unbinder = ButterKnife.bind(this, view);

        title.setText(R.string.defaultRoomSelectionTitle);
        roomRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                String roomName = ((RadioButton) group.findViewById(checkedId)).getText().toString();
                PreferenceManager preferences = PreferenceManager.getInstance(getActivity());
                preferences.setSelectedRoom(roomName);
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public void reloadRooms() {
        PreferenceManager preferences = PreferenceManager.getInstance(getActivity());
        ReservatorApplication application = ((ReservatorApplication) getActivity().getApplication());

        roomRadioGroup.removeAllViews();
        DataProxy proxy = application.getDataProxy();

        ArrayList<String> roomNames = proxy.getRoomNames();

        HashSet<String> unselectedRooms = preferences.getUnselectedRooms();

        for (String roomName : roomNames) {
            if (unselectedRooms.contains(roomName)) {
                continue;
            }

            RadioButton roomRadioButton = new RadioButton(getActivity());
            roomRadioButton.setText(roomName);
            roomRadioGroup.addView(roomRadioButton);
        }


    }
}
