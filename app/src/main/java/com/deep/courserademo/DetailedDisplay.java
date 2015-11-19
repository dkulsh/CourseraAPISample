package com.deep.courserademo;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DetailedDisplay extends AppCompatActivity {

    private String LOGTAG = "DetailDisplay";
    static List<JSONObject> jsonObjects = new ArrayList<JSONObject>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_display);

        String selection = getIntent().getStringExtra(MainActivity.JSON_STRING);
        Log.i(LOGTAG, "Selection = " + selection);
        FetchCourseraTask fetchCourseraTask = new FetchCourseraTask();
        fetchCourseraTask.execute(selection);

        ListView listView = (ListView) findViewById(R.id.listViewDetail);
//        Log.i(LOGTAG, "Size of JSONObjects before calling adapter " + jsonObjects.size());
        listView.setAdapter(new DetailedDisplayAdapter(this, android.R.layout.simple_list_item_1, jsonObjects));
    }

    @Override
    protected void onPause() {
        super.onPause();
        jsonObjects.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detailed_display, menu);
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

    public class DetailedDisplayAdapter extends ArrayAdapter {
        Context context;

        String NAME = "name";
        String SHORT_NAME = "shortName";
        String LINKS = "links";
        String ID = "id";

        public DetailedDisplayAdapter(Context context, int resource, List objects) {
            super(context, resource, objects);

            this.context = context;
            Log.i(LOGTAG, "Size of List inside adapter is " + objects.size());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
//            return super.getView(position, convertView, parent);

            final LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View detailView = layoutInflater.inflate(R.layout.detail_item, parent, false);

            TextView idView = (TextView) detailView.findViewById(R.id.itemId);
            TextView nameView = (TextView) detailView.findViewById(R.id.name);
            TextView shortNameView = (TextView) detailView.findViewById(R.id.shortName);
            TextView linksView = (TextView) detailView.findViewById(R.id.links);

            try {
                idView.setText(jsonObjects.get(position).getString(ID));
                Log.i(LOGTAG, "ID : " + jsonObjects.get(position).getString(ID));
                nameView.setText(jsonObjects.get(position).getString(NAME));
                Log.i(LOGTAG, "NAME : " + jsonObjects.get(position).getString(NAME));
                shortNameView.setText(jsonObjects.get(position).getString(SHORT_NAME));
                Log.i(LOGTAG, "SHORT_NAME : " + jsonObjects.get(position).getString(SHORT_NAME));
                linksView.setText(jsonObjects.get(position).getString(LINKS));
                Log.i(LOGTAG, "LINKS : " + jsonObjects.get(position).getString(LINKS));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return detailView;
        }
    }

    private class FetchCourseraTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {

            HttpURLConnection urlConnection = null;
            BufferedReader bufferedReader = null;
            String responseJsonString = "";

            Uri.Builder builder = new Uri.Builder();
            builder.scheme("https").
                    authority("api.coursera.org").
                    appendPath("api").
                    appendPath("catalog.v1")
                    .appendPath(strings[0]);

            try {
                URL url = new URL(builder.build().toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                if(inputStream == null) { return null; }

                bufferedReader = bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                StringBuffer stringBuffer = new StringBuffer();

                while((line = bufferedReader.readLine()) != null){
                    stringBuffer.append(line + "\n");
                }

                if(stringBuffer == null){ return null; }

                responseJsonString = stringBuffer.toString();
//                formatJson(responseJsonString);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(urlConnection != null) { urlConnection.disconnect();}
                if(bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return responseJsonString;
        }

        @Override
        protected void onPostExecute(String rawJson) {

            String ELEMENTS = "elements";

            try {
//                JSONArray jsonArray = new JSONArray(rawJson);
                JSONObject jsonObject = new JSONObject(rawJson);
                JSONArray elementsArray = jsonObject.getJSONArray(ELEMENTS);

                jsonObjects.clear();
                for (int i = 0; i < elementsArray.length() ; i++){
                    jsonObjects.add(elementsArray.getJSONObject(i));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.i(LOGTAG, "Size of elements array : " + jsonObjects.size());
            super.onPostExecute(rawJson);
        }
    }
}
