package com.seventhmoon.jamnow.Data;


public class Constants {


    public interface ACTION {
        String ADD_SONG_LIST_COMPLETE = "com.seventhmoon.JamNow.AddSongListComplete";
        String ADD_SONG_LIST_CHANGE = "com.seventhmoon.JamNow.AddSongListChange";
        String GET_PLAY_COMPLETE = "com.seventhmoon.JamNow.GetPlayComplete";
        String GET_SONGLIST_ACTION = "com.seventhmoon.JamNow.GetSongListAction";

        String GET_SEARCHLIST_ACTION = "com.seventhmoon.JamNow.GetSearchListAction";

        String GET_SONGLIST_FROM_RECORD_FILE_COMPLETE = "com.seventhmoon.JamNow.GetSongListFromRecordFileComplete";
        String SAVE_SONGLIST_ACTION = "com.seventhmoon.JamNow.SaveSongListAction";
        String SAVE_SONGLIST_TO_FILE_COMPLETE = "com.seventhmoon.JamNow.SaveSongListToFileComplete";

        String MEDIAPLAYER_STATE_PLAYED = "com.seventhmoon.JamNow.MediaPlayerStatePlayed";
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
