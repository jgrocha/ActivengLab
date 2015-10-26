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
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import java.util.HashSet;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();
    
    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(TemperatureDbHelper.DATABASE_NAME);
    }
    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    public void setUp() {
        deleteTheDatabase();
    }
    
    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(TemperatureContract.SensorEntry.TABLE_NAME);
        tableNameHashSet.add(TemperatureContract.TemperatureEntry.TABLE_NAME);
        tableNameHashSet.add(TemperatureContract.CalibrationEntry.TABLE_NAME);

        mContext.deleteDatabase(TemperatureDbHelper.DATABASE_NAME);

        SQLiteDatabase db = new TemperatureDbHelper(this.mContext).getWritableDatabase();

        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        assertTrue("Error: Your database was created without all tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + TemperatureContract.SensorEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> sensorColumnHashSet = new HashSet<String>();
        sensorColumnHashSet.add(TemperatureContract.SensorEntry._ID);
        sensorColumnHashSet.add(TemperatureContract.SensorEntry.COLUMN_SENSORID);
        sensorColumnHashSet.add(TemperatureContract.SensorEntry.COLUMN_ADDRESS);
        sensorColumnHashSet.add(TemperatureContract.SensorEntry.COLUMN_LOCATION);
        sensorColumnHashSet.add(TemperatureContract.SensorEntry.COLUMN_INSTALLDATE);
        sensorColumnHashSet.add(TemperatureContract.SensorEntry.COLUMN_SENSORTYPE);
        sensorColumnHashSet.add(TemperatureContract.SensorEntry.COLUMN_METRIC);
        sensorColumnHashSet.add(TemperatureContract.SensorEntry.COLUMN_CALIBRATED);
        sensorColumnHashSet.add(TemperatureContract.SensorEntry.COLUMN_QUANTITY);
        sensorColumnHashSet.add(TemperatureContract.SensorEntry.COLUMN_DECIMALPLACES);
        sensorColumnHashSet.add(TemperatureContract.SensorEntry.COLUMN_CAL_A);
        sensorColumnHashSet.add(TemperatureContract.SensorEntry.COLUMN_CAL_B);
        sensorColumnHashSet.add(TemperatureContract.SensorEntry.COLUMN_READ_INTERVAL);
        sensorColumnHashSet.add(TemperatureContract.SensorEntry.COLUMN_RECORD_SAMPLE);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            sensorColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required sensor entry columns",
                sensorColumnHashSet.isEmpty());

        db.close();
    }

    public void testSensorTable() {

        insertSensor();
        Log.d("TestProvider", "hello... insertSensor()... done!");
    }

    public void testTemperatureTable() {
        long sensorRowId = insertSensor();
        assertFalse("Error: sensor Not Inserted Correctly", sensorRowId == -1L);
        TemperatureDbHelper dbHelper = new TemperatureDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues temperatureValues = TestUtilities.createTemperatureValues(TestUtilities.TEST_SENSOR, TestUtilities.TEST_SENSOR_ADDRESS);
        long temperatureRowId = db.insert(TemperatureContract.TemperatureEntry.TABLE_NAME, null, temperatureValues);
        assertTrue(temperatureRowId != -1);
        Cursor temperatureCursor = db.query(
                TemperatureContract.TemperatureEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );
        assertTrue("Error: No Records returned from location query", temperatureCursor.moveToFirst());
        TestUtilities.validateCurrentRecord("testInsertReadDb weatherEntry failed to validate",
                temperatureCursor, temperatureValues);
        assertFalse("Error: More than one record returned from weather query",
                temperatureCursor.moveToNext());
        temperatureCursor.close();
        dbHelper.close();
    }

    public void testCalibrationTable() {
        long sensorRowId = insertSensor();
        assertFalse("Error: sensor Not Inserted Correctly", sensorRowId == -1L);
        TemperatureDbHelper dbHelper = new TemperatureDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues calibrationValues = TestUtilities.createCalibrationValues(TestUtilities.TEST_SENSOR, TestUtilities.TEST_SENSOR_ADDRESS);
        long calibrationRowId = db.insert(TemperatureContract.CalibrationEntry.TABLE_NAME, null, calibrationValues);
        assertTrue(calibrationRowId != -1);
        Cursor calibrationCursor = db.query(
                TemperatureContract.CalibrationEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );
        assertTrue("Error: No Records returned from location query", calibrationCursor.moveToFirst());
        TestUtilities.validateCurrentRecord("testInsertReadDb weatherEntry failed to validate",
                calibrationCursor, calibrationValues);
        assertFalse("Error: More than one record returned from weather query",
                calibrationCursor.moveToNext());
        calibrationCursor.close();
        dbHelper.close();
    }

    public long insertSensor() {
        TemperatureDbHelper dbHelper = new TemperatureDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtilities.createSensorValues();

        // Third Step: Insert ContentValues into database and get a row ID back
        long sensorRowId;
        sensorRowId = db.insert(TemperatureContract.SensorEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue("Error: insert did not return a new sensor row id", sensorRowId != -1);

        Cursor cursor = db.query(
                TemperatureContract.SensorEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // Move the cursor to a valid database row and check to see if we got any records back
        // from the query
        assertTrue( "Error: No Records returned from sensor query", cursor.moveToFirst() );

        // Fifth Step: Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        TestUtilities.validateCurrentRecord("Error: Location Query Validation Failed",
                cursor, testValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse( "Error: More than one record returned from location query",
                cursor.moveToNext() );

        // Sixth Step: Close Cursor and Database
        cursor.close();
        db.close();
        return sensorRowId;
    }


}
