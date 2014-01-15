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

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class LeaderboardActivity extends FragmentActivity {

	private String url = "http://192.168.43.224/~David/projectscripts/jsonscript.php";
	private String jsonResult;
	private ListView listView;
    private static TextView textView;
    SessionManager pedometerSession;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.statistics);
		listView = (ListView) findViewById(R.id.listView1);
        //textView = (TextView) findViewById(R.id.json_sentence);
    	pedometerSession = new SessionManager(getApplicationContext());
		accessWebService();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	private class JsonReadTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String...params) {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(params[0]);
			
			pedometerSession.checkLogin();
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
		
		@Override
		  protected void onPostExecute(String result) {
			ListDrwaer();
		 }
	} //end async task
	
	 public void accessWebService() {
		  JsonReadTask task = new JsonReadTask();
		  // passes values for the urls string array
		  task.execute(new String[] { url });
	}

	 
	// build hash set for list view
	 public void ListDrwaer() {
	  List<Map<String, String>> peopleList = new ArrayList<Map<String, String>>();
	 
	  try {
	   JSONObject jsonResponse = new JSONObject(jsonResult);
	   JSONArray jsonMainNode = jsonResponse.optJSONArray("person_info");
	 
	   for (int i = 0; i < jsonMainNode.length(); i++) {
	    JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
	    String usageDate = jsonChildNode.optString("UsageDate");
	    String steps = jsonChildNode.optString("steps");
	    String outPut = "On " + usageDate + " you made " + steps + " steps!";
	    peopleList.add(createEmployee("people", outPut));
	   }
	  } catch (JSONException e) {
	   Toast.makeText(getApplicationContext(), "Error" + e.toString(),
	     Toast.LENGTH_SHORT).show();
	  }
	  

	 
	  SimpleAdapter simpleAdapter = new SimpleAdapter(this, peopleList,
	    android.R.layout.simple_list_item_1,
	    new String[] { "people" }, new int[] { android.R.id.text1 });
	  listView.setAdapter(simpleAdapter);
	 }
	 
	 private HashMap<String, String> createEmployee(String name, String number) {
	  HashMap<String, String> peopleidUser = new HashMap<String, String>();
	  peopleidUser.put(name, number);
	  return peopleidUser;
	 }
	 
}