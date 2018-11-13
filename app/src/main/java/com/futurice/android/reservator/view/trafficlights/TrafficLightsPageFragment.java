package com.futurice.android.reservator.view.trafficlights;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
    private DisconnectedFragment disconnectedFragment;

    private FragmentManager fragmentManager;

    private TrafficLightsPagePresenter presenter;
    private Fragment currentChildFragment;

    /*
    private void openFragment(Fragment fragment) {
        if (fragmentManager != null) {
            FragmentTransaction ft = fragmentManager.beginTransaction();



            ft.commitAllowingStateLoss();
        }
    }

    private void removeCurrentChildFragment() {
       if (this.currentChildFragment != null) {
           if (fragmentManager != null) {
               //@formatter:off
               fragmentManager.beginTransaction()
                       .remove(this.currentChildFragment)
                       .commitAllowingStateLoss();
               //@formatter:on
               this.currentChildFragment = null;
           }
       }
    }

    */
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

        try {
            this.disconnectedFragment = new DisconnectedFragment();
            this.disconnectedFragment.setPresenter((DisconnectedFragment.DisconnectedFragmentPresenter) this.presenter);
        }
        catch (ClassCastException e) {
            throw new ClassCastException(presenter.toString() + " must implement DisconnectedFragmentPresenter");
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
        if (fragmentManager != null) {
            fragmentManager.executePendingTransactions();

            FragmentTransaction ft = fragmentManager.beginTransaction();

            if (this.roomReservationFragment.isAdded()) {
                ft.hide(this.roomReservationFragment);
            }

            if (disconnectedFragment.isAdded()) {
                ft.hide(disconnectedFragment);
            }

            if (this.ongoingReservationFragment.isAdded()) {
                ft.show(this.ongoingReservationFragment);
            } else {
                ft.add(R.id.roomReservationContainer, this.ongoingReservationFragment);
            }

            ft.commitAllowingStateLoss();
            fragmentManager.executePendingTransactions();
        }
    }

    public void hideBothReservationFragments() {
        if (fragmentManager != null) {
            fragmentManager.executePendingTransactions();

            FragmentTransaction ft = fragmentManager.beginTransaction();

            if (this.ongoingReservationFragment.isAdded()) {
                ft.hide(this.ongoingReservationFragment);
            }

            if (this.roomReservationFragment.isAdded()) {
                ft.hide(this.roomReservationFragment);
            }

            if (this.disconnectedFragment.isAdded()) {
                ft.hide(this.disconnectedFragment);
            }

            ft.commitAllowingStateLoss();
            fragmentManager.executePendingTransactions();
        }
    }

    public void showRoomReservationFragment() {
        if (fragmentManager != null) {
            fragmentManager.executePendingTransactions();

            FragmentTransaction ft = fragmentManager.beginTransaction();

            if (this.ongoingReservationFragment.isAdded()) {
                ft.hide(this.ongoingReservationFragment);
            }

            if (disconnectedFragment.isAdded()) {
                ft.hide(disconnectedFragment);
            }

            if (this.roomReservationFragment.isAdded()) {
                ft.show(this.roomReservationFragment);
            } else {
                ft.add(R.id.roomReservationContainer, this.roomReservationFragment);
            }

            ft.commitAllowingStateLoss();
            fragmentManager.executePendingTransactions();
        }
    }

    public void showDisconnectedFragment() {
        if (fragmentManager != null) {
            fragmentManager.executePendingTransactions();

            FragmentTransaction ft = fragmentManager.beginTransaction();

            if (this.ongoingReservationFragment.isAdded()) {
                ft.hide(this.ongoingReservationFragment);
            }

            if (roomReservationFragment.isAdded()) {
                ft.hide(this.roomReservationFragment);
            }

            if (this.disconnectedFragment.isAdded()) {
                ft.show(this.disconnectedFragment);
            } else {
                ft.add(R.id.roomReservationContainer, this.disconnectedFragment);
            }

            ft.commitAllowingStateLoss();
            fragmentManager.executePendingTransactions();
        }
    }

    /*
    public void hideDisconnectedFragment() {
        this.removeCurrentChildFragment();
    }
    */
}
