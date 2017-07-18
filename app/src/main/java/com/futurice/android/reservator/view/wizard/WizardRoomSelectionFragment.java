package com.futurice.android.reservator.view.wizard;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.ReservatorApplication;
import com.futurice.android.reservator.WizardActivity;
import com.futurice.android.reservator.common.PreferenceManager;
import com.futurice.android.reservator.model.DataProxy;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.platformcalendar.PlatformCalendarDataProxy;
import com.github.paolorotolo.appintro.ISlidePolicy;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by shoj on 10/11/2016.
 */

public final class WizardRoomSelectionFragment extends android.support.v4.app.Fragment implements ISlidePolicy {


    CheckBox onlyUseResource = null;
    LinearLayout containerRoomCheckboxes = null;
    ProgressBar progressBar = null;

    DataProxy proxy = null;
    ArrayList<String> roomNames = null;

    PreferenceManager preferences = null;
    ReservatorApplication application = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.wizard_room_selection, container, false);

        onlyUseResource = (CheckBox) view.findViewById(R.id.wizard_rooms_use_resources);
        containerRoomCheckboxes = (LinearLayout) view.findViewById(R.id.wizard_rooms_container);
        progressBar = (ProgressBar) view.findViewById(R.id.wizard_rooms_progressbar);

        TextView title = (TextView) view.findViewById(R.id.wizard_rooms_title);
        title.setText(R.string.shown_rooms);

        preferences = PreferenceManager.getInstance(getActivity());
        application = ((ReservatorApplication) getActivity().getApplication());

        onlyUseResource.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    preferences.setCalendarMode(PlatformCalendarDataProxy.Mode.RESOURCES);
                }
                else{
                    preferences.setCalendarMode(PlatformCalendarDataProxy.Mode.CALENDARS);
                }
                saveSelection();
                loadRooms();
            }
        });
        onlyUseResource.setChecked(preferences.getCalendarMode() == PlatformCalendarDataProxy.Mode.RESOURCES);

        return view;
    }

    public void saveSelection()
    {
        HashSet<String> unselectedRooms = new HashSet<String>();

        for (int i=0; i<containerRoomCheckboxes.getChildCount(); i++)
        {
            CheckBox cb = (CheckBox) containerRoomCheckboxes.getChildAt(i);
            if(cb==null) continue;

            if(cb.isChecked()==false){
                unselectedRooms.add(cb.getText().toString());
            }
        }

        preferences.setUnselectedRooms(unselectedRooms);

    }

    public void loadRooms()
    {
        application.resetDataProxy();
        proxy = application.getDataProxy();

        roomNames = proxy.getRoomNames();

        progressBar.setVisibility(View.GONE);
        containerRoomCheckboxes.removeAllViews();
        onlyUseResource.setVisibility(View.VISIBLE);

        HashSet<String> unselectedRooms = preferences.getUnselectedRooms();

        for(String roomName: roomNames)
        {
            CheckBox cb = new CheckBox(getActivity());
            cb.setText(roomName);
            cb.setChecked(unselectedRooms.contains(roomName)==false);
            containerRoomCheckboxes.addView(cb);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public boolean isPolicyRespected() {
        // check whether any room is checked
        for (int i=0; i<containerRoomCheckboxes.getChildCount(); i++) {
            CheckBox cb = (CheckBox) containerRoomCheckboxes.getChildAt(i);
            if (cb == null) continue;

            if (cb.isChecked() == true) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onUserIllegallyRequestedNextPage() {

    }
}
