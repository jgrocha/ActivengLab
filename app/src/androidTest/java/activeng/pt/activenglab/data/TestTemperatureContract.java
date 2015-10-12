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

import android.test.AndroidTestCase;

public class TestTemperatureContract extends AndroidTestCase {

    // intentionally includes a slash to make sure Uri is getting quoted correctly
    private static final Integer TEST_SENSOR_ID = 1;
    private static final String TEST_DATE = "2015-10-01 14:26:30";

//    public void testBuildTemperatureSensor() {
//
//        Uri locationUri = TemperatureContract.TemperatureEntry.buildTemperatureSensor(TEST_SENSOR_ID);
//
//        assertNotNull("Error: Null Uri returned.  You must fill-in buildTemperatureSensor in " +
//                        "TemperatureContract.", locationUri);
//
//        assertEquals("Error: Sensor ID not properly appended to the end of the Uri",
//                Integer.toString(TEST_SENSOR_ID), locationUri.getLastPathSegment());
//
//        assertEquals("Error: TemperatureSensor Uri doesn't match our expected result",
//                locationUri.toString(),
//                "content://com.example.android.bluetoothchat/temperature/1");
//
//    }

//    public void testBuildTemperatureSensorWithDate() {
//
//        Uri locationUri = TemperatureContract.TemperatureEntry.buildTemperatureSensorWithDate(TEST_SENSOR_ID, TEST_DATE);
//
//        assertNotNull("Error: Null Uri returned.  You must fill-in buildTemperatureSensor in " +
//                "TemperatureContract.", locationUri);
//
//        assertEquals("Error: Sensor ID not properly appended to the end of the Uri",
//                Integer.toString(TEST_SENSOR_ID), TemperatureContract.TemperatureEntry.getSensorFromUri(locationUri));
//
//        assertEquals("Error: TemperatureSensorWithDate Uri path doesn't match our expected result",
//                locationUri.getPath(),
//                "/temperature/1/" + TEST_DATE);
//
//        assertEquals("Error: TemperatureSensorWithDate dates doesn't match our expected result",
//                locationUri.getLastPathSegment(),
//                TEST_DATE );
//    }
}
