package com.futurice.android.reservator;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.futurice.android.reservator.controller.MakeReservations;
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
import com.futurice.android.reservator.view.LobbyReservationRowView.OnReserveListener;
import com.futurice.android.reservator.view.RoomReservationPopup;
import com.futurice.android.reservator.view.RoomTrafficLights;
import com.futurice.android.reservator.view.WeekView;
import com.futurice.android.reservator.view.WeekView.OnFreeTimeClickListener;
import com.futurice.android.reservator.view.WeekView.OnReservationClickListener;

import java.util.Calendar;
import java.util.Vector;

public class RoomActivity extends ReservatorActivity implements OnMenuItemClickListener,
    DataUpdatedListener, AddressBookUpdatedListener {
    public static final String ROOM_EXTRA = "room";
    public static final long ROOMLIST_REFRESH_PERIOD = 10 * 1000;
    final Handler handler = new Handler();
    final Runnable refreshDataRunnable = new Runnable() {
        @Override
        public void run() {
            Log.v("Refresh", getString(R.string.refreshRoom));
            refreshData();
            startAutoRefreshData();
            proxy.synchronize(currentRoom);
        }
    };

    final int DEFAULT_BOOK_NOW_DURATION = 30; // mins
    static DataProxy proxy;
    Room currentRoom;
    WeekView weekView;
    TextView roomNameLabel;
    RoomTrafficLights trafficLights;
    MenuItem settingsMenu, refreshMenu, aboutMenu;
    AlertDialog alertDialog;
    int showLoadingCount = 0;
    private ProgressDialog progressDialog = null;
    private SharedPreferences settings;
    private ReservatorApplication application;

    /**
     * Helper for starting MakeReservationTask RoomActivity
     *
     * @param context
     * @param room
     */
    public static void startWith(Context context, Room room, DataProxy dataProxy) {
        Intent i = new Intent(context, RoomActivity.class);
        i.putExtra(ROOM_EXTRA, room);
        context.startActivity(i);
        proxy = dataProxy;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.room_activity);
        this.settings = this.getSharedPreferences(this.getString(R.string.PREFERENCES_NAME), this.MODE_PRIVATE);
        this.weekView = (WeekView) findViewById(R.id.weekView1);
        this.roomNameLabel = (TextView) findViewById(R.id.roomNameLabel);
        this.trafficLights = (RoomTrafficLights) findViewById(R.id.roomTrafficLights);
        this.application = (ReservatorApplication) this.getApplicationContext();

        this.findViewById(R.id.seeRoomTrafficLightsButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                trafficLights.setVisibility(View.VISIBLE);
            }
        });

        try {
            currentRoom = (Room) getIntent().getSerializableExtra(ROOM_EXTRA);
            refreshData();
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("No room found as Serializable extra " + ROOM_EXTRA);
        }
        if (currentRoom == null) {
            throw new IllegalArgumentException(
                "No room found as Serializable extra " + ROOM_EXTRA);
        }

        findViewById(R.id.seeAllRoomsButton).setOnClickListener(
            new OnClickListener() {
                @Override
                public void onClick(View v) {
                    RoomActivity.this.finish();
                }
            });

        weekView.setOnFreeTimeClickListener(new OnFreeTimeClickListener() {
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

                final RoomReservationPopup d = new RoomReservationPopup(RoomActivity.this, timeSpan, reservationTimeSpan, currentRoom);
                d.setOnReserveCallback(new OnReserveListener() {
                    @Override
                    public void call(LobbyReservationRowView v) {
                        d.dismiss();
                        refreshData();
                    }
                });

                RoomActivity.this.trafficLights.disable();
                d.setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        RoomActivity.this.trafficLights.enable();
                    }
                });

                d.show();

                SharedPreferences settings = getApplicationContext().getSharedPreferences(getBaseContext().getString(R.string.PREFERENCES_NAME), getBaseContext().MODE_PRIVATE);
                if (settings.getString("reservationView","").equals(getBaseContext().getString(R.string.reserv_view_spinner_change))) {
                    d.cancel();
                }
            }
        });

        trafficLights.setBookListener(getBookListener());

        trafficLights.setBookNowListener(getBookNowListener());

        weekView.setOnReservationClickListener(new OnReservationClickListener() {
            @Override
            public void onReservationClick(View v, Reservation reservation) {
                final EditReservationPopup d = new EditReservationPopup(RoomActivity.this, reservation, currentRoom,
                    new EditReservationPopup.OnReservationCancelledListener() {
                        @Override
                        public void onReservationCancelled(Reservation r) {
                            refreshData();
                            roomReservationsUpdated(currentRoom);
                        }
                    });

                RoomActivity.this.trafficLights.disable();
                d.setOnDismissListener(new OnDismissListener() {
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
    public void onResume() {
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
        roomNameLabel.setText(currentRoom.getName());
        weekView.refreshData(currentRoom);
        trafficLights.update(currentRoom);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        refreshMenu = menu.add(getString(R.string.refresh)).setOnMenuItemClickListener(this);
        refreshMenu.setIcon(android.R.drawable.ic_popup_sync);
        settingsMenu = menu.add(getString(R.string.setting)).setOnMenuItemClickListener(this);
        settingsMenu.setIcon(android.R.drawable.ic_menu_preferences);
        aboutMenu = menu.add(getString(R.string.aboutTitle)).setOnMenuItemClickListener(this);
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
        String favouriteRoomName = getResApplication().getFavouriteRoomName();
        return !(currentRoom.getName().equals(favouriteRoomName));
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


    private OnClickListener getBookListener(){
       return new OnClickListener() {
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
               d.setOnReserveCallback(new OnReserveListener() {
                   @Override
                   public void call(LobbyReservationRowView v) {
                       d.dismiss();
                       refreshData();
                   }
               });

               RoomActivity.this.trafficLights.disable();
               d.setOnDismissListener(new OnDismissListener() {
                   @Override
                   public void onDismiss(DialogInterface dialog) {
                       RoomActivity.this.trafficLights.enable();
                   }
               });

               d.show();

               if (settings.getString("reservationView","").equals(getBaseContext().getString(R.string.reserv_view_spinner_change))) {
                   d.cancel();
               }
           }
       };
    }


    private OnClickListener getBookNowListener() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentRoom.isBookable(30)) {
                    new MakeReservationTask(currentRoom.getNextFreeSlot(30), currentRoom.getName())
                            .execute();
                }
                refreshData();
            }
        };
    }

    private class MakeReservationTask extends AsyncTask<Void, Void, Void> {
        private final TimeSpan timeSpan;
        private final String name;

        public MakeReservationTask(TimeSpan timeSpan, String name){
            this.timeSpan = timeSpan;
            this.name = name;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                new MakeReservations().doReservation(application,
                        currentRoom.getName(), currentRoom, timeSpan, name);
            } catch (ReservatorException e) {
                showDialog(application.getString(R.string.ERROR), e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            showDialog(application.getString(R.string.SUCCESS), application.getString(R.string.SUCCESS_Message));
        }
    }

    private void showDialog(String titel, String message) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this).setTitle(titel).setMessage(message);
        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        final AlertDialog alert = dialog.create();
        alert.show();

        // Hide after some seconds
        final Handler handler  = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (alert.isShowing()) {
                    alert.dismiss();
                }
            }
        };

        alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                handler.removeCallbacks(runnable);
            }
        });

        handler.postDelayed(runnable, 3000);
    }
}
