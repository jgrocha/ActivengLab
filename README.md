![ActivengLab](resources/ActivengLab_800x.png)

Measuring temperatures using high precision RTD.

## Glossary

ActivengLab App: Mobile application

Controller: Arduino cnnect to sensors or relay

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

Real device (not working):

```
/home/jgr/Android/Sdk/platform-tools/adb -d shell "run-as activeng.pt.activenglab ls /data/data/activeng.pt.activenglab/databases/"
/home/jgr/Android/Sdk/platform-tools/adb pull /data/data/activeng.pt.activenglab/databases/temperature.db /home/jgr/AndroidStudioProjects/ActivengLab/
/home/jgr/Android/Sdk/platform-tools/adb push /home/jgr/AndroidStudioProjects/ActivengLab/temperature.db /data/data/activeng.pt.activenglab/databases/
 
/home/jgr/Android/Sdk/platform-tools/adb push /home/jgr/AndroidStudioProjects/ActivengLab/temperature.db /sdcard/temperature.db
/home/jgr/Android/Sdk/platform-tools/adb -d shell "run-as activeng.pt.activenglab cat /sdcard/temperature.db > /data/data/activeng.pt.activenglab/databases/temperature.db"
exit
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

### Bluetooth

Initial support:

* BluetoothChatService + Broadcast + Listener in every Activity

More on [broadcast](http://stackoverflow.com/questions/17082393/handlers-and-multiple-activities).

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

##### Notes

[Type Unicode character](http://askubuntu.com/questions/31258/how-can-i-type-a-unicode-character-for-example-em-dash)
Compose key: `Crtl-right` Arrow: `Compose key`, `->`
