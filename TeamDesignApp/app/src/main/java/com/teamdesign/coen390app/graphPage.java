package com.teamdesign.coen390app;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
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
import android.widget.ToggleButton;


import androidx.core.app.NotificationManagerCompat;



public class graphPage extends Activity {

    // initializing sqlite database manager
    DatabaseHelper mDatabaseHelper;

    private Button historyButton;
    public int cnt = 0;

    // intially default mode
    protected boolean UserSelectAlertType=true;
    protected boolean UserSelectFanType= true;
    protected boolean UserSelectFanManual =true;

    private ToggleButton toggleAlertButton;
    private ToggleButton toggleFanButton;
    private ToggleButton FanButton;

    // Notification manager initialization
    private NotificationManagerCompat notificationManager;

    // maximum threshold value
    private int imax = 0;

    private boolean  fanThresholdFlag=true;

    // Integer value used for time system
    private int seconds = 0;

    // Integer counter used manipulate the threshold Notification
    public int counter =10;

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
    private static final String TAG = "BlueTest5-MainActivity";
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


    int lastVal = 0;
    GraphView graphView;
    private LineGraphSeries<DataPoint> series;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_page);
        ActivityHelper.initialize(this);
        mTxtReceive.setMovementMethod(new ScrollingMovementMethod());
        graphView = findViewById(R.id.idGraphView);
        series = new LineGraphSeries<DataPoint>();
        graphView.addSeries(series);
        Viewport viewport = graphView.getViewport();
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(0);
        viewport.setMaxY(400);
        viewport.setScrollable(true);
        graphView.setTitle("Graph of Smoke in PPM");
        graphView.setTitleTextSize(80);
        graphView.setTitleColor(R.color.purple_200);
        GridLabelRenderer gridLabel = graphView.getGridLabelRenderer();
        gridLabel.setHorizontalAxisTitle("Live Sensor Readings");
        gridLabel.setVerticalAxisTitle("CO2 in PPM");


    }



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

                                    if (cnt ==0) {
                                        mTxtReceive.append("");
                                        mTxtReceive.append("\n");
                                        cnt++;
                                    }


                                    String firstInt = strInput.replaceFirst(".*?(\\d+).*", "$1");
                                    int i;
                                    try {
                                        i = Integer.parseInt(firstInt.trim());
                                        series.appendData(new DataPoint(lastVal++, i), false, 50);
                                    }
                                    catch (NumberFormatException e)
                                    {
                                        i = 151;
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
                    Thread.sleep(500);
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

}