package com.futurice.android.reservator;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.WindowManager;
import android.widget.Toast;

import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.Room;

public class ReservatorActivity extends Activity {

    private final ReservatorAppHandler handler = new ReservatorAppHandler();
    private GoToFavouriteRoom goToFavouriteRoomRunable;
    public final int PERMISSIONS_REQUEST = 234;
    protected boolean havePermissions = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (!havePermissions) {
            havePermissions = checkPermissions();
        }
        if (havePermissions) {
            goToFavouriteRoomRunable = new GoToFavouriteRoom(this);
        }
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

    public boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                                              Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this,
                                              Manifest.permission.READ_CALENDAR)
            != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this,
                                              Manifest.permission.WRITE_CALENDAR)
            != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                                                                    Manifest.permission.READ_CONTACTS) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                                                                    Manifest.permission.READ_CALENDAR) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                                                                    Manifest.permission.WRITE_CALENDAR)) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.permission_request_title)
                        .setMessage(R.string.permission_request_reason)
                        .setPositiveButton("Ok",
                                           new DialogInterface.OnClickListener() {
                                               @Override public void onClick(
                                                       DialogInterface dialog,
                                                       int which) {
                                                   ActivityCompat.requestPermissions(ReservatorActivity.this, new String[]{
                                                           Manifest.permission.READ_CONTACTS,
                                                           Manifest.permission.READ_CALENDAR,
                                                           Manifest.permission.WRITE_CALENDAR
                                                   }, PERMISSIONS_REQUEST);
                                               }
                                           })
                        .setNegativeButton("Dismiss",  new DialogInterface.OnClickListener() {
                            @Override public void onClick(
                                    DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish();
                            }
                        })
                        .show();
            } else {
                ActivityCompat.requestPermissions(this,
                                                  new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.READ_CALENDAR,
                                                          Manifest.permission.WRITE_CALENDAR},
                                                  PERMISSIONS_REQUEST);

            }
            return false;
        } else {
            return true;
        }
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
            if (roomName != getString(R.string.lobbyRoomName)) {
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST: {
                if (grantResults.length >= 3
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    havePermissions = true;
                    goToFavouriteRoomRunable = new GoToFavouriteRoom(this);
                } else {
                    finish();
                }
                return;
            }


        }
    }

}
