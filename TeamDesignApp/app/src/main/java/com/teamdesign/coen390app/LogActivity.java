package com.teamdesign.coen390app;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.teamdesign.coen390app.UserActivity.timeView;

public class LogActivity extends AppCompatActivity {
    public static ListView listView;
    public static List list;
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_page);

        listView = (ListView) findViewById(R.id.listview);
         //creating the data array have all data from user activity
        list = new ArrayList<String>();
        //establish the date code
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd / mm / yyyy");
        String td = dateFormat.format(date);
         //filling up the list
        list.add("Date : ".concat(td).concat("\n").concat("Duration : ".concat(timeView.getText().toString()).concat("\n").concat("Sensor Value : ".concat(String.valueOf(UserActivity.r)))));
        //list.add("Date : ".concat(td));
        //converting ArrayList to data that can be shown
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, list);
        LogActivity.listView.setAdapter(arrayAdapter);

    }
}