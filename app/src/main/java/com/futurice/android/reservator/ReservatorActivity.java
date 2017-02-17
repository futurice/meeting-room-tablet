package com.futurice.android.reservator;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;
import android.widget.Toast;

import com.futurice.android.reservator.model.DataProxy;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.Room;

public class ReservatorActivity extends Activity {

    private final ReservatorAppHandler handler = new ReservatorAppHandler();
    private GoToFavouriteRoom goToFavouriteRoomRunable;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        goToFavouriteRoomRunable = new GoToFavouriteRoom(this);
    }

    public void onResume() {
        super.onResume();
        startAutoGoToFavouriteRoom();
    }

    public void onPause() {
        super.onPause();
        stopAutoGoToFavouriteRoom();
    }

    public void onUserInteraction() {
        super.onUserInteraction();
        stopAutoGoToFavouriteRoom();
        startAutoGoToFavouriteRoom();
    }

    /**
     * @return Identical to getApplication, but returns MakeReservationTask ReservatorApplication.
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
        if (isPrehensible()) {
            handler.removeCallbacks(goToFavouriteRoomRunable);
        }
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
            String roomName = activity.getResApplication().getFavouriteRoomName();
            DataProxy dataProxy = activity.getResApplication().getDataProxy();
            if (roomName != getString(R.string.lobbyRoomName)) {
                Room room;
                try {
                    room = dataProxy.getRoomWithName(roomName);
                } catch (ReservatorException ex) {
                    Toast err = Toast.makeText(activity, ex.getMessage(),
                        Toast.LENGTH_LONG);
                    err.show();
                    return;
                }
                RoomActivity.startWith(activity, room, dataProxy);
            }
            activity.onPrehended();
        }
    }

}
