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
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Integer;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class UserSimpleActivity extends Activity {

  // initializing sqlite database manager
  DatabaseHelper mDatabaseHelper;

  private Button historyButton;
  public int cnt = 0;

  // intially default mode
  protected boolean UserSelectAlertType = true;
  protected boolean UserSelectFanType = true;
  protected boolean UserSelectFanManual = true;

  private ToggleButton toggleAlertButton;
  private ToggleButton toggleFanButton;
  private ToggleButton FanButton;

  // Notification manager initialization
  private NotificationManagerCompat notificationManager;

  // maximum threshold value
  private int imax = 0;
  int tempVal = imax;

  private boolean graphflag = true;
  private boolean fanThresholdFlag = true;

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
  private ScrollView scrollView;
  private CheckBox chkScroll;
  private CheckBox chkReceiveText;
  private Button DetailPageLog;
  private Button showGraph;

  // switch to turn the fan and sensor off
  private Switch sensSwitch, fanSwitch;
  GraphView airQGraphView;
  private LineGraphSeries<DataPoint> airQSeries;
  private int airQLastVal = 0;
  NumberStorage numhelper;

  private TextView AirQRead;

  public UserSimpleActivity() {}

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_user_simple);
    numhelper = new NumberStorage(this);
    AirQRead = findViewById(R.id.AirQRead);
    airQGraphView = findViewById(R.id.airQGraphView);
    airQSeries = new LineGraphSeries<DataPoint>();
    airQGraphView.addSeries(airQSeries);
    Viewport viewport1 = airQGraphView.getViewport();
    viewport1.setYAxisBoundsManual(true);
    viewport1.setMinY(350);
    viewport1.setMaxY(1000);
    viewport1.setBorderColor(R.color.purple_200);
    viewport1.setScrollable(true);
    airQGraphView.setTitle("Graph of Smoke in PPM");
    airQGraphView.setTitleTextSize(80);
    airQGraphView.setTitleColor(R.color.purple_200);
    GridLabelRenderer gridLabel1 = airQGraphView.getGridLabelRenderer();
    gridLabel1.setHorizontalAxisTitle("Live Sensor Readings");
    gridLabel1.setVerticalAxisTitle("CO2 in PPM");

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
    notificationText = (TextView) findViewById(R.id.notificationText);
    mTxtReceive = (TextView) findViewById(R.id.txtReceive);
    chkScroll = (CheckBox) findViewById(R.id.chkScroll);
    chkReceiveText = (CheckBox) findViewById(R.id.chkReceiveText);
    scrollView = (ScrollView) findViewById(R.id.viewScroll);
    mDatabaseHelper = new DatabaseHelper(this);
    DetailPageLog = findViewById(R.id.DetailPageLog);
    showGraph = findViewById(R.id.showGraph);
    mTxtReceive.setMovementMethod(new ScrollingMovementMethod());
    notificationManager = NotificationManagerCompat.from(this);
    historyButton = (Button) findViewById(R.id.historyButton);

    DetailPageLog.setOnClickListener(
      new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          Intent intent = new Intent(
            getApplicationContext(),
            DetailLogActivity.class
          );
          startActivity(intent);
        }
      }
    );

    showGraph.setOnClickListener(
      new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          graphflag = true;
          Intent intent = new Intent(
            getApplicationContext(),
            GraphActivity.class
          );
          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          startActivity(intent);
        }
      }
    );

    historyButton.setOnClickListener(
      new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          Intent intent = new Intent(
            getApplicationContext(),
            ListDataActivity.class
          );
          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

          startActivity(intent);
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
    private int AirQ;

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
                    if (cnt == 0) {
                      mTxtReceive.append("");
                      mTxtReceive.append("\n");
                      cnt++;
                    }

                    String firstInt = strInput.replaceFirst(
                      ".*?(\\d+).*",
                      "$1"
                    );
                    int i;
                    try {
                      i = Integer.parseInt(firstInt.trim());
                      AirQ = Integer.parseInt(firstInt.trim());
                      airQSeries.appendData(
                        new DataPoint(airQLastVal++, i),
                        false,
                        1000
                      );
                      runOnUiThread(
                        new Runnable() {
                          @Override
                          public void run() {
                            AirQRead.setText(String.valueOf(AirQ));
                          }
                        }
                      );
                      if (imax < i) {
                        imax = i;
                      }

                      if (i >= 500) {
                        if (i >= 700) {
                          numhelper.addNumber(i);
                          graphflag = false;
                        } else if (graphflag = false) {
                          numhelper.addNumber(i);
                        }
                      }
                    } catch (NumberFormatException e) {
                      i = 501;
                    }

                    if (i >= 700 && fanThresholdFlag == true) {

                      Log.d(TAG, "num helper");
                      if (counter > 1) {
                        notificationText.setText(
                          " Air threshold is reached!" + imax
                        );

                        mTxtReceive.append("         Air particles detected !");
                        mTxtReceive.append("\n");

                        running = true;
                        counter--;

                        fanThresholdFlag = false;
                      }
                      AutoturnOnFan();
                    } else {

                      if (
                        i <= 500 && seconds != 0 && fanThresholdFlag == false
                      ) {
                        AutoturnOffFan();

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
                        running = false;
                        mTxtReceive.append(
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
                        seconds = 0;
                        imax = 0;
                        tempVal = 0;
                        fanThresholdFlag = true;
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

  private void toastMessage(String message) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
  }

  public void AddData(String newEntry) {
    boolean insertData = mDatabaseHelper.addData(newEntry);

    if (insertData) {
      toastMessage("Data Registered");
    } else {
      toastMessage("Something went wrong");
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

  /*
    Sends Alert to user even if application is in background
  */

  public void sendAlertOption() {
    if (!UserSelectAlertType) {
      sendAlert1();
    } else sendAlert2();
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
      UserSimpleActivity.this,
      CHANNEL_1_ID
    )
      .setSmallIcon(R.drawable.ic_notification2)
      .setContentTitle("Smoke Particles Detected!")
      .setContentText("Vibrate Mode Testing")
      .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setCategory(NotificationCompat.CATEGORY_MESSAGE)
      .setColor(getResources().getColor(R.color.purple_700))
      .build();

    notificationManager.notify(1, notification);
  }

  public void sendAlert2() {
    Notification notification = new NotificationCompat.Builder(
      UserSimpleActivity.this,
      CHANNEL_2_ID
    )
      .setSmallIcon(R.drawable.ic_notification1)
      .setContentTitle("Kraken HyperFan Released!")
      .setContentText("Silent Mode Testing!")
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setCategory(NotificationCompat.CATEGORY_MESSAGE)
      .setColor(getResources().getColor(R.color.teal_700))
      .build();

    notificationManager.notify(2, notification);
  }

  private void Disconnect() {
    if (
      mBTSocket != null
    ) { /
      try {
        mBTSocket.close(); 
      } catch (IOException e) {
        msg("Error");
      }
    }
    finish();
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

  private void turnOnSensor() {
    if (mBTSocket != null) {
      try {
        mBTSocket.getOutputStream().write("SO".toString().getBytes());
      } catch (IOException e) {
        msg("Error");
      }
    }
  }

  private void turnOffSensor() {
    if (mBTSocket != null) {
      try {
        mBTSocket.getOutputStream().write("SF".toString().getBytes());
      } catch (IOException e) {
        msg("Error");
      }
    }
  }

  private void msg(String s) {
    Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
  }

  @Override
  protected void onPause() {
    if (mBTSocket != null && mIsBluetoothConnected) {
      new DisConnectBT().execute();
    }
    super.onPause();
    if (running == true && wasRunning == true) running = false;
  }

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
        ProgressDialog.show(UserSimpleActivity.this, "Hold on", "Connecting");
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
        mReadThread = new ReadInput(); 
      }
      progressDialog.dismiss();
    }
  }

  //Disconnect aysynchronously to blueooth
  private class DisConnectBT extends AsyncTask<Void, Void, Void> {

    @Override
    protected void onPreExecute() {}

    @Override
    protected Void doInBackground(Void... params) {
      if (mReadThread != null) {
        mReadThread.stop();
        while (mReadThread.isRunning()); 
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
      if (mIsUserInitiatedDisconnect) {
        finish();
      }
    }
  }

  public void onClickStart(View view) {
    running = true;
  }

  public void onClickStop(View view) {
    wasRunning = false;
    running = false;
  }

  public void onClickReset(View view) {
    running = false;
    seconds = 0;
  }

  private void runTimer() {
    final TextView timeView = (TextView) findViewById(R.id.time_view);
    final Handler handler = new Handler();
    handler.post(
      new Runnable() {
        @Override
        public void run() {
          int hours = seconds / 3600;
          int minutes = (seconds % 3600) / 60;
          int secs = seconds % 60;
          String time = String.format(
            Locale.getDefault(),
            "%d:%02d:%02d",
            hours,
            minutes,
            secs
          );
          timeView.setText(time);
          if (running) {
            seconds++;
          }
          handler.postDelayed(this, 1000);
        }
      }
    );
  }
}
