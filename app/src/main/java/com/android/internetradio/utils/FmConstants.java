package com.android.internetradio.utils;

import com.android.internetradio.BuildConfig;

public interface FmConstants {
//    String URL_FM_SERVER = "https://stage.dbnewshub.com/api/v1/fm-radio?_format=json";
    String URL_FM_SERVER = "https://api.dbnewshub.com/api/v1/fm-radio?_format=json";
    String FM_FILE_NAME = "FmFile.dat";
    String FM_FILE_DIR = "Carvaan";
    String KEY_APP_TITLE = "app_title";
    String KEY_DB_ID = "db_id";
    String KEY_AUTH_TOKEN = "auth_token";
    String KEY_PREVIOUS_SCREEN_NAME = "previous_screen_name";
    String KEY_UID = "user_uid";
    String KEY_LOGIN_STATUS = "login_status";

    String ACTION_PLAY_TRACKS = "ACTION_PLAY_TRACKS";
    String ACTION_PAUSE_TRACKS = "ACTION_PAUSE_TRACKS";
    String ACTION_STOP_TRACKS = "ACTION_STOP_TRACKS";
    String ACTION_BROADCAST_PLAYBACK_CONTROL = BuildConfig.APPLICATION_ID +
            "RadioPlayer";
    String KEY_AUDIO_PLAYBACK_EVENT = "key_audio_playback_event";

    /*String PREF_KEY_UNIQUE_DEVICE_ID = "unique_device_id_for_first_launch";
    String PREF_KEY_DEVICE_ID_SHARED_NAME = "key_device_id_shared_pref";*/

    interface HeaderKeys {
        String KEY_AUTHORIZATION = "Authorization";
        String KEY_CHANNEL = "Channel";
        String KEY_DEVICE_TYPE = "Device-Type";
        String KEY_ACCEPT = "Accept";
        String KEY_DB_ID = "DB-Id";
        String KEY_CONTENT_TYPE = "Content-Type";
        String KEY_UUID = "uuid";
        String KEY_X_CSRF_TOKEN = "X-CSRF-Token";
    }

    interface HeaderValues {
        String VALUE_CHANNEL = "MOBILE";
        String VALUE_DEVICE_TYPE = "ANDROID";
        String VALUE_ACCEPT = "application/json";
        String VALUE_CONTENT_TYPE = "application/json";
    }

    // Google Analytics Constants
    String GA_TAG = "GoogleAnalytics";
    String GA_APPLICATION_METHOD = "getGATracker";

    interface GAEventCategory {
        String RADIO = "Radio";
//        String NEXT_SCREEN = "NextScreen";
    }

    interface GAAction {
        /*String SELECTED_LANGUAGE = "Selected_Language";
        String SELECTED_CATEGORY = "Selected_Category";
        String LISTENED_STATION = "Listened_Station";*/
        String PLAY = "Play";
        String PAUSE = "Pause";
    }

    interface GALabel {
        String PLAY = "Play";
        String PAUSE = "Pause";
        String GUEST = "Guest";
        String LOGGED_IN = "LoggedIn";
    }

    interface GAScreenName {
        String RADIO_BHASKAR_FM = "Radio_BhaskarFM";
        String GA_CURRENT_SCREEN_TAG = "CurrentScreen_";
        String GA_NEXT_SCREEN_TAG = "NextScreen_";
    }

    interface GAScreenCategory {
        String RADIO = "Radio";
    }

    interface GADimensions {
        int CONTENT_CATEGORY = 6;
        int LOGIN_STATUS = 7;
        int PREVIOUS_SCREEN = 9;
        int DB_USER_ID = 8;
        int DB_ID = 12;
        int STORY_ID = 13;
    }
}
