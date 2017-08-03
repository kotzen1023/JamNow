package com.seventhmoon.jamnow;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.seventhmoon.jamnow.Data.MediaOperation;
import com.seventhmoon.jamnow.Data.SongArrayAdapter;
import com.seventhmoon.jamnow.Service.GetSongListFromRecordService;
import com.seventhmoon.jamnow.Service.SaveListToFileService;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import static android.content.Context.MODE_PRIVATE;
import static com.seventhmoon.jamnow.Data.FileOperation.check_record_exist;
import static com.seventhmoon.jamnow.MainActivity.addSongList;
import static com.seventhmoon.jamnow.MainActivity.item_clear;
import static com.seventhmoon.jamnow.MainActivity.item_remove;
import static com.seventhmoon.jamnow.MainActivity.linearSpeed;
import static com.seventhmoon.jamnow.MainActivity.linearLayoutAB;
import static com.seventhmoon.jamnow.MainActivity.layout_seekbar_time;
import static com.seventhmoon.jamnow.MainActivity.loadDialog;
import static com.seventhmoon.jamnow.MainActivity.mediaOperation;
import static com.seventhmoon.jamnow.MainActivity.songArrayAdapter;
import static com.seventhmoon.jamnow.MainActivity.songList;
import static com.seventhmoon.jamnow.MainActivity.seekBar;
import static com.seventhmoon.jamnow.MainActivity.textA;
import static com.seventhmoon.jamnow.MainActivity.textB;
import static com.seventhmoon.jamnow.MainActivity.progress_mark_a;
import static com.seventhmoon.jamnow.MainActivity.progress_mark_b;
import static com.seventhmoon.jamnow.MainActivity.isPlayPress;
import static com.seventhmoon.jamnow.MainActivity.songDuration;
import static com.seventhmoon.jamnow.MainActivity.song_selected;
import static com.seventhmoon.jamnow.MainActivity.songPlaying;
import static com.seventhmoon.jamnow.MainActivity.current_song_duration;
import static com.seventhmoon.jamnow.MainActivity.current_mode;
import static com.seventhmoon.jamnow.MainActivity.current_position;
import static com.seventhmoon.jamnow.MainActivity.MODE_PLAY_ALL;
import static com.seventhmoon.jamnow.MainActivity.MODE_PLAY_SHUFFLE;
import static com.seventhmoon.jamnow.MainActivity.MODE_PLAY_REPEAT;
import static com.seventhmoon.jamnow.MainActivity.MODE_PLAY_AB_LOOP;

public class AudioFragment extends Fragment {
    private static final String TAG = AudioFragment.class.getName();

    //private static final int MODE_PLAY_ALL = 0;
    //private static final int MODE_PLAY_SHUFFLE = 1;
    //private static final int MODE_PLAY_REPEAT = 2;
    //private static final int MODE_PLAY_AB_LOOP = 3;

    //public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;

    private Context context;

    private ListView myListview;
    //SongArrayAdapter songArrayAdapter;

    //LinearLayout linearLayoutAB;
    //LinearLayout linearSpeed;
    //private static TextView songDuration;
    //public static DottedSeekBar seekBar;
    private static DottedSeekBar speedBar;
    ImageView markButtonA, markButtonB;
    //EditText textA, textB;
    TextView textSpeed;
    ImageView btnClear;

    private ImageView imgPlayOrPause;
    ImageView imgSkipPrev;
    ImageView imgSkipNext;
    ImageView imgFastRewind;
    ImageView imgFastForward;

    private static BroadcastReceiver mReceiver = null;
    private static boolean isRegister = false;
    //public static int song_selected = 0;
    //public static int current_mode = MODE_PLAY_ALL;
    //public static int current_volume = 50;
    //MediaOperation mediaOperation;
    //AudioOperation audioOperation;
    //public static int current_song_duration = 0;

    //private static int progress_mark_a = 0;
    //private static int progress_mark_b = 1000;
    //private DateFormat formatter;
    //private static boolean is_seekBarTouch = false;
    private static boolean is_editMarkA_change = false;
    private static boolean is_editMarkB_change = false;

