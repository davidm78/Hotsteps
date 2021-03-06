package com.example.level4project;

import java.util.ArrayList;

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
	 	
	private static final String KEY_ID = "userID";
	
	//username (access to variable from outside class)
	private static final String KEY_USERNAME = "username";
	
	//firstname (access to variable from outside class)
	private static final String KEY_FIRSTNAME = "firstname";
	
	private static final String KEY_STEPS = "steps";
	
		
	//Constructor
	public SessionManager(Context context) {
		this ._context = context;
		preferences = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
		prefEditor = preferences.edit();
	}
	
	/*
	 * Creates a login session for the pedometer app.
	 */
	public void createLoginSession(String userId, String firstName, String userName) {
		
		prefEditor.putBoolean(IS_LOGIN, true);
		prefEditor.putString(KEY_ID, userId);
		prefEditor.putString(KEY_FIRSTNAME, firstName);
		prefEditor.putString(KEY_USERNAME, userName);
		prefEditor.putInt(KEY_STEPS, 0);
		prefEditor.commit();
	}
	
	/*
	 * Get stored session data from Shared Preferences.
	 */
	public ArrayList<String> getUserDetails() {
		
		ArrayList<String> user = new ArrayList<String>();
		user.add(preferences.getString(KEY_ID, null));
		user.add(preferences.getString(KEY_FIRSTNAME, null));
		user.add(preferences.getString(KEY_USERNAME, null));
		Integer noSteps = preferences.getInt("steps", 0);
		user.add(noSteps.toString());

		return user;
		
	}
	
	public void updateSteps (int noSteps) {
		prefEditor.putInt("steps", noSteps);
		prefEditor.commit();
	}
	
	
	/*
	 * Check if user logged in, return to login view if not
	 */
	public void checkLoginLogin() {
		
		//Check if logged in
		if (this.isLoggedIn()) {
			
			//Return to login view
			Intent intent = new Intent(_context, PedometerActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			_context.startActivity(intent);
			
		}
	}
	
	/*
	 * Check if user logged in, return to login view if not
	 */
	public void checkLoginMain() {
		
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
