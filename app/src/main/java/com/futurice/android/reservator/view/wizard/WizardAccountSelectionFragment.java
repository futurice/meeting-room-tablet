package com.futurice.android.reservator.view.wizard;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by shoj on 10/11/2016.
 */

public final class WizardAccountSelectionFragment
        extends android.support.v4.app.Fragment implements ISlidePolicy {

    @BindView(R.id.wizard_accounts_radiogroup)
    RadioGroup accountsRadioGroup = null;
    @BindView(R.id.wizard_accounts_title)
    TextView title;

    Unbinder unbinder;

    AlertDialog alertDialog;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.wizard_account_selection, container, false);
        unbinder = ButterKnife.bind(this, view);
        title.setText(R.string.selectGoogleAccount);
        accountsRadioGroup.setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(
                            RadioGroup group, int checkedId) {
                        String account =
                                ((RadioButton) group.findViewById(checkedId))
                                        .getText().toString();
                        PreferenceManager.getInstance(getActivity())
                                .setDefaultCalendarAccount(account);
                        PreferenceManager.getInstance(getActivity())
                                .setDefaultUserName(account);
                    }
                });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onResume() {
        super.onResume();

        final String[] accounts = getAvailableAccounts();
        if (accounts.length <= 0) {
            showNoAccountsErrorMessage();
            return;
        }

        accountsRadioGroup.removeAllViews();
        for (String account : accounts) {
            RadioButton accountRadioButton = new RadioButton(getActivity());
            accountRadioButton.setText(account);

            float scale = getResources().getDisplayMetrics().density;
            int dpAsPixels = (int) (15 * scale + 0.5f);
            accountRadioButton
                    .setPadding(dpAsPixels, dpAsPixels, dpAsPixels, dpAsPixels);

            accountsRadioGroup.addView(accountRadioButton);
        }

    }

    public String[] getAvailableAccounts() {
        List<String> accountsList = new ArrayList<String>();
        for (Account account : AccountManager.get(getActivity())
                .getAccountsByType(null)) {
            accountsList.add(account.name);
        }
        return accountsList.toArray(new String[accountsList.size()]);
    }

    private void showNoAccountsErrorMessage() {
        String errorMessage = getString(R.string.noCalendarsError);
        final AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity());
        builder.setMessage(errorMessage)
                .setTitle(R.string.calendarError)
                .setPositiveButton(R.string.goToAccountSettings,
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface dialog, int id) {
                                dialog.dismiss();
                                getActivity().startActivityForResult(
                                        new Intent(
                                                android.provider.Settings.ACTION_SYNC_SETTINGS),
                                        0);
                            }
                        });
        alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();

    }


    @Override
    public boolean isPolicyRespected() {
        return accountsRadioGroup.getCheckedRadioButtonId() != -1;
    }

    @Override
    public void onUserIllegallyRequestedNextPage() {
    }

    @Override
    public void onPause() {
        super.onPause();
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }
}

