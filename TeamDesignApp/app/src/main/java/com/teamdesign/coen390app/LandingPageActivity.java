package com.teamdesign.coen390app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Notification;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.teamdesign.coen390app.NotificationActivity.CHANNEL_1_ID;
import static com.teamdesign.coen390app.NotificationActivity.CHANNEL_2_ID;
import static com.teamdesign.coen390app.NotificationActivity.CHANNEL_3_ID;


public class LandingPageActivity extends AppCompatActivity {


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
    private static final String TAG = "landing Page Activity";
    private int mMaxChars = 50000;
    private UUID mDeviceUUID;
    private BluetoothSocket mBTSocket;
    private ReadInput mReadThread = null;
    private TextView mTxtReceive;
    private BluetoothAdapter mBTAdapter;
    private static final int BT_ENABLE_REQUEST = 10; // This is the code we use for BT Enable
    private static final int SETTINGS = 20;
    private int mBufferSize = 50000; //Default
    public static final String DEVICE_EXTRA = "com.teamdesign.coen390app.SOCKET";
    public static final String DEVICE_UUID = "com.teamdesign.coen390app..uuid";
    private static final String DEVICE_LIST = "com.teamdesign.coen390app..devicelist";
    private static final String DEVICE_LIST_SELECTED = "com.teamdesign.coen390app.devicelistselected";
    public static final String BUFFER_SIZE = "com.teamdesign.coen390app.buffersize";

    //added for this part
    private TextView airQRead;
    private TextView tempRead;
    private TextView humiRead;

    private Button toAdvanced;
    private Button toLog;
    private Button toGraph;
    private Button toControl;

    private int HighThresh = 350;
    private int LowThresh = 300;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);

