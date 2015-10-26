package activeng.pt.activenglab.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import activeng.pt.activenglab.data.PollingCheck;

import java.util.Map;
import java.util.Set;

/*
    Students: These are functions and some test data to make it easier to test your database and
    Content Provider.  Note that you'll want your TemperatureContract class to exactly match the one
    in our solution to use these as-given.
 */
public class TestUtilities extends AndroidTestCase {
    static final Integer TEST_SENSOR = 1;
    static final String TEST_SENSOR_ADDRESS = "30:14:12:18:06:34";
    static final String TEST_DATE_STRING = "2015-10-24 16:49:20";
    static final long TEST_DATE_EPOCH = 1445705360; // 2015-10-24 16:49:20
    // select datetime(1445705360, 'unixepoch');

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + valueCursor.getString(idx) +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    static ContentValues createSensorValues() {
        ContentValues testValues = new ContentValues();
        testValues.put(TemperatureContract.SensorEntry.COLUMN_SENSORID, 1);
        testValues.put(TemperatureContract.SensorEntry.COLUMN_ADDRESS, "30:14:12:18:06:34");
        testValues.put(TemperatureContract.SensorEntry.COLUMN_LOCATION, "Seoul");
        testValues.put(TemperatureContract.SensorEntry.COLUMN_INSTALLDATE, "datetime(" + TEST_DATE_EPOCH + ", 'unixepoch')" );
        testValues.put(TemperatureContract.SensorEntry.COLUMN_SENSORTYPE, "PT100");
        testValues.put(TemperatureContract.SensorEntry.COLUMN_METRIC, 1);
        testValues.put(TemperatureContract.SensorEntry.COLUMN_CALIBRATED, 0);
        testValues.put(TemperatureContract.SensorEntry.COLUMN_QUANTITY, "T");
        testValues.put(TemperatureContract.SensorEntry.COLUMN_DECIMALPLACES, 3);
        testValues.put(TemperatureContract.SensorEntry.COLUMN_CAL_A, 0);
        testValues.put(TemperatureContract.SensorEntry.COLUMN_CAL_B, 1);
        testValues.put(TemperatureContract.SensorEntry.COLUMN_READ_INTERVAL, 3000);
        testValues.put(TemperatureContract.SensorEntry.COLUMN_RECORD_SAMPLE, 1);
        return testValues;
    }

    static long insertSensorValues(Context context) {
        // insert our test records into the database
        TemperatureDbHelper dbHelper = new TemperatureDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = TestUtilities.createSensorValues();

        long sensorRowId;
        sensorRowId = db.insert(TemperatureContract.SensorEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue("Error: Failure to insert sensor values", sensorRowId != -1);

        return sensorRowId;
    }

    static ContentValues createTemperatureValues(int sensorid, String address) {
        ContentValues temperatureValues = new ContentValues();
        temperatureValues.put(TemperatureContract.TemperatureEntry.COLUMN_SENSORID, sensorid);
        temperatureValues.put(TemperatureContract.TemperatureEntry.COLUMN_ADDRESS, address);
        temperatureValues.put(TemperatureContract.TemperatureEntry.COLUMN_CREATED, "datetime(" + TEST_DATE_EPOCH + ", 'unixepoch')" );
        temperatureValues.put(TemperatureContract.TemperatureEntry.COLUMN_VALUE, 40.123);
        temperatureValues.put(TemperatureContract.TemperatureEntry.COLUMN_METRIC, 1);
        temperatureValues.put(TemperatureContract.TemperatureEntry.COLUMN_CALIBRATED, 0);
        return temperatureValues;
    }

    static ContentValues createCalibrationValues(int sensorid, String address) {
        ContentValues calibrationValues = new ContentValues();
        calibrationValues.put(TemperatureContract.CalibrationEntry.COLUMN_SENSORID, sensorid);
        calibrationValues.put(TemperatureContract.CalibrationEntry.COLUMN_ADDRESS, address);
        calibrationValues.put(TemperatureContract.CalibrationEntry.COLUMN_CREATED, "datetime(" + TEST_DATE_EPOCH + ", 'unixepoch')" );
        calibrationValues.put(TemperatureContract.CalibrationEntry.COLUMN_CAL_A_OLD, 0);
        calibrationValues.put(TemperatureContract.CalibrationEntry.COLUMN_CAL_B_OLD, 1);
        calibrationValues.put(TemperatureContract.CalibrationEntry.COLUMN_CAL_A_NEW, 0.12345);
        calibrationValues.put(TemperatureContract.CalibrationEntry.COLUMN_CAL_B_NEW, 0.999991);
        calibrationValues.put(TemperatureContract.CalibrationEntry.COLUMN_REF_VALUE_HIGH, 250);
        calibrationValues.put(TemperatureContract.CalibrationEntry.COLUMN_REF_VALUE_LOW, 10.1);
        calibrationValues.put(TemperatureContract.CalibrationEntry.COLUMN_READ_VALUE_HIGH, 250.123);
        calibrationValues.put(TemperatureContract.CalibrationEntry.COLUMN_READ_VALUE_LOW, 10.234);
        return calibrationValues;
    }

    /*
        The functions we provide inside of TestProvider use this utility class to test
        the ContentObserver callbacks using the PollingCheck class that we grabbed from the Android
        CTS tests.

        Note that this only tests that the onChange function is called; it does not test that the
        correct Uri is returned.
     */
    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}
