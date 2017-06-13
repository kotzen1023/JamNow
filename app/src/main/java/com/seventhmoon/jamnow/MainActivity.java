package com.seventhmoon.jamnow;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.seventhmoon.jamnow.Data.Constants;
import com.seventhmoon.jamnow.Data.DottedSeekBar;
import com.seventhmoon.jamnow.Data.FileChooseArrayAdapter;
import com.seventhmoon.jamnow.Data.MediaOperation;
import com.seventhmoon.jamnow.Data.Song;
import com.seventhmoon.jamnow.Data.SongArrayAdapter;

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

import static com.seventhmoon.jamnow.Data.FileOperation.init_folder_and_files;
import static com.seventhmoon.jamnow.MainActivity.seekBar;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();

    private static final int MODE_PLAY_ALL = 0;
    private static final int MODE_PLAY_SHUFFLE = 1;
    private static final int MODE_PLAY_REPEAT = 2;
    private static final int MODE_PLAY_AB_LOOP = 3;

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;



    private Context context;
    public static ListView myListview;
    SongArrayAdapter songArrayAdapter;

    MenuItem item_search;
    ActionBar actionBar;
    LinearLayout linearLayoutAB;
    public static TextView songDuration;
    public static DottedSeekBar seekBar;
    Button markButtonA, markButtonB;
    EditText textA, textB;
    Button btnClear;

    public static ImageView imgPlayOrPause;
    ImageView imgSkipPrev;
    ImageView imgSkipNext;
    ImageView imgFastRewind;
    ImageView imgFastForward;

    private MediaPlayer mediaPlayer;
    public static ArrayList<Song> songList = new ArrayList<>();
    private int index = 0;
    private boolean isPause = true;

    private static BroadcastReceiver mReceiver = null;
    private static boolean isRegister = false;
    public static int song_selected = 0;
    public static int current_mode = MODE_PLAY_ALL;
    MediaOperation mediaOperation;
    private static int current_duration = 0;

    //private DateFormat formatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        setContentView(R.layout.activity_main);

        context = getBaseContext();

        mediaOperation = new MediaOperation(context);

        //formatter = new SimpleDateFormat("mm:ss");

        songList.clear();

        linearLayoutAB = (LinearLayout) findViewById(R.id.layout_ab_loop);

        songDuration = (TextView) findViewById(R.id.textSongDuration);

        seekBar = (DottedSeekBar) findViewById(R.id.seekBarTime);
        textA = (EditText) findViewById(R.id.textViewA);
        textB = (EditText) findViewById(R.id.textViewB);

        markButtonA = (Button) findViewById(R.id.btnMarkA);
        markButtonB = (Button) findViewById(R.id.btnMarkB);
        btnClear = (Button) findViewById(R.id.btnClear);

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
            actionBar.setHomeAsUpIndicator(R.drawable.ic_play_arrow_white_48dp);
            actionBar.setTitle("All");
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            //init_folder_and_files();
            //init_setting();
            init_folder_and_files();
        } else {
            if(checkAndRequestPermissions()) {
                // carry on the normal flow, as the case of  permissions  granted.

                init_folder_and_files();
                //init_setting();
            }
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Log.e(TAG, "onStartTrackingTouch");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Log.e(TAG, "onStopTrackingTouch "+seekBar.getProgress()+" current_duration = "+current_duration);



                if (current_duration != 0) {

                    NumberFormat f = new DecimalFormat("00");
                    int per_unit = current_duration / 100;
                    int duration = seekBar.getProgress() * per_unit;

                    int minutes = (duration/1000)/60;

                    int seconds = (duration/1000) % 60;

                    if (minutes == 0 && seconds == 0 && seekBar.getProgress() == 100) {
                        seconds = 1;
                    }


                    songDuration.setText(f.format(minutes)+":"+f.format(seconds));

                }
            }
        });

        markButtonA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.e(TAG, "textA "+textA.getText().toString());

                //seekBar.setDots(new int[] {25, 50, 75});
                //seekBar.setDotsDrawable(R.drawable.dot);
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //seekBar.setDots(new int[] {});
                //seekBar.setDotsDrawable(R.drawable.dot);
            }
        });

        imgPlayOrPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (songList.size() > 0) { //check if songs exist in list

                    if (mediaOperation.isPause()) { //if stop or pause, play

                        String songPath, songName;
                        if (song_selected > 0) {
                            songPath = songArrayAdapter.getItem(song_selected).getPath();
                            songName = songArrayAdapter.getItem(song_selected).getName();
                            //songList.get(song_selected).setSelected(true);

                            /*for (int i=0; i<songList.size(); i++) {

                                if (i == song_selected) {
                                    songList.get(i).setSelected(true);

                                } else {
                                    songList.get(i).setSelected(false);

                                }
                            }*/


                        } else {
                            songPath = songList.get(0).getPath();
                            songName = songList.get(0).getName();
                            //songList.get(0).setSelected(true);
                            /*for (int i=0; i<songList.size(); i++) {

                                if (i == 0) {
                                    songList.get(i).setSelected(true);

                                } else {
                                    songList.get(i).setSelected(false);

                                }
                            }*/

                        }
                        //myListview.invalidateViews();
                        Log.d(TAG, "play "+songName);


                        mediaOperation.doPlay(songPath);

                        imgPlayOrPause.setImageResource(R.drawable.ic_pause_circle_outline_black_48dp);
                    } else { //playing, pause
                        Log.d(TAG, "pause");
                        mediaOperation.doPause();

                        imgPlayOrPause.setImageResource(R.drawable.ic_play_circle_outline_black_48dp);
                    }
                } else {
                    toast("Song list is empty");
                }
            }
        });

        myListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "select "+position);
                NumberFormat f = new DecimalFormat("00");
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

                current_duration = songList.get(song_selected).getDuration();

            }
        });

        IntentFilter filter;

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(Constants.ACTION.ADD_SONG_LIST_COMPLETE)) {
                    Log.d(TAG, "receive ADD_SONG_LIST_COMPLETE !");

                    for(int i=0; i<songList.size(); i++) {

                        int duration = mediaOperation.getInfo(songList.get(i).getPath());
                        songList.get(i).setDuration(duration);
                    }


                    songArrayAdapter = new SongArrayAdapter(context, R.layout.music_list_item, songList);
                    myListview.setAdapter(songArrayAdapter);



                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION.GET_PLAY_COMPLETE)) {
                    Log.d(TAG, "receive GET_PLAY_COMPLETE !");
                    imgPlayOrPause.setImageResource(R.drawable.ic_play_circle_outline_black_48dp);
                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION.START_TO_PLAY)) {
                    Log.d(TAG, "receive START_TO_PLAY !("+song_selected+")");
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


                }
            }
        };


        if (!isRegister) {
            filter = new IntentFilter();
            filter.addAction(Constants.ACTION.ADD_SONG_LIST_COMPLETE);
            filter.addAction(Constants.ACTION.GET_PLAY_COMPLETE);
            filter.addAction(Constants.ACTION.START_TO_PLAY);
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

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
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
        switch (item.getItemId()) {
            case R.id.action_add:

                intent = new Intent(MainActivity.this, FileChooseActivity.class);
                startActivity(intent);
                break;
            case R.id.action_play_all:
                actionBar.setHomeAsUpIndicator(R.drawable.ic_play_arrow_white_48dp);
                actionBar.setTitle("All");
                linearLayoutAB.setVisibility(View.GONE);
                break;
            case R.id.action_shuffle:
                actionBar.setHomeAsUpIndicator(R.drawable.ic_shuffle_white_48dp);
                actionBar.setTitle("Shuffle");
                linearLayoutAB.setVisibility(View.GONE);
                break;

            case R.id.action_repeat:
                actionBar.setHomeAsUpIndicator(R.drawable.ic_repeat_white_48dp);
                actionBar.setTitle("Repeat");
                linearLayoutAB.setVisibility(View.GONE);
                break;

            case R.id.action_loop:
                actionBar.setHomeAsUpIndicator(R.drawable.ic_loop_white_48dp);
                actionBar.setTitle("AB Loop");
                linearLayoutAB.setVisibility(View.VISIBLE);
                break;

        }
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

    public Locale getCurrentLocale(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            return getResources().getConfiguration().getLocales().get(0);
        } else{
            //noinspection deprecation
            return getResources().getConfiguration().locale;
        }
    }
}
