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

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class TemperatureProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    // http://developer.android.com/guide/topics/providers/content-provider-creating.html
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private TemperatureDbHelper mOpenHelper;

    static final int TEMPERATURE = 100;
    static final int SENSOR = 300;
    static final int SENSOR_BY_SENSORID = 302;
    static final int SENSOR_BY_ID_AND_ADDRESS = 303;
    static final int TEMPERATURE_AFTER = 401;
    static final int CALIBRATION = 700;

    static final int TEMPERATURE_WITH_LOCATION = 101;
    static final int TEMPERATURE_WITH_LOCATION_AND_DATE = 102;

    static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        // public static final String CONTENT_AUTHORITY = "com.example.android.bluetoothchat";
        final String authority = TemperatureContract.CONTENT_AUTHORITY;
        // For each type of URI you want to add, create a corresponding code.
        // matcher.addURI("com.example.android.bluetoothchat", "temperature", 100);
        matcher.addURI(authority, TemperatureContract.PATH_TEMPERATURE, TEMPERATURE);
        // matcher.addURI("com.example.android.bluetoothchat", "sensor", 300);
        matcher.addURI(authority, TemperatureContract.PATH_SENSOR, SENSOR);
        // matcher.addURI("com.example.android.bluetoothchat", "calibration", 700);
        matcher.addURI(authority, TemperatureContract.PATH_CALIBRATION, CALIBRATION);

        matcher.addURI(authority, TemperatureContract.PATH_SENSOR + "/#", SENSOR_BY_SENSORID);
        matcher.addURI(authority, TemperatureContract.PATH_SENSOR + "/#/*", SENSOR_BY_ID_AND_ADDRESS);
        matcher.addURI(authority, TemperatureContract.PATH_TEMPERATURE + "/#/*/*", TEMPERATURE_AFTER);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new TemperatureDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);
        Log.d("TestProvider", "hello... this is getType()");
        switch (match) {
            case SENSOR:
                return TemperatureContract.SensorEntry.CONTENT_DIR_TYPE;
            case SENSOR_BY_SENSORID:
                return TemperatureContract.SensorEntry.CONTENT_DIR_TYPE;
            case SENSOR_BY_ID_AND_ADDRESS:
                return TemperatureContract.SensorEntry.CONTENT_DIR_TYPE;
            case TEMPERATURE_AFTER:
                return TemperatureContract.SensorEntry.CONTENT_DIR_TYPE;
            case TEMPERATURE:
                return TemperatureContract.TemperatureEntry.CONTENT_DIR_TYPE;
            case CALIBRATION:
                return TemperatureContract.CalibrationEntry.CONTENT_DIR_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "sensor"
            case SENSOR: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        TemperatureContract.SensorEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "sensor/2"
            case SENSOR_BY_SENSORID: {
                String sensorId = TemperatureContract.SensorEntry.getSensorIdFromUri(uri);
                Log.d("SQLite3", "sensorId: " + sensorId);

                retCursor = mOpenHelper.getReadableDatabase().query(
                        TemperatureContract.SensorEntry.TABLE_NAME,
                        projection,                 // columns
                        "sensorid = ?",                  // where filter, with ? for values
                        new String[]{sensorId},     // values for ?
                        null,                       // groupBy
                        null,                       // having
                        sortOrder                   // orderBy
                );
                break;
            }
            // "sensor/2/30:14:12:18:06:34"
            case SENSOR_BY_ID_AND_ADDRESS: {
                String sensorId = TemperatureContract.SensorEntry.getSensorIdFromUri(uri);
                String sensorAddress = TemperatureContract.SensorEntry.getSensorAddressFromUri(uri);

                Log.d("SQLite3", "sensorId: " + sensorId + " sensorAddress: " + sensorAddress);

                retCursor = mOpenHelper.getReadableDatabase().query(
                        TemperatureContract.SensorEntry.TABLE_NAME,
                        projection,                                 // columns
                        "sensorid = ? AND address = ?",            // where filter, with ? for values
                        new String[]{sensorId, sensorAddress},    // values for ?
                        null,                       // groupBy
                        null,                       // having
                        sortOrder                   // orderBy
                );
                break;
            }
            // "temperature"
            case TEMPERATURE_AFTER: {
                String sensorId = TemperatureContract.TemperatureEntry.getSensorIdFromUri(uri);
                String sensorAddress = TemperatureContract.TemperatureEntry.getSensorAddressFromUri(uri);
                String lastDate = TemperatureContract.TemperatureEntry.getLastTemperatureFromUri(uri);
                if (lastDate == null || lastDate.equals("null")) {
                    lastDate = "1970-01-01 00:00:00";
                }
                Log.d("SQLite3", "sensorId: " + sensorId + " sensorAddress: " + sensorAddress + " lastDate: " + lastDate);

                retCursor = mOpenHelper.getReadableDatabase().query(
                        TemperatureContract.TemperatureEntry.TABLE_NAME,
                        projection,                                 // columns
                        "sensorid = ? AND address = ? AND created > ?",            // where filter, with ? for values
                        new String[]{sensorId, sensorAddress, lastDate},    // values for ?
                        null,                       // groupBy
                        null,                       // having
                        sortOrder                   // orderBy
                );
                break;
            }
            // "temperature"
            case TEMPERATURE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        TemperatureContract.TemperatureEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "temperature"
            case CALIBRATION: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        TemperatureContract.CalibrationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case SENSOR: {
                long _id = db.insert(TemperatureContract.SensorEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = TemperatureContract.SensorEntry.buildSensorUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case TEMPERATURE: {
                long _id = db.insert(TemperatureContract.TemperatureEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = TemperatureContract.TemperatureEntry.buildTemperatureUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case CALIBRATION: {
                long _id = db.insert(TemperatureContract.CalibrationEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = TemperatureContract.CalibrationEntry.buildCalibrationUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if (null == selection) selection = "1";
        switch (match) {
            case SENSOR:
                rowsDeleted = db.delete(
                        TemperatureContract.SensorEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case SENSOR_BY_ID_AND_ADDRESS:
                String sensorId = TemperatureContract.SensorEntry.getSensorIdFromUri(uri);
                String sensorAddress = TemperatureContract.SensorEntry.getSensorAddressFromUri(uri);
                Log.d("SQLite3", "sensorId: " + sensorId + " sensorAddress: " + sensorAddress);
                rowsDeleted = db.delete(
                        TemperatureContract.SensorEntry.TABLE_NAME, "sensorid = ? AND address = ?", new String[]{sensorId, sensorAddress});
                Log.d("SQLite3", "sensorId: " + sensorId + " sensorAddress: " + sensorAddress + " rowsDeleted: " + rowsDeleted);
                break;
            case TEMPERATURE:
                rowsDeleted = db.delete(
                        TemperatureContract.TemperatureEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case CALIBRATION:
                rowsDeleted = db.delete(
                        TemperatureContract.CalibrationEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case SENSOR:
                rowsUpdated = db.update(TemperatureContract.SensorEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case TEMPERATURE:
                rowsUpdated = db.update(TemperatureContract.TemperatureEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case CALIBRATION:
                rowsUpdated = db.update(TemperatureContract.CalibrationEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TEMPERATURE:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(TemperatureContract.TemperatureEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}