package com.android.viglia.pdfaudiorecorderapp.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.*;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.viglia.pdfaudiorecorderapp.activities.R;
import com.android.viglia.pdfaudiorecorderapp.activities.SavePresentationActivity;
import com.android.viglia.pdfaudiorecorderapp.utility.PausableChronometer;
import com.android.viglia.pdfaudiorecorderapp.utility.PresentationRecorder;
import com.joanzapata.pdfview.PDFView;
import com.joanzapata.pdfview.listener.OnPageChangeListener;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RecorderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecorderFragment extends Fragment implements OnPageChangeListener{

    private Button chooseFileButton;
    private ImageButton recordButton;
    private ImageButton stopButton;
    private PDFView pdfView;
    private PausableChronometer chronometer;

    //the possible states of the PresentationRecorder
    private static final int INITIAL_STATE = 0;
    private static final int STARTED_STATE = 1;
    private static final int PAUSED_STATE = 2;

    private int currentState;   //keeps track of the current state
    private String pdfFilePath;
    private int pageNum;
    private PresentationRecorder recorder;


    private File recordTempDir;
    public int FILE_PICKER_CODE = 1;
    public int SAVE_PRESENTATION_CODE = 2;

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment RecorderFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RecorderFragment newInstance() {
        RecorderFragment fragment = new RecorderFragment();

        return fragment;
    }

    public RecorderFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        currentState = INITIAL_STATE;
        pdfFilePath = null;
        pageNum = 0;
        recordTempDir = getActivity().getExternalFilesDir(null);
        recorder = new PresentationRecorder(getContext(),recordTempDir.getAbsolutePath()+"/audio.mp4",0);
        //Log.d("directory",recordTempDir.getAbsolutePath()+"/prova.mp4");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_recorder, container, false);
        //little trick to set the proper time in the Chronometer when the screen is rotated
        long chronometerTime=0;
        if(chronometer!=null) {
            if(currentState==STARTED_STATE)
                chronometer.stop();
            chronometerTime = chronometer.getCurrentTime();
        }

        pdfView = (PDFView) view.findViewById(R.id.pdfview);
        chooseFileButton = (Button) view.findViewById(R.id.choose_file_button);
        stopButton = (ImageButton) view.findViewById(R.id.stop_button);
        recordButton = (ImageButton) view.findViewById(R.id.record_button);
        chronometer = (PausableChronometer) view.findViewById(R.id.timer);
        chronometer.setFormat("%s:%s:%s");

        chooseFileButton.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), FilePickerActivity.class);
                        startActivityForResult(intent, FILE_PICKER_CODE);
                    }
                }
        );

        //since it is a retained fragment check if a pdf has been already selected and in that case set the pdfView
        if(pdfFilePath != null){
            pdfView.fromFile(new File(pdfFilePath))
                    .defaultPage(pageNum)
                    .showMinimap(false)
                    .onPageChange(this)
                    .load();
            chooseFileButton.setClickable(false);
            recordButton.setVisibility(View.VISIBLE);
        }


        if(currentState==STARTED_STATE || currentState == PAUSED_STATE){
            stopButton.setVisibility(View.VISIBLE);
            chronometer.setCurrentTime(chronometerTime);
            chronometer.setVisibility(View.VISIBLE);
        }

        //since it is a retained fragment check if it is recording a presentation and in that case
        //restart the Chronometer and set the "pause" icon
        if(currentState == STARTED_STATE){
            chronometer.start();
            recordButton.setImageResource(R.mipmap.pause_icon);

        }
        //switch the recordButton icon (record | pause) based on the value of currentState variable
        recordButton.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(currentState==STARTED_STATE){
                            recordButton.setImageResource(R.mipmap.record_icon);
                            currentState = PAUSED_STATE;
                            recorder.pause();
                            chronometer.stop();
                        }
                        else if(currentState!=STARTED_STATE){
                            recordButton.setImageResource(R.mipmap.pause_icon);
                            currentState = STARTED_STATE;
                            if(stopButton.getVisibility()==View.GONE)
                                stopButton.setVisibility(View.VISIBLE);
                            if(chronometer.getVisibility()==View.GONE)
                                chronometer.setVisibility(View.VISIBLE);
                            recorder.start();
                            chronometer.start();
                        }
                    }
                }
        );

        stopButton.setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //when the stop button is clicked we clear all
                        //and we pass the recorded presentation (SlideRecord)
                        //to the SavePresentationActivity Activity
                        stopButton.setVisibility(View.GONE);
                        recordButton.setImageResource(R.mipmap.record_icon);
                        recorder.stop();
                        recordButton.setVisibility(View.GONE);
                        chronometer.setVisibility(View.GONE);
                        chooseFileButton.setClickable(true);
                        if(currentState!=PAUSED_STATE)
                            chronometer.stop();
                        chronometer.reset();
                        currentState = INITIAL_STATE;
                        Intent saveIntent = new Intent(getActivity(), SavePresentationActivity.class);
                        saveIntent.putExtra("slideRecords", recorder.getSlideRecords());
                        saveIntent.putExtra("pdfPath",pdfFilePath);
                        saveIntent.putExtra("task","save");
                        startActivityForResult(saveIntent, SAVE_PRESENTATION_CODE);
                    }
                }
        );
        return view;
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_PICKER_CODE && resultCode == FilePickerActivity.RESULT_OK) {
            //Log.d("log finish","selezione conclusa");
            pdfFilePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            File f = new File(pdfFilePath);
            //check if the file selected is a valid pdf file.In positive case set the pdfView
            // NB.: For more secure and stable control in future should be implemented a Mime type control
            if(f.exists() && !f.isDirectory() && pdfFilePath.substring(pdfFilePath.lastIndexOf(".")+1).equals(("pdf"))){
                    pdfView.fromFile(f)
                            .defaultPage(0)
                            .showMinimap(false)
                            .onPageChange(this)
                            .load();
                    pdfView.setVisibility(View.VISIBLE);
                    recorder.setPage(pdfView.getCurrentPage());
                    chooseFileButton.setClickable(false);
                    recordButton.setVisibility(View.VISIBLE);
            }
            else{Toast.makeText(getActivity(), R.string.choose_valid_field, Toast.LENGTH_LONG).show();}

        }
        else if(requestCode == SAVE_PRESENTATION_CODE){
            //Log.d("log finish","attività conclusa");
            currentState = INITIAL_STATE;
            pdfFilePath = null;
            pageNum = 0;
            recorder = new PresentationRecorder(getContext(),recordTempDir.getAbsolutePath()+"/audio.mp4",0);
            pdfView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNum = page;
        //when the page displayed by the PDFView changes,
        //notify the recorder
        recorder.onPageChanged(page);
    }

}
