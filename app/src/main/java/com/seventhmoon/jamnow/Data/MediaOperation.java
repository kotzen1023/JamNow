package com.seventhmoon.jamnow.Data;


import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaPlayer;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;




import static com.seventhmoon.jamnow.MainActivity.current_song_duration;
import static com.seventhmoon.jamnow.MainActivity.seekBar;
import static com.seventhmoon.jamnow.MainActivity.setActionBarTitle;
import static com.seventhmoon.jamnow.MainActivity.setSongDuration;
import static com.seventhmoon.jamnow.MainActivity.songDuration;
import static com.seventhmoon.jamnow.MainActivity.songList;
import static com.seventhmoon.jamnow.MainActivity.songPlaying;
import static com.seventhmoon.jamnow.MainActivity.song_selected;
import static com.seventhmoon.jamnow.Data.Constants.STATE;


public class MediaOperation {
    private static final String TAG = MediaOperation.class.getName();



    private static MediaPlayer mediaPlayer;
    private static int current_play_mode = 0;
    private boolean pause = true;
    private Context context;


    private static playtask goodTask;
    private int current_position = 0;
    private int ab_loop_start = 0;
    private int ab_loop_end = 0;

    private boolean taskDone = true;

    private STATE current_state = STATE.Created;

    private ArrayList<Integer> shuffleList = new ArrayList<>();
    private int current_shuffle_index = 0;

    private float speed = 1;

    public MediaOperation (Context context){
        this.context = context;
    }

    public int getCurrent_play_mode() {
        return current_play_mode;
    }

    public void setCurrent_play_mode(int current_play_mode) {
        this.current_play_mode = current_play_mode;
    }

    public STATE getCurrent_state() {
        return current_state;
    }

    public void shuffleReset() {
        if (shuffleList.size() > 0) {
            shuffleList.clear();
        }

        for (int i=0; i< songList.size(); i++) {
            shuffleList.add(i);
        }
        Collections.shuffle(shuffleList);

        for (int i=0 ;i<shuffleList.size(); i++) {
            Log.e(TAG, "shuffleList["+i+"] = "+shuffleList.get(i));
        }
    }

    public int getCurrent_shuffle_index() {
        return current_shuffle_index;
    }

    public void setCurrent_shuffle_index(int current_shuffle_index) {
        this.current_shuffle_index = current_shuffle_index;
    }

    public int getShufflePosition() {
        return shuffleList.get(current_shuffle_index);
    }

    public void setAb_loop_start(int ab_loop_start) {
        this.ab_loop_start = ab_loop_start;
    }

    public void setAb_loop_end(int ab_loop_end) {
        this.ab_loop_end = ab_loop_end;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void setSeekTo(int offset) {
        Log.d(TAG, "<setSeekTo>");
        if (current_state == STATE.Prepared ||
                current_state == STATE.Started ||
                current_state == STATE.Paused ||
                current_state == STATE.PlaybackCompleted) {

            mediaPlayer.seekTo(offset);
        }
        Log.d(TAG, "</setSeekTo>");
    }

    public boolean isPause() {
        return pause;
    }

    public int getSongDuration(String songPath) {
        Log.d(TAG, "<getSongDuration>");

        int duration = 0;

        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.reset();
        } else {
            mediaPlayer.reset();

        }

        try {
            mediaPlayer.setDataSource(songPath);
            mediaPlayer.prepare();
            duration = mediaPlayer.getDuration();

        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "</getSongDuration>");

        return duration;
    }

    public int getCurrentPosition() {
        Log.d(TAG, "<getCurrentPosition>");

        if (mediaPlayer != null) {
            current_position = mediaPlayer.getCurrentPosition();
        } else {
            current_position = 0;
        }

        Log.d(TAG, "</getCurrentPosition>");

        return current_position;
    }

    public void setCurrentPosition(int position) {

        this.current_position = position;
    }

    public void doStop() {
        Log.d(TAG, "<doStop>");
        if (mediaPlayer != null) {

            if (current_state == STATE.Prepared ||
                    current_state == STATE.Started ||
                    current_state == STATE.Paused ||
                    current_state == STATE.PlaybackCompleted) {

                pause = false;
                mediaPlayer.stop();
                current_state = STATE.Stopped;
            }
        }

        if (goodTask != null) {
            Log.e(TAG, "cancel task");
            if (!goodTask.isCancelled()) {
                goodTask.cancel(true);
                goodTask = null;
            }
        }

        taskDone = true;

        Log.d(TAG, "</doStop>");
    }

