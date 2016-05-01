package com.mobileprogramming.falcons.fetcher;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private void doSearch(String searchVal) {
        final String apiAddress = "https://www.googleapis.com/customsearch/v1?";
        //Register with ???
        final String apiKey = "key=KEY_HERE";
        //Register new search engine in Google Control Panel.
        final String searchEngine = "cx=SEARCH_ENGINE_ID_HERE";
        final String query = "q=" + searchVal;
        String finalAPI = apiAddress + apiKey + "&" + searchEngine + "&" + query;

        //Do an HTTP GET here.

        //If result isn't 200 OK, log error and abort.

        //Otherwise, we get a JSON object with the results.
        //items contains the actual results.
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Connect to services.

        //Connect to data source.

        //Bind data to UI.
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
