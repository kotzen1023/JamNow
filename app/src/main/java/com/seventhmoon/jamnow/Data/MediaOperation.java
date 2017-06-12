package com.seventhmoon.jamnow.Data;


import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.util.Log;

import com.seventhmoon.jamnow.MainActivity;
import com.seventhmoon.jamnow.R;

import java.io.IOException;

import static com.seventhmoon.jamnow.MainActivity.current_mode;
import static com.seventhmoon.jamnow.MainActivity.songList;
import static com.seventhmoon.jamnow.MainActivity.song_selected;
import static com.seventhmoon.jamnow.R.id.imgPlayOrPause;

public class MediaOperation {
    private static final String TAG = MediaOperation.class.getName();

    private static MediaPlayer mediaPlayer;
    private boolean pause = true;
    private Context context;

    public MediaOperation (Context context){
        this.context = context;
    }

    public boolean isPause() {
        return pause;
    }



    public void doStop() {
        if (mediaPlayer != null) {
            pause = false;
            mediaPlayer.stop();
        }
    }

    public void doPlay(String songPath) {
        Log.d(TAG, "doPlay");
        pause = false;
        playing(songPath);
    }

    public void doPause() {
        pause = true;
        mediaPlayer.pause();
    }

    public void doNext() {
        Log.d(TAG, "doNext");

        if (songList == null || songList.size() == 0) {
            return;
        }

        if (song_selected < songList.size() - 1) {

            song_selected++;
            songList.get(song_selected).setSelected(true);
            String songPath = songList.get(song_selected).getPath();
            playing(songPath);
        }

        /*if (songList == null || songList.size() == 0) {
            return;
        }
        if (index < songList.size() - 1) {
            index++;
            isPause = false;
            playing();
            imgPlayOrPause.setImageResource(R.drawable.ic_pause_circle_outline_black_48dp);
        }*/
    }

    public static void doPrev() {
        /*if (songList == null || songList.size() == 0) {
            return;
        }
        if (index > 0) {
            index--;
            isPause = false;
            playing();
            imgPlayOrPause.setImageResource(R.drawable.ic_pause_circle_outline_black_48dp);
        }*/
    }

    private void playing(String songPath){
        Log.d(TAG, "playing "+songPath);

        if (mediaPlayer != null && !pause) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.reset();

            try {

                mediaPlayer.setDataSource(songPath);
                mediaPlayer.prepare();
                mediaPlayer.start();
                Intent newNotifyIntent = new Intent(Constants.ACTION.START_TO_PLAY);
                context.sendBroadcast(newNotifyIntent);

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        songList.get(song_selected).setSelected(false);
                        Intent newNotifyIntent = new Intent(Constants.ACTION.GET_PLAY_COMPLETE);
                        context.sendBroadcast(newNotifyIntent);
                        switch (current_mode) {
                            case 0:
                                doNext();
                                break;
                            case 1:
                                break;
                            case 2:
                                break;
                            case 3:
                                break;
                        }

                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                Intent newNotifyIntent = new Intent(Constants.ACTION.GET_PLAY_COMPLETE);
                context.sendBroadcast(newNotifyIntent);
            }
        }


    }


}
