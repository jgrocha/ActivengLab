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
package activeng.pt.activenglab.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Manages a local database for weather data.
 */
public class TemperatureDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    //private static final int DATABASE_VERSION = 1;
    private static final int DATABASE_VERSION = 2; // 2016-02-20
    //private static final int DATABASE_VERSION = 3;

    static final String DATABASE_NAME = "temperature.db";

    public TemperatureDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        /*
        CREATE TABLE sensor(
            _id INTEGER PRIMARY KEY autoincrement,
            sensorid integer NOT NULL,
            address TEXT NOT NULL,
            location TEXT NOT NULL,
            installdate DATETIME DEFAULT CURRENT_TIMESTAMP,
            sensortype TEXT NOT NULL,
            metric INTEGER DEFAULT 1 NOT NULL,
            calibrated INTEGER DEFAULT 0 NOT NULL,
            quantity CHAR(1) DEFAULT 'T' NOT NULL,
            decimalplaces SMALLINT DEFAULT 3 NOT NULL,
            cal_a FLOAT DEFAULT 0 NOT NULL,
            cal_b FLOAT DEFAULT 1 NOT NULL,
            read_interval INTEGER DEFAULT 2000 NOT NULL,
            record_sample INTEGER DEFAULT 1 NOT NULL,
            unique (sensorid, address)
        );
        create unique index sensor_idx ON sensor(sensorid, address);
        */
        final String SQL_CREATE_SENSOR_TABLE = "CREATE TABLE " + TemperatureContract.SensorEntry.TABLE_NAME + " (" +
                //TemperatureContract.SensorEntry._ID + " INTEGER PRIMARY KEY," +
                TemperatureContract.SensorEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                TemperatureContract.SensorEntry.COLUMN_SENSORID + " INTEGER NOT NULL, " +
                TemperatureContract.SensorEntry.COLUMN_ADDRESS + " TEXT NOT NULL, " +
                TemperatureContract.SensorEntry.COLUMN_LOCATION + " TEXT NOT NULL, " +
                TemperatureContract.SensorEntry.COLUMN_INSTALLDATE + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                TemperatureContract.SensorEntry.COLUMN_SENSORTYPE + " TEXT NOT NULL, " +
                TemperatureContract.SensorEntry.COLUMN_METRIC + " INTEGER DEFAULT 1 NOT NULL, " +
                TemperatureContract.SensorEntry.COLUMN_CALIBRATED + " INTEGER DEFAULT 0 NOT NULL, " +
                TemperatureContract.SensorEntry.COLUMN_QUANTITY + " CHAR(1) DEFAULT 'T' NOT NULL, " +
                TemperatureContract.SensorEntry.COLUMN_DECIMALPLACES + " INTEGER DEFAULT 3 NOT NULL, " +
                TemperatureContract.SensorEntry.COLUMN_CAL_A + " FLOAT DEFAULT 0 NOT NULL, " +
                TemperatureContract.SensorEntry.COLUMN_CAL_B + " FLOAT DEFAULT 1 NOT NULL, " +
                TemperatureContract.SensorEntry.COLUMN_READ_INTERVAL + " INTEGER DEFAULT 2000 NOT NULL, " +
                TemperatureContract.SensorEntry.COLUMN_RECORD_SAMPLE + " INTEGER DEFAULT 1 NOT NULL, " +
                TemperatureContract.SensorEntry.LAST_VALUE + " FLOAT, " +
                TemperatureContract.SensorEntry.LAST_READ + " DATETIME, " +
                "unique (" + TemperatureContract.SensorEntry.COLUMN_SENSORID + ", " +
                TemperatureContract.SensorEntry.COLUMN_ADDRESS + ")" +
                ");";
        Log.d("ActivEng", SQL_CREATE_SENSOR_TABLE);
        final String SQL_CREATE_SENSOR_TABLE_INDEX = "CREATE UNIQUE INDEX " +
                TemperatureContract.SensorEntry.TABLE_NAME + "_idx ON " +
                TemperatureContract.SensorEntry.TABLE_NAME + " ( " +
                TemperatureContract.SensorEntry.COLUMN_SENSORID + ", " +
                TemperatureContract.SensorEntry.COLUMN_ADDRESS + " );";
        Log.d("ActivEng", SQL_CREATE_SENSOR_TABLE_INDEX);

        sqLiteDatabase.execSQL(SQL_CREATE_SENSOR_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_SENSOR_TABLE_INDEX);

        /*
        CREATE TABLE temperature(
            _id INTEGER PRIMARY KEY autoincrement,
            sensorid integer NOT NULL,
            created DATETIME DEFAULT CURRENT_TIMESTAMP,
            value FLOAT NOT NULL,
            metric INTEGER DEFAULT 1 NOT NULL,
            calibrated INTEGER DEFAULT 0 NOT NULL,
            FOREIGN KEY(sensorid) REFERENCES sensor(_id)
        );
        */
        final String SQL_CREATE_TEMPERATURE_TABLE = "CREATE TABLE " + TemperatureContract.TemperatureEntry.TABLE_NAME + " (" +
                // Why AutoIncrement here, and not above?
                // Unique keys will be auto-generated in either case.  But for weather
                // forecasting, it's reasonable to assume the user will want information
                // for a certain date and all dates *following*, so the forecast data
                // should be sorted accordingly.
                TemperatureContract.TemperatureEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                // the ID of the location entry associated with this weather data
                TemperatureContract.TemperatureEntry.COLUMN_SENSORID + " INTEGER NOT NULL, " +
                TemperatureContract.TemperatureEntry.COLUMN_ADDRESS + " TEXT NOT NULL, " +
                TemperatureContract.TemperatureEntry.COLUMN_CREATED + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                TemperatureContract.TemperatureEntry.COLUMN_VALUE + " FLOAT NOT NULL, " +
                TemperatureContract.TemperatureEntry.COLUMN_METRIC + " INTEGER DEFAULT 1 NOT NULL," +
                TemperatureContract.TemperatureEntry.COLUMN_CALIBRATED + " INTEGER DEFAULT 0 NOT NULL, " +

                // Set up the location column as a foreign key to location table.
                " FOREIGN KEY (" + TemperatureContract.TemperatureEntry.COLUMN_SENSORID + ", " +
                TemperatureContract.TemperatureEntry.COLUMN_ADDRESS + ") REFERENCES " +
                TemperatureContract.SensorEntry.TABLE_NAME + " (" +
                TemperatureContract.SensorEntry.COLUMN_SENSORID + ", " +
                TemperatureContract.SensorEntry.COLUMN_ADDRESS + ") ON DELETE CASCADE " +
                ");";
        Log.d("ActivEng", SQL_CREATE_TEMPERATURE_TABLE);
        final String SQL_CREATE_TEMPERATURE_TABLE_INDEX = "CREATE INDEX " +
                TemperatureContract.TemperatureEntry.TABLE_NAME + "_idx ON " +
                TemperatureContract.TemperatureEntry.TABLE_NAME + "( " +
                TemperatureContract.TemperatureEntry.COLUMN_SENSORID + ", " +
                TemperatureContract.TemperatureEntry.COLUMN_ADDRESS + ", " +
                TemperatureContract.TemperatureEntry.COLUMN_CREATED + " );";
        Log.d("ActivEng", SQL_CREATE_TEMPERATURE_TABLE_INDEX);

        sqLiteDatabase.execSQL(SQL_CREATE_TEMPERATURE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TEMPERATURE_TABLE_INDEX);

        /*
        CREATE TABLE calibration(
            _id INTEGER PRIMARY KEY autoincrement,
            sensorid integer NOT NULL,
            created DATETIME DEFAULT CURRENT_TIMESTAMP,
            cal_a_old FLOAT NOT NULL,
            cal_b_old FLOAT NOT NULL,
            cal_a_new FLOAT NOT NULL,
            cal_b_new FLOAT NOT NULL,
            ref_value_high FLOAT NOT NULL,
            ref_value_low FLOAT NOT NULL,
            read_value_high FLOAT NOT NULL,
            read_value_low FLOAT NOT NULL,
            FOREIGN KEY(sensorid) REFERENCES sensor(_id)
        );
         */
        final String SQL_CREATE_CALIBRATION_TABLE = "CREATE TABLE " + TemperatureContract.CalibrationEntry.TABLE_NAME + " (" +
                TemperatureContract.CalibrationEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                // the ID of the location entry associated with this weather data
                TemperatureContract.CalibrationEntry.COLUMN_SENSORID + " INTEGER NOT NULL, " +
                TemperatureContract.CalibrationEntry.COLUMN_ADDRESS + " TEXT NOT NULL, " +
                TemperatureContract.CalibrationEntry.COLUMN_CREATED + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                TemperatureContract.CalibrationEntry.COLUMN_CAL_A_OLD + " FLOAT NOT NULL, " +
                TemperatureContract.CalibrationEntry.COLUMN_CAL_B_OLD + " FLOAT NOT NULL, " +
                TemperatureContract.CalibrationEntry.COLUMN_CAL_A_NEW + " FLOAT NOT NULL, " +
                TemperatureContract.CalibrationEntry.COLUMN_CAL_B_NEW + " FLOAT NOT NULL, " +
                TemperatureContract.CalibrationEntry.COLUMN_REF_VALUE_HIGH + " FLOAT NOT NULL, " +
                TemperatureContract.CalibrationEntry.COLUMN_REF_VALUE_LOW + " FLOAT NOT NULL, " +
                TemperatureContract.CalibrationEntry.COLUMN_READ_VALUE_HIGH + " FLOAT NOT NULL, " +
                TemperatureContract.CalibrationEntry.COLUMN_READ_VALUE_LOW + " FLOAT NOT NULL, " +
                // Set up the location column as a foreign key to location table.
                " FOREIGN KEY (" + TemperatureContract.CalibrationEntry.COLUMN_SENSORID + ", " +
                TemperatureContract.CalibrationEntry.COLUMN_ADDRESS + ") REFERENCES " +
                TemperatureContract.SensorEntry.TABLE_NAME + " (" +
                TemperatureContract.SensorEntry.COLUMN_SENSORID + ", " +
                TemperatureContract.SensorEntry.COLUMN_ADDRESS + ") ON DELETE CASCADE " +
                ");";
        Log.d("ActivEng", SQL_CREATE_CALIBRATION_TABLE);
        //final String SQL_CREATE_CALIBRATION_TABLE_INDEX = "CREATE UNIQUE INDEX " +
        //        TemperatureContract.SensorEntry.TABLE_NAME + "_idx ON " +
        //        TemperatureContract.SensorEntry.TABLE_NAME + "( " +
        //        TemperatureContract.SensorEntry.COLUMN_SENSORID + ", " +
        //        TemperatureContract.SensorEntry.COLUMN_ADDRESS + " );";
        final String SQL_CREATE_CALIBRATION_TABLE_INDEX = "CREATE INDEX " +
                TemperatureContract.CalibrationEntry.TABLE_NAME + "_idx ON " +
                TemperatureContract.CalibrationEntry.TABLE_NAME + "( " +
                TemperatureContract.CalibrationEntry.COLUMN_SENSORID + ", " +
                TemperatureContract.CalibrationEntry.COLUMN_ADDRESS + ", " +
                TemperatureContract.CalibrationEntry.COLUMN_CREATED + " );";
        Log.d("ActivEng", SQL_CREATE_CALIBRATION_TABLE_INDEX);

        sqLiteDatabase.execSQL(SQL_CREATE_CALIBRATION_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_CALIBRATION_TABLE_INDEX);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        //sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TemperatureContract.SensorEntry.TABLE_NAME);
        //sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TemperatureContract.TemperatureEntry.TABLE_NAME);
        //sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TemperatureContract.CalibrationEntry.TABLE_NAME);
        //onCreate(sqLiteDatabase);
        if ((newVersion > oldVersion) && (oldVersion == 1)) {
            Log.d("ActivEng", "Upgrade SQLite: " + oldVersion + " → " + newVersion);
            sqLiteDatabase.execSQL("ALTER TABLE " + TemperatureContract.SensorEntry.TABLE_NAME + " ADD COLUMN " + TemperatureContract.SensorEntry.LAST_VALUE + " FLOAT");
            sqLiteDatabase.execSQL("ALTER TABLE " + TemperatureContract.SensorEntry.TABLE_NAME + " ADD COLUMN " + TemperatureContract.SensorEntry.LAST_READ + " DATETIME");
        }
        //if ((newVersion > oldVersion) && (oldVersion == 2)) {
        //    sqLiteDatabase.execSQL("ALTER TABLE " + TemperatureContract.SensorEntry.TABLE_NAME + " ADD COLUMN " + TemperatureContract.SensorEntry.COLUMN_ADDRESS + " TEXT NOT NULL");
        //}
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }
}