package com.futurice.android.reservator;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.Room;
import com.futurice.android.reservator.model.rooms.RoomsInfo;
import com.futurice.android.reservator.view.Callback;
import com.futurice.android.reservator.view.RoomReservationView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

public class HomeActivity extends Activity implements OnMenuItemClickListener {
	MenuItem settingsMenu, refreshMenu;
	LinearLayout container = null;
	private long lastTimeRefreshed = 0;

	private ProgressDialog progressDialog = null;
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.lobby_view);
	}

	@Override
	public void onResume(){
		super.onResume();

		// Do not refresh too often
		long now = System.currentTimeMillis();
		if (lastTimeRefreshed + 60000 > now) {
			return;
		}
		lastTimeRefreshed = now;

		refreshRoomInfo();
	}

	private void refreshRoomInfo(){
		showLoading();
		container = (LinearLayout)findViewById(R.id.linearLayout1);
		container.removeAllViews();
		new Thread(){
			public void run(){
				try {
					List<Room> rooms = ((ReservatorApplication)getApplication()).getDataProxy().getRooms();
					for(final Room r : rooms){
						// Skip project rooms
						RoomsInfo info = RoomsInfo.getRoomsInfo(r);
						if (info.isProjectRoom()) {
							continue;
						}

						// Get room reservations and add the room row to the lobby view
						r.getReservations(true);
						container.post(new Runnable() {
							public void run() {
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
										} else if (!room1Free && room2Free){
											return 1;
										} else if (room1Free && room2Free) {
											return room2.minutesFreeFrom(now) - room1.minutesFreeFrom(now);
										} else {
											return room1.reservedForFrom(now) - room2.reservedForFrom(now);
										}
									}
								};

								int roomCount = container.getChildCount();
								boolean added = false;
								for (int index = 0; index < roomCount; index++) {
									Room r2 =  ((RoomReservationView) container.getChildAt(index)).getRoom();
									// Log.v("activity", r.toString() + " -- " + Integer.toString(r2.minutesFreeFromNow()));
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
						});
					}
				} catch (ReservatorException e) {
					Toast.makeText(HomeActivity.this, e.getMessage(), Toast.LENGTH_LONG);
					e.printStackTrace();
				}
				runOnUiThread(new Runnable() {
					public void run(){
						hideLoading();
					}
				});
			}
		}.start();
	}

	private void showLoading(){
		if(this.progressDialog == null){
			this.progressDialog = ProgressDialog.show(this, "Loading" ,"Refreshing meeting rooms list", true, false);
		}

	}
	private void hideLoading(){
		if(this.progressDialog != null){
			this.progressDialog.dismiss();
			this.progressDialog = null;
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
		if(item == settingsMenu){
		Intent i = new Intent(this, SettingsActivity.class);
		startActivity(i);
		}else if(item == refreshMenu){
			refreshRoomInfo();
		}
		return true;
	}
}
