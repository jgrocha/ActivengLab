package activeng.pt.activenglab;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;

import activeng.pt.activenglab.data.TemperatureContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class ProgramActivityFragment extends Fragment implements View.OnClickListener {

    private BroadcastReceiver conn2BTService;
    private EditText prog_currentread;

    private long _ID = 0;           // Local _ID autoincrement
    private int sensorId = 0;       // Arduino ID
    private String address;         // Arduino bluetooth address
    private double cal_a_new = Double.MAX_VALUE, cal_b_new = Double.MAX_VALUE;
    private ContentValues currentSensor = null;

    public ProgramActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_program, container, false);

        Button btnRun = (Button) rootView.findViewById(R.id.bt_prog_run);
        btnRun.setOnClickListener(this);
        Button btnRunStop = (Button) rootView.findViewById(R.id.bt_prog_run_stop);
        btnRunStop.setOnClickListener(this);
        Button btnCool = (Button) rootView.findViewById(R.id.bt_prog_cool);
        btnCool.setOnClickListener(this);
        Button btnCoolStop = (Button) rootView.findViewById(R.id.bt_prog_cool_stop);
        btnCoolStop.setOnClickListener(this);

        prog_currentread = (EditText) rootView.findViewById(R.id.et_prog_currentread);

        // BroadcastReceiver is the same for LocalBroadcast or global Broadcast
        conn2BTService = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                double temp;
                String tempStr;
                long sensor, instant;
                Bundle extras = intent.getExtras();
                Log.d("ActivEng", "ProgramActivityFragment --> onReceive");
                if (extras != null) {

                    //Temperature temp = UtilitySingleton.getInstance().processMessage(extras.getString(Intent.EXTRA_TEXT), sensorId, address);
                    temp = extras.getDouble(Constants.EXTRA_MSG_TEMP, -999);
                    tempStr = extras.getString(Constants.EXTRA_MSG_TEMP_STR);
                    sensor = extras.getLong(Constants.EXTRA_MSG_TEMP_SENSOR, 0);
                    instant = extras.getLong(Constants.EXTRA_MSG_TEMP_MILLIS, 0);
                    if (sensor == currentSensor.getAsInteger(TemperatureContract.SensorEntry.COLUMN_SENSORID)) {
                        prog_currentread.setText(tempStr);
                    }
                }
            }
        };

        Intent calIntent = getActivity().getIntent();
        if (calIntent != null) {
            if (calIntent.hasExtra(TemperatureContract.SensorEntry.TABLE_NAME)) {
                //sensorId = Long.parseLong(calIntent.getStringExtra("_id"), 10);
                currentSensor = (ContentValues) calIntent.getParcelableExtra(TemperatureContract.SensorEntry.TABLE_NAME);
                _ID = currentSensor.getAsLong(TemperatureContract.SensorEntry._ID);
                sensorId = currentSensor.getAsInteger(TemperatureContract.SensorEntry.COLUMN_SENSORID);
                address = currentSensor.getAsString(TemperatureContract.SensorEntry.COLUMN_ADDRESS);

                getActivity().setTitle("Programming " + sensorId);
            }
        }

        return rootView;
    }

    @Override
    public void onClick(View v) {
        String message;
        Intent intent;
        View rootView = v.getRootView();
        Chronometer chronoWarm = (Chronometer) rootView.findViewById(R.id.chrono_warm);
        Chronometer chronoCool = (Chronometer) rootView.findViewById(R.id.chrono_cool);
        EditText et_prog_setpoint = (EditText) rootView.findViewById(R.id.et_prog_setpoint);

        switch (v.getId()) {
            case R.id.bt_prog_run:
                Log.d("Life cyle", "bt_prog_run()");
                double setPoint = Double.parseDouble(et_prog_setpoint.getText().toString());
                message = "W|" + UtilitySingleton.getInstance().formatTemperature(setPoint, 6);
                intent = new Intent(Constants.MESSAGE_TO_ARDUINO).putExtra(Intent.EXTRA_TEXT, message);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                chronoWarm.setBase(SystemClock.elapsedRealtime());
                chronoWarm.start();
                break;
            case R.id.bt_prog_run_stop:
                Log.d("Life cyle", "bt_prog_run_stop()");
                message = "W";
                intent = new Intent(Constants.MESSAGE_TO_ARDUINO).putExtra(Intent.EXTRA_TEXT, message);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                chronoWarm.stop();
                break;
            case R.id.bt_prog_cool:
                Log.d("Life cyle", "bt_prog_cool()");
                message = "F";
                intent = new Intent(Constants.MESSAGE_TO_ARDUINO).putExtra(Intent.EXTRA_TEXT, message);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                chronoCool.setBase(SystemClock.elapsedRealtime());
                chronoCool.start();
                break;
            case R.id.bt_prog_cool_stop:
                Log.d("Life cyle", "bt_prog_cool_stop()");
                message = "F";
                intent = new Intent(Constants.MESSAGE_TO_ARDUINO).putExtra(Intent.EXTRA_TEXT, message);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                chronoCool.stop();
                break;
            default:
                break;
        }
    }

}
