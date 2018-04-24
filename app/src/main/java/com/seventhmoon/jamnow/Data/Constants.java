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

        //video
        String ADD_VIDEO_LIST_COMPLETE = "com.seventhmoon.JamNow.AddVideoListComplete";
        String GET_VIDEOLIST_ACTION = "com.seventhmoon.JamNow.GetVideoListAction";
        String GET_VIDEOLIST_FROM_RECORD_FILE_COMPLETE = "com.seventhmoon.JamNow.GetVideoListFromRecordFileComplete";

        String SAVE_VIDEOLIST_ACTION = "com.seventhmoon.JamNow.SaveVideoListAction";
        String SAVE_VIDEOLIST_TO_FILE_COMPLETE = "com.seventhmoon.JamNow.SaveVideoListToFileComplete";

        String GET_THUMB_IMAGE_ACTION = "com.seventhmoon.JamNow.GetThumbImageAction";
        String GET_THUMB_IMAGE_COMPLETE = "com.seventhmoon.JamNow.GetThumbImageComplete";

        String FILE_CHOOSE_CONFIRM_BUTTON_SHOW = "com.seventhmoon.JamNow.FileChooseConfirmButtonShow";
        String FILE_CHOOSE_CONFIRM_BUTTON_HIDE = "com.seventhmoon.JamNow.FileChooseConfirmButtonHide";

        String GET_URLPATH_ACTION = "com.seventhmoon.JamNow.GetUrlPath";
        String GET_FULLVIEW_ACTION = "com.seventhmoon.JamNow.GetFullViewAction";

        String ADD_REMOTE_COMPLETE = "com.seventhmoon.JamNow.AddRemoteComplete";
        String DELETE_REMOTE_COMPLETE = "com.seventhmoon.JamNow.DeleteRemoteComplete";
        String MODIFY_REMOTE_COMPLETE = "com.seventhmoon.JamNow.ModifyRemoteComplete";

        String SMB_CONNECT_FAILED = "com.seventhmoon.JamNow.SmbConnectFailed";
        String SMB_CONNECT_SUCCESS = "com.seventhmoon.JamNow.SmbConnectSuccess";
        String GET_SMB_FILELIST_COMPLETE = "com.seventhmoon.JamNow.GetSmbFilelistComplete";
        String SMB_LIST_CLEAR = "com.seventhmoon.JamNow.SmbListClear";
        String SAVE_REMOTE_FILE_AS_LOCAL_TEMP_ACTION = "com.seventhmoon.JamNow.SaveRemoteFileAsLocalTempAction";
        String SAVE_REMOTE_FILE_AS_LOCAL_COMPLETE = "com.seventhmoon.JamNow.SaveRemoteFileAsLocalComplete";
        String SAVE_REMOTE_FILE_AS_LOCAL_FAIL = "com.seventhmoon.JamNow.SaveRemoteFileAsLocalFail";

        String PLAY_NEXT_REMOTE_ACTION = "com.seventhmoon.JamNow.PlayNextRemoteAction";
        String CHROME_CAST_AUDIO_ACTION = "com.seventhmoon.JamNow.ChromeCastAudioAction";
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
