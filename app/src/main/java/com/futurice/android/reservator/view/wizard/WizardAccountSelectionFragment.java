package com.futurice.android.reservator.view.wizard;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.github.paolorotolo.appintro.ISlidePolicy;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shoj on 10/11/2016.
 */

public final class WizardAccountSelectionFragment extends android.support.v4.app.Fragment implements  ISlidePolicy {

    RadioGroup accountsRadioGroup = null;
    String[] accounts = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.wizard_account_selection, container, false);
        accountsRadioGroup = (RadioGroup) view.findViewById(R.id.wizard_accounts_radiogroup);

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

    @Override
    public void onResume() {
        super.onResume();

        final String[] accounts = getAvailableAccounts();
        if(accounts.length <= 0)
        {
            showNoAccountsErrorMessage();
            return;
        }

        accountsRadioGroup.removeAllViews();
        for (String account: accounts)
        {
            RadioButton accountRadioButton = new RadioButton(getActivity());
            accountRadioButton.setText(account);

            float scale = getResources().getDisplayMetrics().density;
            int dpAsPixels = (int) (15*scale + 0.5f);
            accountRadioButton.setPadding(dpAsPixels,dpAsPixels,dpAsPixels,dpAsPixels);

            accountsRadioGroup.addView(accountRadioButton);
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

    private void showNoAccountsErrorMessage()
    {
        String errorMessage = getString(R.string.noCalendarsError);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(errorMessage)
                .setTitle(R.string.calendarError)
                .setPositiveButton(R.string.goToAccountSettings, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        getActivity().startActivityForResult(new Intent(android.provider.Settings.ACTION_SYNC_SETTINGS), 0);
                    }
                });
        builder.create().show();

    }



    @Override
    public boolean isPolicyRespected() {
        return accountsRadioGroup.getCheckedRadioButtonId() != -1;
    }

    @Override
    public void onUserIllegallyRequestedNextPage() {
    }


}

