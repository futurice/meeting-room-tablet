package com.futurice.android.reservator;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Window;

import com.futurice.android.reservator.common.LedHelper;
import com.futurice.android.reservator.model.Model;
import com.futurice.android.reservator.view.trafficlights.TrafficLightsPageFragment;
import com.futurice.android.reservator.view.trafficlights.TrafficLightsPresenter;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

public class MainActivity extends FragmentActivity {
    private FragmentManager fragmentManager;

    private TrafficLightsPageFragment trafficLightsPageFragment;
    private TrafficLightsPresenter presenter;

    private Model model;

    private void openFragment(Fragment fragment) {
        if (fragmentManager != null) {
            //@formatter:off
            fragmentManager.beginTransaction()
                    .replace(R.id.main_container, fragment)
                    .addToBackStack(fragment.toString())
                    .commit();
            //@formatter:on
        }
    }

    public String[] getAvailableAccounts() {
        List<String> accountsList = new ArrayList<>();
        for (Account account : AccountManager
                .get(this)
                .getAccountsByType(null)) {
            accountsList.add(account.name);
        }
        return accountsList.toArray(new String[accountsList.size()]);
    }

    private void showSetupWizard() {
        final Intent i = new Intent(this, WizardActivity.class);
        startActivity(i);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.fragmentManager = getSupportFragmentManager();
        this.trafficLightsPageFragment = new TrafficLightsPageFragment();

        this.model = ((ReservatorApplication)getApplication()).getModel();
        this.presenter = new TrafficLightsPresenter(this, this.model);
        this.trafficLightsPageFragment.setPresenter(this.presenter);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_activity);
        ButterKnife.bind(this);

        this.openFragment(this.trafficLightsPageFragment);
        registerReceiver(
            broadcastReceiver,
            new IntentFilter(CalendarStateReceiver.CALENDAR_CHANGED));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getAvailableAccounts().length <= 0) {
            showSetupWizard();
        }
    }

    // Start of implemtation of the calendar change listener with intent filters
    // TODO: make this actually work

    public void onCalendarUpdated() {
        if (this.model != null)
            this.model.getDataProxy().refreshRoomReservations(this.model.getFavoriteRoom());
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onCalendarUpdated();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LedHelper.getInstance().setGreenBrightness(0);
        LedHelper.getInstance().setRedBrightness(0);
        unregisterReceiver(broadcastReceiver);
    }
}
