package com.seventhmoon.jamnow.Service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.seventhmoon.jamnow.Data.Constants;
import com.seventhmoon.jamnow.Data.FileOperation;

import static com.seventhmoon.jamnow.Data.FileOperation.clear_record;
import static com.seventhmoon.jamnow.MainActivity.songList;
import static com.seventhmoon.jamnow.MainActivity.videoList;


public class SaveVideoListToFileService extends IntentService {
    private static final String TAG = SaveVideoListToFileService.class.getName();

    public SaveVideoListToFileService() {
        super("SaveVideoListToFileService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");



    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.i(TAG, "Handle");

        String filename = intent.getStringExtra("FILENAME");


        if (intent.getAction().equals(Constants.ACTION.SAVE_VIDEOLIST_ACTION)) {
            Log.i(TAG, "SAVE_VIDEOLIST_ACTION");
        }

        //video
        clear_record("video_favorite");

        //video
        for (int i=0; i<videoList.size(); i++) {
            String msg;
            if (i== 0) {
                msg = videoList.get(i).getPath()+";"+
                        videoList.get(i).getDuration_u()+";"+videoList.get(i).getMark_a()+";"+videoList.get(i).getMark_b();
            } else {
                msg = "|"+videoList.get(i).getPath()+";"+
                        videoList.get(i).getDuration_u()+";"+videoList.get(i).getMark_a()+";"+videoList.get(i).getMark_b();
            }

            FileOperation.append_record(msg, "video_favorite");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        Intent intent = new Intent(Constants.ACTION.SAVE_VIDEOLIST_TO_FILE_COMPLETE);
        sendBroadcast(intent);
    }
}
