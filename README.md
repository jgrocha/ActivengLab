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

```
MainActivity
    Fragment
        ListView
            TextView (to improve soon...) → Launch DetailActivity
```

```
DetailActivity
    Fragment
        TextView (to improve soon...)
```

##### Notes

[Type Unicode character](http://askubuntu.com/questions/31258/how-can-i-type-a-unicode-character-for-example-em-dash)
Compose key: `Crtl-right` Arrow: `Compose key`, `->`
