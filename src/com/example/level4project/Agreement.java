package com.example.level4project;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class Agreement extends FragmentActivity {
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_agreement);
		getActionBar().setDisplayHomeAsUpEnabled(true);
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
	
	//The user has agreed, move view to the Account Creation screen
	public void onAgree(View view) {
		Intent agreeIntent = new Intent(this, AccountCreation.class);
		startActivity(agreeIntent);
	}
	
	//The user has disagreed, move view back to the Login screen, displaying Toast explaining.
	public void onDisagree(View view) {
		Intent disagreeIntent = new Intent(this, AccountLogin.class);
		Toast.makeText(getApplicationContext(),"In order to use this application, you must agree to the terms of use.", Toast.LENGTH_LONG).show();
		startActivity(disagreeIntent);
	}

}
