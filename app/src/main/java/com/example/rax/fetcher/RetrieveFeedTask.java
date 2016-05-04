package com.example.rax.fetcher;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by rax on 5/1/2016.
 */
public class RetrieveFeedTask extends AsyncTask<String, String, String> {
    private final static String TAG = "RetrieveFeedTask";
    private String resultJSON = "";
    @Override
    protected String doInBackground(String... params) {
        HttpURLConnection myHttpURLConnection = null;
        String urlString = "http://api.duckduckgo.com/?q="+params[0]+"&format=json&pretty=1&skip_disambig=1";
        Log.d(TAG, "getInformation: 1 " +params[0]);
        try{
            URL myUrl = new URL(urlString);
            myHttpURLConnection = (HttpURLConnection)myUrl.openConnection();
            InputStream myInputStream = myHttpURLConnection.getInputStream();
            BufferedReader myBufferedReader = new BufferedReader(new InputStreamReader(myInputStream));
            String line = myBufferedReader.readLine();
            resultJSON += line;

            while(line != null){
                line = myBufferedReader.readLine();
                resultJSON += line;
            }

            Log.d(TAG, "getInformation: "+resultJSON);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        Log.d(TAG, "getInformation: "+3);
        return resultJSON;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
    }

}
