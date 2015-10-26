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

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import activeng.pt.activenglab.data.TemperatureContract.CalibrationEntry;
import activeng.pt.activenglab.data.TemperatureContract.SensorEntry;
import activeng.pt.activenglab.data.TemperatureContract.TemperatureEntry;

public class TestInsertTemperatureData extends AndroidTestCase {

    public static final String LOG_TAG = TestInsertTemperatureData.class.getSimpleName();

    public void deleteAllRecordsFromProvider() {

        mContext.getContentResolver().delete(
                SensorEntry.CONTENT_URI,
                null,
                null
        );
        Cursor cursor = mContext.getContentResolver().query(
                SensorEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from sensor table during delete", 0, cursor.getCount());
        cursor.close();

        mContext.getContentResolver().delete(
                TemperatureEntry.CONTENT_URI,
                null,
                null
        );
        cursor = mContext.getContentResolver().query(
                TemperatureEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from temperature table during delete", 0, cursor.getCount());
        cursor.close();

        mContext.getContentResolver().delete(
                CalibrationEntry.CONTENT_URI,
                null,
                null
        );
        cursor = mContext.getContentResolver().query(
                CalibrationEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from calibration table during delete", 0, cursor.getCount());
        cursor.close();
    }

    /*
        Student: Refactor this function to use the deleteAllRecordsFromProvider functionality once
        you have implemented delete functionality there.
     */
    public void deleteAllRecords() {
        deleteAllRecordsFromProvider();
        Log.d(LOG_TAG, ">>> deleteAllRecordsFromProvider is NOT commented! ");
    }

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // Log.d(LOG_TAG, ">>> remove tudo o que já existe...! ");
        // deleteAllRecords();
    }

    public void testInsertTemperature() {
        // insert our test records into the database
        //TemperatureDbHelper dbHelper = new TemperatureDbHelper(mContext);
        //SQLiteDatabase db = dbHelper.getWritableDatabase();

        Uri mNewUri;
        ContentValues novosValues = new ContentValues();
        novosValues.put(TemperatureEntry.COLUMN_SENSORID, TestUtilities.TEST_SENSOR);
        novosValues.put(TemperatureEntry.COLUMN_ADDRESS, TestUtilities.TEST_SENSOR_ADDRESS);
        novosValues.put(TemperatureEntry.COLUMN_CREATED, "2015-10-21 15:47:00");
        novosValues.put(TemperatureEntry.COLUMN_VALUE, 12.34d);
        novosValues.put(TemperatureEntry.COLUMN_METRIC, 1);
        novosValues.put(TemperatureEntry.COLUMN_CALIBRATED, 0);

        mNewUri = mContext.getContentResolver().insert(
                TemperatureEntry.CONTENT_URI,   // the user dictionary content URI
                novosValues                          // the values to insert
        );

        //db.close();

        // Test the basic content provider query
        Cursor temperatureCursor = mContext.getContentResolver().query(
                TemperatureEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testInsertTemperature", temperatureCursor, novosValues);
    }
}
