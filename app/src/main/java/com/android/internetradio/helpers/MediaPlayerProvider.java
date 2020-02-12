package com.android.internetradio.helpers;

import android.content.Context;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.internetradio.services.RadioInternetService;

import java.util.List;

public class MediaPlayerProvider {

    private MediaBrowserHelper mMediaBrowserHelper;
    private boolean mIsPlaying;
    private OnUIInteractionListener uiInteractionListener;
    private final String TAG = "MediaPlayerProvider";

    public interface OnUIInteractionListener {
        void onPlayStateChanged(int playerState);

        Context getUiContext();

        ////void onMediaMetaDataFound(String title, String artist, String mediaId);
    }

    /***
     *
     * @param listener
     */
    public MediaPlayerProvider(@NonNull OnUIInteractionListener listener) {
        this.uiInteractionListener = listener;
    }

    public void init() {
        if (mMediaBrowserHelper == null) {
            mMediaBrowserHelper = new MediaBrowserConnection(uiInteractionListener.getUiContext());
            mMediaBrowserHelper.registerCallback(new MediaBrowserListener());
        }
    }

    private class MediaBrowserConnection extends MediaBrowserHelper {
        private MediaBrowserConnection(Context context) {
            super(context, RadioInternetService.class);
        }

        @Override
        protected void onConnected(@NonNull MediaControllerCompat mediaController) {
            ////////mSeekBarAudio.setMediaController(mediaController);
            // Connected From Service
            ////////////////////Toast.makeText(uiInteractionListener.getUiContext(), TAG + " onConnected", Toast.LENGTH_LONG).show();
            onProviderStart();
        }

        @Override
        protected void onChildrenLoaded(@NonNull String parentId,
                                        @NonNull List<MediaBrowserCompat.MediaItem> children) {
            super.onChildrenLoaded(parentId, children);

            try {
                final MediaControllerCompat mediaController = getMediaController();

                // Queue up all media items for this simple sample.
                for (final MediaBrowserCompat.MediaItem mediaItem : children) {
                    try {
                        mediaController.addQueueItem(mediaItem.getDescription());
                    } catch (Exception e) {
                        Log.e(TAG, "onChildrenLoaded: " + e.getMessage());
                    }
                }
                if (null != mediaController.getTransportControls()) {
                    // Call prepare now so pressing play just works.
                    mediaController.getTransportControls().prepare();
                }
            } catch (Exception e) {
                Log.e(TAG, "onChildrenLoaded: " + e.getMessage());
            }
        }
    }

    /**
     * Implementation of the {@link MediaControllerCompat.Callback} methods we're interested in.
     * <p>
     * Here would also be where one could override
     * {@code onQueueChanged(List<MediaSessionCompat.QueueItem> queue)} to get informed when items
     * are added or removed from the queue. We don't do this here in order to keep the UI
     * simple.
     */
    private class MediaBrowserListener extends MediaControllerCompat.Callback {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat playbackState) {

            mIsPlaying = playbackState != null &&
                    playbackState.getState() == PlaybackStateCompat.STATE_PLAYING;

            if (playbackState != null)
                uiInteractionListener.onPlayStateChanged(playbackState.getState());
        }

        /*@Override
        public void onMetadataChanged(MediaMetadataCompat mediaMetadata) {
            if (mediaMetadata == null) {
                return;
            }
            *//*uiInteractionListener.onMediaMetaDataFound(mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE),
                    mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST), mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));*//*

         *//*mTitleTextView.setText(
                    mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
            mArtistTextView.setText(
                    mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
            mAlbumArt.setImageBitmap(MusicLibrary.getAlbumBitmap(
                    getActivity(),
                    mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)));*//*
        }*/

        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
        }

        @Override
        public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
            super.onQueueChanged(queue);
        }

       /* @Override
        public void onSessionReady() {
            super.onSessionReady();

        }*/
    }

    public void onErrorFoundInFmStream() {
        if (null != mMediaBrowserHelper && null != mMediaBrowserHelper.getTransportControls()) {
            mMediaBrowserHelper.getTransportControls().pause();
        }
    }

    public void onPlayBtnPressed() {
        try {
            if (mMediaBrowserHelper == null) {
                init();
            }

            if (null != mMediaBrowserHelper && null != mMediaBrowserHelper.getTransportControls()) {
                if (mIsPlaying) {
                    mMediaBrowserHelper.getTransportControls().pause();
                } else {
                    mMediaBrowserHelper.getTransportControls().play();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playFmStation() throws Exception {
        if (mMediaBrowserHelper == null) {
            init();
        }
        if (null != mMediaBrowserHelper && null != mMediaBrowserHelper.getTransportControls()) {
            mMediaBrowserHelper.getTransportControls().play();
        }
    }

    public void onProviderStart() {
        if (mMediaBrowserHelper != null)
            mMediaBrowserHelper.onStart();
    }

    public void onProviderStop() {
        if (mMediaBrowserHelper != null)
            mMediaBrowserHelper.onStop();
    }

    /*private class ClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (null != mMediaBrowserHelper && null != mMediaBrowserHelper.getTransportControls()) {
                switch (v.getArticleId()) {
                    *//*case R.id.button_previous:
                        mMediaBrowserHelper.getTransportControls().skipToPrevious();
                        break;*//*
                        case R.id.ibtn_play:
                        if (mIsPlaying) {
                            mMediaBrowserHelper.getTransportControls().pause();
                        } else {
                            mMediaBrowserHelper.getTransportControls().play();
                        }
                        break;
                    *//*case R.id.button_next:
                        mMediaBrowserHelper.getTransportControls().skipToNext();
                        break;*//*
                }
            }
        }
    }*/
}
