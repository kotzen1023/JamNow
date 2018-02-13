package com.seventhmoon.jamnow.Data;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.seventhmoon.jamnow.R;

import java.util.ArrayList;

public class SmbFileItemArrayAdapter extends ArrayAdapter<SmbFileItem> {
    private static final String TAG = RemoteServerItemArrayAdapter.class.getName();

    private LayoutInflater inflater = null;

    private int layoutResourceId;
    private ArrayList<SmbFileItem> items = new ArrayList<>();
    private Context context;


    public SmbFileItemArrayAdapter(Context context, int textViewResourceId,
                                        ArrayList<SmbFileItem> objects) {
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

    public SmbFileItem getItem(int position)
    {
        return items.get(position);
    }
    @Override
    public @NonNull
    View getView(int position, View convertView, @NonNull final ViewGroup parent) {

        Log.e(TAG, "getView = "+ position);
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

        if (items.size() > 0) {

            final SmbFileItem item = items.get(position);
            if (item != null) {


                Bitmap bitmap;
                int type = item.getFileType();

                switch (type) {
                    case 0:
                        bitmap = BitmapFactory.decodeResource(view.getResources(), R.drawable.file);
                        break;
                    case 1:
                        bitmap = BitmapFactory.decodeResource(view.getResources(), R.drawable.folder);
                        break;
                    default:
                        bitmap = BitmapFactory.decodeResource(view.getResources(), R.drawable.file);
                        break;
                }


                holder.icon.setImageBitmap(bitmap);
                Log.d(TAG, "name = " + item.getFileName());

                holder.name.setText(item.getFileName());


                if (item.isSelected()) {
                    Log.e(TAG, "" + position + " is selected.");
                    view.setBackgroundColor(Color.rgb(0x4d, 0x90, 0xfe));
                } else {
                    Log.e(TAG, "" + position + " clear.");
                    view.setBackgroundColor(Color.TRANSPARENT);
                }


            }
        }
        return view;
    }



    private class ViewHolder {
        ImageView icon;
        ProgressBar progress;
        TextView name;
        //TextView videotime;


        private ViewHolder(View view) {
            this.icon = view.findViewById(R.id.smbFileImageView);
            this.progress = view.findViewById(R.id.smbFileProgressBar);
            this.name = view.findViewById(R.id.smbFileText);
            //this.videotime = (TextView) view.findViewById(R.id.songTime);
        }
    }
}
