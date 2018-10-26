package com.futurice.android.reservator.view.trafficlightsactivity;
import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import com.futurice.android.reservator.R;
import com.futurice.android.reservator.common.Presenter;

import butterknife.ButterKnife;

public class TrafficLightsActivity extends Activity implements com.futurice.android.reservator.common.PresenterView {
    private TrafficLightsPresenter presenter = new TrafficLightsPresenter();;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.traffic_lights_activity);
        ButterKnife.bind(this);

    }

    @Override

    public Presenter getPresenter() {
        return this.presenter;
    }

}
