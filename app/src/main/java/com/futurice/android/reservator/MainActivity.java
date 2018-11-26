package com.futurice.android.reservator;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.futurice.android.reservator.common.LedHelper;
import com.futurice.android.reservator.model.Model;
import com.futurice.android.reservator.view.trafficlights.TrafficLightsPageFragment;
import com.futurice.android.reservator.view.trafficlights.TrafficLightsPresenter;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

public class MainActivity extends FragmentActivity {
    public String KIOSK_ON_INTENT_NAME = "com.futurice.android.reservator.KIOSK_ON";
    public String KIOSK_OFF_INTENT_NAME = "com.futurice.android.reservator.KIOSK_OFF";

    private FragmentManager fragmentManager;

    private TrafficLightsPageFragment trafficLightsPageFragment;
    private TrafficLightsPresenter presenter;

    private Model model;


    BroadcastReceiver calendarChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onCalendarUpdated();
        }
    };

    BroadcastReceiver kioskOnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("Reservator","KIOSK_ON intent received");
        }
    };

    BroadcastReceiver kioskOffReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("Reservator","KIOSK_OFF intent received");
        }
    };

    class ConnectionStateMonitor extends ConnectivityManager.NetworkCallback {

        final NetworkRequest networkRequest;

        public ConnectionStateMonitor() {
            networkRequest = new NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build();

            /*
            networkRequest = new NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
                    .build();
            */
        }

        public void enable(Context context) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            connectivityManager.registerNetworkCallback(networkRequest , this);
        }

        @Override
        public void onAvailable(Network network) {
            updateNetworkStatus();
            /*
            ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cm.getNetworkInfo(network);
            boolean isConnected = (info != null && info.isConnectedOrConnecting());

            if (isConnected) {
                NetworkCapabilities nc = cm.getNetworkCapabilities(network);
                if (nc != null) {
                    boolean isInternetValid = nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
                    if (isInternetValid) {
                        setConnected();
                    }
                }
            }
            */
        }

        @Override
        public void onLost(Network network) {
            setDisconnected();
            //updateNetworkStatus();
        }

    }

    private ConnectionStateMonitor connectionStateMonitor = new ConnectionStateMonitor();

    private void setConnected() {
        if (this.presenter == null)
            return;
        this.presenter.setConnected();
    }

    private void setDisconnected() {
        if (this.presenter == null)
            return;
        this.presenter.setDisconnected();
    }

    public void updateNetworkStatus() {
        if (this.presenter == null)
            return;

        if (this.isConnectedToGoogle())
            this.setConnected();
        else
            this.setDisconnected();
    }

    private boolean isConnectedToGoogle() {
        try {
            HttpURLConnection urlConnection = (HttpURLConnection)
                    (new URL("http://clients3.google.com/generate_204")
                            .openConnection());
            urlConnection.setUseCaches(false);
            urlConnection.setRequestProperty("User-Agent", "Android");
            urlConnection.setRequestProperty("Connection", "close");
            urlConnection.setConnectTimeout(1500);
            urlConnection.connect();
            if (urlConnection.getResponseCode() == 204 &&
                    urlConnection.getContentLength() == 0) {
                Log.d("Reservator", "isConnectedToGoogle() returning true");
                return true;
            }
            else
                return false;
        } catch (Exception e) {
            return false;
        }
    }
    /*
    private boolean hasInternetConnection() {
        final ConnectivityManager connectivityManager = (ConnectivityManager)this.
                getSystemService(Context.CONNECTIVITY_SERVICE);

        final Network network = connectivityManager.getActiveNetwork();
        final NetworkCapabilities capabilities = connectivityManager
                .getNetworkCapabilities(network);

        return capabilities != null
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }
*/
    private void openFragment(Fragment fragment) {
        if (fragmentManager != null) {
            fragmentManager.executePendingTransactions();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            if (fragment.isAdded())
                ft.show(fragment);
            else
                ft.add(R.id.main_container,fragment);

            ft.commit();
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

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        this.fragmentManager = getSupportFragmentManager();
        this.trafficLightsPageFragment = new TrafficLightsPageFragment();

        this.model = ((ReservatorApplication)getApplication()).getModel();
        this.presenter = new TrafficLightsPresenter(this, this.model);
        this.trafficLightsPageFragment.setPresenter(this.presenter);

        this.connectionStateMonitor.enable(this);

        setContentView(R.layout.main_activity);
        ButterKnife.bind(this);

        this.openFragment(this.trafficLightsPageFragment);
        this.updateNetworkStatus();

        this.registerReceiver(calendarChangeReceiver, new IntentFilter(CalendarStateReceiver.CALENDAR_CHANGED));
        this.registerReceiver(kioskOnReceiver, new IntentFilter(this.KIOSK_ON_INTENT_NAME));
        this.registerReceiver(kioskOffReceiver, new IntentFilter(this.KIOSK_OFF_INTENT_NAME));
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LedHelper.getInstance().setGreenBrightness(0);
        LedHelper.getInstance().setRedBrightness(0);
        unregisterReceiver(calendarChangeReceiver);
    }

    private void hideSoftKeyboard() {
        InputMethodManager inputMethodManager =
                (InputMethodManager) this.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                getWindow().getDecorView().getRootView().getWindowToken(), 0);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        //Log.d("Reservator","touch event");
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN)
            this.hideSoftKeyboard();
        return super.dispatchTouchEvent(event);
    }

}