    public void doPlay(String songPath) {
        Log.d(TAG, "<doPlay>");

        pause = false;
        playing(songPath);
        Log.d(TAG, "</doPlay>");
    }

    public void doPause() {

        Log.d(TAG, "<doPause>");

        //if (!goodTask.isCancelled())
        //    goodTask.cancel(true);
        if (current_state == STATE.Started) {
            //taskDone = true; //stop progress

            pause = true;
            mediaPlayer.pause();
            //set state
            current_state = Constants.STATE.Paused;

            Intent newNotifyIntent = new Intent(Constants.ACTION.MEDIAPLAYER_STATE_PAUSED);
            context.sendBroadcast(newNotifyIntent);
        }

        Log.d(TAG, "</doPause>");

    }

    public void doPrev() {
        Log.d(TAG, "<doPrev>");

        if (songList == null || songList.size() == 0) {
            return;
        }

        if (current_play_mode == 1) { //shuffle
            //find out current select
            for (int i=0; i<songList.size(); i++) {
                if (song_selected == shuffleList.get(i)) {
                    current_shuffle_index = i;
                }
            }

            //get next

            if (current_shuffle_index > 0) {
                current_shuffle_index--;
            } else {
                current_shuffle_index = 0;
            }
            song_selected = shuffleList.get(current_shuffle_index);
        } else {

            if (song_selected > 0 && song_selected < songList.size()) { //song_selected must >= 1
                song_selected--;
            } else {
                song_selected = 0;
            }
        }

        songPlaying = song_selected;
        current_song_duration = (int)(songList.get(song_selected).getDuration_u()/1000);

        ab_loop_start = songList.get(song_selected).getMark_a();
        ab_loop_end = songList.get(song_selected).getMark_b();

        if (current_play_mode == 3) { //ab loop
            current_position = ab_loop_start;
        }

        String songPath = songList.get(song_selected).getPath();
        playing(songPath);

        Log.d(TAG, "</doPrev>");
    }

    public void doNext() {
        Log.d(TAG, "<doNext>");

        if (songList == null || songList.size() == 0) {
            return;
        }

        if (current_play_mode == 1) { //shuffle
            //find out current select
            for (int i=0; i<songList.size(); i++) {
                if (song_selected == shuffleList.get(i)) {
                    current_shuffle_index = i;
                }
            }

            //get next

            if (current_shuffle_index < shuffleList.size() - 1) {
                current_shuffle_index++;
            } else {
                current_shuffle_index = 0;
            }
            song_selected = shuffleList.get(current_shuffle_index);
        } else {
            if (song_selected < songList.size() - 1) {

                //songList.get(song_selected).setSelected(false);

                song_selected++;

            } else {
                song_selected = songList.size() - 1;
            }
        }

        songPlaying = song_selected;
        current_song_duration = (int)(songList.get(song_selected).getDuration_u()/1000);

        ab_loop_start = songList.get(song_selected).getMark_a();
        ab_loop_end = songList.get(song_selected).getMark_b();

        if (current_play_mode == 3) { //ab loop
            current_position = ab_loop_start;
        }

        String songPath = songList.get(song_selected).getPath();
        playing(songPath);

        Log.d(TAG, "</doNext>");
    }

    public void doShuffle() {
        Log.d(TAG, "<doShuffle>");

        if (songList == null || songList.size() == 0) {
            return;
        }

        //find out current select
        for (int i=0; i<songList.size(); i++) {
            if (song_selected == shuffleList.get(i)) {
                current_shuffle_index = i;
            }
        }

        //get next

        if (current_shuffle_index < shuffleList.size() - 1) {
            current_shuffle_index++;
        } else {
            current_shuffle_index = 0;
        }
        song_selected = shuffleList.get(current_shuffle_index);
        songPlaying = song_selected;

        current_song_duration = (int)(songList.get(song_selected).getDuration_u()/1000);

        String songPath = songList.get(song_selected).getPath();
        playing(songPath);

        Log.d(TAG, "</doShuffle>");
    }

