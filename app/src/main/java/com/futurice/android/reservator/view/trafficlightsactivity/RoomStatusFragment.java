package com.futurice.android.reservator.view.trafficlightsactivity;
import android.app.Fragment;
import android.os.Bundle;
import com.futurice.android.reservator.R;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;

public class RoomStatusFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.room_status_fragment, container, false);
    }
}
