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
import java.net.URLConnection;
import java.net.URLEncoder;

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
    /*
     * Put the data transfer code here.
     */
        Log.d("ActivEng", "---8<------ onPerformSync -------------------->8----");

        URL url;
        HttpURLConnection urlConn = null;
        DataOutputStream printout;
        DataInputStream input;
        try {
            url = new URL("http://192.168.1.101:3000/direct");
            urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setDoInput(true);
            urlConn.setDoOutput(true);
            urlConn.setUseCaches(false);
            urlConn.setConnectTimeout(5000);
            urlConn.setReadTimeout(5000);
            urlConn.setRequestProperty("Accept", "application/json");
            urlConn.setRequestProperty("Content-Type", "application/json");
            urlConn.connect();

            //Create JSONObject here
            JSONObject jsonParam = new JSONObject();
            jsonParam.put("action", "AppGeoExt3.PgPersonnel");
            jsonParam.put("method", "create");
            jsonParam.put("type", "rpc");
            jsonParam.put("tid", 4);

            JSONObject jsonData = new JSONObject();
            jsonData.put("name", "Ana Isabel");
            jsonData.put("phone", "910333131");
            jsonData.put("email", "anivilar@gmail.com");
            jsonData.put("id", "5126");

            JSONArray rows = new JSONArray();
            rows.put(jsonData);
            jsonParam.put("data", rows);

            //String request = URLEncoder.encode(
            //        jsonParam.toString(),
            //        "UTF-8" // java.nio.charset.StandardCharsets.UTF_8.toString()
            //);
            String request = jsonParam.toString();

            Log.d("ActivEng", "onPerformSync: JSON: " + request);

            // Send POST output.
            //printout = new DataOutputStream(urlConn.getOutputStream());
            //printout.writeUTF(request);
            //printout.flush();
            //printout.close();

            OutputStreamWriter writer = new OutputStreamWriter(urlConn.getOutputStream());
            writer.write(request);
            writer.flush();

            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            while ((line = reader.readLine()) != null) {
                //System.out.println(line);
                Log.d("ActivEng", "onPerformSync: InputStreamReader: " +  line);
            }
            writer.close();
            reader.close();

        } catch (MalformedURLException e) {
            Log.d("ActivEng", "onPerformSync: MalformedURLException");
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            Log.d("ActivEng", "onPerformSync: UnsupportedEncodingException");
            e.printStackTrace();
        } catch (JSONException e) {
            Log.d("ActivEng", "onPerformSync: JSONException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("ActivEng", "onPerformSync: IOException");
            e.printStackTrace();
        } finally {
            urlConn.disconnect();
        }

    }

}


