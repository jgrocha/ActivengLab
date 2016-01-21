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
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;
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
    URL url;

    /**
     * Set up the sync adapter
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);

        try {
            url = new URL("http://192.168.1.102:3000/direct");
        } catch (MalformedURLException e) {
            Log.d("ActivEng", "SyncAdapter: MalformedURLException");
            e.printStackTrace();
        }

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

    public String getSensorStatusFromRemoteServer(int sid, String address, final SyncResult syncResult) {
        Log.d("ActivEng", "---8<------ getSensorStatusFromRemoteServer -------------------->8----");

        //URL url;
        HttpURLConnection urlConn = null;
        DataOutputStream printout;
        String line, result = "";

        try {
            urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setDoOutput(true);
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

            Log.d("ActivEng", "getSensorStatusFromRemoteServer: JSON: " + request);

            OutputStreamWriter writer = new OutputStreamWriter(urlConn.getOutputStream());
            writer.write(request);
            writer.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            while ((line = reader.readLine()) != null) {
                Log.d("ActivEng", "getSensorStatusFromRemoteServer: InputStreamReader: " + line);
                result += line;
            }
            writer.close();
            reader.close();

        } catch (MalformedURLException e) {
            Log.d("ActivEng", "getSensorStatusFromRemoteServer: MalformedURLException");
            syncResult.stats.numParseExceptions++;
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            Log.d("ActivEng", "getSensorStatusFromRemoteServer: UnsupportedEncodingException");
            e.printStackTrace();
        } catch (JSONException e) {
            Log.d("ActivEng", "getSensorStatusFromRemoteServer: JSONException");
            syncResult.stats.numParseExceptions++;
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("ActivEng", "getSensorStatusFromRemoteServer: IOException");
            syncResult.stats.numIoExceptions++;
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
            Map<String, List<String>> map = urlConn.getHeaderFields();
            for (String key : map.keySet()) {
                List<String> values = map.get(key);
                for (String value : values) {
                    Log.d("ActivEng", key + " --> " + value);
                }
            }
            e.printStackTrace();
        } finally {
            urlConn.disconnect();
        }
        return result;
    }

    public String createSensor(int sid, String address, final SyncResult syncResult) {
        Log.d("ActivEng", "---8<------ createSensor -------------------->8----");

        //URL url;
        HttpURLConnection urlConn = null;
        DataOutputStream printout;
        String line, result = "";

        try {
            urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setDoOutput(true);
            urlConn.setConnectTimeout(5000);
            urlConn.setReadTimeout(5000);
            urlConn.setRequestProperty("Accept", "application/json");
            urlConn.setRequestProperty("Content-Type", "application/json");
            urlConn.connect();

            //Create JSONObject here
            JSONObject jsonParam = new JSONObject();
            jsonParam.put("action", "ActiveEngCloud.Sensor");
            jsonParam.put("method", "create");
            jsonParam.put("type", "rpc");
            jsonParam.put("tid", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));

            JSONObject jsonData = new JSONObject();
            //jsonData.put("type", "Ana Isabel");
            //jsonData.put("location", "910333131");
            jsonData.put("address", address);
            jsonData.put("sensorid", Integer.toString(sid));
            jsonData.put("location", "Meizu MX 4 PRO");
            jsonData.put("sensortype", "Nimbus 2000");

            JSONArray rows = new JSONArray();
            rows.put(jsonData);
            jsonParam.put("data", rows);

            //String request = URLEncoder.encode(
            //        jsonParam.toString(),
            //        "UTF-8" // java.nio.charset.StandardCharsets.UTF_8.toString()
            //);
            String request = jsonParam.toString();

            Log.d("ActivEng", "createSensor: JSON: " + request);

            OutputStreamWriter writer = new OutputStreamWriter(urlConn.getOutputStream());
            writer.write(request);
            writer.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            while ((line = reader.readLine()) != null) {
                Log.d("ActivEng", "createSensor: InputStreamReader: " + line);
                result += line;
            }
            writer.close();
            reader.close();

        } catch (MalformedURLException e) {
            Log.d("ActivEng", "createSensor: MalformedURLException");
            syncResult.stats.numParseExceptions++;
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            Log.d("ActivEng", "createSensor: UnsupportedEncodingException");
            e.printStackTrace();
        } catch (JSONException e) {
            Log.d("ActivEng", "createSensor: JSONException");
            syncResult.stats.numParseExceptions++;
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("ActivEng", "createSensor: IOException");
            syncResult.stats.numIoExceptions++;
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
            Map<String, List<String>> map = urlConn.getHeaderFields();
            for (String key : map.keySet()) {
                List<String> values = map.get(key);
                for (String value : values) {
                    Log.d("ActivEng", key + " --> " + value);
                }
            }
            e.printStackTrace();
        } finally {
            urlConn.disconnect();
        }
        return result;
    }

    public int processSensor(String result, int sid, String address, final SyncResult syncResult) {
        Log.d("ActivEng", "---8<------ processSensor -------------------->8----");
        Log.d("ActivEng", "result = " + result);

        String resultCreate;
        int inserts = 0;
        int updates = 0;
        int res = 0;

        /*
        {
          "action": "ActiveEngCloud.Sensor",
          "result": {
            "total": 1,resultCreate
            "data": [getSensorStatusFromRemoteServer
              {
                "addressresultCreate": "30:14:12:18:06:34",
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

        try {
            JSONArray json = new JSONArray(result);
            for (int i = 0; i < json.length(); i++) {
                JSONObject jObj = json.getJSONObject(i);
                Log.d("ActivEng", "result[" + i + "] = " + jObj);
                JSONObject subObj = jObj.getJSONObject("result");
                int total = subObj.getInt("total");
                if (total == 0) {
                    Log.d("ActivEng", "sensor " + sid + "@" + address + " does not exist on the server"); // good!
                    resultCreate = createSensor(sid, address, syncResult);
                    Log.d("ActivEng", "createSensor(" + sid + ", " + address + ")=" + resultCreate);
                    // [{"type":"exception","tid":1452936830,"action":"ActiveEngCloud.Sensor","method":"create","message":{"text":"Database error","detail":"error: null value in column \"sensortype\" violates not-null constraint"},"data":{"message":{"text":"Database error","detail":"error: null value in column \"sensortype\" violates not-null constraint"}}}]
                    // [{"type":"rpc","tid":1452938503,"action":"ActiveEngCloud.Sensor","method":"create","result":{"data":[{"sensorid":2,"address":"30:14:12:18:06:34","location":"Meizu MX 4 PRO","installdate":"2016-01-16T10:01:43.430Z","sensortype":"Nimbus 2000","metric":1,"calibrated":0,"quantity":"T","decimalplaces":3,"cal_a":0,"cal_b":1,"read_interval":2000,"record_sample":1,"id":8}],"total":1,"success":true}}]
                    JSONArray jsonCreate = new JSONArray(resultCreate);
                    JSONObject jObjCreate = jsonCreate.getJSONObject(0);
                    Log.d("ActivEng", "createSensor result[0] = " + jObjCreate);
                    String type = jObjCreate.getString("type");
                    if (type.equals("exception")) {
                        Log.d("ActivEng", "createSensor deu erro :-( " + type);
                    } else {
                        JSONObject subObjCreate = jObjCreate.getJSONObject("result");
                        int totalCreate = subObjCreate.getInt("total");
                        if (totalCreate == 1) {
                            Log.d("ActivEng", "createSensor correu muito bem :-) ");
                            inserts++;
                            res = uploadSensorTemperature(sid, address, null, syncResult);
                            inserts += res;
                        }
                    }
                } else {
                    JSONArray jArr = subObj.getJSONArray("data");
                    for (int j = 0; j < jArr.length(); j++) {
                        JSONObject jRow = jArr.getJSONObject(j);
                        String lastcalibrationstr = jRow.getString("lastcalibration");
                        String lasttemperaturestr = jRow.getString("lasttemperature");
                        // "lasttemperature":"2015-11-08T19:28:14.038Z"
                        // "lasttemperature": null
                        Log.d("ActivEng", "processSensor: lasttemperaturestr: " + lasttemperaturestr);
                        if (lasttemperaturestr != null && !lasttemperaturestr.isEmpty() && !lasttemperaturestr.equals("null")) {
                            try {
                                lasttemperaturestr = lasttemperaturestr.replaceAll("Z", "+0000");
                                Log.d("ActivEng", "processSensor: lasttemperaturestr: " + lasttemperaturestr);
                                Date lasttemperature = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault()).parse(lasttemperaturestr);
                                //TimeZone tz = TimeZone.getTimeZone("UTC");
                                //DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                //df.setTimeZone(tz);
                                String nowAsISO = df.format(lasttemperature);
                                Log.d("ActivEng", "processSensor: " + nowAsISO); // perfeito p/ sensor 1
                                res = uploadSensorTemperature(sid, address, nowAsISO, syncResult);
                                inserts += res;
                            } catch (ParseException e) {
                                Log.d("ActivEng", "processSensor: ParseException: lasttemperaturestr=" + lasttemperaturestr + " lastcalibrationstr=" + lastcalibrationstr);
                                e.printStackTrace();
                            }
                        } else {
                            Log.d("ActivEng", "processSensor: no temperature data on server for sensor " + sid); // perfeito p/ sensor 3
                            res = uploadSensorTemperature(sid, address, null, syncResult);
                            inserts += res;
                        }
                        Log.d("ActivEng", "row[" + j + "] = " + jRow);
                    }
                }
            }
        } catch (JSONException e) {
            Log.d("ActivEng", "processSensor: JSONException");
            syncResult.stats.numParseExceptions++;
            e.printStackTrace();
        }
        return inserts;
    }

    public String createTemperature(int sid, String address, String created, Double value, Integer metric, Integer cal, final SyncResult syncResult) {
        Log.d("ActivEng", "---8<------ createTemperature -------------------->8----");

        //URL url;
        HttpURLConnection urlConn = null;
        DataOutputStream printout;
        String line, result = "";

        try {
            urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setDoOutput(true);
            urlConn.setConnectTimeout(5000);
            urlConn.setReadTimeout(5000);
            urlConn.setRequestProperty("Accept", "application/json");
            urlConn.setRequestProperty("Content-Type", "application/json");
            urlConn.connect();

            //Create JSONObject here
            JSONObject jsonParam = new JSONObject();
            jsonParam.put("action", "ActiveEngCloud.Temperature");
            jsonParam.put("method", "create");
            jsonParam.put("type", "rpc");
            jsonParam.put("tid", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));

            JSONObject jsonData = new JSONObject();
            //jsonData.put("type", "Ana Isabel");
            //jsonData.put("location", "910333131");
            jsonData.put("address", address);
            jsonData.put("sensorid", Integer.toString(sid));
            jsonData.put("value", value);
            jsonData.put("created", created);

            JSONArray rows = new JSONArray();
            rows.put(jsonData);
            jsonParam.put("data", rows);

            //String request = URLEncoder.encode(
            //        jsonParam.toString(),
            //        "UTF-8" // java.nio.charset.StandardCharsets.UTF_8.toString()
            //);
            String request = jsonParam.toString();

            Log.d("ActivEng", "createTemperature: JSON: " + request);

            OutputStreamWriter writer = new OutputStreamWriter(urlConn.getOutputStream());
            writer.write(request);
            writer.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            while ((line = reader.readLine()) != null) {
                Log.d("ActivEng", "createTemperature: InputStreamReader: " + line);
                result += line;
            }
            writer.close();
            reader.close();

        } catch (MalformedURLException e) {
            Log.d("ActivEng", "createTemperature: MalformedURLException");
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            Log.d("ActivEng", "createTemperature: UnsupportedEncodingException");
            e.printStackTrace();
        } catch (JSONException e) {
            Log.d("ActivEng", "createTemperature: JSONException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("ActivEng", "createTemperature: IOException");
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
            Map<String, List<String>> map = urlConn.getHeaderFields();
            for (String key : map.keySet()) {
                List<String> values = map.get(key);
                for (String thevalue : values) {
                    Log.d("ActivEng", key + " --> " + thevalue);
                }
            }
            e.printStackTrace();
        } finally {
            urlConn.disconnect();
        }
        return result;
    }

    public int uploadSensorTemperature(int sid, String address, String last, final SyncResult syncResult) {
        Log.d("ActivEng", "uploadSensorTemperature: " + sid + " " + address + " " + last);

        Uri weatherForLocationUri = TemperatureContract.TemperatureEntry.buildTemperatureWithLastDate(sid, address, last);
        Cursor mCursor = mContentResolver.query(
                weatherForLocationUri,   // The content URI of the words table
                null,                        // The columns to return for each row
                null,                    // Selection criteria
                null,                     // Selection criteria
                null);
        Log.d("ActivEng", "uploadSensorTemperature: mCursor.getCount(): " + mCursor.getCount());
        int i=0;
        while (mCursor.moveToNext() && i<500) {
            Integer _sensorid = mCursor.getInt(mCursor.getColumnIndexOrThrow(TemperatureContract.TemperatureEntry.COLUMN_SENSORID));
            String _address = mCursor.getString(mCursor.getColumnIndexOrThrow(TemperatureContract.TemperatureEntry.COLUMN_ADDRESS));
            // SQLite stores a string representing the time in UTC (GMT), using the format YYYY-MM-DD HH:MM:SS
            // http://stackoverflow.com/questions/14255830/fetching-date-from-sqlite-database-in-android
            String _created = mCursor.getString(mCursor.getColumnIndexOrThrow(TemperatureContract.TemperatureEntry.COLUMN_CREATED));
            Double _value = mCursor.getDouble(mCursor.getColumnIndexOrThrow(TemperatureContract.TemperatureEntry.COLUMN_VALUE));
            Integer _metric = mCursor.getInt(mCursor.getColumnIndexOrThrow(TemperatureContract.TemperatureEntry.COLUMN_METRIC));
            Integer _cal = mCursor.getInt(mCursor.getColumnIndexOrThrow(TemperatureContract.TemperatureEntry.COLUMN_CALIBRATED));
            createTemperature(_sensorid, _address, _created, _value, _metric, _cal, syncResult);
            i = i+1;
        }
        mCursor.close();
        return i;
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
        int i = 0;
        int res = 0;

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
            Log.d("ActivEng", "onPerformSync: getSensorStatusFromRemoteServer(" + sensorid + "); // TODO");
            res = processSensor(getSensorStatusFromRemoteServer(sensorid, address, syncResult), sensorid, address, syncResult);
            Log.d("ActivEng", "onPerformSync: getSensorStatusFromRemoteServer(" + sensorid + "); Done!");
            i += 1;
            syncResult.stats.numInserts += res;
        }
        cursor.close();

        syncResult.stats.numEntries = syncResult.stats.numInserts;
    }

}


