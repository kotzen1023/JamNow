package com.seventhmoon.jamnow.Data;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import android.graphics.drawable.Drawable;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;

import android.os.Message;
import android.support.annotation.NonNull;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;


import com.seventhmoon.jamnow.R;
import com.seventhmoon.jamnow.VideoPlayActivity;

import java.util.ArrayList;

import static com.seventhmoon.jamnow.MainActivity.current_video_position;
import static com.seventhmoon.jamnow.MainActivity.current_volume;
import static com.seventhmoon.jamnow.MainActivity.isVideoPreview;
import static com.seventhmoon.jamnow.MainActivity.videoList;
import static com.seventhmoon.jamnow.MainActivity.video_selected;


public class VideoItemArrayAdapter extends ArrayAdapter<VideoItem> {
    private static final String TAG = VideoItemArrayAdapter.class.getName();
    private LayoutInflater inflater = null;

    private int layoutResourceId;
    private ArrayList<VideoItem> items = new ArrayList<>();
    private Context context;
    private MediaPlayer mediaPlayer;
    private VideoView currentVideoView;

    //private Display display;
    //private android.widget.MediaController mediaController;

    public VideoItemArrayAdapter(Context context, int textViewResourceId,
                            ArrayList<VideoItem> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
        //super(context, textViewResourceId, objects);
        this.layoutResourceId = textViewResourceId;
        this.items = objects;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        //display = wm.getDefaultDisplay();


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
    public @NonNull View getView(int position, View convertView, @NonNull final ViewGroup parent) {

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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Drawable f = context.getResources().getDrawable(R.drawable.ic_play_circle_outline_white_48dp, context.getTheme());

                    holder.videoicon.setForeground(f);
                    holder.videoicon.setForegroundGravity(Gravity.CENTER);
                }
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
                //holder.progressBar.setVisibility(View.VISIBLE);
                holder.videoicon.setVisibility(View.GONE);
                holder.videoView.setVisibility(View.VISIBLE);
                holder.videoView.setVideoURI(Uri.parse(item.getPath()));
                //holder.videoView.setVideoPath(item.getPath());



                holder.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {

                        holder.videoView.setVisibility(View.VISIBLE);
                        holder.videoView.requestFocus();
                        //holder.progressBar.setVisibility(View.GONE);

                        mediaPlayer = mp;
                        mediaPlayer.setVolume(current_volume, current_volume);
                        Log.d(TAG, "onPrepared");
                        isVideoPreview = true;
                        holder.videoView.start();
                        //videoView.start();
                    }
                });

                holder.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        Log.d(TAG, "onCompletion");
                        holder.videoicon.setVisibility(View.VISIBLE);
                        holder.videoView.setVisibility(View.GONE);
                        isVideoPreview = false;
                    }
                });

                holder.videoView.setOnTouchListener(new View.OnTouchListener() {

                    public boolean onTouch(View v, MotionEvent event) {

                        Log.e(TAG, "onTouch");

                        Intent intent = new Intent(Constants.ACTION.GET_FULLVIEW_ACTION);
                        intent.putExtra("AB_LOOP_START", String.valueOf(item.getMark_a()));
                        intent.putExtra("AB_LOOP_END", String.valueOf(item.getMark_b()));
                        context.sendBroadcast(intent);

                        return false;
                    }
                });


                view.setBackgroundColor(Color.rgb(0x4d, 0x90, 0xfe));
            } else {
                //Log.e(TAG, ""+position+" clear.");
                //view.setSelected(false);
                //holder.progressBar.setVisibility(View.GONE);
                holder.videoicon.setVisibility(View.VISIBLE);
                holder.videoView.setVisibility(View.GONE);


                holder.videoView.stopPlayback();
                isVideoPreview = false;
                view.setBackgroundColor(Color.TRANSPARENT);
            }








        }
        return view;
    }



    private class ViewHolder {
        ImageView videoicon;
        ProgressBar progressBar;
        VideoView videoView;
        TextView videoname;
        //TextView videotime;


        private ViewHolder(View view) {
            this.videoicon = view.findViewById(R.id.imageView);
            this.progressBar = view.findViewById(R.id.progressBar);
            this.videoView = view.findViewById(R.id.videoView);
            this.videoname = view.findViewById(R.id.itemText);
            //this.videotime = (TextView) view.findViewById(R.id.songTime);
        }
    }


}
