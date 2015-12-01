package com.viglia.android.pdfaudiorecorderapp.activities;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.viglia.android.pdfaudiorecorderapp.fragments.SavePresentationFragment;

public class SavePresentationActivity extends AppCompatActivity implements SavePresentationFragment.OnSaveFragmentListener {

    private String task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_presentation);

        // Add the RecorderFragment fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment saveFragment = (Fragment) fragmentManager.findFragmentById(R.id.save_presentation_layout);
        if(saveFragment==null){
            //The task variable allow us to distinguish between a
            //save task (when the presentation is saved from scratch the first time)
            //and an edit task (when the presentation already exists and we want to edit it)
            task = getIntent().getStringExtra("task");
            switch (task){
                case "save":
                    //save task: we pass to the saveFragment an ArrayList of SlideRecord and the path to the PDF file
                    saveFragment = SavePresentationFragment.newInstance(getIntent().getSerializableExtra("slideRecords"),getIntent().getSerializableExtra("pdfPath"));
                    break;

                case "edit":
                    //edit task: we pass the path to the existing json file of the presentation
                    saveFragment = SavePresentationFragment.newInstance(getIntent().getSerializableExtra("jsonFilePath"));
            }

            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.save_presentation_layout, saveFragment);
            fragmentTransaction.commit();
        }
    }

    @Override
    public void endSave() {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void endCancel() {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }
}
