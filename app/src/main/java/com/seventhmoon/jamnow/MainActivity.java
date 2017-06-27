package com.seventhmoon.jamnow;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.os.Build;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.seventhmoon.jamnow.Data.Constants;
import com.seventhmoon.jamnow.Data.DottedSeekBar;

import com.seventhmoon.jamnow.Data.FileOperation;
import com.seventhmoon.jamnow.Data.MediaOperation;
import com.seventhmoon.jamnow.Data.Song;
import com.seventhmoon.jamnow.Data.SongArrayAdapter;
import com.seventhmoon.jamnow.Service.GetSongListFromRecordService;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.seventhmoon.jamnow.Data.FileOperation.check_file_exist;
import static com.seventhmoon.jamnow.Data.FileOperation.check_record_exist;
import static com.seventhmoon.jamnow.Data.FileOperation.init_folder_and_files;
import static com.seventhmoon.jamnow.Data.FileOperation.read_record;
import static com.seventhmoon.jamnow.MainActivity.seekBar;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();

    private static final int MODE_PLAY_ALL = 0;
    private static final int MODE_PLAY_SHUFFLE = 1;
    private static final int MODE_PLAY_REPEAT = 2;
    private static final int MODE_PLAY_AB_LOOP = 3;

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;

    static SharedPreferences pref ;
    static SharedPreferences.Editor editor;
    private static final String FILE_NAME = "Preference";

    private Context context;
    public static ListView myListview;
    SongArrayAdapter songArrayAdapter;

    MenuItem item_search;
    public static ActionBar actionBar;
    LinearLayout linearLayoutAB;
    public static TextView songDuration;
    public static DottedSeekBar seekBar;
    ImageView markButtonA, markButtonB;
    EditText textA, textB;
    ImageView btnClear;

    public static ImageView imgPlayOrPause;
    ImageView imgSkipPrev;
    ImageView imgSkipNext;
    ImageView imgFastRewind;
    ImageView imgFastForward;

    //private MediaPlayer mediaPlayer;
    public static ArrayList<Song> songList = new ArrayList<>();
    private int index = 0;


    private static BroadcastReceiver mReceiver = null;
    private static boolean isRegister = false;
    public static int song_selected = 0;
    public static int current_mode = MODE_PLAY_ALL;
    MediaOperation mediaOperation;
    public static int current_song_duration = 0;

    private static int progress_mark_a = 0;
    private static int progress_mark_b = 1000;
    //private DateFormat formatter;
    private static boolean is_seekBarTouch = false;
    private static boolean is_editMarkA_change = false;
    private static boolean is_editMarkB_change = false;

    private static int current_position = 0;
    ProgressDialog loadDialog = null;

    //public static int currentSongPlay = 0;



    public static int songPlaying = 0;

    private static String currentAcitonBarTitle;
    private static boolean isPlayPress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        setContentView(R.layout.activity_main);

        context = getBaseContext();

        pref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        current_mode = pref.getInt("PLAY_MODE", 0);


        mediaOperation = new MediaOperation(context);

        mediaOperation.setCurrent_play_mode(current_mode);

        //formatter = new SimpleDateFormat("mm:ss");

        songList.clear();

        linearLayoutAB = (LinearLayout) findViewById(R.id.layout_ab_loop);

        songDuration = (TextView) findViewById(R.id.textSongDuration);

        seekBar = (DottedSeekBar) findViewById(R.id.seekBarTime);
        textA = (EditText) findViewById(R.id.textViewA);
        textB = (EditText) findViewById(R.id.textViewB);

        markButtonA = (ImageView) findViewById(R.id.btnMarkA);
        markButtonB = (ImageView) findViewById(R.id.btnMarkB);
        btnClear = (ImageView) findViewById(R.id.btnClear);

        myListview = (ListView) findViewById(R.id.listViewMyFavorite);

        imgPlayOrPause = (ImageView) findViewById(R.id.imgPlayOrPause);
        imgSkipPrev = (ImageView) findViewById(R.id.imgSkipPrev);
        imgSkipNext = (ImageView) findViewById(R.id.imgSkipNext);
        imgFastRewind = (ImageView) findViewById(R.id.imgFastRewind);
        imgFastForward = (ImageView) findViewById(R.id.imgFastForward);


        //for action bar
        actionBar = getSupportActionBar();

        if (actionBar != null) {

            actionBar.setDisplayUseLogoEnabled(true);
            //actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            //actionBar.setHomeAsUpIndicator(R.drawable.ic_all_inclusive_white_48dp);
            //actionBar.setTitle("All");

            switch (current_mode) {
                case MODE_PLAY_ALL:
                    actionBar.setHomeAsUpIndicator(R.drawable.ic_all_inclusive_white_48dp);
                    currentAcitonBarTitle = getResources().getString(R.string.play_mode_all);
                    actionBar.setTitle(currentAcitonBarTitle);
                    linearLayoutAB.setVisibility(View.GONE);
                    break;
                case MODE_PLAY_SHUFFLE:
                    actionBar.setHomeAsUpIndicator(R.drawable.ic_shuffle_white_48dp);
                    currentAcitonBarTitle = getResources().getString(R.string.play_mode_shuffle);
                    actionBar.setTitle(currentAcitonBarTitle);
                    linearLayoutAB.setVisibility(View.GONE);
                    break;

                case MODE_PLAY_REPEAT:
                    actionBar.setHomeAsUpIndicator(R.drawable.ic_repeat_white_48dp);
                    currentAcitonBarTitle = getResources().getString(R.string.play_mode_repeat);
                    actionBar.setTitle(currentAcitonBarTitle);
                    linearLayoutAB.setVisibility(View.GONE);
                    break;

                case MODE_PLAY_AB_LOOP:
                    actionBar.setHomeAsUpIndicator(R.drawable.ic_loop_white_48dp);
                    currentAcitonBarTitle = getResources().getString(R.string.play_mode_ab_loop);
                    actionBar.setTitle(currentAcitonBarTitle);
                    linearLayoutAB.setVisibility(View.VISIBLE);
                    break;

            }
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            //init_folder_and_files();
            //init_setting();
            init_folder_and_files();
            loadSongs();

        } else {
            if(checkAndRequestPermissions()) {
                // carry on the normal flow, as the case of  permissions  granted.

                init_folder_and_files();
                loadSongs();
            }
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //Log.e(TAG, "=> onProgressChanged");

                if (mediaOperation.getCurrent_state() == Constants.STATE.Started) { //is playing
                    //Log.e(TAG, "song was playing, don't change");
                } else {

                    if (current_song_duration != 0) {

                        //NumberFormat f = new DecimalFormat("00");
                        //NumberFormat f2 = new DecimalFormat("000");

                        double per_unit = (double) current_song_duration / 1000.0;

                        double duration = seekBar.getProgress() * per_unit;

                        //Log.e(TAG, "=> onProgressChanged unit = "+String.valueOf(per_unit)+" duration = "+String.valueOf(duration));

                        setSongDuration((int) duration);
                        //setActionBarTitle((int) duration);
                    }
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.e(TAG, "onStartTrackingTouch >");

                if (isPlayPress) { //play is pressed
                    if (mediaOperation.getCurrent_state() == Constants.STATE.Started) { //if playing, pause
                        mediaOperation.doPause();
                        Log.d(TAG, "songPlaying = "+songPlaying+" song_selected = "+song_selected);

                        if (songPlaying == song_selected) {
                            Log.d(TAG, "seekBar: The same song from pause to play");

                            //mediaOperation.setSeekTo(current_position);
                        } else {
                            Log.d(TAG, "seekBar: The song was different from pause to play, stop!");
                            songPlaying = song_selected;
                            mediaOperation.doStop();
                            //current_position = 0;
                        }
                    }
                }


            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.e(TAG, "onStopTrackingTouch <");

                //use seekbar, set seekbar value for mark
                is_seekBarTouch = true;
                is_editMarkA_change = false;
                is_editMarkB_change = false;

                if (current_song_duration != 0) {

                    NumberFormat f = new DecimalFormat("00");
                    NumberFormat f2 = new DecimalFormat("000");

                    double per_unit = (double) current_song_duration / 1000.0;



                    double duration = seekBar.getProgress() * per_unit;

                    Log.e(TAG, "unit = "+String.valueOf(per_unit)+" duration = "+String.valueOf(duration));

                    /*int minutes = ((int)duration)/60000;

                    int seconds = ((int)duration/1000) % 60;

                    int minisec = (int)duration%1000;*/

                    setSongDuration((int)duration);
                    //setActionBarTitle((int)duration);

                    //songDuration.setText(f.format(minutes)+":"+f.format(seconds)+"."+f2.format(minisec));
                    /*switch (current_mode) {
                        case MODE_PLAY_ALL:
                            actionBar.setTitle(getResources().getString(R.string.play_mode_all)+"    "+f.format(minutes)+":"+f.format(seconds)+"."+f2.format(minisec));
                            break;
                        case MODE_PLAY_SHUFFLE:
                            actionBar.setTitle(getResources().getString(R.string.play_mode_shuffle)+"    "+f.format(minutes)+":"+f.format(seconds)+"."+f2.format(minisec));
                            break;
                        case MODE_PLAY_REPEAT:
                            actionBar.setTitle(getResources().getString(R.string.play_mode_repeat)+"    "+f.format(minutes)+":"+f.format(seconds)+"."+f2.format(minisec));
                            break;
                        case MODE_PLAY_AB_LOOP:
                            actionBar.setTitle(getResources().getString(R.string.play_mode_ab_loop)+"    "+f.format(minutes)+":"+f.format(seconds)+"."+f2.format(minisec));
                            break;
                    }*/

                    if (isPlayPress) { //play is pressed, state: pause -> start
                        if (mediaOperation.getCurrent_state() == Constants.STATE.Paused) {
                            mediaOperation.setSeekTo((int) duration);
                            mediaOperation.doPlay(songList.get(song_selected).getPath());
                        } else {
                            mediaOperation.doPlay(songList.get(song_selected).getPath());
                        }
                    } else {
                        current_position = (int)duration;
                        mediaOperation.setCurrentPosition(current_position);
                    }

                    /*if (!mediaOperation.isPause()) { // is playing
                        mediaOperation.doPause();
                        mediaOperation.setCurrentPosition((int)duration);
                        mediaOperation.doPlay(songList.get(song_selected).getPath());
                    } else { //pause
                        current_position = (int)duration;
                        mediaOperation.setCurrentPosition(current_position);
                    }*/
                } else {
                    Log.e(TAG, "current_song_duration = 0");
                }


            }
        });

        textA.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "text A beforeTextChanged");
                is_editMarkA_change = true;
                is_seekBarTouch = false;
            }
        });

        markButtonA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int text_a_duration;

                if (current_song_duration != 0) {

                    if (is_editMarkA_change) {
                        String time[];
                        String secs[];

                        if (textA.getText().length() > 0) {

                            time = textA.getText().toString().split(":");

                            if (time.length == 1) {
                                toast("can't find ( : )");
                            } else if (time.length > 2) {
                                toast("Invalid input!");
                            } else {

                                secs = time[1].split("\\.");

                                if (secs.length == 0) {
                                    toast("can't find ( . )");
                                } else {

                                    if (textA.getText().length() < 9) {
                                        toast("Mark A range is invalid");
                                    } else if (time[0].length() != 2 && time[1].length() != 6) {
                                        toast("Mark A range is invalid");
                                    } else if (secs[0].length() != 2 && secs[1].length() != 3) {
                                        toast("Mark A range is invalid");
                                    } else if (!isNumber(time[0]) ||
                                                !isNumber(secs[0]) ||
                                                !isNumber(secs[1]) ) {
                                        toast("Invalid input!");
                                    } else {



                                        text_a_duration = Integer.valueOf(time[0]) * 60000;
                                        text_a_duration += Integer.valueOf(secs[0]) * 1000;
                                        text_a_duration += Integer.valueOf(secs[1]);

                                        progress_mark_a = (text_a_duration * 1000) / current_song_duration;

                                        if (progress_mark_a < 1000) {
                                            seekBar.setDots(new int[]{progress_mark_a, progress_mark_b});
                                            seekBar.setDotsDrawable(R.drawable.dot);

                                            seekBar.setmLine(R.drawable.line);
                                        } else {
                                            toast("Mark A value must less than song duration");
                                        }


                                    }
                                }
                            }
                        } else {
                            toast("Mark A range should not be empty");
                        }
                    } else { //get current seekbar position
                        NumberFormat f = new DecimalFormat("00");
                        NumberFormat f2 = new DecimalFormat("000");

                        double per_unit = (double) current_song_duration / 1000.0;


                        double duration = seekBar.getProgress() * per_unit;

                        Log.e(TAG, "unit = " + String.valueOf(per_unit) + " duration = " + String.valueOf(duration));

                        int minutes = ((int) duration) / 60000;

                        int seconds = ((int) duration / 1000) % 60;

                        int minisec = (int) duration % 1000;


                        textA.setText(f.format(minutes) + ":" + f.format(seconds) + "." + f2.format(minisec));

                        progress_mark_a = seekBar.getProgress();
                        seekBar.setDots(new int[]{progress_mark_a, progress_mark_b});
                        seekBar.setDotsDrawable(R.drawable.dot);
                        seekBar.setmLine(R.drawable.line);

                        if (current_mode == MODE_PLAY_AB_LOOP) {
                            songList.get(song_selected).setMark_a((int)duration);
                        }
                    }



                    if (progress_mark_b <= progress_mark_a) {
                        toast("Mark B must greater than Mark A");

                        progress_mark_a = 0;
                        progress_mark_b = 1000;

                        songList.get(song_selected).setMark_a(0);
                        songList.get(song_selected).setMark_b(current_song_duration);

                        seekBar.setDots(new int[]{progress_mark_a, progress_mark_b});
                        seekBar.setDotsDrawable(R.drawable.dot);
                        seekBar.setmLine(R.drawable.line);

                        mediaOperation.setAb_loop_start(0);
                        mediaOperation.setAb_loop_end(current_song_duration);
                    } else {
                        Log.e(TAG, "Mark A reset");
                        //if (current_mode == MODE_PLAY_AB_LOOP) {
                            mediaOperation.setAb_loop_start(songList.get(song_selected).getMark_a());
                            mediaOperation.setAb_loop_end(songList.get(song_selected).getMark_b());
                        //}
                    }
                }



                //Log.e(TAG, "textA "+textA.getText().toString());

                //seekBar.setDots(new int[] {25, 50, 75});
                //seekBar.setDotsDrawable(R.drawable.dot);
            }
        });

        textB.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "text B beforeTextChanged");
                is_editMarkB_change = true;
                is_seekBarTouch = false;
            }
        });

        markButtonB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (current_song_duration != 0) {

                    if (is_editMarkB_change) {
                        String time[];
                        String secs[];

                        if (textB.getText().length() > 0) {

                            time = textB.getText().toString().split(":");

                            if (time.length == 1) {
                                toast("can't find ( : )");
                            } else if (time.length > 2) {
                                toast("Invalid input!");
                            } else {

                                secs = time[1].split("\\.");

                                if (secs.length == 0) {
                                    toast("can't find ( . )");
                                } else {

                                    if (textB.getText().length() < 9) {
                                        toast("Mark B range is invalid");
                                    } else if (time[0].length() != 2 && time[1].length() != 6) {
                                        toast("Mark B range is invalid");
                                    } else if (secs[0].length() != 2 && secs[1].length() != 3) {
                                        toast("Mark B range is invalid");
                                    } else if (!isNumber(time[0]) ||
                                            !isNumber(secs[0]) ||
                                            !isNumber(secs[1]) ) {
                                        toast("Invalid input!");
                                    } else {



                                        int duration = Integer.valueOf(time[0]) * 60000;
                                        duration += Integer.valueOf(secs[0]) * 1000;
                                        duration += Integer.valueOf(secs[1]);

                                        progress_mark_b = (duration * 1000) / current_song_duration;

                                        if (progress_mark_b <= 1000) {

                                            seekBar.setDots(new int[]{progress_mark_a, progress_mark_b});
                                            seekBar.setDotsDrawable(R.drawable.dot);

                                            seekBar.setmLine(R.drawable.line);
                                        } else {
                                            toast("Mark B value must less than song duration or equal to it");
                                        }
                                    }
                                }
                            }
                        } else {
                            toast("Mark A range should not be empty");
                        }
                    } else {
                        NumberFormat f = new DecimalFormat("00");
                        NumberFormat f2 = new DecimalFormat("000");

                        double per_unit = (double) current_song_duration / 1000.0;


                        double duration = seekBar.getProgress() * per_unit;

                        Log.e(TAG, "unit = " + String.valueOf(per_unit) + " duration = " + String.valueOf(duration));

                        int minutes = ((int) duration) / 60000;

                        int seconds = ((int) duration / 1000) % 60;

                        int minisec = (int) duration % 1000;


                        textB.setText(f.format(minutes) + ":" + f.format(seconds) + "." + f2.format(minisec));

                        progress_mark_b = seekBar.getProgress();
                        seekBar.setDots(new int[]{progress_mark_a, progress_mark_b});
                        seekBar.setDotsDrawable(R.drawable.dot);
                        seekBar.setmLine(R.drawable.line);

                        if (current_mode == MODE_PLAY_AB_LOOP) {
                            songList.get(song_selected).setMark_b((int)duration);
                        }
                    }

                    if (progress_mark_b <= progress_mark_a) {
                        toast("Mark B must greater than Mark A");

                        progress_mark_a = 0;
                        progress_mark_b = 1000;

                        songList.get(song_selected).setMark_a(0);
                        songList.get(song_selected).setMark_b(current_song_duration);

                        seekBar.setDots(new int[]{progress_mark_a, progress_mark_b});
                        seekBar.setDotsDrawable(R.drawable.dot);
                        seekBar.setmLine(R.drawable.line);

                        mediaOperation.setAb_loop_start(0);
                        mediaOperation.setAb_loop_end(current_song_duration);
                    } else {
                        Log.e(TAG, "Mark B reset");
                        //if (current_mode == MODE_PLAY_AB_LOOP) {
                            mediaOperation.setAb_loop_start(songList.get(song_selected).getMark_a());
                            mediaOperation.setAb_loop_end(songList.get(song_selected).getMark_b());
                        //}
                    }
                }
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekBar.setDots(new int[] {});
                seekBar.setDotsDrawable(R.drawable.dot);

                progress_mark_a = 0;
                textA.setText("00:00.000");

                progress_mark_b = 1000;
                textB.setText("00:00.000");

                songList.get(song_selected).setMark_a(0);
                songList.get(song_selected).setMark_b(current_song_duration);

                mediaOperation.setAb_loop_start(0);
                mediaOperation.setAb_loop_end(current_song_duration);
            }
        });

        imgPlayOrPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (songList.size() > 0) { //check if songs exist in list

                    if (mediaOperation.getCurrent_state() == Constants.STATE.Started) { // a song is playing, do pause
                        Log.d(TAG, "[imgPlayOrPause] isPlaying, songPlaying = "+songPlaying);
                        isPlayPress = false;

                        //songPlayBeforePause = song_selected; //save song location before pause
                        mediaOperation.doPause();
                        current_position = mediaOperation.getCurrentPosition();

                        //imgPlayOrPause.setImageResource(R.drawable.ic_play_circle_outline_black_48dp);
                    } else { //state is paused, stopped...
                        Log.d(TAG, "[imgPlayOrPause] state is paused, stopped...");
                        String songPath, songName;
                        isPlayPress = true;

                        if (song_selected > 0) {
                            songPath = songArrayAdapter.getItem(song_selected).getPath();
                            songName = songArrayAdapter.getItem(song_selected).getName();
                        } else {

                            if (current_mode == MODE_PLAY_SHUFFLE) {
                                songPath = songList.get(mediaOperation.getShufflePosition()).getPath();
                                songName = songList.get(mediaOperation.getShufflePosition()).getName();
                                song_selected = mediaOperation.getShufflePosition();

                                //deselect other
                                for (int i=0; i<songList.size(); i++) {

                                    if (i == song_selected) {
                                        songList.get(i).setSelected(true);

                                    } else {
                                        songList.get(i).setSelected(false);

                                    }
                                }

                                myListview.invalidateViews();

                                current_song_duration = songList.get(song_selected).getDuration();
                            } else {

                                songPath = songList.get(0).getPath();
                                songName = songList.get(0).getName();
                                current_song_duration = songList.get(0).getDuration();
                            }
                        }

                        if (mediaOperation.getCurrent_state() == Constants.STATE.Paused) {
                            Log.d(TAG, "state: Paused");

                            if (songPlaying == song_selected) {
                                Log.d(TAG, "The same song from pause to play");

                                if (current_mode == MODE_PLAY_AB_LOOP) {
                                    if (current_position >= songList.get(song_selected).getMark_b()) {
                                        current_position = songList.get(song_selected).getMark_a();
                                    }
                                }

                                mediaOperation.setSeekTo(current_position);
                            } else {
                                Log.d(TAG, "The song was different from pause to play, stop!");
                                songPlaying = song_selected;
                                mediaOperation.doStop();

                                if (current_mode == MODE_PLAY_AB_LOOP) {
                                    current_position = songList.get(song_selected).getMark_a();
                                    mediaOperation.setAb_loop_start(songList.get(song_selected).getMark_a());
                                    mediaOperation.setAb_loop_end(songList.get(song_selected).getMark_b());
                                } else {
                                    current_position = 0;
                                }
                            }
                        } else {
                            Log.d(TAG, "state: other");
                            songPlaying = song_selected;

                            if (current_mode == MODE_PLAY_AB_LOOP) {
                                current_position = songList.get(song_selected).getMark_a();
                                mediaOperation.setAb_loop_start(songList.get(song_selected).getMark_a());
                                mediaOperation.setAb_loop_end(songList.get(song_selected).getMark_b());
                            }
                        }

                        Log.d(TAG, "play "+songName+" position = "+current_position);
                        mediaOperation.setCurrentPosition(current_position);
                        mediaOperation.doPlay(songPath);

                        //here we compare song_select before pause and play
                        //songPlayAfterPauseToPlay = song_selected;





                        /*if (songPlayBeforePause == songPlayAfterPauseToPlay) {
                            Log.d(TAG, "The same song from pause to play");

                            if (mediaOperation.getCurrent_state() == Constants.STATE.Paused) {
                                mediaOperation.setSeekTo(current_position);

                            } else if (mediaOperation.getCurrent_state() == Constants.STATE.PlaybackCompleted) {

                            }


                        } else {
                            Log.d(TAG, "The song was different from pause to play, stop!");
                            mediaOperation.doStop();

                            current_position = 0;
                            //mediaOperation.setCurrentPosition(0);
                        }*/



                        //imgPlayOrPause.setImageResource(R.drawable.ic_pause_circle_outline_black_48dp);
                    }

                    /*if (mediaOperation.isPause()) { //if stop or pause, play
                        isPlayPress = true;

                        String songPath, songName;
                        if (song_selected > 0) {
                            songPath = songArrayAdapter.getItem(song_selected).getPath();
                            songName = songArrayAdapter.getItem(song_selected).getName();
                        } else {
                            songPath = songList.get(0).getPath();
                            songName = songList.get(0).getName();
                            current_song_duration = songList.get(0).getDuration();
                        }
                        //myListview.invalidateViews();
                        Log.d(TAG, "play "+songName+" position = "+current_position);
                        mediaOperation.setCurrentPosition(current_position);
                        currentSongPlay = song_selected;
                        mediaOperation.doPlay(songPath);

                        imgPlayOrPause.setImageResource(R.drawable.ic_pause_circle_outline_black_48dp);
                    } else { //playing, pause
                        isPlayPress = false;
                        Log.d(TAG, "pause currentSongPlay = "+currentSongPlay+" song_selected = "+song_selected);

                        if (currentSongPlay != song_selected) {
                            Log.e(TAG, "select play change");
                            current_position = 0;
                            mediaOperation.setCurrentPosition(0);
                        } else {
                            current_position = mediaOperation.getCurrentPosition();
                        }





                        Log.e(TAG, "current_position = "+current_position);

                        mediaOperation.doPause();

                        imgPlayOrPause.setImageResource(R.drawable.ic_play_circle_outline_black_48dp);
                    }*/
                } else {
                    toast("Song list is empty");
                }
            }
        });

        imgSkipPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "imgSkipPrev");

                if (mediaOperation.getCurrent_state() == Constants.STATE.Started) { //playing
                    mediaOperation.doStop();
                    mediaOperation.doPrev();
                } else {
                    if (song_selected > 0 && song_selected < songList.size() ) { //song_selected must >= 1
                        song_selected--;
                    } else {
                        song_selected = 0;
                    }

                    //deselect other
                    for (int i=0; i<songList.size(); i++) {

                        if (i == song_selected) {
                            songList.get(i).setSelected(true);

                        } else {
                            songList.get(i).setSelected(false);

                        }
                    }

                    myListview.invalidateViews();

                    current_song_duration = songList.get(song_selected).getDuration();
                }

                if (current_song_duration != 0) {

                    NumberFormat f = new DecimalFormat("00");
                    NumberFormat f2 = new DecimalFormat("000");

                    switch (current_mode) {
                        case MODE_PLAY_ALL:
                            break;
                        case MODE_PLAY_SHUFFLE:
                            break;
                        case MODE_PLAY_REPEAT:
                            break;
                        case MODE_PLAY_AB_LOOP:
                            progress_mark_a = (int) ((float) songList.get(song_selected).getMark_a() / (float) current_song_duration * 1000.0);
                            progress_mark_b = (int) ((float) songList.get(song_selected).getMark_b() / (float) current_song_duration * 1000.0);

                            int minutes_a = songList.get(song_selected).getMark_a()/60000;
                            int seconds_a = (songList.get(song_selected).getMark_a()/1000) % 60;
                            int minisec_a = songList.get(song_selected).getMark_a()%1000;

                            int minutes_b = songList.get(song_selected).getMark_b()/60000;
                            int seconds_b = (songList.get(song_selected).getMark_b()/1000) % 60;
                            int minisec_b = songList.get(song_selected).getMark_b()%1000;

                            seekBar.setDots(new int[]{progress_mark_a, progress_mark_b});
                            seekBar.setDotsDrawable(R.drawable.dot);
                            seekBar.setmLine(R.drawable.line);

                            textA.setText(f.format(minutes_a)+":"+f.format(seconds_a)+"."+f2.format(minisec_a));
                            textB.setText(f.format(minutes_b)+":"+f.format(seconds_b)+"."+f2.format(minisec_b));


                            break;
                    }

                }
            }
        });

        imgSkipNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "imgSkipNext");

                if (mediaOperation.getCurrent_state() == Constants.STATE.Started) { //playing
                    mediaOperation.doStop();
                    mediaOperation.doNext();
                } else {
                    if (song_selected < songList.size()-1 ) { //song_selected must >= 1
                        song_selected++;
                    } else {
                        song_selected = songList.size()-1;
                    }

                    //deselect other
                    for (int i=0; i<songList.size(); i++) {

                        if (i == song_selected) {
                            songList.get(i).setSelected(true);

                        } else {
                            songList.get(i).setSelected(false);

                        }
                    }

                    myListview.invalidateViews();

                    current_song_duration = songList.get(song_selected).getDuration();
                }

                if (current_song_duration != 0) {

                    NumberFormat f = new DecimalFormat("00");
                    NumberFormat f2 = new DecimalFormat("000");

                    switch (current_mode) {
                        case MODE_PLAY_ALL:
                            break;
                        case MODE_PLAY_SHUFFLE:
                            break;
                        case MODE_PLAY_REPEAT:
                            break;
                        case MODE_PLAY_AB_LOOP:
                            progress_mark_a = (int) ((float) songList.get(song_selected).getMark_a() / (float) current_song_duration * 1000.0);
                            progress_mark_b = (int) ((float) songList.get(song_selected).getMark_b() / (float) current_song_duration * 1000.0);

                            int minutes_a = songList.get(song_selected).getMark_a()/60000;
                            int seconds_a = (songList.get(song_selected).getMark_a()/1000) % 60;
                            int minisec_a = songList.get(song_selected).getMark_a()%1000;

                            int minutes_b = songList.get(song_selected).getMark_b()/60000;
                            int seconds_b = (songList.get(song_selected).getMark_b()/1000) % 60;
                            int minisec_b = songList.get(song_selected).getMark_b()%1000;

                            seekBar.setDots(new int[]{progress_mark_a, progress_mark_b});
                            seekBar.setDotsDrawable(R.drawable.dot);
                            seekBar.setmLine(R.drawable.line);

                            textA.setText(f.format(minutes_a)+":"+f.format(seconds_a)+"."+f2.format(minisec_a));
                            textB.setText(f.format(minutes_b)+":"+f.format(seconds_b)+"."+f2.format(minisec_b));


                            break;
                    }

                }
            }
        });

        imgFastRewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "state : "+mediaOperation.getCurrent_state());

                if (mediaOperation.getCurrent_state() == Constants.STATE.Started) { //if playing, pause
                    mediaOperation.doPause();
                    current_position = mediaOperation.getCurrentPosition();
                    if (current_position > 10000) {
                        mediaOperation.setSeekTo(current_position-10000);
                    } else {
                        mediaOperation.setSeekTo(0);
                    }

                    mediaOperation.doPlay(songList.get(song_selected).getPath());
                } else {

                    if (mediaOperation.getCurrent_state() == Constants.STATE.Prepared ||
                            mediaOperation.getCurrent_state() == Constants.STATE.Paused ||
                            mediaOperation.getCurrent_state() == Constants.STATE.PlaybackCompleted) {
                        current_position = mediaOperation.getCurrentPosition();
                        if (current_position > 10000) {
                            current_position = current_position - 10000;
                        } else {
                            current_position = 0;
                        }
                        mediaOperation.setSeekTo(current_position);
                    }

                    setSeekBarLocation(current_position);
                    setSongDuration(current_position);
                }
            }
        });

        imgFastForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "state : "+mediaOperation.getCurrent_state());

                if (mediaOperation.getCurrent_state() == Constants.STATE.Started) { //if playing, pause
                    mediaOperation.doPause();
                    current_position = mediaOperation.getCurrentPosition()+10000;
                    if (current_position >= current_song_duration) {
                        mediaOperation.setSeekTo(current_song_duration);
                    } else {
                        mediaOperation.setSeekTo(current_position);
                    }

                    mediaOperation.doPlay(songList.get(song_selected).getPath());
                } else {

                    if (mediaOperation.getCurrent_state() == Constants.STATE.Prepared ||
                            mediaOperation.getCurrent_state() == Constants.STATE.Paused ||
                            mediaOperation.getCurrent_state() == Constants.STATE.PlaybackCompleted) {
                        current_position = mediaOperation.getCurrentPosition()+10000;
                        if (current_position >= current_song_duration) {
                            current_position = current_song_duration;
                        }
                        mediaOperation.setSeekTo(current_position);
                    }

                    setSeekBarLocation(current_position);
                    setSongDuration(current_position);
                }
            }
        });

        myListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "select "+position);

                song_selected = position;

                //deselect other
                for (int i=0; i<songList.size(); i++) {

                    if (i == position) {
                        songList.get(i).setSelected(true);

                    } else {
                        songList.get(i).setSelected(false);

                    }
                }

                myListview.invalidateViews();

                current_song_duration = songList.get(song_selected).getDuration();

                if (current_song_duration != 0) {

                    NumberFormat f = new DecimalFormat("00");
                    NumberFormat f2 = new DecimalFormat("000");

                    switch (current_mode) {
                        case MODE_PLAY_ALL:
                            break;
                        case MODE_PLAY_SHUFFLE:
                            break;
                        case MODE_PLAY_REPEAT:
                            break;
                        case MODE_PLAY_AB_LOOP:
                            progress_mark_a = (int) ((float) songList.get(position).getMark_a() / (float) current_song_duration * 1000.0);
                            progress_mark_b = (int) ((float) songList.get(position).getMark_b() / (float) current_song_duration * 1000.0);

                            int minutes_a = songList.get(position).getMark_a()/60000;
                            int seconds_a = (songList.get(position).getMark_a()/1000) % 60;
                            int minisec_a = songList.get(position).getMark_a()%1000;

                            int minutes_b = songList.get(position).getMark_b()/60000;
                            int seconds_b = (songList.get(position).getMark_b()/1000) % 60;
                            int minisec_b = songList.get(position).getMark_b()%1000;

                            seekBar.setDots(new int[]{progress_mark_a, progress_mark_b});
                            seekBar.setDotsDrawable(R.drawable.dot);
                            seekBar.setmLine(R.drawable.line);

                            textA.setText(f.format(minutes_a)+":"+f.format(seconds_a)+"."+f2.format(minisec_a));
                            textB.setText(f.format(minutes_b)+":"+f.format(seconds_b)+"."+f2.format(minisec_b));
                            break;
                    }

                }

            }
        });



        IntentFilter filter;

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(Constants.ACTION.GET_SONGLIST_FROM_RECORD_FILE_COMPLETE)) {

                    songArrayAdapter = new SongArrayAdapter(context, R.layout.music_list_item, songList);
                    myListview.setAdapter(songArrayAdapter);

                    loadDialog.dismiss();
                    //set shuffle list
                    mediaOperation.shuffleReset();

                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION.ADD_SONG_LIST_COMPLETE)) {
                    Log.d(TAG, "receive ADD_SONG_LIST_COMPLETE !");

                    for(int i=0; i<songList.size(); i++) {

                        int duration = mediaOperation.getSongDuration(songList.get(i).getPath());
                        songList.get(i).setDuration(duration);
                        songList.get(i).setMark_a(0);
                        songList.get(i).setMark_b(duration);
                    }


                    songArrayAdapter = new SongArrayAdapter(context, R.layout.music_list_item, songList);
                    myListview.setAdapter(songArrayAdapter);

                    //write list file
                    for (int i=0; i<songList.size(); i++) {
                        String msg;
                        if (i== 0) {
                            msg = songList.get(i).getPath()+";"+
                                    songList.get(i).getDuration()+";"+songList.get(i).getMark_a()+";"+songList.get(i).getMark_b();
                        } else {
                            msg = "|"+songList.get(i).getPath()+";"+
                                    songList.get(i).getDuration()+";"+songList.get(i).getMark_a()+";"+songList.get(i).getMark_b();
                        }

                        FileOperation.append_record(msg, "favorite");
                    }

                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION.GET_PLAY_COMPLETE)) {
                    Log.d(TAG, "receive GET_PLAY_COMPLETE !");
                    imgPlayOrPause.setImageResource(R.drawable.ic_play_circle_outline_black_48dp);
                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION.MEDIAPLAYER_STATE_STARTED)) {
                    Log.d(TAG, "receive MEDIAPLAYER_STATE_STARTED !("+song_selected+")");
                    /*playtask goodTask;
                    goodTask = new playtask();
                    goodTask.execute(10);*/

                    imgPlayOrPause.setImageResource(R.drawable.ic_pause_circle_outline_black_48dp);

                    myListview.smoothScrollToPosition(song_selected);

                    for (int i=0; i<songList.size(); i++) {


                        if (i == song_selected) {
                            songList.get(i).setSelected(true);

                        } else {
                            songList.get(i).setSelected(false);

                        }


                        /*
                        View view;
                        if ((view = myListview.getChildAt(i)) != null) {
                            if (i==song_selected) {
                                view.setSelected(true);
                                view.setBackgroundColor(Color.rgb(0x4d, 0x90, 0xfe));
                            }
                            else {
                                view.setSelected(false);
                                view.setBackgroundColor(Color.TRANSPARENT);
                            }
                        }*/
                    }
                    myListview.invalidateViews();


                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION.MEDIAPLAYER_STATE_PAUSED)) {
                    Log.d(TAG, "receive MEDIAPLAYER_STATE_PAUSED !");
                    imgPlayOrPause.setImageResource(R.drawable.ic_play_circle_outline_black_48dp);
                }
            }
        };


        if (!isRegister) {
            filter = new IntentFilter();
            filter.addAction(Constants.ACTION.ADD_SONG_LIST_COMPLETE);
            filter.addAction(Constants.ACTION.GET_PLAY_COMPLETE);
            filter.addAction(Constants.ACTION.GET_SONGLIST_FROM_RECORD_FILE_COMPLETE);
            filter.addAction(Constants.ACTION.MEDIAPLAYER_STATE_STARTED);
            filter.addAction(Constants.ACTION.MEDIAPLAYER_STATE_PAUSED);
            context.registerReceiver(mReceiver, filter);
            isRegister = true;
            Log.d(TAG, "registerReceiver mReceiver");
        }
    }

    public void loadSongs() {

        if (check_record_exist("favorite")) {
            Log.d(TAG, "load file success!");
            loadDialog = new ProgressDialog(this);
            loadDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            loadDialog.setTitle("Loading...");
            loadDialog.setIndeterminate(false);
            loadDialog.setCancelable(false);

            loadDialog.show();

            Intent intent = new Intent(context, GetSongListFromRecordService.class);
            intent.setAction(Constants.ACTION.GET_SONGLIST_ACTION);
            intent.putExtra("FILENAME", "favorite");
            context.startService(intent);

            /*String message = read_record("favorite");
            Log.d(TAG, "message = "+ message);
            String msg[] = message.split("\\|");

            Log.d(TAG, "msg[0] = "+ msg[0]);




            for (int i=0; i<msg.length; i++) {

                String info[] = msg[i].split(";");

                Song new_song = new Song();
                File file = new File(info[0]); //path

                if (check_file_exist(info[0])) { // if file exist, then add
                    new_song.setName(file.getName());
                    new_song.setPath(info[0]);
                    new_song.setDuration(Integer.valueOf(info[1]));
                    new_song.setMark_a(Integer.valueOf(info[2]));
                    new_song.setMark_b(Integer.valueOf(info[3]));
                    songList.add(new_song);
                }
            }



            loadDialog.dismiss();

            songArrayAdapter = new SongArrayAdapter(context, R.layout.music_list_item, songList);
            myListview.setAdapter(songArrayAdapter);*/
        }
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");

        if (isRegister && mReceiver != null) {
            try {
                context.unregisterReceiver(mReceiver);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            isRegister = false;
            mReceiver = null;
        }

        /*if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }*/





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

        if (songArrayAdapter != null)
            songArrayAdapter.notifyDataSetChanged();

        super.onResume();
    }

    @Override
    public void onBackPressed() {

        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        //SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        item_search = menu.findItem(R.id.action_search);
        //item_clear = menu.findItem(R.id.action_clear);

        item_search.setVisible(false);

        try {
            //SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search_keeper));
            searchView.setOnQueryTextListener(queryListener);
        }catch(Exception e){
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intent;
        NumberFormat f = new DecimalFormat("00");
        NumberFormat f2 = new DecimalFormat("000");

        switch (item.getItemId()) {
            case R.id.action_add:

                intent = new Intent(MainActivity.this, FileChooseActivity.class);
                startActivity(intent);
                break;
            case R.id.action_play_all:
                actionBar.setHomeAsUpIndicator(R.drawable.ic_all_inclusive_white_48dp);
                currentAcitonBarTitle = getResources().getString(R.string.play_mode_all);
                actionBar.setTitle(currentAcitonBarTitle);
                linearLayoutAB.setVisibility(View.GONE);
                current_mode = MODE_PLAY_ALL;

                //clear loop
                seekBar.setDots(new int[] {});
                seekBar.setDotsDrawable(R.drawable.dot);

                progress_mark_a = 0;
                textA.setText("00:00.000");

                progress_mark_b = 1000;
                textB.setText("00:00.000");
                break;
            case R.id.action_shuffle:
                actionBar.setHomeAsUpIndicator(R.drawable.ic_shuffle_white_48dp);
                currentAcitonBarTitle = getResources().getString(R.string.play_mode_shuffle);
                actionBar.setTitle(currentAcitonBarTitle);
                linearLayoutAB.setVisibility(View.GONE);
                current_mode = MODE_PLAY_SHUFFLE;

                //clear loop
                seekBar.setDots(new int[] {});
                seekBar.setDotsDrawable(R.drawable.dot);

                progress_mark_a = 0;
                textA.setText("00:00.000");

                progress_mark_b = 1000;
                textB.setText("00:00.000");
                break;

            case R.id.action_repeat:
                actionBar.setHomeAsUpIndicator(R.drawable.ic_repeat_white_48dp);
                currentAcitonBarTitle = getResources().getString(R.string.play_mode_repeat);
                actionBar.setTitle(currentAcitonBarTitle);
                linearLayoutAB.setVisibility(View.GONE);
                current_mode = MODE_PLAY_REPEAT;

                //clear loop
                seekBar.setDots(new int[] {});
                seekBar.setDotsDrawable(R.drawable.dot);

                progress_mark_a = 0;
                textA.setText("00:00.000");

                progress_mark_b = 1000;
                textB.setText("00:00.000");
                break;

            case R.id.action_loop:

                Log.e(TAG, "song_selected = "+song_selected);

                actionBar.setHomeAsUpIndicator(R.drawable.ic_loop_white_48dp);
                currentAcitonBarTitle = getResources().getString(R.string.play_mode_ab_loop);
                actionBar.setTitle(currentAcitonBarTitle);
                linearLayoutAB.setVisibility(View.VISIBLE);
                current_mode = MODE_PLAY_AB_LOOP;

                int minutes_a = 0;
                int seconds_a = 0;
                int minisec_a = 0;
                int minutes_b = 0;
                int seconds_b = 0;
                int minisec_b = 0;

                if (current_song_duration > 0 ) {
                    progress_mark_a = (int) ((float) songList.get(song_selected).getMark_a() / (float) current_song_duration * 1000.0);
                    progress_mark_b = (int) ((float) songList.get(song_selected).getMark_b() / (float) current_song_duration * 1000.0);

                    minutes_a = songList.get(song_selected).getMark_a() / 60000;
                    seconds_a = (songList.get(song_selected).getMark_a() / 1000) % 60;
                    minisec_a = songList.get(song_selected).getMark_a() % 1000;

                    minutes_b = songList.get(song_selected).getMark_b() / 60000;
                    seconds_b = (songList.get(song_selected).getMark_b() / 1000) % 60;
                    minisec_b = songList.get(song_selected).getMark_b() % 1000;

                    seekBar.setDots(new int[]{progress_mark_a, progress_mark_b});
                    seekBar.setDotsDrawable(R.drawable.dot);
                    seekBar.setmLine(R.drawable.line);
                } else {
                    seekBar.setDots(new int[]{0, 1000});
                    seekBar.setDotsDrawable(R.drawable.dot);
                    seekBar.setmLine(R.drawable.line);
                }

                textA.setText(f.format(minutes_a)+":"+f.format(seconds_a)+"."+f2.format(minisec_a));
                textB.setText(f.format(minutes_b)+":"+f.format(seconds_b)+"."+f2.format(minisec_b));
                break;

        }

        editor = pref.edit();
        editor.putInt("PLAY_MODE", current_mode);
        editor.apply();

        return true;
    }

    final private android.support.v7.widget.SearchView.OnQueryTextListener queryListener = new android.support.v7.widget.SearchView.OnQueryTextListener() {
        //searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            Intent intent;

            /*
            //ArrayList<MeetingListItem> list = new ArrayList<>();
            sortedNotifyList.clear();
            if (!newText.equals("")) {



                //ArrayList<PasswordKeeperItem> list = new ArrayList<PasswordKeeperItem>();
                for (int i = 0; i < historyItemArrayList.size(); i++) {
                    if (historyItemArrayList.get(i).getTitle() != null && historyItemArrayList.get(i).getTitle().contains(newText)) {
                        sortedNotifyList.add(historyItemArrayList.get(i));
                    } else if (historyItemArrayList.get(i).getMsg() != null && historyItemArrayList.get(i).getMsg().contains(newText)) {
                        sortedNotifyList.add(historyItemArrayList.get(i));
                    } else if (historyItemArrayList.get(i).getDate() != null && historyItemArrayList.get(i).getDate().contains(newText)) {
                        sortedNotifyList.add(historyItemArrayList.get(i));
                    }
                }

                //passwordKeeperArrayAdapter = new PasswordKeeperArrayAdapter(Password_Keeper.this, R.layout.passwd_keeper_browsw_item, list);
                //listView.setAdapter(passwordKeeperArrayAdapter);

            } else {
                //ArrayList<PasswordKeeperItem> list = new ArrayList<PasswordKeeperItem>();

                for (int i = 0; i < historyItemArrayList.size(); i++) {
                    sortedNotifyList.add(historyItemArrayList.get(i));
                }


                //passwordKeeperArrayAdapter = new PasswordKeeperArrayAdapter(Password_Keeper.this, R.layout.passwd_keeper_browsw_item, list);
                //listView.setAdapter(passwordKeeperArrayAdapter);
            }

            //meetingArrayAdapter = new MeetingArrayAdapter(context, R.layout.meeting_list_item, list);
            //AllFragment.resetAdapter(list);
            //AllFragment.listView.setAdapter(AllFragment.meetingArrayAdapter);
            intent = new Intent(Constants.ACTION.GET_HISTORY_LIST_SORT_COMPLETE);
            sendBroadcast(intent);*/


            return false;
        }
    };




    private  boolean checkAndRequestPermissions() {
        //int permissionSendMessage = ContextCompat.checkSelfPermission(this,
        //        android.Manifest.permission.WRITE_CALENDAR);
        int locationPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        //int cameraPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);

        List<String> listPermissionsNeeded = new ArrayList<>();
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        //if (permissionSendMessage != PackageManager.PERMISSION_GRANTED) {
        //    listPermissionsNeeded.add(android.Manifest.permission.WRITE_CALENDAR);
        //}
        //if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
        //    listPermissionsNeeded.add(android.Manifest.permission.CAMERA);
        //}

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }


    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        //Log.e(TAG, "result size = "+grantResults.length+ "result[0] = "+grantResults[0]+", result[1] = "+grantResults[1]);


        /*switch (requestCode) {
            case 200: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.


                    Log.i(TAG, "WRITE_CALENDAR permissions granted");
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.i(TAG, "READ_CONTACTS permissions denied");

                    RetryDialog();
                }
            }
            break;

            // other 'case' lines to check for other
            // permissions this app might request
        }*/
        Log.d(TAG, "Permission callback called-------");
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {

                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                //perms.put(android.Manifest.permission.WRITE_CALENDAR, PackageManager.PERMISSION_GRANTED);
                perms.put(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                //perms.put(android.Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (//perms.get(android.Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED &&
                            perms.get(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED )
                    //&& perms.get(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                    {
                        Log.d(TAG, "write permission granted");

                        // process the normal flow
                        //else any one or both the permissions are not granted
                        init_folder_and_files();
                        //init_setting();
                    } else {
                        Log.d(TAG, "Some permissions are not granted ask again ");
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (//ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_CALENDAR) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            //|| ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.CAMERA)
                                ) {
                            showDialogOK("Warning",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    // proceed with logic by disabling the related features or quit the app.
                                                    finish();
                                                    break;
                                            }
                                        }
                                    });
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                            Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG)
                                    .show();
                            //                            //proceed with logic by disabling the related features or quit the app.
                        }
                    }
                }
            }
        }

    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("Ok", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }


    public void toast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
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

    public static void setSongDuration(int timeStamp) {
        NumberFormat f = new DecimalFormat("00");
        NumberFormat f2 = new DecimalFormat("000");


        int minutes = (timeStamp/60000);

        int seconds = (timeStamp/1000) % 60;

        int minisec = (timeStamp%1000);

        songDuration.setText(f.format(minutes)+":"+f.format(seconds)+"."+f2.format(minisec));
    }

    public static void setActionBarTitle(int timeStamp) {
        NumberFormat f = new DecimalFormat("00");
        NumberFormat f2 = new DecimalFormat("000");


        int minutes = (timeStamp/60000);

        int seconds = (timeStamp/1000) % 60;

        int minisec = (timeStamp%1000);

        actionBar.setTitle(currentAcitonBarTitle+"            "+f.format(minutes)+":"+f.format(seconds)+"."+f2.format(minisec));
    }

    public static void setSeekBarLocation(int timeStamp) {

        if (timeStamp == 0) {
            seekBar.setProgress(0);
        } else {

            double per_unit = (double) current_song_duration / 1000.0;
            double progress = (double) timeStamp / per_unit;

            Log.d(TAG, "unit = " + per_unit + ", progress = " + progress);

            seekBar.setProgress((int)progress);
        }


    }
}
