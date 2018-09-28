package com.seventhmoon.jamnow;


import android.Manifest;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.content.res.Configuration;

import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Build;

import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.content.ContextCompat;

import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.app.MediaRouteButton;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.support.v7.widget.SearchView;

import android.text.TextUtils;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;



import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.seventhmoon.jamnow.Data.Constants;
import com.seventhmoon.jamnow.Data.DottedSeekBar;


import com.seventhmoon.jamnow.Data.MediaOperation;
import com.seventhmoon.jamnow.Data.Song;
import com.seventhmoon.jamnow.Data.SongArrayAdapter;

import com.seventhmoon.jamnow.Data.VideoItem;
import com.seventhmoon.jamnow.Data.VideoItemArrayAdapter;

import com.seventhmoon.jamnow.Service.SaveListToFileService;
import com.seventhmoon.jamnow.Service.SaveRemoteFileAsLocalTemp;
import com.seventhmoon.jamnow.Service.SaveVideoListToFileService;


import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import java.util.ArrayList;

import java.util.HashMap;

import java.util.List;

import java.util.Map;


import static com.seventhmoon.jamnow.Data.FileOperation.getAudioInfo;
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

    MenuItem item_search, item_play_all, item_shuffle, item_single_repeat, item_ab_loop;
    public static ActionBar actionBar;
    public static LinearLayout linearLayoutAB;
    public static LinearLayout layout_seekbar_time;
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

    public static int current_position = 0;
    public static int current_video_position = 0;
    //private static float current_speed = 0;

    public static ProgressDialog loadDialog = null;

    //public static int currentSongPlay = 0;



    public static int songPlaying = 0;

    private static String currentAcitonBarTitle;
    public static boolean isPlayPress = false;

    private static AlertDialog dialog = null;

    public static MenuItem item_remove, item_clear, item_add, mediaRouteMenuItem;

    public static boolean isVideoPreview = false;

    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    private MediaRouter.Callback mMediaRouterCallback;
    private RemoteMediaPlayer mRemoteMediaPlayer;
    private RemoteMediaClient mRemoteMediaClient;
    public static CastDevice mSelectedDevice;
    //private GoogleApiClient mApiClient;
    private Cast.Listener mCastClientListener;
    private boolean mWaitingForReconnect = false;
    private boolean mApplicationStarted = false;
    private boolean mVideoIsLoaded;
    private boolean mAudioIsLoaded;
    private boolean mIsPlaying;

    String APP_ID = "F6D3E50B";

    public static final String EXTRA_START_FULLSCREEN = "com.seventhmoon.jamnow.EXTRA_START_FULLSCREEN";

    public static final String EXTRA_CURRENT_MEDIA_DESCRIPTION = "com.seventhmoon.jamnow.CURRENT_MEDIA_DESCRIPTION";
    private static final String SAVED_MEDIA_ID="com.seventhmoon.jamnow.MEDIA_ID";
    private Bundle mVoiceSearchParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        setContentView(R.layout.activity_main);

        //for chrome cast
        MediaRouteButton mMediaRouteButton = findViewById(R.id.media_route_button);
        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), mMediaRouteButton);

        //CastContext castContext = CastContext.getSharedInstance(this);

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

                default:
                    current_mode = MODE_PLAY_ALL;
                    actionBar.setHomeAsUpIndicator(R.drawable.ic_all_inclusive_white_48dp);
                    currentAcitonBarTitle = getResources().getString(R.string.play_mode_all);
                    actionBar.setTitle(currentAcitonBarTitle);
                    if (linearLayoutAB != null)
                        linearLayoutAB.setVisibility(View.GONE);
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

        //initMediaRouter();

        mMediaRouter = MediaRouter.getInstance(this);
        mMediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(CastMediaControlIntent.categoryForCast(APP_ID))
                .build();

        mMediaRouterCallback = new MediaRouter.Callback() {
            @Override
            public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo route) {
                super.onRouteSelected(router, route);
                Log.d(TAG, "Connected to "+ route.getName());

                //initCastClientListener();
                //initRemoteMediaPlayer();

                mSelectedDevice = CastDevice.getFromBundle(route.getExtras());
                Toast.makeText(getApplicationContext(), "Connected to "+route.getName(),Toast.LENGTH_LONG).show();


            }

            @Override
            public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo route) {
                super.onRouteUnselected(router, route);
                Log.d(TAG, "Disconnected to "+ route.getName());
                Toast.makeText(getApplicationContext(), "Disconnected to "+route.getName(),Toast.LENGTH_LONG).show();
                mSelectedDevice = null;
            }
        };

        IntentFilter filter;

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction() != null) {
                    if (intent.getAction().equalsIgnoreCase(Constants.ACTION.CHROME_CAST_AUDIO_ACTION)) {
                        Log.e(TAG, "receive CHROME_CAST_AUDIO_ACTION");

                        if( !mAudioIsLoaded ) {
                            Log.e(TAG, "startAudio");

                            String type = getMediaMime(songList.get(song_selected).getPath());
                            //Uri fileUri = Uri.fromFile(new File(songList.get(song_selected).getPath()));
                            String filePath = songList.get(song_selected).getPath();
                            String fileName = songList.get(song_selected).getName();

                            //startAudio(type, filePath, fileName);
                        } else {
                            Log.e(TAG, "controlAudio");

                        }


                    }
                }


            }
        };


        if (!isRegister) {
            filter = new IntentFilter();
            filter.addAction(Constants.ACTION.CHROME_CAST_AUDIO_ACTION);
            context.registerReceiver(mReceiver, filter);
            isRegister = true;
            Log.d(TAG, "registerReceiver mReceiver");
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

        super.onDestroy();

    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause");

        if ( isFinishing() ) {
            // End media router discovery
            mMediaRouter.removeCallback( mMediaRouterCallback );
        }

        super.onPause();
    }
    @Override
    public void onResume() {

        Log.i(TAG, "onResume");



        super.onResume();

        // Start media router discovery
        mMediaRouter.addCallback( mMediaRouteSelector, mMediaRouterCallback, MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN );
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
        //SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();



        if (searchManager != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

            item_search = menu.findItem(R.id.action_search);
            item_remove = menu.findItem(R.id.action_remove);
            item_clear = menu.findItem(R.id.action_clear);
            item_add = menu.findItem(R.id.action_add);

            item_play_all = menu.findItem(R.id.action_play_all);
            item_shuffle = menu.findItem(R.id.action_shuffle);
            item_single_repeat = menu.findItem(R.id.action_repeat);
            item_ab_loop = menu.findItem(R.id.action_loop);

            //mediaRouteMenuItem = menu.findItem( R.id.action_cast );
            //MediaRouteActionProvider mediaRouteActionProvider = (MediaRouteActionProvider) MenuItemCompat.getActionProvider( mediaRouteMenuItem );
            //mediaRouteActionProvider.setRouteSelector( mMediaRouteSelector );

            mediaRouteMenuItem = menu.findItem(R.id.action_cast);
            MediaRouteActionProvider provider = (MediaRouteActionProvider) MenuItemCompat.getActionProvider(mediaRouteMenuItem);
            provider.setRouteSelector(mMediaRouteSelector);
            //mediaRouteMenuItem.setVisible(true);
            //item_clear = menu.findItem(R.id.action_clear);

            item_search.setVisible(false);

            try {
                //SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search_keeper));
                searchView.setOnQueryTextListener(queryListener);
            }catch(Exception e){
                e.printStackTrace();
            }
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
                showInputDialog();


                //intent = new Intent(MainActivity.this, FileChooseActivity.class);
                //startActivity(intent);
                break;

            //case R.id.action_link:
            //    showInputDialog();

            //    break;


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
                    String msg_a = f.format(minutes_a) + ":" + f.format(seconds_a) + "." + f2.format(minisec_a);
                    String msg_b = f.format(minutes_b) + ":" + f.format(seconds_b) + "." + f2.format(minisec_b);

                    textA.setText(msg_a);
                    textB.setText(msg_b);
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
            case R.id.action_cast:
                Log.d(TAG, "action_cast");



                /*if (current_mode == MODE_PLAY_VIDEO) {
                    if( !mVideoIsLoaded ) {
                        Log.e(TAG, "startVideo");
                        startVideo();
                    } else {
                        Log.e(TAG, "controlVideo");
                        controlVideo();
                    }
                } else {
                    intent = new Intent(Constants.ACTION.CHROME_CAST_AUDIO_ACTION);
                    sendBroadcast(intent);
                    if( !mAudioIsLoaded ) {
                        Log.e(TAG, "startAudio");

                        String type = getMediaMime(songList.get(song_selected).getPath());
                        //Uri fileUri = Uri.fromFile(new File(songList.get(song_selected).getPath()));
                        String filePath = songList.get(song_selected).getPath();

                        startAudio(type, filePath);
                    } else {
                        Log.e(TAG, "controlAudio");
                        controlAudio();
                    }
                }*/


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

        final TextView textVolume = promptView.findViewById(R.id.textVolume);
        final SeekBar seekbarVolume = promptView.findViewById(R.id.seekBarVolume);

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

        int accessNetworkStatePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_NETWORK_STATE);

        int accessWiFiStatePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_WIFI_STATE);

        int readPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        int writePermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        int networkPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.INTERNET);

        //int cameraPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);

        List<String> listPermissionsNeeded = new ArrayList<>();

        if (readPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if (writePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (networkPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.INTERNET);
        }

        if (accessNetworkStatePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_NETWORK_STATE);
        }

        if (accessWiFiStatePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_WIFI_STATE);
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
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
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
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(android.Manifest.permission.INTERNET, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.ACCESS_NETWORK_STATE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.ACCESS_WIFI_STATE
                        , PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                        perms.get(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                            perms.get(Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED &&
                            perms.get(Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED &&
                            perms.get(Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED)

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
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            || ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.INTERNET ) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_NETWORK_STATE ) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_WIFI_STATE )
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
                                         int resid, int genresIcon) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.tab_item, null);
        v.setBackgroundResource(resid);
        //TextView tv = (TextView)v.findViewById(R.id.txt_tabtxt);
        ImageView img = v.findViewById(R.id.img_tabtxt);

        //tv.setText(string);
        img.setBackgroundResource(genresIcon);
        return spec.setIndicator(v);
    }

    private void InitView() {
        FragmentTabHost mTabHost;

        mTabHost = findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

        //mTabHost.addTab(setIndicator(MainMenu.this, mTabHost.newTabSpec(TAB_1_TAG),
        //        R.drawable.tab_indicator_gen, getResources().getString(R.string.scm_history_tab), R.drawable.ic_history_white_48dp), HistoryFragment.class, null);
        mTabHost.addTab(setIndicator(MainActivity.this, mTabHost.newTabSpec(TAB_1_TAG),
                R.drawable.tab_indicator_gen, R.drawable.ic_audiotrack_white_48dp), AudioFragment.class, null);




        //mTabHost.addTab(setIndicator(MainMenu.this, mTabHost.newTabSpec(TAB_2_TAG),
        //        R.drawable.tab_indicator_gen, getResources().getString(R.string.scm_setting), R.drawable.ic_settings_white_48dp), SettingsFragment.class, null);
        mTabHost.addTab(setIndicator(MainActivity.this, mTabHost.newTabSpec(TAB_2_TAG),
                R.drawable.tab_indicator_gen, R.drawable.ic_music_video_white_48dp), VideoFragment.class, null);






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

                        item_play_all.setVisible(true);
                        item_shuffle.setVisible(true);
                        item_single_repeat.setVisible(true);
                        item_ab_loop.setVisible(true);

                        break;
                    case "tab_2":
                        //if audio is playing, must stop
                        if (mediaOperation.getCurrent_state() == Constants.STATE.Started) { //if playing, pause

                            Log.d(TAG, "tab_2: audio isPlaying, songPlaying = "+songPlaying);

                            mediaOperation.setTaskStop();

                            isPlayPress = false;

                            mediaOperation.doPause();
                            current_position = mediaOperation.getCurrentPosition();
                            Log.e(TAG, "===> current_position = "+current_position);

                        }

                        previos_mode = current_mode;
                        current_mode = MODE_PLAY_VIDEO;
                        if (item_search != null)
                            item_search.setVisible(false);
                        actionBar.setHomeAsUpIndicator(R.drawable.ic_music_video_white_48dp);
                        currentAcitonBarTitle = getResources().getString(R.string.play_mode_video);
                        actionBar.setTitle(currentAcitonBarTitle);

                        item_play_all.setVisible(false);
                        item_shuffle.setVisible(false);
                        item_single_repeat.setVisible(false);
                        item_ab_loop.setVisible(false);
                        break;

                    default:
                        break;

                }
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.e(TAG, "landscape");
            layout_seekbar_time.setOrientation(LinearLayout.HORIZONTAL);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Log.e(TAG, "portrait");
            layout_seekbar_time.setOrientation(LinearLayout.VERTICAL);

        }
    }

    protected void showInputDialog() {

        // get prompts.xml view
        /*LayoutInflater layoutInflater = LayoutInflater.from(Nfc_read_app.this);
        View promptView = layoutInflater.inflate(R.layout.input_dialog, null);*/
        View promptView = View.inflate(MainActivity.this, R.layout.select_source_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setView(promptView);

        //final EditText editFileName = (EditText) promptView.findViewById(R.id.editFileName);
        //final EditText editUrlInput = promptView.findViewById(R.id.editUrlAddress);
        final RadioButton radioBtnLocal = promptView.findViewById(R.id.radioBtnLocal);
        final RadioButton radioBtnRemote = promptView.findViewById(R.id.radioBtnRemote);
        //final EditText editPlayerDown = promptView.findViewById(R.id.editResetPlayerDown);
        //if (playerUp != null)
        //    editPlayerUp.setText(playerUp);
        //if (playerDown != null)
        //    editPlayerDown.setText(playerDown);
        // setup a dialog window
        radioBtnLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioBtnRemote.setChecked(false);
            }
        });

        radioBtnRemote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioBtnLocal.setChecked(false);
            }
        });

        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //resultText.setText("Hello, " + editText.getText());
                //Log.e(TAG, "input password = " + editText.getText());

                if (radioBtnLocal.isChecked()) {
                    Log.d(TAG, "local checked");
                    Intent intent = new Intent(MainActivity.this, FileChooseActivity.class);
                    startActivity(intent);
                } else if (radioBtnRemote.isChecked()) {
                    Log.d(TAG, "remote checked");
                    Intent intent = new Intent(MainActivity.this, RemoteActivity.class);
                    startActivity(intent);
                } else {
                    Log.d(TAG, "no checked");
                }



            }
        });
        alertDialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDialogBuilder.show();
    }

    public String getMediaMime(String filePath) {
        Log.e(TAG, "<getAudioMime>");
        String infoMsg = null;
        //boolean hasFrameRate = false;

        MediaExtractor mex = new MediaExtractor();
        try {
            mex.setDataSource(filePath);// the adresss location of the sound on sdcard.
        } catch (IOException e) {

            e.printStackTrace();
        }



        File file = new File(filePath);
        Log.d(TAG, "file name: "+file.getName());

        if (mex != null) {

            try {
                MediaFormat mf = mex.getTrackFormat(0);
                Log.d(TAG, "file: "+file.getName()+" mf = "+mf.toString());
                infoMsg = mf.getString(MediaFormat.KEY_MIME);
                Log.d(TAG, "type: "+infoMsg);

                if (infoMsg.contains("audio")) {

                    /*Log.d(TAG, "duration(us): " + mf.getLong(MediaFormat.KEY_DURATION));
                    Log.d(TAG, "channel: " + mf.getInteger(MediaFormat.KEY_CHANNEL_COUNT));
                    if (mf.toString().contains("channel-mask")) {
                        Log.d(TAG, "channel mask: " + mf.getInteger(MediaFormat.KEY_CHANNEL_MASK));
                    }
                    if (mf.toString().contains("aac-profile")) {
                        Log.d(TAG, "aac profile: " + mf.getInteger(MediaFormat.KEY_AAC_PROFILE));
                    }

                    Log.d(TAG, "sample rate: " + mf.getInteger(MediaFormat.KEY_SAMPLE_RATE));

                    if (infoMsg != null) {
                        Song song = new Song();
                        song.setName(file.getName());
                        song.setPath(file.getAbsolutePath());
                        //song.setDuration((int)(mf.getLong(MediaFormat.KEY_DURATION)/1000));
                        song.setDuration_u(mf.getLong(MediaFormat.KEY_DURATION));
                        song.setChannel((byte) mf.getInteger(MediaFormat.KEY_CHANNEL_COUNT));
                        song.setSample_rate(mf.getInteger(MediaFormat.KEY_SAMPLE_RATE));
                        song.setMark_a(0);
                        song.setMark_b((int) (mf.getLong(MediaFormat.KEY_DURATION) / 1000));
                        addSongList.add(song);

                    }*/
                } else if (infoMsg.contains("video")) { //video
                    /*try {
                        Log.d(TAG, "frame rate : " + mf.getInteger(MediaFormat.KEY_FRAME_RATE));
                        hasFrameRate = true;
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                    Log.d(TAG, "height : " + mf.getInteger(MediaFormat.KEY_HEIGHT));
                    Log.d(TAG, "width : " + mf.getInteger(MediaFormat.KEY_WIDTH));
                    Log.d(TAG, "duration(us): " + mf.getLong(MediaFormat.KEY_DURATION));

                    if (infoMsg != null) {
                        VideoItem video = new VideoItem();
                        video.setName(file.getName());
                        video.setPath(file.getAbsolutePath());
                        if (hasFrameRate)
                            video.setFrame_rate(mf.getInteger(MediaFormat.KEY_FRAME_RATE));

                        video.setHeight(mf.getInteger(MediaFormat.KEY_HEIGHT));
                        video.setWidth(mf.getInteger(MediaFormat.KEY_WIDTH));
                        video.setDuration_u( mf.getLong(MediaFormat.KEY_DURATION));
                        video.setMark_a(0);
                        video.setMark_b((int) (mf.getLong(MediaFormat.KEY_DURATION) / 1000));
                        addVideoList.add(video);


                    }*/

                } else {
                    Log.e(TAG, "Unknown type");
                }

            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }

        } else {
            Log.d(TAG, "file: "+file.getName()+" not support");
        }

        Log.e(TAG, "</getAudioMime>");




        return infoMsg;
    }

    private void startAudio(String type, String filePath, String fileName) {



        Log.d(TAG, "startAudio cast");

        RemoteMediaClient remoteMediaClient = CastContext.getSharedInstance(this).getSessionManager().getCurrentCastSession().getRemoteMediaClient();

        MediaInfo mediaInfo = new MediaInfo.Builder(filePath)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType(type)
                .build();


        remoteMediaClient.load(mediaInfo, true, 0);

        /*MediaMetadata mediaMetadata = new MediaMetadata( MediaMetadata.MEDIA_TYPE_MOVIE );
        mediaMetadata.putString( MediaMetadata.KEY_TITLE, getString( R.string.video_title ) );

        MediaInfo mediaInfo = new MediaInfo.Builder( getString( R.string.video_url ) )
                .setContentType( getString( R.string.content_type_mp4 ) )
                .setStreamType( MediaInfo.STREAM_TYPE_BUFFERED )
                .setMetadata( mediaMetadata )
                .build();
        try {
            mRemoteMediaPlayer.load( mApiClient, mediaInfo, true )
                    .setResultCallback( new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
                        @Override
                        public void onResult( RemoteMediaPlayer.MediaChannelResult mediaChannelResult ) {
                            if( mediaChannelResult.getStatus().isSuccess() ) {
                                mVideoIsLoaded = true;
                                mButton.setText( getString( R.string.pause_video ) );
                            }
                        }
                    } );
        } catch( Exception e ) {
        }*/
    }

    private void controlAudio() {
        /*if( mRemoteMediaPlayer == null || !mVideoIsLoaded )
            return;

        if( mIsPlaying ) {
            mRemoteMediaPlayer.pause( mApiClient );
            mButton.setText( getString( R.string.resume_video ) );
        } else {
            mRemoteMediaPlayer.play( mApiClient );
            mButton.setText( getString( R.string.pause_video ) );
        }*/
    }
}
