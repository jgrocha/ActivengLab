package activeng.pt.activenglab;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class DetailActivity extends AppCompatActivity {

    static final String STATE_SENSOR = "Sensor";
    public String mySensor = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("Life cyle", "DetailActivity onCreate");
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        //FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        //fab.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View view) {
        //        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
        //                .setAction("Action", null).show();
        //    }
        //});

        if (savedInstanceState == null) {
            Log.d("Life cyle", "DetailActivity onCreate: savedInstanceState == null");
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
                mySensor = intent.getStringExtra(Intent.EXTRA_TEXT);
            }
        } else {
            Log.d("Life cyle", "DetailActivity onCreate: savedInstanceState != null");
            mySensor = savedInstanceState.getString(STATE_SENSOR);
        }
        setTitle(mySensor);

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.detail_action_settings) {
            //Toast toast = Toast.makeText(getApplicationContext(), "Let's calibrate this sensor", Toast.LENGTH_SHORT);
            //toast.show();

            Intent intent = new Intent(this, CalibrationActivity.class);
            intent.putExtra(Intent.EXTRA_TEXT, mySensor);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // http://stackoverflow.com/questions/151777/saving-activity-state-on-android
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(STATE_SENSOR, mySensor);
        Log.d("Life cyle", "--> DetailActivity onSaveInstanceState. mySensor = " + mySensor);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);
        // Restore state members from saved instance
        mySensor = savedInstanceState.getString(STATE_SENSOR);
        Log.d("Life cyle", "<-- DetailActivity onRestoreInstanceState");
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        Log.d("Life cyle", "DetailActivity onResume");
    }

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first
        Log.d("Life cyle", "DetailActivity onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();  // Always call the superclass method first
        Log.d("Life cyle", "DetailActivity onStop");
    }

    @Override
    protected void onRestart() {
        super.onRestart();  // Always call the superclass method first
        Log.d("Life cyle", "DetailActivity onRestart");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();  // Always call the superclass
        Log.d("Life cyle", "DetailActivity onDestroy");
    }

    @Override
    protected void onStart() {
        super.onStart();  // Always call the superclass method first
        Log.d("Life cyle", "DetailActivity onStart");
    }

}
