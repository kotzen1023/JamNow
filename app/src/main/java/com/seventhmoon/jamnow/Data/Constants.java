package com.seventhmoon.jamnow.Data;


public class Constants {


    public interface ACTION {
        String ADD_SONG_LIST_COMPLETE = "com.seventhmoon.JamNow.AddSongListComplete";
        String GET_PLAY_COMPLETE = "com.seventhmoon.JamNow.GetPlayComplete";
        String GET_SONGLIST_ACTION = "com.seventhmoon.JamNow.GetSongListAction";
        String GET_SONGLIST_FROM_RECORD_FILE_COMPLETE = "com.seventhmoon.JamNow.GetSongListFromRecordFileComplete";

        String MEDIAPLAYER_STATE_STARTED = "com.seventhmoon.JamNow.MediaPlayerStateStarted";
        String MEDIAPLAYER_STATE_PAUSED = "com.seventhmoon.JamNow.MediaPlayerStatePaused";
    }

    public enum STATE {
        Created,
        Idle,
        Initialized,
        Preparing,
        Prepared,
        Started,
        Paused,
        Stopped,
        PlaybackCompleted,
        End,
        Error,

    }


}
