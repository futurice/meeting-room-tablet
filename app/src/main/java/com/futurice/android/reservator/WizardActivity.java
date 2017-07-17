
package com.futurice.android.reservator;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.futurice.android.reservator.common.PreferenceManager;
import com.futurice.android.reservator.view.wizard
        .WizardAccountSelectionFragment;
import com.futurice.android.reservator.view.wizard
        .WizardDefaultRoomSelectionFragment;
import com.futurice.android.reservator.view.wizard.WizardRoomSelectionFragment;
import com.github.paolorotolo.appintro.AppIntro;

/**
 * Created by shoj on 10/11/2016.
 */

public final class WizardActivity extends AppIntro {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Fragment calendarAccountSelection =
                new WizardAccountSelectionFragment();
        final Fragment roomSelection = new WizardRoomSelectionFragment();
        final Fragment roomDefaultSelection =
                new WizardDefaultRoomSelectionFragment();


        super.addSlide(calendarAccountSelection);
        super.addSlide(roomSelection);
        super.addSlide(roomDefaultSelection);

        showSkipButton(false);
        setProgressButtonEnabled(true);
        setFadeAnimation();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onSlideChanged(
            @Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.

        // save old
        if (oldFragment instanceof WizardRoomSelectionFragment) {
            ((WizardRoomSelectionFragment) oldFragment).saveSelection();
        }

        // load new
        if (newFragment instanceof WizardRoomSelectionFragment) {
            ((WizardRoomSelectionFragment) newFragment).loadRooms();
        } else if (newFragment instanceof WizardDefaultRoomSelectionFragment) {
            ((WizardDefaultRoomSelectionFragment) newFragment).reloadRooms();
        }

    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);

        PreferenceManager.getInstance(this).setApplicationConfigured(true);

        final Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }

}
