package com.teamdesign.coen390app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ActivityHelper.initialize(this);
    setContentView(R.layout.activity_detail);
  }
}
