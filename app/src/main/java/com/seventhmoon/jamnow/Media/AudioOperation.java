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


import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.android.AndroidAudioPlayer;
import be.tarsos.dsp.io.android.AndroidFFMPEGLocator;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;


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

    private static boolean is_thread_running = false;
    private Thread playThread;

    private AudioDispatcher adp;
    //private AudioEvent aet;
    private static AudioTask goodTask;
    private boolean taskDone = true;
    private boolean pause = true;

    private static int current_play_mode = 0;
    private static long song_duration_u = 0;
    private double current_position_u = 0;

    private ArrayList<Integer> shuffleList = new ArrayList<>();
    private int current_shuffle_index = 0;

    //ab loop
    private int ab_loop_start = 0;
    private int ab_loop_end = 0;


    public AudioOperation (Context context){
        this.context = context;
    }

    public boolean isPause() {
        return pause;
    }

    public int getCurrent_play_mode() {
        return current_play_mode;
    }

    public void setCurrent_play_mode(int current_play_mode) {
        this.current_play_mode = current_play_mode;
    }

    public double getCurrentPosition() {
        Log.d(TAG, "<getCurrentPosition>");

        if (adp != null) {
            current_position_u = current_position_u + adp.secondsProcessed();
        } else {
            current_position_u = 0.0;
        }

        Log.d(TAG, "</getCurrentPosition>");

        return current_position_u;
    }

    public void setCurrentPosition(double position) {

        this.current_position_u = position;
    }

    public int getCurrent_shuffle_index() {
        return current_shuffle_index;
    }

    public void setCurrent_shuffle_index(int current_shuffle_index) {
        this.current_shuffle_index = current_shuffle_index;
    }

    public int getShufflePosition() {
        return shuffleList.get(current_shuffle_index);
    }

    public void setAb_loop_start(int ab_loop_start) {
        this.ab_loop_start = ab_loop_start;
    }

    public void setAb_loop_end(int ab_loop_end) {
        this.ab_loop_end = ab_loop_end;
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

    public boolean isPlaying() {
        boolean ret = false;
        if (adp != null && !adp.isStopped()) {
            ret = true;
        }

        return ret;
    }

    public void stop() {
        if (!adp.isStopped()) {
            adp.stop();



            Intent newNotifyIntent = new Intent(Constants.ACTION.MEDIAPLAYER_STATE_PAUSED);
            context.sendBroadcast(newNotifyIntent);


        }

        if (goodTask != null) {
            Log.e(TAG, "cancel task");
            if (!goodTask.isCancelled()) {
                goodTask.cancel(true);
                goodTask = null;
                taskDone = true;
            }
        }
    }

    public void doPause() {

        Log.d(TAG, "<doPause>");

        //stop the thread
        Log.e(TAG, "*** cancel thread, send interrupt! ***");
        is_thread_running = false;
        playThread.interrupt();
        playThread = null;

        if (goodTask != null) {
            Log.e(TAG, "*** cancel task ***");
            if (!goodTask.isCancelled()) {
                goodTask.cancel(true);
                goodTask = null;
                taskDone = true;
            }
        }

        if (adp!= null && !adp.isStopped()) {
            adp.stop();
        }

        pause = true;

        Intent newNotifyIntent = new Intent(Constants.ACTION.MEDIAPLAYER_STATE_PAUSED);
        context.sendBroadcast(newNotifyIntent);


        Log.d(TAG, "</doPause>");

    }

    public void doPlay(String songPath) {
        Log.d(TAG, "<doPlay>");

        pause = false;
        playing(songPath);
        Log.d(TAG, "</doPlay>");
    }

    public void doNext() {
        Log.d(TAG, "<doNext>");

        if (songList == null || songList.size() == 0) {
            return;
        }

        if (current_play_mode == 1) { //shuffle
            //find out current select
            for (int i=0; i<songList.size(); i++) {
                if (song_selected == shuffleList.get(i)) {
                    current_shuffle_index = i;
                }
            }

            //get next

            if (current_shuffle_index < shuffleList.size() - 1) {
                current_shuffle_index++;
            } else {
                current_shuffle_index = 0;
            }
            song_selected = shuffleList.get(current_shuffle_index);
        } else {
            if (song_selected < songList.size() - 1) {

                //songList.get(song_selected).setSelected(false);

                song_selected++;

            } else {
                song_selected = songList.size() - 1;
            }
        }

        songPlaying = song_selected;
        current_song_duration = (int)(songList.get(song_selected).getDuration_u()/1000);

        ab_loop_start = songList.get(song_selected).getMark_a();
        ab_loop_end = songList.get(song_selected).getMark_b();

        if (current_play_mode == 3) { //ab loop
            current_position_u = ab_loop_start;
        }

        String songPath = songList.get(song_selected).getPath();
        playing(songPath);

        Log.d(TAG, "</doNext>");
    }

    private void playing(String songPath) {
        Log.d(TAG, "<playing " + songPath + ">");

        MediaCodec decoderAudio = null;
        MediaFormat mf;
        long duration_u ;
        int channel;
        int sample_rate = 0;
        int audioTrack = 0;

        // We keep temporarily filePath globally as we have only two sample sounds now..
        if (songPath == null || !check_file_exist(songPath)) {
            Log.e(TAG, "path is null or file is not exist");
            return;
        }

        //check before play
        MediaExtractor mex = new MediaExtractor();
        try {
            mex.setDataSource(songPath);// the address location of the sound on sdcard.
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //find track

        for (int i = 0; i < mex.getTrackCount(); i++) {
            MediaFormat format = mex.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("audio/")) {
                Log.d(TAG, "mime type = "+mime);
                audioTrack = i;
                mex.selectTrack(audioTrack);
                //mf = format;

                //decoderAudio = MediaCodec.createDecoderByType(mime);
                sample_rate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                Log.d(TAG, "sample_rate = "+sample_rate);
                channel = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
                Log.d(TAG, "channel = "+channel);

                song_duration_u = format.getLong(MediaFormat.KEY_DURATION);
                Log.d(TAG, "duration_u = "+song_duration_u);

                //decoderAudio.configure(format, null, null, 0);

                break;
            }
        }

        if (audioTrack >=0) {
            if (taskDone) {
                File mp3 = new File(songPath);

                Log.e(TAG, "current_position_u = "+current_position_u);

                adp = AudioDispatcherFactory.fromPipe(mp3.getAbsolutePath(), 44100, 5000, 2500, current_position_u);



                taskDone = false;
                goodTask = new AudioTask();
                goodTask.execute(10);

                Intent newNotifyIntent = new Intent(Constants.ACTION.MEDIAPLAYER_STATE_STARTED);
                context.sendBroadcast(newNotifyIntent);

                new AndroidFFMPEGLocator(context);

                playThread = new Thread() {
                    @Override
                    public void run() {
                        Log.d(TAG, "<Thread>");
                        //File externalStorage = Environment.getExternalStorageDirectory();

                        //AudioDispatcher adp;




                        //aet = new AudioEvent(adp.getFormat());

                        //Log.e(TAG, "format = " + adp.getFormat().toString()+" end timestamp = "+aet.getSampleRate());
                        //androidAudioPlayer = new AndroidAudioPlayer(adp.getFormat(), 5000, AudioManager.STREAM_MUSIC);

                        //adp.skip(current_position_u);
                        adp.addAudioProcessor(new AndroidAudioPlayer(adp.getFormat(), 5000, AudioManager.STREAM_MUSIC));


                        adp.run();

                        Message msg = new Message();
                        //msg.setData(countBundle);
                        mHandler.sendMessage(msg);



                        Log.d(TAG, "</Thread>");
                    }
                };

                is_thread_running = true;
                playThread.start();

                /*new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //File externalStorage = Environment.getExternalStorageDirectory();

                        //AudioDispatcher adp;




                        aet = new AudioEvent(adp.getFormat());

                        //Log.e(TAG, "format = " + adp.getFormat().toString()+" end timestamp = "+aet.getSampleRate());
                        //androidAudioPlayer = new AndroidAudioPlayer(adp.getFormat(), 5000, AudioManager.STREAM_MUSIC);

                        //adp.skip(current_position_u);
                        adp.addAudioProcessor(new AndroidAudioPlayer(adp.getFormat(), 5000, AudioManager.STREAM_MUSIC));


                        adp.run();
                    }
                }).start();*/
            }
        }

        Log.d(TAG, "</playing>");
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Intent newNotifyIntent = new Intent(Constants.ACTION.MEDIAPLAYER_STATE_PAUSED);
            context.sendBroadcast(newNotifyIntent);

            current_position_u = 0.0;

            switch (current_play_mode) {
                case 0: //play all
                    doNext();
                    break;
                case 1: //play shuffle
                    //doShuffle();
                    break;
                case 2: //single repeat
                    //doSingleRepeat();
                    break;
                case 3: //an loop
                    //doABLoop();
                    break;
            }


            Log.e(TAG, "====>Thread is over.");
        }
    };

    class AudioTask extends AsyncTask<Integer, Integer, String>
    {
        @Override
        protected String doInBackground(Integer... countTo) {


            //while(current_state == STATE.Started) {
            while(adp!= null && !adp.isStopped()) {



                /*if (current_play_mode == 3) {//ab loop, check if current position is bigger than mark_b

                    if (mediaPlayer.getCurrentPosition() < ab_loop_start || mediaPlayer.getCurrentPosition() > ab_loop_end) {

                        if (current_state == Constants.STATE.Started) { //pause must in started state

                            Log.d(TAG, "position = " + mediaPlayer.getCurrentPosition() + "ab_loop_start = " + ab_loop_start + " ab_loop_end = " + ab_loop_end);
                            mediaPlayer.pause();
                            mediaPlayer.seekTo(ab_loop_start);
                            mediaPlayer.start();
                        }
                    }
                }*/


                try {


                    //if (current_state == Constants.STATE.Started) {

                        //int position = ((mediaPlayer.getCurrentPosition() * 1000) / mediaPlayer.getDuration());

                        //publishProgress(position);
                    //}
                    if (adp != null) {

                        int position = (int) (((current_position_u + adp.secondsProcessed()) * 1000000.0 * 1000.0) / song_duration_u);
                        Log.d(TAG, "second process :" + adp.secondsProcessed() + " position = " + position);
                        //int position = (int)(aet.getProgress());
                        if (position > 0)
                            publishProgress(position);
                    }

                    Thread.sleep(200);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            Log.d(TAG, "==== AsyncTask onPreExecute ====");




            /*loadDialog = new ProgressDialog(PhotoList.this);
            loadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            loadDialog.setTitle(R.string.photolist_decrypting_title);
            loadDialog.setProgress(0);
            loadDialog.setMax(100);
            loadDialog.setIndeterminate(false);
            loadDialog.setCancelable(false);

            loadDialog.show();*/
        }

        @Override
        protected void onProgressUpdate(Integer... values) {

            super.onProgressUpdate(values);


            /*NumberFormat f = new DecimalFormat("00");
            NumberFormat f2 = new DecimalFormat("000");


            int minutes = (mediaPlayer.getCurrentPosition()/60000);

            int seconds = (mediaPlayer.getCurrentPosition()/1000) % 60;

            int minisec = (mediaPlayer.getCurrentPosition()%1000);

            songDuration.setText(f.format(minutes)+":"+f.format(seconds)+"."+f2.format(minisec));*/

            //setActionBarTitle(mediaPlayer.getCurrentPosition());

            if (values[0] >= 1000) {
                seekBar.setProgress(1000);
                //setSongDuration(mediaPlayer.getDuration());
            } else {

                seekBar.setProgress(values[0]);
                //setSongDuration(mediaPlayer.getCurrentPosition());
            }




        }

        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);

            Log.d(TAG, "==== AsyncTask onPostExecute ====");


            taskDone = true;
            adp.stop();
            adp = null;

            /*if (pause) { //if pause, don't change progress
                Log.d(TAG, "Pause was pressed while playing");
            } else {
                Log.e(TAG, "pause is not been pressed.");
                seekBar.setProgress(0);
            }*/

            //taskDone = true;

            //loadDialog.dismiss();
            /*btnDecrypt.setVisibility(View.INVISIBLE);
            btnShare.setVisibility(View.INVISIBLE);
            btnDelete.setVisibility(View.INVISIBLE);
            selected_count = 0;*/
        }

        @Override
        protected void onCancelled() {

            Log.d(TAG, "==== onCancelled ====");



            super.onCancelled();

        }
    }
}