    //private static int current_position = 0;
    private static float current_speed = 0;
    //private static double current_position_d = 0.0;
    //ProgressDialog loadDialog = null;

    //public static int currentSongPlay = 0;



    //public static int songPlaying = 0;

    //private static String currentAcitonBarTitle;
    //public static boolean isPlayPress = false;

    private static AlertDialog dialog = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getContext();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView");

        View view = inflater.inflate(R.layout.audio_fragment, container, false);


        //audioOperation.setCurrent_play_mode(current_mode);

        //formatter = new SimpleDateFormat("mm:ss");

        //songList.clear();

        layout_seekbar_time = (LinearLayout) view.findViewById(R.id.layout_seekbar_time);
        linearLayoutAB = (LinearLayout) view.findViewById(R.id.layout_ab_loop);
        linearSpeed = (LinearLayout) view.findViewById(R.id.linearSpeed);

        songDuration = (TextView) view.findViewById(R.id.textSongDuration);

        seekBar = (DottedSeekBar) view.findViewById(R.id.seekBarTime);
        speedBar = (DottedSeekBar) view.findViewById(R.id.seekBarSpeed);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            linearSpeed.setVisibility(View.VISIBLE);
        } else {
            linearSpeed.setVisibility(View.GONE);
        }

        textA = (EditText) view.findViewById(R.id.textViewA);
        textB = (EditText) view.findViewById(R.id.textViewB);
        textSpeed = (TextView) view.findViewById(R.id.textSpeed);

        markButtonA = (ImageView) view.findViewById(R.id.btnMarkA);
        markButtonB = (ImageView) view.findViewById(R.id.btnMarkB);
        btnClear = (ImageView) view.findViewById(R.id.btnClear);

        myListview = (ListView) view.findViewById(R.id.listViewMyFavorite);

        imgPlayOrPause = (ImageView) view.findViewById(R.id.imgPlayOrPause);
        imgSkipPrev = (ImageView) view.findViewById(R.id.imgSkipPrev);
        imgSkipNext = (ImageView) view.findViewById(R.id.imgSkipNext);
        imgFastRewind = (ImageView) view.findViewById(R.id.imgFastRewind);
        imgFastForward = (ImageView) view.findViewById(R.id.imgFastForward);




        /*if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
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
        }*/

        speedBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String value;
                if (progress < 100) {
                    value = String.valueOf((int)(50.0 + 0.5 * (progress)))+"%";

                } else {
                    value = String.valueOf(progress)+"%";

                }

                textSpeed.setText(value);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.e(TAG, "onStartTrackingTouch Speed >");

                if (songList.size() > 0) {

                    if (mediaOperation.getCurrent_state() == Constants.STATE.Started) { //if playing, pause
                        mediaOperation.doPause();

                        current_speed = mediaOperation.getSpeed();

                        Log.d(TAG, "original speed = " + current_speed);


                    }
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.e(TAG, "onStopTrackingTouch Speed <");
                if (songList.size() > 0) {

                    if (speedBar.getProgress() == 0) { //min speed 0.5f (50%)
                        current_speed = 0.5f;
                    } else if (speedBar.getProgress() > 0 && speedBar.getProgress() < 100) {
                        current_speed = 0.5f + ((float) speedBar.getProgress()) * 0.005f;
                    } else if (speedBar.getProgress() >= 100 && speedBar.getProgress() < 200) {
                        current_speed = speedBar.getProgress() * 0.01f;
                    } else { //speed = 2.0f
                        current_speed = 2.0f;
                    }

                    Log.d(TAG, "new speed = " + current_speed);

                    mediaOperation.setSpeed(current_speed);

                    //if (mediaOperation.getCurrent_state() == Constants.STATE.Paused) {
                    if (isPlayPress) {
                        //if (audioOperation.isPause()) {
                        //mediaOperation.setSeekTo((int) duration);
                        if (mediaOperation.getCurrent_state() == Constants.STATE.Paused) {
                            mediaOperation.doPlay(songList.get(song_selected).getPath());
                        } else {
                            Log.e(TAG, "Not Pause state");
                        }

                        //audioOperation.setCurrentPosition(duration/1000.0);
                        //audioOperation.doPlay(songList.get(song_selected).getPath());
                    }
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //Log.e(TAG, "=> onProgressChanged");

                if (mediaOperation.getCurrent_state() == Constants.STATE.Started) { //is playing
                    //Log.e(TAG, "song was playing, don't change");
                } else {
                    //if (mediaOperation.getCurrent_state() != Constants.STATE.Started) {
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

                if (songList.size() > 0) {

                    if (isPlayPress) { //play is pressed
                        if (mediaOperation.getCurrent_state() == Constants.STATE.Started) { //if playing, pause
                            mediaOperation.doPause();
                            Log.d(TAG, "songPlaying = " + songPlaying + " song_selected = " + song_selected);

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
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.e(TAG, "onStopTrackingTouch <");

                if (songList.size() > 0) {
                    //use seekbar, set seekbar value for mark
                    //is_seekBarTouch = true;
                    is_editMarkA_change = false;
                    is_editMarkB_change = false;

                    if (current_song_duration != 0) {

                        //NumberFormat f = new DecimalFormat("00");
                        //NumberFormat f2 = new DecimalFormat("000");

                        double per_unit = (double) current_song_duration / 1000.0;


                        double duration = seekBar.getProgress() * per_unit;

                        Log.e(TAG, "unit = " + String.valueOf(per_unit) + " duration = " + String.valueOf(duration));



                        setSongDuration((int) duration);
                        //setActionBarTitle((int)duration);

                        if (isPlayPress) { //play is pressed, state: pause -> start
                            if (mediaOperation.getCurrent_state() == Constants.STATE.Paused) {
                                //if (audioOperation.isPause()) {
                                mediaOperation.setSeekTo((int) duration);
                                mediaOperation.doPlay(songList.get(song_selected).getPath());

                                //audioOperation.setCurrentPosition(duration/1000.0);
                                //audioOperation.doPlay(songList.get(song_selected).getPath());
                            } else {
                                mediaOperation.doPlay(songList.get(song_selected).getPath());
                                //audioOperation.doPlay(songList.get(song_selected).getPath());
                            }
                        } else {
                            current_position = (int) duration;
                            //current_position_d = duration/1000.0;
                            mediaOperation.setCurrentPosition(current_position);
                            //audioOperation.setCurrentPosition(current_position_d);
                        }


                    } else {
                        Log.e(TAG, "current_song_duration = 0");
                    }

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
                //is_seekBarTouch = false;
            }
        });

        markButtonA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (songList.size() > 0) {

                    if (current_song_duration != 0) {

                        if (is_editMarkA_change) {
                            String time[];
                            String secs[];

                            if (textA.getText().length() > 0) {

                                time = textA.getText().toString().split(":");

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

                                        if (textA.getText().length() < 9) {
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

                                            progress_mark_a = (duration * 1000) / current_song_duration;

                                            if (progress_mark_a < 1000) {
                                                seekBar.setDots(new int[]{progress_mark_a, progress_mark_b});
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
                            is_editMarkA_change = false;
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
                                songList.get(song_selected).setMark_a((int) duration);
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
                            if (songPlaying != song_selected) {
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
                                    songList.get(song_selected).setMark_a((int) duration);
                                }
                            }
                        }
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
                //is_seekBarTouch = false;
            }
        });

        markButtonB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (songList.size() > 0) {
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
                                                !isNumber(secs[1])) {
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
                            is_editMarkB_change = false;
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
                                songList.get(song_selected).setMark_b((int) duration);
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
                            if (songPlaying != song_selected) {
                                NumberFormat f = new DecimalFormat("00");
                                NumberFormat f2 = new DecimalFormat("000");

                                double per_unit = (double) current_song_duration / 1000.0;


                                double duration = seekBar.getProgress() * per_unit;

                                Log.e(TAG, "unit = " + String.valueOf(per_unit) + " duration = " + String.valueOf(duration));

                                int minutes = ((int) duration) / 60000;

                                int seconds = ((int) duration / 1000) % 60;

                                int minisec = (int) duration % 1000;


                                textA.setText(f.format(minutes) + ":" + f.format(seconds) + "." + f2.format(minisec));

                                progress_mark_b = seekBar.getProgress();
                                seekBar.setDots(new int[]{progress_mark_a, progress_mark_b});
                                seekBar.setDotsDrawable(R.drawable.dot);
                                seekBar.setmLine(R.drawable.line);

                                if (current_mode == MODE_PLAY_AB_LOOP) {
                                    songList.get(song_selected).setMark_b((int) duration);
                                }
                            }
                        }
                    }
                }
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (songList.size() > 0) {

                    seekBar.setDots(new int[]{});
                    seekBar.setDotsDrawable(R.drawable.dot);

                    progress_mark_a = 0;
                    String ta = "00:00.000";
                    textA.setText(ta);

                    progress_mark_b = 1000;
                    String tb = "00:00.000";
                    textB.setText(tb);

                    songList.get(song_selected).setMark_a(0);
                    songList.get(song_selected).setMark_b(current_song_duration);

                    mediaOperation.setAb_loop_start(0);
                    mediaOperation.setAb_loop_end(current_song_duration);
                }
            }
        });

        imgPlayOrPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (songList.size() > 0) { //check if songs exist in list
                    //set click disable
                    //imgPlayOrPause.setClickable(false);

                    if (mediaOperation.getCurrent_state() == Constants.STATE.Started) { //if playing, pause

                        Log.d(TAG, "[imgPlayOrPause] isPlaying, songPlaying = "+songPlaying);

                        mediaOperation.setTaskStop();

                        isPlayPress = false;

                        mediaOperation.doPause();
                        current_position = mediaOperation.getCurrentPosition();
                        Log.e(TAG, "===> current_position = "+current_position);

                    } else {
                        Log.d(TAG, "[imgPlayOrPause] state is paused, stopped...");

                        mediaOperation.setTaskStart();

                        String songPath, songName;
                        isPlayPress = true;



                        if (song_selected > 0) { //if selected, get selected
                            songPath = songArrayAdapter.getItem(song_selected).getPath();
                            songName = songArrayAdapter.getItem(song_selected).getName();

                            current_song_duration = (int)(songList.get(song_selected).getDuration_u()/1000);
                        } else { //else use first

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

                                current_song_duration = (int)(songList.get(song_selected).getDuration_u()/1000);
                            } else {

                                songPath = songList.get(0).getPath();
                                songName = songList.get(0).getName();
                                current_song_duration = (int)(songList.get(0).getDuration_u()/1000);
                            }
                        }





                        myListview.invalidateViews();

                        if (mediaOperation.getCurrent_state() == Constants.STATE.Paused) {
                            if (songPlaying == song_selected) {
                                Log.d(TAG, "The same song from pause to play");

                                if (current_mode == MODE_PLAY_AB_LOOP) {
                                    if (current_position >= songList.get(song_selected).getMark_b()) {
                                        current_position = songList.get(song_selected).getMark_a();
                                    }
                                    mediaOperation.setLooping(false);
                                } else if (current_mode == MODE_PLAY_REPEAT) {
                                    mediaOperation.setLooping(true);
                                } else {
                                    mediaOperation.setLooping(false);
                                }
                            } else {
                                Log.d(TAG, "The song was different from pause to play, stop!");
                                mediaOperation.doStop();

                                songPlaying = song_selected;


                                if (current_mode == MODE_PLAY_AB_LOOP) {
                                    current_position = songList.get(song_selected).getMark_a();
                                    mediaOperation.setAb_loop_start(songList.get(song_selected).getMark_a());
                                    mediaOperation.setAb_loop_end(songList.get(song_selected).getMark_b());
                                } else {
                                    current_position = 0;
                                }
                            }
                        } else { //not pause, maybe stop
                            Log.d(TAG, "not pause, maybe stop");

                            if (current_mode == MODE_PLAY_AB_LOOP) {
                                NumberFormat f = new DecimalFormat("00");
                                NumberFormat f2 = new DecimalFormat("000");

                                progress_mark_a = (int) ((float) songList.get(song_selected).getMark_a() / (float) current_song_duration * 1000.0);
                                progress_mark_b = (int) ((float) songList.get(song_selected).getMark_b() / (float) current_song_duration * 1000.0);

                                int minutes_a = songList.get(song_selected).getMark_a() / 60000;
                                int seconds_a = (songList.get(song_selected).getMark_a() / 1000) % 60;
                                int minisec_a = songList.get(song_selected).getMark_a() % 1000;

                                int minutes_b = songList.get(song_selected).getMark_b() / 60000;
                                int seconds_b = (songList.get(song_selected).getMark_b() / 1000) % 60;
                                int minisec_b = songList.get(song_selected).getMark_b() % 1000;

                                seekBar.setDots(new int[]{progress_mark_a, progress_mark_b});
                                seekBar.setDotsDrawable(R.drawable.dot);
                                seekBar.setmLine(R.drawable.line);

                                textA.setText(f.format(minutes_a) + ":" + f.format(seconds_a) + "." + f2.format(minisec_a));
                                textB.setText(f.format(minutes_b) + ":" + f.format(seconds_b) + "." + f2.format(minisec_b));

                                current_position = songList.get(song_selected).getMark_a();
                                mediaOperation.setAb_loop_start(songList.get(song_selected).getMark_a());
                                mediaOperation.setAb_loop_end(songList.get(song_selected).getMark_b());
                            }
                        }





                        Log.d(TAG, "play "+songName+" position = "+current_position);
                        //audioOperation.setCurrentPosition(current_position_d);
                        //audioOperation.doPlay(songPath);
                        mediaOperation.setCurrentPosition(current_position);
                        mediaOperation.doPlay(songPath);
                    }

                } else {
                    toast(getResources().getString(R.string.song_list_empty));
                }
            }
        });

        imgSkipPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "imgSkipPrev");

                if (songList.size() > 0) {

                    if (mediaOperation.getCurrent_state() == Constants.STATE.Started) { //playing
                        mediaOperation.doStop();
                        mediaOperation.doPrev();
                    } else {
                        if (song_selected > 0 && song_selected < songList.size()) { //song_selected must >= 1
                            song_selected--;
                        } else {
                            song_selected = 0;
                        }

                        //deselect other
                        for (int i = 0; i < songList.size(); i++) {

                            if (i == song_selected) {
                                songList.get(i).setSelected(true);

                            } else {
                                songList.get(i).setSelected(false);

                            }
                        }

                        myListview.invalidateViews();

                        current_song_duration = (int) (songList.get(song_selected).getDuration_u() / 1000);
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

                                int minutes_a = songList.get(song_selected).getMark_a() / 60000;
                                int seconds_a = (songList.get(song_selected).getMark_a() / 1000) % 60;
                                int minisec_a = songList.get(song_selected).getMark_a() % 1000;

                                int minutes_b = songList.get(song_selected).getMark_b() / 60000;
                                int seconds_b = (songList.get(song_selected).getMark_b() / 1000) % 60;
                                int minisec_b = songList.get(song_selected).getMark_b() % 1000;

                                seekBar.setDots(new int[]{progress_mark_a, progress_mark_b});
                                seekBar.setDotsDrawable(R.drawable.dot);
                                seekBar.setmLine(R.drawable.line);

                                textA.setText(f.format(minutes_a) + ":" + f.format(seconds_a) + "." + f2.format(minisec_a));
                                textB.setText(f.format(minutes_b) + ":" + f.format(seconds_b) + "." + f2.format(minisec_b));


                                break;
                        }

                    }
                }
            }
        });

        imgSkipNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "imgSkipNext");

                if (songList.size() > 0) {

                    if (mediaOperation.getCurrent_state() == Constants.STATE.Started) { //playing
                        mediaOperation.doStop();
                        mediaOperation.doNext();
                    } else {
                        if (song_selected < songList.size() - 1) { //song_selected must >= 1
                            song_selected++;
                        } else {
                            song_selected = songList.size() - 1;
                        }

                        //deselect other
                        for (int i = 0; i < songList.size(); i++) {

                            if (i == song_selected) {
                                songList.get(i).setSelected(true);

                            } else {
                                songList.get(i).setSelected(false);

                            }
                        }

                        myListview.invalidateViews();

                        current_song_duration = (int) (songList.get(song_selected).getDuration_u() / 1000);
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

                                int minutes_a = songList.get(song_selected).getMark_a() / 60000;
                                int seconds_a = (songList.get(song_selected).getMark_a() / 1000) % 60;
                                int minisec_a = songList.get(song_selected).getMark_a() % 1000;

                                int minutes_b = songList.get(song_selected).getMark_b() / 60000;
                                int seconds_b = (songList.get(song_selected).getMark_b() / 1000) % 60;
                                int minisec_b = songList.get(song_selected).getMark_b() % 1000;

                                seekBar.setDots(new int[]{progress_mark_a, progress_mark_b});
                                seekBar.setDotsDrawable(R.drawable.dot);
                                seekBar.setmLine(R.drawable.line);

                                textA.setText(f.format(minutes_a) + ":" + f.format(seconds_a) + "." + f2.format(minisec_a));
                                textB.setText(f.format(minutes_b) + ":" + f.format(seconds_b) + "." + f2.format(minisec_b));


                                break;
                        }

                    }
                }
            }
        });

        imgFastRewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "imgFastRewind");

                if (songList.size() > 0) {

                    Log.d(TAG, "current_position = " + mediaOperation.getCurrentPosition());

                    if (mediaOperation.getCurrent_state() == Constants.STATE.Started) { //if playing, pause, seek to new position then play
                        //if (!audioOperation.isPause()) {

                        mediaOperation.doPause();

                        current_position = mediaOperation.getCurrentPosition();
                        if (current_position > 10000) { //10 seconds
                            current_position = current_position - 10000;
                        } else {
                            current_position = 0;
                        }
                        mediaOperation.setCurrentPosition(current_position);
                        mediaOperation.setSeekTo(current_position);
                        mediaOperation.doPlay(songList.get(song_selected).getPath());
                    } else { //

                        current_position = mediaOperation.getCurrentPosition();

                        if (current_position > 10000) {
                            current_position = current_position - 10000;
                        } else {
                            current_position = 0;
                        }
                        mediaOperation.setCurrentPosition(current_position);

                        setSeekBarLocation(current_position);
                        setSongDuration(current_position);
                    }
                }
            }
        });

        imgFastForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "imgFastForward");

                if (songList.size() > 0) {

                    Log.d(TAG, "current_position = " + mediaOperation.getCurrentPosition());

                    if (!mediaOperation.isPause()) { //if playing, pause, seek to new position then play
                        mediaOperation.doPause();
                        //current_position = mediaOperation.getCurrentPosition()+10000;
                        //current_position = (int)(audioOperation.getCurrentPosition()*1000.0)+10000;

                        current_position = mediaOperation.getCurrentPosition() + 10000;

                        if (current_position >= current_song_duration) {
                            mediaOperation.setSeekTo(current_song_duration);
                            current_position = current_song_duration;
                        }
                        mediaOperation.setCurrentPosition(current_position);
                        mediaOperation.setSeekTo(current_position);
                        mediaOperation.doPlay(songList.get(song_selected).getPath());
                    } else {

                        current_position = mediaOperation.getCurrentPosition() + 10000;

                        if (current_position >= current_song_duration) {
                            mediaOperation.setSeekTo(current_song_duration);
                            current_position = current_song_duration;
                        }
                        mediaOperation.setCurrentPosition(current_position);

                        setSeekBarLocation(current_position);
                        setSongDuration(current_position);
                    }
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

                current_song_duration = (int)(songList.get(song_selected).getDuration_u()/1000);

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

                    if (songList.size() > 0) {

                        songArrayAdapter = new SongArrayAdapter(context, R.layout.music_list_item, songList);
                        myListview.setAdapter(songArrayAdapter);

                        if (loadDialog != null)
                            loadDialog.dismiss();
                        //set shuffle list
                        mediaOperation.shuffleReset();

                        NumberFormat f = new DecimalFormat("00");
                        NumberFormat f2 = new DecimalFormat("000");

                        switch (current_mode) {
                            case MODE_PLAY_ALL:
                                song_selected = 0;
                                current_song_duration = (int) (songList.get(song_selected).getDuration_u() / 1000);
                                break;
                            case MODE_PLAY_SHUFFLE:
                                song_selected = mediaOperation.getShufflePosition();
                                current_song_duration = (int) (songList.get(song_selected).getDuration_u() / 1000);
                                break;
                            case MODE_PLAY_REPEAT:
                                song_selected = 0;
                                current_song_duration = (int) (songList.get(song_selected).getDuration_u() / 1000);
                                break;
                            case MODE_PLAY_AB_LOOP:
                                song_selected = 0;
                                current_song_duration = (int) (songList.get(song_selected).getDuration_u() / 1000);

                                progress_mark_a = (int) ((float) songList.get(song_selected).getMark_a() / (float) current_song_duration * 1000.0);
                                progress_mark_b = (int) ((float) songList.get(song_selected).getMark_b() / (float) current_song_duration * 1000.0);

                                int minutes_a = songList.get(song_selected).getMark_a() / 60000;
                                int seconds_a = (songList.get(song_selected).getMark_a() / 1000) % 60;
                                int minisec_a = songList.get(song_selected).getMark_a() % 1000;

                                int minutes_b = songList.get(song_selected).getMark_b() / 60000;
                                int seconds_b = (songList.get(song_selected).getMark_b() / 1000) % 60;
                                int minisec_b = songList.get(song_selected).getMark_b() % 1000;

                                seekBar.setDots(new int[]{progress_mark_a, progress_mark_b});
                                seekBar.setDotsDrawable(R.drawable.dot);
                                seekBar.setmLine(R.drawable.line);

                                textA.setText(f.format(minutes_a) + ":" + f.format(seconds_a) + "." + f2.format(minisec_a));
                                textB.setText(f.format(minutes_b) + ":" + f.format(seconds_b) + "." + f2.format(minisec_b));
                                break;
                        }


                        //deselect other
                        for (int i = 0; i < songList.size(); i++) {

                            if (i == song_selected) {
                                songList.get(i).setSelected(true);

                            } else {
                                songList.get(i).setSelected(false);

                            }
                        }

                        //show item
                        if (item_remove != null) {
                            item_remove.setVisible(true);
                        }
                        if (item_clear != null) {
                            item_clear.setVisible(true);
                        }

                    } else {
                        if (loadDialog != null)
                            loadDialog.dismiss();

                        if (item_remove != null) {
                            item_remove.setVisible(false);
                        }
                        if (item_clear != null) {
                            item_clear.setVisible(false);
                        }

                        toast(getResources().getString(R.string.list_empty));
                    }



                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION.ADD_SONG_LIST_COMPLETE)) {
                    Log.d(TAG, "receive ADD_SONG_LIST_COMPLETE !");


                    for (int i=0; i<addSongList.size(); i++) {
                        songList.add(addSongList.get(i));
                        Log.d(TAG, "add "+addSongList.get(i).getName()+" to songList");
                    }

                    mediaOperation.shuffleReset();
                    mediaOperation.setShufflePosition(0);

                    if (songArrayAdapter == null) {
                        songArrayAdapter = new SongArrayAdapter(context, R.layout.music_list_item, songList);
                        myListview.setAdapter(songArrayAdapter);
                    } else {
                        Log.e(TAG, "notifyDataSetChanged");
                        songArrayAdapter.notifyDataSetChanged();
                    }



                    Intent saveintent = new Intent(context, SaveListToFileService.class);
                    saveintent.setAction(Constants.ACTION.SAVE_SONGLIST_ACTION);
                    saveintent.putExtra("FILENAME", "favorite");
                    context.startService(saveintent);

                    loadDialog = new ProgressDialog(context);
                    loadDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    loadDialog.setTitle("Saving...");
                    loadDialog.setIndeterminate(false);
                    loadDialog.setCancelable(false);

                    loadDialog.show();

                    //clear list

                    //show item
                    if (item_remove != null) {
                        item_remove.setVisible(true);
                    }
                    if (item_clear != null) {
                        item_clear.setVisible(true);
                    }


                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION.ADD_SONG_LIST_CHANGE)) {
                    if (songArrayAdapter != null) {
                        songArrayAdapter.notifyDataSetChanged();
                    }
                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION.GET_PLAY_COMPLETE)) {
                    Log.d(TAG, "receive GET_PLAY_COMPLETE !");
                    imgPlayOrPause.setImageResource(R.drawable.ic_play_circle_outline_black_48dp);
                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION.MEDIAPLAYER_STATE_PLAYED)) {
                    Log.d(TAG, "receive MEDIAPLAYER_STATE_STARTED !("+song_selected+")");
                    //set click true
                    //imgPlayOrPause.setClickable(true);

                    imgPlayOrPause.setImageResource(R.drawable.ic_pause_circle_outline_black_48dp);

                    myListview.smoothScrollToPosition(song_selected);

                    for (int i=0; i<songList.size(); i++) {


                        if (i == song_selected) {
                            songList.get(i).setSelected(true);

                        } else {
                            songList.get(i).setSelected(false);

                        }



                    }
                    myListview.invalidateViews();


                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION.MEDIAPLAYER_STATE_PAUSED)) {
                    Log.d(TAG, "receive MEDIAPLAYER_STATE_PAUSED !");
                    //imgPlayOrPause.setClickable(true);
                    imgPlayOrPause.setImageResource(R.drawable.ic_play_circle_outline_black_48dp);
                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION.SAVE_SONGLIST_TO_FILE_COMPLETE)) {
                    if (loadDialog != null)
                        loadDialog.dismiss();
                }
            }
        };


        if (!isRegister) {
            filter = new IntentFilter();
            filter.addAction(Constants.ACTION.ADD_SONG_LIST_COMPLETE);
            filter.addAction(Constants.ACTION.GET_PLAY_COMPLETE);
            filter.addAction(Constants.ACTION.GET_SONGLIST_FROM_RECORD_FILE_COMPLETE);
            filter.addAction(Constants.ACTION.SAVE_SONGLIST_TO_FILE_COMPLETE);
            filter.addAction(Constants.ACTION.MEDIAPLAYER_STATE_PLAYED);
            filter.addAction(Constants.ACTION.MEDIAPLAYER_STATE_PAUSED);
            context.registerReceiver(mReceiver, filter);
            isRegister = true;
            Log.d(TAG, "registerReceiver mReceiver");
        }

        if (songList.size() == 0 ) {
            loadSongs();
        } else {
            songArrayAdapter = new SongArrayAdapter(context, R.layout.music_list_item, songList);
            myListview.setAdapter(songArrayAdapter);
        }

        switch (current_mode) {
            case MODE_PLAY_ALL:
                if (linearLayoutAB != null)
                    linearLayoutAB.setVisibility(View.GONE);
                break;
            case MODE_PLAY_SHUFFLE:
                if (linearLayoutAB != null)
                    linearLayoutAB.setVisibility(View.GONE);
                break;

            case MODE_PLAY_REPEAT:
                if (linearLayoutAB != null)
                    linearLayoutAB.setVisibility(View.GONE);
                break;

            case MODE_PLAY_AB_LOOP:
                if (linearLayoutAB != null) {
                    linearLayoutAB.setVisibility(View.VISIBLE);

                    seekBar.setDots(new int[]{progress_mark_a, progress_mark_b});
                    seekBar.setDotsDrawable(R.drawable.dot);
                    seekBar.setmLine(R.drawable.line);
                }
                break;

        }

        return view;
    }

    public void loadSongs() {

        if (check_record_exist("favorite")) {
            Log.d(TAG, "load file success!");
            loadDialog = new ProgressDialog(context);
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

    public static void setSongDuration(int timeStamp) {

        NumberFormat f = new DecimalFormat("00");
        NumberFormat f2 = new DecimalFormat("000");


        int minutes = (timeStamp/60000);

        int seconds = (timeStamp/1000) % 60;

        int minisec = (timeStamp%1000);

        songDuration.setText(f.format(minutes)+":"+f.format(seconds)+"."+f2.format(minisec));
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

    @Override
    public void onDestroyView() {
        Log.i(TAG, "onDestroyView");

        if (isRegister && mReceiver != null) {
            try {
                context.unregisterReceiver(mReceiver);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            isRegister = false;
            mReceiver = null;
        }

        songArrayAdapter = null;

        super.onDestroyView();
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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

    }


}
