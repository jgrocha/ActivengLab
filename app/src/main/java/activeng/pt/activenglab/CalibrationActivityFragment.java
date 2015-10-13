package activeng.pt.activenglab;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class CalibrationActivityFragment extends Fragment {

    public CalibrationActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_calibration, container, false);

        // The detail Activity called via intent.  Inspect the intent for forecast data.
        Intent intent = getActivity().getIntent();
        if (intent != null) {
            if (intent.hasExtra(Intent.EXTRA_TEXT)) {
                String sensorStr = intent.getStringExtra(Intent.EXTRA_TEXT);
                ((TextView) rootView.findViewById(R.id.calibration_text))
                        .setText(sensorStr);
            } else {
                ((TextView) rootView.findViewById(R.id.calibration_text))
                        .setText("Missing sensor to calabration");
            }
        }

        // http://stackoverflow.com/questions/33090978/change-listview-via-custom-simplecursoradapter

        return rootView;


        // return inflater.inflate(R.layout.fragment_calibration, container, false);
    }
}
