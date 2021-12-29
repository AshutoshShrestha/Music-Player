package com.example.musicplayer;

import android.annotation.SuppressLint;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

public class PlayerFragment extends Fragment {

    private static int position = 0;
    private static ArrayList<File> allSongs = null;

    Button playBtn, forwardBtn, rewindBtn, nextBtn, prevBtn, shuffleBtn, repeatBtn;
    TextView songTitle, durationEnd, durationCurrent;
    ImageView imgView;
    SeekBar seekbar;

    Thread seekbarUpdater;

    int[] shufflePositions;
    String songName;
    private static MediaPlayer mediaPlayer;

    LinearLayout player_fragment;

    int[] backgroundImageResources = {
            R.drawable.music_player_bg1,
            R.drawable.music_player_bg2,
            R.drawable.music_player_bg3,
            R.drawable.music_player_bg4,
            R.drawable.music_player_bg5,
            R.drawable.music_player_bg6
    };

    public PlayerFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            position = getArguments().getInt("POSITION");
            allSongs = (ArrayList) getArguments().getSerializable("SONGLIST");
        }
        else{
            allSongs = ((MainActivity)getActivity()).getSong(
                new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "Music")
            );
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_player, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        player_fragment = getView().findViewById(R.id.music_player);
        player_fragment.setBackgroundResource(((MainActivity)getActivity()).getCurrPlayerBg());

        playBtn = getView().findViewById(R.id.play_btn);
        nextBtn = getView().findViewById(R.id.next_btn);
        prevBtn = getView().findViewById(R.id.prev_btn);
        forwardBtn = getView().findViewById(R.id.forward_btn);
        rewindBtn = getView().findViewById(R.id.rewind_btn);
        shuffleBtn = getView().findViewById(R.id.shuffle_btn);
        repeatBtn = getView().findViewById(R.id.repeat_btn);

        seekbar = getView().findViewById(R.id.seek_bar);
        songTitle = getView().findViewById(R.id.song_title);

        durationCurrent = getView().findViewById(R.id.duration_current);
        durationEnd = getView().findViewById(R.id.duration_end);

        imgView = getView().findViewById(R.id.song_image);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Now playing");
        if(((MainActivity)getActivity()).getShuffleState()) shuffleBtn.setBackgroundResource(R.drawable.ic_shuffle_on);
        if(((MainActivity)getActivity()).getRepeatState()) repeatBtn.setBackgroundResource(R.drawable.ic_repeat_once_on);
        if(getArguments()==null && mediaPlayer==null){
            playBtn.setBackgroundResource(R.drawable.ic_pause);
        }
        else {
            if(mediaPlayer!=null){
                boolean isPaused = !mediaPlayer.isPlaying() && mediaPlayer.getCurrentPosition() > 1;
                if(isPaused){
                    playBtn.setBackgroundResource(R.drawable.ic_pause);
                }
            }
            if (getArguments() != null) {
                Log.d("ARGS", String.valueOf(getArguments().size()));
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                }
                Uri song = Uri.parse(allSongs.get(position).toString());

                mediaPlayer = MediaPlayer.create(getActivity().getBaseContext(), song);
                mediaPlayer.start();
                playBtn.setBackgroundResource(R.drawable.ic_play);
            }
            songTitle.setSelected(true);
            songName = allSongs.get(position).getName();
            songTitle.setText(songName.replace(".mp3", "").replace(".wav", ""));

            mediaPlayer.setOnCompletionListener(mp -> nextBtn.performClick());

            seekbarUpdater = new Thread() {
                @Override
                public void run() {
                    int totalTime = mediaPlayer.getDuration();
                    int currTime = 0;

                    while (currTime < totalTime) {
                        try {
                            sleep(500);
                            currTime = mediaPlayer.getCurrentPosition();
                            seekbar.setProgress(currTime);
                        } catch (InterruptedException | IllegalStateException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };

            seekbar.setMax(mediaPlayer.getDuration());
            seekbarUpdater.start();
            seekbar.getProgressDrawable()
                    .setColorFilter(ContextCompat.getColor(getActivity().getBaseContext(), R.color.purple_700),
                            PorterDuff.Mode.MULTIPLY);
            seekbar.getThumb().setColorFilter(ContextCompat.getColor(getActivity().getBaseContext(), R.color.purple_200),
                    PorterDuff.Mode.SRC_IN);

            String songEndTime = getSongTime(mediaPlayer.getDuration());
            Log.d("SONGDURATION", songEndTime);
            durationEnd.setText(songEndTime);


            final Handler handler = new Handler();
            final int updateDelay = 1000; // update time every second

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    String songCurrTime = getSongTime(mediaPlayer.getCurrentPosition());
                    durationCurrent.setText(songCurrTime);
                    handler.postDelayed(this, updateDelay);
                }
            }, updateDelay);

            seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                }
            });

            playBtn.setOnClickListener(v -> {
                if (mediaPlayer.isPlaying()) {
                    playBtn.setBackgroundResource(R.drawable.ic_pause);
                    mediaPlayer.pause();
                } else {
                    playBtn.setBackgroundResource(R.drawable.ic_play);
                    mediaPlayer.start();
                }
            });

            //TODO: make shuffle button work
            //TODO: set different background change on song change

            shuffleBtn.setOnClickListener(v -> {
                if (!((MainActivity)getActivity()).getShuffleState()) {
                    ((MainActivity)getActivity()).setShuffleState(true);
                    shuffleBtn.setBackgroundResource(R.drawable.ic_shuffle_on);
                    generateShufflePositions(mediaPlayer.getCurrentPosition());
//                        Toast.makeText(getActivity().getBaseContext(), "Shuffle on", Toast.LENGTH_SHORT).show();
                } else {
                    ((MainActivity)getActivity()).setShuffleState(false);
                    shuffleBtn.setBackgroundResource(R.drawable.ic_shuffle_off);
//                        Toast.makeText(getView().getContext(), "Shuffle off", Toast.LENGTH_SHORT).show();
                }
            });

            repeatBtn.setOnClickListener(v -> {
                if (!((MainActivity)getActivity()).getRepeatState()) {
                    ((MainActivity)getActivity()).setRepeatState(true);
                    repeatBtn.setBackgroundResource(R.drawable.ic_repeat_once_on);
                    Toast.makeText(getView().getContext(), "Repeat once on", Toast.LENGTH_SHORT).show();
                } else {
                    ((MainActivity)getActivity()).setRepeatState(false);
                    repeatBtn.setBackgroundResource(R.drawable.ic_repeat_once_off);
                    Toast.makeText(getView().getContext(), "Repeat once off", Toast.LENGTH_SHORT).show();
                }
            });

            nextBtn.setOnClickListener(v -> {
                int rand = (int)(Math.random() * 6);
                player_fragment.setBackgroundResource(backgroundImageResources[rand]);
                ((MainActivity)getActivity()).setPlayerBg(backgroundImageResources[rand]);
                mediaPlayer.stop();
                mediaPlayer.release();
                if(((MainActivity)getActivity()).getRepeatState()){
                    repeatBtn.setBackgroundResource(R.drawable.ic_repeat_once_off);
                    ((MainActivity)getActivity()).setRepeatState(false);
                }
                else{
                    position = (position + 1) % allSongs.size();
                }
                Uri uri;
                if(((MainActivity)getActivity()).getShuffleState() && shufflePositions != null){
                    uri = Uri.parse(allSongs.get(shufflePositions[position]).toString());
                    songName = allSongs.get(shufflePositions[position]).getName();
                }
                else{
                    uri = Uri.parse(allSongs.get(position).toString());
                    songName = allSongs.get(position).getName();
                }
                mediaPlayer = MediaPlayer.create(getView().getContext(), uri);
                songTitle.setText(songName.replace(".mp3", "").replace(".wav", ""));
                mediaPlayer.start();
                playBtn.setBackgroundResource(R.drawable.ic_play);

                durationEnd.setText(getSongTime(mediaPlayer.getDuration()));
            });

            prevBtn.setOnClickListener(v -> {
                int rand = (int)(Math.random() * 6);
                player_fragment.setBackgroundResource(backgroundImageResources[rand]);
                ((MainActivity)getActivity()).setPlayerBg(backgroundImageResources[rand]);
                if(mediaPlayer.getCurrentPosition()<6000) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    position = (position - 1) < 0 ? allSongs.size() - 1 : position - 1;
                    Uri uri;
                    if(((MainActivity)getActivity()).getShuffleState() && shufflePositions != null){
                        uri = Uri.parse(allSongs.get(shufflePositions[position]).toString());
                        songName = allSongs.get(shufflePositions[position]).getName();
                    }
                    else{
                        uri = Uri.parse(allSongs.get(position).toString());
                        songName = allSongs.get(position).getName();
                    }
                    mediaPlayer = MediaPlayer.create(getView().getContext(), uri);
                    songTitle.setText(songName.replace(".mp3", "").replace(".wav", ""));
                    mediaPlayer.start();
                    playBtn.setBackgroundResource(R.drawable.ic_play);
                    durationEnd.setText(getSongTime(mediaPlayer.getDuration()));
                }
                else{
                    mediaPlayer.seekTo(1);
                }
            });

            forwardBtn.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    final Handler fastForwardHandler = new Handler();

                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        fastForwardHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 4000);
                                durationCurrent.setText(getSongTime(mediaPlayer.getCurrentPosition()));
                                if (!(event.getAction() == MotionEvent.ACTION_UP)) {
                                    fastForwardHandler.postDelayed(this, 500);
                                }
                            }
                        }, 500);
                    }
                    return true;
                }
            });

            rewindBtn.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    final Handler fastForwardHandler = new Handler();

                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        fastForwardHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 4000);
                                durationCurrent.setText(getSongTime(mediaPlayer.getCurrentPosition()));
                                if (!(event.getAction() == MotionEvent.ACTION_UP) &&
                                mediaPlayer.getCurrentPosition()>1) {
                                    fastForwardHandler.postDelayed(this, 500);
                                }
                            }
                        }, 500);
                    }
                    return true;
                }
            });
        }
    }

    public void generateShufflePositions(int seed){
        shufflePositions = new int[allSongs.size()];
        boolean[] positionFilled = new boolean[allSongs.size()];

        Random rand = new Random();
        Log.d("SEED", String.valueOf(seed));
        rand.setSeed(seed);

        for(int i = 0;i < shufflePositions.length ; i++){
            shufflePositions[i] = shufflePositions.length;
            int num = Math.abs(rand.nextInt() % (shufflePositions.length-1));
            if(!positionFilled[num]){
                shufflePositions[i] = num;
                positionFilled[num] = true;
            }
        }
        for(int i = 0; i< positionFilled.length ; i++){
            if(!positionFilled[i]){
                for(int j = 0; j<shufflePositions.length;j++){
                    if(shufflePositions[j]==shufflePositions.length){
                        shufflePositions[j] = i;
                        break;
                    }
                }
            }
        }
    }

    public String getSongTime(int time){
        String s_time = "";
        int minutes = time/1000/60;
        int seconds = (time/1000)%60;

        s_time += minutes + ":";
        if(seconds<10)s_time+="0";
        s_time+=String.valueOf(seconds);
        return s_time;
    }
}