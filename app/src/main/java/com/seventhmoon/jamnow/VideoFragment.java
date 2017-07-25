package com.seventhmoon.jamnow;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.seventhmoon.jamnow.Data.SongArrayAdapter;

import static android.content.Context.MODE_PRIVATE;
import static com.seventhmoon.jamnow.MainActivity.songArrayAdapter;
import static com.seventhmoon.jamnow.MainActivity.songList;

/**
 * Created by 1050636 on 2017/7/25.
 */

public class VideoFragment extends Fragment {
    private static final String TAG = VideoFragment.class.getName();

    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView");



        View view = inflater.inflate(R.layout.video_fragment, container, false);



        return view;
    }

    @Override
    public void onDestroyView() {
        Log.i(TAG, "onDestroyView");

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
