package com.futurice.android.reservator.view.trafficlights;

import android.app.Activity;
import android.content.res.Resources;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.os.Handler;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.common.Helpers;
import com.futurice.android.reservator.model.Model;
import com.futurice.android.reservator.model.Reservation;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.Room;
import com.futurice.android.reservator.model.TimeSpan;

import java.util.Calendar;
import java.util.Locale;
import java.util.Vector;

public class TrafficLightsPresenter implements
        TrafficLightsPageFragment.TrafficLightsPagePresenter,
        RoomStatusFragment.RoomStatusPresenter,
        RoomReservationFragment.RoomReservationPresenter,
        DayCalendarFragment.DayCalendarPresenter,
        OngoingReservationFragment.OngoingReservationPresenter,
        com.futurice.android.reservator.common.Presenter,
        com.futurice.android.reservator.model.DataUpdatedListener,
        com.futurice.android.reservator.model.AddressBookUpdatedListener {

    final int QUICK_BOOK_THRESHOLD = 5; // minutes

    private TrafficLightsPageFragment trafficLightsPageFragment;
    private RoomStatusFragment roomStatusFragment;
    private RoomReservationFragment roomReservationFragment;
    private OngoingReservationFragment ongoingReservationFragment;
    private DayCalendarFragment dayCalendarFragment;

    private Activity activity;
    private Model model;
    private Resources resources;

    private Room room;

    private Handler handler = new Handler();
    private Runnable minuteRunnable = new Runnable() {
        @Override
        public void run() {
            onMinuteElapsed();
            handler.postDelayed(this, 60000);
        }
    };

    public void onMinuteElapsed() {
        this.updateOngoingReservationFragment();
    }

    public TrafficLightsPresenter(Activity activity, Model model) {
        this.activity = activity;
        this.resources = activity.getResources();

        this.model = model;
        this.model.getDataProxy().addDataUpdatedListener(this);
        this.model.getAddressBook().addDataUpdatedListener(this);

    }

    private void tryStarting() {
        if (trafficLightsPageFragment != null && roomStatusFragment != null &&
                ongoingReservationFragment != null && roomReservationFragment != null && dayCalendarFragment != null) {
            this.model.getDataProxy().refreshRoomReservations(this.model.getFavoriteRoom());
            handler.postDelayed(minuteRunnable, 60000);
        }
    }


    // ------ Implementation of RoomReservationFragment.RoomReservationPresenter

    @Override
    public void setRoomReservationFragment(RoomReservationFragment fragment) {
        this.roomReservationFragment = fragment;

        this.roomReservationFragment.setTimeLimits(System.currentTimeMillis(), System.currentTimeMillis() + 1000*60*120);
        this.tryStarting();
    }

    @Override
    public void onReservationRequestMade(long reservationDuration, String reservationName) {
        Log.d("","TrafficLightsPresenter::onReservationRequestMade() reservationDuration: "+reservationDuration+" reservationName: "+reservationName);
    }

    // ------ Implementation of OngoingReservationFragment.OngoingReservationPresenter

    @Override
    public void setOngoingReservationFragment(OngoingReservationFragment fragment) {
        this.ongoingReservationFragment = fragment;

        //this.roomReservationFragment.setTimeLimits(System.currentTimeMillis(), System.currentTimeMillis() + 1000*60*120);
        this.tryStarting();
    }

    // ------ Implementation of TrafficLightsPageFragment.TrafficLightsPagePresenter

    @Override
    public void setTrafficLightsPageFragment(TrafficLightsPageFragment fragment) {
        this.trafficLightsPageFragment = fragment;
        this.tryStarting();
    }

    // ------- Implementation of RoomStatusFragment.RoomStatusPresenter

    @Override
    public void setRoomStatusFragment(RoomStatusFragment fragment) {
        this.roomStatusFragment = fragment;
        this.tryStarting();
    }

    // ------- Implementation of DayCalendarFragment.DayCalendarPresenter

    @Override
    public void setDayCalendarFragment(DayCalendarFragment fragment) {
        this.dayCalendarFragment = fragment;
        this.tryStarting();
    }

    // ------- Implementation of model.DataUpdatedListener

    @Override
    public void roomListUpdated(Vector<Room> rooms) {

    }

    @Override
    public void roomReservationsUpdated(Room room) {
        this.updateRoomData(room);
    }

    @Override
    public void refreshFailed(ReservatorException ex) {

    }

    // ------- Implementation of model.AddressBookUpdatedListener

    @Override
    public void addressBookUpdated() {

    }

    @Override
    public void addressBookUpdateFailed(ReservatorException e) {

    }


        //Methods for updating the view according to model's Room object

    /*
    private void updateConnected() {
        ConnectivityManager cm = null;
        try {
            cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        } catch (ClassCastException cce) {
            return;
        }
        if (cm == null) return;

        NetworkInfo ni = cm.getActiveNetworkInfo();

        if (ni != null && ni.isConnectedOrConnecting()) {
            // Connected
            lastTimeConnected = new Date();
            if (disconnected.getVisibility() != GONE) {
                disconnected.setVisibility(GONE);
            }
        } else {
            // Disconnected
            if (lastTimeConnected.before(new Date(new Date().getTime() - DISCONNECTED_WARNING_ICON_THRESHOLD))) {
                if (disconnected.getVisibility() != VISIBLE) {
                    disconnected.setVisibility(VISIBLE);
                }
            }
        }
    }
    */
    private void showReservationDetails(Reservation r, TimeSpan nextFreeSlot) {
        if (r == null) {
            this.roomStatusFragment.setMeetingNameText("");
        } else {
            this.roomStatusFragment.setMeetingNameText(r.getSubject());
        }

        if (nextFreeSlot == null) {
            // More than a day away
            this.roomStatusFragment.setStatusUntilText("");
        } else {
            String temp = resources.getString(R.string.free_at);
            this.roomStatusFragment.setStatusUntilText(Html.fromHtml(String.format(
                    Locale.getDefault(),
                    temp+" <b>%02d:%02d</b>",
                    nextFreeSlot.getStart().get(Calendar.HOUR_OF_DAY),
                    nextFreeSlot.getStart().get(Calendar.MINUTE))).toString());
        }
    }

    private void updateOngoingReservationFragment()
        {
        if (this.room == null)
            return;

        Reservation currentReservation = this.room.getCurrentReservation();
        if (currentReservation == null)
            return;

        long endTime = currentReservation.getEndTime().getTimeInMillis();
        int remainingMinutes = (int)(endTime - System.currentTimeMillis())/60000;

        if (this.ongoingReservationFragment != null)
            this.ongoingReservationFragment.setRemainingMinutes(remainingMinutes);
        }

    public void updateRoomData(Room room) {
        //updateConnected();
        this.room = room;

        this.dayCalendarFragment.updateRoomData(room);
        this.roomStatusFragment.setRoomTitleText(room.getName());


        if (room.isBookable(QUICK_BOOK_THRESHOLD)) {
            this.roomStatusFragment.setStatusText(resources.getString(R.string.status_free));
            this.roomStatusFragment.setMeetingNameText("");
            if (room.isFreeRestOfDay()) {
                this.roomStatusFragment.setStatusUntilText(resources.getString(R.string.free_for_the_day));

                this.trafficLightsPageFragment.getView().setBackgroundColor(resources.getColor(R.color.TrafficLightFree));
                this.trafficLightsPageFragment.showRoomReservationFragment();

                // Must use deprecated API for some reason or it crashes on older tablets

                //bookNowButton.setBackgroundDrawable(resources.getDrawable(R.drawable.traffic_lights_button_green));
                //bookNowButton.setTextColor(resources.getColorStateList(R.color.traffic_lights_button_green));
                //this.roomReservationFragment.getView().setVisibility(View.VISIBLE);
                this.roomStatusFragment.showBookNowText();
            } else {
                int freeMinutes = room.minutesFreeFromNow();
                this.roomStatusFragment.setStatusText(resources.getString(R.string.status_free));
                this.roomStatusFragment.setStatusUntilText(resources.getString(R.string.free_for_specific_amount, Helpers.humanizeTimeSpan2(freeMinutes)));
                if (freeMinutes >= Room.RESERVED_THRESHOLD_MINUTES) {
                    this.trafficLightsPageFragment.getView().setBackgroundColor(resources.getColor(R.color.TrafficLightFree));
                    this.trafficLightsPageFragment.showRoomReservationFragment();
                    //this.roomReservationFragment.getView().setVisibility(View.VISIBLE);

                    this.roomStatusFragment.showBookNowText();
                    //bookNowButton.setBackgroundDrawable(resources.getDrawable(R.drawable.traffic_lights_button_green));
                    //bookNowButton.setTextColor(resources.getColorStateList(R.color.traffic_lights_button_green));
                } else {
                    this.trafficLightsPageFragment.getView().setBackgroundColor(resources.getColor(R.color.TrafficLightYellow));
                    this.trafficLightsPageFragment.showOngoingReservationFragment();
                    this.updateOngoingReservationFragment();
                    //this.roomReservationFragment.getView().setVisibility(View.GONE);
                    this.roomStatusFragment.hideBookNowText();
                    //bookNowButton.setBackgroundDrawable(resources.getDrawable(R.drawable.traffic_lights_button_yellow));
                    //bookNowButton.setTextColor(resources.getColorStateList(R.color.traffic_lights_button_yellow));
                }
            }
            //reservationInfoView.setVisibility(GONE);
            //roomStatusInfoView.setVisibility(VISIBLE);
            //bookNowButton.setVisibility(VISIBLE);
            this.trafficLightsPageFragment.showRoomReservationFragment();
            //this.roomReservationFragment.getView().setVisibility(View.VISIBLE);
        } else {
            this.trafficLightsPageFragment.getView().setBackgroundColor(resources.getColor(R.color.TrafficLightReserved));
            this.roomStatusFragment.setStatusText(resources.getString(R.string.status_reserved));
            this.trafficLightsPageFragment.showOngoingReservationFragment();
            this.updateOngoingReservationFragment();
            //this.roomReservationFragment.getView().setVisibility(View.GONE);
            this.roomStatusFragment.hideBookNowText();
            this.showReservationDetails(room.getCurrentReservation(), room.getNextFreeSlot());
        }
    }
}
