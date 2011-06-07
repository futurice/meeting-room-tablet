package com.futurice.android.reservator;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Reservator extends Activity implements OnClickListener{
    /** Called when the activity is first created. */
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.free_rooms);
        Button btn = (Button)findViewById(R.id.button1);
        btn.setOnClickListener(this);

        findViewById(R.id.button2).setOnClickListener(this);
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button2:
			((TextView)findViewById(R.id.textView1)).setText("Hello!");
			
			String user = ((EditText)findViewById(R.id.username)).getText().toString();
			String password = ((EditText)findViewById(R.id.username)).getText().toString();
			
			((ReservatorApplication) getApplication()).getDataProxy().setCredentials(user, password);
			
			((TextView)findViewById(R.id.textView1)).setText("Hello " + user);
			return;
		}
		
		Intent i = new Intent(this, RoomInfo.class);
		startActivity(i);
	}
}
