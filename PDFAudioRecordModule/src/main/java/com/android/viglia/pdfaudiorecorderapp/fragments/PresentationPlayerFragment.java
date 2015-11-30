package com.android.viglia.pdfaudiorecorderapp.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;

import com.android.viglia.pdfaudiorecorderapp.activities.PresentationPlayerActivity;
import com.android.viglia.pdfaudiorecorderapp.activities.R;
import com.android.viglia.pdfaudiorecorderapp.utility.Presentation;
import com.joanzapata.pdfview.PDFView;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

/**
 * This Fragment implements a media player for the recorded presentations
 *
 * Use the {@link PresentationPlayerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PresentationPlayerFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final int STARTED_STATE = 1;
    private static final int PAUSED_STATE = 2;
    private static final int STOPPED_STATE = 3;
    private static final int INITIALIZED_STATE = 4;
    private static final int TICK_MESSAGE = 10;

    private int currentState;
    private Map.Entry<Long,Integer> currentEntry;   //represent the current "frame". The key is the time at which the slide starts; the value is the index of the pdf page
    private Map.Entry<Long,Integer> nextEntry;  // in the same way this represent the next "frame"

    private Presentation presentation;

    private long audioDuration;
    private int pageNumber;
    private File pdfFile;
    private File audioFile;
    private MediaPlayer player;

    private long currentSlideSupLimit;

    private PDFView pdfView;
    private ImageButton play_pause_button;
    private ImageButton stop_button;
    private SeekBar seekBar;
    private Handler mHandler;
    private TickThread tickThread;

    private ActionBar actionBar;
    private View playerController;
    private boolean hiddenView;

    public static PresentationPlayerFragment newInstance(Presentation param1) {
        PresentationPlayerFragment fragment = new PresentationPlayerFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    public PresentationPlayerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getArguments() != null) {
            presentation = (Presentation) getArguments().getSerializable(ARG_PARAM1);
        }
        currentState = INITIALIZED_STATE;
        pdfFile =  new File(getActivity().getExternalFilesDir(null),presentation.getId()+"/presentation.pdf");
        audioFile =  new File(getActivity().getExternalFilesDir(null),presentation.getId()+"/audio.mp4");
        hiddenView = false;

        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            player.setDataSource(getContext(), Uri.fromFile(audioFile));
            player.prepare();
            audioDuration = (player.getDuration()/1000);
            Log.i("seekDuration",""+audioDuration);
        } catch (IOException e) {}

        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                Log.i("seek player: ", "prepared");
                if (currentState != INITIALIZED_STATE) {
                    tickThread = new TickThread(mHandler);  //start a thread that send a message to the handler every X milliseconds
                    (new Thread(tickThread)).start();
                    mediaPlayer.seekTo(seekBar.getProgress() * 1000);
                    mediaPlayer.start();
                    currentState = STARTED_STATE;
                    play_pause_button.setImageResource(R.mipmap.pause_icon);
                }
            }
        });


        currentEntry = presentation.getSlides().floorEntry(0L);
        nextEntry = presentation.getSlides().ceilingEntry(0L);

        //message Handler
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {

                switch(inputMessage.what){
                    case TICK_MESSAGE:
                        try {
                            if (currentState == STARTED_STATE) {
                                if(player.isPlaying()) {
                                    //update the seekbar according to the player progress
                                    long currentPosition = player.getCurrentPosition() / 1000;
                                    seekBar.setProgress((int) currentPosition);
                                    //if the player exceeded the current frame, update the PDF View to the next frame
                                    if (nextEntry != null && currentPosition >= nextEntry.getKey()) {
                                        pageNumber = nextEntry.getValue();
                                        pdfView.jumpTo(pageNumber);
                                        updateCachedEntries(currentPosition);
                                    }
                                }
                                else{
                                    stopPlayer(); //if we reached the end of the presentation, stop the player
                                }
                            }
                            //Log.i("handler","message received");
                        }catch(Exception exc){}
                        break;
                }
            }//
        };
        tickThread = new TickThread(mHandler);
        (new Thread(tickThread)).start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_presentation_player, container, false);

        pdfView = (PDFView) view.findViewById(R.id.pdfview_player);
        seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        seekBar.setMax((int) audioDuration);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                long progress = seekBar.getProgress();
                if(currentState == STARTED_STATE || currentState == PAUSED_STATE)
                    player.seekTo((int) progress * 1000);
                updateCachedEntries(progress);
                //Log.d("Entry detail: ","progress: "progress+" - currentEntry: "+currentEntry.getValue()+" - nextEntry:"+nextEntry.getValue());
                if (nextEntry != null && progress >= nextEntry.getKey()) {
                    pageNumber = nextEntry.getValue();
                    pdfView.jumpTo(pageNumber);
                } else {
                    pageNumber = currentEntry.getValue();
                    pdfView.jumpTo(pageNumber);
                }
            }
        });//end OnSeekBarChangeListener

        play_pause_button = (ImageButton) view.findViewById(R.id.player_play);
        //since this is a retained fragment we must set the right icon for the play button after a screen rotation
        if (currentState==STARTED_STATE){
            play_pause_button.setImageResource(R.mipmap.pause_icon);
        }
        play_pause_button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (currentState == STOPPED_STATE) {
                            player.prepareAsync();
                            play_pause_button.setImageResource(R.mipmap.pause_icon);
                        } else if (currentState == INITIALIZED_STATE || currentState == PAUSED_STATE) {
                            player.start();
                            currentState = STARTED_STATE;
                            play_pause_button.setImageResource(R.mipmap.pause_icon);
                        } else if (currentState == STARTED_STATE) {
                            player.pause();
                            currentState = PAUSED_STATE;
                            play_pause_button.setImageResource(R.mipmap.play_icon);
                        }

                    }
                }
        );//end setOnClickListener

        stop_button = (ImageButton) view.findViewById(R.id.player_stop);
        stop_button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        stopPlayer();
                    }
                }
        );//end setOnClickListener

        pdfView.fromFile(pdfFile)
                .defaultPage(pageNumber)
                .enableSwipe(false)
                .showMinimap(false)
                .load();

        pdfView.setOnTouchListener(
                new View.OnTouchListener() {

                    private GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public boolean onDoubleTap(MotionEvent e) {
                            //if the action bar and the control bar are shown, hide them
                            if (!hiddenView) {
                                if (actionBar != null)
                                    actionBar.hide();
                                playerController.animate()
                                        .translationY(playerController.getHeight()).alpha(0.0f).setDuration(250)
                                        .setListener(new AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationEnd(Animator animation) {
                                                super.onAnimationEnd(animation);
                                                playerController.setVisibility(View.GONE);
                                            }
                                        });
                                hiddenView = true;
                            } else {    // if the action bar and the control bar are hidden, show them
                                if (actionBar != null)
                                    actionBar.show();
                                playerController.animate()
                                        .translationY(0).alpha(1.0f).setDuration(250)
                                        .setListener(new AnimatorListenerAdapter() {
                                            @Override
                                            public void onAnimationStart(Animator animation) {
                                                super.onAnimationStart(animation);
                                                playerController.setVisibility(View.VISIBLE);
                                                playerController.setAlpha(0.0f);
                                            }
                                        });
                                hiddenView = false;
                            }
                            return super.onDoubleTap(e);
                        }

                    });

                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        gestureDetector.onTouchEvent(motionEvent);
                        return true;
                    }

                }//end OnTouchListener
        );
        actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        playerController = view.findViewById(R.id.player_controller);

        return view;
    }

    private void updateCachedEntries(long time){
        currentEntry = presentation.getSlides().floorEntry(time);
        nextEntry = presentation.getSlides().ceilingEntry(time);
    }

    public void stopPlayer(){
        if (currentState == STARTED_STATE || currentState == PAUSED_STATE) {
            tickThread.stopTick();
            player.seekTo(0);
            player.stop();
            currentState = STOPPED_STATE;
            play_pause_button.setImageResource(R.mipmap.play_icon);
            seekBar.setProgress(0);
            updateCachedEntries(0);
            pageNumber = currentEntry.getValue();
            pdfView.jumpTo(pageNumber);
        }
    }

    /**
     * This class is used to send a message to the main thread
     * every x milliseconds. The main thread (through the message handler),
     * if necessary, will update the interface
     * */
    private class TickThread implements Runnable{

        private boolean stopped;
        private Handler msgHandler;

        public void stopTick(){
            stopped = true;
        }

        public TickThread(Handler msgHandler){
            this.msgHandler = msgHandler;
            stopped = false;
        }

        @Override
        public void run() {
            while(! stopped){
                try {
                    Thread.sleep(500);
                }catch(InterruptedException intExc){}
                msgHandler.sendMessage(msgHandler.obtainMessage(TICK_MESSAGE));
            }
        }
    }

    //when the back button is pressed
    //stop the TickThread and the player
    public void backPressed(){
        tickThread.stopTick();
        if(currentState==STARTED_STATE || currentState==PAUSED_STATE)
            player.stop();
        player.release();
    }


}
