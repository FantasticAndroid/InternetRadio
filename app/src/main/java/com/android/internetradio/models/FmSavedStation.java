package com.android.internetradio.models;

public class FmSavedStation extends FmStation {

    private String fmChannelName;
    private String fmCategoryName;

    public String getFmChannelName() {
        return fmChannelName;
    }

    public String getFmCategoryName() {
        return fmCategoryName;
    }

    public void setFmChannelName(String fmChannelName) {
        this.fmChannelName = fmChannelName;
    }

    public void setFmCategoryName(String fmCategoryName) {
        this.fmCategoryName = fmCategoryName;
    }
}
