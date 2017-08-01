package com.seventhmoon.jamnow.Service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.seventhmoon.jamnow.Data.Constants;
import com.seventhmoon.jamnow.Data.Song;
import com.seventhmoon.jamnow.Data.VideoItem;

import java.io.File;

import static com.seventhmoon.jamnow.Data.FileOperation.check_file_exist;
import static com.seventhmoon.jamnow.Data.FileOperation.check_record_exist;
import static com.seventhmoon.jamnow.Data.FileOperation.read_record;
import static com.seventhmoon.jamnow.MainActivity.songList;
import static com.seventhmoon.jamnow.MainActivity.videoList;


public class GetVideoListFromRecordService extends IntentService {
    private static final String TAG = GetVideoListFromRecordService.class.getName();

    public GetVideoListFromRecordService() {
        super("GetVideoListFromRecordService");
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


        if (intent.getAction().equals(Constants.ACTION.GET_VIDEOLIST_ACTION)) {
            Log.i(TAG, "GET_VIDEOLIST_ACTION");
        }

        if (check_record_exist("video_favorite")) {
            Log.d(TAG, "load file success!");


            String message = read_record(filename);
            //Log.d(TAG, "message = "+ message);
            String msg[] = message.split("\\|");

            //Log.d(TAG, "msg[0] = "+ msg[0]);




            for (int i=0; i<msg.length; i++) {

                Log.d(TAG, "msg["+i+"] = "+ msg[i]);
                String info[] = msg[i].split(";");



                VideoItem new_video = new VideoItem();
                File file = new File(info[0]); //path



                if (check_file_exist(info[0])) { // if file exist, then add



                    new_video.setName(file.getName());
                    new_video.setPath(info[0]);
                    new_video.setDuration_u(Long.valueOf(info[1]));
                    new_video.setMark_a(Integer.valueOf(info[2]));
                    new_video.setMark_b(Integer.valueOf(info[3]));

                    videoList.add(new_video);
                }
            }

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        Intent intent = new Intent(Constants.ACTION.GET_VIDEOLIST_FROM_RECORD_FILE_COMPLETE);
        sendBroadcast(intent);
    }
}
