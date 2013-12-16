package com.example.level4project;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

public class AccountLogin extends FragmentActivity {
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_view);
	}
	
	public void logIn(View view) {
		Intent returnToMain = new Intent(this, MainActivity.class);
		startActivity(returnToMain);
	}

}
