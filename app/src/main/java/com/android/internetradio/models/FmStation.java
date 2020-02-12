package com.android.internetradio.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FmStation implements GsonProguardMarker {

    public boolean isPlaying = false;

    @SerializedName("radio_id")
    @Expose
    protected int radioId;

    @SerializedName("radio_name")
    @Expose
    protected String fmName;
    @SerializedName("radio_url")
    @Expose
    protected String fmUrl;
    @SerializedName("radio_icon")
    @Expose
    protected String fmIconUrl;

    @SerializedName("radio_category_id")
    @Expose
    protected int fmCategoryId;
    @SerializedName("radio_channel_id")
    @Expose
    protected int fmChannelId;

    public String getFmName() {
        return fmName;
    }

    public void setFmName(String fmName) {
        this.fmName = fmName;
    }

    public String getFmUrl() {
        return fmUrl;
    }

    public void setFmUrl(String fmUrl) {
        this.fmUrl = fmUrl;
    }

    public String getFmIconUrl() {
        return fmIconUrl;
    }

    public void setFmIconUrl(String fmIconUrl) {
        this.fmIconUrl = fmIconUrl;
    }

    public int getFmCategoryId() {
        return fmCategoryId;
    }

    public int getFmChannelId() {
        return fmChannelId;
    }

    public void setFmChannelId(int fmChannelId) {
        this.fmChannelId = fmChannelId;
    }

    public void setFmCategoryId(int fmCategoryId) {
        this.fmCategoryId = fmCategoryId;
    }

    public int getRadioId() {
        return radioId;
    }

    public void setRadioId(int radioId) {
        this.radioId = radioId;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    public String toString() {
        return fmName;
    }
}
