                                            package com.teamdesign.coen390app;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.UUID;
import java.lang.Integer;
import android.app.Activity;
import android.app.Notification;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.icu.text.UnicodeSetSpanner;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import android.os.Vibrator;
import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.widget.ToggleButton;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import static com.teamdesign.coen390app.NotificationActivity.CHANNEL_1_ID;
import static com.teamdesign.coen390app.NotificationActivity.CHANNEL_2_ID;
import static com.teamdesign.coen390app.NotificationActivity.CHANNEL_3_ID;

public class UserActivity extends Activity {


    // intially default mode
    protected boolean UserSelectAlertType=true;
    protected boolean UserSelectFanType= true;
    protected boolean UserSelectFanManual =true;
    protected boolean autoFlip = false;
    protected boolean humiFlip = true;

    // Notification manager initialization
    private NotificationManagerCompat notificationManager;

    // maximum threshold value
    private int imax = 0;
    private double humiMax = 0;

    // Integer value used for time system
    private int seconds = 0;

    // Integer counter used manipulate the threshold Notification
    public int counter =5;

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
    private static final String TAG = "Main Activity";
    private int mMaxChars = 50000;
    private UUID mDeviceUUID;
    private BluetoothSocket mBTSocket;
    private ReadInput mReadThread = null;

    // Widgets for Main screen layout
    private TextView mTxtReceive;
    private Button mBtnClearInput;
    //private Button logButton;
    //private Button detailButton;
    private ScrollView scrollView;
    private CheckBox chkScroll;
    private CheckBox chkReceiveText;
    private ToggleButton toggleAlertButton;
    private ToggleButton toggleFanButton;
    private ToggleButton FanButton;
    private ToggleButton toggleHumidityButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // setContentView basically connects this instance to the designated xml file
        setContentView(R.layout.activity_user_activity);

