package com.example.level4project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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

import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.level4project.StepCounter.StepListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;

public class MainActivity extends FragmentActivity implements
ActionBar.TabListener,
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener, 
StepListener{
	
	// Global constants
    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    public final static String EXTRA_MESSAGE = "com.example.level4project.MESSAGE";
    LocationClient mLocationClient;
    Location mCurrentLocation;
    ConnectionResult connectionResult = new ConnectionResult(0, null);
    ErrorDialogFragment errorFragment = new ErrorDialogFragment(); 
    public StepCounter stepCounter;
    private static TextView nameTextView;
    private static TextView stepTextView;
    public boolean isLoggedIn = false;
    SessionManager pedometerSession;
	private String jsonResult;
    boolean savedStepsAdded = false;
	Timer timer;
    int timesExecuted = 0;
    private Menu optionsMenu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
    	pedometerSession = new SessionManager(getApplicationContext());
    	pedometerSession.checkLogin();
		setContentView(R.layout.activity_main);
		
		 /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
		mLocationClient = new LocationClient(this, this, this);
		servicesConnected(); 
		
        
		stepCounter = new StepCounter(getApplicationContext());
		SensorManager mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		boolean mIsSensoring;
        
		mIsSensoring = mSensorManager.registerListener(stepCounter.getListener(),
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_FASTEST);
		
        //if (mIsSensoring == true) {
        	//System.out.println(stepCounter.getSteps());
        //}
		
        stepTextView = (TextView) findViewById(R.id.steps_total);        
    	                
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		this.optionsMenu = menu;
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_menu, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	// Controls the selection of different options in the menu and handles them appropriately.
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		
		case R.id.main_refresh:
	        postData(getCurrentFocus());
	    return true;
        	
        case R.id.action_statistics:
        	Intent intent3 = new Intent(this, StatisticsActivity.class);
        	startActivity(intent3);
        	return true;
        	
        case R.id.action_leaderboards:
			Intent leaderboardIntent = new Intent(this, LeaderboardActivity.class);
			startActivity(leaderboardIntent);
			return true;
        	
        case R.id.action_logout:
        	timer.cancel();
        	pedometerSession.logoutUser();
            
        default:
            return super.onOptionsItemSelected(item);
		}
	}
	
	public class UpdatePedometer extends AsyncTask <StepCounter, Void, String> {

		@Override
		protected String doInBackground(StepCounter...stepCounter) {
			
			StepCounter sc = stepCounter[0];
			Integer currentStepValue = sc.getSteps();
			System.out.println(currentStepValue);
			sc.setSteps(); //REMOVE AT SOME POINT!!!!!!
			mCurrentLocation = mLocationClient.getLastLocation();
			Double latitude = mCurrentLocation.getLatitude();
			Double longitude = mCurrentLocation.getLongitude();
			
			pedometerSession.checkLogin();
			
			//Assemble the parameters of the request in a ArrayList of NameValuePairs
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
			nameValuePairs.add(new BasicNameValuePair("userID", pedometerSession.getUserDetails().get(0)));
			nameValuePairs.add(new BasicNameValuePair("nosteps", currentStepValue.toString()));
			nameValuePairs.add(new BasicNameValuePair("latitude", latitude.toString()));
			nameValuePairs.add(new BasicNameValuePair("longitude", longitude.toString()));
			
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://tethys.dcs.gla.ac.uk/davidsteps/scripts/updatedb.php");
			
			try {

				post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = client.execute(post);
				jsonResult = inputStreamToString(response.getEntity().getContent()).toString();

			} catch (ClientProtocolException e) {
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
		
		//Sends result to handleJson to be handled
		protected void onPostExecute(String result) {
			handleJson(result);
	        setRefreshActionButtonState(false);
		}
	}
	
	public void callAsyncTask() {
		timer = new Timer();
		final Handler handler = new Handler();
		TimerTask doAsyncTask = new TimerTask() {
			@Override
			public void run() {
				handler.post(new Runnable() {
					public void run() {
						try {
							postData(getCurrentFocus());
						} catch (Exception e) {
							System.out.println("Problem with timerUpdate");
						}
					}
				});
			}
		};
		timer.schedule(doAsyncTask, 3000, 6000);
		System.out.println("Timer started!");
	}
	
	/** Called when the user clicks the Send button */
	public void postData(View view) {
		
		if (!isNetworkAvailable()) {
			Toast.makeText(this, "Can't sync with server. No Internet Connection", Toast.LENGTH_SHORT).show();
		}
		
		if (isNetworkAvailable()) {
			UpdatePedometer up = new UpdatePedometer();
			setRefreshActionButtonState(true);
			//spinner.setVisibility(View.VISIBLE);
			up.execute(stepCounter);
		}
        
	}
	
	/** Called when user clicks the "View recent step counts button */
	public void goToStepCounts(View view) {

		Intent stepIntent = new Intent(this, StatisticsActivity.class);
    	startActivity(stepIntent);
	}
	
	/** Called when user clicks the "View leaderboards button */
	public void goToLeaderboards(View view) {

		Intent leaderboardIntent = new Intent(this, LeaderboardActivity.class);
		startActivity(leaderboardIntent);
	}
	
	public void handleJson(String jsonString) {
				
		try {
			
			System.out.println(jsonString);
									
			JSONObject jsonResponse = new JSONObject(jsonString);
			JSONArray jsonNode = jsonResponse.optJSONArray("step_info");
			
			JSONObject jsonChildNode = jsonNode.getJSONObject(0);
			int steps = jsonChildNode.optInt("steps");
						
			String updateString = "" + steps;
			stepTextView.setText(updateString);
			
		}
		
		catch (JSONException e) {
			System.out.println("Problem parsing recieved JSON");
		}
	}
	
	// Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;
        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }
        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }
        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
		public void show(FragmentManager supportFragmentManager, String string) {
			// TODO Auto-generated method stub
			
		}
    }
    
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST :
            /*
             * If the result code is Activity.RESULT_OK, try
             * to connect again
             */
                switch (resultCode) {
                    case Activity.RESULT_OK :
                    /*
                     * Try the request again
                     */
                    break;
                }
        }
     }
    
    private boolean servicesConnected() {
    	
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates",
                    "Google Play services is available.");
            // Continue
            return true;
            
        // Google Play services was not available for some reason
        } else {
            // Get the error code
            int errorCode = connectionResult.getErrorCode();
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    errorCode,
                    this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
   
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(errorDialog);
                // Show the error dialog in the DialogFragment
                //errorFragment.show(getSupportFragmentManager(), "Location Updates");
            }
        }
		return false;
    }
    
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager 
              = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    
    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        // Display the connection status
        Toast.makeText(this, "Connected to location services", Toast.LENGTH_SHORT).show();
        mCurrentLocation = mLocationClient.getLastLocation();
        System.out.println(mCurrentLocation.toString());
        UpdatePedometer up = new UpdatePedometer();
        setRefreshActionButtonState(true);
        up.execute(stepCounter);
    }
    
    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {
        // Display the connection status
        Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
    }
    
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();        }
    }
    
    /*
     * Called when the Activity becomes visible.
     */
    @Override
    protected void onStart() {
    	      
      super.onStart();
      // Connect the client.
      if (!mLocationClient.isConnected()) {
    	  mLocationClient.connect();
      }
      if (savedStepsAdded == false) {
    	  stepCounter.addSavedSteps();
    	  savedStepsAdded = true;
      }
      if (timesExecuted == 0 && pedometerSession.isLoggedIn()) { 
    	  callAsyncTask();
      }
      timesExecuted++;
    }
    
    /*
     * Called when the Activity is no longer visible.
     */
    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        //mLocationClient.disconnect();
        super.onStop();

    }

	@Override
	public void newSteps(int step) {
		System.out.println("New step!");
	}
	
	public void setRefreshActionButtonState(final boolean refreshing) {
	    if (optionsMenu != null) {
	        final MenuItem refreshItem = optionsMenu.findItem(R.id.main_refresh);
	        if (refreshItem != null) {
	            if (refreshing) {
	                refreshItem.setActionView(R.layout.actionbar_inprogress);
	            } else {
	                refreshItem.setActionView(null);
	            }
	        }
	    }
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

}