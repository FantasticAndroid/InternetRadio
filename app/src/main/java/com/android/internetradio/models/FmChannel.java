package com.android.internetradio.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FmChannel implements GsonProguardMarker {
    @SerializedName("radio_channel_name")
    @Expose
    private String fmChannelName;
    @SerializedName("radio_channel_id")
    @Expose
    private int fmChannelId;

    public String getFmChannelName() {
        return fmChannelName;
    }

    public void setFmChannelName(String fmChannelName) {
        this.fmChannelName = fmChannelName;
    }

    public int getFmChannelId() {
        return fmChannelId;
    }

    public void setFmChannelId(int fmChannelId) {
        this.fmChannelId = fmChannelId;
    }

    @Override
    public String toString() {
        return fmChannelName;
    }
}
