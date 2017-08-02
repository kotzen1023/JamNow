package com.seventhmoon.jamnow;

import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;


import com.seventhmoon.jamnow.Data.Constants;
import com.seventhmoon.jamnow.Data.MediaOperation;

import java.text.DecimalFormat;
import java.text.NumberFormat;


import static com.seventhmoon.jamnow.AudioFragment.setSongDuration;
import static com.seventhmoon.jamnow.MainActivity.current_song_duration;
import static com.seventhmoon.jamnow.MainActivity.current_video_duration;

import static com.seventhmoon.jamnow.MainActivity.isPlayPress;
import static com.seventhmoon.jamnow.MainActivity.mediaOperation;
import static com.seventhmoon.jamnow.MainActivity.seekBar;
import static com.seventhmoon.jamnow.MainActivity.songList;
import static com.seventhmoon.jamnow.MainActivity.song_selected;
import static com.seventhmoon.jamnow.MainActivity.videoList;
import static com.seventhmoon.jamnow.MainActivity.video_selected;

public class VideoPlayActivity extends AppCompatActivity {
    private static final String TAG = VideoPlayActivity.class.getName();

    //private MediaController mediacontroller;
    private ImageView videoPlayOrPause;
    private VideoView videoView;
    private boolean is_playing = false;
    private ActionBar actionBar;
    private SeekBar seekBar;
    private SeekBar seekSpeedBar;
    private TextView textViewSpeed;
    private static TextView videoDuration;
    private MediaPlayer mediaPlayer;
    private static float current_video_speed = 0;
    public static boolean isVideoPlayPress = false;
    private static videoplaytask goodTask;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.video_play_activity);

        actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.hide();


        //videoPlayOrPause = (ImageView) findViewById(R.id.imgVideoPlayOrPause);
        videoDuration = (TextView) findViewById(R.id.textVideoDuration);
        videoView = (VideoView) findViewById(R.id.videoView);
        seekBar = (SeekBar) findViewById(R.id.seekBarVideoTime);
        textViewSpeed = (TextView) findViewById(R.id.textSpeedVideo);
        seekSpeedBar = (SeekBar) findViewById(R.id.seekBarVideoSpeed);
        videoPlayOrPause = (ImageView) findViewById(R.id.imgVideoPlayOrPause);

        //mediacontroller = new MediaController(this);
        //mediacontroller.setAnchorView(videoView);
        //videoView.setMediaController(mediacontroller);
        videoView.setVideoPath(videoList.get(video_selected).getPath());
        videoView.requestFocus();

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaPlayer = mp;
                Log.d(TAG, "onPrepared");
                //videoView.start();
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.d(TAG, "onPrepared");
                videoPlayOrPause.setImageResource(R.drawable.ic_play_circle_outline_black_48dp);
                setTaskStop();
                seekBar.setProgress(0);
                isVideoPlayPress = false;
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (videoView.isPlaying()) {

                } else {

                    if (current_video_duration != 0) {

                        //NumberFormat f = new DecimalFormat("00");
                        //NumberFormat f2 = new DecimalFormat("000");

                        double per_unit = (double) current_video_duration / 1000.0;

                        double duration = seekBar.getProgress() * per_unit;

                        //Log.e(TAG, "=> onProgressChanged unit = "+String.valueOf(per_unit)+" duration = "+String.valueOf(duration));
                        videoView.seekTo((int) duration);

                        setVideoDuration((int) duration);
                        //setActionBarTitle((int) duration);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (videoList.size() > 0) {
                    if (videoView.isPlaying()) {
                        videoView.pause();
                    }
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (videoList.size() > 0) {
                    //is_editMarkA_change = false;
                    //is_editMarkB_change = false;

                    if (current_video_duration != 0) {

                        //NumberFormat f = new DecimalFormat("00");
                        //NumberFormat f2 = new DecimalFormat("000");

                        double per_unit = (double) current_video_duration / 1000.0;


                        double duration = seekBar.getProgress() * per_unit;

                        Log.e(TAG, "unit = " + String.valueOf(per_unit) + " duration = " + String.valueOf(duration));



                        setVideoDuration((int) duration);
                        //setActionBarTitle((int)duration);

                        videoView.seekTo((int) duration);
                        videoView.start();


                    } else {
                        Log.e(TAG, "current_song_duration = 0");
                    }
                }
            }
        });

        seekSpeedBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String value;
                if (progress < 100) {
                    value = String.valueOf((int)(50.0 + 0.5 * (progress)))+"%";

                } else {
                    value = String.valueOf(progress)+"%";

                }

                textViewSpeed.setText(value);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (seekSpeedBar.getProgress() == 0) { //min speed 0.5f (50%)
                    current_video_speed = 0.5f;
                } else if (seekSpeedBar.getProgress() > 0 && seekSpeedBar.getProgress() < 100) {
                    current_video_speed = 0.5f + ((float) seekSpeedBar.getProgress()) * 0.005f;
                } else if (seekSpeedBar.getProgress() >= 100 && seekSpeedBar.getProgress() < 200) {
                    current_video_speed = seekSpeedBar.getProgress() * 0.01f;
                } else { //speed = 2.0f
                    current_video_speed = 2.0f;
                }

                Log.d(TAG, "new speed = " + current_video_speed);

                if (mediaPlayer != null) {
                    //set speed
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Log.e(TAG, "set setPlaybackParams");
                        mediaPlayer.setPlaybackParams(mediaPlayer.getPlaybackParams().setSpeed(current_video_speed));
                    }
                }
            }
        });



        videoPlayOrPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isVideoPlayPress) { //playing, pause
                    isVideoPlayPress = false;
                    videoView.pause();
                    setTaskStop();
                    videoPlayOrPause.setImageResource(R.drawable.ic_play_circle_outline_black_48dp);
                } else {
                    isVideoPlayPress = true;
                    videoView.start();
                    setTaskStart();
                    videoPlayOrPause.setImageResource(R.drawable.ic_pause_circle_outline_black_48dp);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause");
        super.onPause();
    }
    @Override
    public void onResume() {

        Log.i(TAG, "onResume");
        super.onResume();
    }

    public void onBackPressed() {
        finish();
    }

    public static void setVideoDuration(int timeStamp) {

        NumberFormat f = new DecimalFormat("00");
        NumberFormat f2 = new DecimalFormat("000");


        int minutes = (timeStamp/60000);

        int seconds = (timeStamp/1000) % 60;

        int minisec = (timeStamp%1000);

        videoDuration.setText(f.format(minutes)+":"+f.format(seconds)+"."+f2.format(minisec));
    }

    private class videoplaytask extends AsyncTask<Integer, Integer, String>
    {
        @Override
        protected String doInBackground(Integer... countTo) {


            //while(current_state == STATE.Started) {
            while(isVideoPlayPress) {

                /*if (current_play_mode == 3) {//ab loop, check if current position is bigger than mark_b

                    if (current_state == Constants.STATE.Started)  { //pause must in started state


                        if (mediaPlayer.getCurrentPosition() < ab_loop_start || mediaPlayer.getCurrentPosition() > ab_loop_end) {

                            Log.d(TAG, "position = " + mediaPlayer.getCurrentPosition() + " ab_loop_start = " + ab_loop_start + " ab_loop_end = " + ab_loop_end);
                            //mediaPlayer.pause();
                            //current_state = Constants.STATE.Paused;
                            //Log.e(TAG, "==>0");
                            if (mediaPlayer != null && current_state == Constants.STATE.Started) {
                                mediaPlayer.seekTo(ab_loop_start);
                            }
                            //Log.e(TAG, "==>1");



                            //mediaPlayer.start();
                            //Log.e(TAG, "==>2");
                        }
                    } else {
                        Log.d(TAG, "other state...");
                    }
                }*/


                try {

                    //long percent = 0;
                    //if (Data.current_file_size > 0)
                    //    percent = (Data.complete_file_size * 100)/Data.current_file_size;
                    if (videoView != null && isVideoviewPlaying()) {
                        int position = ((getPosition() * 1000) / current_video_duration);
                        publishProgress(position);
                    }


                    Thread.sleep(100);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (videoView != null)
                Log.d(TAG, "=== playtask onPreExecute set "+current_video_duration+" ===");




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
                if (videoView != null && isVideoviewPlaying())
                    setVideoDuration(getPosition());
            } else {

                seekBar.setProgress(values[0]);
                if (videoView != null && isVideoviewPlaying())
                    setVideoDuration(getPosition());
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

            Log.d(TAG, "=== onPostExecute ===");


            seekBar.setProgress(0);

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

            Log.d(TAG, "=== onCancelled ===");
        }
    }

    public boolean isVideoviewPlaying() {
        return videoView.isPlaying();
    }

    public int getPosition() {
        return videoView.getCurrentPosition();
    }

    public void setTaskStart() {
        Log.d(TAG, "setTaskStart");
        if (goodTask == null) {
            goodTask = new videoplaytask();
            goodTask.execute(10);
        }
    }

    public void setTaskStop() {
        Log.d(TAG, "setTaskStop");
        if (goodTask != null && !goodTask.isCancelled()) {
            goodTask.cancel(true);
            goodTask = null;
        }
    }
}
