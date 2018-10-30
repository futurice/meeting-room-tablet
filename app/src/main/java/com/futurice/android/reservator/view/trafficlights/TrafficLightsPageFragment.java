package com.futurice.android.reservator.view.trafficlights;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.futurice.android.reservator.R;

public class TrafficLightsPageFragment extends Fragment {
    public interface TrafficLightsPagePresenter {
    void setTrafficLightsPageFragment(TrafficLightsPageFragment fragment);
    }
    private RoomStatusFragment roomStatusFragment;
    private DayCalendarFragment dayCalendarFragment;
    private BottomFragment bottomFragment;
    private RoomReservationFragment roomReservationFragment;
    private OngoingReservationFragment ongoingReservationFragment;

    private FragmentManager fragmentManager;

    private TrafficLightsPagePresenter presenter;

    private void openFragment(Fragment fragment) {
        if (fragmentManager != null) {
            //@formatter:off
            fragmentManager.beginTransaction()
                    .replace(R.id.roomReservationContainer, fragment)
                    .commitAllowingStateLoss();
            //@formatter:on
        }
    }

    public void setPresenter(TrafficLightsPagePresenter presenter) {
        this.presenter = presenter;
        this.presenter.setTrafficLightsPageFragment(this);
    }

    @Override
    public void onAttach(android.content.Context context) {
        super.onAttach(context);
        this.fragmentManager = getChildFragmentManager();

        try {
            this.roomReservationFragment = new RoomReservationFragment();
            this.roomReservationFragment.setPresenter((RoomReservationFragment.RoomReservationPresenter) this.presenter);
        }
        catch (ClassCastException e) {
            throw new ClassCastException(presenter.toString() + " must implement RoomReserationPresenter");
        }

        try {
            this.ongoingReservationFragment = new OngoingReservationFragment();
            this.ongoingReservationFragment.setPresenter((OngoingReservationFragment.OngoingReservationPresenter) this.presenter);
        }
        catch (ClassCastException e) {
            throw new ClassCastException(presenter.toString() + " must implement OngoingReservationPresenter");
        }
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
            this.dayCalendarFragment = (DayCalendarFragment)getChildFragmentManager().findFragmentById(R.id.dayCalendarFragment);
            this.dayCalendarFragment.setPresenter((DayCalendarFragment.DayCalendarPresenter) this.presenter);
        }
        catch (ClassCastException e) {
            throw new ClassCastException(presenter.toString() + " must implement DayCalendarPresenter");
        }

        /*try {
            this.bottomFragment = (BottomFragment)getChildFragmentManager().findFragmentById(R.id.bottomFragment);
            this.bottomFragment.setPresenter((BottomFragment.BottomFragmentPresenter) this.presenter);
        }
        catch (ClassCastException e) {
            throw new ClassCastException(presenter.toString() + " must implement BottomFragmentPresenter");
        }*/

        return view;
    }

    public void showOngoingReservationFragment() {
        this.openFragment(this.ongoingReservationFragment);
    }

    public void showRoomReservationFragment() {
        this.openFragment(this.roomReservationFragment);
    }
}
