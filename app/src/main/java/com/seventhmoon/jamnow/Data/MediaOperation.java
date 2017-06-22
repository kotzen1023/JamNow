package com.seventhmoon.jamnow.Data;


import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;

import android.os.AsyncTask;
import android.util.Log;


import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import static com.seventhmoon.jamnow.MainActivity.currentSongPlay;
import static com.seventhmoon.jamnow.MainActivity.current_mode;

import static com.seventhmoon.jamnow.MainActivity.seekBar;
import static com.seventhmoon.jamnow.MainActivity.songDuration;
import static com.seventhmoon.jamnow.MainActivity.songList;
import static com.seventhmoon.jamnow.MainActivity.song_selected;


public class MediaOperation {
    private static final String TAG = MediaOperation.class.getName();

    private static MediaPlayer mediaPlayer;
    private int current_play_mode = 0;
    private boolean pause = true;
    private Context context;


    playtask goodTask;
    private int current_position = 0;

    private boolean taskDone = true;




    public MediaOperation (Context context){
        this.context = context;
    }

    public int getCurrent_play_mode() {
        return current_play_mode;
    }

    public void setCurrent_play_mode(int current_play_mode) {
        this.current_play_mode = current_play_mode;
    }

    public boolean isPause() {
        return pause;
    }

    public int getSongDuration(String songPath) {
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

        return duration;
    }

    public int getCurrentPosition() {

        if (mediaPlayer != null) {
            current_position = mediaPlayer.getCurrentPosition();
        } else {
            current_position = 0;
        }

        return current_position;
    }

    public void setCurrentPosition(int position) {

        this.current_position = position;
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

        Log.d(TAG, "doPause ");

        //if (!goodTask.isCancelled())
        //    goodTask.cancel(true);
        pause = true;
        mediaPlayer.pause();
    }

    public void doNext() {
        Log.d(TAG, "doNext");

        if (songList == null || songList.size() == 0) {
            return;
        }

        if (song_selected < songList.size() - 1) {

            //songList.get(song_selected).setSelected(false);

            song_selected++;
            currentSongPlay = song_selected;
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

    }

    public void doShuffle() {
        Log.d(TAG, "doShuffle");

        if (songList == null || songList.size() == 0) {
            return;
        }

        if (song_selected < songList.size() - 1) {

            songList.get(song_selected).setSelected(false);

            song_selected++;
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
                mediaPlayer.seekTo(current_position);
                mediaPlayer.start();

                if (taskDone) {
                    goodTask = new playtask();
                    goodTask.execute(10);
                    taskDone = false;
                }


                Intent newNotifyIntent = new Intent(Constants.ACTION.START_TO_PLAY);
                context.sendBroadcast(newNotifyIntent);

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        current_position = 0; //play complete, set position = 0

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

    class playtask extends AsyncTask<Integer, Integer, String>
    {
        @Override
        protected String doInBackground(Integer... countTo) {


            while(mediaPlayer.isPlaying()) {
                try {

                    //long percent = 0;
                    //if (Data.current_file_size > 0)
                    //    percent = (Data.complete_file_size * 100)/Data.current_file_size;

                    int position = ((mediaPlayer.getCurrentPosition()*1000)/mediaPlayer.getDuration());

                    publishProgress(position);
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


            NumberFormat f = new DecimalFormat("00");
            NumberFormat f2 = new DecimalFormat("000");


            int minutes = (mediaPlayer.getCurrentPosition()/60000);

            int seconds = (mediaPlayer.getCurrentPosition()/1000) % 60;

            int minisec = (mediaPlayer.getCurrentPosition()%1000);

            songDuration.setText(f.format(minutes)+":"+f.format(seconds)+"."+f2.format(minisec));
            seekBar.setProgress(values[0]);

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
                seekBar.setProgress(0);
            }

            taskDone = true;

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
