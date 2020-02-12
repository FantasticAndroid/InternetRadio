package com.android.internetradio.services;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.media.MediaBrowserServiceCompat;

import com.android.internetradio.R;
import com.android.internetradio.adapters.MediaPlayerExoAdapter;
import com.android.internetradio.adapters.PlayerAdapter;
import com.android.internetradio.helpers.PlaybackInfoListener;
import com.android.internetradio.models.FmSavedStation;
import com.android.internetradio.notifications.MediaNotificationManager;
import com.android.internetradio.utils.FmConstants;
import com.android.internetradio.utils.FmSharedPref;

import java.util.ArrayList;
import java.util.List;

public final class RadioInternetService extends MediaBrowserServiceCompat {

    private static final String TAG = RadioInternetService.class.getSimpleName();

    private MediaSessionCompat mSession;
    private PlayerAdapter mPlayback;
    private MediaNotificationManager mMediaNotificationManager;
    private MediaSessionCallback mCallback;
    private boolean mServiceInStartedState;
    private MediaControllerCompat.TransportControls transportControls;

    @Override
    public void onCreate() {
        super.onCreate();
        // Create a new MediaSession.
        mSession = new MediaSessionCompat(this, TAG);
        mCallback = new MediaSessionCallback();
        mSession.setCallback(mCallback);
        mSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        setSessionToken(mSession.getSessionToken());
        mSession.setActive(true);
        mMediaNotificationManager = new MediaNotificationManager(this);

        transportControls = mSession.getController().getTransportControls();

        mPlayback = new MediaPlayerExoAdapter(this, new MediaPlayerListener());
        Log.d(TAG, "onCreate: RadioInternetService creating MediaSession, and MediaNotificationManager");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case FmConstants.ACTION_PLAY_TRACKS: {
                    transportControls.play();
                    /*try {
                        if (mPlayback != null && mPlayback.isPlaying())
                            mPlayback.play();
                    } catch (Error e) {
                        Log.e(TAG, e.getMessage() + "");
                    }*/
                    break;
                }
                case FmConstants.ACTION_PAUSE_TRACKS: {
                    transportControls.pause();
                    /*try {
                        if (mPlayback != null && !mPlayback.isPlaying())
                            mPlayback.pause();
                    } catch (Error e) {
                        Log.e(TAG, e.getMessage() + "");
                    }*/
                    break;
                }
                case FmConstants.ACTION_STOP_TRACKS: {
                    if (transportControls != null) {
                        transportControls.stop();
                    }
                    stopSelf();
                    /*try {
                        if (mPlayback != null && !mPlayback.isPlaying())
                            mPlayback.pause();
                    } catch (Error e) {
                        Log.e(TAG, e.getMessage() + "");
                    }*/
                    break;
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        try {
            mMediaNotificationManager.onDestroy();
            mPlayback.stop();
            mSession.release();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage() + "");
        } finally {
            super.onDestroy();
        }
        Log.d(TAG, "onDestroy: PlayerAdapter stopped, and MediaSession released");
    }

    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName,
                                 int clientUid,
                                 Bundle rootHints) {
        return new BrowserRoot("root", null);
    }

    @Override
    public void onLoadChildren(
            @NonNull final String parentMediaId,
            @NonNull final Result<List<MediaBrowserCompat.MediaItem>> result) {
//        result.sendResult(MusicLibrary.getMediaItems());
        List<MediaBrowserCompat.MediaItem> tempResult = new ArrayList<>();

        if (mCallback.mPreparedMedia != null) {
            tempResult.add(new MediaBrowserCompat.MediaItem(mCallback.mPreparedMedia.getDescription(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE));
        }

        result.sendResult(tempResult);
    }

    // MediaSession Callback: Transport Controls -> PlayerAdapter
    public class MediaSessionCallback extends MediaSessionCompat.Callback {
        private MediaMetadataCompat mPreparedMedia;

        MediaSessionCallback() {
            FmSavedStation fmStation = new FmSharedPref(RadioInternetService.this.getApplicationContext()).getCurrentStation();
            if (null != fmStation) {
                // mPreparedMedia = MusicLibrary.getMetadata(RadioInternetService.this, mediaId);
                mPreparedMedia = createMediaMetadataCompat(fmStation);
            }
        }

        @Override
        public void onAddQueueItem(MediaDescriptionCompat description) {
        }

        @Override
        public void onRemoveQueueItem(MediaDescriptionCompat description) {
        }

        @Override
        public void onPrepare() {
            FmSavedStation fmStation = new FmSharedPref(RadioInternetService.this.getApplicationContext()).getCurrentStation();
            if (null != fmStation) {
                mPreparedMedia = createMediaMetadataCompat(fmStation);
                mSession.setMetadata(mPreparedMedia);

                if (!mSession.isActive()) {
                    mSession.setActive(true);
                }
            }
        }

        @Override
        public void onPlay() {

            if (!isReadyToPlay()) {
                // Nothing to play.
                return;
            }

            onPrepare();
            if (null != mPreparedMedia)
                mPlayback.playFromMedia(mPreparedMedia);
            Log.d(TAG, "onPlayFromMediaId: MediaSession active");
        }

        @Override
        public void onPause() {
            mPlayback.pause();
        }

        @Override
        public void onStop() {
            mPlayback.stop();
            mSession.setActive(false);
        }

        @Override
        public void onSkipToNext() {
            mPreparedMedia = null;
            onPlay();
        }

        @Override
        public void onSkipToPrevious() {
            mPreparedMedia = null;
            onPlay();
        }

        @Override
        public void onSeekTo(long pos) {
            mPlayback.seekTo(pos);
        }

        private boolean isReadyToPlay() {
            return true;
        }
    }

    // PlayerAdapter Callback: PlayerAdapter state -> RadioInternetService.
    public class MediaPlayerListener extends PlaybackInfoListener {

        private final ServiceManager mServiceManager;

        MediaPlayerListener() {
            mServiceManager = new ServiceManager();
        }

        @Override
        public void onPlaybackStateChange(PlaybackStateCompat state) {
            // Report the state to the MediaSession.
            mSession.setPlaybackState(state);
            broadCastPlaybackEvent(state.getState());
            // Manage the started state of this service.
            switch (state.getState()) {
                case PlaybackStateCompat.STATE_PLAYING:
                    mServiceManager.moveServiceToStartedState(state);
                    break;
                case PlaybackStateCompat.STATE_PAUSED:
                    mServiceManager.updateNotificationForPause(state);
                    break;
                case PlaybackStateCompat.STATE_STOPPED:
                    mServiceManager.moveServiceOutOfStartedState(state);
                    break;
            }
        }

        class ServiceManager {

            private void moveServiceToStartedState(PlaybackStateCompat state) {
                Notification notification =
                        mMediaNotificationManager.getNotification(
                                mPlayback.getCurrentMedia(), state, getSessionToken());

                if (!mServiceInStartedState) {
                    ContextCompat.startForegroundService(
                            RadioInternetService.this,
                            new Intent(RadioInternetService.this, RadioInternetService.class));
                    mServiceInStartedState = true;
                }

                startForeground(MediaNotificationManager.NOTIFICATION_ID, notification);
            }

            private void updateNotificationForPause(PlaybackStateCompat state) {
                stopForeground(false);
                Notification notification =
                        mMediaNotificationManager.getNotification(
                                mPlayback.getCurrentMedia(), state, getSessionToken());
                mMediaNotificationManager.getNotificationManager()
                        .notify(MediaNotificationManager.NOTIFICATION_ID, notification);
            }

            private void moveServiceOutOfStartedState(PlaybackStateCompat state) {
                stopForeground(true);
                stopSelf();
                mServiceInStartedState = false;
            }
        }
    }

    /**
     * @param playerState
     */
    private void broadCastPlaybackEvent(int playerState) {
        Intent broadcastIntent = new Intent(FmConstants.ACTION_BROADCAST_PLAYBACK_CONTROL);
        broadcastIntent.putExtra(FmConstants.KEY_AUDIO_PLAYBACK_EVENT, playerState);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    }

    private MediaMetadataCompat createMediaMetadataCompat(FmSavedStation fmStation) {
        return new MediaMetadataCompat.Builder().putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, String.valueOf(fmStation.getRadioId()))
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, fmStation.getFmName())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, fmStation.getFmChannelName() + " | " + fmStation.getFmCategoryName())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM,
                        getApplicationContext().getString(R.string.app_name))
                .putString(
                        MediaMetadataCompat.METADATA_KEY_MEDIA_URI,
                        fmStation.getFmUrl())
                .putString(
                        MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI,
                        fmStation.getFmIconUrl())
                .build();
    }

    /***
     *
     * @param context
     */
    public static void playTrack(@NonNull Context context) {
        Intent intent = new Intent(FmConstants.ACTION_PLAY_TRACKS, null, context, RadioInternetService.class);
        context.startService(intent);
    }

    /***
     *
     * @param context
     */
    public static void stopTrack(@NonNull Context context) {
        Intent intent = new Intent(FmConstants.ACTION_STOP_TRACKS, null, context, RadioInternetService.class);
        context.startService(intent);
    }


    /**
     * @param context
     */
    public static void pauseTrack(@NonNull Context context) {
        Intent intent = new Intent(FmConstants.ACTION_PAUSE_TRACKS, null, context, RadioInternetService.class);
        context.startService(intent);
    }
}

