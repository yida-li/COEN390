package com.teamdesign.coen390app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class LogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityHelper.initialize(this);
    }
}