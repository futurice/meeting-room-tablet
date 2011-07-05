package com.futurice.android.reservator;

import java.util.Comparator;
import java.util.Vector;

import com.futurice.android.reservator.model.DataProxy;
import com.futurice.android.reservator.model.DataUpdatedListener;
import com.futurice.android.reservator.model.DateTime;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.Room;
import com.futurice.android.reservator.model.rooms.RoomsInfo;
import com.futurice.android.reservator.view.LobbyReservationRowView;
import com.futurice.android.reservator.view.LobbyReservationRowView.OnReserveListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.Window;
import android.widget.DigitalClock;
import android.widget.LinearLayout;

public class LobbyActivity extends Activity implements OnMenuItemClickListener,
		DataUpdatedListener {
	MenuItem settingsMenu, refreshMenu;
	LinearLayout container = null;
	DataProxy proxy;

	private ProgressDialog progressDialog = null;
	int showLoadingCount = 0;

	final Handler handler = new Handler();

	final Runnable updateRoomsRunnable = new Runnable() {
		@Override
		public void run() {
			Log.v("Refresh", "refreshing room info");
			refreshRoomInfo();
			handler.postDelayed(updateRoomsRunnable, 10*60000); // update after 10minutes
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.lobby_activity);
		proxy = ((ReservatorApplication) getApplication()).getDataProxy();
		DigitalClock clock =  (DigitalClock)findViewById(R.id.digitalClock1);
        clock.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/EHSMB.TTF"));
		// handler.postDelayed(updateRoomsRunnable, 10*60000); // update after 10minutes
	}

	@Override
	public void onResume() {
		super.onResume();
		showLoadingCount = 0; //TODO better fix
		proxy.addDataUpdatedListener(this);
		refreshRoomInfo();
	}
	@Override
	public void onPause() {
		super.onPause();
		proxy.removeDataUpdatedListener(this);
		if(progressDialog != null){
			progressDialog.dismiss();
			progressDialog = null;
		}
	}

	private void refreshRoomInfo() {
		showLoading();
		container = (LinearLayout) findViewById(R.id.linearLayout1);
		container.removeAllViews();
		proxy.refreshRooms();
	}

	private void showLoading() {
		showLoadingCount++;
		if (this.progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setMessage("Refreshing room list...");
			progressDialog.setCancelable(false);
			progressDialog.setMax(1);
			progressDialog.show();
		}

		if (this.progressDialog != null) {
			if (showLoadingCount > progressDialog.getMax()) {
				progressDialog.setMax(showLoadingCount);
			}
		}
	}

	private void hideLoading() {
		showLoadingCount--;
		if (this.progressDialog != null){
			progressDialog.setTitle("Loading count " + showLoadingCount);
			progressDialog.setProgress(progressDialog.getMax() - Math.max(0, showLoadingCount));
		}
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
	public void roomListUpdated(Vector<Room> rooms) {
		//proceed to requesting room reservation data
		for (Room r : rooms) {
			RoomsInfo info = RoomsInfo.getRoomsInfo(r);
			if (info.isProjectRoom()) {
				continue; // skip project room
			}
			showLoading();
			proxy.refreshRoomReservations(r);
		}
		hideLoading();
	}

	@Override
	public void roomReservationsUpdated(final Room room) {
		processRoom(room);
		hideLoading();
	}

	@Override
	public void refreshFailed(ReservatorException e) {
		hideLoading();
		Builder alertBuilder = new AlertDialog.Builder(this);
		alertBuilder.setTitle("Error")
			.setMessage(e.getMessage())
			.show();
	}

	private void processRoom(Room r) {
		LobbyReservationRowView v = new LobbyReservationRowView(LobbyActivity.this);
		v.setRoom(r);
		v.setOnReserveCallback(new OnReserveListener() {
			@Override
			public void call(LobbyReservationRowView v) {
				refreshRoomInfo();
			}
		});

		// This is ugly, adding views in order.
		Comparator<Room> roomCmp = new Comparator<Room>() {
			private DateTime now = new DateTime();

			@Override
			public int compare(Room room1, Room room2) {
				boolean room1Free = room1.isFree() && room1.minutesFreeFromNow() >= 30;
				boolean room2Free = room2.isFree() && room2.minutesFreeFromNow() >= 30;

				if (room1Free && !room2Free) {
					return -1;
				} else if (!room1Free && room2Free) {
					return 1;
				} else if (room1Free && room2Free) {
					// Log.d("Lobby", room1.toString() + " -- " + room2.toString());
					return room2.minutesFreeFrom(now)
							- room1.minutesFreeFrom(now);
				} else {
					return 0;
					//return room1.reservedForFrom(now) - room2.reservedForFrom(now);
				}
			}
		};

		int roomCount = container.getChildCount();
		boolean added = false;
		for (int index = 0; index < roomCount; index++) {
			Room r2 = ((LobbyReservationRowView) container.getChildAt(index))
					.getRoom();

			// Log.d("Lobby", "sorting: " + r.getName() + ":" + r.isFree() + " -- " + r2.getName() + ":" + r2.isFree());

			if (r.equals(r2)) {
				Log.d("LobbyActivity", "duplicate room -- " + r.getEmail());
				// XXX: minor logic error; same room
				// someone else requested also an update, and we got rooms twice
				container.removeViewAt(index);
				container.addView(v, index);
				added = true;
				break;
			}
			else if (roomCmp.compare(r, r2) < 0) {
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
