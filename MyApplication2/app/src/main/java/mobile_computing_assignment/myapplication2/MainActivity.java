package mobile_computing_assignment.myapplication2;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.database.sqlite.*;
import android.widget.Toast;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private LineGraphSeries<DataPoint> seriesX;
    private LineGraphSeries<DataPoint> seriesY;
    private LineGraphSeries<DataPoint> seriesZ;
    private GraphView graph;
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private Button start,stop,add;
    private EditText name,gender,age;
    float x,y,z = 0;
    int lastX = 0, lastY = 0, lastZ = 0;
    boolean isRunning = false;
    Cursor res;
    SQLiteDatabase db;
    String currentTableName;
    static int ACCE_FILTER_DATA_MIN_TIME = 200; //ms
    long lastSaved = System.currentTimeMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = this.openOrCreateDatabase("patientDB", MODE_PRIVATE, null);

        name = (EditText) findViewById(R.id.name);
        gender = (EditText) findViewById(R.id.gender);
        age = (EditText) findViewById(R.id.age);

        // Setting Button Listener
        start = (Button) findViewById(R.id.start);
        stop = (Button) findViewById(R.id.stop);
        add = (Button) findViewById(R.id.add);

        // Setting up the Accelerometer Listener
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(MainActivity.this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        // Creating a graph instance.
        graph = (GraphView) findViewById(R.id.graph);

        // Adding the data here.
        seriesX = new LineGraphSeries<DataPoint>();
        seriesY = new LineGraphSeries<DataPoint>();
        seriesZ = new LineGraphSeries<DataPoint>();
        for(int i = 0; i < 10; i++) {
            seriesX.appendData(new DataPoint(lastX++, 0), true, 10);
            seriesY.appendData(new DataPoint(lastY++, 0), true, 10);
            seriesZ.appendData(new DataPoint(lastZ++, 0), true, 10);
        }

        // Adding different Colours to graph Lines
        seriesX.setColor(Color.BLACK);
        seriesY.setColor(Color.BLACK);
        seriesZ.setColor(Color.BLACK);

        // Customizing the ViewPort
        Viewport viewport = graph.getViewport();
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(0);
        viewport.setMaxY(10);
        viewport.setMaxX(10);
        //viewport.setScrollable(true);
        currentTableName = name.getText().toString() + "_" + age.getText().toString() + "_" + gender.getText().toString();
        onResume();

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isRunning == false) {
                    try {
                        currentTableName = name.getText().toString() + "_" + age.getText().toString() + "_" + gender.getText().toString();
                        res = db.rawQuery("SELECT x, y, z FROM " + currentTableName, null);
                        res.moveToFirst();
                        isRunning = true;
                    } catch (SQLiteException e) {
                        Toast.makeText(MainActivity.this, "This table doesn't exist!", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPause();
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //create db table
                String temp;
                temp = name.getText().toString() + "_" + age.getText().toString() + "_" + gender.getText().toString();

                try {
                    db.execSQL("CREATE TABLE " + temp + "(time INTEGER primary key, x FLOAT, y FLOAT, z FLOAT);");
                    currentTableName = name.getText().toString() + "_" + age.getText().toString() + "_" + gender.getText().toString();

                    Toast.makeText(MainActivity.this, "Create Success!", Toast.LENGTH_LONG).show();
                } catch (SQLiteException e) {
                    Toast.makeText(MainActivity.this, "This table already exist!", Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        //senSensorManager.registerListener(MainActivity.this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        graph.addSeries(seriesX);
        graph.addSeries(seriesY);
        graph.addSeries(seriesZ);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true)
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isRunning == true) {
                                    seriesX.setColor(Color.RED);
                                    seriesY.setColor(Color.GREEN);
                                    seriesZ.setColor(Color.BLUE);

                                    if(res.isLast() == false) {
                                        seriesX.appendData(new DataPoint(lastX++, res.getFloat(0)), true, 10);
                                        seriesY.appendData(new DataPoint(lastY++, res.getFloat(1)), true, 10);
                                        seriesZ.appendData(new DataPoint(lastZ++, res.getFloat(2)), true, 10);
                                        res.moveToNext();
                                    }
                                    else{
                                        Toast.makeText(MainActivity.this, "All data had shown.", Toast.LENGTH_LONG).show();
                                        isRunning = false;
                                    }
                                }
                            }
                        });
                        try {
                            Thread.sleep(1500);
                        }
                        catch (InterruptedException e) {
                        }
                    }
                }
            }).start();

    }

    @Override
    protected void onPause() {
        super.onPause();
        isRunning = false;
        //graph.removeAllSeries();
        for(int i = 0; i < 10; i++) {
            seriesX.appendData(new DataPoint(lastX++, 0), true, 10);
            seriesY.appendData(new DataPoint(lastY++, 0), true, 10);
            seriesZ.appendData(new DataPoint(lastZ++, 0), true, 10);
        }

        seriesX.setColor(Color.BLACK);
        seriesY.setColor(Color.BLACK);
        seriesZ.setColor(Color.BLACK);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // We are getting the accelerometer data here
        Sensor mySensor = event.sensor;
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER && (System.currentTimeMillis() - lastSaved) > ACCE_FILTER_DATA_MIN_TIME){
            lastSaved = System.currentTimeMillis();
             x = event.values[0];
             y = event.values[1];
             z = event.values[2];

            try {
                db.execSQL("INSERT INTO " + currentTableName + "(x, y, z) values (" + x + "," + y + "," + z + ");");
            }
            catch(SQLiteException e) {

            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
