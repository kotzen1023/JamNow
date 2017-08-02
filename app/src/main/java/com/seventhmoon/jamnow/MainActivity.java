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

import android.graphics.drawable.ColorDrawable;
import android.os.Build;

import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTabHost;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.seventhmoon.jamnow.Data.Constants;
import com.seventhmoon.jamnow.Data.DottedSeekBar;


import com.seventhmoon.jamnow.Data.MediaOperation;
import com.seventhmoon.jamnow.Data.Song;
import com.seventhmoon.jamnow.Data.SongArrayAdapter;

import com.seventhmoon.jamnow.Data.VideoItem;
import com.seventhmoon.jamnow.Data.VideoItemArrayAdapter;
import com.seventhmoon.jamnow.Service.GetSongListFromRecordService;
import com.seventhmoon.jamnow.Service.SaveListToFileService;
import com.seventhmoon.jamnow.Service.SaveVideoListToFileService;


import java.text.DecimalFormat;
import java.text.NumberFormat;

import java.util.ArrayList;

import java.util.HashMap;

import java.util.List;

import java.util.Map;



import static com.seventhmoon.jamnow.Data.FileOperation.check_record_exist;

import static com.seventhmoon.jamnow.Data.FileOperation.init_folder_and_files;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();

    private static final String TAB_1_TAG = "tab_1";
    private static final String TAB_2_TAG = "tab_2";

    public static final int MODE_PLAY_ALL = 0;
    public static final int MODE_PLAY_SHUFFLE = 1;
    public static final int MODE_PLAY_REPEAT = 2;
    public static final int MODE_PLAY_AB_LOOP = 3;
    public static final int MODE_PLAY_VIDEO = 4;

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;

    static SharedPreferences pref ;
    static SharedPreferences.Editor editor;
    private static final String FILE_NAME = "Preference";

    private Context context;
    //private ListView myListview;
    public static SongArrayAdapter songArrayAdapter;
    public static VideoItemArrayAdapter videoItemArrayAdapter;

    MenuItem item_search;
    public static ActionBar actionBar;
    public static LinearLayout linearLayoutAB;
    public static LinearLayout linearSpeed;
    public static TextView songDuration;
    public static DottedSeekBar seekBar;
    private static DottedSeekBar speedBar;
    //public static ImageView markButtonA, markButtonB;
    public static EditText textA, textB;
    //TextView textSpeed;
    //ImageView btnClear;

    //private ImageView imgPlayOrPause;
    //ImageView imgSkipPrev;
    //ImageView imgSkipNext;
    //ImageView imgFastRewind;
    //ImageView imgFastForward;

    //private MediaPlayer mediaPlayer;
    public static ArrayList<Song> songList = new ArrayList<>();
    //for add songs to list
    public static ArrayList<String> searchList = new ArrayList<>();
    public static ArrayList<Song> addSongList = new ArrayList<>();
    //Video
    public static ArrayList<VideoItem> videoList = new ArrayList<>();
    public static ArrayList<VideoItem> addVideoList = new ArrayList<>();

    private static BroadcastReceiver mReceiver = null;
    private static boolean isRegister = false;
    public static int song_selected = 0;
    public static int video_selected = 0;
    public static int current_mode = MODE_PLAY_ALL;
    public static int previos_mode = MODE_PLAY_ALL;
    public static int current_volume = 50;
    public static MediaOperation mediaOperation;
    //AudioOperation audioOperation;
    public static int current_song_duration = 0;
    public static int current_video_duration = 0;

    public static int progress_mark_a = 0;
    public static int progress_mark_b = 1000;
    //private DateFormat formatter;
    //private static boolean is_seekBarTouch = false;
    //private static boolean is_editMarkA_change = false;
    //private static boolean is_editMarkB_change = false;

    //private static int current_position = 0;
    private static float current_speed = 0;
    //private static double current_position_d = 0.0;
    public static ProgressDialog loadDialog = null;

    //public static int currentSongPlay = 0;



    public static int songPlaying = 0;

    private static String currentAcitonBarTitle;
    public static boolean isPlayPress = false;

    private static AlertDialog dialog = null;

    public static MenuItem item_remove, item_clear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        setContentView(R.layout.activity_main);


        context = getBaseContext();

        pref = getSharedPreferences(FILE_NAME, MODE_PRIVATE);
        current_mode = pref.getInt("PLAY_MODE", 0);
        current_volume = pref.getInt("PLAY_VOLUME", 50);



        mediaOperation = new MediaOperation(context);
        //audioOperation = new AudioOperation(context);


        mediaOperation.setCurrent_play_mode(current_mode);
        mediaOperation.setCurrent_volume(current_volume);

        if (current_mode == MODE_PLAY_REPEAT) {
            mediaOperation.setLooping(true);
        } else {
            mediaOperation.setLooping(false);
        }
        //audioOperation.setCurrent_play_mode(current_mode);

        //formatter = new SimpleDateFormat("mm:ss");

        songList.clear();



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
                    if (linearLayoutAB != null)
                        linearLayoutAB.setVisibility(View.GONE);
                    break;
                case MODE_PLAY_SHUFFLE:
                    actionBar.setHomeAsUpIndicator(R.drawable.ic_shuffle_white_48dp);
                    currentAcitonBarTitle = getResources().getString(R.string.play_mode_shuffle);
                    actionBar.setTitle(currentAcitonBarTitle);
                    if (linearLayoutAB != null)
                        linearLayoutAB.setVisibility(View.GONE);
                    break;

                case MODE_PLAY_REPEAT:
                    actionBar.setHomeAsUpIndicator(R.drawable.ic_repeat_white_48dp);
                    currentAcitonBarTitle = getResources().getString(R.string.play_mode_repeat);
                    actionBar.setTitle(currentAcitonBarTitle);
                    if (linearLayoutAB != null)
                        linearLayoutAB.setVisibility(View.GONE);
                    break;

                case MODE_PLAY_AB_LOOP:
                    actionBar.setHomeAsUpIndicator(R.drawable.ic_loop_white_48dp);
                    currentAcitonBarTitle = getResources().getString(R.string.play_mode_ab_loop);
                    actionBar.setTitle(currentAcitonBarTitle);
                    if (linearLayoutAB != null)
                        linearLayoutAB.setVisibility(View.VISIBLE);
                    break;

                case MODE_PLAY_VIDEO:
                    actionBar.setHomeAsUpIndicator(R.drawable.ic_music_video_white_48dp);
                    currentAcitonBarTitle = getResources().getString(R.string.play_mode_video);
                    actionBar.setTitle(currentAcitonBarTitle);
                    break;

            }
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            //init_folder_and_files();
            //init_setting();
            init_folder_and_files();
            //loadSongs();

        } else {
            if(checkAndRequestPermissions()) {
                // carry on the normal flow, as the case of  permissions  granted.

                init_folder_and_files();
                //loadSongs();
            }
        }

        InitView();

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
        item_remove = menu.findItem(R.id.action_remove);
        item_clear = menu.findItem(R.id.action_clear);

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
        String ta, tb;

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
                mediaOperation.setCurrent_play_mode(current_mode);

                //set loop = false
                mediaOperation.setLooping(false);

                //clear loop
                seekBar.setDots(new int[] {});
                seekBar.setDotsDrawable(R.drawable.dot);

                progress_mark_a = 0;
                ta = "00:00.000";
                textA.setText(ta);

                progress_mark_b = 1000;
                tb = "00:00.000";
                textB.setText(tb);
                break;
            case R.id.action_shuffle:
                actionBar.setHomeAsUpIndicator(R.drawable.ic_shuffle_white_48dp);
                currentAcitonBarTitle = getResources().getString(R.string.play_mode_shuffle);
                actionBar.setTitle(currentAcitonBarTitle);
                linearLayoutAB.setVisibility(View.GONE);
                current_mode = MODE_PLAY_SHUFFLE;
                mediaOperation.setCurrent_play_mode(current_mode);

                //set loop = false
                mediaOperation.setLooping(false);

                //clear loop
                seekBar.setDots(new int[] {});
                seekBar.setDotsDrawable(R.drawable.dot);

                progress_mark_a = 0;
                ta = "00:00.000";
                textA.setText(ta);

                progress_mark_b = 1000;
                tb = "00:00.000";
                textB.setText(tb);
                break;

            case R.id.action_repeat:
                actionBar.setHomeAsUpIndicator(R.drawable.ic_repeat_white_48dp);
                currentAcitonBarTitle = getResources().getString(R.string.play_mode_repeat);
                actionBar.setTitle(currentAcitonBarTitle);
                linearLayoutAB.setVisibility(View.GONE);
                current_mode = MODE_PLAY_REPEAT;
                mediaOperation.setCurrent_play_mode(current_mode);

                //set loop = true
                mediaOperation.setLooping(true);

                //clear loop
                seekBar.setDots(new int[] {});
                seekBar.setDotsDrawable(R.drawable.dot);

                progress_mark_a = 0;
                ta = "00:00.000";
                textA.setText(ta);

                progress_mark_b = 1000;
                tb = "00:00.000";
                textB.setText(tb);
                break;

            case R.id.action_loop:


                Log.e(TAG, "song_selected = "+song_selected);

                actionBar.setHomeAsUpIndicator(R.drawable.ic_loop_white_48dp);
                currentAcitonBarTitle = getResources().getString(R.string.play_mode_ab_loop);
                actionBar.setTitle(currentAcitonBarTitle);
                linearLayoutAB.setVisibility(View.VISIBLE);
                current_mode = MODE_PLAY_AB_LOOP;
                mediaOperation.setCurrent_play_mode(current_mode);

                if (songList.size() > 0) {

                    //set loop = false
                    mediaOperation.setLooping(false);

                    int minutes_a = 0;
                    int seconds_a = 0;
                    int minisec_a = 0;
                    int minutes_b = 0;
                    int seconds_b = 0;
                    int minisec_b = 0;

                    if (current_song_duration > 0) {
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

                    textA.setText(f.format(minutes_a) + ":" + f.format(seconds_a) + "." + f2.format(minisec_a));
                    textB.setText(f.format(minutes_b) + ":" + f.format(seconds_b) + "." + f2.format(minisec_b));
                }
                break;

            case R.id.action_volume:
                showVolumeDialog();
                break;
            case R.id.action_remove:
                if (current_mode == MODE_PLAY_VIDEO) { // video mode
                    if (videoList.size() > 0) {
                        Log.e(TAG, "remove song_selected = " + song_selected);

                        AlertDialog.Builder confirmdialog = new AlertDialog.Builder(this);
                        confirmdialog.setIcon(R.drawable.ic_warning_black_48dp);
                        confirmdialog.setTitle(getResources().getString(R.string.remove_select));
                        confirmdialog.setMessage(getResources().getString(R.string.remove_file_from_list, videoList.get(video_selected).getName()));
                        confirmdialog.setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                /*if (mediaOperation.getCurrent_state() == Constants.STATE.Started) {
                                    if (songPlaying == song_selected) {
                                        mediaOperation.doStop();
                                    }
                                }*/


                                videoList.remove(video_selected);

                                videoItemArrayAdapter.notifyDataSetChanged();

                                //reset shuffle
                                //mediaOperation.shuffleReset();

                                Intent saveintent = new Intent(MainActivity.this, SaveVideoListToFileService.class);
                                saveintent.setAction(Constants.ACTION.SAVE_VIDEOLIST_ACTION);
                                saveintent.putExtra("FILENAME", "video_favorite");
                                context.startService(saveintent);

                                loadDialog = new ProgressDialog(MainActivity.this);
                                loadDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                loadDialog.setTitle("Saving...");
                                loadDialog.setIndeterminate(false);
                                loadDialog.setCancelable(false);

                                loadDialog.show();

                                if (videoList.size() == 0) {
                                    //show item
                                    if (item_remove != null) {
                                        item_remove.setVisible(false);
                                    }
                                    if (item_clear != null) {
                                        item_clear.setVisible(false);
                                    }

                                    //clear loop
                                    /*seekBar.setDots(new int[] {});
                                    seekBar.setDotsDrawable(R.drawable.dot);

                                    String zero = "00:00.000";
                                    progress_mark_a = 0;

                                    textA.setText(zero);

                                    progress_mark_b = 1000;

                                    textB.setText(zero);

                                    songDuration.setText(zero);*/


                                }
                                //reset song_selected
                                video_selected = 0;
                                //mediaOperation.setShufflePosition(0);
                            }
                        });
                        confirmdialog.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {


                            }
                        });
                        confirmdialog.show();
                    }
                } else {
                    if (songList.size() > 0) {

                        Log.e(TAG, "remove song_selected = " + song_selected);

                        AlertDialog.Builder confirmdialog = new AlertDialog.Builder(this);
                        confirmdialog.setIcon(R.drawable.ic_warning_black_48dp);
                        confirmdialog.setTitle(getResources().getString(R.string.remove_select));
                        confirmdialog.setMessage(getResources().getString(R.string.remove_file_from_list, songList.get(song_selected).getName()));
                        confirmdialog.setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                if (mediaOperation.getCurrent_state() == Constants.STATE.Started) {
                                    if (songPlaying == song_selected) {
                                        mediaOperation.doStop();
                                    }
                                }


                                songList.remove(song_selected);

                                songArrayAdapter.notifyDataSetChanged();

                                //reset shuffle
                                mediaOperation.shuffleReset();

                                Intent saveintent = new Intent(MainActivity.this, SaveListToFileService.class);
                                saveintent.setAction(Constants.ACTION.SAVE_SONGLIST_ACTION);
                                saveintent.putExtra("FILENAME", "favorite");
                                context.startService(saveintent);

                                loadDialog = new ProgressDialog(MainActivity.this);
                                loadDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                loadDialog.setTitle("Saving...");
                                loadDialog.setIndeterminate(false);
                                loadDialog.setCancelable(false);

                                loadDialog.show();

                                if (songList.size() == 0) {
                                    //show item
                                    if (item_remove != null) {
                                        item_remove.setVisible(false);
                                    }
                                    if (item_clear != null) {
                                        item_clear.setVisible(false);
                                    }

                                    //clear loop
                                    seekBar.setDots(new int[] {});
                                    seekBar.setDotsDrawable(R.drawable.dot);

                                    String zero = "00:00.000";
                                    progress_mark_a = 0;

                                    textA.setText(zero);

                                    progress_mark_b = 1000;

                                    textB.setText(zero);

                                    songDuration.setText(zero);


                                }
                                //reset song_selected
                                song_selected = 0;
                                mediaOperation.setShufflePosition(0);
                            }
                        });
                        confirmdialog.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {


                            }
                        });
                        confirmdialog.show();
                    }
                }



                break;
            case R.id.action_clear:
                Log.e(TAG, "clear all");

                if (current_mode == MODE_PLAY_VIDEO) { // video mode
                    if (videoList.size() > 0) {
                        AlertDialog.Builder cleardialog = new AlertDialog.Builder(this);
                        cleardialog.setIcon(R.drawable.ic_warning_black_48dp);
                        cleardialog.setTitle(getResources().getString(R.string.clear_all));
                        cleardialog.setMessage(getResources().getString(R.string.clear_list_all));
                        cleardialog.setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                //if (mediaOperation.getCurrent_state() == Constants.STATE.Started) {
                                //    mediaOperation.doStop();
                                //}

                                videoList.clear();

                                videoItemArrayAdapter.notifyDataSetChanged();

                                Intent saveintent = new Intent(MainActivity.this, SaveVideoListToFileService.class);
                                saveintent.setAction(Constants.ACTION.SAVE_VIDEOLIST_ACTION);
                                saveintent.putExtra("FILENAME", "video_favorite");
                                context.startService(saveintent);

                                loadDialog = new ProgressDialog(MainActivity.this);
                                loadDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                loadDialog.setTitle("Saving...");
                                loadDialog.setIndeterminate(false);
                                loadDialog.setCancelable(false);

                                loadDialog.show();

                                if (videoList.size() == 0) {
                                    //show item
                                    if (item_remove != null) {
                                        item_remove.setVisible(false);
                                    }
                                    if (item_clear != null) {
                                        item_clear.setVisible(false);
                                    }
                                }
                                /*if (songList.size() == 0) {
                                    //show item
                                    if (item_remove != null) {
                                        item_remove.setVisible(false);
                                    }
                                    if (item_clear != null) {
                                        item_clear.setVisible(false);
                                    }

                                    //clear loop
                                    seekBar.setDots(new int[]{});
                                    seekBar.setDotsDrawable(R.drawable.dot);

                                    String zero = "00:00.000";
                                    progress_mark_a = 0;

                                    textA.setText(zero);

                                    progress_mark_b = 1000;

                                    textB.setText(zero);

                                    songDuration.setText(zero);

                                    current_song_duration = 0;

                                    song_selected = 0;
                                }

                                //reset song_selected
                                song_selected = 0;
                                mediaOperation.setShufflePosition(0);*/
                            }
                        });
                        cleardialog.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {


                            }
                        });
                        cleardialog.show();
                    }
                } else { //audio

                    if (songList.size() > 0) {

                        AlertDialog.Builder cleardialog = new AlertDialog.Builder(this);
                        cleardialog.setIcon(R.drawable.ic_warning_black_48dp);
                        cleardialog.setTitle(getResources().getString(R.string.clear_all));
                        cleardialog.setMessage(getResources().getString(R.string.clear_list_all));
                        cleardialog.setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                if (mediaOperation.getCurrent_state() == Constants.STATE.Started) {
                                    mediaOperation.doStop();
                                }

                                songList.clear();

                                songArrayAdapter.notifyDataSetChanged();

                                Intent saveintent = new Intent(MainActivity.this, SaveListToFileService.class);
                                saveintent.setAction(Constants.ACTION.SAVE_SONGLIST_ACTION);
                                saveintent.putExtra("FILENAME", "favorite");
                                context.startService(saveintent);

                                loadDialog = new ProgressDialog(MainActivity.this);
                                loadDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                loadDialog.setTitle("Saving...");
                                loadDialog.setIndeterminate(false);
                                loadDialog.setCancelable(false);

                                loadDialog.show();

                                if (songList.size() == 0) {
                                    //show item
                                    if (item_remove != null) {
                                        item_remove.setVisible(false);
                                    }
                                    if (item_clear != null) {
                                        item_clear.setVisible(false);
                                    }

                                    //clear loop
                                    seekBar.setDots(new int[]{});
                                    seekBar.setDotsDrawable(R.drawable.dot);

                                    String zero = "00:00.000";
                                    progress_mark_a = 0;

                                    textA.setText(zero);

                                    progress_mark_b = 1000;

                                    textB.setText(zero);

                                    songDuration.setText(zero);

                                    current_song_duration = 0;

                                    song_selected = 0;
                                }

                                //reset song_selected
                                song_selected = 0;
                                mediaOperation.setShufflePosition(0);
                            }
                        });
                        cleardialog.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {


                            }
                        });
                        cleardialog.show();
                    }
                }
                break;

        }

        editor = pref.edit();
        editor.putInt("PLAY_MODE", current_mode);
        editor.apply();

        return true;
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
        View promptView = View.inflate(MainActivity.this, R.layout.volume_dialog, null);

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
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

                if (isPlayPress) {

                    if (mediaOperation.getCurrent_state() == Constants.STATE.Paused) {
                        //if (audioOperation.isPause()) {
                        //mediaOperation.setSeekTo((int) duration);

                        mediaOperation.doPlay(songList.get(song_selected).getPath());

                        //audioOperation.setCurrentPosition(duration/1000.0);
                        //audioOperation.doPlay(songList.get(song_selected).getPath());
                    } else {
                        Log.e(TAG, "Not Pause state");
                    }
                }

                editor = pref.edit();
                editor.putInt("PLAY_VOLUME", current_volume);
                editor.apply();

                Message msg = new Message();
                mHandler.sendMessage(msg);
            }
        });

        // setup a dialog window
        alertDialogBuilder.setCancelable(false);


        /*alertDialogBuilder.setPositiveButton(getResources().getString(R.string.dialog_confirm), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //resultText.setText("Hello, " + editText.getText());
                //Log.e(TAG, "input password = " + editText.getText());

                if (editFileName.getText().toString().equals("")) {
                    toast("file name empty");

                } else {
                    //check same file name
                    if (check_file_exist(editFileName.getText().toString()))
                    {
                        AlertDialog.Builder confirmdialog = new AlertDialog.Builder(PlayMainActivity.this);
                        confirmdialog.setTitle("File "+"\""+editFileName.getText().toString()+"\" is exist, want to overwrite it?");
                        confirmdialog.setIcon(R.drawable.ball_icon);

                        confirmdialog.setCancelable(false);
                        confirmdialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //overwrite
                                //clear
                                clear_record(editFileName.getText().toString());

                                //String msg = editPlayerUp.getText().toString() + ";" + editPlayerDown.getText().toString() + "|";
                                //append_record(msg, editFileName.getText().toString());

                                Intent intent = new Intent(PlayMainActivity.this, SetupMain.class);
                                intent.putExtra("FILE_NAME", editFileName.getText().toString());
                                if (!editPlayerUp.getText().toString().equals(""))
                                    intent.putExtra("PLAYER_UP", editPlayerUp.getText().toString());
                                else
                                    intent.putExtra("PLAYER_UP", "");
                                if (!editPlayerDown.getText().toString().equals(""))
                                    intent.putExtra("PLAYER_DOWN", editPlayerDown.getText().toString());
                                else
                                    intent.putExtra("PLAYER_DOWN", "");
                                startActivity(intent);
                                finish();
                            }
                        });
                        confirmdialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {


                            }
                        });
                        confirmdialog.show();
                    } else {

                        //add new file
                        //String msg = editPlayerUp.getText().toString() + ";" + editPlayerDown.getText().toString() + "|";
                        //append_record(msg, editFileName.getText().toString());


                        Intent intent = new Intent(PlayMainActivity.this, SetupMain.class);
                        intent.putExtra("FILE_NAME", editFileName.getText().toString());
                        if (!editPlayerUp.getText().toString().equals(""))
                            intent.putExtra("PLAYER_UP", editPlayerUp.getText().toString());
                        else
                            intent.putExtra("PLAYER_UP", "Player1");
                        if (!editPlayerDown.getText().toString().equals(""))
                            intent.putExtra("PLAYER_DOWN", editPlayerDown.getText().toString());
                        else
                            intent.putExtra("PLAYER_DOWN", "Player2");
                        startActivity(intent);
                        finish();
                    }
                }
            }
        });
        alertDialogBuilder.setNegativeButton(getResources().getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });*/
        dialog = alertDialogBuilder.show();
    }

    final private android.support.v7.widget.SearchView.OnQueryTextListener queryListener = new android.support.v7.widget.SearchView.OnQueryTextListener() {
        //searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            /*Intent intent;


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


    /*public void toast(String message) {
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


    }*/

    private TabHost.TabSpec setIndicator(Context ctx, TabHost.TabSpec spec,
                                         int resid, String string, int genresIcon) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.tab_item, null);
        v.setBackgroundResource(resid);
        //TextView tv = (TextView)v.findViewById(R.id.txt_tabtxt);
        ImageView img = (ImageView)v.findViewById(R.id.img_tabtxt);

        //tv.setText(string);
        img.setBackgroundResource(genresIcon);
        return spec.setIndicator(v);
    }

    private void InitView() {
        FragmentTabHost mTabHost;

        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

        //mTabHost.addTab(setIndicator(MainMenu.this, mTabHost.newTabSpec(TAB_1_TAG),
        //        R.drawable.tab_indicator_gen, getResources().getString(R.string.scm_history_tab), R.drawable.ic_history_white_48dp), HistoryFragment.class, null);
        mTabHost.addTab(setIndicator(MainActivity.this, mTabHost.newTabSpec(TAB_1_TAG),
                R.drawable.tab_indicator_gen, "", R.drawable.ic_audiotrack_white_48dp), AudioFragment.class, null);




        //mTabHost.addTab(setIndicator(MainMenu.this, mTabHost.newTabSpec(TAB_2_TAG),
        //        R.drawable.tab_indicator_gen, getResources().getString(R.string.scm_setting), R.drawable.ic_settings_white_48dp), SettingsFragment.class, null);
        mTabHost.addTab(setIndicator(MainActivity.this, mTabHost.newTabSpec(TAB_2_TAG),
                R.drawable.tab_indicator_gen, "", R.drawable.ic_music_video_white_48dp), VideoFragment.class, null);






        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {


                switch (tabId) {
                    case "tab_1":
                        //if (item_clear != null)
                        //    item_clear.setVisible(true);
                        current_mode = previos_mode;

                        if (item_search != null)
                            item_search.setVisible(false);

                        switch (current_mode) {
                            case MODE_PLAY_ALL:
                                actionBar.setHomeAsUpIndicator(R.drawable.ic_all_inclusive_white_48dp);
                                currentAcitonBarTitle = getResources().getString(R.string.play_mode_all);
                                actionBar.setTitle(currentAcitonBarTitle);
                                if (linearLayoutAB != null)
                                    linearLayoutAB.setVisibility(View.GONE);
                                break;
                            case MODE_PLAY_SHUFFLE:
                                actionBar.setHomeAsUpIndicator(R.drawable.ic_shuffle_white_48dp);
                                currentAcitonBarTitle = getResources().getString(R.string.play_mode_shuffle);
                                actionBar.setTitle(currentAcitonBarTitle);
                                if (linearLayoutAB != null)
                                    linearLayoutAB.setVisibility(View.GONE);
                                break;

                            case MODE_PLAY_REPEAT:
                                actionBar.setHomeAsUpIndicator(R.drawable.ic_repeat_white_48dp);
                                currentAcitonBarTitle = getResources().getString(R.string.play_mode_repeat);
                                actionBar.setTitle(currentAcitonBarTitle);
                                if (linearLayoutAB != null)
                                    linearLayoutAB.setVisibility(View.GONE);
                                break;

                            case MODE_PLAY_AB_LOOP:
                                actionBar.setHomeAsUpIndicator(R.drawable.ic_loop_white_48dp);
                                currentAcitonBarTitle = getResources().getString(R.string.play_mode_ab_loop);
                                actionBar.setTitle(currentAcitonBarTitle);
                                if (linearLayoutAB != null)
                                    linearLayoutAB.setVisibility(View.VISIBLE);
                                break;

                        }

                        break;
                    case "tab_2":
                        //if (item_clear != null)
                        //    item_clear.setVisible(false);
                        previos_mode = current_mode;
                        current_mode = MODE_PLAY_VIDEO;
                        if (item_search != null)
                            item_search.setVisible(false);
                        actionBar.setHomeAsUpIndicator(R.drawable.ic_music_video_white_48dp);
                        currentAcitonBarTitle = getResources().getString(R.string.play_mode_video);
                        actionBar.setTitle(currentAcitonBarTitle);
                        break;

                    default:
                        break;

                }
            }
        });
    }
}