//        new ConnectBT().execute();

        ActivityHelper.initialize(this);
        if (savedInstanceState != null) {
            // Get the previous state of the stopwatch if the activity has been destroyed and recreated.
            seconds = savedInstanceState.getInt("seconds");
            running = savedInstanceState.getBoolean("running");
            wasRunning = savedInstanceState.getBoolean("wasRunning");
        }
        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        mDevice = b.getParcelable(MainActivity.DEVICE_EXTRA);
        mDeviceUUID = UUID.fromString(b.getString(MainActivity.DEVICE_UUID));
        mMaxChars = b.getInt(MainActivity.BUFFER_SIZE);
        notificationText = findViewById(R.id.alertText);
        //mTxtReceive = (TextView) findViewById(R.id.txtReceiveGraph);
        notificationManager = NotificationManagerCompat.from(this);


        tempRead = findViewById(R.id.tempRead_TV);
        airQRead = findViewById(R.id.airQRead_TV);
        humiRead = findViewById(R.id.humiRead_TV);


        toAdvanced = findViewById(R.id.toAdvance_B);
        toAdvanced.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String deviceName;
                mBTAdapter = BluetoothAdapter.getDefaultAdapter();

                Set<BluetoothDevice> pairedDevices = mBTAdapter.getBondedDevices();
                List<BluetoothDevice> listDevices = new ArrayList<BluetoothDevice>();
                for (BluetoothDevice device : pairedDevices) {
                    listDevices.add(device);
                }
                for(int i = 0 ; i < listDevices.size(); i ++)
                {
                    deviceName = (listDevices.get(i).getName());
                    if(deviceName.equals("HC-05"))
                    {
                        BluetoothDevice device = listDevices.get(i);
                        Intent intent = new Intent(getApplicationContext(), UserActivity.class);
                        intent.putExtra(DEVICE_EXTRA, device);
                        intent.putExtra(DEVICE_UUID, mDeviceUUID.toString());
                        intent.putExtra(BUFFER_SIZE, mBufferSize);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        new DisConnectBT().execute();
                        break;
                    }
                }
            }
        });

        toLog = findViewById(R.id.toLog_B);
        toLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String deviceName;
                mBTAdapter = BluetoothAdapter.getDefaultAdapter();

                Set<BluetoothDevice> pairedDevices = mBTAdapter.getBondedDevices();
                List<BluetoothDevice> listDevices = new ArrayList<BluetoothDevice>();
                for (BluetoothDevice device : pairedDevices) {
                    listDevices.add(device);
                }
                for(int i = 0 ; i < listDevices.size(); i ++)
                {
                    deviceName = (listDevices.get(i).getName());
                    if(deviceName.equals("HC-05"))
                    {
                        BluetoothDevice device = listDevices.get(i);
                        Intent intent = new Intent(getApplicationContext(), UserSimpleActivity.class);
                        intent.putExtra(DEVICE_EXTRA, device);
                        intent.putExtra(DEVICE_UUID, mDeviceUUID.toString());
                        intent.putExtra(BUFFER_SIZE, mBufferSize);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        new DisConnectBT().execute();
                        break;
                    }
                }
            }
        });

        toGraph = findViewById(R.id.toGraph_B);
        toGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String deviceName;
                mBTAdapter = BluetoothAdapter.getDefaultAdapter();

                Set<BluetoothDevice> pairedDevices = mBTAdapter.getBondedDevices();
                List<BluetoothDevice> listDevices = new ArrayList<BluetoothDevice>();
                for (BluetoothDevice device : pairedDevices) {
                    listDevices.add(device);
                }
                for(int i = 0 ; i < listDevices.size(); i ++)
                {
                    deviceName = (listDevices.get(i).getName());
                    if(deviceName.equals("HC-05"))
                    {
                        BluetoothDevice device = listDevices.get(i);
                        Intent intent = new Intent(getApplicationContext(), GraphActivity.class);
                        intent.putExtra(DEVICE_EXTRA, device);
                        intent.putExtra(DEVICE_UUID, mDeviceUUID.toString());
                        intent.putExtra(BUFFER_SIZE, mBufferSize);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        new DisConnectBT().execute();
                        break;
                    }
                }
            }
        });

        toControl = findViewById(R.id.toControl_B);
        toControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String deviceName;
                mBTAdapter = BluetoothAdapter.getDefaultAdapter();

                Set<BluetoothDevice> pairedDevices = mBTAdapter.getBondedDevices();
                List<BluetoothDevice> listDevices = new ArrayList<BluetoothDevice>();
                for (BluetoothDevice device : pairedDevices) {
                    listDevices.add(device);
                }
                for(int i = 0 ; i < listDevices.size(); i ++)
                {
                    deviceName = (listDevices.get(i).getName());
                    if(deviceName.equals("HC-05"))
                    {
                        BluetoothDevice device = listDevices.get(i);
                        Intent intent = new Intent(getApplicationContext(), ControlActivity.class);
                        intent.putExtra(DEVICE_EXTRA, device);
                        intent.putExtra(DEVICE_UUID, mDeviceUUID.toString());
                        intent.putExtra(BUFFER_SIZE, mBufferSize);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        new DisConnectBT().execute();
                        break;
                    }
                }
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

        private int humiSetLevel = 25;

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

                        //TODO : INCLUDE THE DECIMAL POINTS
                        String firstInt = strInput.replaceAll(".*?(\\d+).*", "$1");


                        if((strInput.trim().charAt(0)) == 'A') {
                            try {
                                airQ = Integer.parseInt(firstInt.trim());
                            } catch (NumberFormatException e) {
                                airQ = LowThresh + 1;
                            }
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run() {
                                    airQRead.setText(String.valueOf(airQ));
                                }
                            });
                        }
                        if((strInput.trim().charAt(0)) == 'H') {
                            try {
                                humi = Float.parseFloat(firstInt.trim());
                            } catch (NumberFormatException e) {
                                humi = humiSetLevel + 1;
                            }
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run() {
                                    humiRead.setText(String.valueOf(humi));
                                }
                            });
                        }
                        if((strInput.trim().charAt(0)) == 'T') {
                            try {
                                temp = Float.parseFloat(firstInt.trim());
                            } catch (NumberFormatException e) {
                                temp = 22.0;
                            }
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run() {
                                    tempRead.setText(String.valueOf(temp));
                                }
                            });
                        }

                        Log.d(TAG, "AQ = " + String.valueOf(airQ));
                        Log.d(TAG, "H = " + String.valueOf(humi));
                        Log.d(TAG, "T = " + String.valueOf(temp));
                        Log.d(TAG, "__INPUT__" + firstInt.trim());

                        //Auto Fan Logic
                        if( airQ >= HighThresh && UserSelectFanType == true && autoFlip == false)
                        {
                            if(counter >1)
                            {
                                sendAlertOption();
                                running =true;
                                counter--;
                            }

                            if(imax < airQ)
                            {imax = airQ;}

                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run() {
                                    notificationText.setText("Air threshold is reached! " + imax);
                                }
                            });
                            //notificationText.setText("Air threshold is reached! " + imax);
                            autoFlip = true;
                        }

                        if( airQ < LowThresh && UserSelectFanType == true && autoFlip == true)
                        {

                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run() {
                                    notificationText.setText(" ");
                                }
                            });
//                            notificationText.setText(" ");
                            running = false;
                            autoFlip = false;
                        }

                        //Auto Humidity Logic
                        if( humi > humiSetLevel && UserSelectFanType == true && humiFlip == false)
                        {

                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run() {

                                }
                            });
//                            notificationText.setText("Above set Humidity Level " + String.valueOf(humiSetLevel));
                            humiFlip = true;
                        }

                        if( humi <= humiSetLevel && UserSelectFanType == true && humiFlip == true)
                        {

                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run() {
                                }
                            });
//                            notificationText.setText("Below set Humidity " + String.valueOf(humiSetLevel));
                            humiFlip = false;
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






    public void sendAlert1(){

        Notification notification = new NotificationCompat.Builder(LandingPageActivity.this, CHANNEL_1_ID)
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

        Notification notification = new NotificationCompat.Builder(LandingPageActivity.this, CHANNEL_2_ID)
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
           //new DisConnectBT().execute();
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

//    @Override
//    public void onSaveInstanceState(
//            Bundle savedInstanceState)
//    {
//        savedInstanceState
//                .putInt("seconds", seconds);
//        savedInstanceState
//                .putBoolean("running", running);
//        savedInstanceState
//                .putBoolean("wasRunning", wasRunning);
//    }




    // Connect aysynchronously to blueooth
    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean mConnectSuccessful = true;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(LandingPageActivity.this, "Hold on", "Connecting");
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

}