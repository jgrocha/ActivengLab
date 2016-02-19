package activeng.pt.activenglab;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import activeng.pt.activenglab.data.TemperatureContract;

public class EditActivity extends AppCompatActivity {

    private ContentValues currentSensor = null;
    private int sensorId = 0;
    private String location;
    private String sensortype;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        EditText edit_location_read = (EditText) findViewById(R.id.edit_location_read);
        EditText edit_type_read = (EditText) findViewById(R.id.edit_type_read);

        Intent calIntent = getIntent();
        if (calIntent != null) {
            if (calIntent.hasExtra(TemperatureContract.SensorEntry.TABLE_NAME)) {
                currentSensor = (ContentValues) calIntent.getParcelableExtra(TemperatureContract.SensorEntry.TABLE_NAME);
                sensorId = currentSensor.getAsInteger(TemperatureContract.SensorEntry.COLUMN_SENSORID);
                location = currentSensor.getAsString(TemperatureContract.SensorEntry.COLUMN_LOCATION);
                sensortype = currentSensor.getAsString(TemperatureContract.SensorEntry.COLUMN_SENSORTYPE);
                setTitle("Edit " + sensorId);
                edit_location_read.setText(location);
                edit_type_read.setText(sensortype);
            }
        }

        Button scanButton = (Button) findViewById(R.id.edit_button_save);
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                EditText edit_location_read = (EditText) findViewById(R.id.edit_location_read);
                EditText edit_type_read = (EditText) findViewById(R.id.edit_type_read);

                String location = edit_location_read.getText().toString();
                String sensortype = edit_type_read.getText().toString();

                // update sensor
                ContentValues updatedValues = new ContentValues();
                updatedValues.put(TemperatureContract.SensorEntry.COLUMN_LOCATION, location);
                updatedValues.put(TemperatureContract.SensorEntry.COLUMN_SENSORTYPE, sensortype);
                int count = getContentResolver().update(
                        TemperatureContract.SensorEntry.CONTENT_URI, updatedValues, TemperatureContract.SensorEntry.COLUMN_SENSORID + "= ?",
                        new String[] { Long.toString(sensorId)});

                Toast toast;
                if (count == 1) {
                    toast = Toast.makeText(getApplicationContext(), "Sensor updated", Toast.LENGTH_SHORT);
                } else {
                    toast = Toast.makeText(getApplicationContext(), "Error updating sensor", Toast.LENGTH_SHORT);
                }
                toast.show();
                Log.d("ActivEng", "update: count = " + count);
                done();
            }
        });

    }

    public void done() {
        super.onBackPressed();
    }

}
