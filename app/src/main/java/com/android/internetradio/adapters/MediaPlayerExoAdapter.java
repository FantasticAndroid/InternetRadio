package com.android.internetradio.adapters;

import android.content.Context;
import android.net.Uri;
import android.os.SystemClock;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.android.internetradio.R;
import com.android.internetradio.activities.InternetRadioActivity;
import com.android.internetradio.helpers.PlaybackInfoListener;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public final class MediaPlayerExoAdapter extends PlayerAdapter {

    private SimpleExoPlayer mPlayer;
    private String mMediaId;
    private PlaybackInfoListener mPlaybackInfoListener;
    private MediaMetadataCompat mCurrentMedia;
    private int mState;
    private static final String TAG = MediaPlayerExoAdapter.class.getSimpleName();
    // Work-around for a MediaPlayer bug related to the behavior of MediaPlayer.seekTo()
    // while not playing.
    private int mSeekWhileNotPlaying = -1;

    private Context context;

    /**
     * @param context
     * @param listener
     */
    public MediaPlayerExoAdapter(Context context, PlaybackInfoListener listener) {
        super(context);
        this.context = context;
        mPlaybackInfoListener = listener;
    }

    /**
     * Once the {@link android.media.MediaPlayer} is released, it can't be used again, and another one has to be
     * created. In the onStop() method of the {@link InternetRadioActivity} the {@link android.media.MediaPlayer} is
     * released. Then in the onStart() of the {@link InternetRadioActivity} a new {@link android.media.MediaPlayer}
     * object has to be created. That's why this method is private, and called by load(int) and
     * not the constructor.
     */
    private void initializeMediaPlayer() {
        mPlayer = ExoPlayerFactory.newSimpleInstance(context);//, renderersFactory, null, new ExoLoadControl());
        mPlayer.setRepeatMode(Player.REPEAT_MODE_OFF);
        mPlayer.addListener(new Player.EventListener() {
        });
        mPlayer.setPlayWhenReady(true);
        setNewState(PlaybackStateCompat.STATE_BUFFERING);

        mPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == Player.STATE_BUFFERING) {
                    setNewState(PlaybackStateCompat.STATE_BUFFERING);
                } else if (playbackState == Player.STATE_READY) {
                    setNewState(PlaybackStateCompat.STATE_PLAYING);
                } else if (playbackState == Player.STATE_ENDED) {
                    mPlaybackInfoListener.onPlaybackCompleted();

                    // Set the state to "paused" because it most closely matches the state
                    // in MediaPlayer with regards to available state transitions compared
                    // to "stop".
                    // Paused allows: seekTo(), start(), pause(), stop()
                    // Stop allows: stop()
                    setNewState(PlaybackStateCompat.STATE_PAUSED);
                }
            }
        });
    }

    // Implements PlaybackControl.
    @Override
    public void playFromMedia(MediaMetadataCompat metadata) {
        mCurrentMedia = metadata;
        String mediaUrl = String.valueOf(metadata.getDescription().getMediaUri());
        String mediaId = String.valueOf(metadata.getDescription().getMediaId());
        playFile(mediaUrl, mediaId);
    }

    @Override
    public MediaMetadataCompat getCurrentMedia() {
        return mCurrentMedia;
    }

    /**
     * @param mediaUrl
     * @param mediaId
     */
    private void playFile(String mediaUrl, String mediaId) {
        boolean mediaChanged = (mMediaId == null || !mediaId.equals(mMediaId));

        if (!mediaChanged) {
            if (!isPlaying()) {
                play();
            }
            return;
        } else {
            release();
        }

        mMediaId = mediaId;

        initializeMediaPlayer();

        try {
            DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(
                    context, Util.getUserAgent(context, context.getString(R.string.app_name)));
            ExtractorMediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(mediaUrl));
            mPlayer.prepare(mediaSource, true, false);
        } catch (Exception e) {
            e.printStackTrace();
            setNewState(PlaybackStateCompat.STATE_ERROR);
            ////throw new RuntimeException("Failed to open FM with FM URL: " + mediaUrl, e);
        }
    }

    /***
     *
     * @param uri
     * @return
     */
    private MediaSource buildMediaSource(Uri uri, DefaultDataSourceFactory dataSourceFactory) {
        @C.ContentType int type = Util.inferContentType(uri);
        switch (type) {
            case C.TYPE_DASH:
                Log.d(TAG, "buildMediaSource: TYPE = TYPE_DASH");
                return new DashMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
            case C.TYPE_SS:
                Log.d(TAG, "buildMediaSource: TYPE = TYPE_SS");
                return new SsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
            case C.TYPE_HLS:
                Log.d(TAG, "buildMediaSource: TYPE = TYPE_HLS");
                return new HlsMediaSource.Factory(dataSourceFactory).setAllowChunklessPreparation(true).createMediaSource(uri);
            case C.TYPE_OTHER:
                Log.d(TAG, "buildMediaSource: TYPE = TYPE_OTHER");
                return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
            default:
                Log.d(TAG, "buildMediaSource: TYPE = Unsupported type" + type);
                throw new IllegalStateException("Unsupported type: " + type);
        }
    }

    @Override
    public void onStop() {
        // Regardless of whether or not the MediaPlayer has been created / started, the state must
        // be updated, so that MediaNotificationManager can take down the notification.
        setNewState(PlaybackStateCompat.STATE_STOPPED);
        release();
    }

    private void release() {
        try {
            if (mPlayer != null) {
                if (isPlaying()) {
                    mPlayer.stop();
                }
                mPlayer.release();
                mPlayer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isPlaying() {
        try {
            return mPlayer != null
                    && mPlayer.getPlaybackState() != Player.STATE_ENDED
                    && mPlayer.getPlaybackState() != Player.STATE_IDLE
                    && mPlayer.getPlayWhenReady();
        } catch (Exception e) {
            e.printStackTrace();
            setNewState(PlaybackStateCompat.STATE_ERROR);
        }
        return false;
    }

    @Override
    protected void onPlay() {
        try {
            if (mPlayer != null && !isPlaying()) {
                startPlayer();
                setNewState(PlaybackStateCompat.STATE_PLAYING);
            }
        } catch (Exception e) {
            e.printStackTrace();
            setNewState(PlaybackStateCompat.STATE_ERROR);
        }
    }

    private void pausePlayer() {
        mPlayer.setPlayWhenReady(false);
        mPlayer.getPlaybackState();
    }

    private void startPlayer() {
        mPlayer.setPlayWhenReady(true);
        mPlayer.getPlaybackState();
    }

    @Override
    protected void onPause() {
        try {
            if (mPlayer != null && isPlaying()) {
                pausePlayer();
                setNewState(PlaybackStateCompat.STATE_PAUSED);
            }
        } catch (Exception e) {
            e.printStackTrace();
            setNewState(PlaybackStateCompat.STATE_ERROR);
        }
    }

    // This is the main reducer for the player state machine.
    private void setNewState(@PlaybackStateCompat.State int newPlayerState) {
        try {
            mState = newPlayerState;

            // Whether playback goes to completion, or whether it is stopped, the
            // mCurrentMediaPlayedToCompletion is set to true.
            if (mState == PlaybackStateCompat.STATE_STOPPED) {
                /// mCurrentMediaPlayedToCompletion = true;
            }

            // Work around for MediaPlayer.getCurrentPosition() when it changes while not playing.
            final long reportPosition;
            if (mSeekWhileNotPlaying >= 0) {
                reportPosition = mSeekWhileNotPlaying;

                if (mState == PlaybackStateCompat.STATE_PLAYING) {
                    mSeekWhileNotPlaying = -1;
                }
            } else {
                reportPosition = mPlayer == null ? 0 : mPlayer.getCurrentPosition();
            }

            final PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder();
            stateBuilder.setActions(getAvailableActions());
            stateBuilder.setState(mState,
                    reportPosition,
                    1.0f,
                    SystemClock.elapsedRealtime());
            mPlaybackInfoListener.onPlaybackStateChange(stateBuilder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Set the current capabilities available on this session. Note: If a capability is not
     * listed in the bitmask of capabilities then the MediaSession will not handle it. For
     * example, if you don't want ACTION_STOP to be handled by the MediaSession, then don't
     * included it in the bitmask that's returned.
     */
    @PlaybackStateCompat.Actions
    private long getAvailableActions() {
        long actions = PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
                | PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
                | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
        switch (mState) {
            case PlaybackStateCompat.STATE_STOPPED:
                actions |= PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_PAUSE;
                break;
            case PlaybackStateCompat.STATE_PLAYING:
                actions |= PlaybackStateCompat.ACTION_STOP
                        | PlaybackStateCompat.ACTION_PAUSE
                        | PlaybackStateCompat.ACTION_SEEK_TO;
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                actions |= PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_STOP;
                break;
            default:
                actions |= PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_PLAY_PAUSE
                        | PlaybackStateCompat.ACTION_STOP
                        | PlaybackStateCompat.ACTION_PAUSE;
        }
        return actions;
    }

    @Override
    public void seekTo(long position) {
        try {
            if (mPlayer != null) {
                if (!isPlaying()) {
                    mSeekWhileNotPlaying = (int) position;
                }
                mPlayer.seekTo((int) position);

                // Set the state (to the current state) because the position changed and should
                // be reported to clients.
                setNewState(mState);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setVolume(float volume) {
        if (mPlayer != null) {
            mPlayer.setVolume(volume);
        }
    }
}
