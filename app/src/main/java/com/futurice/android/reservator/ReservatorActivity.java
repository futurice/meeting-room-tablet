package com.futurice.android.reservator;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.futurice.android.reservator.common.PreferenceManager;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.Room;


public class ReservatorActivity extends Activity {

    private final ReservatorAppHandler handler = new ReservatorAppHandler();
    private GoToFavouriteRoom goToFavouriteRoomRunable;
    protected boolean havePermissions = false;
    BatteryStateReceiver batteryStateReceiver = new BatteryStateReceiver();
    IntentFilter filter = new IntentFilter();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING || status ==
            BatteryManager.BATTERY_STATUS_FULL);
        if (isCharging) {
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        goToFavouriteRoomRunable = new GoToFavouriteRoom(this);
    }

    public void onResume() {
        super.onResume();
        startAutoGoToFavouriteRoom();
        registerReceiver(batteryStateReceiver, filter);
    }

    public void onPause() {
        super.onPause();
        stopAutoGoToFavouriteRoom();
        unregisterReceiver(batteryStateReceiver);
    }

    public void onUserInteraction() {
        super.onUserInteraction();
        stopAutoGoToFavouriteRoom();
        startAutoGoToFavouriteRoom();
    }


    /**
     * @return Identical to getApplication, but returns a ReservatorApplication.
     */
    public ReservatorApplication getResApplication() {
        return (ReservatorApplication) getApplication();
    }

    /**
     * Hook to execute actions when the activity has been prehended
     */
    public void onPrehended() {

    }

    /**
     * @return false to forbid the application to prehend the activity and go to favourite room, true to allow that.
     */
    protected Boolean isPrehensible() {
        return false;
    }

    private void startAutoGoToFavouriteRoom() {
        if (isPrehensible()) {
            handler.postDelayed(goToFavouriteRoomRunable, 60000);
        }
    }

    private void stopAutoGoToFavouriteRoom() {
        handler.removeCallbacks(goToFavouriteRoomRunable);
    }

    static class ReservatorAppHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            return;
        }
    }

    class GoToFavouriteRoom implements Runnable {

        ReservatorActivity activity;

        public GoToFavouriteRoom(ReservatorActivity anAct) {
            activity = anAct;
        }

        @Override
        public void run() {
            String roomName = PreferenceManager.getInstance(getApplicationContext()).getSelectedRoom();
            if (roomName != null) {
                Room room;
                try {
                    room = activity.getResApplication().getDataProxy().getRoomWithName(roomName);
                } catch (ReservatorException ex) {
                    Toast err = Toast.makeText(activity, ex.getMessage(),
                            Toast.LENGTH_LONG);
                    err.show();
                    return;
                }
                RoomActivity.startWith(activity, room);
            }
            activity.onPrehended();
        }
    }

    class BatteryStateReceiver extends BroadcastReceiver {

        private int status = -1;

        @Override
        public void onReceive(Context context, Intent intent) {
            status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            if (isCharging()) {
                ReservatorActivity.this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } else {
                ReservatorActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }

        public boolean isCharging() {
            return (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL);
        }
    }
}
