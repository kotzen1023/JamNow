package com.seventhmoon.jamnow.Service;

import android.app.IntentService;
import android.content.Intent;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import com.seventhmoon.jamnow.Data.Constants;


import java.io.File;
import java.io.IOException;


import static com.seventhmoon.jamnow.MainActivity.addSongList;
import static com.seventhmoon.jamnow.MainActivity.searchList;


import com.seventhmoon.jamnow.Data.Song;



public class SearchFileService extends IntentService {
    private static final String TAG = SearchFileService.class.getName();

    public SearchFileService() {
        super("SearchFileService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");



    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.i(TAG, "Handle");

        //String filename = intent.getStringExtra("FILENAME");


        if (intent.getAction().equals(Constants.ACTION.GET_SEARCHLIST_ACTION)) {
            Log.i(TAG, "GET_SEARCHLIST_ACTION");
        }

        //clear add list
        addSongList.clear();

        searchFiles();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

        Intent intent = new Intent(Constants.ACTION.ADD_SONG_LIST_COMPLETE);
        sendBroadcast(intent);
    }

    public String getAudioInfo(String filePath) {
        Log.e(TAG, "<getAudioInfo>");
        String infoMsg = null;

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

                Log.d(TAG, "duration(us): "+mf.getLong(MediaFormat.KEY_DURATION));
                Log.d(TAG, "channel: "+mf.getInteger(MediaFormat.KEY_CHANNEL_COUNT));
                if (mf.toString().contains("channel-mask")) {
                    Log.d(TAG, "channel mask: "+mf.getInteger(MediaFormat.KEY_CHANNEL_MASK));
                }
                if (mf.toString().contains("aac-profile")) {
                    Log.d(TAG, "aac profile: "+mf.getInteger(MediaFormat.KEY_AAC_PROFILE));
                }

                Log.d(TAG, "sample rate: "+mf.getInteger(MediaFormat.KEY_SAMPLE_RATE));

                if (infoMsg != null) {
                    Song song = new Song();
                    song.setName(file.getName());
                    song.setPath(file.getAbsolutePath());
                    //song.setDuration((int)(mf.getLong(MediaFormat.KEY_DURATION)/1000));
                    song.setDuration_u(mf.getLong(MediaFormat.KEY_DURATION));
                    song.setChannel((byte)mf.getInteger(MediaFormat.KEY_CHANNEL_COUNT));
                    song.setSample_rate(mf.getInteger(MediaFormat.KEY_SAMPLE_RATE));
                    song.setMark_a(0);
                    song.setMark_b((int)(mf.getLong(MediaFormat.KEY_DURATION)/1000));
                    addSongList.add(song);

                }

            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }

        } else {
            Log.d(TAG, "file: "+file.getName()+" not support");
        }

        Log.e(TAG, "</getAudioInfo>");




        return infoMsg;
    }

    /*public void checkFileAndDuration(File file) {

        getAudioInfo(file.getAbsolutePath());

    }*/

    private void search(File file) {


        if (file.exists() && file.isDirectory())
        {
            Log.e(TAG, " <Dir>");
            Log.d(TAG, ""+file.getName()+" is a directory");

            //String[] children = file.list();
            File[] dirs = file.listFiles();

            for (File children : dirs)
            {
                Log.d(TAG, "["+children.getAbsolutePath()+"]");
                File chk = new File(children.getAbsolutePath());
                if (chk.exists() && chk.isDirectory()) {
                    Log.e(TAG, "Enter "+chk.getName()+" :");
                    search(chk);
                } else if (chk.isFile()){
                    Log.e(TAG, " <File>");
                    getAudioInfo(chk.getAbsolutePath());
                } else {
                    Log.e(TAG, "Unknown error(1)");
                }
            }
        } else if (file.isFile()) {
            Log.e(TAG, " <File>");
            getAudioInfo(file.getAbsolutePath());
        } else {
            Log.e(TAG, "Unknown error(2)");
        }


        Log.e(TAG, " <search>");
    }


    public void searchFiles() {
        Log.e(TAG, "<searchFiles>");

        for (int i=0; i<searchList.size(); i++) {
            File file = new File(searchList.get(i));
            search(file);
        }

        //Intent newNotifyIntent = new Intent(Constants.ACTION.ADD_SONG_LIST_COMPLETE);
        //sendBroadcast(newNotifyIntent);

        Log.e(TAG, "<searchFiles>");
    }
}
