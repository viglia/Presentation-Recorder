package com.android.viglia.pdfaudiorecorderapp.activities;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.android.viglia.pdfaudiorecorderapp.fragments.PresentationPlayerFragment;
import com.android.viglia.pdfaudiorecorderapp.utility.Presentation;

public class PresentationPlayerActivity extends AppCompatActivity {

    private Fragment playerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presentation_player);

        FragmentManager fragmentManager = getSupportFragmentManager();
        playerFragment = (Fragment) fragmentManager.findFragmentById(R.id.activity_player_frame);
        if(playerFragment==null) {
            playerFragment = PresentationPlayerFragment.newInstance((Presentation) getIntent().getSerializableExtra("presentation"));
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.activity_player_frame, playerFragment);
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onBackPressed() {
        if(playerFragment != null)
            //notify the playerFragment that the user has pressed the BACK button
            ((PresentationPlayerFragment)playerFragment).backPressed();
        super.onBackPressed();
    }
}
