package com.futurice.android.reservator;

import java.util.Calendar;

import com.futurice.android.reservator.model.DataProxy;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.Room;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

/**
 * Requires begin (long), end (long) and roomMail(String) parameters.
 * 
 * @author vman
 * 
 */
public class ReserveActivity extends Activity implements
		OnSeekBarChangeListener {
	Calendar begin, end;
	Room target;
	DataProxy proxy;
	String roomMail;
	SeekBar startBar, endBar;
	TextView startLabel, endLabel;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reserve);
		begin = Calendar.getInstance();
		begin.setTimeInMillis(getIntent().getLongExtra("begin", 0));
		end = Calendar.getInstance();
		end.setTimeInMillis(getIntent().getLongExtra("end", 0));
		proxy = ((ReservatorApplication) getApplication()).getDataProxy();
		roomMail = getIntent().getStringExtra("roomMail");
		try {
			for (Room r : proxy.getRooms()) {
				if (r.getEmail().equals(roomMail)) {
					target = r;
				}
			}
		} catch (ReservatorException e) {
			// TODO: XXX
			Log.e("DataProxy", "getRooms", e);
		}
		startBar = (SeekBar) findViewById(R.id.startBar);
		startBar.setMax((int) ((end.getTimeInMillis() - begin.getTimeInMillis()) / 300000));
		startBar.setOnSeekBarChangeListener(this);
		endBar = (SeekBar) findViewById(R.id.endBar);
		endBar.setMax((int) ((end.getTimeInMillis() - begin.getTimeInMillis()) / 300000));
		endBar.setOnSeekBarChangeListener(this);

		startLabel = (TextView) findViewById(R.id.startLabel);
		endLabel = (TextView) findViewById(R.id.endLabel);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		Calendar tmp = (Calendar) begin.clone();
		tmp.add(Calendar.MINUTE, progress * 5);
		if (seekBar == startBar) {
			startLabel.setText(tmp.getTime().toLocaleString());
		} else if (seekBar == endBar) {
			endLabel.setText(tmp.getTime().toLocaleString());
		}

	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}
}
