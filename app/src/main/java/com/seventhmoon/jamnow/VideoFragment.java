package com.seventhmoon.jamnow;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.seventhmoon.jamnow.Data.Constants;
import com.seventhmoon.jamnow.Data.SongArrayAdapter;
import com.seventhmoon.jamnow.Data.VideoItem;
import com.seventhmoon.jamnow.Data.VideoItemArrayAdapter;
import com.seventhmoon.jamnow.Service.GetSongListFromRecordService;
import com.seventhmoon.jamnow.Service.GetThumbImageService;
import com.seventhmoon.jamnow.Service.GetVideoListFromRecordService;
import com.seventhmoon.jamnow.Service.SaveListToFileService;
import com.seventhmoon.jamnow.Service.SaveVideoListToFileService;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import static android.content.Context.MODE_PRIVATE;
import static com.seventhmoon.jamnow.Data.FileOperation.check_record_exist;
import static com.seventhmoon.jamnow.MainActivity.MODE_PLAY_AB_LOOP;
import static com.seventhmoon.jamnow.MainActivity.MODE_PLAY_ALL;
import static com.seventhmoon.jamnow.MainActivity.MODE_PLAY_REPEAT;
import static com.seventhmoon.jamnow.MainActivity.MODE_PLAY_SHUFFLE;
import static com.seventhmoon.jamnow.MainActivity.addSongList;
import static com.seventhmoon.jamnow.MainActivity.addVideoList;
import static com.seventhmoon.jamnow.MainActivity.current_mode;
import static com.seventhmoon.jamnow.MainActivity.current_song_duration;
import static com.seventhmoon.jamnow.MainActivity.current_video_position;
import static com.seventhmoon.jamnow.MainActivity.item_clear;
import static com.seventhmoon.jamnow.MainActivity.item_remove;
import static com.seventhmoon.jamnow.MainActivity.loadDialog;
import static com.seventhmoon.jamnow.MainActivity.mediaOperation;
import static com.seventhmoon.jamnow.MainActivity.progress_mark_a;
import static com.seventhmoon.jamnow.MainActivity.progress_mark_b;
import static com.seventhmoon.jamnow.MainActivity.seekBar;
import static com.seventhmoon.jamnow.MainActivity.songArrayAdapter;
import static com.seventhmoon.jamnow.MainActivity.songList;
import static com.seventhmoon.jamnow.MainActivity.song_selected;
import static com.seventhmoon.jamnow.MainActivity.textA;
import static com.seventhmoon.jamnow.MainActivity.textB;
import static com.seventhmoon.jamnow.MainActivity.videoItemArrayAdapter;
import static com.seventhmoon.jamnow.MainActivity.videoList;
import static com.seventhmoon.jamnow.MainActivity.video_selected;
import static com.seventhmoon.jamnow.MainActivity.current_video_duration;


public class VideoFragment extends Fragment {
    private static final String TAG = VideoFragment.class.getName();

    private Context context;

    private GridView myGridview;

    private static BroadcastReceiver mReceiver = null;
    private static boolean isRegister = false;
    private int previous_selected = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getContext();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView");



        View view = inflater.inflate(R.layout.video_fragment, container, false);

        myGridview = (GridView) view.findViewById(R.id.gridViewMyFavorite);

        myGridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                VideoItem item = (VideoItem) parent.getItemAtPosition(position);

                //MenuItem menuItem = actionmenu.findItem(R.id.action_selectall);
                //Log.i("item", "" + position + " was select");
                if (item.isSelected()) {
                    Log.i(TAG, "selected -> unselected");
                    //selected[position] = false;
                    item.setSelected(false);
                    //if (selected_count > 0)
                    //    selected_count--;

                } else {
                    Log.i(TAG, "unselected -> selected");
                    //selected[position] = true;
                    item.setSelected(true);
                    //selected_count++;
                }

                //deselect other
                for (int i=0; i<videoList.size(); i++) {

                    if (i == position) {
                        videoList.get(i).setSelected(true);

                    } else {
                        videoList.get(i).setSelected(false);

                    }
                }

                video_selected = position;
                current_video_duration = (int)(videoList.get(video_selected).getDuration_u()/1000);


                myGridview.invalidateViews();

