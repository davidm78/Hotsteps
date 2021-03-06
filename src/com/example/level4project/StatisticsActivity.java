package com.example.level4project;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.level4project.R.id;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class StatisticsActivity extends FragmentActivity {

	private String url = "http://tethys.dcs.gla.ac.uk/davidsteps/scripts/jsonscript.php";
	private String jsonResult;
	private ListView listView;
    SessionManager pedometerSession;
    private ProgressBar spinner;

    //On start of the activity, load UI and load in session data
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.statistics);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		listView = (ListView) findViewById(R.id.listView1);
        //textView = (TextView) findViewById(R.id.json_sentence);
    	pedometerSession = new SessionManager(getApplicationContext());
    	spinner = (ProgressBar) findViewById(R.id.progressBar1);
        spinner.setVisibility(View.VISIBLE);
		accessWebService();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.statistics_menu, menu);
		return true;
	}
	
	// Controls the selection of different options in the menu and handles them appropriately.
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			switch (item.getItemId()) {
			
			case android.R.id.home:
		        onBackPressed();
		        return true;
			
			case R.id.action_main_activity:
				onBackPressed();
				return true;
				
			case R.id.action_leaderboards:
				Intent leaderboardIntent = new Intent(this, LeaderboardActivity.class);
				startActivity(leaderboardIntent);
			
			case R.id.action_logout:
		        pedometerSession.logoutUser();
		            
		    default:
		        return super.onOptionsItemSelected(item);
			
			}
		}

	//AsyncTask to send POST request and receive JSON response containing user data.
	private class JsonReadTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String...params) {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(params[0]);
			
			pedometerSession.checkLoginMain();
			try {
				
				//Assemble the parameters of the request in a ArrayList of NameValuePairs
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
				nameValuePairs.add(new BasicNameValuePair("userID", pedometerSession.getUserDetails().get(0)));
				
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
				
				jsonResult = inputStreamToString(response.getEntity().getContent()).toString();
				Log.d("StatisticsActivity", "Recieved json result");
				System.out.println(jsonResult);
			}
			catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return jsonResult;
		}
		
		private StringBuilder inputStreamToString(InputStream is) {
			String rLine = "";
			StringBuilder answer = new StringBuilder();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));

			try {
				while ((rLine = rd.readLine()) != null) {
					answer.append(rLine);
				}
			}

			catch (IOException e) {
				// e.printStackTrace();
				Toast.makeText(getApplicationContext(),
						"Error..." + e.toString(), Toast.LENGTH_LONG).show();
			}
			return answer;
		}
		
		//execute on completion of asynctask
		@Override
		  protected void onPostExecute(String result) {
			createStatsList();
		    spinner.setVisibility(View.GONE);

		 }
	} //end async task
	
	 public void accessWebService() {
		  JsonReadTask task = new JsonReadTask();
		  // passes values for the urls string array
		  task.execute(new String[] { url });
	}

	 
	// build hash set for list view
	 public void createStatsList() {
	  List<Map<String, String>> recordList = new ArrayList<Map<String, String>>();
	 
	  try {
	   JSONObject jsonResponse = new JSONObject(jsonResult);
	   JSONArray jsonMainNode = jsonResponse.optJSONArray("person_info");
	 
	   for (int i = 0; i < jsonMainNode.length(); i++) {
	    JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
	    String usageDate = jsonChildNode.optString("UsageDate");
	    String steps = jsonChildNode.optString("totalsteps");
	    String outPut = "On " + usageDate + " you made " + steps + " steps!";
	    recordList.add(createRecord("people", outPut));
	   }
	  } catch (JSONException e) {
	   Toast.makeText(getApplicationContext(), "Error" + e.toString(),
	     Toast.LENGTH_SHORT).show();
	  }
	  

	 
	  SimpleAdapter simpleAdapter = new SimpleAdapter(this, recordList,
	    android.R.layout.simple_list_item_1,
	    new String[] { "people" }, new int[] { android.R.id.text1 });
	  listView.setAdapter(simpleAdapter);
	 }
	 
	 //create a new record to display in the stats listview
	 private HashMap<String, String> createRecord(String name, String number) {
	  HashMap<String, String> peopleidUser = new HashMap<String, String>();
	  peopleidUser.put(name, number);
	  return peopleidUser;
	 }
	 
}