        ActivityHelper.initialize(this);
        if (savedInstanceState != null) {
            // Get the previous state of the stopwatch if the activity has been destroyed and recreated.
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
        notificationText =(TextView) findViewById(R.id.notificationText);
        mTxtReceive = (TextView) findViewById(R.id.txtReceive);
        chkScroll = (CheckBox) findViewById(R.id.chkScroll);
        chkReceiveText = (CheckBox) findViewById(R.id.chkReceiveText);
        scrollView = (ScrollView) findViewById(R.id.viewScroll);
        mBtnClearInput = (Button) findViewById(R.id.btnClearInput);
        mTxtReceive.setMovementMethod(new ScrollingMovementMethod());



        toggleAlertButton=(ToggleButton)findViewById(R.id.toggleAlertButton);
        toggleFanButton=(ToggleButton)findViewById(R.id.toggleFanButton);
        FanButton=(ToggleButton)findViewById(R.id.toggleButtonMan);
        toggleHumidityButton=(ToggleButton)findViewById((R.id.toggleButtonHumidity));

        toggleAlertButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {

                UserSelectAlertType=false;
            } else {
                UserSelectAlertType=true;
                // The toggle is disabled
            }
        });

        FanButton.setEnabled(false);
        toggleHumidityButton.setEnabled((false));
        toggleFanButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                UserSelectFanType=false;
                FanButton.setEnabled(true);
                toggleHumidityButton.setEnabled((true));

            } else {
                UserSelectFanType=true;
                FanButton.setEnabled(false);
                FanButton.setChecked(false);
                toggleHumidityButton.setEnabled(false);
                toggleHumidityButton.setChecked(false);
                turnOffFan();
                turnOffHumidity();
            }
        });

        FanButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && UserSelectFanType == false) {
             //   UserSelectFanManual=false;
                turnOnFan();
            } else
            if (!isChecked && UserSelectFanType == false)
            {
               // UserSelectFanManual=true;
                turnOffFan();
                // The toggle is disabled
            }
        });

        toggleHumidityButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked ) {
                Log.d(TAG, "TURNING ON Humidifier");
                turnOnHumidity();
            } else
            if (!isChecked)
            {
                turnOffHumidity();
                Log.d(TAG, "TURNING OFF Humidifier");
            }
        });



        notificationManager = NotificationManagerCompat.from(this);

        mBtnClearInput.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                mTxtReceive.setText("");
                notificationText.setText("");
            }
        });
    } // end of Oncreate function
    
    /*
    Thread based child-0process datapath to constantly uplaod stream of
     */
    private class ReadInput implements Runnable {

        private boolean bStop = false;
        private Thread t;

        //Read variables
        private int airQ;
        private double humi;
        private double temp;

        //specified
        private int airQUpperThresh = 300;
        private int airQLowerThresh = airQUpperThresh - 20;
        private int humiSetLevel = 30;



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
                        /*
                         * This is needed because new String(buffer) is taking the entire buffer i.e. 256 chars on Android 2.3.4 http://stackoverflow.com/a/8843462/1287554
                         */
                        for (i = 0; i < buffer.length && buffer[i] != 0; i++) {
                        }
                        final String strInput = new String(buffer, 0, i);

                        /*
                         * If checked then receive text, better design would probably be to stop thread if unchecked and free resources, but this is a quick fix
                         */

                        if (chkReceiveText.isChecked()) {
                            mTxtReceive.post(new Runnable() {
                                @Override
                                public void run() {
                                    mTxtReceive.append(strInput);

                                    //TODO : INCLUDE THE DECIMAL POINTS
                                    String firstInt = strInput.replaceAll(".*?(\\d+).*", "$1");

                                    if((strInput.trim().charAt(0)) == 'A') {
                                        try {
                                            airQ = Integer.parseInt(firstInt.trim());
                                        } catch (NumberFormatException e) {
                                            airQ = airQLowerThresh + 1;
                                        }
                                    }
                                    if((strInput.trim().charAt(0)) == 'H') {
                                        try {
                                            humi = Float.parseFloat(firstInt.trim());
                                        } catch (NumberFormatException e) {
                                            humi = 50.0;
                                        }
                                    }
                                    if((strInput.trim().charAt(0)) == 'T') {
                                        try {
                                            temp = Float.parseFloat(firstInt.trim());
                                        } catch (NumberFormatException e) {
                                            temp = 22.0;
                                        }
                                    }

                                    Log.d(TAG, "AQ = " + String.valueOf(airQ));
                                    Log.d(TAG, "H = " + String.valueOf(humi));
                                    Log.d(TAG, "T = " + String.valueOf(temp));
                                    Log.d(TAG, "__INPUT__" + firstInt.trim());

                                //Auto Fan Logic
                                    if( airQ >= airQUpperThresh && UserSelectFanType == true && autoFlip == false)
                                    {
                                        if(counter >1)
                                        {
                                            sendAlertOption();
                                            running =true;
                                            counter--;
                                        }
                                        AutoturnOnFan();
                                        if(imax < airQ)
                                        {imax = airQ;}
                                        notificationText.setText("Air threshold is reached! " + imax);
                                        autoFlip = true;
                                    }

                                    if( airQ < airQLowerThresh && UserSelectFanType == true && autoFlip == true)
                                    {
                                        AutoturnOffFan();
                                        notificationText.setText(" ");
                                        running = false;
                                        autoFlip = false;
                                    }

                                //Auto Humidity Logic
                                    if( humi > humiSetLevel && UserSelectFanType == true && humiFlip == false)
                                    {
                                        //AutoturnOffHumidifier();
                                        notificationText.setText("Above set Humidity Level " + String.valueOf(humiSetLevel));
                                        humiFlip = true;
                                    }

                                    if( humi <= humiSetLevel && UserSelectFanType == true && humiFlip == true)
                                    {
                                        //AutoturnOnHumidifier();
                                        notificationText.setText("Below set Humidity " + String.valueOf(humiSetLevel));
                                        humiFlip = false;
                                    }

                                    int txtLength = mTxtReceive.getEditableText().length();
                                    if(txtLength > mMaxChars){
                                        mTxtReceive.getEditableText().delete(0, txtLength - mMaxChars);
                                    }

                                    if (chkScroll.isChecked()) { // Scroll only if this is checked
                                        scrollView.post(new Runnable() { // Snippet from http://stackoverflow.com/a/4612082/1287554
                                            @Override
                                            public void run() {
                                                scrollView.fullScroll(View.FOCUS_DOWN);
                                            }
                                        });
                                    }
                                }
                            });
                        }

                    }
                    Thread.sleep(1500);
                }
            } catch (IOException e) {
// TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e) {
// TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public void stop() {
            bStop = true;
        }
    }



 /*  Unused function for testing

  private void Vibration(){
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
// Vibrate for 500 milliseconds
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
} else {
    //deprecated in API 26 
    v.vibrate(500);
}
    }
*/


    /*
    Sends Alert to user even if application is in background
     */

    public void sendAlertOption(){
        if(!UserSelectAlertType){
            sendAlert1();
        }
        else
            sendAlert2();
    }


    private void AutoturnOffFan()
    {
        if (mBTSocket!=null)
        {
            try
            {
                mBTSocket.getOutputStream().write("AF".toString().getBytes());
                Log.d(TAG, "AutoturnOffFan: AF");

            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
        FanButton.setChecked(false);
    }


private void AutoturnOnFan()
    {
        if (mBTSocket!=null)
        {
            try
            {
                mBTSocket.getOutputStream().write("AO".toString().getBytes());
                FanButton.setChecked(true);
                Log.d(TAG, "AutoturnOnFan: AO");
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    private void AutoturnOffHumidifier()
    {
        if (mBTSocket!=null)
        {
            try
            {
                mBTSocket.getOutputStream().write("AHF".toString().getBytes());
                Log.d(TAG, "AutoturnOffHumidfier: AHF");

            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
        FanButton.setChecked(false);
    }


    private void AutoturnOnHumidifier()
    {
        if (mBTSocket!=null)
        {
            try
            {
                mBTSocket.getOutputStream().write("AHO".toString().getBytes());
                FanButton.setChecked(true);
                Log.d(TAG, "AutoturnOnHumidifier: AHO");
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    public void sendAlert1(){

        Notification notification = new NotificationCompat.Builder(UserActivity.this,CHANNEL_1_ID)
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



    public void sendAlert2(){

        Notification notification = new NotificationCompat.Builder(UserActivity.this,CHANNEL_2_ID)
                .setSmallIcon(R.drawable.ic_notification1)
                .setContentTitle("Kraken HyperFan Released!")
                .setContentText("Silent Mode Testing!")
                //.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setColor(getResources().getColor(R.color.teal_700))
                .build();

        notificationManager.notify(2, notification);
    }



    private void Disconnect()
    {
        if (mBTSocket!=null) //If the btSocket is busy
        {
            try
            {
                mBTSocket.close(); //close connection
            }
            catch (IOException e)
            { msg("Error");}
        }
        finish(); //return to the first layout
    }
    private void turnOffFan()
    {
        if (mBTSocket!=null)
        {
            try
            {
                mBTSocket.getOutputStream().write("TF".toString().getBytes());
                Log.d(TAG, "turnOffFan: TF");
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }
    private void turnOnFan()
    {
        if (mBTSocket!=null)
        {
            try
            {
                mBTSocket.getOutputStream().write("TO".toString().getBytes());
                Log.d(TAG, "turnOnFan: TO");
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    private void turnOnHumidity()
    {
        if (mBTSocket!=null)
        {
            try
            {
                mBTSocket.getOutputStream().write("HO".toString().getBytes());
                Log.d(TAG, "turnOnHumidity: HO");
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }
    private void turnOffHumidity()
    {
        if (mBTSocket!=null)
        {
            try
            {
                mBTSocket.getOutputStream().write("HF".toString().getBytes());
                Log.d(TAG, "turnOffHumidity: HF");
            }
            catch (IOException e)
            {
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
        Log.d(TAG, "Paused");
        super.onPause();
        if(running ==true && wasRunning==true)
        running = false;
    }

    // If the activity is resumed,start the stopwatch again if it was running previously.
    @Override
    protected void onResume() {
        if (mBTSocket == null || !mIsBluetoothConnected) {
            new ConnectBT().execute();
        }
        Log.d(TAG, "Resumed");
        super.onResume();
        if (wasRunning) {
            running = true;
        }
    }
    // Start the stopwatch running when the Start button is clicked. Below method gets called when the Start button is clicked.

    @Override
    protected void onStop() {
        Log.d(TAG, "Stopped");
        super.onStop();
    }


    // protected void onSaveInstanceState(Bundle outState) {
    // TODO Auto-generated method stub
    //      super.onSaveInstanceState(outState);
    //  }

    @Override
    public void onSaveInstanceState(
            Bundle savedInstanceState)
    {
        savedInstanceState
                .putInt("seconds", seconds);
        savedInstanceState
                .putBoolean("running", running);
        savedInstanceState
                .putBoolean("wasRunning", wasRunning);
    }




    // Connect aysynchronously to blueooth
    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean mConnectSuccessful = true;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(UserActivity.this, "Hold on", "Connecting");
        }

        @Override
        protected Void doInBackground(Void... devices) {

            try {
                if (mBTSocket == null || !mIsBluetoothConnected) {
                    mBTSocket = mDevice.createInsecureRfcommSocketToServiceRecord(mDeviceUUID);
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
                Toast.makeText(getApplicationContext(), "Could not connect to device. Is it a Serial device? Also check if the UUID is correct in the settings", Toast.LENGTH_LONG).show();
                finish();
            } else {
                msg("Connected to device");
                mIsBluetoothConnected = true;
                mReadThread = new ReadInput(); // Kick off input reader
            }
            progressDialog.dismiss();
        }

    }

    //Disconnect aysynchronously to blueooth
    private class DisConnectBT extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params) {

            if (mReadThread != null) {
                mReadThread.stop();
                while (mReadThread.isRunning())
                    ; // Wait until it stops
                mReadThread = null;
            }
            try {
                mBTSocket.close();
            } catch (IOException e) {
// TODO Auto-generated catch block
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


    // Start the stop watch when the start button is clicked
    public void onClickStart(View view)
    {
        running = true;
    }

    // Stop the stopwatch when the Stop button is clicked.

    public void onClickStop(View view)
    {
        wasRunning=false;
        running = false;
    }

    // Reset the stopwatch when the Reset button is clicked.
    public void onClickReset(View view)
    {
        running = false;
        seconds = 0;
    }

    // Sets the Number of seconds on the timer. The runTimer() method uses a Handler to increment the seconds and update the text view.
    private void runTimer()
    {
        // Get the text view.
        final TextView timeView = (TextView)findViewById(R.id.time_view);

        // Creates a new Handler
        final Handler handler = new Handler();
        // Call the post() method, passing in a new Runnable.
        // The post() method processes code without a delay,so the code in the Runnable will run almost immediately.

        handler.post(new Runnable() {
            @Override
            public void run()
            {
                int hours = seconds / 3600;
                int minutes = (seconds % 3600) / 60;
                int secs = seconds % 60;
                // Format the seconds into hours, minutes,and seconds

                String time = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs);
                // Set the text view text.
                timeView.setText(time);

                // If running is true, increment the seconds variable
                if (running) {
                    seconds++;
                }

                // Post the code again with a delay of 1 second
                handler.postDelayed(this, 1000);
            }
        });
    }

}
