![ActivengLab](resources/ActivengLab_800x.png)

Measuring temperatures using high precision RTD.

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

Android Device Monitor → File Explorer → data/data/activeng.pt.activenglab/databases/temperature.db

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
        Settings
    Fragment
        ListView
            Custom View (from ItemSensorCursorAdapter) → Launch DetailActivity(Uri)
```

```
DetailActivity
    Action Bar
        CalibrationActivity
            GridLayout
                (TextView|EditText)+
                Calculate button, Save button
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

##### Notes

[Type Unicode character](http://askubuntu.com/questions/31258/how-can-i-type-a-unicode-character-for-example-em-dash)
Compose key: `Crtl-right` Arrow: `Compose key`, `->`
