package com.seventhmoon.jamnow.Service;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.util.Log;

import com.seventhmoon.jamnow.Data.Constants;

import static com.seventhmoon.jamnow.MainActivity.videoList;


public class GetThumbImageService extends IntentService {
    private static final String TAG = GetThumbImageService.class.getName();

    public GetThumbImageService() {
        super("GetThumbImageService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");



    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.i(TAG, "Handle");

        if (intent.getAction() != null) {
            if (intent.getAction().equals(Constants.ACTION.GET_THUMB_IMAGE_ACTION)) {
                Log.i(TAG, "GET_THUMB_IMAGE_ACTION");
            }

            if (videoList.size() > 0) {
                for (int i=0; i<videoList.size(); i++) {
                    Bitmap thumb = ThumbnailUtils.createVideoThumbnail(videoList.get(0).getPath(),
                            MediaStore.Images.Thumbnails.MINI_KIND);

                    videoList.get(i).setBitmap(thumb);
                }
            }
        }



    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        Intent intent = new Intent(Constants.ACTION.GET_THUMB_IMAGE_COMPLETE);
        sendBroadcast(intent);
    }
}
