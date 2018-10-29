package com.futurice.android.reservator.view.trafficlights;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.futurice.android.reservator.R;

public class TrafficLightsPageFragment extends Fragment {
    public interface TrafficLightsPagePresenter {
    void setTrafficLightsPageFragment(TrafficLightsPageFragment fragment);
    }
    private RoomStatusFragment roomStatusFragment;
    private RoomReservationFragment roomReservationFragment;
    private DayCalendarFragment dayCalendarFragment;

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

        try {
            this.dayCalendarFragment = (DayCalendarFragment)getChildFragmentManager().findFragmentById(R.id.dayCalendarFragment);
            this.dayCalendarFragment.setPresenter((DayCalendarFragment.DayCalendarPresenter) this.presenter);
        }
        catch (ClassCastException e) {
            throw new ClassCastException(presenter.toString() + " must implement DayCalendarPresenter");
        }
        return view;
    }
}
