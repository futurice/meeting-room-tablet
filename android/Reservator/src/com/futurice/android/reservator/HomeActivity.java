package com.futurice.android.reservator;
import java.util.List;

import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.Room;
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
import android.widget.Toast;

public class HomeActivity extends Activity implements OnMenuItemClickListener{
	MenuItem settingsMenu, refreshMenu;
	LinearLayout container = null;
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
						r.getReservations(true);
						container.post(new Runnable() {
							public void run() {
								RoomReservationView v = new RoomReservationView(HomeActivity.this);
								v.setRoom(r);
								container.addView(v);// TODO Auto-generated method stub
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
			this.progressDialog = ProgressDialog.show(this, "Loading", "Loading", true, false); 
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
