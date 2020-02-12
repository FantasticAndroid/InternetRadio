package com.android.internetradio.vodyasov.amr;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.util.List;

class RetrieverHandler extends Handler
{
    public static final int ACTION_METADATA = 0;
    public static final int ACTION_HEADERS = 1;

    private WeakReference<OnNewMetadataListener> ref;
    private String mUrlString;

    /**
     *
     * @param uri
     * @param listener
     */
    public RetrieverHandler(String uri, OnNewMetadataListener listener)
    {
        mUrlString = uri;
        ref = new WeakReference<OnNewMetadataListener>(listener);
    }

    @Override
    public void handleMessage(Message msg)
    {
        super.handleMessage(msg);

        OnNewMetadataListener listener = ref.get();
        if (listener == null)
        {
            return;
        }
        switch (msg.what)
        {
            case(ACTION_METADATA):
            {
                String streamTitle = (String) msg.obj;
                listener.onNewStreamTitle(mUrlString, streamTitle);
                break;
            }
            case(ACTION_HEADERS):
            {
                Bundle data = msg.getData();
                List<String> name = data.getStringArrayList(IcecastHeader.NAME);
                List<String> desc = data.getStringArrayList(IcecastHeader.DESC);
                List<String> br = data.getStringArrayList(IcecastHeader.BR);
                List<String> genre = data.getStringArrayList(IcecastHeader.GENRE);
                List<String> info = data.getStringArrayList(IcecastHeader.INFO);
                listener.onNewHeaders(mUrlString, name, desc, br, genre, info);
                break;
            }
        }
    }
}
