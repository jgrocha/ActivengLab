![ActivengLab](resources/ActivengLab_800x.png)

Measuring temperatures using high precision RTD.

## Glossary

ActivengLab App: Mobile application

Controller: Arduino connected to sensors or relay

Sensor: PT 100, etc

Relay: 

## Android development

### Icon

New → Image Asset → Asset Type: Launcher Icons

```
    protected void onCreate(Bundle savedInstanceState) {
        (...)
        // show the app icon
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
        (...)
```

### Testing

Testing for Android can be classified into:

 * Local unit tests - tests which can run on the JVM

 * Instrumented unit tests - tests which require the Android system

#### Local Unit tests

`ActivengLab/app/src/test/java/activeng/pt/activenglab/ExampleUnitTest.java`

To run these tests:

* Build → Select Build Variant... Test Artifact: Unit Tests

#### Instrumented unit tests (tests on Android)

`ActivengLab/app/src/androidTest/java/activeng/pt/activenglab/ApplicationTest.java`

To run these tests:

* Build → Select Build Variant... Test Artifact: Unit Tests

Class to load all tests:

 * `ActivengLab/app/src/androidTest/java/activeng/pt/activenglab/FullTestSuite.java`
 
Testing Activities: See sample `ActivityInstrumentation`. Nice example.

### Preferences (Done!)

