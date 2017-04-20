package com.example.codyhunsberger.nexttoarrive;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity implements OutputFragment.NavigationListener{
    String departing_station, arrival_station, json;
    int number_results, jsonArrayLength, index = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Spinner departure_spinner = (Spinner) findViewById(R.id.departure_spinner);
        final Spinner arrival_spinner = (Spinner) findViewById(R.id.arrival_spinner);
        final Spinner results_count_spinner = (Spinner) findViewById(R.id.results_count_spinner);
        ArrayAdapter<CharSequence> station_adapter, results_adapter;

        station_adapter = ArrayAdapter.createFromResource(this, R.array.stations,android.R.layout.simple_spinner_item);
        station_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        results_adapter = ArrayAdapter.createFromResource(this, R.array.results,android.R.layout.simple_spinner_item);
        results_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        departure_spinner.setAdapter(station_adapter);
        arrival_spinner.setAdapter(station_adapter);
        results_count_spinner.setAdapter(results_adapter);

        Button submit_button = (Button) findViewById(R.id.submit_button);

        departure_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
                                       int position, long id) {
                departing_station = departure_spinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
        });

        arrival_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
                                       int position, long id) {
                arrival_station = arrival_spinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
        });

        results_count_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
                                       int position, long id) {
                number_results = position + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
        });

        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullUrl = format_url(getResources().getString(R.string.nta_base_url) + departing_station + "/" + arrival_station + "/" + number_results);
                getJsonString(fullUrl);
                if (departing_station.equals(arrival_station)) {
                    Toast.makeText(getApplicationContext(), "Please select two different stations.", Toast.LENGTH_LONG).show();
                }
                else {
                    OutputFragment frag = OutputFragment.newInstance(getJSONData(0), index, jsonArrayLength);
                    displayFrag(frag);
                }
            }
        });
    }

    public void onButtonPress(int index) {
        OutputFragment frag = OutputFragment.newInstance(getJSONData(index), index, jsonArrayLength);
        displayFrag(frag);
    }

    // Convert string JSON response to string array of values
    public String[] getJSONData(int index) {
        /* Six items are included in the response:
         0. Train Number
         1. Line
         2. Departure Time
         3. Arrival Time
         4. Delay
         5. isdirect (unused for now, has to do with connections and transfers)
          */

        // Will return an array of length n, where n is the number of responses requested.
        String[] items = new String[6];
        JSONObject jsonObj;
        JSONArray jsonArray;

        try {
            jsonArray = new JSONArray(json);
            jsonArrayLength = jsonArray.length();
            jsonObj = jsonArray.getJSONObject(index);
            items[0] = jsonObj.getString("orig_train");
            items[1] = jsonObj.getString("orig_line");
            items[2] = jsonObj.getString("orig_departure_time");
            items[3] = jsonObj.getString("orig_arrival_time");
            items[4] = jsonObj.getString("orig_delay");
            items[5] = jsonObj.getString("isdirect");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    public void displayFrag(Fragment frag) {
        FragmentManager fragMan = getFragmentManager();
        FragmentTransaction fragTrans = fragMan.beginTransaction();
        fragTrans.replace(R.id.fragContainer, frag).addToBackStack(null).commit();
        // todo add landscape layout functionality
        /*
        if (!twoPanes) {
            fragTrans.replace(R.id.FragmentContainerA, frag)
                    .addToBackStack(null)
                    .commit();
        }
        else {
            fragTrans.replace(R.id.FragmentContainerB, frag)
                    .commit();
        }
        */
    }


    public String format_url(String url) {
        String[] words = url.split(" ");
        StringBuilder sentence = new StringBuilder(words[0]);

        for (int i = 1; i < words.length; ++i) {
            sentence.append("%20");
            sentence.append(words[i]);
        }

        return sentence.toString();
    }

    public String getJsonString(String url) {
        json = "";
        UrlToJsonString urlToJsonString = new UrlToJsonString();
        urlToJsonString.execute(url);

        while (urlToJsonString.isLocked()) {
            try {
                Thread.sleep(10);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (json.length() == 0)
        {
            Toast.makeText(this, "Error retrieving data from API.", Toast.LENGTH_SHORT).show();
        }

        return json;
    }

    class UrlToJsonString extends AsyncTask<String, Void, Void> {
        public boolean locked;

        public boolean isLocked() {
            return locked;
        }

        protected void onPreExecute() {
            locked = true;
        }

        protected Void doInBackground(String... urlString) {
            StringBuilder sb = new StringBuilder();
            URLConnection urlConn;
            InputStreamReader in = null;
            try {
                URL url = new URL(urlString[0]);
                urlConn = url.openConnection();
                if(urlConn != null)
                    urlConn.setReadTimeout(60 * 1000);
                if(urlConn != null && urlConn.getInputStream() != null) {
                    in = new InputStreamReader(urlConn.getInputStream(),
                            Charset.defaultCharset());
                    BufferedReader bufferedReader = new BufferedReader(in);
                    int cp;
                    while((cp = bufferedReader.read()) != -1) {
                        sb.append((char) cp);
                    }
                    bufferedReader.close();
                }
                if(in != null) {
                    in.close();
                }
            }
            catch(Exception e) {
                throw new RuntimeException("Exception while calling URL:" + urlString[0], e);
            }

            json = sb.toString();
            locked = false;
            return null;
        }
        // Would have set locked = false in onPostExecute but I can't get it to call that method
    }
}
