package activeng.pt.activenglab;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import activeng.pt.activenglab.data.TemperatureContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private long _SENSORID = 0;

    private ContentValues currentSensor = null;

    private SensorCursorAdapter mySensorCursorAdapter;

    private LineGraphSeries<DataPoint> series;

    BroadcastReceiver connectionUpdates;
    private boolean registered;

    private EditText etCurrentRead;

    private final NumberFormat f = NumberFormat.getInstance();

    public DetailActivityFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mySensorCursorAdapter = new SensorCursorAdapter(getActivity(), null, 0);
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_sensor);
        listView.setAdapter(mySensorCursorAdapter);

        //setHasOptionsMenu(true);

        etCurrentRead = (EditText) rootView.findViewById(R.id.etLabel_BeforeListView);
        if (etCurrentRead == null) {
            Log.d("ActivEng", "DetailActivityFragment R.id.etLabel_BeforeListView failed!");
        } else {
            Log.d("ActivEng", "DetailActivityFragment R.id.etLabel_BeforeListView OK!");
        }

        if (f instanceof DecimalFormat) {
            //((DecimalFormat)f).setDecimalSeparatorAlwaysShown(true);
            f.setMaximumFractionDigits(2);
            f.setMinimumFractionDigits(2);
            DecimalFormatSymbols custom = new DecimalFormatSymbols();
            custom.setDecimalSeparator('.');
            ((DecimalFormat) f).setDecimalFormatSymbols(custom);
        }

        UtilitySingleton.getInstance().init(getActivity().getApplicationContext());
        //assert(getActivity().getApplicationContext() == UtilitySingleton.get());

        connectionUpdates = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                double temp;
                String tempStr;
                long sensor, instant;
                Bundle extras = intent.getExtras();
                Log.d("ActivEng", "DetailActivityFragment --> onReceive");
                if (extras != null) {

                    temp = extras.getDouble(Constants.EXTRA_MSG_TEMP, -999);
                    tempStr = extras.getString(Constants.EXTRA_MSG_TEMP_STR);
                    sensor = extras.getLong(Constants.EXTRA_MSG_TEMP_SENSOR, 0);
                    instant = extras.getLong(Constants.EXTRA_MSG_TEMP_MILLIS, 0);

                    if (sensor == currentSensor.getAsInteger(TemperatureContract.SensorEntry.COLUMN_SENSORID)) {
                        etCurrentRead.setText(tempStr);
                        // TODO
                        // Create the graph only after we have enougth data.
                        // Log.d("ActivEng", "d5 " + temp.getMillis());
                        // series.appendData(new DataPoint(new Date(temp.getMillis()), temp.getValue()), true, 40);
                    }
                }
            }
        };

        // generate Dates
        // TODO
        // Get data from RTD for some time, to show the graph
        // The graph will only show after a few moments...
        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.SECOND, -30);
        Date d4 = calendar.getTime();
        Log.d("ActivEng", "d4 " + d4.toString() + " " + d4.getTime());

        calendar.add(Calendar.SECOND, -30);
        Date d3 = calendar.getTime();
        Log.d("ActivEng", "d3 " + d3.toString() + " " + d3.getTime());

        calendar.add(Calendar.SECOND, -30);
        Date d2 = calendar.getTime();
        Log.d("ActivEng", "d2 " + d2.toString() + " " + d2.getTime());

        calendar.add(Calendar.SECOND, -30);
        Date d1 = calendar.getTime();
        Log.d("ActivEng", "d1 " + d1.toString() + " " + d1.getTime());

        GraphView graph = (GraphView) rootView.findViewById(R.id.graph);

        // you can directly pass Date objects to DataPoint-Constructor
        // this will convert the Date to double via Date#getTime()
        series = new LineGraphSeries<DataPoint>(new DataPoint[]{
                new DataPoint(d1, 1),
                new DataPoint(d2, 5),
                new DataPoint(d3, 3),
                new DataPoint(d4, 22),
        });
        graph.addSeries(series);

        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getActivity(), format));
        graph.getGridLabelRenderer().setNumHorizontalLabels(4); // only 4 because of the space

        // set manual x bounds to have nice steps
        graph.getViewport().setMinX(d1.getTime());
        graph.getViewport().setMaxX(d4.getTime());
        graph.getViewport().setXAxisBoundsManual(true);

        //series.appendData(new DataPoint(d4, 7), true, 40);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Bundle args = new Bundle();
        Uri uri;
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(TemperatureContract.SensorEntry.COLUMN_SENSORID)) {
            _SENSORID = intent.getLongExtra(TemperatureContract.SensorEntry.COLUMN_SENSORID, 0);
            uri = TemperatureContract.SensorEntry.buildSensorUri(_SENSORID);
            Log.d("SensorCursorAdapter", uri.toString());
            args.putParcelable("URI", uri);
            getLoaderManager().initLoader(0, args, this);
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri mUri;
        if (args != null) {
            mUri = args.getParcelable("URI");
            Log.d("SensorCursorAdapter", "onCreateLoader: " + mUri.toString());

            return new CursorLoader(getActivity(),
                    mUri,
                    null, // projection
                    null, // selection
                    null, // selectionArgs
                    null); // sortOrder

        } else {
            return null;
        }
    }

    @Override
    // Called when a previously created loader is reset, making the data unavailable
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mySensorCursorAdapter.swapCursor(null);
    }

    // Called when a previously created loader has finished loading
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        //Cursor aux;
        Log.d("ActivEng", "DetailActivityFragment onLoadFinished. getCount() = " + data.getCount() + " getColumnCount() = " + data.getColumnCount());
        if (data.moveToFirst()) {
            if (currentSensor == null) {
                currentSensor = new ContentValues();
                DatabaseUtils.cursorRowToContentValues(data, currentSensor);
            }
        }
        //Log.d("ActivEng", "DetailActivityFragment onLoadFinished. sensorID = " + mysensorid + " getColumnCount() = " + sensorType);
        mySensorCursorAdapter.swapCursor(data);
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d("ActivEng", "DetailActivityFragment registerReceiver");
        //getActivity().registerReceiver(this.connectionUpdates, new IntentFilter(Constants.MESSAGE_TEMPERATURE));
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter filter = new IntentFilter(Constants.MESSAGE_TEMPERATURE);
        manager.registerReceiver(this.connectionUpdates, filter);
        registered = true;
    }

    @Override
    public void onPause() {
        if (registered) {
            //getActivity().unregisterReceiver(this.connectionUpdates);
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getActivity());
            manager.unregisterReceiver(this.connectionUpdates);
        }
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Toast toast;
        Intent intent;
        switch (id) {
            //case R.id.activity_menu_item:
            //    // Not implemented here
            //    return false;
            case R.id.detail_action_calibration:
                // Do Fragment menu item stuff here
                intent = new Intent(getActivity(), CalibrationActivity.class);
                intent.putExtra(TemperatureContract.SensorEntry.TABLE_NAME, currentSensor);
                startActivity(intent);
                return true;
            case R.id.detail_action_edit:
                // Do Fragment menu item stuff here
                //toast = Toast.makeText(getActivity().getApplicationContext(), "Edit sensor", Toast.LENGTH_SHORT);
                //toast.show();
                //((DetailActivity) getActivity()).done();

                intent = new Intent(getActivity(), EditActivity.class);
                intent.putExtra(TemperatureContract.SensorEntry.TABLE_NAME, currentSensor);
                startActivity(intent);

                return true;
            case R.id.detail_action_program:
                intent = new Intent(getActivity(), ProgramActivity.class);
                intent.putExtra(TemperatureContract.SensorEntry.TABLE_NAME, currentSensor);
                startActivity(intent);
                //((DetailActivity) getActivity()).done();
                return true;
            case R.id.detail_action_delete:
                // Do Fragment menu item stuff here
                //toast = Toast.makeText(getActivity().getApplicationContext(), "Delete sensor", Toast.LENGTH_SHORT);
                //toast.show();

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        int s = currentSensor.getAsInteger(TemperatureContract.SensorEntry.COLUMN_SENSORID);
                        String a = currentSensor.getAsString(TemperatureContract.SensorEntry.COLUMN_ADDRESS);
                        Uri mNewUri = TemperatureContract.SensorEntry.buildSensorIDAddressUri(s, a);
                        getActivity().getContentResolver().delete(mNewUri, null, null);

                        String message = "Sensor " + s + " at " + a + " was deleted.";
                        Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        // Get back to MainActivity
                        ((DetailActivity) getActivity()).done();
                    }
                };
                new AlertDialog.Builder(getActivity())
                        .setTitle("Delete sensor")
                        .setMessage("Do you really want to delete this sensor and all data already recorded?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(R.string.detail_action_delete, dialogClickListener)
                        .setNegativeButton(android.R.string.no, null).show();

                return true;
            default:
                break;
        }
        return false;
    }

}
