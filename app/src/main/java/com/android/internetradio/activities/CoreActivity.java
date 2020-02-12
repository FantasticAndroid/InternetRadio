package com.android.internetradio.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.internetradio.RadioApp;

public class CoreActivity extends AppCompatActivity {

    protected RadioApp dbApplication;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbApplication = (RadioApp) getApplication();
    }
}
