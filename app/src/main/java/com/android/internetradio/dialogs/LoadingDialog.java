package com.android.internetradio.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.android.internetradio.R;

public final class LoadingDialog extends Dialog {

    /**
     * cancelable is false by default
     * @param context
     */
    public LoadingDialog(@NonNull Activity context) {
        super(context, R.style.TransLoader);
        setCancelable(false);
    }

    /**
     * @param context
     * @param isCancelable default is false
     */
    public LoadingDialog(@NonNull Activity context, boolean isCancelable) {
        super(context, R.style.TransLoader);
        setCancelable(isCancelable);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_loader);

    }
}