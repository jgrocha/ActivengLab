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

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import activeng.pt.activenglab.data.TemperatureContract.SensorEntry;
import activeng.pt.activenglab.data.TemperatureContract.TemperatureEntry;
import activeng.pt.activenglab.data.TemperatureContract.CalibrationEntry;

public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

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
                TemperatureContract.CalibrationEntry.CONTENT_URI,
                null,
                null
        );
        cursor = mContext.getContentResolver().query(
                TemperatureContract.CalibrationEntry.CONTENT_URI,
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
        Log.d(LOG_TAG, ">>> remove tudo o que já existe...! ");
        deleteAllRecords();
    }

    /*
        This test checks to make sure that the content provider is registered correctly.
        Students: Uncomment this test to make sure you've correctly registered the WeatherProvider.
     */

    /*
    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // WeatherProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                TemperatureProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: WeatherProvider registered with authority: " + providerInfo.authority +
                    " instead of authority: " + TemperatureContract.CONTENT_AUTHORITY,
                    providerInfo.authority, TemperatureContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: WeatherProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }
    */

    /*
            This test doesn't touch the database.  It verifies that the ContentProvider returns
            the correct type for each type of URI that it can handle.
            Students: Uncomment this test to verify that your implementation of GetType is
            functioning correctly.
         */

    public void testSensorGetType1() {
        // content://com.example.android.sunshine.app/location/
        ContentResolver resolver = getContext().getContentResolver();
        String type = resolver.getType(SensorEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/location
        Log.d(LOG_TAG, " getType(SensorEntry.CONTENT_URI) = " + type);
        Log.d(LOG_TAG, "     SensorEntry.CONTENT_DIR_TYPE = " + SensorEntry.CONTENT_DIR_TYPE);
        assertEquals("Error: the SensorEntry CONTENT_URI should return SensorEntry.CONTENT_DIR_TYPE",
                SensorEntry.CONTENT_DIR_TYPE, type);
    }

    public void testSensorGetType2() {
        // content://com.example.android.sunshine.app/location/
        ContentResolver resolver = getContext().getContentResolver();
        String type = resolver.getType(TemperatureContract.SensorEntry.buildSensorUri(2));
        // vnd.android.cursor.dir/com.example.android.sunshine.app/location
        Log.d(LOG_TAG, " getType(SensorEntry.CONTENT_URI) = " + type);
        Log.d(LOG_TAG, "     SensorEntry.CONTENT_DIR_TYPE = " + SensorEntry.CONTENT_DIR_TYPE);
        assertEquals("Error: the SensorEntry CONTENT_URI should return SensorEntry.CONTENT_DIR_TYPE",
                SensorEntry.CONTENT_DIR_TYPE, type);
    }

    public void testTemperatureGetType() {

        // content://com.example.android.sunshine.app/weather/
        String type = mContext.getContentResolver().getType(TemperatureEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals("Error: the TemperatureEntry CONTENT_URI should return TemperatureEntry.CONTENT_DIR_TYPE",
                TemperatureEntry.CONTENT_DIR_TYPE, type);

    }


    /*
        This test uses the database directly to insert and then uses the ContentProvider to
        read out the data.  Uncomment this test to see if the basic weather query functionality
        given in the ContentProvider is working correctly.
     */

    public void testBasicSensorQuery() {
        // insert our test records into the database
        TemperatureDbHelper dbHelper = new TemperatureDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtilities.createSensorValues();
        long sensorId = TestUtilities.insertSensorValues(mContext);

        db.close();

        // Test the basic content provider query
        Cursor sensorCursor = mContext.getContentResolver().query(
                SensorEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicWeatherQuery", sensorCursor, testValues);

        // Test the basic content provider query
        //Cursor sensorCursor2 = mContext.getContentResolver().query(
        //        SensorEntry.CONTENT_URI,
        //        null,
        //        null,
        //        null,
        //        null
        //);
        //
        //ContentValues novosValues = new ContentValues();
        //novosValues.put(TemperatureContract.SensorEntry.COLUMN_LOCATION, "Seoul");
        //novosValues.put(TemperatureContract.SensorEntry.COLUMN_INSTALLDATE, "2015-10-09 15:47:00");
        //novosValues.put(TemperatureContract.SensorEntry.COLUMN_SENSORTYPE, "PT100");
        //novosValues.put(TemperatureContract.SensorEntry.COLUMN_METRIC, 1);
        //novosValues.put(TemperatureContract.SensorEntry.COLUMN_CALIBRATED, 0);
        //novosValues.put(TemperatureContract.SensorEntry.COLUMN_CAL_A, 0);
        //novosValues.put(TemperatureContract.SensorEntry.COLUMN_CAL_B, 1);
        //
        //TestUtilities.validateCursor("testBasicWeatherQuery", sensorCursor2, novosValues);
    }

}