    public void doSingleRepeat() {
        Log.d(TAG, "<doSingleRepeat>");

        if (songList == null || songList.size() == 0) {
            return;
        }

        if (song_selected < songList.size()) {

            //songList.get(song_selected).setSelected(false);

            //song_selected++;
            songPlaying = song_selected;
            current_song_duration = (int)(songList.get(song_selected).getDuration_u()/1000);
            /*for (int i=0; i<songList.size(); i++) {
                if (i==song_selected) {
                    songList.get(song_selected).setSelected(true);
                } else {
                    songList.get(song_selected).setSelected(false);
                }
            }

            myListview.invalidateViews();*/

            String songPath = songList.get(song_selected).getPath();
            playing(songPath);
        }

        Log.d(TAG, "</doSingleRepeat>");
    }

    public void doABLoop() {
        Log.d(TAG, "<doABLoop>");

        if (songList == null || songList.size() == 0) {
            return;
        }

        if (song_selected < songList.size()) {

            //songList.get(song_selected).setSelected(false);

            //song_selected++;
            songPlaying = song_selected;
            current_song_duration = (int)(songList.get(song_selected).getDuration_u()/1000);
            current_position = songList.get(song_selected).getMark_a();
            ab_loop_end = songList.get(song_selected).getMark_b();
            /*for (int i=0; i<songList.size(); i++) {
                if (i==song_selected) {
                    songList.get(song_selected).setSelected(true);
                } else {
                    songList.get(song_selected).setSelected(false);
                }
            }

            myListview.invalidateViews();*/

            String songPath = songList.get(song_selected).getPath();
            playing(songPath);
        }

        Log.d(TAG, "</doABLoop>");
    }



    private void playing(String songPath){
        Log.d(TAG, "<playing "+songPath+">");







        //int bitRate = mf.getInteger(MediaFormat.KEY_BIT_RATE);
        //int sampleRate = mf.getInteger(MediaFormat.KEY_SAMPLE_RATE);

        //Log.d(TAG, "bitRate = "+bitRate+", sampleRate = "+sampleRate);

        if (mediaPlayer != null) {

            if (current_state == STATE.Paused) {
                Log.d(TAG, "State: "+STATE.Paused);
                mediaPlayer.start();
                //set state
                current_state = STATE.Started;

                /*if (taskDone) {
                    taskDone = false;
                    goodTask = new playtask();
                    goodTask.execute(10);

                }*/

                Intent newNotifyIntent = new Intent(Constants.ACTION.MEDIAPLAYER_STATE_PLAYED);
                context.sendBroadcast(newNotifyIntent);
            } else {
                mediaPlayer.release();
                //set state
                current_state = STATE.End;
                mediaPlayer = null;
            }
        }

        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            //set state
            current_state = STATE.Created;

            mediaPlayer.reset();
            //set state
            current_state = STATE.Idle;
            try {

                mediaPlayer.setDataSource(songPath);

                //set state
                current_state = STATE.Initialized;
                mediaPlayer.prepare();
                //set state
                current_state = STATE.Prepared;



                mediaPlayer.seekTo(current_position);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Log.e(TAG, "set setPlaybackParams");
                    mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(speed));
                }
                mediaPlayer.start();
                //set state
                current_state = STATE.Started;

                if (taskDone) {
                    taskDone = false;
                    goodTask = new playtask();
                    goodTask.execute(10);

                }


                Intent newNotifyIntent = new Intent(Constants.ACTION.MEDIAPLAYER_STATE_PLAYED);
                context.sendBroadcast(newNotifyIntent);

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        taskDone = true;
                        //set state
                        current_state = STATE.PlaybackCompleted;


                        current_position = 0; //play complete, set position = 0

                        Intent newNotifyIntent = new Intent(Constants.ACTION.GET_PLAY_COMPLETE);
                        context.sendBroadcast(newNotifyIntent);
                        switch (current_play_mode) {
                            case 0: //play all
                                doNext();
                                break;
                            case 1: //play shuffle
                                doShuffle();
                                break;
                            case 2: //single repeat
                                doSingleRepeat();
                                break;
                            case 3: //an loop
                                doABLoop();
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

        Log.d(TAG, "</playing>");
    }

