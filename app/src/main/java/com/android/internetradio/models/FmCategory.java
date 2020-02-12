package com.android.internetradio.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FmCategory implements GsonProguardMarker {

    @SerializedName("radio_category_name")
    @Expose
    private String fmCatagoryName;
    @SerializedName("radio_category_id")
    @Expose
    private int fmCategoryId;

    @SerializedName("radio_channel_id")
    @Expose
    private int fmChannelId;

    public String getFmCatagoryName() {
        return fmCatagoryName;
    }

    public void setFmCatagoryName(String fmCatagoryName) {
        this.fmCatagoryName = fmCatagoryName;
    }

    public int getFmCategoryId() {
        return fmCategoryId;
    }

    public void setFmCategoryId(int fmCategoryId) {
        this.fmCategoryId = fmCategoryId;
    }

    public int getFmChannelId() {
        return fmChannelId;
    }

    @Override
    public String toString() {
        return fmCatagoryName;
    }
}
