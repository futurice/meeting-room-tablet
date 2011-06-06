package com.futurice.android.reservator;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Reservator extends Activity implements OnClickListener{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.free_rooms);
        Button btn = (Button)findViewById(R.id.button1);
        btn.setOnClickListener(this);
    }

	@Override
	public void onClick(View v) {
		Intent i = new Intent(this, RoomInfo.class);
		startActivity(i);
		
	}
}