package activeng.pt.activenglab;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import activeng.pt.activenglab.data.TemperatureContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private ItemSensorCursorAdapter myItemSensorCursorAdapter;

    BroadcastReceiver connectionUpdates;
    private boolean registered;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        myItemSensorCursorAdapter = new ItemSensorCursorAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        //listView.setAdapter(myItemSensorCursorAdapter);

        //ItemSensorCursorAdapter todoAdapter = new ItemSensorCursorAdapter(getContext(), cursor, 0);
        // Attach cursor adapter to the ListView
        listView.setAdapter(myItemSensorCursorAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    //long _ID = cursor.getLong(cursor.getColumnIndex(TemperatureContract.SensorEntry._ID));
                    long _SENSORID = cursor.getLong(cursor.getColumnIndex(TemperatureContract.SensorEntry.COLUMN_SENSORID));
                    Intent intent = new Intent(getActivity(), DetailActivity.class);
                    intent.putExtra(TemperatureContract.SensorEntry.COLUMN_SENSORID, _SENSORID);
                    startActivity(intent);
                }
            }
        });

        connectionUpdates = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                double temp;
                String tempStr;
                long sensor, instant;
                Bundle extras = intent.getExtras();
                Log.d("ActivEng", "MainActivityFragment --> onReceive");
                if (extras != null) {

                    temp = extras.getDouble(Constants.EXTRA_MSG_TEMP, -999);
                    tempStr = extras.getString(Constants.EXTRA_MSG_TEMP_STR);
                    sensor = extras.getLong(Constants.EXTRA_MSG_TEMP_SENSOR, 0);
                    instant = extras.getLong(Constants.EXTRA_MSG_TEMP_MILLIS, 0);

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    //Date dateOn = new Date(instant*1000);
                    Date dateOn = new Date(instant);

                    // update sensor
                    ContentValues updatedValues = new ContentValues();
                    updatedValues.put(TemperatureContract.SensorEntry.LAST_VALUE, temp);
                    updatedValues.put(TemperatureContract.SensorEntry.LAST_READ, dateFormat.format(dateOn) );
                    int count = getContext().getContentResolver().update(
                            TemperatureContract.SensorEntry.CONTENT_URI, updatedValues, TemperatureContract.SensorEntry.COLUMN_SENSORID + "= ?",
                            new String[]{Long.toString(sensor)});
                    if (count == 1) {
                        Log.d("ActivEng", "MainActivityFragment --> getContext().getContentResolver().update()");
                    }
                    /*
                    if (sensor == currentSensor.getAsInteger(TemperatureContract.SensorEntry.COLUMN_SENSORID)) {
                        etCurrentRead.setText(tempStr);
                        // TODO
                        // Create the graph only after we have enougth data.
                        // Log.d("ActivEng", "d5 " + temp.getMillis());
                        // series.appendData(new DataPoint(new Date(temp.getMillis()), temp.getValue()), true, 40);
                    }
                    */
                }
            }
        };

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(0, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                TemperatureContract.SensorEntry.CONTENT_URI,
                //TemperatureContract.SensorEntry.buildSensorUri(2),
                null, // projection
                null, // selection
                null, // selectionArgs
                null); // sortOrder
    }

    @Override
    // Called when a previously created loader is reset, making the data unavailable
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        myItemSensorCursorAdapter.swapCursor(null);
    }

    // Called when a previously created loader has finished loading
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        myItemSensorCursorAdapter.swapCursor(data);
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d("ActivEng", "MainActivityFragment registerReceiver");
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
}
