package activeng.pt.activenglab;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import activeng.pt.activenglab.data.TemperatureContract;

public class DetailActivity extends AppCompatActivity {

    static final String STATE_SENSOR = "Sensor";
    private long sensorId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("Life cyle", "DetailActivity onCreate");
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            Log.d("Life cyle", "DetailActivity onCreate: savedInstanceState == null");
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra(TemperatureContract.SensorEntry._ID)) {
                sensorId = intent.getLongExtra(TemperatureContract.SensorEntry._ID, 0);
            }
        }

        setTitle("Sensor " + sensorId);

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
        if (id == R.id.detail_action_calibration) {
            // Not implemented here
            return false;
        }

        return super.onOptionsItemSelected(item);
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

    public void done() {
        super.onBackPressed();
    }
}
