package com.futurice.android.reservator.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.futurice.android.reservator.R;
import com.futurice.android.reservator.ReservatorApplication;
import com.futurice.android.reservator.model.DateTime;
import com.futurice.android.reservator.model.Reservation;
import com.futurice.android.reservator.model.ReservatorException;
import com.futurice.android.reservator.model.Room;

import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditReservationPopup extends Dialog {
    private ReservatorApplication application;
    private String reservationInfo;
    private OnReservationCancelledListener cancelledListener;
    private Reservation reservation;

    @BindView(R.id.cancelButton)
    ImageButton cancelButton;
    @BindView(R.id.roomName)
    TextView roomName;
    @BindView(R.id.reservationInfo)
    TextView reservationInfoTextView;
    @BindView(R.id.cancelReservationButton)
    Button cancelReservationButton;

    public EditReservationPopup(Context context, Reservation reservation, Room room,
                                OnReservationCancelledListener cancelledListener) {
        super(context, R.style.Theme_Transparent);
        setCancelable(true);
        setContentView(R.layout.edit_reservation_popup);
        ButterKnife.bind(this);

        application = (ReservatorApplication) this.getContext().getApplicationContext();
        this.cancelledListener = cancelledListener;
        this.reservation = reservation;

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditReservationPopup.this.cancel();
            }
        });

        roomName.setText(room.getName());

        DateTime start = reservation.getTimeSpan().getStart();
        DateTime end = reservation.getTimeSpan().getEnd();
        reservationInfo = String.format(
                Locale.getDefault(),
                "%02d:%02d–%02d:%02d\n%s",
                start.get(Calendar.HOUR_OF_DAY),
                start.get(Calendar.MINUTE),
                end.get(Calendar.HOUR_OF_DAY),
                end.get(Calendar.MINUTE),
                reservation.getSubject());
        reservationInfoTextView.setText(reservationInfo);

        if (reservation.isCancellable()) {
            cancelReservationButton.setEnabled(true);
            cancelReservationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(EditReservationPopup.this.getContext());
                    builder
                            .setTitle("Cancel reservation?")
                            .setMessage(reservationInfo)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    try {
                                        application.getDataProxy().cancelReservation(EditReservationPopup.this.reservation);
                                        if (EditReservationPopup.this.cancelledListener != null) {
                                            EditReservationPopup.this.cancelledListener.onReservationCancelled(
                                                    EditReservationPopup.this.reservation);
                                        }
                                    } catch (ReservatorException e) {
                                        android.util.Log.w("CANCEL", e);
                                    }
                                    EditReservationPopup.this.cancel();
                                }
                            })
                            .setNegativeButton("Back", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    EditReservationPopup.this.cancel();
                                }
                            });
                    builder.create().show();
                }
            });
        }
    }

    public interface OnReservationCancelledListener {
        void onReservationCancelled(Reservation r);
    }
}
