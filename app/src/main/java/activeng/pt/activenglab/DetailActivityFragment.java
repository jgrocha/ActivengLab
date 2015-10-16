package activeng.pt.activenglab;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Random;

import activeng.pt.activenglab.data.TemperatureContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private SensorCursorAdapter mySensorCursorAdapter;

    private final Handler mHandler = new Handler();

    private Runnable mTimer2;
    private LineGraphSeries<DataPoint> mSeries2;
    private double graph2LastXValue = 5d;

    BroadcastReceiver connectionUpdates;
    private EditText etCurrentRead;

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mySensorCursorAdapter = new SensorCursorAdapter(getActivity(), null, 0);
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_sensor);
        listView.setAdapter(mySensorCursorAdapter);

        etCurrentRead = (EditText) rootView.findViewById(R.id.etLabel_BeforeListView);
        if (etCurrentRead == null) {
            Log.d("ActivEng", "DetailActivityFragment R.id.etLabel_BeforeListView failed!");
        } else {
            Log.d("ActivEng", "DetailActivityFragment R.id.etLabel_BeforeListView OK!");
        }

        connectionUpdates = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle extras = intent.getExtras();
                Log.d("ActivEng", "DetailActivityFragment --> onReceive");
                if (extras != null) {
                    String temperatureStr = extras.getString(Intent.EXTRA_TEXT);
                    Log.d("ActivEng", temperatureStr);
                    //Log.d("ActivEng", etCurrentRead.getText().toString());
                    etCurrentRead.setText(temperatureStr);
                    Double t;
                    try {
                        t = Double.parseDouble(temperatureStr); // Make use of autoboxing.  It's also easier to read.
                    } catch (NumberFormatException e) {
                        t = 0.0d;
                    }
                    graph2LastXValue += 1d;
                    mSeries2.appendData(new DataPoint(graph2LastXValue, t), true, 40);
                }
            }
        };

        GraphView graph2 = (GraphView) rootView.findViewById(R.id.graph);
        mSeries2 = new LineGraphSeries<DataPoint>();
        graph2.addSeries(mSeries2);
        graph2.getViewport().setXAxisBoundsManual(true);
        graph2.getViewport().setMinX(0);
        graph2.getViewport().setMaxX(40);

        return rootView;
    }

    //@Override
    //public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    //    inflater.inflate(R.menu.bluetooth_chat, menu);
    //}

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        Bundle args = new Bundle();
        Uri uri;
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            uri = Uri.parse(intent.getStringExtra(Intent.EXTRA_TEXT));
            Log.d("SensorCursorAdapter", uri.toString());
            args.putParcelable("URI", uri);
        }
        getLoaderManager().initLoader(0, args, this);
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
        mySensorCursorAdapter.swapCursor(data);
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d("ActivEng", "DetailActivityFragment registerReceiver");
        getActivity().registerReceiver(this.connectionUpdates, new IntentFilter(Constants.MESSAGE_TEMPERATURE));

        //mTimer2 = new Runnable() {
        //    @Override
        //    public void run() {
        //        graph2LastXValue += 1d;
        //        mSeries2.appendData(new DataPoint(graph2LastXValue, getRandom()), true, 40);
        //        mHandler.postDelayed(this, 1000);
        //    }
        //};
        //mHandler.postDelayed(mTimer2, 1000);
    }

    @Override
    public void onPause() {
        mHandler.removeCallbacks(mTimer2);
        getActivity().unregisterReceiver(this.connectionUpdates);
        super.onPause();
    }

    private DataPoint[] generateData() {
        int count = 30;
        DataPoint[] values = new DataPoint[count];
        for (int i = 0; i < count; i++) {
            double x = i;
            double f = mRand.nextDouble() * 0.15 + 0.3;
            double y = Math.sin(i * f + 2) + mRand.nextDouble() * 0.3;
            DataPoint v = new DataPoint(x, y);
            values[i] = v;
        }
        return values;
    }

    double mLastRandom = 2;
    Random mRand = new Random();

    private double getRandom() {
        return mLastRandom += mRand.nextDouble() * 0.5 - 0.25;
    }

}
