package com.seventhmoon.jamnow.Data;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;



import com.seventhmoon.jamnow.R;


public class DottedSeekBar extends AppCompatSeekBar {
    //private static final String TAG = DottedSeekBar.class.getName();
    /** Int values which corresponds to dots */
    private int[] mDotsPositions = null;
    /** Drawable for dot */
    private Bitmap mDotBitmap = null;
    private Bitmap mLineBitmap = null;

    public DottedSeekBar(final Context context) {
        super(context);
        init(null);
    }

    public DottedSeekBar(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public DottedSeekBar(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    /**
     * Initializes Seek bar extended attributes from xml
     *
     * @param attributeSet {@link AttributeSet}
     */
    private void init(final AttributeSet attributeSet) {
        final TypedArray attrsArray = getContext().obtainStyledAttributes(attributeSet, R.styleable.DottedSeekBar, 0, 0);

        final int dotsArrayResource = attrsArray.getResourceId(R.styleable.DottedSeekBar_dots_positions, 0);

        if (0 != dotsArrayResource) {
            mDotsPositions = getResources().getIntArray(dotsArrayResource);
        }

        final int dotDrawableId = attrsArray.getResourceId(R.styleable.DottedSeekBar_dots_drawable, 0);

        if (0 != dotDrawableId) {
            mDotBitmap = BitmapFactory.decodeResource(getResources(), dotDrawableId);
        }

        attrsArray.recycle();
    }

    /**
     * @param dots to be displayed on this SeekBar
     */
    public void setDots(final int[] dots) {
        mDotsPositions = dots;
        invalidate();
    }

    /**
     * @param dotsResource resource id to be used for dots drawing
     */
    public void setDotsDrawable(final int dotsResource) {
        mDotBitmap = BitmapFactory.decodeResource(getResources(), dotsResource);
        invalidate();
    }

    public void setmLine(final int lineResource) {
        mLineBitmap = BitmapFactory.decodeResource(getResources(), lineResource);
    }

    @Override
    protected synchronized void onDraw(final Canvas canvas) {
        super.onDraw(canvas);



        final int width = getMeasuredWidth();

        final int height = getMeasuredHeight();
        final int thumb = getThumbOffset();
        final int top = height/2 - 20;
        final int top2 = height/2 - 5;
        final float step = (float) (width - (getPaddingRight()+getPaddingLeft())) / (float) getMax();


        //Log.d(TAG, "Width = "+getMeasuredWidth()+ " height = "+getMeasuredHeight()+" getMax = "+getMax()+" thumb = "
        //        +getThumbOffset()+" step = "+step+" current pos = "+getProgress()+"left = "+getPaddingLeft()+" right = "+getPaddingRight()
        //        );



        boolean isFirst = true;
        int start_position = 0;
        int end_position = 0;

        if (null != mDotsPositions && 0 != mDotsPositions.length && null != mDotBitmap) {
            // draw dots if we have ones
            for (int position : mDotsPositions) {

                if (isFirst) {
                    start_position = position;
                    isFirst = false;
                } else {
                    end_position = position;
                }

                canvas.drawBitmap(mDotBitmap, position * step + (thumb-7), top, null);

            }


        }

        if (mLineBitmap != null) {
            for (int i = start_position; i < end_position; i++) {
                canvas.drawBitmap(mLineBitmap, i * step + (thumb-7), top2, null);

            }
        }
    }

}