[Android app preferences](http://developer.android.com/guide/topics/ui/settings.html)

* android:key="location_text"
* android:key="temperature_unit"
* android:key="sync_frequency"
* android:key="remote_url"
        
Check preferences on emulator:
 
Android Device Monitor → File Explorer → data/data/activeng.pt.activenglab/shared_prefs/activeng.pt.activenglab_preferences.xml

```
<?xml version='1.0' encoding='utf-8' standalone='yes' ?>
<map>
    <string name="sync_frequency">60</string>
    <string name="temperature_unit">C</string>
    <string name="remote_url">http://lab.activeng.pt</string>
    <string name="location_text">Carnaxide, Portugal</string>
</map>
```
### Data: Contract + DbHelper + Provider

New package (src/mail/java/activeng.pt.activenglab): activeng.pt.activenglab.data

 * TemperatureProvider.java
 * TemperatureDbHelper.java
 * TemperatureContract.java

New package (src/androidTest/java/activeng.pt.activenglab): activeng.pt.activenglab.data

* PollingCheck.java
* TestDb.java
* TestProvider.java
* TestTemperatureContract.java
* TestUtilities.java

#### Inspect Sqlite

Emulator:

Android Device Monitor → File Explorer → data/data/activeng.pt.activenglab/databases/temperature.db

Real device (working):

From device to local:

```
#!/bin/bash

# cd ~/Android/Sdk/platform-tools
# ~/Android/Sdk/platform-tools/adb devices
# List of devices attached
# KBLJZTKVAICMEQ6H      device
# ./adb -d shell "run-as activeng.pt.activenglab ls /data/data/activeng.pt.activenglab/databases/"
# temperature.db
# temperature.db-journal
# Copy the database file from your application folder to your sd card.
~/Android/Sdk/platform-tools/adb -d shell "run-as activeng.pt.activenglab cat /data/data/activeng.pt.activenglab/databases/temperature.db > /sdcard/temperature.db"
~/Android/Sdk/platform-tools/adb pull /sdcard/temperature.db
sqlitebrowser temperature.db
```

Local to device:

```
#!/bin/bash

~/Android/Sdk/platform-tools/adb push temperature.db /sdcard/
~/Android/Sdk/platform-tools/adb shell
su
cp /sdcard/temperature.db /data/data/activeng.pt.activenglab/databases/temperature.db
```

### Activities and Layout

Life cycle: DetailActivity must have `android:launchMode="singleTop"` on `AndroidManifest.xml`. See [launchMode=singleTop](http://stackoverflow.com/questions/12276027/how-can-i-return-to-a-parent-activity-correctly).

```
<activity
            android:name=".DetailActivity"
            android:label="@string/title_activity_detail"
            android:parentActivityName=".MainActivity"
            android:theme="@style/AppTheme.NoActionBar"
            android:launchMode="singleTop" >
```            

#### Hierarchy

```
MainActivity
    Action Bar
        Connect
        Settings
    Fragment
        ListView
            Custom View (from ItemSensorCursorAdapter) → Launch DetailActivity(Uri)
                + Current read
                + Set Point | Clear set point
                    Log data/hora
                + Program (sequence of set points)
                    90º 35 minutos (critério de estabilidade) tempo x intervalo
                    130º 35 minutos
                    121º 35 minutos                    
```

```
DetailActivity
    Action Bar
        Record On | off
            EditText (Read comment from user (optional))
        Properties
            Sensor name
            Sensor type
            Record Interval        
        CalibrationActivity
            GridLayout
                (TextView|EditText)+
                Calculate button, Save button
                    Log data/hora
    Fragment
        ListView
            Custom View (from SensorCursorAdapter - just one Sensor)
```
### Charts and Graphs

[GraphView](https://github.com/jjoe64/GraphView)

[x- axis time resolution](https://github.com/jjoe64/GraphView/issues/368)

build.gradle (Module:app)

```
dependencies {
    (...)
    compile 'com.jjoe64:graphview:4.0.1'
    (...)
}
```

`fragment_detail.xml`

```
    <ListView
        android:id="@+id/listview_sensor"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"/>

    <com.jjoe64.graphview.GraphView
        android:layout_width="match_parent"
        android:layout_height="100dip"
        android:id="@+id/graph" />
```        

### Bluetooth (Novo BLE)

http://developer.android.com/guide/topics/connectivity/bluetooth-le.html

Address: D0:39:72:C1:36:54 Name: BlunoV1.8



### Bluetooth (to be deprecated...)

Initial support:

* BluetoothChatService + Broadcast + Listener in every Activity

More on [broadcast](http://stackoverflow.com/questions/17082393/handlers-and-multiple-activities).

### Updates e refresh do CursorAdaptor

// TODO
http://www.androprogrammer.com/2013/10/database-demo-database-operations-with_13.html

### Communication between BluetoothChatService and Activities and Fragments

Possibilities:

* Handlers
* Broadcast sender and receivers (using Intents).
* [Local Broadcast](http://stackoverflow.com/questions/25246185/what-is-more-efficient-broadcast-receiver-or-handler/25246377#25246377) sender and receivers (using Intents)
** Local Broadcast has a [sendBroadcastSync](http://developer.android.com/reference/android/support/v4/content/LocalBroadcastManager.html)
* [EventBus](https://github.com/greenrobot/EventBus) seems a good alternative

Communication used

* Local Broadcast

Further reading:

* http://codetheory.in/android-broadcast-receivers/

### Convertions: String versus Double

Arduino: doubles are always returned with '.' as decimal separator.

Incoming doubles are converted with: 

```
t = Double.parseDouble(parts[2]);
cal_a = Double.parseDouble(calIntent.getStringExtra("cal_a"));
```

To format doubles as string, we can not use the local locale (to avoid ',' as separator).

We use:

```
    private final NumberFormat f = NumberFormat.getInstance();
    
        if (f instanceof DecimalFormat) {
            //((DecimalFormat)f).setDecimalSeparatorAlwaysShown(true);
            f.setMaximumFractionDigits(2);
            f.setMinimumFractionDigits(2);
            DecimalFormatSymbols custom = new DecimalFormatSymbols();
            custom.setDecimalSeparator('.');
            ((DecimalFormat)f).setDecimalFormatSymbols(custom);
        }
```

and then: 

```
//etCurrentRead.setText( String.format( "%.3f", t ));
etCurrentRead.setText( f.format(t) );
```

Instead of:

```
String.format( "%.3f", t )
or
NumberFormat nf = NumberFormat.getInstance();
nf.setMinimumFractionDigits(2);
nf.setMaximumFractionDigits(2);
String output = nf.format(val);
```

### PID

[Arduino PID library](http://playground.arduino.cc/Code/PIDLibrary)

### Remote synchronization

Status: 
Faz a query para os sensores;
    Para cada sensor, vê se existe no servidor e até que data tem dados.
        Se não existir no servidor, cria o sensor.
Falta: Com os sensores criados e com a última data conhecida, falta mandar os dados de cada sensor para o servidor.

```
        Request URL: http://localhost:3000/direct
        Request Method:POST
        Content-type:application/json

        {
        "action":"AppGeoExt3.PgPersonnel",
        "method":"create",
        "data":[{"name":"Adelaide",
            "email":"adele@gmail.com",
            "phone":"234360101",
            "id":"My1stGeoExt3App.model.Personnel-1"}],
        "type":"rpc",
        "tid":3
        }
```

```
        curl -v -H "Content-type: application/json" -d '{"data":[{"id":"5126","email":"anivilar@gmail.com","phone":"910333131","name":"Ana Isabel"}],"action":"AppGeoExt3.PgPersonnel","method":"create","tid":4,"type":"rpc"}' http://192.168.1.101:3000/direct
        curl -v -H "Content-type: application/json" -d @status.json http://localhost:3000/direct
```
       
#### Hide dummy account from Account Settings
 
[Hide dummy account](http://stackoverflow.com/questions/19562432/hide-dummy-account-for-sync-adapter-from-settings).
[sync-adapter-without-account](http://stackoverflow.com/questions/5146272/sync-adapter-without-account)

## Date and time

Unix time, or POSIX time, is a system for describing points in time, defined as the number of 
seconds elapsed since midnight proleptic Coordinated Universal Time (UTC) of January 1, 1970, 
not counting leap seconds.

In Linux:
```
$ date +%s
1446723851
```

JAVA

```
long timeMillis = System.currentTimeMillis();
long timeSeconds = TimeUnit.MILLISECONDS.toSeconds(timeMillis);
```

Arduino

```
now() // Returns the current time as seconds since Jan 1 1970
```

SQLite

[Date And Time Functions](https://www.sqlite.org/lang_datefunc.html)

### Store datetime in SQLite

JAVA System.currentTimeMillis() to Date

```
SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
// Android time
Date addedOn = new Date(System.currentTimeMillis());
// Arduino time
Date addedOn = new Date(epoch*1000);

novosValues.put(TemperatureContract.TemperatureEntry.COLUMN_CREATED, dateFormat.format(dateOn) );


```

### Comunication android ←→ server in real time using socket.io

http://socket.io/blog/native-socket-io-and-android/

## Android to Arduino protocol

### Arduino metadata

* header
* sensor*

### Messages to Arduino

Message | Example
--------|-----------
M       | M             | Metadata request
T       | T1446723851   | Android epoch to ajust the Arduino clock
I       | I2            | Reading interval (2 sec.)
P       | P             | Pause readings

### Messages from Arduino

Message | Example
--------|-----------
R       | `R|2|147.681|1446723851`
T       | `T|1446723851`
T       | `T|1446723851|1446723851` // TODO
M       | `M|1|3|1445125085`
S       | `S|1|Rita Rocha|1445122175|PT 100|T|1|1|3|2.738895|1.007828`
E       | 
P       | `P|On` ou `P|Off`
W       | `W|Message not recognized`

## Arduino DHT11

Two possible libraries that works:

### DHTlib (needs a small adjustment on the DHTLIB_TIMEOUT)

```
cd ~/Arduino/libraries
svn export https://github.com/RobTillaart/Arduino.git/trunk/libraries/DHTlib
```

Edit `DHTlib/dht.h`

```
#ifndef F_CPU
#define DHTLIB_TIMEOUT 10000  // ahould be approx. clock/40000
#else
#define DHTLIB_TIMEOUT (F_CPU/1600) // (F_CPU/40000)
#endif
```

```
DHT TEST PROGRAM 
LIBRARY VERSION: 0.1.21

Type,	status,	Humidity (%),	Temperature (C)
16000000
DHT11, 	OK,	47.0,	17.0
DHT11, 	OK,	47.0,	17.0
DHT11, 	OK,	47.0,	17.0
DHT11, 	OK,	47.0,	17.0
DHT11, 	OK,	47.0,	17.0
```

### DHT-sensor-library

```
DHTtester
```

```
DHTxx test!
Humidity: 47.00 %	Temperature: 17.00 *C 
Humidity: 47.00 %	Temperature: 17.00 *C 
Humidity: 47.00 %	Temperature: 17.00 *C 
Humidity: 47.00 %	Temperature: 17.00 *C 
Humidity: 47.00 %	Temperature: 17.00 *C 
```

### DHT-sensor-library + Adafruit_DHT_Unified

```
DHT_Unified_Sensor/DHT_Unified_Sensor.ino

// Depends on the following Arduino libraries:
// - Adafruit Unified Sensor Library: https://github.com/adafruit/Adafruit_Sensor
// - DHT Sensor Library: https://github.com/adafruit/DHT-sensor-library

#include <Adafruit_Sensor.h>
#include <DHT.h>
#include <DHT_U.h>
```

Output:

```
DHTxx Unified Sensor Example
------------------------------------
Temperature
Sensor:       DHT11
Driver Ver:   1
Unique ID:    -1
Max Value:    50.00 *C
Min Value:    0.00 *C
Resolution:   2.00 *C
------------------------------------
------------------------------------
Humidity
Sensor:       DHT11
Driver Ver:   1
Unique ID:    -1
Max Value:    80.00%
Min Value:    20.00%
Resolution:   5.00%
------------------------------------
Temperature: 17.000000 *C
Humidity: 45.00%
Temperature: 17.000000 *C
Humidity: 45.00%
Temperature: 17.000000 *C
Humidity: 46.00%
```
