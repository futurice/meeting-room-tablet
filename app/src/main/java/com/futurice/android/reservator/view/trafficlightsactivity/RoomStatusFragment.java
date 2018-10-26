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
        View view = inflater.inflate(R.layout.room_status_fragment, container, false);
        this.statusText = (TextView) view.findViewById(R.id.statusText);
        this.statusUntilText = (TextView) view.findViewById(R.id.statusUntilText);
        this.meetingNameText = (TextView) view.findViewById(R.id.meetingNameText);

        return view;
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
