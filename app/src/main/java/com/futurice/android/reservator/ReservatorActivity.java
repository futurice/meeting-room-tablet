package com.futurice.android.reservator;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;
import android.widget.Toast;

import com.futurice.android.reservator.common.PreferenceManager;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.Room;


public class ReservatorActivity extends Activity {

    private final ReservatorAppHandler handler = new ReservatorAppHandler();
    private GoToFavouriteRoom goToFavouriteRoomRunable;
    protected boolean havePermissions = false;

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

}
