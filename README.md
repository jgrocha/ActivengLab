![ActivengLab](resources/ActivengLab_800x.png)

Measuring temperatures using high precision RTD.

## Android development

### Icon

New -> Image Asset -> Asset Type: Launcher Icons

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

##### Notes

[Type Unicode character](http://askubuntu.com/questions/31258/how-can-i-type-a-unicode-character-for-example-em-dash)
Compose key: `Crtl-right` Arrow: `Compose key`, `->`
