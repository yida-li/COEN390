package com.teamdesign.coen390app;

import static com.teamdesign.coen390app.NotificationActivity.CHANNEL_1_ID;
import static com.teamdesign.coen390app.NotificationActivity.CHANNEL_2_ID;
import static com.teamdesign.coen390app.NotificationActivity.CHANNEL_3_ID;

import android.app.Activity;
import android.app.Notification;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Integer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class UserActivity extends Activity {

  private boolean Logflag = false;

  // initializing sqlite database manager
  DatabaseHelper mDatabaseHelper;

  // intially default mode
  protected boolean UserSelectAlertType = true;
  protected boolean UserSelectFanType = true;
  protected boolean UserSelectFanManual = true;

  private ToggleButton toggleAlertButton;
  private ToggleButton toggleFanButton;
  private ToggleButton FanButton;

  // Notification manager initialization
  private NotificationManagerCompat notificationManager;

  // THRESHOLDS
  private int HighThresh = 700;
  private int LowThresh = 500;

  // maximum sensor value
  private int imax = 0;

  private int count = 0;

  // Integer value used for time system
  private int seconds = 0;

  // Integer counter used manipulate the threshold Notification
  public int counter = 10;

  // TextView layout to display a notification if Air PPM reach a certain threshold
  private TextView notificationText;

  // Boolean variable used for timer
  private boolean running;
  private boolean wasRunning;

  // Variable Initialization for blueooth module
  private boolean mIsUserInitiatedDisconnect = false;
  private boolean mIsBluetoothConnected = false;
  private BluetoothDevice mDevice;
  private ProgressDialog progressDialog;
  private int mMaxChars = 50000;
  private UUID mDeviceUUID;
  private BluetoothSocket mBTSocket;
  private ReadInput mReadThread = null;

  // Widgets for Main screen layout
  private TextView mTxtReceive;
  private Button mBtnClearInput;
  private Button logButton;
  private Button detailButton;
  private ScrollView scrollView;
  private CheckBox chkScroll;
  private CheckBox chkReceiveText;

  // Widgets Sensitivity Buttons
  private Button SensLow;
  private Button SensNormal;
  private Button SensHigh;

  // switch to turn the fan and sensor off
  private Switch sensSwitch, fanSwitch;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_user_activity);
    ActivityHelper.initialize(this);
    if (savedInstanceState != null) {
      seconds = savedInstanceState.getInt("seconds");
      running = savedInstanceState.getBoolean("running");
      wasRunning = savedInstanceState.getBoolean("wasRunning");
    }
    runTimer();
    Intent intent = getIntent();
    Bundle b = intent.getExtras();
    mDevice = b.getParcelable(MainActivity.DEVICE_EXTRA);
    mDeviceUUID = UUID.fromString(b.getString(MainActivity.DEVICE_UUID));
    mMaxChars = b.getInt(MainActivity.BUFFER_SIZE);
    notificationText = findViewById(R.id.notificationText);
    mTxtReceive = (TextView) findViewById(R.id.txtReceive);
    chkScroll = (CheckBox) findViewById(R.id.chkScroll);
    chkReceiveText = (CheckBox) findViewById(R.id.chkReceiveText);
    scrollView = (ScrollView) findViewById(R.id.viewScroll);
    mBtnClearInput = (Button) findViewById(R.id.btnClearInput);
    mTxtReceive.setMovementMethod(new ScrollingMovementMethod());
    detailButton = (Button) findViewById(R.id.detailButton);
    toggleAlertButton = (ToggleButton) findViewById(R.id.toggleAlertButton);
    toggleFanButton = (ToggleButton) findViewById(R.id.toggleFanButton);
    FanButton = (ToggleButton) findViewById(R.id.toggleButtonMan);
    SensLow = findViewById(R.id.SensLow);
    SensNormal = findViewById(R.id.SensNormal);
    SensHigh = findViewById(R.id.SensHigh);
    mDatabaseHelper = new DatabaseHelper(this);

    FanButton.setEnabled(false);
    SensLow.setOnClickListener(
      new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          HighThresh = 850;
          LowThresh = 500;
          Toast
            .makeText(
              UserActivity.this,
              "Changed to Low Sensitivity (850 PPM)",
              Toast.LENGTH_SHORT
            )
            .show();
          CallSensLow();
        }
      }
    );

    SensNormal.setOnClickListener(
      new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          HighThresh = 700;
          LowThresh = 500;
          Toast
            .makeText(
              UserActivity.this,
              "Changed to Normal Sensitivity (700 PPM)",
              Toast.LENGTH_SHORT
            )
            .show();
          CallSensNormal();
        }
      }
    );

    SensHigh.setOnClickListener(
      new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          HighThresh = 600;
          LowThresh = 500;
          Toast
            .makeText(
              UserActivity.this,
              "Changed to High Sensitivity (600 PPM)",
              Toast.LENGTH_SHORT
            )
            .show();
          CallSensHigh();
        }
      }
    );

    toggleAlertButton.setOnCheckedChangeListener(
      (buttonView, isChecked) -> {
        if (isChecked) {
          UserSelectAlertType = false;
          Toast
            .makeText(
              UserActivity.this,
              "Vibrate Mode Enabled",
              Toast.LENGTH_SHORT
            )
            .show();
        } else {
          UserSelectAlertType = true;
          Toast
            .makeText(
              UserActivity.this,
              "Silent Mode Enabled",
              Toast.LENGTH_SHORT
            )
            .show();
        }
      }
    );

    toggleFanButton.setOnCheckedChangeListener(
      (buttonView, isChecked) -> {
        if (isChecked) {
          UserSelectFanType = false;
          ManualMode();
          FanButton.setEnabled(true);
          Toast
            .makeText(
              UserActivity.this,
              "Enabled Manual Mode",
              Toast.LENGTH_SHORT
            )
            .show();
        } else {
          UserSelectFanType = true;
          AutoMode();
          FanButton.setEnabled(false);
          Toast
            .makeText(
              UserActivity.this,
              "Enabled Auto Mode",
              Toast.LENGTH_SHORT
            )
            .show();
        }
      }
    );

    FanButton.setOnCheckedChangeListener(
      (buttonView, isChecked) -> {
        if (isChecked && UserSelectFanType == false) {
          turnOffFan();
        } else if (!isChecked && UserSelectFanType == false) {
          turnOnFan();
        }
      }
    );

    detailButton.setOnClickListener(
      new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          Intent intent = new Intent(
            getApplicationContext(),
            DetailActivity.class
          );
          startActivity(intent);
        }
      }
    );

    notificationManager = NotificationManagerCompat.from(this);

    mBtnClearInput.setOnClickListener(
      new OnClickListener() {
        @Override
        public void onClick(View arg0) {
          mTxtReceive.setText("");
          notificationText.setText("");
          seconds = 0;
        }
      }
    );
  }

  /*
    Thread based child-0process datapath to constantly uplaod stream of
     */
  private class ReadInput implements Runnable {

    private boolean bStop = false;
    private Thread t;

    public ReadInput() {
      t = new Thread(this, "Input Thread");
      t.start();
    }

    public boolean isRunning() {
      return t.isAlive();
    }

    @Override
    public void run() {
      InputStream inputStream;

      try {
        inputStream = mBTSocket.getInputStream();
        while (!bStop) {
          byte[] buffer = new byte[256];
          if (inputStream.available() > 0) {
            inputStream.read(buffer);
            int i = 0;
            for (i = 0; i < buffer.length && buffer[i] != 0; i++) {}
            final String strInput = new String(buffer, 0, i);
            if (chkReceiveText.isChecked()) {
              mTxtReceive.post(
                new Runnable() {
                  @Override
                  public void run() {
                    mTxtReceive.append(strInput);

                    String firstInt = strInput.replaceFirst(
                      ".*?(\\d+).*",
                      "$1"
                    );
                    int i;
                    try {
                      i = Integer.parseInt(firstInt.trim());
                      if (imax < i) {
                        imax = i;
                      }
                    } catch (NumberFormatException e) {
                      i = LowThresh + 1;
                    }

                    if (i >= HighThresh && UserSelectFanType == true) {
                      notificationText.setText(
                        "Air threshold is reached! " + imax
                      );
                      sendAlertOption();
                      if (counter > 1) {
                        running = true;
                        counter--;
                      }
                      Logflag = true;
                    } else {
                      if (
                        i <= 500 &&
                        seconds != 0 &&
                        UserSelectFanType == true &&
                        Logflag == true
                      ) {
                        //	AutoturnOffFan();
                        running = false;
                        String currentTime = new SimpleDateFormat(
                          "HH:mm:ss",
                          Locale.getDefault()
                        )
                        .format(new Date());
                        Date date = new Date();
                        SimpleDateFormat dateFormat = new SimpleDateFormat(
                          "dd / 04 / yyyy"
                        );
                        String td = dateFormat.format(date);

                        AddData(
                          "         " +
                          td +
                          " " +
                          currentTime +
                          "\n" +
                          "         Smoke in PPM " +
                          imax +
                          "\n" +
                          "         Fan Duration: " +
                          seconds +
                          " Seconds\n\n"
                        );

                        imax = 0;
                        Logflag = false;
                      }
                    }

                    int txtLength = mTxtReceive.getEditableText().length();
                    if (txtLength > mMaxChars) {
                      mTxtReceive
                        .getEditableText()
                        .delete(0, txtLength - mMaxChars);
                    }

                    if (chkScroll.isChecked()) {
                      scrollView.post(
                        new Runnable() {
                          @Override
                          public void run() {
                            scrollView.fullScroll(View.FOCUS_DOWN);
                          }
                        }
                      );
                    }
                  }
                }
              );
            }
          }
          Thread.sleep(500);
        }
      } catch (IOException e) {
        e.printStackTrace();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    public void stop() {
      bStop = true;
    }
  }

  //Send to Arduino New Thresholds:

  public void CallSensLow() {
    if (mBTSocket != null) {
      try {
        mBTSocket.getOutputStream().write("SL".toString().getBytes());
      } catch (IOException e) {
        msg("Error");
      }
    }
  }

  public void CallSensNormal() {
    if (mBTSocket != null) {
      try {
        mBTSocket.getOutputStream().write("SN".toString().getBytes());
      } catch (IOException e) {
        msg("Error");
      }
    }
  }

  public void CallSensHigh() {
    if (mBTSocket != null) {
      try {
        mBTSocket.getOutputStream().write("SH".toString().getBytes());
      } catch (IOException e) {
        msg("Error");
      }
    }
  }

  public void sendAlertOption() {
    if (!UserSelectAlertType) {
      sendAlert1();
    } else {
      sendAlert2();
    }
  }

  private void AutoMode() {
    if (mBTSocket != null) {
      try {
        mBTSocket.getOutputStream().write("AM".toString().getBytes());
      } catch (IOException e) {
        msg("Error");
      }
    }
  }

  private void ManualMode() {
    if (mBTSocket != null) {
      try {
        mBTSocket.getOutputStream().write("AMO".toString().getBytes());
      } catch (IOException e) {
        msg("Error");
      }
    }
  }

  private void AutoturnOffFan() {
    if (mBTSocket != null) {
      try {
        mBTSocket.getOutputStream().write("AF".toString().getBytes());
      } catch (IOException e) {
        msg("Error");
      }
    }
  }

  private void AutoturnOnFan() {
    if (mBTSocket != null) {
      try {
        mBTSocket.getOutputStream().write("AO".toString().getBytes());
      } catch (IOException e) {
        msg("Error");
      }
    }
  }

  public void sendAlert1() {
    Notification notification = new NotificationCompat.Builder(
      UserActivity.this,
      CHANNEL_1_ID
    )
      .setSmallIcon(R.drawable.ic_notification2)
      .setContentTitle("Smoke Particles Detected!")
      .setContentText("Vibrate Mode!")
      .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setCategory(NotificationCompat.CATEGORY_MESSAGE)
      .setColor(getResources().getColor(R.color.purple_700))
      .build();

    notificationManager.notify(1, notification);
  }

  public void sendAlert2() {
    Notification notification = new NotificationCompat.Builder(
      UserActivity.this,
      CHANNEL_2_ID
    )
      .setSmallIcon(R.drawable.ic_notification1)
      .setContentTitle("Smoke Particles Detected!")
      .setContentText("Silent Mode!")
      //.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setCategory(NotificationCompat.CATEGORY_MESSAGE)
      .setColor(getResources().getColor(R.color.teal_700))
      .build();

    notificationManager.notify(2, notification);
  }

  private void turnOffFan() {
    if (mBTSocket != null) {
      try {
        mBTSocket.getOutputStream().write("TF".toString().getBytes());
      } catch (IOException e) {
        msg("Error");
      }
    }
  }

  private void turnOnFan() {
    if (mBTSocket != null) {
      try {
        mBTSocket.getOutputStream().write("TO".toString().getBytes());
      } catch (IOException e) {
        msg("Error");
      }
    }
  }

  private void msg(String s) {
    Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
  }

  public void AddData(String newEntry) {
    boolean insertData = mDatabaseHelper.addData(newEntry);
  }

  @Override
  protected void onPause() {
    if (mBTSocket != null && mIsBluetoothConnected) {
      new DisConnectBT().execute();
    }
    super.onPause();
    if (running == true && wasRunning == true) running = false;
  }

  // If the activity is resumed,start the stopwatch again if it was running previously.
  @Override
  protected void onResume() {
    if (mBTSocket == null || !mIsBluetoothConnected) {
      new ConnectBT().execute();
    }
    super.onResume();
    if (wasRunning) {
      running = true;
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
  }

  @Override
  public void onSaveInstanceState(Bundle savedInstanceState) {
    savedInstanceState.putInt("seconds", seconds);
    savedInstanceState.putBoolean("running", running);
    savedInstanceState.putBoolean("wasRunning", wasRunning);
  }

  // Connect aysynchronously to blueooth
  private class ConnectBT extends AsyncTask<Void, Void, Void> {

    private boolean mConnectSuccessful = true;

    @Override
    protected void onPreExecute() {
      progressDialog =
        ProgressDialog.show(UserActivity.this, "Hold on", "Connecting");
    }

    @Override
    protected Void doInBackground(Void... devices) {
      try {
        if (mBTSocket == null || !mIsBluetoothConnected) {
          mBTSocket =
            mDevice.createInsecureRfcommSocketToServiceRecord(mDeviceUUID);
          BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
          mBTSocket.connect();
        }
      } catch (IOException e) {
        // Unable to connect to device
        e.printStackTrace();
        mConnectSuccessful = false;
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void result) {
      super.onPostExecute(result);

      if (!mConnectSuccessful) {
        Toast
          .makeText(
            getApplicationContext(),
            "Could not connect to device. Is it a Serial device? Also check if the UUID is correct in the settings",
            Toast.LENGTH_LONG
          )
          .show();
        finish();
      } else {
        msg("Connected to device");
        mIsBluetoothConnected = true;
        mReadThread = new ReadInput(); // Kick off input reader
      }
      progressDialog.dismiss();
    }
  }

  //Disconnect aysynchronously to bluetooth

  private class DisConnectBT extends AsyncTask<Void, Void, Void> {

    @Override
    protected void onPreExecute() {}

    @Override
    protected Void doInBackground(Void... params) {
      if (mReadThread != null) {
        mReadThread.stop();
        while (mReadThread.isRunning()); // Wait until it stops
        mReadThread = null;
      }
      try {
        mBTSocket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
      return null;
    }

    @Override
    protected void onPostExecute(Void result) {
      super.onPostExecute(result);
      mIsBluetoothConnected = false;
    }
  }

  // Start the stop watch when the start button is clicked
  public void onClickStart(View view) {
    running = true;
  }

  // Stop the stopwatch when the Stop button is clicked.

  public void onClickStop(View view) {
    wasRunning = false;
    running = false;
  }

  // Reset the stopwatch when the Reset button is clicked.
  public void onClickReset(View view) {
    running = false;
    seconds = 0;
  }

  // Sets the Number of seconds on the timer. The runTimer() method uses a Handler to increment the seconds and update the text view.
  private void runTimer() {
    // Get the text view.
    final TextView timeView = (TextView) findViewById(R.id.time_view);

    // Creates a new Handler
    final Handler handler = new Handler();
    // Call the post() method, passing in a new Runnable.
    // The post() method processes code without a delay,so the code in the Runnable will run almost immediately.

    handler.post(
      new Runnable() {
        @Override
        public void run() {
          int hours = seconds / 3600;
          int minutes = (seconds % 3600) / 60;
          int secs = seconds % 60;
          // Format the seconds into hours, minutes,and seconds

          String time = String.format(
            Locale.getDefault(),
            "%d:%02d:%02d",
            hours,
            minutes,
            secs
          );
          // Set the text view text.
          timeView.setText(time);

          // If running is true, increment the seconds variable
          if (running) {
            seconds++;
          }

          // Post the code again with a delay of 1 second
          handler.postDelayed(this, 1000);
        }
      }
    );
  }
}
