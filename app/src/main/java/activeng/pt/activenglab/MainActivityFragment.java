package activeng.pt.activenglab;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import activeng.pt.activenglab.data.TemperatureContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private SensorCursorAdapter mySensorCursorAdapter;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mySensorCursorAdapter = new SensorCursorAdapter(getActivity(), null, 0);

        //Cursor cursor = getContext().getContentResolver().query(TemperatureContract.SensorEntry.CONTENT_URI, null, null, null, null);
        //int count = 0;
        //while (cursor.moveToNext()) {
        //    long sensorId = cursor.getLong(cursor.getColumnIndex(TemperatureContract.SensorEntry._ID));
        //    String location = cursor.getString(cursor.getColumnIndex(TemperatureContract.SensorEntry.COLUMN_LOCATION));
        //    String sensorType = cursor.getString(cursor.getColumnIndex(TemperatureContract.SensorEntry.COLUMN_SENSORTYPE));
        //    Log.d("SQLite3", "Linha " + count + ": _ID: " + sensorId + " Location: " + location + " Type: " + sensorType);
        //    count++;
        //}
        //Log.d("SQLite3", "Com " + count + " linhas.");
        //// cursor.close();
        //
        //// Create some dummy data for the ListView.  Here's a sample weekly forecast
        //String[] data = {
        //        "Sensor 1 - Carnaxide, Portugal - 31/17",
        //        "Sensor 2 - Carnaxide, Portugal - 21/8",
        //        "Sensor 3 - Sofarimex - 22/13"
        //};
        //List<String> weekForecast = new ArrayList<String>(Arrays.asList(data));
        //
        //// Now that we have some dummy forecast data, create an ArrayAdapter.
        //// The ArrayAdapter will take data from a source (like our dummy forecast) and
        //// use it to populate the ListView it's attached to.
        //mySensorCursorAdapter =
        //        new ArrayAdapter<String>(
        //                getActivity(), // The current context (this activity)
        //                R.layout.list_item_forecast, // The name of the layout ID.
        //                R.id.list_item_forecast_textview, // The ID of the textview to populate.
        //                weekForecast);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        //listView.setAdapter(mySensorCursorAdapter);

        //SensorCursorAdapter todoAdapter = new SensorCursorAdapter(getContext(), cursor, 0);
        // Attach cursor adapter to the ListView
        listView.setAdapter(mySensorCursorAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    long sensorId = cursor.getLong(cursor.getColumnIndex(TemperatureContract.SensorEntry._ID));
                    String sensorType = cursor.getString(cursor.getColumnIndex(TemperatureContract.SensorEntry.COLUMN_SENSORTYPE));

                    //Intent intent = new Intent(getActivity(), DetailActivity.class).putExtra(Intent.EXTRA_TEXT, sensorId + sensorType);
                    // uri = Uri.parse(stringUri);
                    Intent intent = new Intent(getActivity(), DetailActivity.class).putExtra(Intent.EXTRA_TEXT, TemperatureContract.SensorEntry.buildSensorUri(sensorId).toString());
                    startActivity(intent);
                }
            }
        });

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
        mySensorCursorAdapter.swapCursor(null);
    }

    // Called when a previously created loader has finished loading
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mySensorCursorAdapter.swapCursor(data);
    }

}
