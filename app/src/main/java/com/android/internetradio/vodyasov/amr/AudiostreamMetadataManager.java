package com.android.internetradio.vodyasov.amr;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class AudiostreamMetadataManager
{
    public static final String LOG_TAG = AudiostreamMetadataManager.class.getName();

    private static AudiostreamMetadataManager sInstance;

    private Thread mThread;
    private boolean mIsRunning = false;

    private String mUrlString;
    private OnNewMetadataListener mListener;
    private String mUserAgent;

    private AudiostreamMetadataManager()
    {
        mUserAgent = UserAgent.VLC.toString();
    }

    public static AudiostreamMetadataManager getInstance()
    {
        if (sInstance == null)
        {
            sInstance = new AudiostreamMetadataManager();
        }
        return sInstance;
    }

    public AudiostreamMetadataManager setUri(String stringUri)
    {
        mUrlString = stringUri;
        return this;
    }

    public AudiostreamMetadataManager setUri(Uri uri)
    {
        mUrlString = uri.toString();
        return this;
    }

    public AudiostreamMetadataManager setUserAgent(String userAgent)
    {
        mUserAgent = userAgent;
        return this;
    }

    public AudiostreamMetadataManager setUserAgent(UserAgent userAgent)
    {
        mUserAgent = userAgent.toString();
        return this;
    }

    public AudiostreamMetadataManager setOnNewMetadataListener(OnNewMetadataListener listener)
    {
        mListener = listener;
        return this;
    }

    public void start()
    {
        if (!TextUtils.isEmpty(mUrlString) && !TextUtils.isEmpty(mUserAgent) && mListener != null)
        {
            //stop previous task if running
            stop();

            mThread = new Thread(new AudiostreamMetadataRetriever(mUrlString, mListener, mUserAgent));
            mThread.start();
            mIsRunning = true;
        }
        else
        {
            Log.e(LOG_TAG, "Error.");
        }
    }

    public void stop()
    {
        if (mIsRunning && mThread != null && !mThread.isInterrupted())
        {
            mThread.interrupt();
            mIsRunning = false;
        }
    }

    public boolean isRunning()
    {
        return mIsRunning;
    }
}
