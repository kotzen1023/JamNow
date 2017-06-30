package com.seventhmoon.jamnow.Media;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import com.seventhmoon.jamnow.Data.Song;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static com.seventhmoon.jamnow.Data.FileOperation.check_file_exist;
import static com.seventhmoon.jamnow.MainActivity.songList;


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

    public void PlayAudioFileViaAudioTrack(Song song) throws IOException
    {
        Log.d(TAG, "<PlayAudioFileViaAudioTrack>");


        // We keep temporarily filePath globally as we have only two sample sounds now..
        if (song.getPath() == null || !check_file_exist(song.getPath()))
            return;

        //check before play
        MediaExtractor mex = new MediaExtractor();
        try {
            mex.setDataSource(song.getPath());// the adresss location of the sound on sdcard.
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        MediaFormat mf;
        long duration_u ;
        int channel;
        int sample_rate;

        try {
            mf = mex.getTrackFormat(0);
            duration_u = mf.getLong(MediaFormat.KEY_DURATION);
            channel = mf.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
            sample_rate = mf.getInteger(MediaFormat.KEY_SAMPLE_RATE);

            Log.d(TAG, "duration_u : "+duration_u);
            Log.d(TAG, "channel: "+channel);
            Log.d(TAG, "sample rate: "+sample_rate);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }




        int intSize = android.media.AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT);

        AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT, intSize, AudioTrack.MODE_STREAM);


        if (at==null){
            Log.d("TCAudio", "audio track is not initialised ");
            return;
        }


        Log.d(TAG, "channel = "+at.getChannelCount()+" sample rate = "+at.getSampleRate()+" format = "+at.getAudioFormat());


        int count = 512 * 1024; // 512 kb
        //Reading the file..
        byte[] byteData = null;
        File file = null;
        file = new File(song.getPath());

        byteData = new byte[(int)count];
        FileInputStream in = null;
        try {
            in = new FileInputStream( file );

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        int bytesread = 0, ret = 0;
        int size = (int) file.length();
        at.play();
        Log.d(TAG, "time = "+( at.getPlaybackHeadPosition( ) / at.getSampleRate( ) ) * 1000.0);
        while (bytesread < size) {
            ret = in.read( byteData,0, count);
            if (ret != -1) { // Write the byte array to the track
                at.write(byteData,0, ret);
                bytesread += ret;
            } else
                break;
        }
        in.close();

        Log.d(TAG, "time = "+( at.getPlaybackHeadPosition( ) / at.getSampleRate( ) ) * 1000.0);
        at.stop();
        at.release();

        Log.d(TAG, "</PlayAudioFileViaAudioTrack>");
    }
}
