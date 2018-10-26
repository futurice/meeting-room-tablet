package com.futurice.android.reservator.view.trafficlightsactivity;
import android.app.Fragment;
import android.os.Bundle;
import com.futurice.android.reservator.R;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.TextView;

public class RoomStatusFragment extends Fragment {
    private TextView statusText = null;
    private TextView statusUntilText = null;
    private TextView meetingNameText = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.statusText = (TextView) getActivity().findViewById(R.id.statusText);
        this.statusUntilText = (TextView) getActivity().findViewById(R.id.statusUtilText);
        this.meetingNameText = (TextView) getActivity().findViewById(R.id.meetingNameText);

        return inflater.inflate(R.layout.room_status_fragment, container, false);
    }

    public void setStatusText(String text) {
        this.statusText.setText(text);
    }
    public void setStatusUntilText(String text) {
        this.statusUntilText.setText(text);
    }
    public void setMeetingNameText(String text) {
        this.meetingNameText.setText(text);
    }

}
