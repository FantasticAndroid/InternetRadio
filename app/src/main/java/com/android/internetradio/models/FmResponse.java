package com.android.internetradio.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FmResponse  implements GsonProguardMarker {

    @SerializedName("data")
    @Expose
    private FmModel fmModel;
    @SerializedName("code")
    @Expose
    private int code;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("time")
    @Expose
    private long time;

    public FmModel getFmModel() {
        return fmModel;
    }

    public void setFmModel(FmModel fmModel) {
        this.fmModel = fmModel;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public long getTime() {
        return time;
    }
}

