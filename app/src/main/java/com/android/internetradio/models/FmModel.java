package com.android.internetradio.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FmModel implements GsonProguardMarker {

    @SerializedName("FmRadio")
    @Expose
    private List<FmStation> fmStation = null;

    @SerializedName("radio_channels")
    @Expose
    private List<FmChannel> fmChannels = null;

    @SerializedName("radio_category")
    @Expose
    private List<FmCategory> fmCategory = null;

    public List<FmStation> getFmStation() {
        return fmStation;
    }

    public void setFmStation(List<FmStation> fmStation) {
        this.fmStation = fmStation;
    }

    public List<FmChannel> getFmChannels() {
        return fmChannels;
    }

    public void setFmChannels(List<FmChannel> fmChannels) {
        this.fmChannels = fmChannels;
    }

    public List<FmCategory> getFmCategories() {
        return fmCategory;
    }

    public void setFmCategory(List<FmCategory> fmCategory) {
        this.fmCategory = fmCategory;
    }
}
