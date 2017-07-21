package com.futurice.android.reservator;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

public class CheckPermissionsActivity extends AppCompatActivity {

    public final int PERMISSIONS_REQUEST = 234;
    public boolean isPermitted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_permissions);
        if (!isPermitted) {
            isPermitted = checkPermissions();
        } else {
            ((ReservatorApplication) getApplication()).resetDataProxy();
            startLoginActivity();
        }
    }

    private void startLoginActivity() {
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }

    public boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_CALENDAR)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission
                                .WRITE_CALENDAR)
                        != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest
                            .permission.READ_CONTACTS) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest
                                    .permission.READ_CALENDAR) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest
                                    .permission.WRITE_CALENDAR)) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.permission_request_title)
                        .setMessage(R.string.permission_request_reason)
                        .setPositiveButton("Ok",
                                new DialogInterface
                                        .OnClickListener() {
                                    @Override
                                    public void onClick(
                                            DialogInterface dialog,
                                            int which) {
                                        ActivityCompat
                                                .requestPermissions(
                                                        CheckPermissionsActivity.this,
                                                        new String[]{
                                                                Manifest.permission.READ_CONTACTS,
                                                                Manifest.permission.READ_CALENDAR,
                                                                Manifest.permission.WRITE_CALENDAR
                                                        },
                                                        PERMISSIONS_REQUEST);
                                    }
                                })
                        .setNegativeButton("Dismiss",
                                new DialogInterface
                                        .OnClickListener() {
                                    @Override
                                    public void onClick(
                                            DialogInterface dialog,
                                            int which) {
                                        dialog.dismiss();
                                    }
                                })
                        .show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission
                                        .READ_CONTACTS,
                                Manifest.permission
                                        .READ_CALENDAR,
                                Manifest.permission
                                        .WRITE_CALENDAR
                        },
                        PERMISSIONS_REQUEST);
            }
            return false;
        } else {
            return true;
        }
    }


    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST: {
                if (grantResults.length >= 3
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    ((ReservatorApplication) getApplication()).resetDataProxy();
                    startLoginActivity();
                }
                return;
            }
        }
    }
}
