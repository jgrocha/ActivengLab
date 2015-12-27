package activeng.pt.activenglab.sync;

/**
 * Created by jgr on 06-11-2015.
 */

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import activeng.pt.activenglab.data.TemperatureContract;

/**
 * Handle the transfer of data between a server and an
 * app, using the Android sync adapter framework.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    // Global variables
    // Define a variable to contain a content resolver instance
    ContentResolver mContentResolver;

    /**
     * Set up the sync adapter
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContentResolver = context.getContentResolver();
    }

    /**
     * Set up the sync adapter. This form of the
     * constructor maintains compatibility with Android 3.0
     * and later platform versions
     */
    public SyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContentResolver = context.getContentResolver();

    }

    public String commSyncSensor(int sid, String address) {
        Log.d("ActivEng", "---8<------ commSyncSensor -------------------->8----");

        URL url;
        HttpURLConnection urlConn = null;
        DataOutputStream printout;
        String line, result = "";

        try {
            url = new URL("http://192.168.1.102:3000/direct");
            urlConn = (HttpURLConnection) url.openConnection();
            //urlConn.setDoInput(true); // not required...
            urlConn.setDoOutput(true);
            //urlConn.setUseCaches(false);
            urlConn.setConnectTimeout(5000);
            urlConn.setReadTimeout(5000);
            urlConn.setRequestProperty("Accept", "application/json");
            urlConn.setRequestProperty("Content-Type", "application/json");
            urlConn.connect();

            //Create JSONObject here
            JSONObject jsonParam = new JSONObject();
            jsonParam.put("action", "ActiveEngCloud.Sensor");
            jsonParam.put("method", "status");
            jsonParam.put("type", "rpc");
            jsonParam.put("tid", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));

            JSONObject jsonData = new JSONObject();
            jsonData.put("type", "Ana Isabel");
            jsonData.put("location", "910333131");
            jsonData.put("address", address);
            jsonData.put("sensorid", Integer.toString(sid));

            JSONArray rows = new JSONArray();
            rows.put(jsonData);
            jsonParam.put("data", rows);

            //String request = URLEncoder.encode(
            //        jsonParam.toString(),
            //        "UTF-8" // java.nio.charset.StandardCharsets.UTF_8.toString()
            //);
            String request = jsonParam.toString();

            Log.d("ActivEng", "commSyncSensor: JSON: " + request);

            OutputStreamWriter writer = new OutputStreamWriter(urlConn.getOutputStream());
            writer.write(request);
            writer.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            while ((line = reader.readLine()) != null) {
                //System.out.println(line);
                Log.d("ActivEng", "commSyncSensor: InputStreamReader: " + line);
                result += line;
            }
            writer.close();
            reader.close();

        } catch (MalformedURLException e) {
            Log.d("ActivEng", "commSyncSensor: MalformedURLException");
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            Log.d("ActivEng", "commSyncSensor: UnsupportedEncodingException");
            e.printStackTrace();
        } catch (JSONException e) {
            Log.d("ActivEng", "commSyncSensor: JSONException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("ActivEng", "commSyncSensor: IOException");
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(urlConn.getErrorStream()));
            try {
                while ((line = errorReader.readLine()) != null) {
                    //System.out.println(line);
                    Log.d("ActivEng", "getErrorStream: " + line);
                }
                errorReader.close();
            } catch (IOException enew) {
                Log.d("ActivEng", "getErrorStream: IOException");
                enew.printStackTrace();
            }
            // debug
            /*
            if (urlConn != null) {
                Map<String, List<String>> map = urlConn.getHeaderFields();
                for (String key : map.keySet()) {
                    List<String> values = map.get(key);
                    for (String value : values) {
                        Log.d("ActivEng", key + " --> " + value);
                    }
                }
            }
            */
            e.printStackTrace();
        } finally {
            urlConn.disconnect();
        }
        return result;
    }

    public void processSensor(String result) {
        Log.d("ActivEng", "---8<------ processSensor -------------------->8----");
        Log.d("ActivEng", "result = " + result);

        /*
        {
          "action": "ActiveEngCloud.Sensor",
          "result": {
            "total": 1,
            "data": [
              {
                "address": "30:14:12:18:06:34",
                "sensorid": 2,
                "lastcalibration": null,
                "lasttemperature": null
              }
            ],
            "success": true
          },
          "method": "status",
          "tid": 1451177847,
          "type": "rpc"
        }
         */
        //DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

        try {
            JSONArray json = new JSONArray(result);
            for (int i = 0; i < json.length(); i++) {
                JSONObject jObj = json.getJSONObject(i);
                Log.d("ActivEng", "result[" + i + "] = " + jObj);
                JSONObject subObj = jObj.getJSONObject("result");
                JSONArray jArr = subObj.getJSONArray("data");
                for (int j = 0; j < jArr.length(); j++) {
                    JSONObject jRow = jArr.getJSONObject(j);
                    String lastcalibration = jRow.getString("lastcalibration");
                    String lasttemperature = jRow.getString("lasttemperature");
                    //String string1 = "2001-07-04T12:08:56.235-0700";

                    /*
                    try {
                        Date result1 = df1.parse(lasttemperature);
                        javax.xml.bind.DatatypeConverter.parseDateTime("2010-01-01T12:00:00Z");
                        Calendar c = GregorianCalendar.getInstance();
                        c.setTime(aDate);
                        return javax.xml.bind.DatatypeConverter.printDateTime(c);

                        Log.d("ActivEng", "processSensor: " + result1);

                    } catch (ParseException e) {
                        Log.d("ActivEng", "processSensor: ParseException");
                        e.printStackTrace();
                    }
                    */

                    /*
                    {
                    "address":"30:14:12:18:06:34",
                    "sensorid":1,
                    "lastcalibration":"2015-11-09T14:24:03.880Z",
                    "lasttemperature":"2015-11-08T19:28:14.038Z"

                    SimpleDateFormat mdyFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSX");

 SimpleDateFormat mdyFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSZ");

    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
    Date d1 = mdyFormat.parse("2014-04-01 15:19:49.31146+05:30");

    String mdx = sdf.format(d1);

    System.out.println(mdx);

                    }
                     */
                    Log.d("ActivEng", "row[" + j + "] = " + jRow);

                }
            }
        } catch (JSONException e) {
            Log.d("ActivEng", "commSyncSensor: JSONException");
            e.printStackTrace();
        }
    }

    /*
 * Specify the code you want to run in the sync adapter. The entire
 * sync adapter runs in a background thread, so you don't have to set
 * up your own background processing.
 */

    // https://developer.android.com/training/sync-adapters/creating-sync-adapter.html
    @Override
    public void onPerformSync(
            Account account,
            Bundle extras,
            String authority,
            ContentProviderClient provider,
            SyncResult syncResult) {

        long _ID;
        int sensorid;
        String address, location, type;
        int i = 1;

        Log.d("ActivEng", "---8<------ onPerformSync -------------------->8----");

        Cursor cursor = mContentResolver.query(
                TemperatureContract.SensorEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        Log.d("ActivEng", "onPerformSync: cursor.getCount(): " + cursor.getCount());

        while (cursor.moveToNext()) {
            _ID = cursor.getLong(cursor.getColumnIndex(TemperatureContract.SensorEntry._ID));
            sensorid = cursor.getInt(cursor.getColumnIndexOrThrow(TemperatureContract.SensorEntry.COLUMN_SENSORID));
            address = cursor.getString(cursor.getColumnIndexOrThrow(TemperatureContract.SensorEntry.COLUMN_ADDRESS));
            location = cursor.getString(cursor.getColumnIndexOrThrow(TemperatureContract.SensorEntry.COLUMN_LOCATION));
            type = cursor.getString(cursor.getColumnIndexOrThrow(TemperatureContract.SensorEntry.COLUMN_SENSORTYPE));
            Log.d("ActivEng", "cursor: " + address);

            processSensor(commSyncSensor(sensorid, address));

            Log.d("ActivEng", "onPerformSync: commSyncSensor(" + sensorid + "); ");
            i += 1;
        }
        cursor.close();

    }

}


