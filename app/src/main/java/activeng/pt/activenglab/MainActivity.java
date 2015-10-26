package activeng.pt.activenglab;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothChatService mChatService = null;
    private String mConnectedDeviceName = null;
    private String mConnectedDeviceAddress = null;

    private Switch switcha = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Life cyle", "MainActivity onCreate");
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // show the app icon
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

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

        /*
                Comentar para desativar Bluetooth
         */
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.d("ActivEng", String.format("Nome do dispositivo: %s", mBluetoothAdapter.getName()));


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem menuItem = menu.findItem(R.id.myswitch);
        View view = MenuItemCompat.getActionView(menuItem);
        switcha = (Switch) view.findViewById(R.id.switchForActionBar);
        switcha.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Intent serverIntent = new Intent(getApplicationContext(), DeviceListActivity.class);
                    startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                } else {
                    if (mChatService != null) {
                        mChatService.stop();
                    }
                }
                Log.d("Life cyle", "New Switch");
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            //Toast toast = Toast.makeText(getApplicationContext(), "Settings", Toast.LENGTH_SHORT);
            //toast.show();
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        switch (id) {
            case R.id.action_settings:
                //Toast toast = Toast.makeText(getApplicationContext(), "Settings", Toast.LENGTH_SHORT);
                //toast.show();
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            case R.id.myswitch:
                Toast toast = Toast.makeText(getApplicationContext(), "Connect to bluetooth", Toast.LENGTH_SHORT);
                toast.show();
                Log.d("Life cyle", "Switch");
                return true;
            /*
            case R.id.secure_connect_scan:
                //Toast toast = Toast.makeText(getApplicationContext(), "Connect to bluetooth", Toast.LENGTH_SHORT);
                //toast.show();

                Intent serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);

                return true;
            case R.id.disconnect:
                //Toast toast = Toast.makeText(getApplicationContext(), "Connect to bluetooth", Toast.LENGTH_SHORT);
                //toast.show();
                if (mChatService != null) {
                    mChatService.stop();
                }
                return true;
            */
        }
        return super.onOptionsItemSelected(item);
    }

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
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        Log.d("Life cyle", "MainActivity onResume");
    }

    @Override
    public void onPause() {
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
    protected void onStart() {
        super.onStart();  // Always call the superclass method first
        Log.d("Life cyle", "MainActivity onStart");

        /*
                Comentar para desativar Bluetooth
         */
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            setupChat();
        }

    }

    public void syncronizeTime() {
        // syncronize clocks
        String message = "T|" + System.currentTimeMillis()/1000;
        Log.d("ActivEng", "Set time: " + message);
        Intent intent = new Intent(Constants.MESSAGE_TO_ARDUINO).putExtra(Intent.EXTRA_TEXT, message);
        this.sendBroadcast(intent);
    }

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
                            syncronizeTime();
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

    private void setupChat() {
        Log.d("ActivEng", "setupChat()");

        //// Initialize the array adapter for the conversation thread
        //mConversationArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.message);
        //
        //mConversationView.setAdapter(mConversationArrayAdapter);
        //
        //// Initialize the compose field with a listener for the return key
        //mOutEditText.setOnEditorActionListener(mWriteListener);
        //
        //// Initialize the send button with a listener that for click events
        //mSendButton.setOnClickListener(new View.OnClickListener() {
        //    public void onClick(View v) {
        //        // Send a message using content of the edit text widget
        //        View view = getView();
        //        if (null != view) {
        //            TextView textView = (TextView) view.findViewById(R.id.edit_text_out);
        //            String message = textView.getText().toString();
        //            sendMessage(message);
        //        }
        //    }
        //});

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);
        //mChatService = new BluetoothChatService(this);

        // Initialize the buffer for outgoing messages
        //mOutStringBuffer = new StringBuffer("");
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                if (resultCode == Activity.RESULT_CANCELED) {
                    // TODO
                    // voltar a por o switch button a off
                    switcha.setChecked(false);
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

    /**
     * Establish connection with other divice
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

}
