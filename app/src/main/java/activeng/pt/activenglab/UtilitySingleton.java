/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package activeng.pt.activenglab;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.text.format.Time;
import android.util.Log;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import activeng.pt.activenglab.data.TemperatureContract;

/*
we use a instance of the class (just one instance).

http://stackoverflow.com/questions/11165852/java-singleton-and-synchronization/11165926#11165926

 */
public class UtilitySingleton {

    private Context appContext;

    private final NumberFormat f = NumberFormat.getInstance();

    private UtilitySingleton() {
    }

    public void init(Context context) {
        if (appContext == null) {
            appContext = context;
        }
        if (f instanceof DecimalFormat) {
            //((DecimalFormat)f).setDecimalSeparatorAlwaysShown(true);
            f.setMaximumFractionDigits(2);
            f.setMinimumFractionDigits(2);
            DecimalFormatSymbols custom = new DecimalFormatSymbols();
            custom.setDecimalSeparator('.');
            ((DecimalFormat) f).setDecimalFormatSymbols(custom);
        }
    }

    private Context getContext() {
        return appContext;
    }

    public static Context get() {
        return getInstance().getContext();
    }

    private static class Loader {
        static UtilitySingleton INSTANCE = new UtilitySingleton();
    }

    public static UtilitySingleton getInstance() {
        return Loader.INSTANCE;
    }

    public String formatTemperature(double temperature, int decimals) {
        f.setMaximumFractionDigits(decimals);
        f.setMinimumFractionDigits(decimals);
        return f.format(temperature);
    }

    public void saveTemperature(Context mContext, double temperature, long sensorId, String address, long epoch) {
        Uri mNewUri;

        //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date dateOn = new Date(epoch*1000);

        ContentValues novosValues = new ContentValues();
        novosValues.put(TemperatureContract.TemperatureEntry.COLUMN_SENSORID, sensorId);
        novosValues.put(TemperatureContract.TemperatureEntry.COLUMN_ADDRESS, address);
        novosValues.put(TemperatureContract.TemperatureEntry.COLUMN_CREATED, dateFormat.format(dateOn) );
        novosValues.put(TemperatureContract.TemperatureEntry.COLUMN_VALUE, temperature);
        novosValues.put(TemperatureContract.TemperatureEntry.COLUMN_METRIC, 1);
        novosValues.put(TemperatureContract.TemperatureEntry.COLUMN_CALIBRATED, 0);

        mNewUri = mContext.getContentResolver().insert(
                TemperatureContract.TemperatureEntry.CONTENT_URI,   // the user dictionary content URI
                novosValues                          // the values to insert
        );

    }

    public Temperature processAndSaveMessage(Context mContext, String message, String address) {
        long sensor;
        Double t;
        long instant;
        String[] parts = message.split("\\|", -1);
        // R|1|26.430|4423
        //parts[0] = "R";
        //parts[1] = "1";
        //parts[2] = "26.430";
        //parts[3] = "1445279973"; // seconds, not milliseconds
        //Log.d("ActivEng", message);
        assert (message.charAt(0) == 'R');
        sensor = Integer.parseInt(parts[1]);
        try {
            t = Double.parseDouble(parts[2]);
            instant = Long.parseLong(parts[3]);
        } catch (NumberFormatException e) {
            t = 0.0d;
            instant = 0;
        }
        saveTemperature(mContext, t, sensor, address, instant);
        // TODO: number of decimals places
        return new Temperature(t, formatTemperature(t, 2), sensor, instant);
    }

}

final class Temperature {
    private final double value;
    private final String str;
    //private final Date datetime;
    private final long sensor;
    private final long millis;

    public Temperature(double temperature, String temperatureStr, long sensor, long dt) {
        this.value = temperature;
        this.str = temperatureStr;
        this.sensor = sensor;
        //this.datetime = new Date(dt);
        this.millis = dt * 1000;
    }

    public double getRead() {
        return value;
    }

    public String getReadString() {
        return str;
    }

    public long getReadSensor() {
        return sensor;
    }

    public long getReadMillis() {
        return millis;
    }
}
