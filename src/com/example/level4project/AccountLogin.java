package com.example.level4project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

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

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class AccountLogin extends FragmentActivity {
	
	private String jsonResult;
	SessionManager pedometerSession;

	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_view);
		pedometerSession = new SessionManager(getApplicationContext());
	}
	
	@SuppressWarnings("unchecked")
	public void logIn(View view) {
		
		//Get the contents of the userName field
		EditText usernameView = (EditText) findViewById(R.id.username_field);
		String usernameString  = usernameView.getText().toString();
				
		//Get the encrypted hash of the password field
		EditText passwordView = (EditText) findViewById(R.id.password_field);
		String passwordString  = encryptPassword(passwordView.getText().toString());
		
		// Assemble request
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("username", usernameString));
		nameValuePairs.add(new BasicNameValuePair("password", passwordString));
		
		SendLoginToServer accountSender = new SendLoginToServer();
		accountSender.execute(nameValuePairs);
			
	}
	
	public void testLogin(View view) {
		Intent returnToMain = new Intent(this, MainActivity.class);
		startActivity(returnToMain);
	}
	
	//Encrypts the password string into an MD5 hash string.
	public static String encryptPassword(String password) {
		String encrypted = "";
	    try {
	        MessageDigest digest = MessageDigest.getInstance("SHA-256");
	        digest.update(password.getBytes());
	        byte[] passwordBytes = digest.digest();
	        
	        StringBuffer hexString = new StringBuffer();
	        for (int i=0; i < passwordBytes.length; i++) {
	            hexString.append(Integer.toString((passwordBytes[i] & 0xFF) + 0x100, 16).substring(1));
	        }
	        
	        encrypted = hexString.toString();
	    }
	    catch(Exception e){}
	    return encrypted;
	}

	// Static class that acts as an AsyncTask to send information to the server. Takes in the name value pair created 
	// in postAccount() and sends it to the server, and will not crash the app if that fails.
	public class SendLoginToServer extends AsyncTask<List<NameValuePair>, Void,  String> {

		@Override
		protected String doInBackground(List<NameValuePair>... arrayList) {

			HttpClient client = new DefaultHttpClient();
			List<NameValuePair> nameValuePairs = arrayList[0];
			HttpPost post = new HttpPost("http://tethys.dcs.gla.ac.uk/davidsteps/scripts/replacementlogin.php");

			try {

				post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = client.execute(post);
				jsonResult = inputStreamToString(response.getEntity().getContent()).toString();

			} catch (ClientProtocolException e) {
				e.printStackTrace();
				Toast.makeText(getApplicationContext(),"Error authenticating with server", Toast.LENGTH_LONG).show();
			} catch (IOException e) {
				e.printStackTrace();
				Toast.makeText(getApplicationContext(),"Error authenticating with server", Toast.LENGTH_LONG).show();
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
		
		protected void onPostExecute(String result) {
			handleLogin(result);
		}
	
	}
	
	public void handleLogin(String jsonString) {

		try {

			JSONObject jsonResponse = new JSONObject(jsonString);
			JSONArray jsonNode = jsonResponse.optJSONArray("login_info");

			JSONObject jsonChildNode = jsonNode.getJSONObject(0);
			String userID = jsonChildNode.optString("userID");
			String firstName = jsonChildNode.optString("FirstName");
			String username = jsonChildNode.optString("UserName");

			System.out.println(userID);
			System.out.println(firstName);
			System.out.println(username);
			
			pedometerSession.createLoginSession(userID, firstName, username);
			
			//System.out.print(pedometerSession.isLoggedIn());
			Intent goToMain = new Intent(this, MainActivity.class);
			startActivity(goToMain);
		}
		
		catch (JSONException e ) {
			System.out.println("Problem parsing recieved JSON");
			Toast.makeText(getApplicationContext(),"Error with provided Login details", Toast.LENGTH_LONG).show();
		}
		
	}

	public void createAccountView(View view) {
		Intent goToCreateAccount = new Intent(this, AccountCreation.class);
		startActivity(goToCreateAccount);
	}

}
