package com.seventhmoon.jamnow.Data;


import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


import jcifs.smb.NtlmPasswordAuthentication;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

import static com.seventhmoon.jamnow.Data.FileOperation.check_record_exist;


public class SmbStreamSource {
    private static final String TAG = MediaOperation.class.getName();

    private static File RootDirectory = new File("/");


    protected String mime;
    protected long fp;
    protected long len;
    protected String name;
    protected SmbFile smbFile;
    InputStream inputStream;
    protected int bufferSize;
    protected int openFlags;
    protected int access;
    private File file_temp;

    public SmbStreamSource(String path, String auth_name, String auth_password) {

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //path = Environment.getExternalStorageDirectory();
            RootDirectory = Environment.getExternalStorageDirectory();
        }

        file_temp = new File(RootDirectory.getAbsolutePath() + "/.jamNow/temp");

        fp = 0;
        //len = smbFile.length();

        //name = smbFile.getName();
        //this.smbFile = smbFile;
        bufferSize = 1024*16;

        try {
            NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(null, auth_name, auth_password);


            smbFile = new SmbFile(path, auth);
            smbFile.connect();

            if (smbFile.exists()) {
                Log.d(TAG, "file exist.");

                len = smbFile.length();
                mime = MimeTypeMap.getFileExtensionFromUrl(smbFile.getName());
                name = smbFile.getName();

                smbFile.setReadOnly();
            } else {
                Log.e(TAG, "file not exist.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }



    public void open() throws IOException {
        Log.d(TAG, "open");
        try {
            //input = new SmbFileInputStreamOld(file, bufferSize, 1);
            //inputStream = smbFile.getInputStream();

            SmbFileInputStream inputSmbFileStream = new SmbFileInputStream(smbFile);

            if(!check_record_exist(file_temp.getName())) {
                Log.i(TAG, "file not exist");
                boolean ret = false;
                try {
                    ret = file_temp.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (!ret)
                    Log.e(TAG, "init_info: failed to create file "+file_temp.getAbsolutePath());

            }

            FileOutputStream outputFileStream = new FileOutputStream(file_temp);

            byte[] buffer = new byte[1024*16];
            int length = 0;
            while ((length = inputSmbFileStream.read(buffer)) > 0) {
                outputFileStream.write(buffer, 0, length);
            }

            Log.d(TAG, "file saved");

            outputFileStream.close();
            inputSmbFileStream.close();

            //if(fp>0)
            //    inputStream.skip(fp);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public int read(byte[] buff) throws IOException{
        return read(buff, 0, buff.length);
    }

    public int read(byte[] bytes, int start, int offs) throws IOException {
        int read =  inputStream.read(bytes, start, offs);
        fp += read;
        return read;
    }

    public long moveTo(long position) throws IOException {
        fp = position;
        return fp;
    }

    public void close() {
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getMimeType(){
        return mime;
    }

    public long length(){
        return len;
    }

    public String getName(){
        return name;
    }

    public long available(){
        return len - fp;
    }

    public void reset(){
        fp = 0;
    }

    public SmbFile getFile(){
        return smbFile;
    }

    public int getBufferSize(){
        return bufferSize;
    }

    public static Song getAudioInfo() {
        Log.e(TAG, "<getAudioInfo>");

        File file_temp = new File(RootDirectory.getAbsolutePath() + "/.jamNow/temp");

        Song song_ret = new Song();

        String infoMsg = null;
        boolean hasFrameRate = false;

        MediaExtractor mex = new MediaExtractor();
        try {
            mex.setDataSource(file_temp.getAbsolutePath());// the adresss location of the sound on sdcard.
        } catch (IOException e) {

            e.printStackTrace();
        }



        File file = new File(file_temp.getAbsolutePath());
        Log.d(TAG, "file name: "+file.getName());

        if (mex != null) {

            try {
                MediaFormat mf = mex.getTrackFormat(0);
                Log.d(TAG, "file: "+file.getName()+" mf = "+mf.toString());
                infoMsg = mf.getString(MediaFormat.KEY_MIME);
                Log.d(TAG, "type: "+infoMsg);

                if (infoMsg.contains("audio")) {

                    Log.d(TAG, "duration(us): " + mf.getLong(MediaFormat.KEY_DURATION));
                    Log.d(TAG, "channel: " + mf.getInteger(MediaFormat.KEY_CHANNEL_COUNT));
                    if (mf.toString().contains("channel-mask")) {
                        Log.d(TAG, "channel mask: " + mf.getInteger(MediaFormat.KEY_CHANNEL_MASK));
                    }
                    if (mf.toString().contains("aac-profile")) {
                        Log.d(TAG, "aac profile: " + mf.getInteger(MediaFormat.KEY_AAC_PROFILE));
                    }

                    Log.d(TAG, "sample rate: " + mf.getInteger(MediaFormat.KEY_SAMPLE_RATE));

                    if (infoMsg != null) {
                        //Song song = new Song();
                        //song_ret.setName(file.getName());
                        song_ret.setPath(file.getAbsolutePath());
                        //song.setDuration((int)(mf.getLong(MediaFormat.KEY_DURATION)/1000));
                        song_ret.setDuration_u(mf.getLong(MediaFormat.KEY_DURATION));
                        song_ret.setChannel((byte) mf.getInteger(MediaFormat.KEY_CHANNEL_COUNT));
                        song_ret.setSample_rate(mf.getInteger(MediaFormat.KEY_SAMPLE_RATE));
                        song_ret.setMark_a(0);
                        song_ret.setMark_b((int) (mf.getLong(MediaFormat.KEY_DURATION) / 1000));
                        //addSongList.add(song);

                    }
                } else if (infoMsg.contains("video")) { //video
                    try {
                        Log.d(TAG, "frame rate : " + mf.getInteger(MediaFormat.KEY_FRAME_RATE));
                        hasFrameRate = true;
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                    Log.d(TAG, "height : " + mf.getInteger(MediaFormat.KEY_HEIGHT));
                    Log.d(TAG, "width : " + mf.getInteger(MediaFormat.KEY_WIDTH));
                    Log.d(TAG, "duration(us): " + mf.getLong(MediaFormat.KEY_DURATION));

                    if (infoMsg != null) {
                        VideoItem video = new VideoItem();
                        video.setName(file.getName());
                        video.setPath(file.getAbsolutePath());
                        if (hasFrameRate)
                            video.setFrame_rate(mf.getInteger(MediaFormat.KEY_FRAME_RATE));

                        video.setHeight(mf.getInteger(MediaFormat.KEY_HEIGHT));
                        video.setWidth(mf.getInteger(MediaFormat.KEY_WIDTH));
                        video.setDuration_u( mf.getLong(MediaFormat.KEY_DURATION));
                        video.setMark_a(0);
                        video.setMark_b((int) (mf.getLong(MediaFormat.KEY_DURATION) / 1000));
                        //addVideoList.add(video);
                        /*Song song = new Song();
                        song.setName(file.getName());
                        song.setPath(file.getAbsolutePath());
                        //song.setDuration((int)(mf.getLong(MediaFormat.KEY_DURATION)/1000));
                        song.setDuration_u(mf.getLong(MediaFormat.KEY_DURATION));
                        song.setChannel((byte) mf.getInteger(MediaFormat.KEY_CHANNEL_COUNT));
                        song.setSample_rate(mf.getInteger(MediaFormat.KEY_SAMPLE_RATE));
                        song.setMark_a(0);
                        song.setMark_b((int) (mf.getLong(MediaFormat.KEY_DURATION) / 1000));
                        addSongList.add(song);*/

                    }

                } else {
                    Log.e(TAG, "Unknown type");
                }

            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }

        } else {
            Log.d(TAG, "file: "+file.getName()+" not support");
        }

        Log.e(TAG, "</getAudioInfo>");




        return song_ret;
    }
}
