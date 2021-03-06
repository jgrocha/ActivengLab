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

/**
 * Defines several constants used between {@link BluetoothChatService} and the UI.
 */
public interface Constants {

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    //public static final int MESSAGE_METADATA = 1025;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String DEVICE_ADRESS= "device_address";
    public static final String TOAST = "toast";

    public static final String MESSAGE_TEMPERATURE = "activeng.pt.activenglab.temperature";
    public static final String MESSAGE_METADATA       = "activeng.pt.activenglab.metadata";
    public static final String MESSAGE_SENSORMETADATA = "activeng.pt.activenglab.sensor.metadata";
    public static final String MESSAGE_CLOCK = "activeng.pt.activenglab.sensor.clock";
    public static final String MESSAGE_TO_ARDUINO = "activeng.pt.activenglab.arduino";
    public static final String MESSAGE_BT_STATE_CHANGE = "activeng.pt.activenglab.bluetooth";
    public static final String MESSAGE_BT_NAME = "activeng.pt.activenglab.bluetooth.name";
    public static final String MESSAGE_BT_FAIL = "activeng.pt.activenglab.bluetooth.fail";

    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    // if STATE_CONNECTED
    public static final int STATE_PROTOCOL_NONE = 0;            // we're doing nothing
    public static final int STATE_PROTOCOL_METADATA = 1;        // We send a Metadata request
    public static final int STATE_PROTOCOL_TIME = 2;            // We send a Time update request to adjust Arduino clock
    public static final int STATE_PROTOCOL_RECORDING = 3;       // The clock is adjusted and we are recording all temperatures coming
    public static final int STATE_PROTOCOL_NOTRECORDING = 4;    // The clock is adjusted but we are not recording temperatures

    public static final String EXTRA_MSG_TEMP = "activeng.extra.msg.temp";
    public static final String EXTRA_MSG_TEMP_STR = "activeng.extra.msg.temp.str";
    public static final String EXTRA_MSG_TEMP_SENSOR = "activeng.extra.msg.temp.sensor";
    public static final String EXTRA_MSG_TEMP_MILLIS = "activeng.extra.msg.temp.millis";

}
