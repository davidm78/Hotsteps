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

import com.example.level4project.AccountCreation.SendAccountToServer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.EditText;

public class AccountLogin extends FragmentActivity {
	
	private static boolean loggedIn = false;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_view);
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
			
		Intent returnToMain = new Intent(this, MainActivity.class);
		startActivity(returnToMain);
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

	// Static class that acts as an AsyncTask to send information to the server. Takes in the name value pair created 
	// in postAccount() and sends it to the server, and will not crash the app if that fails.
	public static class SendLoginToServer extends AsyncTask<List<NameValuePair>, Void,  HttpResponse> {

		@Override
		protected HttpResponse doInBackground(List<NameValuePair>... arrayList) {

			HttpClient client = new DefaultHttpClient();
			List<NameValuePair> nameValuePairs = arrayList[0];
			System.out.println(nameValuePairs.toString());
			HttpPost post = new HttpPost("http://192.168.43.224/~David/projectscripts/login.php");

			try {

				post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = client.execute(post);

				BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				String line = "";
				while ((line = rd.readLine()) != null) {
					System.out.println(line);
					System.out.println(nameValuePairs.get(0));
					if (line.equals(nameValuePairs.get(0))) {
						loggedIn = true;
					}
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

	public void createAccountView(View view) {
		Intent goToCreateAccount = new Intent(this, AccountCreation.class);
		startActivity(goToCreateAccount);
	}

}
