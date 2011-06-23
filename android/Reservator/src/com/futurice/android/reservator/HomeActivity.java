package com.futurice.android.reservator;

import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

import com.futurice.android.reservator.model.DataProxy;
import com.futurice.android.reservator.model.DataUpdatedListener;
import com.futurice.android.reservator.model.Reservation;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.Room;
import com.futurice.android.reservator.model.rooms.RoomsInfo;
import com.futurice.android.reservator.view.Callback;
import com.futurice.android.reservator.view.RoomReservationView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.Window;
import android.widget.LinearLayout;

public class HomeActivity extends Activity implements OnMenuItemClickListener,
		DataUpdatedListener {
	MenuItem settingsMenu, refreshMenu;
	LinearLayout container = null;
	private long lastTimeRefreshed = 0;
	DataProxy proxy;

	private ProgressDialog progressDialog = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.lobby_view);
		proxy = ((ReservatorApplication) getApplication()).getDataProxy();
	}

	@Override
	public void onResume() {
		super.onResume();
		proxy.addDataUpdatedListener(this);
		// Do not refresh too often
		long now = System.currentTimeMillis();
		if (lastTimeRefreshed + 60000 > now) {
			return;
		}
		lastTimeRefreshed = now;

		refreshRoomInfo();
	}
	@Override 
	public void onPause() {
		super.onPause();
		proxy.removeDataUpdatedListener(this);
	}
	private void refreshRoomInfo() {
		showLoading();
		container = (LinearLayout) findViewById(R.id.linearLayout1);
		container.removeAllViews();
		proxy.refreshRooms();
	}

	int showLoadingCount = 0;

	private void showLoading() {
		showLoadingCount++;
		if (this.progressDialog == null) {
			this.progressDialog = ProgressDialog.show(this, "Loading",
					"Refreshing meeting rooms list", true, false);
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
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		refreshMenu = menu.add("Refresh").setOnMenuItemClickListener(this);
		refreshMenu.setIcon(android.R.drawable.ic_popup_sync);
		settingsMenu = menu.add("Settings").setOnMenuItemClickListener(this);
		settingsMenu.setIcon(android.R.drawable.ic_menu_preferences);
		return true;
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		if (item == settingsMenu) {
			Intent i = new Intent(this, SettingsActivity.class);
			startActivity(i);
		} else if (item == refreshMenu) {
			refreshRoomInfo();
		}
		return true;
	}

	@Override
	public void roomListUpdated(List<Room> rooms) {
		//proceed to requesting room reservation data
		for (Room r : rooms) {
			RoomsInfo info = RoomsInfo.getRoomsInfo(r);
			if (info.isProjectRoom()) {
				return; // skip project room
			}
			showLoading();
			proxy.refreshRoomReservations(r);
		}
		hideLoading();
	}

	@Override
	public void roomReservationsUpdated(final Room room,
			List<Reservation> reservations) {
		//RUN THE WHOLE THING IN UI THREAD now that we have all the reservation data for this room
		container.post(new Runnable() {
			
			@Override
			public void run() {
				processRoom(room);
				hideLoading();
			}
		});
	}

	@Override
	public void refreshFailed(ReservatorException ex) {
		// TODO Auto-generated method stub
		throw new RuntimeException(ex.getMessage());
	}

	private void processRoom(Room r) {
		RoomReservationView v = new RoomReservationView(HomeActivity.this);
		v.setRoom(r);
		v.setOnReserveCallback(new Callback() {
			@Override
			public void call(RoomReservationView v) {
				refreshRoomInfo();
			}
		});

		// This is ugly, adding views in order.
		Comparator<Room> roomCmp = new Comparator<Room>() {
			private Calendar now = Calendar.getInstance();

			@Override
			public int compare(Room room1, Room room2) {
				boolean room1Free = room1.isFree();
				boolean room2Free = room2.isFree();

				if (room1Free && !room2Free) {
					return -1;
				} else if (!room1Free && room2Free) {
					return 1;
				} else if (room1Free && room2Free) {
					return room2.minutesFreeFrom(now)
							- room1.minutesFreeFrom(now);
				} else {
					return room1.reservedForFrom(now)
							- room2.reservedForFrom(now);
				}
			}
		};

		int roomCount = container.getChildCount();
		boolean added = false;
		for (int index = 0; index < roomCount; index++) {
			Room r2 = ((RoomReservationView) container.getChildAt(index))
					.getRoom();
			// Log.v("activity", r.toString() + " -- " +
			// Integer.toString(r2.minutesFreeFromNow()));
			if (roomCmp.compare(r, r2) < 0) {
				container.addView(v, index);
				added = true;
				break;
			}
		}
		if (!added) {
			container.addView(v);
		}
	}
}
