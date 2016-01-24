package activeng.pt.activenglab;

import android.content.Intent;
import android.database.Cursor;
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

    private ItemSensorCursorAdapter myItemSensorCursorAdapter;

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

}
