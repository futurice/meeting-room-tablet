package com.futurice.android.reservator.view.trafficlightsactivity;

import android.util.Log;

public class TrafficLightsPresenter implements RoomReservationFragment.ReservationRequestListener, com.futurice.android.reservator.common.Presenter {
    public void onReservationRequestMade(int reservationDuration, String reservationName)
        {
            Log.d("","TrafficLightsPresenter::onReservationRequestMade() reservationDuration: "+reservationDuration+" reservationName: "+reservationName);
        }

}
