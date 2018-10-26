package com.futurice.android.reservator.view.trafficlightsactivity;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.common.PresenterView;

public class RoomReservationFragment extends Fragment {

    private ReservationRequestPresenter listener;

    public interface ReservationRequestPresenter {
        public void setRoomReservationFragment(RoomReservationFragment fragment);
        public void onReservationRequestMade(int reservationDuration, String reservationName);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.room_reservation_fragment, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (ReservationRequestPresenter) (((PresenterView)context).getPresenter());
            listener.setRoomReservationFragment(this);
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement ReservationRequestListener");
        }
    }
}
