package com.futurice.android.reservator.view.trafficlights;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.common.Helpers;

public class DisconnectedFragment extends Fragment {

    public interface DisconnectedFragmentPresenter {
        void setDisconnectedFragment(DisconnectedFragment fragment);
    }

    private DisconnectedFragmentPresenter presenter;

    public void setPresenter(DisconnectedFragmentPresenter presenter) {
        this.presenter = presenter;
        this.presenter.setDisconnectedFragment(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.disconnected_fragment, container, false);

        return view;
    }
}
