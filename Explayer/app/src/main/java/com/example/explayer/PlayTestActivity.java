package com.example.explayer;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.PlaybackPreparer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Util;

import java.util.EventListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PlayTestActivity extends AppCompatActivity {
    PlayerView playerView;
    Button btn_play,btn_speed;
    TextView textView_total,textView_current;
    SeekBar seekBar;
    SimpleExoPlayer player;
    private DataSource.Factory dataSourceFactory;
    private MediaSource mediaSource;
    private float speed = 1f;
    private ScheduledExecutorService mVideoExecutor;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        dataSourceFactory = buildDataSourceFactory();
        playerView = findViewById(R.id.player_view);
        btn_play = findViewById(R.id.btn_play);
        btn_speed = findViewById(R.id.btn_speed);
        textView_current = findViewById(R.id.text_current);
        textView_total = findViewById(R.id.text_total);
        seekBar = findViewById(R.id.seekbar_video);
        initializePlayer();

        btn_play.setOnClickListener(v -> {
            if (player.isPlaying()){
                player.setPlayWhenReady(false);
            }else {
                player.setPlayWhenReady(true);
                updateSeekBar();
            }
        });
        btn_speed.setOnClickListener(v -> {
            if (speed == 1){
                speed = 2;
            }else if (speed == 2){
                speed = 4;
            }else {
                speed = 1;
            }
            Log.e("play1","position: " + player.getCurrentPosition());
            player.setPlaybackParameters(new PlaybackParameters(speed));
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                player.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private DataSource.Factory buildDataSourceFactory() {
        return new DefaultDataSourceFactory(this, Util.getUserAgent(this, "yourApplicationName"));
    }

    private void initializePlayer() {
        if (player == null) {
            mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse("/storage/emulated/0/SmartPilot/Camera/56.mp4"));
            if (mediaSource == null) {
                return;
            }
            player = new SimpleExoPlayer.Builder(/* context= */ this)
                            .build();
            player.addListener(new PlayerEventListener());
            player.setAudioAttributes(AudioAttributes.DEFAULT, /* handleAudioFocus= */ true);
            player.setPlayWhenReady(false);
            playerView.setPlayer(player);
            playerView.setPlaybackPreparer(new mPlaybackPreparer());
        }
        player.prepare(mediaSource, true, false);
    }

    class PlayerEventListener implements Player.EventListener{

        @Override
        public void onTimelineChanged(Timeline timeline, int reason) {
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            Log.e("play_load","isLoading: " + isLoading);
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            switch (playbackState){
                case ExoPlayer.STATE_ENDED:
                    Log.e("play_end","Playback ended!");
                    //Stop playback and return to start position
                case ExoPlayer.STATE_READY:
                    textView_total.setText(SecToTime(player.getDuration()));
                    seekBar.setMax((int) player.getDuration());
                    Log.e("play","getCurrentPosition: " + SecToTime(player.getDuration()));
                    break;
            }


        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {

        }
    }


    private Handler monitorHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

           long current = player.getCurrentPosition();
            if (textView_current != null) {
                textView_current.setText(SecToTime(current));
                seekBar.setProgress((int) current);
            }
        }
    };


    private void updateSeekBar() {
        if (mVideoExecutor != null && !mVideoExecutor.isShutdown())
            mVideoExecutor.shutdownNow();
        mVideoExecutor = Executors.newScheduledThreadPool(1);
        mVideoExecutor.scheduleWithFixedDelay(
                () -> monitorHandler.sendEmptyMessage(0),
                200, //initialDelay
                200, //delay
                TimeUnit.MILLISECONDS);
    }

    class  mPlaybackPreparer implements PlaybackPreparer{

        @Override
        public void preparePlayback() {
        }
    }

    private String SecToTime(double mSec) {
        int sec = (int) Math.ceil(mSec / 1000);
        return String.format("%02d:%02d", sec / 60, sec % 60);
    }

    private int ToTime(double mSec) {
        int sec = (int) Math.ceil(mSec / 1000 /60 /60);
        return sec;
    }
}