                if (previous_selected == video_selected) {
                    current_video_position = 0;
                    Intent intent = new Intent(context, VideoPlayActivity.class);
                    startActivity(intent);
                } else {
                    previous_selected = video_selected;
                }
            }
        });

        IntentFilter filter;

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase(Constants.ACTION.GET_VIDEOLIST_FROM_RECORD_FILE_COMPLETE)) {

                    if (videoList.size() > 0) {

                        videoItemArrayAdapter = new VideoItemArrayAdapter(context, R.layout.video_choose_item, videoList);
                        myGridview.setAdapter(videoItemArrayAdapter);

                        if (loadDialog != null)
                            loadDialog.dismiss();

                        /*songArrayAdapter = new SongArrayAdapter(context, R.layout.music_list_item, songList);
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
                        }*/

                        //show item
                        if (item_remove != null) {
                            item_remove.setVisible(true);
                        }
                        if (item_clear != null) {
                            item_clear.setVisible(true);
                        }

                        //get video thumb
                        Intent myintent = new Intent(context, GetThumbImageService.class);
                        myintent.setAction(Constants.ACTION.GET_THUMB_IMAGE_ACTION);
                        context.startService(myintent);

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



                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION.ADD_VIDEO_LIST_COMPLETE)) {
                    Log.d(TAG, "receive ADD_VIDEO_LIST_COMPLETE !");


                    for (int i=0; i<addVideoList.size(); i++) {
                        videoList.add(addVideoList.get(i));
                        Log.d(TAG, "add "+addVideoList.get(i).getName()+" to videoList");
                    }

                    //mediaOperation.shuffleReset();
                    //mediaOperation.setShufflePosition(0);

                    if (videoItemArrayAdapter == null) {
                        videoItemArrayAdapter = new VideoItemArrayAdapter(context, R.layout.video_choose_item, videoList);
                        myGridview.setAdapter(videoItemArrayAdapter);
                    } else {
                        Log.e(TAG, "notifyDataSetChanged");
                        videoItemArrayAdapter.notifyDataSetChanged();
                    }



                    Intent saveintent = new Intent(context, SaveVideoListToFileService.class);
                    saveintent.setAction(Constants.ACTION.SAVE_SONGLIST_ACTION);
                    saveintent.putExtra("FILENAME", "video_favorite");
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

                    //get video thumb
                    Intent myintent = new Intent(context, GetThumbImageService.class);
                    myintent.setAction(Constants.ACTION.GET_THUMB_IMAGE_ACTION);
                    context.startService(myintent);

                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION.GET_THUMB_IMAGE_COMPLETE)) {
                    videoItemArrayAdapter.notifyDataSetChanged();
                } else if (intent.getAction().equalsIgnoreCase(Constants.ACTION.SAVE_VIDEOLIST_TO_FILE_COMPLETE)) {
                    if (loadDialog != null)
                        loadDialog.dismiss();
                }
            }
        };

        if (videoList.size() == 0 ) {
            loadVideos();
        } else {
            videoItemArrayAdapter = new VideoItemArrayAdapter(context, R.layout.video_choose_item, videoList);
            myGridview.setAdapter(videoItemArrayAdapter);

            //show item
            if (item_remove != null) {
                item_remove.setVisible(true);
            }
            if (item_clear != null) {
                item_clear.setVisible(true);
            }
        }

        if (!isRegister) {
            filter = new IntentFilter();
            filter.addAction(Constants.ACTION.ADD_VIDEO_LIST_COMPLETE);
            filter.addAction(Constants.ACTION.GET_VIDEOLIST_FROM_RECORD_FILE_COMPLETE);
            filter.addAction(Constants.ACTION.GET_THUMB_IMAGE_COMPLETE);
            filter.addAction(Constants.ACTION.SAVE_VIDEOLIST_TO_FILE_COMPLETE);

            context.registerReceiver(mReceiver, filter);
            isRegister = true;
            Log.d(TAG, "registerReceiver mReceiver");
        }

        return view;
    }

    public void loadVideos() {

        if (check_record_exist("video_favorite")) {
            Log.d(TAG, "load file success!");
            loadDialog = new ProgressDialog(context);
            loadDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            loadDialog.setTitle("Loading...");
            loadDialog.setIndeterminate(false);
            loadDialog.setCancelable(false);

            loadDialog.show();

            Intent intent = new Intent(context, GetVideoListFromRecordService.class);
            intent.setAction(Constants.ACTION.GET_VIDEOLIST_ACTION);
            intent.putExtra("FILENAME", "video_favorite");
            context.startService(intent);


        }
    }

    public void toast(String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
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

        videoItemArrayAdapter = null;

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
