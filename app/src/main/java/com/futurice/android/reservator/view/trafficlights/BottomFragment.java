package com.futurice.android.reservator.view.trafficlights;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.futurice.android.reservator.R;

public class BottomFragment extends Fragment {

    public interface BottomFragmentPresenter {
    void setBottomFragment(BottomFragment fragment);
    }

    private BottomFragmentPresenter presenter;

    public void setPresenter(BottomFragmentPresenter presenter) {
        this.presenter = presenter;
        this.presenter.setBottomFragment(this);
        }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        /*
        try {
            presenter = (RoomStatusPresenter) (((PresenterView)context).getPresenter());
            presenter.setRoomStatusFragment(this);
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement RoomStatusPresenter");
        } */
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_fragment, container, false);

        return view;
    }
}
