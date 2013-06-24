package com.futurice.android.reservator.view;

import com.futurice.android.reservator.R;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ImageView;

import com.futurice.android.reservator.model.Room;

public class RoomTrafficLights extends FrameLayout {
	TextView roomStatusView;
	ImageView roomTrafficLightView;

	public RoomTrafficLights(Context context) {
		this(context, null);
	}
	
	public RoomTrafficLights(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.room_traffic_lights, this);
		roomStatusView = (TextView) findViewById(R.id.roomStatus);
		roomTrafficLightView = (ImageView) findViewById(R.id.roomTrafficLightImage);
	}
	
	public void update(Room room) {
		roomStatusView.setText(room.getStatusText());
		if (room.isBookable()) {
			roomStatusView.setTextColor(getResources().getColor(
					R.color.StatusFreeColor));
			if (room.isLongBookable()) {
				roomTrafficLightView.setImageResource(R.drawable.traffic_light_green);
			} else {
				roomTrafficLightView.setImageResource(R.drawable.traffic_light_yellow);
			}
		} else {
			roomStatusView.setTextColor(getResources().getColor(
					R.color.StatusReservedColor));
			roomTrafficLightView.setImageResource(R.drawable.traffic_light_red);
		}
	}
}
