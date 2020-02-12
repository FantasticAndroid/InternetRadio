package com.android.internetradio.helpers;

import android.util.Log;

import com.android.internetradio.utils.CommonUtils;
import com.android.internetradio.vodyasov.amr.AudiostreamMetadataManager;
import com.android.internetradio.vodyasov.amr.OnNewMetadataListener;
import com.android.internetradio.vodyasov.amr.UserAgent;

import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

public class FMMetaDataProvider {
    private FMMetaDataListener fmMetaDataListener;

    public interface FMMetaDataListener {
        void onFMMetaDataFound(String title, String artis, String album);

        void onErrorFound(Exception e);
    }

    /**
     * @param fmMetaDataListener
     */
    public FMMetaDataProvider(FMMetaDataListener fmMetaDataListener) {
        this.fmMetaDataListener = fmMetaDataListener;
    }

    /**
     * @param fmUrl
     */
    public void init(String fmUrl) {
        //Start parsing
        AudiostreamMetadataManager.getInstance()
                .setUri(fmUrl)
                .setOnNewMetadataListener(listener)
                .setUserAgent(UserAgent.WINDOWS_MEDIA_PLAYER)
                .start();
    }

    private final OnNewMetadataListener listener = new OnNewMetadataListener() {
        @Override
        public void onNewHeaders(String stringUri, List<String> name, List<String> desc, List<String> br, List<String> genre, List<String> info) {

        }

        @Override
        public void onErrorFound(Exception e) {

            if (null != fmMetaDataListener) {
                fmMetaDataListener.onErrorFound(e);
            }
        }

        @Override
        public void onNewStreamTitle(String stringUri, String streamTitle) {
            Log.d("StreamTitle", streamTitle);
            String title = null, artist = null, album = null;
            if (streamTitle != null) {
                try {
                    Map<String, String> queryParams = CommonUtils.getIdenticalQueryParameters(stringUri + "?" + streamTitle);

                    if (queryParams == null || queryParams.isEmpty()) {
                        title = URLDecoder.decode(streamTitle, "UTF-8");
                    } else {
                        Log.d("StreamTitle", streamTitle);
                        title = queryParams.get("title");
                        artist = queryParams.get("artist");
                        album = queryParams.get("album");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    title = streamTitle;
                }
            }
            if (null != fmMetaDataListener) {
                fmMetaDataListener.onFMMetaDataFound(title, artist, album);
            }
        }
    };
}
