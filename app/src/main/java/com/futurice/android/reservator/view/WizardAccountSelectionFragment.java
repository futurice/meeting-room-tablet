package com.futurice.android.reservator.view;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.common.PreferenceManager;
import com.github.paolorotolo.appintro.ISlideBackgroundColorHolder;
import com.github.paolorotolo.appintro.ISlidePolicy;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shoj on 10/11/2016.
 */

public final class WizardAccountSelectionFragment extends android.support.v4.app.Fragment implements  ISlidePolicy {

    RadioGroup accountsRadioGroup = null;
    View view = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.wizard_account_selection, container, false);

        final String[] accounts = getAvailableAccounts();
        accountsRadioGroup = (RadioGroup) view.findViewById(R.id.wizard_accounts_radiogroup);

        for (String account: accounts)
        {
            RadioButton accountRadioButton = new RadioButton(getActivity());
            accountRadioButton.setText(account);

            float scale = getResources().getDisplayMetrics().density;
            int dpAsPixels = (int) (15*scale + 0.5f);
            accountRadioButton.setPadding(dpAsPixels,dpAsPixels,dpAsPixels,dpAsPixels);

            accountsRadioGroup.addView(accountRadioButton);
        }

        TextView title = (TextView) view.findViewById(R.id.wizard_accounts_title);
        title.setText(R.string.selectGoogleAccount);

        accountsRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                String account = ((RadioButton)group.findViewById(checkedId)).getText().toString();
                PreferenceManager.getInstance(getActivity()).setDefaultCalendarAccount(account);
                PreferenceManager.getInstance(getActivity()).setDefaultUserName(account);
            }
        });

        return view;
    }



    public String[] getAvailableAccounts()
    {
        List<String> accountsList = new ArrayList<String>();
        for (Account account : AccountManager.get(getActivity()).getAccountsByType(getString(R.string.googleAccountType))) {
            accountsList.add(account.name);
        }
        return accountsList.toArray(new String[accountsList.size()]);
    }




    @Override
    public boolean isPolicyRespected() {
        return accountsRadioGroup.getCheckedRadioButtonId() != -1;
    }

    @Override
    public void onUserIllegallyRequestedNextPage() {
    }


}

