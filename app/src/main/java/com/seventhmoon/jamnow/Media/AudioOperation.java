package com.seventhmoon.jamnow.Media;

import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.seventhmoon.jamnow.Data.Constants;
import com.seventhmoon.jamnow.Data.MediaOperation;
import com.seventhmoon.jamnow.Data.Song;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;





import static com.seventhmoon.jamnow.Data.FileOperation.check_file_exist;
import static com.seventhmoon.jamnow.MainActivity.current_song_duration;
import static com.seventhmoon.jamnow.MainActivity.seekBar;
import static com.seventhmoon.jamnow.MainActivity.setSongDuration;
import static com.seventhmoon.jamnow.MainActivity.songList;
import static com.seventhmoon.jamnow.MainActivity.songPlaying;
import static com.seventhmoon.jamnow.MainActivity.song_selected;


public class AudioOperation {
    private static final String TAG = AudioOperation.class.getName();

    private Context context;

    public AudioOperation (Context context){
        this.context = context;
    }

    public String getAudioInfo(String filePath) {
        Log.e(TAG, "<getAudioInfo>");
        String infoMsg = null;

        MediaExtractor mex = new MediaExtractor();
        try {
            mex.setDataSource(filePath);// the adresss location of the sound on sdcard.
        } catch (IOException e) {
            // TODO Auto-generated catch block
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
                    songList.add(song);
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



}
