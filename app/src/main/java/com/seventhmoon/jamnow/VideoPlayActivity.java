package com.seventhmoon.jamnow;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;


import com.seventhmoon.jamnow.Data.Constants;
import com.seventhmoon.jamnow.Data.DottedSeekBar;


import java.text.DecimalFormat;
import java.text.NumberFormat;


import static com.seventhmoon.jamnow.MainActivity.current_video_duration;

import static com.seventhmoon.jamnow.MainActivity.current_volume;

import static com.seventhmoon.jamnow.MainActivity.mediaOperation;


import static com.seventhmoon.jamnow.MainActivity.progress_mark_b;
import static com.seventhmoon.jamnow.MainActivity.seekBar;
import static com.seventhmoon.jamnow.MainActivity.videoList;
import static com.seventhmoon.jamnow.MainActivity.video_selected;
import static com.seventhmoon.jamnow.MainActivity.current_video_position;

public class VideoPlayActivity extends AppCompatActivity {
    private static final String TAG = VideoPlayActivity.class.getName();

    private Context context;

    static SharedPreferences pref ;
    static SharedPreferences.Editor editor;
    private static final String FILE_NAME = "Preference";
    //private MediaController mediacontroller;
    private ImageView videoPlayOrPause;
    private ImageView imgVolumeChange;
    private ImageView imgFullScreen;
    private ImageView btnVideoMarkA;
    private ImageView btnVideoMarkB;
    private ImageView btnVideoClear;
    private EditText textVideoA;
    private EditText textVideoB;
    private LinearLayout linearLayoutMain;
    private LinearLayout linearLayoutTop;
    private LinearLayout linearSeekBar;
    private LinearLayout linear_ab_loop;
    private LinearLayout linearLayoutDown;
    private LinearLayout screenOfVideoView;
    private VideoView videoView;
    private boolean is_playing = false;
    private boolean is_fullscreen = false;
    private ActionBar actionBar;
    private DottedSeekBar seekBar;
    private SeekBar seekSpeedBar;
    private TextView textViewSpeed;
    private static TextView videoDuration;
    private MediaPlayer mediaPlayer;
    private static float current_video_speed = 0;
    public static boolean isVideoPlayPress = false;
    private static videoplaytask goodTask;

    private static AlertDialog dialog = null;
    //private int current_video_position;
    private static boolean is_editMarkA_video_change = false;
    private static boolean is_editMarkB_video_change = false;

    public static int progress_video_mark_a = 0;
    public static int progress_video_mark_b = 1000;

    private int ab_loop_video_start = 0;
    private int ab_loop_video_end = 0;

