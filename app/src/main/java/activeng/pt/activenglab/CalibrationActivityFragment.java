package activeng.pt.activenglab;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jjoe64.graphview.series.DataPoint;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

/**
 * A placeholder fragment containing a simple view.
 */
public class CalibrationActivityFragment extends Fragment implements OnClickListener {

    BroadcastReceiver connectionUpdates;
    private EditText cal_current_read;
    private EditText cal_new_read;

    private final NumberFormat f = NumberFormat.getInstance();

    private long sensorId = 0;
    private double cal_a, cal_b;

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

        if (f instanceof DecimalFormat) {
            //((DecimalFormat)f).setDecimalSeparatorAlwaysShown(true);
            f.setMaximumFractionDigits(6);
            f.setMinimumFractionDigits(6);
            DecimalFormatSymbols custom = new DecimalFormatSymbols();
            custom.setDecimalSeparator('.');
            ((DecimalFormat)f).setDecimalFormatSymbols(custom);
        }

        cal_current_read = (EditText) rootView.findViewById(R.id.cal_current_read);
        cal_new_read = (EditText) rootView.findViewById(R.id.cal_new_read);

        connectionUpdates = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle extras = intent.getExtras();
                Log.d("ActivEng", "CalibrationActivityFragment --> onReceive");
                if (extras != null) {
                    String temperatureStr = extras.getString(Intent.EXTRA_TEXT);
                    Log.d("ActivEng", temperatureStr);
                    //Log.d("ActivEng", etCurrentRead.getText().toString());
                    cal_current_read.setText(temperatureStr);
                    cal_new_read.setText(temperatureStr);
                }
            }
        };

        Intent calIntent = getActivity().getIntent();
        if (calIntent != null) {
            if (calIntent.hasExtra("_id")) {
                //sensorId = Long.parseLong(calIntent.getStringExtra("_id"), 10);
                sensorId = calIntent.getLongExtra("_id", 0);
            }
            if (calIntent.hasExtra("cal_a")) {
                //cal_a = Double.parseDouble(calIntent.getStringExtra("cal_a"));
                cal_a = calIntent.getDoubleExtra("cal_a", 0.0);
                ((TextView) rootView.findViewById(R.id.cal_current_offset)).setText(f.format(cal_a));
            }
            if (calIntent.hasExtra("cal_b")) {
                //cal_b = Double.parseDouble(calIntent.getStringExtra("cal_b"));
                cal_b = calIntent.getDoubleExtra("cal_b", 1.0);
                ((TextView) rootView.findViewById(R.id.cal_current_gain)).setText(f.format(cal_b));
            }
        }

        // http://stackoverflow.com/questions/33090978/change-listview-via-custom-simplecursoradapter

        return rootView;

        // return inflater.inflate(R.layout.fragment_calibration, container, false);
    }

    // et_Amount

    @Override
    public void onClick(View v) {
        View rootView = v.getRootView();
        switch(v.getId()){
            case R.id.cal_button_calculate :
                Log.d("Life cyle", "calculate()");
                EditText cal_read_high_text = (EditText) rootView.findViewById(R.id.cal_read_high);
                double cal_read_high = Double.parseDouble(cal_read_high_text.getText().toString());
                ((EditText) rootView.findViewById(R.id.cal_new_offset)).setText(String.valueOf(cal_read_high * 2));
                Log.d("Life cyle", "calculate()" + String.valueOf(cal_read_high) );
                break;
            case R.id.cal_button_save :
                Log.d("Life cyle", "save()");
                Intent intent = new Intent(Constants.MESSAGE_TO_ARDUINO).putExtra(Intent.EXTRA_TEXT, "T1445177600");
                getActivity().sendBroadcast(intent);
                break;
            default:
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("ActivEng", "CalibrationActivityFragment registerReceiver onResume()");
        getActivity().registerReceiver(this.connectionUpdates, new IntentFilter(Constants.MESSAGE_TEMPERATURE));
    }

    @Override
    public void onPause() {
        Log.d("ActivEng", "CalibrationActivityFragment unregisterReceiver onPause()");
        getActivity().unregisterReceiver(this.connectionUpdates);
        super.onPause();
    }
}
