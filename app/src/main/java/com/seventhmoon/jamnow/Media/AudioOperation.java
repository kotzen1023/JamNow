package com.seventhmoon.jamnow.Media;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.seventhmoon.jamnow.Data.Song;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;


import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.android.AndroidAudioPlayer;
import be.tarsos.dsp.io.android.AndroidFFMPEGLocator;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;

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


    public void PlayAudioFileViaAudioTrack(final Song song) throws IOException
    {
        Log.d(TAG, "<PlayAudioFileViaAudioTrack>");

        MediaCodec decoderAudio = null;
        MediaFormat mf;
        long duration_u ;
        int channel;
        int sample_rate = 0;
        int audioTrack = 0;

        // We keep temporarily filePath globally as we have only two sample sounds now..
        if (song.getPath() == null || !check_file_exist(song.getPath()))
            return;

        //check before play
        MediaExtractor mex = new MediaExtractor();
        try {
            mex.setDataSource(song.getPath());// the address location of the sound on sdcard.
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

                new AndroidFFMPEGLocator(context);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //File externalStorage = Environment.getExternalStorageDirectory();
                        File mp3 = new File(song.getPath());
                        AudioDispatcher adp;
                        adp = AudioDispatcherFactory.fromPipe(mp3.getAbsolutePath(),44100,5000,2500);

                        adp.addAudioProcessor(new AndroidAudioPlayer(adp.getFormat(),5000, AudioManager.STREAM_MUSIC));

                        adp.run();
                    }
                }).start();


                /*
                inputBuffersAudio = decoderAudio.getInputBuffers();
                outputBuffersAudio = decoderAudio.getOutputBuffers();
                infoAudio = new MediaCodec.BufferInfo();


                // create our AudioTrack instance
                //int minBufferSize = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_CONFIGURATION_STEREO, AudioFormat.ENCODING_PCM_16BIT);
                int minBufferSize = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
                int bufferSize = 4 * minBufferSize;
                AudioTrack playAudioTrack = new AudioTrack(
                        AudioManager.STREAM_MUSIC,
                        sample_rate,
                        AudioFormat.CHANNEL_OUT_STEREO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize,
                        AudioTrack.MODE_STREAM
                );

                Log.d(TAG, "time = "+( playAudioTrack.getPlaybackHeadPosition( ) / playAudioTrack.getSampleRate( ) ) * 1000.0);
                playAudioTrack.play();
                decoderAudio.start();

                boolean isEOS = false;
                long startMs = System.currentTimeMillis();
                long lasAudioStartMs = System.currentTimeMillis();
                while (!Thread.interrupted()) {
                    if (audioTrack >=0)
                    {
                        if (!isEOS) {
                            int inIndex=-1;
                            try {
                                inIndex = decoderAudio.dequeueInputBuffer(10000);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (inIndex >= 0) {
                                ByteBuffer buffer = inputBuffersAudio[inIndex];
                                int sampleSize = mex.readSampleData(buffer, 0);
                                if (sampleSize < 0) {

                                    decoderAudio.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                                    buffer.clear();
                                    isEOS = true;
                                } else {
                                    decoderAudio.queueInputBuffer(inIndex, 0, sampleSize, mex.getSampleTime(), 0);
                                    buffer.clear();
                                    mex.advance();
                                }

                            }
                        }

                        int outIndex=-1;
                        try {
                            outIndex = decoderAudio.dequeueOutputBuffer(infoAudio,10000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        switch (outIndex) {
                            case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                                Log.d(TAG, "INFO_OUTPUT_BUFFERS_CHANGED");
                                outputBuffersAudio = decoderAudio.getOutputBuffers();
                                break;
                            case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                                Log.d(TAG, "New format " + decoderAudio.getOutputFormat());
                                playAudioTrack.setPlaybackRate(sample_rate);
                                break;
                            case MediaCodec.INFO_TRY_AGAIN_LATER:
                                Log.d(TAG, "dequeueOutputBuffer timed out!");
                                break;
                            default:
                                if(outIndex>=0)
                                {
                                    ByteBuffer buffer = outputBuffersAudio[outIndex];
                                    byte[] chunk = new byte[infoAudio.size];
                                    buffer.get(chunk);
                                    buffer.clear();
                                    if(chunk.length>0){
                                        playAudioTrack.write(chunk,0,chunk.length);
                                    }
                                    decoderAudio.releaseOutputBuffer(outIndex, false);
                                }
                                break;
                        }

                        // All decoded frames have been rendered, we can stop playing now
                        if ((infoAudio.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            Log.d(TAG, "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
                            break;
                        }
                    }
                }




                Log.d(TAG, "time = "+( playAudioTrack.getPlaybackHeadPosition( ) / playAudioTrack.getSampleRate( ) ) * 1000.0);

                if (audioTrack >=0)
                {
                    decoderAudio.stop();
                    decoderAudio.release();
                    playAudioTrack.stop();
                }

                */


            }
        }







        /*int intSize = android.media.AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_STEREO,
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
        at.release();*/

        Log.d(TAG, "</PlayAudioFileViaAudioTrack>");
    }
}
