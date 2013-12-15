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

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class AccountCreation extends Activity {
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_form);
	}
	
	//Gets the content of the fields, assembles the name value pair
	//and starts executing the AsyncTask that uploads to the server.
	@SuppressWarnings("unchecked")
	public void postAccount(View view) {
				
		//Get the contents of the firstName Field
		EditText firstName = (EditText) findViewById(R.id.first_name_field);
		String firstNameString  = firstName.getText().toString();
				
		//Get the contents of the Surname field
		EditText surnameView = (EditText) findViewById(R.id.surname_field);
		String surnameString  = surnameView.getText().toString();
		
		//Get the contents of the email Field
		EditText emailView = (EditText) findViewById(R.id.mail_field);
		String emailString  = emailView.getText().toString();
				
		//Get the contents of the userName field
		EditText usernameView = (EditText) findViewById(R.id.username_field);
		String usernameString  = usernameView.getText().toString();
		
		//Get the encrypted hash of the password field
		EditText passwordView = (EditText) findViewById(R.id.password_field);
		String passwordString  = encryptPassword(passwordView.getText().toString());
		
		//Get the encrypted hash of the confirmPassword field
		EditText confirmPasswordView = (EditText) findViewById(R.id.confirm_password_field);
		String confirmPasswordString  = encryptPassword(confirmPasswordView.getText().toString());
		
		System.out.println(passwordString);
		System.out.println(confirmPasswordString);
		
		//Get the encrypted hash of the confirmPassword field
		EditText deviceView = (EditText) findViewById(R.id.device_field);
		String deviceString  = deviceView.getText().toString();

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

		Intent returnIntent = new Intent(this, MainActivity.class);
        startActivity(returnIntent);
	}
	
	//Encrypts the password string into an MD5 hash string.
	public static String encryptPassword(String password) {
	    String encrypted = "";
	    try {
	        MessageDigest digest = MessageDigest.getInstance("MD5"); 
	        byte[] passwordBytes = password.getBytes(); 

	        digest.reset();
	        digest.update(passwordBytes);
	        byte[] message = digest.digest();

	        StringBuffer hexString = new StringBuffer();
	        for (int i=0; i < message.length; i++) {
	            hexString.append(Integer.toHexString(0xFF & message[i]));
	        }
	        encrypted = hexString.toString();
	    }
	    catch(Exception e){}
	    return encrypted; 
	}
	
	public static class SendAccountToServer extends AsyncTask<List<NameValuePair>, Void,  HttpResponse> {

		@Override
		protected HttpResponse doInBackground(List<NameValuePair>... arrayList) {

			HttpClient client = new DefaultHttpClient();
			List<NameValuePair> nameValuePairs = arrayList[0];
			System.out.println(nameValuePairs.toString());
			HttpPost post = new HttpPost("http://192.168.43.224/~David/projectscripts/accountcreation.php");

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
	}
	
	public void onPostExecute(HttpResponse httpresponse){
		Toast.makeText(this, "Sent!", Toast.LENGTH_SHORT).show();
	}
}
