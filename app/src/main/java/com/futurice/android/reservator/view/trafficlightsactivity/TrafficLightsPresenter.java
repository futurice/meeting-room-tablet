package com.futurice.android.reservator.view.trafficlightsactivity;

import android.util.Log;

public class TrafficLightsPresenter implements RoomReservationFragment.ReservationRequestPresenter, com.futurice.android.reservator.common.Presenter {

    private RoomReservationFragment roomReservationFragment;

    // Implementation of RoomReservationFragment.ReservationRequestPresenter

    @Override
    public void setRoomReservationFragment(RoomReservationFragment fragment) {
    this.roomReservationFragment = fragment;
    }

    @Override
    public void onReservationRequestMade(int reservationDuration, String reservationName)
        {
            Log.d("","TrafficLightsPresenter::onReservationRequestMade() reservationDuration: "+reservationDuration+" reservationName: "+reservationName);

        }

}
