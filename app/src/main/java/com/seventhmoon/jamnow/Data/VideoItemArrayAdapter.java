package com.seventhmoon.jamnow.Data;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.media.session.MediaController;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;


import com.seventhmoon.jamnow.R;
import com.seventhmoon.jamnow.VideoFragment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;


public class VideoItemArrayAdapter extends ArrayAdapter<VideoItem> {
    private static final String TAG = VideoItemArrayAdapter.class.getName();
    private LayoutInflater inflater = null;

    private int layoutResourceId;
    private ArrayList<VideoItem> items = new ArrayList<>();
    private Context context;
    private Display display;
    private android.widget.MediaController mediaController;

    public VideoItemArrayAdapter(Context context, int textViewResourceId,
                            ArrayList<VideoItem> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
        //super(context, textViewResourceId, objects);
        this.layoutResourceId = textViewResourceId;
        this.items = objects;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        display = wm.getDefaultDisplay();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mediaController = new android.widget.MediaController(context);
        }
    }

    @Override
    public int getCount() {
        return items.size();

    }

    public VideoItem getItem(int position)
    {
        return items.get(position);
    }
    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {

        //Log.e(TAG, "getView = "+ position);
        View view;
        final ViewHolder holder;
        if (convertView == null || convertView.getTag() == null) {
            //Log.e(TAG, "convertView = null");
            /*view = inflater.inflate(layoutResourceId, null);
            holder = new ViewHolder(view);
            view.setTag(holder);*/

            //LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(layoutResourceId, null);
            holder = new ViewHolder(view);
            //holder.checkbox.setVisibility(View.INVISIBLE);
            view.setTag(holder);
        }
        else {
            view = convertView ;
            holder = (ViewHolder) view.getTag();
        }

        //holder.fileicon = (ImageView) view.findViewById(R.id.fd_Icon1);
        //holder.filename = (TextView) view.findViewById(R.id.fileChooseFileName);
        //holder.checkbox = (CheckBox) view.findViewById(R.id.checkBoxInRow);

        final VideoItem item = items.get(position);
        if (item != null) {


            //NumberFormat f = new DecimalFormat("00");
            //NumberFormat f2 = new DecimalFormat("000");


            //int minutes = (int)(item.getDuration_u()/60000000);

            //int seconds = (int)(item.getDuration_u()/1000000) % 60;

            //int minisec = (int)((item.getDuration_u()/1000)%1000);

            //if (minutes == 0 && seconds == 0) {
            //    seconds = 1;
            //}


            //songDuration.setText(f.format(minutes)+":"+f.format(seconds));

            //
            /*Bitmap bp = null;
            try {
                bp = GetFrameFromFile(item.getPath());
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (bp != null)
                holder.videoicon.setImageBitmap(bp);
            else {
                bp = BitmapFactory.decodeResource(view.getResources(), R.drawable.ic_audiotrack_black_48dp);
                holder.videoicon.setImageBitmap(bp);
            }*/
            if (item.getBitmap() != null) {
                holder.videoicon.setImageBitmap(item.getBitmap());
            } else {
                Bitmap bitmap = BitmapFactory.decodeResource(view.getResources(), R.drawable.ic_music_video_black_48dp);
                holder.videoicon.setImageBitmap(bitmap);
            }

            holder.videoname.setText(item.getName());

            //Bitmap thumb = ThumbnailUtils.createVideoThumbnail(item.getPath(),
            //        MediaStore.Images.Thumbnails.MINI_KIND);
            //Drawable d = new BitmapDrawable(context.getResources(), thumb);

            /*holder.videoView.setBackground(d);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Drawable f = context.getResources().getDrawable(R.drawable.ic_play_circle_outline_white_48dp, context.getTheme());

                holder.videoView.setForeground(f);
                holder.videoView.setForegroundGravity(Gravity.CENTER);
            }
            holder.videoView.setVideoPath(item.getPath());
            holder.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    Log.d(TAG, ""+item.getName()+ " is prepared.");
                }
            });
            holder.videoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mediaController.setAnchorView(holder.videoView);
                    mediaController.setMediaPlayer(holder.videoView);
                    holder.videoView.setMediaController(mediaController);
                    holder.videoView.start();
                }
            });
            holder.videoname.setText(item.getName());
            //holder.songtime.setText(f.format(minutes)+":"+f.format(seconds)+"."+f2.format(minisec));
            /*if (seconds > 1) {
                holder.songtime.setText(minutes+":"+f.format(seconds));
            } else {
                holder.songtime.setText(seconds+"."+f2.format(minisec));
            }*/



            if (item.isSelected()) {
                //Log.e(TAG, ""+position+" is selected.");
                //view.setSelected(true);
                view.setBackgroundColor(Color.rgb(0x4d, 0x90, 0xfe));
            } else {
                //Log.e(TAG, ""+position+" clear.");
                //view.setSelected(false);
                view.setBackgroundColor(Color.TRANSPARENT);
            }








        }
        return view;
    }

    private class ViewHolder {
        ImageView videoicon;
        //VideoView videoView;
        TextView videoname;
        //TextView videotime;


        private ViewHolder(View view) {
            this.videoicon = (ImageView) view.findViewById(R.id.imageView);
            //this.videoView = (VideoView) view.findViewById(R.id.videoView);
            this.videoname = (TextView) view.findViewById(R.id.itemText);
            //this.videotime = (TextView) view.findViewById(R.id.songTime);
        }
    }


}
