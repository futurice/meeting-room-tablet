package com.futurice.android.reservator.view;

import android.app.Dialog;
import android.content.Context;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.model.Room;
import com.futurice.android.reservator.model.TimeSpan;

public class RoomQuickReservationPopup extends Dialog {
    QuickBookReservationView quickBookReservationView;

    public RoomQuickReservationPopup() {
        super(null, 0);
    }

    public RoomQuickReservationPopup(Context context, TimeSpan timeLimits, TimeSpan presetTime, Room room) {
        super(context, R.style.Theme_Transparent);
        setCancelable(true);

        setContentView(R.layout.quickbook_reservation_screen);
        // tap on outside to cancel
//        findViewById(R.id.quickRelativeLayout).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                cancel();
//            }
//        });

        quickBookReservationView = (QuickBookReservationView) findViewById(R.id.quickBookReservationView);

        quickBookReservationView.setAnimationDuration(0);
        quickBookReservationView.setClickable(true);
        quickBookReservationView.setRoom(room);
        quickBookReservationView.resetTimeSpan();
        quickBookReservationView.setMinTime(timeLimits.getStart());
        quickBookReservationView.setMaxTime(timeLimits.getEnd());
        quickBookReservationView.setStartTime(presetTime.getStart());
        quickBookReservationView.setEndTime(presetTime.getEnd());
        quickBookReservationView.setEndTimeRelatively(60);
        quickBookReservationView.setOnQuickCancelListener(new QuickBookReservationView.OnQuickCancelListener() {
            @Override
            public void onCancel(QuickBookReservationView view) {
                cancel();
            }
        });
        quickBookReservationView.setOnQuickReserveCallback(new QuickBookReservationView.OnQuickReserveListener() {
            @Override
            public void call(QuickBookReservationView v) {
                cancel();
            }
        });
        quickBookReservationView.setReserveMode();
    }

    public void setOnQuickReserveCallback(QuickBookReservationView.OnQuickReserveListener onQuickReserveCallback) {
        quickBookReservationView.setOnQuickReserveCallback(onQuickReserveCallback);
    }
}
