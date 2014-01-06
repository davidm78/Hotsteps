package com.example.level4project;

import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SessionManager {
	
	//Application shared preferences
	SharedPreferences preferences;
	
	//Editor to edit Shared Preferences
	Editor prefEditor;
	
	//Context
	Context _context;
	
	//Shared Preferences mode
	int PRIVATE_MODE = 0;
	
	//Shared Preferences file name
	private static final String PREF_NAME = "PedometerSharedPreferences";
	
	//Shared Preferences keys
	private static final String IS_LOGIN = "IsLoggedIn";
	
	//Username (access to variable from outside class)
	private static final String KEY_NAME = "name";
	
	//email (access to variable from outside class)
	private static final String KEY_EMAIL = "email";
	
	//Constructor
	public SessionManager(Context context) {
		this ._context = context;
		preferences = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
		prefEditor = preferences.edit();
	}
	
	/*
	 * Creates a login session for the pedometer app.
	 */
	public void createLoginSession(String name, String email) {
		
		prefEditor.putBoolean(IS_LOGIN, true);
		prefEditor.putString(KEY_NAME, name);
		prefEditor.putString(KEY_EMAIL, email);
		prefEditor.commit();
	}
	
	/*
	 * Get stored session data from Shared Preferences.
	 */
	public HashMap<String, String> getUserDetails() {
		
		HashMap<String, String> user = new HashMap <String, String>();
		user.put(KEY_NAME, preferences.getString(KEY_NAME, null));
		user.put(KEY_EMAIL, preferences.getString(KEY_EMAIL, null));
		return user;
		
	}
	
	/*
	 * Check if user logged in, return to login view if not
	 */
	public void checkLogin() {
		
		//Check if logged in
		if (!this.isLoggedIn()) {
			
			//Return to login view
			Intent intent = new Intent(_context, AccountLogin.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			_context.startActivity(intent);
			
		}
	}
	
	/*
	 * Clear session details in Shared Preferences
	 */
	public void logoutUser() {
		
		//Clear all data from SharedPreferences
		prefEditor.clear();
		prefEditor.commit();
		
		//Return to login view
		Intent intent = new Intent(_context, AccountLogin.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		_context.startActivity(intent);
	}
	
	public boolean isLoggedIn() {
		return preferences.getBoolean(IS_LOGIN, false);
	}
	
	

}