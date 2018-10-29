package com.futurice.android.reservator.view.trafficlights;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.common.Presenter;

public class TrafficLightsPageFragment extends Fragment {
    public interface TrafficLightsPagePresenter {
    void setTrafficLightsPageFragment(TrafficLightsPageFragment fragment);
    }
    private RoomStatusFragment roomStatusFragment;
    private RoomReservationFragment roomReservationFragment;

    private TrafficLightsPagePresenter presenter;


    public void setPresenter(TrafficLightsPagePresenter presenter) {
        this.presenter = presenter;
        this.presenter.setTrafficLightsPageFragment(this);
    }

    @Override
    public void onAttach(android.content.Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.traffic_lights_page_fragment, container, false);

        try {
            this.roomStatusFragment = (RoomStatusFragment) getChildFragmentManager().findFragmentById(R.id.roomStatusFragment);
            this.roomStatusFragment.setPresenter((RoomStatusFragment.RoomStatusPresenter) this.presenter);
        }
        catch (ClassCastException e) {
                throw new ClassCastException(presenter.toString() + " must implement RoomStatusPresenter");
        }

        try {
            this.roomReservationFragment = (RoomReservationFragment)getChildFragmentManager().findFragmentById(R.id.roomReservationFragment);
            this.roomReservationFragment.setPresenter((RoomReservationFragment.RoomReservationPresenter) this.presenter);
            }
            catch (ClassCastException e) {
                throw new ClassCastException(presenter.toString() + " must implement RoomReserationPresenter");
            }
        return view;
    }
}
