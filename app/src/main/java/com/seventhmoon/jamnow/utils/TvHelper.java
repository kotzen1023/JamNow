package com.seventhmoon.jamnow.utils;

import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

public class TvHelper {
    private static final String TAG = TvHelper.class.getName();

    /**
     * Returns true when running Android TV
     *
     * @param c Context to detect UI Mode.
     * @return true when device is running in tv mode, false otherwise.
     */
    public static boolean isTvUiMode(Context c) {
        UiModeManager uiModeManager = (UiModeManager) c.getSystemService(Context.UI_MODE_SERVICE);
        if (uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
            Log.d(TAG, "Running in TV mode");
            return true;
        } else {
            Log.d(TAG, "Running on a non-TV mode");
            return false;
        }
    }
}
