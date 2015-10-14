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
        return LayoutInflater.from(context).inflate(R.layout.sensor, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView tvSensor_ID = (TextView) view.findViewById(R.id.tvSensor_ID);
        TextView tvSensor_Location = (TextView) view.findViewById(R.id.tvSensor_Location);
        TextView tvSensor_SensorType = (TextView) view.findViewById(R.id.tvSensor_SensorType);
        // Extract properties from cursor
        int sensorId = cursor.getInt(cursor.getColumnIndexOrThrow(TemperatureContract.SensorEntry._ID));
        String sensorLocation = cursor.getString(cursor.getColumnIndexOrThrow(TemperatureContract.SensorEntry.COLUMN_LOCATION));
        String sensorType = cursor.getString(cursor.getColumnIndexOrThrow(TemperatureContract.SensorEntry.COLUMN_SENSORTYPE));
        // Populate fields with extracted properties
        tvSensor_ID.setText(String.valueOf(sensorId));
        tvSensor_Location.setText(sensorLocation);
        tvSensor_SensorType.setText(sensorType);
    }
}
