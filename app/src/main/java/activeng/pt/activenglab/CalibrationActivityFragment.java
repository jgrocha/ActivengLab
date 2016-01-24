package activeng.pt.activenglab;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import activeng.pt.activenglab.data.TemperatureContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class CalibrationActivityFragment extends Fragment implements OnClickListener {

    private BroadcastReceiver conn2BTService;
    private boolean registered = false;

    private EditText cal_current_read;
    private EditText cal_new_read;

    private int sensorId = 0;       // Arduino ID
    private String address;         // Arduino bluetooth address
    private double cal_a, cal_b;
    private double cal_a_new = Double.MAX_VALUE, cal_b_new = Double.MAX_VALUE;
    private ContentValues currentSensor = null;

    public CalibrationActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        String sensorStr;
        View rootView = inflater.inflate(R.layout.fragment_calibration, container, false);

        Button btnCalculate = (Button) rootView.findViewById(R.id.cal_button_calculate);
        btnCalculate.setOnClickListener(this);
        Button btnSave = (Button) rootView.findViewById(R.id.cal_button_save);
        btnSave.setOnClickListener(this);

        cal_current_read = (EditText) rootView.findViewById(R.id.cal_current_read);
        cal_new_read = (EditText) rootView.findViewById(R.id.cal_new_read);

        EditText cal_current_offset = (EditText) rootView.findViewById(R.id.cal_current_offset);
        EditText cal_current_gain = (EditText) rootView.findViewById(R.id.cal_current_gain);

        // BroadcastReceiver is the same for LocalBroadcast or global Broadcast
        conn2BTService = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                double temp;
                String tempStr;
                long sensor, instant;
                double newcal;
                Bundle extras = intent.getExtras();
                Log.d("ActivEng", "CalibrationActivityFragment --> onReceive");
                if (extras != null) {

                    //Temperature temp = UtilitySingleton.getInstance().processMessage(extras.getString(Intent.EXTRA_TEXT), sensorId, address);
                    temp = extras.getDouble(Constants.EXTRA_MSG_TEMP, -999);
                    tempStr = extras.getString(Constants.EXTRA_MSG_TEMP_STR);
                    sensor = extras.getLong(Constants.EXTRA_MSG_TEMP_SENSOR, 0);
                    instant = extras.getLong(Constants.EXTRA_MSG_TEMP_MILLIS, 0);

                    if (sensor == currentSensor.getAsInteger(TemperatureContract.SensorEntry.COLUMN_SENSORID)) {
                        cal_current_read.setText(tempStr);

                        if (cal_a_new != Double.MAX_VALUE && cal_b_new != Double.MAX_VALUE) {
                            newcal = cal_a_new + temp * cal_b_new;
                            cal_new_read.setText(UtilitySingleton.getInstance().formatTemperature(newcal, 2));
                        } else {
                            cal_new_read.setText("");
                        }
                    }
                }
            }
        };

        Intent calIntent = getActivity().getIntent();
        if (calIntent != null) {
            if (calIntent.hasExtra(TemperatureContract.SensorEntry.TABLE_NAME)) {
                //sensorId = Long.parseLong(calIntent.getStringExtra("_id"), 10);
                currentSensor = (ContentValues) calIntent.getParcelableExtra(TemperatureContract.SensorEntry.TABLE_NAME);
                sensorId = currentSensor.getAsInteger(TemperatureContract.SensorEntry.COLUMN_SENSORID);
                address = currentSensor.getAsString(TemperatureContract.SensorEntry.COLUMN_ADDRESS);

                getActivity().setTitle("Now calibrating " + sensorId);
                cal_a = currentSensor.getAsDouble(TemperatureContract.SensorEntry.COLUMN_CAL_A);
                cal_b = currentSensor.getAsDouble(TemperatureContract.SensorEntry.COLUMN_CAL_B);
                cal_current_offset.setText(UtilitySingleton.getInstance().formatTemperature(cal_a, 6));
                cal_current_gain.setText(UtilitySingleton.getInstance().formatTemperature(cal_b, 6));
            }
        }

        // http://stackoverflow.com/questions/33090978/change-listview-via-custom-simplecursoradapter
        return rootView;
        // return inflater.inflate(R.layout.fragment_calibration, container, false);
    }

    private void saveCalibration(View rootView) {
        // send calibration to Arduino
        String message = "C|" + sensorId + "|" + cal_a_new + "|" + cal_b_new;
        //Intent intent = new Intent(Constants.MESSAGE_TO_ARDUINO).putExtra(Intent.EXTRA_TEXT, "T1445177600");
        Intent intent = new Intent(Constants.MESSAGE_TO_ARDUINO).putExtra(Intent.EXTRA_TEXT, message);
        //getActivity().sendBroadcast(intent);
        // new
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
        // update sensor
        ContentValues updatedValues = new ContentValues();
        updatedValues.put(TemperatureContract.SensorEntry.COLUMN_CAL_A, cal_a_new);
        updatedValues.put(TemperatureContract.SensorEntry.COLUMN_CAL_B, cal_b_new);
        int count = getActivity().getContentResolver().update(
                TemperatureContract.SensorEntry.CONTENT_URI, updatedValues, TemperatureContract.SensorEntry.COLUMN_SENSORID + "= ?",
                new String[] { Long.toString(sensorId)});
        // create new entry in calibration
        Uri mNewUri;

        //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ////Date date = new Date(System.currentTimeMillis());
        //Date date = new Date();

        // save calibration on local sqlite database
        ContentValues novosValues = new ContentValues();
        novosValues.put(TemperatureContract.CalibrationEntry.COLUMN_SENSORID, sensorId);
        novosValues.put(TemperatureContract.CalibrationEntry.COLUMN_ADDRESS, address);
        //novosValues.put(TemperatureContract.CalibrationEntry.COLUMN_CREATED, System.currentTimeMillis()/1000);
        //novosValues.put(TemperatureContract.CalibrationEntry.COLUMN_CREATED, "datetime(" + System.currentTimeMillis()/1000 + ", 'unixepoch')" );
        //novosValues.put(TemperatureContract.CalibrationEntry.COLUMN_CREATED, dateFormat.format(date) );
        novosValues.put(TemperatureContract.CalibrationEntry.COLUMN_CAL_A_OLD, cal_a);
        novosValues.put(TemperatureContract.CalibrationEntry.COLUMN_CAL_B_OLD, cal_b);
        novosValues.put(TemperatureContract.CalibrationEntry.COLUMN_CAL_A_NEW, cal_a_new);
        novosValues.put(TemperatureContract.CalibrationEntry.COLUMN_CAL_B_NEW, cal_b_new);
        EditText et_cal_read_high = (EditText) rootView.findViewById(R.id.cal_read_high);
        EditText et_cal_ref_high = (EditText) rootView.findViewById(R.id.cal_ref_high);
        EditText et_cal_read_low = (EditText) rootView.findViewById(R.id.cal_read_low);
        EditText et_cal_ref_low = (EditText) rootView.findViewById(R.id.cal_ref_low);
        double cal_read_high;
        double cal_ref_high;
        double cal_read_low;
        double cal_ref_low;
        cal_read_high = Double.parseDouble(et_cal_read_high.getText().toString());
        cal_ref_high = Double.parseDouble(et_cal_ref_high.getText().toString());
        cal_read_low = Double.parseDouble(et_cal_read_low.getText().toString());
        cal_ref_low = Double.parseDouble(et_cal_ref_low.getText().toString());
        novosValues.put(TemperatureContract.CalibrationEntry.COLUMN_REF_VALUE_HIGH, cal_ref_high);
        novosValues.put(TemperatureContract.CalibrationEntry.COLUMN_REF_VALUE_LOW, cal_ref_low);
        novosValues.put(TemperatureContract.CalibrationEntry.COLUMN_READ_VALUE_HIGH, cal_read_high);
        novosValues.put(TemperatureContract.CalibrationEntry.COLUMN_READ_VALUE_LOW, cal_read_low);
        mNewUri = getActivity().getContentResolver().insert(TemperatureContract.CalibrationEntry.CONTENT_URI, novosValues);
    }

    private void computeCalibration(View rootView) {
        EditText et_cal_read_high = (EditText) rootView.findViewById(R.id.cal_read_high);
        EditText et_cal_ref_high = (EditText) rootView.findViewById(R.id.cal_ref_high);
        EditText et_cal_read_low = (EditText) rootView.findViewById(R.id.cal_read_low);
        EditText et_cal_ref_low = (EditText) rootView.findViewById(R.id.cal_ref_low);

        double cal_read_high;
        double cal_ref_high;
        double cal_read_low;
        double cal_ref_low;

        try {
            cal_read_high = Double.parseDouble(et_cal_read_high.getText().toString());
            cal_ref_high = Double.parseDouble(et_cal_ref_high.getText().toString());
            cal_read_low = Double.parseDouble(et_cal_read_low.getText().toString());
            cal_ref_low = Double.parseDouble(et_cal_ref_low.getText().toString());

            //cal_a = currentSensor.getAsDouble(TemperatureContract.SensorEntry.COLUMN_CAL_A);
            //cal_b = currentSensor.getAsDouble(TemperatureContract.SensorEntry.COLUMN_CAL_B);

            cal_b_new = cal_b * (cal_read_high - cal_read_low) / (cal_ref_high - cal_ref_low);
            cal_a_new = cal_read_low - (cal_ref_low - cal_a) * (cal_read_high - cal_read_low) / (cal_ref_high - cal_ref_low);

            ((EditText) rootView.findViewById(R.id.cal_new_offset)).setText(UtilitySingleton.getInstance().formatTemperature(cal_a_new, 6));
            ((EditText) rootView.findViewById(R.id.cal_new_gain)).setText(UtilitySingleton.getInstance().formatTemperature(cal_b_new, 6));

        } catch (NumberFormatException e) {
            cal_a_new = Double.MAX_VALUE;
            cal_b_new = Double.MAX_VALUE;
            // immediate feedback
            cal_new_read.setText("");
        }
    }

    @Override
    public void onClick(View v) {
        View rootView = v.getRootView();
        switch (v.getId()) {
            case R.id.cal_button_calculate:
                Log.d("Life cyle", "calculate()");
                computeCalibration(rootView);
                break;
            case R.id.cal_button_save:
                Log.d("Life cyle", "save()");
                if (cal_a_new != Double.MAX_VALUE && cal_b_new != Double.MAX_VALUE) {
                    // TODO
                    // enable or disable SAVE button
                    saveCalibration(rootView);
                    // close this activity
                    ((CalibrationActivity) getActivity()).done();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("ActivEng", "CalibrationActivityFragment registerReceiver onResume()");
        // old global broadcast receiver
        //getActivity().registerReceiver(this.conn2BTService, new IntentFilter(Constants.MESSAGE_TEMPERATURE));
        // new
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter filter = new IntentFilter(Constants.MESSAGE_TEMPERATURE);
        manager.registerReceiver(this.conn2BTService, filter);
        registered = true;
    }

    @Override
    public void onPause() {
        Log.d("ActivEng", "CalibrationActivityFragment unregisterReceiver onPause()");
        // old global broadcast receiver
        //getActivity().unregisterReceiver(this.conn2BTService);
        // new
        if (registered) {
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getActivity());
            manager.unregisterReceiver(this.conn2BTService);
        }
        super.onPause();
    }
}
