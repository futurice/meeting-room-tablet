package com.futurice.android.reservator.view.trafficlights;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.futurice.android.reservator.R;

public class OngoingReservationFragment extends Fragment {

    public interface OngoingReservationPresenter {
    void setOngoingReservationFragment(OngoingReservationFragment fragment);
    }

    private OngoingReservationPresenter presenter;

    private TextView barDurationText = null;
    private SeekBar seekBar = null;


    public void setPresenter(OngoingReservationPresenter presenter) {
        this.presenter = presenter;
        this.presenter.setOngoingReservationFragment(this);
        }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ongoing_reservation_fragment, container, false);

        this.barDurationText = (TextView) view.findViewById(R.id.barDurationText);
        this.seekBar = (SeekBar) view.findViewById(R.id.ongoingSeekBar);

        return view;
    }


}
