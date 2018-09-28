package com.seventhmoon.jamnow;

import android.app.Activity;
import android.app.UiModeManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

import com.seventhmoon.jamnow.playback.LocalPlayback;

public class NowPlayingActivity extends Activity {
    private static final String TAG = LocalPlayback.class.getName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        Intent newIntent;
        UiModeManager uiModeManager = (UiModeManager) getSystemService(UI_MODE_SERVICE);
        if (uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
            Log.d(TAG, "Running on a TV Device");
            //newIntent = new Intent(this, TvPlaybackActivity.class);
        } else {
            Log.d(TAG, "Running on a non-TV Device");
            //newIntent = new Intent(this, MusicPlayerActivity.class);
        }
        //startActivity(newIntent);
        finish();
    }
}