    class playtask extends AsyncTask <Integer, Integer, String>
    {
        @Override
        protected String doInBackground(Integer... countTo) {


            //while(current_state == STATE.Started) {
            while(!taskDone) {

                if (current_play_mode == 3) {//ab loop, check if current position is bigger than mark_b

                    if (mediaPlayer.getCurrentPosition() < ab_loop_start || mediaPlayer.getCurrentPosition() > ab_loop_end) {

                        if (current_state == STATE.Started) { //pause must in started state

                            Log.d(TAG, "position = " + mediaPlayer.getCurrentPosition() + "ab_loop_start = " + ab_loop_start + " ab_loop_end = " + ab_loop_end);
                            mediaPlayer.pause();
                            mediaPlayer.seekTo(ab_loop_start);
                            mediaPlayer.start();
                        }
                    }
                }


                try {

                    //long percent = 0;
                    //if (Data.current_file_size > 0)
                    //    percent = (Data.complete_file_size * 100)/Data.current_file_size;
                    if (current_state == STATE.Started) {
                        int position = ((mediaPlayer.getCurrentPosition() * 1000) / mediaPlayer.getDuration());
                        publishProgress(position);
                    }


                    Thread.sleep(200);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            Log.d(TAG, "onPreExecute set "+mediaPlayer.getDuration());




            /*loadDialog = new ProgressDialog(PhotoList.this);
            loadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            loadDialog.setTitle(R.string.photolist_decrypting_title);
            loadDialog.setProgress(0);
            loadDialog.setMax(100);
            loadDialog.setIndeterminate(false);
            loadDialog.setCancelable(false);

            loadDialog.show();*/
        }

        @Override
        protected void onProgressUpdate(Integer... values) {

            super.onProgressUpdate(values);


            /*NumberFormat f = new DecimalFormat("00");
            NumberFormat f2 = new DecimalFormat("000");


            int minutes = (mediaPlayer.getCurrentPosition()/60000);

            int seconds = (mediaPlayer.getCurrentPosition()/1000) % 60;

            int minisec = (mediaPlayer.getCurrentPosition()%1000);

            songDuration.setText(f.format(minutes)+":"+f.format(seconds)+"."+f2.format(minisec));*/

            //setActionBarTitle(mediaPlayer.getCurrentPosition());

            if (values[0] >= 1000) {
                seekBar.setProgress(1000);
                setSongDuration(mediaPlayer.getDuration());
            } else {

                seekBar.setProgress(values[0]);
                setSongDuration(mediaPlayer.getCurrentPosition());
            }

            // 背景工作處理"中"更新的事
            /*long percent = 0;
            if (Data.current_file_size > 0)
                percent = (Data.complete_file_size * 100)/Data.current_file_size;

            decryptDialog.setMessage(getResources().getString(R.string.photolist_decrypting_files) + "(" + values[0] + "/" + selected_names.size() + ") " + percent + "%\n" + selected_names.get(values[0] - 1));
            */
            /*if (Data.OnDecompressing) {
                loadDialog.setTitle(getResources().getString(R.string.decompressing_files_title) + " " + Data.CompressingFileName);
                loadDialog.setProgress(values[0]);
            } else if (Data.OnDecrypting) {
                loadDialog.setTitle(getResources().getString(R.string.decrypting_files_title) + " " + Data.EnryptingOrDecryptingFileName);
                loadDialog.setProgress(values[0]);
            } else {
                loadDialog.setMessage(getResources().getString(R.string.decrypting_files_title));
            }*/

        }

        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);


            if (pause) { //if pause, don't change progress
                Log.d(TAG, "Pause was pressed while playing");
            } else {
                Log.e(TAG, "pause is not been pressed.");
                seekBar.setProgress(0);
            }

            //taskDone = true;

            //loadDialog.dismiss();
            /*btnDecrypt.setVisibility(View.INVISIBLE);
            btnShare.setVisibility(View.INVISIBLE);
            btnDelete.setVisibility(View.INVISIBLE);
            selected_count = 0;*/
        }

        @Override
        protected void onCancelled() {

            super.onCancelled();
        }
    }
}
