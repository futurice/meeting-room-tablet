package com.futurice.android.reservator;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.futurice.android.reservator.common.PreferenceManager;
import com.futurice.android.reservator.model.AddressBook;
import com.futurice.android.reservator.model.AddressBookUpdatedListener;
import com.futurice.android.reservator.model.CachedDataProxy;
import com.futurice.android.reservator.model.DataProxy;
import com.futurice.android.reservator.model.DataUpdatedListener;
import com.futurice.android.reservator.model.DateTime;
import com.futurice.android.reservator.model.Reservation;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.Room;
import com.futurice.android.reservator.model.TimeSpan;
import com.futurice.android.reservator.view.EditReservationPopup;
import com.futurice.android.reservator.view.LobbyReservationRowView;
import com.futurice.android.reservator.view.RoomReservationPopup;
import com.futurice.android.reservator.view.RoomTrafficLights;
import com.futurice.android.reservator.view.WeekView;

import java.util.Calendar;
import java.util.Vector;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RoomActivity extends ReservatorActivity implements OnMenuItemClickListener,
        DataUpdatedListener, AddressBookUpdatedListener {
    public static final String ROOM_EXTRA = "room";
    public static final long ROOMLIST_REFRESH_PERIOD = 60 * 1000;
    final Handler handler = new Handler();
    final Runnable refreshDataRunnable = new Runnable() {
        @Override
        public void run() {
            Log.v("Refresh", "refreshing room info");
            refreshData();
            startAutoRefreshData();
        }
    };
    final int DEFAULT_BOOK_NOW_DURATION = 30; // mins
    DataProxy proxy;
    Room currentRoom;

    MenuItem settingsMenu, refreshMenu, aboutMenu;
    AlertDialog alertDialog;
    int showLoadingCount = 0;
    private ProgressDialog progressDialog = null;

    @BindView(R.id.weekView1)
    WeekView weekView;
    @BindView(R.id.roomNameLabel)
    TextView roomNameLabel;
    @BindView(R.id.roomTrafficLights)
    RoomTrafficLights trafficLights;
    @BindView(R.id.backToMain)
    Button backToMainMenu;


    /**
     * Helper for starting a RoomActivity
     *
     * @param context
     * @param room
     */
    public static void startWith(Context context, Room room) {
        Intent i = new Intent(context, RoomActivity.class);
        i.putExtra(ROOM_EXTRA, room);
        context.startActivity(i);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.room_activity);
        ButterKnife.bind(this);
        initRoomActivity();
    }

    @Override
    public void onResume() {
        proxy = this.getResApplication().getDataProxy();
        proxy.addDataUpdatedListener(this);
        refreshData();
        startAutoRefreshData();
        super.onResume();
        trafficLights.enable();
    }

    @Override
    public void onPause() {
        stopAutoRefreshData();
        super.onPause();
        this.getResApplication().getDataProxy().removeDataUpdatedListener(this);
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
        showLoadingCount = 0;
        trafficLights.disable();
    }

    private void setRoom(Room r) {
        currentRoom = r;
        roomNameLabel
                .setText(currentRoom.getName());
        weekView.refreshData(currentRoom);
        trafficLights.update(currentRoom);
    }

    private void initRoomActivity() {
        try {
            currentRoom = (Room) getIntent().getSerializableExtra(ROOM_EXTRA);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("No room found as Serializable extra " + ROOM_EXTRA);
        }
        if (currentRoom == null) {
            throw new IllegalArgumentException(
                    "No room found as Serializable extra " + ROOM_EXTRA);
        }

        backToMainMenu.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) { //Once I figure out how to go to the page, make it go there automatically after making a reservation
                        //RoomActivity.this.finish();
                        RoomActivity.this.trafficLights.enable();
                        RoomActivity.this.trafficLights.setVisibility(View.VISIBLE);
                    }
                });

        weekView.setOnFreeTimeClickListener(new WeekView.OnFreeTimeClickListener() {
            @Override
            public void onFreeTimeClick(View v, TimeSpan timeSpan, DateTime touch) {

                TimeSpan reservationTimeSpan = timeSpan;

                // if time span is greater than hour do stuff
                if (timeSpan.getLength() > 60 * 60000) {
                    DateTime start = timeSpan.getStart();
                    DateTime end = timeSpan.getEnd();

                    DateTime now = new DateTime();

                    touch = touch.stripMinutes();

                    if (touch.before(start)) {
                        touch = start;
                    }
                    if (touch.before(now) && now.before(end)) {
                        touch = now;
                    }

                    reservationTimeSpan = new TimeSpan(touch, Calendar.HOUR, 1);
                    DateTime touchend = reservationTimeSpan.getEnd();

                    // quantize end to 15min steps
                    touchend = touchend.set(Calendar.MINUTE, (touchend.get(Calendar.MINUTE) / 15) * 15);

                    if (touchend.after(end)) {
                        reservationTimeSpan.setEnd(end);
                    }
                }

                final RoomReservationPopup
                        d = new RoomReservationPopup(RoomActivity.this, timeSpan, reservationTimeSpan, currentRoom);
                d.setOnReserveCallback(new LobbyReservationRowView.OnReserveListener() {
                    @Override
                    public void call(LobbyReservationRowView v) {
                        d.dismiss();
                        refreshData();
                    }
                });

                RoomActivity.this.trafficLights.disable();
                d.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        RoomActivity.this.trafficLights.enable();
                    }
                });

                d.show();
            }
        });

        trafficLights.setBookNowListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!currentRoom.isFree()) return;
                TimeSpan limits = currentRoom.getNextFreeTime();

                DateTime now = new DateTime();
                TimeSpan suggested = new TimeSpan(now, now.add(Calendar.MINUTE, DEFAULT_BOOK_NOW_DURATION));

                if (limits == null) {
                    // No next free time was found. Use the suggested time.
                    limits = suggested;
                } else if (limits.getEnd().before(suggested.getEnd())) {
                    // The next free time ends before the suggested time.
                    suggested = limits;
                }

                final RoomReservationPopup d = new RoomReservationPopup(RoomActivity.this, limits, suggested, currentRoom);
                d.setOnReserveCallback(new LobbyReservationRowView.OnReserveListener() {
                    @Override
                    public void call(LobbyReservationRowView v) {
                        d.dismiss();
                        refreshData();
                    }
                });

                RoomActivity.this.trafficLights.disable();
                d.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        RoomActivity.this.trafficLights.enable();
                    }
                });

                d.show();
            }
        });

        weekView.setOnReservationClickListener(new WeekView.OnReservationClickListener() {
            @Override
            public void onReservationClick(View v, Reservation reservation) {
                final EditReservationPopup
                        d = new EditReservationPopup(RoomActivity.this, reservation, currentRoom,
                        new EditReservationPopup.OnReservationCancelledListener() {
                            @Override
                            public void onReservationCancelled(Reservation r) {
                                refreshData();
                            }
                        });

                RoomActivity.this.trafficLights.disable();
                d.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        RoomActivity.this.trafficLights.enable();
                    }
                });

                d.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        refreshMenu = menu.add("Refresh").setOnMenuItemClickListener(this);
        refreshMenu.setIcon(android.R.drawable.ic_popup_sync);
        settingsMenu = menu.add("Settings").setOnMenuItemClickListener(this);
        settingsMenu.setIcon(android.R.drawable.ic_menu_preferences);
        aboutMenu = menu.add("About").setOnMenuItemClickListener(this);
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item == settingsMenu) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
        } else if (item == refreshMenu) {
            refreshData();
        } else if (item == aboutMenu) {
            SpannableString s = new SpannableString(getString(R.string.aboutInfo));
            Linkify.addLinks(s, Linkify.ALL);

            Builder aboutBuilder = new AlertDialog.Builder(this);
            aboutBuilder.setTitle(R.string.aboutTitle);
            aboutBuilder.setMessage(s);
            aboutBuilder.setNegativeButton(R.string.close, null);
            alertDialog = aboutBuilder.show();

            //	Makes links clickable.
            ((TextView) alertDialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
        }
        return true;
    }

    public void onUserInteraction() {
        super.onUserInteraction();
        stopAutoRefreshData();
        startAutoRefreshData();
    }

    @Override
    protected Boolean isPrehensible() {
        String favouriteRoomName = PreferenceManager.getInstance(this).getSelectedRoom();
        System.out.println("Is prehensible: " + favouriteRoomName);

        // jump to another room if we have a selected room AND we are not displaying it atm
        return (favouriteRoomName != null) && (!favouriteRoomName.equals(currentRoom.getName()));
    }

    @Override
    public void onPrehended() {
        this.finish();
    }

    private void refreshData() {
        //showLoading();
        if (proxy instanceof CachedDataProxy) {
            ((CachedDataProxy) proxy).forceRefreshRoomReservations(currentRoom);
        } else {
            proxy.refreshRoomReservations(currentRoom);
        }
    }

    private void startAutoRefreshData() {
        handler.postDelayed(refreshDataRunnable, ROOMLIST_REFRESH_PERIOD);
    }

    private void stopAutoRefreshData() {
        handler.removeCallbacks(refreshDataRunnable);
    }
    /*
     *
	 * These do no create any extra value for the user.
	 * 

	private void showLoading() {
		showLoadingCount++;
		if (this.progressDialog == null) {
			this.progressDialog = ProgressDialog.show(this, "Loading",
					"Refreshing reservations", true, false);
		}

	}

	private void hideLoading() {
		showLoadingCount--;
		if (showLoadingCount <= 0) {
			if (this.progressDialog != null) {
				this.progressDialog.dismiss();
				this.progressDialog = null;
			}
		}
	}*/

    @Override
    public void roomListUpdated(Vector<Room> rooms) {
        // XXX: todo
		/*
		for (Room r : rooms) {
			if (r.getEmail().equals(roomEmail)) {
				final Room theRoom = r;
				setRoom(theRoom);
				return;
			}
		}
		// TODO what if the room is not in the list?
		throw new RuntimeException("Requested room not in the list.");
		*/
    }

    @Override
    public void roomReservationsUpdated(Room room) {
        if (currentRoom != null && room.equals(currentRoom)) {
            setRoom(room);
        }
        // let's update the cache (otherwise the changes (eg. new employees) won't be shown in the lists before restart)
        AddressBook ab = getResApplication().getAddressBook();
        ab.prefetchEntries();
        //hideLoading();
    }

    @Override
    public void refreshFailed(ReservatorException e) {
        //hideLoading();
        stopAutoRefreshData();
        Toast err = Toast.makeText(this, e.getMessage(),
                Toast.LENGTH_LONG);
        err.show();
        startAutoRefreshData();
        return;
    }

    @Override
    public void addressBookUpdated() {
        // No operation, because the update operation is executed only to refresh the cache.
    }

    @Override
    public void addressBookUpdateFailed(ReservatorException e) {
        refreshFailed(e);
    }
}
