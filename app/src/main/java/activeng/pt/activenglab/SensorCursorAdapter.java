package activeng.pt.activenglab;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import activeng.pt.activenglab.data.TemperatureContract;

/**
 * Created by jgr on 14-10-2015.
 */
public class SensorCursorAdapter extends CursorAdapter {

    public SensorCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_sensor, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView tvID = (TextView) view.findViewById(R.id.tvID);
        TextView tvLocation = (TextView) view.findViewById(R.id.tvLocation);
        TextView tvSensorType = (TextView) view.findViewById(R.id.tvSensorType);
        // Extract properties from cursor
        int sensorId = cursor.getInt(cursor.getColumnIndexOrThrow(TemperatureContract.SensorEntry._ID));
        String sensorLocation = cursor.getString(cursor.getColumnIndexOrThrow(TemperatureContract.SensorEntry.COLUMN_LOCATION));
        String sensorType = cursor.getString(cursor.getColumnIndexOrThrow(TemperatureContract.SensorEntry.COLUMN_SENSORTYPE));
        // Populate fields with extracted properties
        tvID.setText(String.valueOf(sensorId));
        tvLocation.setText(sensorLocation);
        tvSensorType.setText(sensorType);
    }
}
