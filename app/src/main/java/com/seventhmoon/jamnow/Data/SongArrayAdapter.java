package com.seventhmoon.jamnow.Data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.seventhmoon.jamnow.R;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import static com.seventhmoon.jamnow.FileChooseActivity.FileChooseLongClick;
import static com.seventhmoon.jamnow.MainActivity.songDuration;
import static com.seventhmoon.jamnow.MainActivity.song_selected;


public class SongArrayAdapter extends ArrayAdapter<Song> {
    private static final String TAG = SongArrayAdapter.class.getName();

    LayoutInflater inflater = null;

    private int layoutResourceId;
    private ArrayList<Song> items = new ArrayList<>();

    public SongArrayAdapter(Context context, int textViewResourceId,
                                  ArrayList<Song> objects) {
        super(context, textViewResourceId, objects);
        this.layoutResourceId = textViewResourceId;
        this.items = objects;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        return items.size();

    }

    public Song getItem(int position)
    {
        return items.get(position);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //Log.e(TAG, "getView = "+ position);
        View view;
        ViewHolder holder;
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

        Song songItem = items.get(position);
        if (songItem != null) {

            NumberFormat f = new DecimalFormat("00");


            int minutes = (songItem.getDuration()/1000)/60;

            int seconds = (songItem.getDuration()/1000) % 60;

            if (minutes == 0 && seconds == 0) {
                seconds = 1;
            }


            //songDuration.setText(f.format(minutes)+":"+f.format(seconds));

            Bitmap bitmap = BitmapFactory.decodeResource(view.getResources(), R.drawable.ic_audiotrack_black_48dp);
            holder.songicon.setImageBitmap(bitmap);
            holder.songname.setText(songItem.getName());
            holder.songtime.setText(f.format(minutes)+":"+f.format(seconds));

            if (songItem.isSelected()) {
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

    class ViewHolder {
        ImageView songicon;
        TextView songname;
        TextView songtime;


        public ViewHolder(View view) {
            this.songicon = (ImageView) view.findViewById(R.id.songIcon);
            this.songname = (TextView) view.findViewById(R.id.songName);
            this.songtime = (TextView) view.findViewById(R.id.songTime);
        }
    }
}
