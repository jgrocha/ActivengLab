package activeng.pt.activenglab;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import activeng.pt.activenglab.data.TemperatureContract;

//public class MainActivity extends AppCompatActivity {
public class MainActivity extends BlunoLibrary {

    // Constants
    // The authority for the sync adapter's content provider
    public static final String AUTHORITY = "activeng.pt.activenglab";
    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = "activeng.pt.activenglab.account";
    // The account name
    public static final String ACCOUNT = "android@activeng.pt";
    // Instance fields
    Account mAccount;
    // A content resolver for accessing the provider
    ContentResolver mResolver;
    private static final long SYNC_FREQUENCY = 60 * 60;  // 1 hour (in seconds)


    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    private BluetoothAdapter mBluetoothAdapter = null;
    private static BluetoothChatService mChatService = null;

    //private String mDeviceName;
    //private String mDeviceAddress;

    private String mDeviceName;
    private String mDeviceAddress;

    private BroadcastReceiver conn2BTService;
    private boolean registered = false;

    private static Switch bt_switch = null;
    private static boolean bt_switch_listener_enabled = true;

    private int numOfSensorsConnected = 0;
    private int numOfSensorsRegistered = 0;
    private long timeRequestTime = 0;

    private MenuItem buttonScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // MainActivity onCreate running on thl 5000 thl/thl/THL:4.4.2/KOT49H/1419316124:user/test-keys
        Log.d("Life cyle", "MainActivity onCreate running on " + Build.PRODUCT + " " + Build.FINGERPRINT);
        setContentView(R.layout.activity_main);
        // Bluno
        onCreateProcess();														//onCreate Process by BlunoLibrary
        serialBegin(115200);													//set the Uart Baudrate on BLE chip to 115200


        Toolbar toolbar = (Toolbar) findViewById(R.id.maintoolbar);
        setSupportActionBar(toolbar);

        // show the app icon
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        // Create the dummy account
        mAccount = CreateSyncAccount(this);
        // Get the content resolver for your app
        mResolver = getContentResolver();
        // Turn on automatic syncing for the default account and authority
        //mResolver.setIsSyncable(mAccount, AUTHORITY, 1);
        //mResolver.setSyncAutomatically(mAccount, AUTHORITY, true);
        //mResolver.addPeriodicSync(mAccount, AUTHORITY, new Bundle(), SYNC_FREQUENCY);

        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */

        if (Build.BRAND.equalsIgnoreCase("generic")) {
            Log.d("Life cyle", "YES, I am an emulator");
        } else {
            Log.d("Life cyle", "NO, I am NOT an emulator");
        }

        /*
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.d("ActivEng", String.format("Nome bluetooth do dispositivo Android: %s", mBluetoothAdapter.getName()));
        */

