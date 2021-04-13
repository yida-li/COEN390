package com.teamdesign.coen390app;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.LineGraphSeries;

public class GraphActivity extends AppCompatActivity {

  GraphView airQGraphView;
  private LineGraphSeries<DataPoint> airQSeries;
  private int airQLastVal = 0;
  NumberStorage numhelper;
  Button deleteGraph;
  TextView graphText;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_graph);
    deleteGraph = (Button) findViewById(R.id.DeleteGraph);
    deleteGraph.setOnClickListener(
      new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          delete();
          Toast
            .makeText(
              getApplicationContext(),
              "Graph Deleted: Please Refresh Page",
              Toast.LENGTH_LONG
            )
            .show();
        }
      }
    );
    graphText = findViewById(R.id.graphText);
    numhelper = new NumberStorage(this);
    airQGraphView = findViewById(R.id.airQGraphView);
    airQSeries = new LineGraphSeries<DataPoint>();
    airQGraphView.addSeries(airQSeries);
    Viewport viewport1 = airQGraphView.getViewport();
    viewport1.setYAxisBoundsManual(true);
    viewport1.setMinY(650);
    viewport1.setMaxY(1000);
    viewport1.setBorderColor(R.color.purple_200);
    viewport1.setScrollable(true);
    airQGraphView.setTitle("Recorded Values Passed Thresholds");
    airQGraphView.setTitleTextSize(80);
    airQGraphView.setTitleColor(R.color.purple_200);
    GridLabelRenderer gridLabel1 = airQGraphView.getGridLabelRenderer();
    gridLabel1.setHorizontalAxisTitle("Recorded Readings");
    gridLabel1.setVerticalAxisTitle("CO2 in PPM");
    plotGraph();
  }

  private void toastMessage(String message) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
  }

  public void delete() {
    numhelper.recycle();
  }

  public void plotGraph() {
    Cursor data = numhelper.getData();
    String Text = "";
    while (data.moveToNext()) {
      //get the value from the database in column 1 then add it to the ArrayList
      int temp = Integer.parseInt(data.getString(1));
      airQSeries.appendData(new DataPoint(airQLastVal++, temp), false, 200);
    }
    graphText.setText(Text);
  }
}
