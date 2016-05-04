package com.example.rax.fetcher;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {


    // View UI buttons
    private EditText mTextViewWord;
    private TextView mTextViewDesc;
    private Button mButtonFetch;
    private ListView mListView;
    private Firebase mRef;
    private ArrayList<String> mWords;
    private ArrayList<String> mDesc;
    private RetrieveFeedTask myFeed;
    private String wordDescription = "";

    // Contants Declaration
    private final static String DATABASE_NAME = "Fetcher";
    private final static String TAG = "MainActivity";



    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Fetching UI components
        mTextViewWord = (EditText) findViewById(R.id.editTextWord);
        mTextViewDesc = (TextView) findViewById(R.id.textViewDescription);
        mButtonFetch = (Button) findViewById(R.id.buttonFetch);
        // Recycler view
        mListView = (ListView) findViewById(R.id.listViewFetch);

        // Getting Firebase root reference
        mRef = new Firebase("https://scorching-fire-6605.firebaseio.com");

        mWords = new ArrayList<>();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        // Fetching description for the provided keyWord using Asynctask
        myFeed = new RetrieveFeedTask();
        try {
            wordDescription = myFeed.execute("iOS").get();
            searchParseResponse(wordDescription);
            Log.d(TAG, "onCreate: "+wordDescription);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        final ArrayAdapter<String> myArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mWords);
        mListView.setAdapter(myArrayAdapter);

        Firebase messagesRef = mRef.child("Fetcher");
//        messagesRef.addValueEventListener(new ValueEventListener() {
//            // Fired every time data changes in our database
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
////                Log.d(TAG, "onDataChange: " + dataSnapshot.getValue(String.class));
//                Map<String, String> mMap = dataSnapshot.getValue(Map.class);
//                Set<String> mKeySet = mMap.keySet();
//                for(String s : mKeySet)
//                    mTextViewWord.setText(s);
//
//            }
//
//            @Override
//            public void onCancelled(FirebaseError firebaseError) {
//
//            }
//        });
        messagesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String word = dataSnapshot.getKey();
                String desc = dataSnapshot.getValue(String.class);
                mWords.add(word);
//                mDesc.add(desc);

                myArrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

//        FirebaseListAdapter<String> myFirebaseListAdapter = new FirebaseListAdapter<String>(
//                this,
//                String.class,
//                android.R.layout.simple_list_item_1,
//                mRef
//        ) {
//            @Override
//            protected void populateView(View view, String s, int i) {
//                TextView tv = (TextView) view.findViewById(android.R.id.text1);
//                tv.setText(s);
//            }
//        };
//        mListViewFetch.setAdapter(myFirebaseListAdapter);
    }

//    public void getInformation(String key){
//        HttpURLConnection myHttpURLConnection = null;
//        String urlString = "http://api.duckduckgo.com/?q="+key+"&format=json&pretty=1&skip_disambig=1";
//        Log.d(TAG, "getInformation: "+1);
//        try{
//            URL myUrl = new URL(urlString);
//            myHttpURLConnection = (HttpURLConnection)myUrl.openConnection();
//            InputStream myInputStream = myHttpURLConnection.getInputStream();
//            BufferedReader myBufferedReader = new BufferedReader(new InputStreamReader(myInputStream));
//            String json = myBufferedReader.readLine();
//            Log.d(TAG, "getInformation: "+json);
//        }
//        catch (Exception e){
//            e.printStackTrace();
//        }
//        Log.d(TAG, "getInformation: "+3);
//    }

    @Override
    protected void onStart() {
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.rax.fetcher/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    // Parse tags in the JSON object got from API results
    protected void searchParseResponse(String response){
        Log.d(TAG, "searchParseResponse: "+1);
        try{
            JSONObject obj = new JSONObject(response);
            String abs = obj.getString("Abstract");
            Log.d(TAG, "searchParseResponse: 2 "+abs);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        Log.d(TAG, "searchParseResponse: "+3);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.rax.fetcher/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

}