        // BroadcastReceiver is the same for LocalBroadcast or global Broadcast
        conn2BTService = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle extras = intent.getExtras();
                String action = intent.getAction();
                Log.d("ActivEng", "MainActivity → onReceive → action");
                switch (action) {
                    case Constants.MESSAGE_BT_STATE_CHANGE:
                        handleBtChange(extras);
                        break;
                    case Constants.MESSAGE_BT_FAIL:
                        break;
                    case Constants.MESSAGE_TEMPERATURE:
                        break;
                    case Constants.MESSAGE_METADATA:
                        handleBtMetadata(extras);
                        break;
                    case Constants.MESSAGE_SENSORMETADATA:
                        handleBtSensorMetadata(extras);
                        break;
                    case Constants.MESSAGE_CLOCK:
                        handleArduinoClock(extras);
                        break;
                    default:
                        Log.d("ActivEng", "MainActivity: unable to handle " + action);
                }
            }
        };

    }

    //long requestTime = System.currentTimeMillis();

    private void handleArduinoClock(Bundle extras) {
        //Log.d("ActivEng", "MainActivity: handleBtMetada");
        String metadata;
        if (extras != null) {
            metadata = extras.getString(Intent.EXTRA_TEXT);
            Log.d("ActivEng", "MainActivity: handleArduinoClock " + metadata);

            // T|1446723851
            // T|1446723851|1446723851
            String[] parts = metadata.split("\\|", -1);
            long arduino = Long.parseLong(parts[1]); // seconds
            long delta = System.currentTimeMillis() - timeRequestTime; // milliseconds
            Log.d("ActivEng", "MainActivity: handleArduinoClock: Difference: " + delta);
        }
    }

    private void handleBtSensorMetadata(Bundle extras) {
        //Log.d("ActivEng", "MainActivity: handleBtMetada");
        String metadata;
        if (extras != null) {
            metadata = extras.getString(Intent.EXTRA_TEXT);
            Log.d("ActivEng", "MainActivity: handleBtSensorMetadata " + metadata);

            //S|2|Sara Rocha|1445122574|2x 220 Ohm|T|1|1|3|-1.547600|1.001300
            String[] parts = metadata.split("\\|", -1);

            int id = Integer.parseInt(parts[1]);
            //mDeviceAddress
            String location = parts[2];
            long installdate = Long.parseLong(parts[3]);
            String sensortype = parts[4];
            String quantity = parts[5];
            int metric = Integer.parseInt(parts[6]);
            int calibrated = Integer.parseInt(parts[7]);
            int decimalplaces = Integer.parseInt(parts[8]);
            double cal_a = Double.parseDouble(parts[9]);
            double cal_b = Double.parseDouble(parts[10]);

            Uri mNewUri = TemperatureContract.SensorEntry.buildSensorIDAddressUri(id, mDeviceAddress);
            Cursor sensorCursor = getContentResolver().query(mNewUri, null, null, null, null);
            if (sensorCursor.moveToFirst()) {
                Log.d("ActivEng", "MainActivity: handleBtSensorMetadata: Sensor " + id + "@" + mDeviceAddress + " already exists in database");
                // Compare existing data with data from Arduino
                // Update fields if necessary
                // TODO
            } else {
                Log.d("ActivEng", "MainActivity: handleBtSensorMetadata: Sensor " + id + "@" + mDeviceAddress + " TODO!");
                // Insert to local database the new sensor
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date installedOn = new Date(installdate * 1000);
                ContentValues novosValues = new ContentValues();
                novosValues.put(TemperatureContract.SensorEntry.COLUMN_SENSORID, id);
                novosValues.put(TemperatureContract.SensorEntry.COLUMN_ADDRESS, mDeviceAddress);
                novosValues.put(TemperatureContract.SensorEntry.COLUMN_LOCATION, location);
                novosValues.put(TemperatureContract.SensorEntry.COLUMN_INSTALLDATE, dateFormat.format(installedOn));
                novosValues.put(TemperatureContract.SensorEntry.COLUMN_SENSORTYPE, sensortype);
                novosValues.put(TemperatureContract.SensorEntry.COLUMN_QUANTITY, quantity);
                novosValues.put(TemperatureContract.SensorEntry.COLUMN_METRIC, metric);
                novosValues.put(TemperatureContract.SensorEntry.COLUMN_CALIBRATED, calibrated);
                novosValues.put(TemperatureContract.SensorEntry.COLUMN_DECIMALPLACES, decimalplaces);
                novosValues.put(TemperatureContract.SensorEntry.COLUMN_CAL_A, cal_a);
                novosValues.put(TemperatureContract.SensorEntry.COLUMN_CAL_B, cal_b);
                novosValues.put(TemperatureContract.SensorEntry.COLUMN_READ_INTERVAL, 2000);
                novosValues.put(TemperatureContract.SensorEntry.COLUMN_RECORD_SAMPLE, 1);
                mNewUri = getContentResolver().insert(
                        TemperatureContract.SensorEntry.CONTENT_URI,   // the user dictionary content URI
                        novosValues                          // the values to insert
                );
                // Once, updated, the listeners of TemperatureContract.SensorEntry.CONTENT_URI will be notified
                // The MainActivityFragment Cursor will be reloaded "for free"
            }
            sensorCursor.close();
            numOfSensorsRegistered++;
            if (numOfSensorsRegistered == numOfSensorsConnected) {
                syncronizeTime();
            }
        }
    }

    private void handleBtMetadata(Bundle extras) {
        //Log.d("ActivEng", "MainActivity: handleBtMetada");
        String metadata;
        if (extras != null) {
            metadata = extras.getString(Intent.EXTRA_TEXT);
            Log.d("ActivEng", "MainActivity: handleBtMetadata " + metadata);
            // M|1|3|1445125085
            String[] parts = metadata.split("\\|", -1);

            int serial = Integer.parseInt(parts[1]);
            int numOfSensors = Integer.parseInt(parts[2]);
            numOfSensorsConnected = numOfSensors;

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            long epoch = Long.parseLong(parts[3]);
            Date matadataDate = new Date(epoch * 1000);

            Log.d("ActivEng", "MainActivity: handleBtMetadata: Serial: " + serial + " #ofSensor: " +
                    numOfSensors + " Date: " + dateFormat.format(matadataDate));

            // and if there is no metadata, or no sensors?
            String message = "";
            if (numOfSensors > 0) {
                message = "S1";
                for (int i = 2; i <= numOfSensors; i++) {
                    message += "\nS" + i;
                }
            }
            // message = "S1\nS2\nS3"; // The last \n is added by BluetoothChatService
            Intent intent = new Intent(Constants.MESSAGE_TO_ARDUINO).putExtra(Intent.EXTRA_TEXT, message);
            //this.sendBroadcast(intent);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

            serialSend(message + "\n");

        }
    }

    private void handleBtChange(Bundle extras) {
        Log.d("ActivEng", "MainActivity: handleBtChange");
        int btState;
        if (extras != null) {
            //Temperature temp = UtilitySingleton.getInstance().processMessage(extras.getString(Intent.EXTRA_TEXT), sensorId, address);
            btState = extras.getInt(Intent.EXTRA_TEXT);
            if (btState == Constants.STATE_CONNECTED) {
                getArduinoMetadata();
            }
        }
    }

    public void getArduinoMetadata() {
        // syncronize clocks
        String message = "M";
        Log.d("ActivEng", "Get metadata: " + message);
        //Intent intent = new Intent(Constants.MESSAGE_TO_ARDUINO).putExtra(Intent.EXTRA_TEXT, message);
        ////this.sendBroadcast(intent);
        //LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        serialSend(message + "\n");
    }

    public void syncronizeTime() {
        // syncronize clocks
        long requestTime = System.currentTimeMillis();
        timeRequestTime = requestTime;
        String message = "T|" + requestTime / 1000;
        Log.d("ActivEng", "Set time: " + message);
        //Intent intent = new Intent(Constants.MESSAGE_TO_ARDUINO).putExtra(Intent.EXTRA_TEXT, message);
        ////this.sendBroadcast(intent);
        //LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        serialSend(message + "\n");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem menuItem = menu.findItem(R.id.myswitch);
        View view = MenuItemCompat.getActionView(menuItem);
        bt_switch = (Switch) view.findViewById(R.id.switchForActionBar);

        bt_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (bt_switch_listener_enabled) {
                    if (isChecked) {
                        if (mChatService != null && (mChatService.getState() != Constants.STATE_CONNECTED && mChatService.getState() != Constants.STATE_CONNECTING)) {
                            Intent serverIntent = new Intent(getApplicationContext(), DeviceListActivity.class);
                            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                        }
                    } else {
                        if (mChatService != null) {
                            Log.d("Life cyle", "mChatService.stop()");
                            mChatService.stop();
                        }
                    }
                    Log.d("Life cyle", "Switch onCheckedChanged");
                } else {
                    Log.d("Life cyle", "Switch onCheckedChanged: nothing todo");
                }
            }
        });

        MenuItem buttonScan = menu.findItem(R.id.buttonScan);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Toast toast;
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            //Toast toast = Toast.makeText(getApplicationContext(), "Settings", Toast.LENGTH_SHORT);
            //toast.show();
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        switch (id) {
            case R.id.action_settings:
                // toast = Toast.makeText(getApplicationContext(), "Settings", Toast.LENGTH_SHORT);
                //toast.show();
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_sync_now:
                toast = Toast.makeText(getApplicationContext(), "Sync in progress...", Toast.LENGTH_SHORT);
                toast.show();
                Log.d("Life cyle", "Sync now");
                // on demand synchronization
                //ContentResolver.requestSync(
                //        newAccount,"com.sportsteamkarma.provider", Bundle.EMPTY);
                Bundle settingsBundle = new Bundle();
                settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
                settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
                mResolver.requestSync(mAccount, AUTHORITY, settingsBundle);
                return true;
            case R.id.myswitch:
                toast = Toast.makeText(getApplicationContext(), "Connect to bluetooth", Toast.LENGTH_SHORT);
                toast.show();
                Log.d("Life cyle", "Switch");
                return true;
            case R.id.buttonScan:
                toast = Toast.makeText(getApplicationContext(), "Scanning...", Toast.LENGTH_SHORT);
                toast.show();
                buttonScanOnClickProcess();                                        //Alert Dialog for selecting the BLE device
                Log.d("Life cyle", "Scan");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //11-01 17:04:44.411 29446-29446/activeng.pt.activenglab D/Life cyle: MainActivity onCreate
    //11-01 17:04:44.449 29446-29446/activeng.pt.activenglab D/ActivEng: Nome bluetooth do dispositivo Android: thl 5000
    //        11-01 17:04:44.451 29446-29446/activeng.pt.activenglab D/Life cyle: MainActivity onStart
    //11-01 17:04:44.452 29446-29446/activeng.pt.activenglab D/ActivEng: setupChat()
    //11-01 17:04:44.452 29446-29446/activeng.pt.activenglab D/Life cyle: MainActivity onResume

    @Override
    protected void onStart() {
        int state;
        super.onStart();  // Always call the superclass method first
        Log.d("Life cyle", "MainActivity onStart");

        /*
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {
            if (mChatService == null) {
                setupChat();
            } else {
                state = mChatService.getState();
                //public static final int STATE_NONE = 0;       // we're doing nothing
                //public static final int STATE_LISTEN = 1;     // now listening for incoming connections
                //public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
                //public static final int STATE_CONNECTED = 3;  // now connected to a remote device
                switch (state) {
                    case Constants.STATE_LISTEN:
                        Log.d("ActivEng", "MainActivity onStart(): BluetoothChatService: STATE_LISTEN");
                        break;
                    case Constants.STATE_CONNECTING:
                        Log.d("ActivEng", "MainActivity onStart(): BluetoothChatService: STATE_CONNECTING");
                        break;
                    case Constants.STATE_NONE:
                        Log.d("ActivEng", "MainActivity onStart(): BluetoothChatService: STATE_NONE");
                        break;
                    case Constants.STATE_CONNECTED:
                        //bt_switch.setChecked(true);
                        Log.d("ActivEng", "MainActivity onStart(): BluetoothChatService: STATE_CONNECTED");
                        break;
                }
            }
        }
        */
    }

    /*
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d("ActivEng", "handleMessage");
            //Context context = getBaseContext();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            //setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            //mConversationArrayAdapter.clear();
                            Log.d("ActivEng", "BluetoothChatService.STATE_CONNECTED");
                            getArduinoMetadata();
                            //syncronizeTime();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            //setStatus(R.string.title_connecting);
                            Log.d("ActivEng", "BluetoothChatService.STATE_CONNECTING");
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            //setStatus(R.string.title_not_connected);
                            Log.d("ActivEng", "BluetoothChatService.STATE_NONE | BluetoothChatService.STATE_LISTEN");
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    //mConversationArrayAdapter.add("Me:  " + writeMessage);
                    Log.d("ActivEng", "Constants.MESSAGE_WRITE " + "Me:  " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    //byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    //String readMessage = new String(readBuf, 0, msg.arg1);
//                    mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + msg.obj);
                    Log.d("ActivEng", "Constants.MESSAGE_READ " + mConnectedDeviceName + ":  " + msg.obj);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    mConnectedDeviceAddress = msg.getData().getString(Constants.DEVICE_ADRESS);
                    //if (null != activity) {
                    //    Toast.makeText(this, "Connected to "
                    //            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    //}
                    Log.d("ActivEng", "Constants.MESSAGE_DEVICE_NAME " + mConnectedDeviceName + " @ " + mConnectedDeviceAddress);
                    break;
                case Constants.MESSAGE_TOAST:
                    //if (null != activity) {
                    //    Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                    //            Toast.LENGTH_SHORT).show();
                    //}
                    Log.d("ActivEng", "Constants.MESSAGE_TOAST " + msg.getData().getString(Constants.TOAST));
                    break;
            }
        }
    };
    */

    private void setupChat() {
        Log.d("ActivEng", "setupChat()");
        // Initialize the BluetoothChatService to perform bluetooth connections
        // mChatService = new BluetoothChatService(this, mHandler);
        mChatService = new BluetoothChatService(this);
    }

    /*
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                if (resultCode == Activity.RESULT_CANCELED) {
                    bt_switch.setChecked(false);
                    Log.d("ActivEng", "resultCode" + resultCode);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d("ActivEng", "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    this.finish();
                }
        }
    }


    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        mDeviceName = device.getName();
        mDeviceAddress = device.getAddress();

        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }
    */

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d("Life cyle", "--> MainActivity onSaveInstanceState");
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);
        // Restore state members from saved instance
        Log.d("Life cyle", "<-- MainActivity onRestoreInstanceState");
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        Log.d("ActivEng", "MainActivity onPrepareOptionsMenu");

        if (mChatService != null && (mChatService.getState() == Constants.STATE_CONNECTED || mChatService.getState() == Constants.STATE_CONNECTING)) {
            bt_switch.setChecked(true);
        }

        //swtService.setChecked(ServiceHelper.isServiceStarted(this, MySystemService.class.getName()));
        return true;
    }

    @Override
    public void onResume() {
        int state;
        super.onResume();  // Always call the superclass method first
        invalidateOptionsMenu();
        Log.d("Life cyle", "MainActivity onResume");

        /*
        if (mChatService != null) {
            state = mChatService.getState();
            //public static final int STATE_NONE = 0;       // we're doing nothing
            //public static final int STATE_LISTEN = 1;     // now listening for incoming connections
            //public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
            //public static final int STATE_CONNECTED = 3;  // now connected to a remote device
            switch (state) {
                case Constants.STATE_LISTEN:
                    Log.d("ActivEng", "MainActivity onResume(): BluetoothChatService: STATE_LISTEN");
                    break;
                case Constants.STATE_CONNECTING:
                    Log.d("ActivEng", "MainActivity onResume(): BluetoothChatService: STATE_CONNECTING");
                    break;
                case Constants.STATE_NONE:
                    Log.d("ActivEng", "MainActivity onResume(): BluetoothChatService: STATE_NONE");
                    break;
                case Constants.STATE_CONNECTED:
                    //if (bt_switch != null) {
                    //    bt_switch_listener_enabled = false;
                    //    bt_switch.setChecked(true);
                    //    bt_switch_listener_enabled = true;
                    //    Log.d("ActivEng", "MainActivity onResume(): bt_switch.setChecked(false and true) ;");
                    //} else {
                    //    Log.d("ActivEng", "MainActivity onResume(): bt_switch == null ;");
                    //}
                    Log.d("ActivEng", "MainActivity onResume(): BluetoothChatService: STATE_CONNECTED");
                    break;
            }
        }
        */

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.registerReceiver(this.conn2BTService, new IntentFilter(Constants.MESSAGE_BT_FAIL));
        manager.registerReceiver(this.conn2BTService, new IntentFilter(Constants.MESSAGE_BT_STATE_CHANGE));
        manager.registerReceiver(this.conn2BTService, new IntentFilter(Constants.MESSAGE_METADATA));
        manager.registerReceiver(this.conn2BTService, new IntentFilter(Constants.MESSAGE_SENSORMETADATA));
        manager.registerReceiver(this.conn2BTService, new IntentFilter(Constants.MESSAGE_CLOCK));
        registered = true;

        // Bluno
        onResumeProcess();														//onResume Process by BlunoLibrary

    }

    @Override
    public void onPause() {
        if (registered) {
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
            manager.unregisterReceiver(this.conn2BTService);
        }
        super.onPause();  // Always call the superclass method first
        Log.d("Life cyle", "MainActivity onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();  // Always call the superclass method first
        Log.d("Life cyle", "MainActivity onStop");
    }

    @Override
    protected void onRestart() {
        super.onRestart();  // Always call the superclass method first
        Log.d("Life cyle", "MainActivity onRestart");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();  // Always call the superclass
        Log.d("Life cyle", "MainActivity onDestroy");
    }

    @Override
    public void onConectionStateChange(connectionStateEnum theConnectionState, String deviceName, String deviceAddress) {//Once connection state changes, this function will be called
        switch (theConnectionState) {											//Four connection state
            case isConnected:
                Log.d("Life cyle", "onConectionStateChange: isConnected");
                if (this.buttonScan != null) {
                    this.buttonScan.setTitle("isConnected");
                }
                mDeviceName = deviceName;
                mDeviceAddress = deviceAddress;
                getArduinoMetadata();
                break;
            case isConnecting:
                Log.d("Life cyle", "onConectionStateChange: isConnecting");
                if (this.buttonScan != null) {
                    this.buttonScan.setTitle("isConnecting");
                }
                break;
            case isToScan:
                Log.d("Life cyle", "onConectionStateChange: isToScan");
                if (this.buttonScan != null) {
                    this.buttonScan.setTitle("isToScan");
                }
                break;
            case isScanning:
                Log.d("Life cyle", "onConectionStateChange: isScanning");
                if (this.buttonScan != null) {
                    this.buttonScan.setTitle("isScanning");
                }
                break;
            case isDisconnecting:
                Log.d("Life cyle", "onConectionStateChange: isDisconnecting");
                if (this.buttonScan != null) {
                    this.buttonScan.setTitle("isDisconnecting");
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onSerialReceived(String theString) {							//Once connection data received, this function will be called
        // TODO Auto-generated method stub
        Log.d("ActivEng", "onSerialReceived" + theString);
    }

    /**
     * Create a new dummy account for the sync adapter
     *
     * @param context The application context
     */
    public static Account CreateSyncAccount(Context context) {
        // Create the account type and default account
        Account newAccount = new Account(
                ACCOUNT, ACCOUNT_TYPE);
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(
                        ACCOUNT_SERVICE);
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            Log.d("Life cyle", "MainActivity CreateSyncAccount: if");

        } else {
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */
            Log.d("Life cyle", "MainActivity CreateSyncAccount: else");

        }
        return newAccount;
    }
}