    private static int videoPlaying = 0;
    private static double secs_per_progress_unit = 0.0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.video_play_activity);

        secs_per_progress_unit = (double) current_video_duration / 1000.0;

        if (videoList.get(video_selected).getMark_a() == 0) {
            progress_video_mark_a = 0;
            ab_loop_video_start = 0;
        } else {
            progress_video_mark_a = (int) ((double) videoList.get(video_selected).getMark_a() / secs_per_progress_unit);
            ab_loop_video_start = videoList.get(video_selected).getMark_a();
        }

        if (videoList.get(video_selected).getDuration_u()/1000 == videoList.get(video_selected).getMark_b()) {
            progress_video_mark_b = 1000;
            ab_loop_video_end = current_video_duration;
        } else {
            progress_video_mark_b = (int) ((double) videoList.get(video_selected).getMark_b() / secs_per_progress_unit);
            ab_loop_video_end = videoList.get(video_selected).getMark_b();
        }


        Intent intent = getIntent();

        ab_loop_video_start = Integer.valueOf(intent.getStringExtra("AB_LOOP_START"));
        ab_loop_video_end = Integer.valueOf(intent.getStringExtra("AB_LOOP_END"));

        Log.d(TAG, "ab_loop_video_start = "+ab_loop_video_start+", ab_loop_video_end = "+ab_loop_video_end);

        context = getBaseContext();

        pref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);

        actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.hide();


        //videoPlayOrPause = (ImageView) findViewById(R.id.imgVideoPlayOrPause);
        videoDuration = (TextView) findViewById(R.id.textVideoDuration);
        videoView = (VideoView) findViewById(R.id.videoView);
        seekBar = (DottedSeekBar) findViewById(R.id.seekBarVideoTime);
        textViewSpeed = (TextView) findViewById(R.id.textSpeedVideo);
        seekSpeedBar = (SeekBar) findViewById(R.id.seekBarVideoSpeed);
        videoPlayOrPause = (ImageView) findViewById(R.id.imgVideoPlayOrPause);
        imgVolumeChange = (ImageView) findViewById(R.id.imgVolume);
        imgFullScreen = (ImageView) findViewById(R.id.imgFullScreen);

        linearLayoutMain = (LinearLayout) findViewById(R.id.layout_video_play);
        linearLayoutTop = (LinearLayout) findViewById(R.id.linearTop);
        linearSeekBar = (LinearLayout) findViewById(R.id.linearSeekBar);
        linear_ab_loop = (LinearLayout) findViewById(R.id.linear_ab_loop);
        linearLayoutDown = (LinearLayout) findViewById(R.id.linearDown);
        screenOfVideoView = (LinearLayout) findViewById(R.id.screenOfVideoView);

        btnVideoMarkA = (ImageView) findViewById(R.id.btnVideoMarkA);
        btnVideoMarkB = (ImageView) findViewById(R.id.btnVideoMarkB);
        btnVideoClear = (ImageView) findViewById(R.id.btnVideoClear);
        textVideoA = (EditText) findViewById(R.id.textViewVideoA);
        textVideoB = (EditText) findViewById(R.id.textViewVideoB);

        //set videoview center
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            linearSeekBar.setOrientation(LinearLayout.HORIZONTAL);


        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

        }


        //init ab loop
        seekBar.setDots(new int[]{progress_video_mark_a, progress_video_mark_b});
        seekBar.setDotsDrawable(R.drawable.dot);
        seekBar.setmLine(R.drawable.line);

        if (progress_video_mark_a > 0) {
            current_video_position = videoList.get(video_selected).getMark_a();
            seekBar.setProgress(progress_video_mark_a);
        }

        //init text
        if (videoList.get(video_selected).getMark_a() > 0) {
            NumberFormat f = new DecimalFormat("00");
            NumberFormat f2 = new DecimalFormat("000");

            //double per_unit = (double) current_video_duration / 1000.0;

            int minutes = (videoList.get(video_selected).getMark_a()) / 60000;

            int seconds = ((videoList.get(video_selected).getMark_a()) / 1000) % 60;

            int minisec = (videoList.get(video_selected).getMark_a()) % 1000;


            textVideoA.setText(f.format(minutes) + ":" + f.format(seconds) + "." + f2.format(minisec));
            setVideoDuration(videoList.get(video_selected).getMark_a());
        } else {
            textVideoA.setText("00:00.000");
            setVideoDuration(0);
        }

        if (videoList.get(video_selected).getMark_b() > 0) {
            NumberFormat f = new DecimalFormat("00");
            NumberFormat f2 = new DecimalFormat("000");

            //double per_unit = (double) current_video_duration / 1000.0;

            int minutes = (videoList.get(video_selected).getMark_b()) / 60000;

            int seconds = ((videoList.get(video_selected).getMark_b()) / 1000) % 60;

            int minisec = (videoList.get(video_selected).getMark_b()) % 1000;


            textVideoB.setText(f.format(minutes) + ":" + f.format(seconds) + "." + f2.format(minisec));
        } else {
            textVideoB.setText("00:00.000");
        }





        //mediacontroller = new MediaController(this);
        //mediacontroller.setAnchorView(videoView);
        //videoView.setMediaController(mediacontroller);
        videoView.setVideoPath(videoList.get(video_selected).getPath());
        //videoView.requestFocus();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            Bitmap thumb = ThumbnailUtils.createVideoThumbnail(videoList.get(video_selected).getPath(),
                    MediaStore.Images.Thumbnails.MINI_KIND);
            Drawable b = new BitmapDrawable(getResources(), thumb);

            videoView.setBackground(b);

            Drawable f = getResources().getDrawable(R.drawable.ic_play_circle_outline_white_48dp, getTheme());

            videoView.setForeground(f);
            videoView.setForegroundGravity(Gravity.CENTER);
        }



        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaPlayer = mp;
                mediaPlayer.setVolume(current_volume, current_volume);
                Log.d(TAG, "onPrepared");
                //videoView.start();
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.d(TAG, "onCompletion");

                Message msg = new Message();

                mIncomingHandler.sendMessage(msg);
            }
        });


        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.e(TAG, "onTouch");

                if (isVideoPlayPress) { //playing, pause
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Drawable f = getResources().getDrawable(R.drawable.ic_play_circle_outline_white_48dp, getTheme());

                        videoView.setForeground(f);
                        videoView.setForegroundGravity(Gravity.CENTER);
                    }

                    isVideoPlayPress = false;
                    videoView.pause();
                    current_video_position = videoView.getCurrentPosition();
                    Log.e(TAG, "current_video_position = "+current_video_position);
                    setTaskStop();
                    videoPlayOrPause.setImageResource(R.drawable.ic_play_circle_outline_black_48dp);
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Drawable transparentDrawable = new ColorDrawable(Color.TRANSPARENT);
                        videoView.setForeground(transparentDrawable);
                        videoView.setBackground(transparentDrawable);
                    }
                    isVideoPlayPress = true;
                    Log.e(TAG, "current_video_position = "+current_video_position);
                    videoView.seekTo(current_video_position);
                    videoView.start();
                    setTaskStart();
                    videoPlayOrPause.setImageResource(R.drawable.ic_pause_circle_outline_black_48dp);
                }

                return false;
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

                        current_video_position = (int) duration;
                        //Log.e(TAG, "=> onProgressChanged unit = "+String.valueOf(per_unit)+" duration = "+String.valueOf(duration));
                        videoView.seekTo(current_video_position);

                        setVideoDuration((int) duration);
                        //setActionBarTitle((int) duration);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (videoList.size() > 0) {
                    if (isVideoPlayPress) {
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
                        current_video_position = (int) duration;

                        videoView.seekTo(current_video_position);



                        //videoView.start();

                        if (isVideoPlayPress) { //play is pressed, state: pause -> start
                            if (!videoView.isPlaying()) {
                                //if (audioOperation.isPause()) {
                                //videoView.seekTo((int) duration);
                                videoView.start();

                                //audioOperation.setCurrentPosition(duration/1000.0);
                                //audioOperation.doPlay(songList.get(song_selected).getPath());
                            }
                        } else {
                            Log.e(TAG, "isVideoPlayPress not press");
                        }
                    } else {
                        Log.e(TAG, "current_song_duration = 0");
                    }
                }
            }
        });

        textVideoA.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "text A afterTextChanged");
                is_editMarkA_video_change = true;
                //is_seekBarTouch = false;
            }
        });

        btnVideoMarkA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (videoList.size() > 0) {

                    if (current_video_duration != 0) {

                        if (is_editMarkA_video_change) {
                            String time[];
                            String secs[];

                            if (textVideoA.getText().length() > 0) {

                                time = textVideoA.getText().toString().split(":");

                                if (time.length == 1) {
                                    Log.d(TAG, "can't find ( : )");
                                    toast(getResources().getString(R.string.invalid_input));

                                } else if (time.length > 2) {
                                    Log.d(TAG, "Invalid input!");
                                    toast(getResources().getString(R.string.invalid_input));
                                } else {

                                    secs = time[1].split("\\.");

                                    if (secs.length == 0) {
                                        Log.d(TAG, "can't find ( . )");
                                        toast(getResources().getString(R.string.invalid_input));
                                    } else {

                                        if (textVideoA.getText().length() < 9) {
                                            Log.d(TAG, "Mark A range is invalid");
                                            toast(getResources().getString(R.string.mark_a) + " " + getResources().getString(R.string.range_invalid));
                                        } else if (time[0].length() != 2 && time[1].length() != 6) {
                                            Log.d(TAG, "Mark A range is invalid");
                                            toast(getResources().getString(R.string.mark_a) + " " + getResources().getString(R.string.range_invalid));
                                        } else if (secs[0].length() != 2 && secs[1].length() != 3) {
                                            Log.d(TAG, "Mark A range is invalid");
                                            toast(getResources().getString(R.string.mark_a) + " " + getResources().getString(R.string.range_invalid));
                                        } else if (!isNumber(time[0]) ||
                                                !isNumber(secs[0]) ||
                                                !isNumber(secs[1])) {
                                            Log.d(TAG, "Invalid input!");
                                            toast(getResources().getString(R.string.invalid_input));
                                        } else {


                                            int duration = Integer.valueOf(time[0]) * 60000;
                                            duration += Integer.valueOf(secs[0]) * 1000;
                                            duration += Integer.valueOf(secs[1]);

                                            progress_video_mark_a = (duration * 1000) / current_video_duration;

                                            if (progress_video_mark_a < 1000) {
                                                seekBar.setDots(new int[]{progress_video_mark_a, progress_video_mark_b});
                                                seekBar.setDotsDrawable(R.drawable.dot);

                                                seekBar.setmLine(R.drawable.line);
                                            } else {
                                                toast("Mark A value must less than song duration");
                                                toast(getResources().getString(R.string.mark_a) + " " + getResources().getString(R.string.must_be_less_than_song_duration));
                                            }


                                        }
                                    }
                                }
                            } else {
                                toast("Mark A range should not be empty");
                            }
                            is_editMarkA_video_change = false;
                        } else { //get current seekbar position


                            double per_unit = (double) current_video_duration / 1000.0;


                            double duration = seekBar.getProgress() * per_unit;

                            Log.e(TAG, "unit = " + String.valueOf(per_unit) + " duration = " + String.valueOf(duration));

                            setTextVideoA(duration);

                            progress_video_mark_a = seekBar.getProgress();
                            seekBar.setDots(new int[]{progress_video_mark_a, progress_video_mark_b});
                            seekBar.setDotsDrawable(R.drawable.dot);
                            seekBar.setmLine(R.drawable.line);

                            //if (current_mode == MODE_PLAY_AB_LOOP) {
                                videoList.get(video_selected).setMark_a((int) duration);
                            //}
                        }


                        if (progress_video_mark_b <= progress_video_mark_a) {
                            toast("Mark B must greater than Mark A");

                            progress_video_mark_a = 0;
                            progress_video_mark_b = 1000;

                            videoList.get(video_selected).setMark_a(0);
                            videoList.get(video_selected).setMark_b(current_video_duration);

                            seekBar.setDots(new int[]{progress_video_mark_a, progress_video_mark_b});
                            seekBar.setDotsDrawable(R.drawable.dot);
                            seekBar.setmLine(R.drawable.line);

                            ab_loop_video_start = 0;
                            ab_loop_video_end = current_video_duration;
                            //mediaOperation.setAb_loop_end(current_song_duration);
                        } else {
                            Log.e(TAG, "Mark A reset");
                            //if (current_mode == MODE_PLAY_AB_LOOP) {
                            //mediaOperation.setAb_loop_start(songList.get(song_selected).getMark_a());
                            //mediaOperation.setAb_loop_end(songList.get(song_selected).getMark_b());
                            ab_loop_video_start = videoList.get(video_selected).getMark_a();
                            ab_loop_video_end = videoList.get(video_selected).getMark_b();
                            //}

                            double per_unit = (double) current_video_duration / 1000.0;


                            double duration = seekBar.getProgress() * per_unit;

                            Log.e(TAG, "unit = " + String.valueOf(per_unit) + " duration = " + String.valueOf(duration));

                            setTextVideoA(duration);

                            progress_video_mark_a = seekBar.getProgress();

                            if (progress_video_mark_b == 1000) {
                                Log.e(TAG, "progress_video_mark_b = 1000");
                                setTextVideoB(current_video_duration);
                            }

                            seekBar.setDots(new int[]{progress_video_mark_a, progress_video_mark_b});
                            seekBar.setDotsDrawable(R.drawable.dot);
                            seekBar.setmLine(R.drawable.line);

                            ab_loop_video_start = (int) duration;
                            //if (current_mode == MODE_PLAY_AB_LOOP) {
                            videoList.get(video_selected).setMark_a((int) duration);
                            //}
                        }
                    }
                }
            }
        });

        textVideoB.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "text B afterTextChanged");
                is_editMarkB_video_change = true;
                //is_seekBarTouch = false;
            }
        });

        btnVideoMarkB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoList.size() > 0) {
                    if (current_video_duration != 0) {

                        if (is_editMarkB_video_change) {
                            String time[];
                            String secs[];

                            if (textVideoB.getText().length() > 0) {

                                time = textVideoB.getText().toString().split(":");

                                if (time.length == 1) {
                                    toast("can't find ( : )");
                                } else if (time.length > 2) {
                                    toast("Invalid input!");
                                } else {

                                    secs = time[1].split("\\.");

                                    if (secs.length == 0) {
                                        toast("can't find ( . )");
                                    } else {

                                        if (textVideoB.getText().length() < 9) {
                                            toast("Mark B range is invalid");
                                        } else if (time[0].length() != 2 && time[1].length() != 6) {
                                            toast("Mark B range is invalid");
                                        } else if (secs[0].length() != 2 && secs[1].length() != 3) {
                                            toast("Mark B range is invalid");
                                        } else if (!isNumber(time[0]) ||
                                                !isNumber(secs[0]) ||
                                                !isNumber(secs[1])) {
                                            toast("Invalid input!");
                                        } else {


                                            int duration = Integer.valueOf(time[0]) * 60000;
                                            duration += Integer.valueOf(secs[0]) * 1000;
                                            duration += Integer.valueOf(secs[1]);

                                            progress_video_mark_b = (duration * 1000) / current_video_duration;

                                            if (progress_video_mark_b <= 1000) {

                                                seekBar.setDots(new int[]{progress_video_mark_a, progress_video_mark_b});
                                                seekBar.setDotsDrawable(R.drawable.dot);

                                                seekBar.setmLine(R.drawable.line);
                                            } else {
                                                toast("Mark B value must less than video duration or equal to it");
                                            }
                                        }
                                    }
                                }
                            } else {
                                toast("Mark A range should not be empty");
                            }
                            is_editMarkB_video_change = false;
                        } else {

                            double per_unit = (double) current_video_duration / 1000.0;


                            double duration = seekBar.getProgress() * per_unit;

                            Log.e(TAG, "unit = " + String.valueOf(per_unit) + " duration = " + String.valueOf(duration));

                            setTextVideoB(duration);

                            progress_video_mark_b = seekBar.getProgress();
                            seekBar.setDots(new int[]{progress_video_mark_a, progress_video_mark_b});
                            seekBar.setDotsDrawable(R.drawable.dot);
                            seekBar.setmLine(R.drawable.line);

                            //if (current_mode == MODE_PLAY_AB_LOOP) {
                                videoList.get(video_selected).setMark_b((int) duration);
                            //}
                        }

                        if (progress_video_mark_b <= progress_video_mark_a) {
                            toast("Mark B must greater than Mark A");

                            progress_video_mark_a = 0;
                            progress_video_mark_b = 1000;

                            videoList.get(video_selected).setMark_a(0);
                            videoList.get(video_selected).setMark_b(current_video_duration);

                            seekBar.setDots(new int[]{progress_video_mark_a, progress_video_mark_b});
                            seekBar.setDotsDrawable(R.drawable.dot);
                            seekBar.setmLine(R.drawable.line);

                            //mediaOperation.setAb_loop_start(0);
                            //mediaOperation.setAb_loop_end(current_song_duration);
                            ab_loop_video_start = 0;
                            ab_loop_video_end = current_video_duration;
                        } else {
                            Log.e(TAG, "Mark B reset");
                            //if (current_mode == MODE_PLAY_AB_LOOP) {
                            //mediaOperation.setAb_loop_start(songList.get(song_selected).getMark_a());
                            //mediaOperation.setAb_loop_end(songList.get(song_selected).getMark_b());
                            ab_loop_video_start = videoList.get(video_selected).getMark_a();
                            ab_loop_video_end = videoList.get(video_selected).getMark_b();
                            //}


                            double per_unit = (double) current_video_duration / 1000.0;


                            double duration = seekBar.getProgress() * per_unit;

                            Log.e(TAG, "unit = " + String.valueOf(per_unit) + " duration = " + String.valueOf(duration));

                            setTextVideoB(duration);

                            progress_video_mark_b = seekBar.getProgress();
                            seekBar.setDots(new int[]{progress_video_mark_a, progress_video_mark_b});
                            seekBar.setDotsDrawable(R.drawable.dot);
                            seekBar.setmLine(R.drawable.line);

                            ab_loop_video_end = (int) duration;
                            //if (current_mode == MODE_PLAY_AB_LOOP) {
                            videoList.get(video_selected).setMark_b((int) duration);
                            //}
                        }
                    }
                }
            }
        });

        btnVideoClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoList.size() > 0) {

                    seekBar.setDots(new int[]{});
                    seekBar.setDotsDrawable(R.drawable.dot);

                    progress_video_mark_a = 0;
                    String ta = "00:00.000";
                    textVideoA.setText(ta);

                    progress_video_mark_b = 1000;
                    String tb = "00:00.000";
                    textVideoB.setText(tb);

                    videoList.get(video_selected).setMark_a(0);
                    videoList.get(video_selected).setMark_b(current_video_duration);

                    //mediaOperation.setAb_loop_start(0);
                    //mediaOperation.setAb_loop_end(current_song_duration);
                    ab_loop_video_start = 0;
                    ab_loop_video_end = videoList.get(video_selected).getMark_b();
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                        //Bitmap thumb = ThumbnailUtils.createVideoThumbnail(videoList.get(video_selected).getPath(),
                        //        MediaStore.Images.Thumbnails.MINI_KIND);
                        //Drawable b = new BitmapDrawable(getResources(), thumb);

                        //videoView.setBackground(b);

                        Drawable f = getResources().getDrawable(R.drawable.ic_play_circle_outline_white_48dp, getTheme());

                        videoView.setForeground(f);
                        videoView.setForegroundGravity(Gravity.CENTER);
                    }

                    isVideoPlayPress = false;
                    videoView.pause();
                    current_video_position = videoView.getCurrentPosition();
                    Log.e(TAG, "current_video_position = "+current_video_position);
                    setTaskStop();
                    videoPlayOrPause.setImageResource(R.drawable.ic_play_circle_outline_black_48dp);
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Drawable transparentDrawable = new ColorDrawable(Color.TRANSPARENT);
                        videoView.setForeground(transparentDrawable);
                        videoView.setBackground(transparentDrawable);
                    }

                    isVideoPlayPress = true;
                    Log.e(TAG, "current_video_position = "+current_video_position);

                    videoView.seekTo(current_video_position);
                    videoView.start();
                    setTaskStart();
                    videoPlayOrPause.setImageResource(R.drawable.ic_pause_circle_outline_black_48dp);

                    videoPlaying = video_selected;
                }
            }
        });

        imgVolumeChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showVolumeDialog();
            }
        });

        imgFullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* (is_fullscreen) {
                    is_fullscreen = false;
                    imgFullScreen.setImageResource(R.drawable.ic_fullscreen_black_48dp);

                    linearLayoutMain.setBackgroundColor(Color.WHITE);
                    linear_ab_loop.setVisibility(View.VISIBLE);
                    videoDuration.setTextColor(Color.BLACK);
                } else {
                    is_fullscreen = true;
                    imgFullScreen.setImageResource(R.drawable.ic_fullscreen_exit_white_48dp);

                    linearLayoutMain.setBackgroundColor(Color.BLACK);
                    linear_ab_loop.setVisibility(View.GONE);
                    videoDuration.setTextColor(Color.WHITE);
                }*/
                if (!is_fullscreen) {
                    is_fullscreen = true;

                    linearLayoutMain.setBackgroundColor(Color.BLACK);
                    linearLayoutTop.setVisibility(View.GONE);
                    linearLayoutDown.setVisibility(View.GONE);

                }

                if (Build.VERSION.SDK_INT < 16)//before Jelly Bean Versions
                {
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                            WindowManager.LayoutParams.FLAG_FULLSCREEN);
                }
                else // Jelly Bean and up
                {
                    View decorView = getWindow().getDecorView();
                    // Hide the status bar.
                    int ui = View.SYSTEM_UI_FLAG_FULLSCREEN;
                    decorView.setSystemUiVisibility(ui);


                }
            }
        });
    }

    private Handler mIncomingHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Log.e(TAG, "mIncomingHandler: play finished! textVideoB = "+textVideoB);

            if (textVideoB.getText().toString().equals("00:00.000")) { //clear, no loop
                Log.e(TAG, "No Loop");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Drawable f = getResources().getDrawable(R.drawable.ic_play_circle_outline_white_48dp, getTheme());

                    videoView.setForeground(f);
                    videoView.setForegroundGravity(Gravity.CENTER);
                }

                videoPlayOrPause.setImageResource(R.drawable.ic_play_circle_outline_black_48dp);
                setTaskStop();
                seekBar.setProgress(0);
                isVideoPlayPress = false;

            } else {
                Log.e(TAG, "Loop condition");
                videoView.seekTo(ab_loop_video_start);
                videoView.start();
                setTaskStart();
            }



            return true;
        }
    });

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");

        if (videoView != null) {
            videoView.stopPlayback();
            videoView.destroyDrawingCache();
            videoView = null;
        }

        isVideoPlayPress = false;
        setTaskStop();

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

        if (is_fullscreen) {
            linearLayoutMain.setBackgroundColor(Color.WHITE);
            linearLayoutTop.setVisibility(View.VISIBLE);
            linearLayoutDown.setVisibility(View.VISIBLE);
            is_fullscreen = false;

            if (Build.VERSION.SDK_INT < 16)//before Jelly Bean Versions
            {
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
            else // Jelly Bean and up
            {
                View decorView = getWindow().getDecorView();
                // Hide the status bar.
                //int ui = View.SYSTEM_UI_FLAG_FULLSCREEN;
                int ui = View.SYSTEM_UI_FLAG_VISIBLE;
                decorView.setSystemUiVisibility(ui);


            }
        } else {
            finish();
        }


    }

    public static void setVideoDuration(int timeStamp) {

        NumberFormat f = new DecimalFormat("00");
        NumberFormat f2 = new DecimalFormat("000");


        int minutes = (timeStamp/60000);

        int seconds = (timeStamp/1000) % 60;

        int minisec = (timeStamp%1000);

        videoDuration.setText(f.format(minutes)+":"+f.format(seconds)+"."+f2.format(minisec));
    }

    private void setTextVideoA(double duration) {
        NumberFormat f = new DecimalFormat("00");
        NumberFormat f2 = new DecimalFormat("000");

        int minutes = ((int) duration) / 60000;

        int seconds = ((int) duration / 1000) % 60;

        int minisec = (int) duration % 1000;


        textVideoA.setText(f.format(minutes) + ":" + f.format(seconds) + "." + f2.format(minisec));
    }

    private void setTextVideoB(double duration) {
        NumberFormat f = new DecimalFormat("00");
        NumberFormat f2 = new DecimalFormat("000");

        int minutes = ((int) duration) / 60000;

        int seconds = ((int) duration / 1000) % 60;

        int minisec = (int) duration % 1000;


        textVideoB.setText(f.format(minutes) + ":" + f.format(seconds) + "." + f2.format(minisec));
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
                if (getPosition() > ab_loop_video_end) {
                    Log.d(TAG, "position = " + getPosition() + " ab_loop_video_start = " + ab_loop_video_start + " ab_loop_end = " + ab_loop_video_end);
                    //mediaPlayer.pause();
                    //current_state = Constants.STATE.Paused;
                    //Log.e(TAG, "==>0");
                    //if (mediaPlayer != null && current_state == Constants.STATE.Started) {
                    //    mediaPlayer.seekTo(ab_loop_start);
                    //}
                    //Log.e(TAG, "==>1");
                    setSeekTo(ab_loop_video_start);
                }


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

    public void setSeekTo(int position) {
        videoView.seekTo(position);
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

    private Handler mHandler = new Handler(new Handler.Callback() {
        public boolean handleMessage(Message msg) {
            //super.handleMessage(msg);

            Log.e(TAG, "receive close");
            dialog.dismiss();

            return true;
        }
    });

    protected void showVolumeDialog() {

        // get prompts.xml view
        /*LayoutInflater layoutInflater = LayoutInflater.from(Nfc_read_app.this);
        View promptView = layoutInflater.inflate(R.layout.input_dialog, null);*/
        View promptView = View.inflate(VideoPlayActivity.this, R.layout.volume_dialog, null);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(VideoPlayActivity.this);
        //AlertDialog dialog = null;

        alertDialogBuilder.setView(promptView);

        final TextView textVolume = (TextView) promptView.findViewById(R.id.textVolume);
        final SeekBar seekbarVolume = (SeekBar) promptView.findViewById(R.id.seekBarVolume);

        current_volume = mediaOperation.getCurrent_volume();

        seekbarVolume.setProgress(current_volume);
        String vol = mediaOperation.getCurrent_volume()+"%";
        textVolume.setText(vol);

        seekbarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String volume = String.valueOf(progress) + "%";
                textVolume.setText(volume);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mediaOperation.getCurrent_state() == Constants.STATE.Started) { //playing, doPause
                    mediaOperation.doPause();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                Log.d(TAG, "new volume = "+seekBar.getProgress());

                current_volume = seekBar.getProgress();

                mediaOperation.setCurrent_volume(current_volume);


                editor = pref.edit();
                editor.putInt("PLAY_VOLUME", current_volume);
                editor.apply();

                Message msg = new Message();
                mHandler.sendMessage(msg);
            }
        });

        // setup a dialog window
        alertDialogBuilder.setCancelable(false);

        dialog = alertDialogBuilder.show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.e(TAG, "landscape");
            linearSeekBar.setOrientation(LinearLayout.HORIZONTAL);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Log.e(TAG, "portrait");
            linearSeekBar.setOrientation(LinearLayout.VERTICAL);

        }
    }

    public void toast(String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
    }

    public boolean isNumber(String string) {
        boolean ret = false;

        if(string.matches("\\d+(?:\\.\\d+)?"))
        {
            Log.d(TAG, "string: "+string+" is number");
            ret = true;
        }
        else
        {
            Log.d(TAG, "string: "+string+" is not number");

        }

        return ret;
    }
}
