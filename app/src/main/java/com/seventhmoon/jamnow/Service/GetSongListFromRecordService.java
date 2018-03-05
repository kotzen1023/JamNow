package com.seventhmoon.jamnow.Service;

import android.app.IntentService;

import android.content.Intent;

import android.util.Log;
import android.webkit.MimeTypeMap;

import com.seventhmoon.jamnow.Data.Constants;
import com.seventhmoon.jamnow.Data.Song;


import java.io.File;
import java.io.IOException;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;

import static com.seventhmoon.jamnow.Data.FileOperation.check_file_exist;
import static com.seventhmoon.jamnow.Data.FileOperation.check_record_exist;

import static com.seventhmoon.jamnow.Data.FileOperation.read_record;

import static com.seventhmoon.jamnow.MainActivity.songList;


public class GetSongListFromRecordService extends IntentService {
    private static final String TAG = GetSongListFromRecordService.class.getName();

    public GetSongListFromRecordService() {
        super("GetSongListFromRecordService");
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


        if (intent.getAction() != null) {
            if (intent.getAction().equals(Constants.ACTION.GET_SONGLIST_ACTION)) {
                Log.i(TAG, "GET_SONGLIST_ACTION");
            }

            if (check_record_exist("favorite")) {
                Log.d(TAG, "load file success!");


                String message = read_record(filename);
                //Log.d(TAG, "message = "+ message);
                String msg[] = message.split("\\|");

                //Log.d(TAG, "msg[0] = "+ msg[0]);




                for (int i=0; i<msg.length; i++) {

                    Log.d(TAG, "msg["+i+"] = "+ msg[i]);
                    String info[] = msg[i].split(";");



                    Song new_song = new Song();
                    File file = new File(info[0]); //path






                    if (check_file_exist(info[0])) { // if file exist, then add



                        new_song.setName(file.getName());
                        new_song.setPath(info[0]);
                        new_song.setDuration_u(Long.valueOf(info[1]));
                        new_song.setMark_a(Integer.valueOf(info[2]));
                        new_song.setMark_b(Integer.valueOf(info[3]));

                        songList.add(new_song);
                    }

                    if (info.length > 4) {
                        if (Boolean.valueOf(info[4])) {
                            Log.e(TAG, "====> file "+info[4]+ " is remote" );

                            String remote_path = info[5];
                            String auth_name = info[6];
                            String auth_password = info[7];

                            try {
                                NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(null, auth_name, auth_password);


                                SmbFile smbFile = new SmbFile(remote_path, auth);

                                new_song.setName(smbFile.getName());
                                new_song.setIs_remote(true);
                                new_song.setRemote_path(remote_path);
                                new_song.setAuth_name(auth_name);
                                new_song.setAuth_pwd(auth_password);
                                songList.add(new_song);
                                /*smbFile.connect();
                                smbFile.setConnectTimeout(5000);

                                if (smbFile.exists()) {
                                    Log.d(TAG, "file exist.");

                                    new_song.setName(smbFile.getName());
                                    new_song.setIs_remote(true);
                                    new_song.setRemote_path(remote_path);
                                    new_song.setAuth_name(auth_name);
                                    new_song.setAuth_pwd(auth_password);
                                    songList.add(new_song);
                                } else {
                                    Log.e(TAG, "file not exist.");
                                }*/

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

            }
        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        Intent intent = new Intent(Constants.ACTION.GET_SONGLIST_FROM_RECORD_FILE_COMPLETE);
        sendBroadcast(intent);
    }
}
