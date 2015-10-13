package activeng.pt.activenglab;

import android.content.Intent;
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

/**
 * A placeholder fragment containing a simple view.
 */
public class CalibrationActivityFragment extends Fragment implements OnClickListener {

    public CalibrationActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_calibration, container, false);

        Button btnCalculate = (Button) rootView.findViewById(R.id.cal_button_calculate);
        btnCalculate.setOnClickListener(this);
        Button btnSave = (Button) rootView.findViewById(R.id.cal_button_save);
        btnSave.setOnClickListener(this);

        // The detail Activity called via intent.  Inspect the intent for forecast data.
        //Intent intent = getActivity().getIntent();
        //if (intent != null) {
        //    if (intent.hasExtra(Intent.EXTRA_TEXT)) {
        //        String sensorStr = intent.getStringExtra(Intent.EXTRA_TEXT);
        //        ((TextView) rootView.findViewById(R.id.calibration_text))
        //                .setText(sensorStr);
        //    } else {
        //        ((TextView) rootView.findViewById(R.id.calibration_text))
        //                .setText("Missing sensor to calabration");
        //    }
        //}

        // http://stackoverflow.com/questions/33090978/change-listview-via-custom-simplecursoradapter

        return rootView;

        // return inflater.inflate(R.layout.fragment_calibration, container, false);
    }

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
                break;
            default:
                break;
        }
    }

}
