package com.example.level4project;

import java.io.BufferedReader;
import java.io.IOException;
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

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class AccountCreation extends FragmentActivity {
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_form);
		getActionBar().setDisplayHomeAsUpEnabled(true);
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
	        NavUtils.navigateUpFromSameTask(this);
	        return true;
		
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	//Gets the content of the fields, assembles the name value pair
	//and starts executing the AsyncTask that uploads to the server.
	@SuppressWarnings("unchecked")
	public void postAccount(View view) {
		
		if (!isNetworkAvailable()) {
			Toast.makeText(this, "Can't create account. No internet connection.", Toast.LENGTH_SHORT).show();
			return;
		}
		
		boolean emailEmpty = false;
		boolean usernameEmpty = false;
		boolean passwordEmpty = false;
				
		//Get the contents of the firstName Field
		EditText firstName = (EditText) findViewById(R.id.first_name_field);
		String firstNameString  = firstName.getText().toString();
				
		//Get the contents of the Surname field
		EditText surnameView = (EditText) findViewById(R.id.surname_field);
		String surnameString  = surnameView.getText().toString();
		
		//Get the contents of the email Field
		EditText emailView = (EditText) findViewById(R.id.mail_field);
		String emailString  = emailView.getText().toString();
		if (emailString.isEmpty()) {
			emailEmpty = true;
		}
				
		//Get the contents of the userName field
		EditText usernameView = (EditText) findViewById(R.id.username_field);
		String usernameString  = usernameView.getText().toString();
		if (usernameString.isEmpty()) {
			usernameEmpty = true;
		}
		
		//Get the encrypted hash of the password field
		EditText passwordView = (EditText) findViewById(R.id.password_field);
		if (passwordView.getText().toString().isEmpty()) {
			passwordEmpty = true;
		}
		String passwordString  = encryptPassword(passwordView.getText().toString());
		
		//Get the encrypted hash of the confirmPassword field
		EditText confirmPasswordView = (EditText) findViewById(R.id.confirm_password_field);
		String confirmPasswordString  = encryptPassword(confirmPasswordView.getText().toString());
		
		//Get the encrypted hash of the device field
		EditText deviceView = (EditText) findViewById(R.id.device_field);
		String deviceString  = deviceView.getText().toString();
		
		//If any of these strings empty, return error and leave method
		if (emailEmpty || usernameEmpty || passwordEmpty) {
			Toast.makeText(this, "You haven't filled in required fields! Fill them in and try again", Toast.LENGTH_SHORT).show();
			return;
		}

		// Assemble request
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(6);
		nameValuePairs.add(new BasicNameValuePair("firstname", firstNameString));
		nameValuePairs.add(new BasicNameValuePair("surname", surnameString));
		nameValuePairs.add(new BasicNameValuePair("email", emailString));
		nameValuePairs.add(new BasicNameValuePair("username", usernameString));
		nameValuePairs.add(new BasicNameValuePair("password", passwordString));
		nameValuePairs.add(new BasicNameValuePair("device", deviceString));
		
		if (passwordString.equals(confirmPasswordString) == false) {
			//Not OK to make account
			Toast.makeText(this, "Passwords do not match! Try again!", Toast.LENGTH_SHORT).show();
		} else {
			//OK to create account
			SendAccountToServer accountSender = new SendAccountToServer();
			accountSender.execute(nameValuePairs);
		}
		
		Toast.makeText(this, "Account Created!", Toast.LENGTH_SHORT).show();

		Intent returnIntent = new Intent(this, PedometerActivity.class);
        startActivity(returnIntent);
	}
	
	//Encrypts the password string into an SHA-256 hash string.
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
	public class SendAccountToServer extends AsyncTask<List<NameValuePair>, Void,  HttpResponse> {

		@Override
		protected HttpResponse doInBackground(List<NameValuePair>... arrayList) {

			HttpClient client = new DefaultHttpClient();
			List<NameValuePair> nameValuePairs = arrayList[0];
			System.out.println(nameValuePairs.toString());
			HttpPost post = new HttpPost("http://tethys.dcs.gla.ac.uk/davidsteps/scripts/accountcreation.php");

			try {

				post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = client.execute(post);

				BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				String line = "";
				while ((line = rd.readLine()) != null) {
					System.out.println(line);
				}

				return response;

			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return null;

		}
		
		//Executed upon completion of the ASyncTask
		public void onPostExecute(HttpResponse httpresponse){
			handleCreation(httpresponse);
		}

	}
	
	//Handles the return to the MainActivity
	public void handleCreation(HttpResponse httpresponse){
		Intent returnIntent = new Intent(this, AccountLogin.class);
        startActivity(returnIntent);
		Toast.makeText(this, "Account Created!", Toast.LENGTH_SHORT).show();
	}
	
	//Helper function to determine availability of a network
	private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager 
              = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
