package com.futurice.android.reservator;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class Reservator extends Activity implements OnClickListener{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.free_rooms);
        findViewById(R.id.button1).setOnClickListener(this);
        //DataProxy proxy = ((ReservatorApplication)getApplication()).getDataProxy();
        
        /*ListView roomList = (ListView)findViewById(R.id.listView1);
        ArrayAdapter<Room> adapter = new ArrayAdapter<Room>(this, android.R.layout.simple_list_item_1, proxy.getRooms().toArray(new Room[0]));
        roomList.setAdapter(adapter);*/
    }

	@Override
	public void onClick(View v) {
		Intent i = new Intent(this, RoomInfo.class);
		startActivity(i);
		
	}
}