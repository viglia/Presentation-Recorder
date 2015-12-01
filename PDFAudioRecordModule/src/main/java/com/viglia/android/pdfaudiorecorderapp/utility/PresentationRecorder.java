package com.viglia.android.pdfaudiorecorderapp.utility;

import android.content.Context;

import com.github.lassana.recorder.AudioRecorder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * A recorder that records both the voice and the pages flipped through
 */
public class PresentationRecorder implements Serializable{

    private static final long serialVersionUID = 1L;

    private long elapsedTime;
    private ArrayList<SlideRecord> slides;
    private AudioRecorder audioRecorder;
    private int pageNumber;
    private boolean isRecording;

    /*
     * Il costruttore vuoto invoca il costruttore della classe estesa e poi inizializza la struttura che tiene conto
     * delle slide visualizzate in un certo istante di tempo
     */
    public PresentationRecorder(Context context, String fileName, int page){
        slides = new ArrayList<SlideRecord>();
        pageNumber = page;
        audioRecorder = AudioRecorder.build(context, fileName);
        elapsedTime = 0;
        pageNumber = page;
        isRecording = false;
    }

    public void start(){
        audioRecorder.start(
                new AudioRecorder.OnStartListener() {
                    @Override
                    public void onStarted() {
                        // started
                    }

                    @Override
                    public void onException(Exception e) {
                        // error
                    }
                }
        );
        elapsedTime = ((long)Calendar.getInstance().getTimeInMillis()/(long)1000)-elapsedTime;
        //currentTime --> seconds passed since the "Record Button" was pressed the first time
        //when the recorder is paused and then restarted, the currentTime restart from the last value of the currentTime variable
        //Ex: start recording: currentTime:0 --> after 2 seconds stop recording --> after some time restart recording: --> currentTime:2
        long currentTime = ((long)Calendar.getInstance().getTimeInMillis()/(long)1000)- elapsedTime;
        isRecording = true;
        //if the slides ArrayList is empty add a new SlideRecord --> first time the start method is called
        //if slides is not empty and the page number of the last slide added is different from the current one,
        //then add a SlideRecord --> when the start method is called after a "pause" event
        if(slides.isEmpty() || slides.get(slides.size()-1).getSlide()!=pageNumber ){
            slides.add((new SlideRecord(pageNumber,currentTime)));
        }
        //Log.d("Startlog","started at: "+currentTime);
    }


    public void stop(){
        audioRecorder.pause(
                new AudioRecorder.OnPauseListener() {
                    @Override
                    public void onPaused(String activeRecordFileName) {
                        // paused
                    }

                    @Override
                    public void onException(Exception e) {
                        // error
                    }
                }
        );
    }

    public void pause(){
        audioRecorder.pause(new AudioRecorder.OnPauseListener() {
            @Override
            public void onPaused(String activeRecordFileName) {
                // paused
            }

            @Override
            public void onException(Exception e) {
                // error
            }
        });
        //update the elapsedTime variable
        elapsedTime = ( (long)Calendar.getInstance().getTimeInMillis()/1000)- elapsedTime;
        isRecording = false;
        //Log.d("Pauselog","Paused at: "+elapsedTime);
    }

    public void reset(){
        elapsedTime = 0;
        slides = new ArrayList<SlideRecord>();
        pageNumber = 0;
        isRecording = false;
    }

    public ArrayList<SlideRecord> getSlideRecords(){
        return slides;
    }

    public void setPage(int page){
        pageNumber = page;
    }

    public void onPageChanged(int pageNumber){

        this.pageNumber=pageNumber;
        if(isRecording){
            //add a new SlideRecord with the current page and the elapsed time
            long currentTime = ((long)Calendar.getInstance().getTimeInMillis())/((long)1000) - elapsedTime;
            slides.add((new SlideRecord(pageNumber,currentTime)));
            //Log.d("PageChangelog", "Page changed at: " + currentTime);
        }
    }
}
