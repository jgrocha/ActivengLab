package activeng.pt.activenglab.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines table and column names for the temperature database.
 */
public class TemperatureContract {

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "activeng.pt.activenglab";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com.example.android.sunshine.app/temperature/ is a valid path for
    // looking at temperature data. content://com.example.android.sunshine.app/givemeroot/ will fail,
    // as the ContentProvider hasn't been given any information on what to do with "givemeroot".
    // At least, let's hope not.  Don't be that dev, reader.  Don't be that dev.

    public static final String PATH_SENSOR = "sensor";
    public static final String PATH_TEMPERATURE = "temperature";
    public static final String PATH_CALIBRATION = "calibration";

    /* Inner class that defines the table contents of the sensor table */
    public static final class SensorEntry implements BaseColumns {
        // Table name and columns
        public static final String TABLE_NAME = "sensor";
        public static final String COLUMN_SENSORID = "sensorid";
        public static final String COLUMN_ADDRESS = "address";
        public static final String COLUMN_LOCATION = "location";
        public static final String COLUMN_INSTALLDATE = "installdate";
        public static final String COLUMN_SENSORTYPE = "sensortype";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_METRIC = "metric";
        public static final String COLUMN_CALIBRATED = "calibrated";
        public static final String COLUMN_DECIMALPLACES = "decimalplaces";
        public static final String COLUMN_CAL_A = "cal_a";
        public static final String COLUMN_CAL_B = "cal_b";
        // read_interval 1, 2, 5, 10, 20, 30, 60
        public static final String COLUMN_READ_INTERVAL = "read_interval";
        // each cycle, each 2 cycles, each 5 cycles, each 10 cycles, 20, 30, 60
        public static final String COLUMN_RECORD_SAMPLE = "record_sample";

        // Contract URIs
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SENSOR).build();

        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SENSOR;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SENSOR;

        public static Uri buildSensorUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildSensorIDAddressUri(long id, String address) {
            //return ContentUris.withAppendedId(CONTENT_URI, id);
            return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).appendPath(address).build();
        }

        public static String getSensorIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
        public static String getSensorAddressFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

    }

    /* Inner class that defines the table contents of the temperature table */
    public static final class TemperatureEntry implements BaseColumns {
        // Table name and columns
        public static final String TABLE_NAME = "temperature";
        // Column with the foreign key into the sensor table.
        public static final String COLUMN_SENSORID = "sensorid";
        public static final String COLUMN_ADDRESS = "address";
        public static final String COLUMN_CREATED = "created";
        public static final String COLUMN_VALUE = "value";
        public static final String COLUMN_METRIC = "metric";
        public static final String COLUMN_CALIBRATED = "calibrated";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TEMPERATURE).build();

        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TEMPERATURE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TEMPERATURE;

        // sacar temperaturas
        public static Uri buildTemperatureUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }

    public static final class CalibrationEntry implements BaseColumns {
        // Table name and columns
        public static final String TABLE_NAME = "calibration";
        // Column with the foreign key into the sensor table.
        public static final String COLUMN_SENSORID = "sensorid";
        public static final String COLUMN_ADDRESS = "address";
        public static final String COLUMN_CREATED = "created";
        public static final String COLUMN_CAL_A_OLD = "cal_a_old";
        public static final String COLUMN_CAL_B_OLD = "cal_b_old";
        public static final String COLUMN_CAL_A_NEW = "cal_a_new";
        public static final String COLUMN_CAL_B_NEW = "cal_b_new";
        public static final String COLUMN_REF_VALUE_HIGH = "ref_value_high";
        public static final String COLUMN_REF_VALUE_LOW = "ref_value_low";
        public static final String COLUMN_READ_VALUE_HIGH = "read_value_high";
        public static final String COLUMN_READ_VALUE_LOW = "read_value_low";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CALIBRATION).build();

        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CALIBRATION;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CALIBRATION;

        public static Uri buildCalibrationUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }

}
