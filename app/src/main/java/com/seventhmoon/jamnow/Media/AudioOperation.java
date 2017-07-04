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
import android.util.Log;

import com.seventhmoon.jamnow.Data.Constants;
import com.seventhmoon.jamnow.Data.Song;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;


import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.android.AndroidAudioPlayer;
import be.tarsos.dsp.io.android.AndroidFFMPEGLocator;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;

import static com.seventhmoon.jamnow.Data.FileOperation.check_file_exist;
import static com.seventhmoon.jamnow.MainActivity.seekBar;
import static com.seventhmoon.jamnow.MainActivity.setSongDuration;
import static com.seventhmoon.jamnow.MainActivity.songList;
import static com.seventhmoon.jamnow.MainActivity.songPlaying;


public class AudioOperation {
    private static final String TAG = AudioOperation.class.getName();

    private Context context;
    private AudioDispatcher adp;
    private AudioEvent aet;
    private static AudioTask goodTask;
    private boolean taskDone = true;
    private boolean pause = true;

    private static int current_play_mode = 0;
    private static long song_duration_u = 0;
    private double current_position_u = 0;

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
            current_position_u = adp.secondsProcessed();
        } else {
            current_position_u = 0.0;
        }

        Log.d(TAG, "</getCurrentPosition>");

        return current_position_u;
    }

    public void setCurrentPosition(double position) {

        this.current_position_u = position;
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

    private void PlayShortAudioFileViaAudioTrack(String filePath) throws IOException
    {
        // We keep temporarily filePath globally as we have only two sample sounds now..
        if (filePath==null)
            return;

        //Reading the file..
        byte[] byteData = null;
        File file = null;
        file = new File(filePath); // for ex. path= "/sdcard/samplesound.pcm" or "/sdcard/samplesound.wav"
        byteData = new byte[(int) file.length()];
        FileInputStream in = null;
        try {
            in = new FileInputStream( file );
            in.read( byteData );
            in.close();

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // Set and push to audio track..
        int intSize = android.media.AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_8BIT);
        AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC, 8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_8BIT, intSize, AudioTrack.MODE_STREAM);
        if (at!=null) {
            at.play();
            // Write the byte array to the track
            at.write(byteData, 0, byteData.length);
            at.stop();
            at.release();
        }
        else
            Log.d("TCAudio", "audio track is not initialised ");

    }



    public void PlayAudioFileViaAudioTrack(final int song_select) throws IOException
    {
        Log.d(TAG, "<PlayAudioFileViaAudioTrack>");

        MediaCodec decoderAudio = null;
        MediaFormat mf;
        long duration_u ;
        int channel;
        int sample_rate = 0;
        int audioTrack = 0;

        // We keep temporarily filePath globally as we have only two sample sounds now..
        if (songList.get(song_select).getPath() == null || !check_file_exist(songList.get(song_select).getPath()))
            return;

        //check before play
        MediaExtractor mex = new MediaExtractor();
        try {
            mex.setDataSource(songList.get(song_select).getPath());// the address location of the sound on sdcard.
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        for (int i = 0; i < mex.getTrackCount(); i++) {
            MediaFormat format = mex.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("audio/")) {
                Log.d(TAG, "mime type = "+mime);
                audioTrack = i;
                mex.selectTrack(audioTrack);
                mf = format;
                decoderAudio = MediaCodec.createDecoderByType(mime);
                sample_rate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                Log.d(TAG, "sample_rate = "+sample_rate);
                channel = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
                Log.d(TAG, "channel = "+channel);

                song_duration_u = format.getLong(MediaFormat.KEY_DURATION);
                Log.d(TAG, "duration_u = "+song_duration_u);

                decoderAudio.configure(format, null, null, 0);

                break;
            }
        }

        if (audioTrack >=0) {
            if(decoderAudio == null)
            {
                Log.e(TAG, "Can't find audio info!");
                return;
            }
            else
            {
                int minBufferSize = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
                int bufferSize = 4 * minBufferSize;
                final AudioTrack playAudioTrack = new AudioTrack(
                        AudioManager.STREAM_MUSIC,
                        sample_rate,
                        AudioFormat.CHANNEL_OUT_STEREO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize,
                        AudioTrack.MODE_STREAM
                );



                if (taskDone) {
                    File mp3 = new File(songList.get(song_select).getPath());
                    adp = AudioDispatcherFactory.fromPipe(mp3.getAbsolutePath(), 88200, 5000, 2500);

                    taskDone = false;
                    goodTask = new AudioTask();
                    goodTask.execute(10);

                    new AndroidFFMPEGLocator(context);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            //File externalStorage = Environment.getExternalStorageDirectory();

                            //AudioDispatcher adp;




                            aet = new AudioEvent(adp.getFormat());

                            //Log.e(TAG, "format = " + adp.getFormat().toString()+" end timestamp = "+aet.getSampleRate());


                            adp.addAudioProcessor(new AndroidAudioPlayer(adp.getFormat(), 5000, AudioManager.STREAM_MUSIC));


                            adp.run();
                        }
                    }).start();
                }





            }
        }









        Log.d(TAG, "</PlayAudioFileViaAudioTrack>");
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

        if (!adp.isStopped()) {
            adp.stop();
            pause = true;

            Intent newNotifyIntent = new Intent(Constants.ACTION.MEDIAPLAYER_STATE_PAUSED);
            context.sendBroadcast(newNotifyIntent);
        }


        Log.d(TAG, "</doPause>");

    }

    public void doPlay(String songPath) {
        Log.d(TAG, "<doPlay>");

        pause = false;
        playing(songPath);
        Log.d(TAG, "</doPlay>");
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
                adp = AudioDispatcherFactory.fromPipe(mp3.getAbsolutePath(), 88200, 5000, 2500);

                taskDone = false;
                goodTask = new AudioTask();
                goodTask.execute(10);

                new AndroidFFMPEGLocator(context);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //File externalStorage = Environment.getExternalStorageDirectory();

                        //AudioDispatcher adp;




                        aet = new AudioEvent(adp.getFormat());

                        //Log.e(TAG, "format = " + adp.getFormat().toString()+" end timestamp = "+aet.getSampleRate());

                        adp.skip(current_position_u);
                        adp.addAudioProcessor(new AndroidAudioPlayer(adp.getFormat(), 5000, AudioManager.STREAM_MUSIC));


                        adp.run();
                    }
                }).start();
            }
        }

        Log.d(TAG, "</playing>");
    }

    class AudioTask extends AsyncTask<Integer, Integer, String>
    {
        @Override
        protected String doInBackground(Integer... countTo) {


            //while(current_state == STATE.Started) {
            while(!adp.isStopped()) {



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

                        int position = (int) ((adp.secondsProcessed() * 1000000.0 * 1000.0) / song_duration_u);
                        Log.d(TAG, "second process :" + adp.secondsProcessed() + " position = " + position);
                        //int position = (int)(aet.getProgress());
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

            Log.d(TAG, "==== onPreExecute ====");




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


            taskDone = true;

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

            super.onCancelled();

        }
    }
}
