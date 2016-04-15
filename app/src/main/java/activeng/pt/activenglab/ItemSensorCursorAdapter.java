package activeng.pt.activenglab;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import activeng.pt.activenglab.data.TemperatureContract;

/**
 * Created by jgr on 14-10-2015.
 */
public class ItemSensorCursorAdapter extends CursorAdapter {

    public ItemSensorCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_sensor, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        //TextView tvID = (TextView) view.findViewById(R.id.tvID);
        //TextView tvAddress = (TextView) view.findViewById(R.id.tvAddress);
        TextView tvLocation = (TextView) view.findViewById(R.id.tvLocation);
        //TextView tvSensorType = (TextView) view.findViewById(R.id.tvSensorType);
        // TODO
        TextView tvSensorCurrentValue = (TextView) view.findViewById(R.id.tvSensorLastValue);
        TextView tvSensorLastRead = (TextView) view.findViewById(R.id.tvSensorLastRead);

        // Extract properties from cursor
        int sensorId = cursor.getInt(cursor.getColumnIndexOrThrow(TemperatureContract.SensorEntry.COLUMN_SENSORID));
        String sensorAddress = cursor.getString(cursor.getColumnIndexOrThrow(TemperatureContract.SensorEntry.COLUMN_ADDRESS));
        String sensorLocation = cursor.getString(cursor.getColumnIndexOrThrow(TemperatureContract.SensorEntry.COLUMN_LOCATION));
        String sensorType = cursor.getString(cursor.getColumnIndexOrThrow(TemperatureContract.SensorEntry.COLUMN_SENSORTYPE));
        String sensorQuantity = cursor.getString(cursor.getColumnIndexOrThrow(TemperatureContract.SensorEntry.COLUMN_QUANTITY));

        String quantity;
        switch (sensorQuantity.charAt(0)) {
            case 'T':
                quantity = "º";
                break;
            case 'H':
                quantity = "%";
                break;
            default:
                quantity = "";
        }

        // TODO
        double lastValue = cursor.getDouble(cursor.getColumnIndexOrThrow(TemperatureContract.SensorEntry.LAST_VALUE));
        String lastRead = cursor.getString(cursor.getColumnIndexOrThrow(TemperatureContract.SensorEntry.LAST_READ));

        // Populate fields with extracted properties
        //tvID.setText(String.valueOf(sensorId));
        //tvAddress.setText(sensorAddress);
        tvLocation.setText(sensorLocation);
        //tvSensorType.setText(sensorType);
        // TODO
        tvSensorCurrentValue.setText(String.valueOf(lastValue) + ' ' + quantity);

        if (lastRead != null) {
            try {
                //java.text.ParseException: Unparseable date: "2016-04-06 01:07:02" (at offset 10)
                Date lastread = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(lastRead);
                DateFormat df = new SimpleDateFormat("HH:mm:ss");
                String last = df.format(lastread);
                tvSensorLastRead.setText(last);
            } catch (ParseException e) {
                Log.d("ActivEng", "processSensor: ParseException: lastRead=" + lastRead);
                e.printStackTrace();
            }
        }


    }
}
