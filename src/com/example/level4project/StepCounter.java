package com.example.level4project;

import java.util.ArrayList;
import java.util.Arrays;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

public class StepCounter {
	private static final String TAG = "StepCounter";
	private static final String DBNAME = "PTB2_STEPS";
	private static final int INSERT_INTERVAL = 1000;	// milliseconds

	//private Database mDB;
	
	public interface StepListener {
		public void newSteps(int step);
	}
	
	private SharedPreferences mPrefs;
	
	SessionManager pedometerSession;

	private StepListener mListener = null;
	private int mCount = 0;

	private long mLastStepTime;
	private long mStepsLastInsert = 0;
	private long mSteps = 0;

//	private long mCurrentSecond = 0;
//	private float mAccumulatedSamples = 0;
//	private int mNumSamples;

	private long mLastInsertTime = 0;

	class TimeValue<T> {
		long time;
		T value;
		TimeValue(long time, T value) { this.time = time; this.value = value; }
	}
		
	public StepCounter(Context applicationContext) {
		//mPrefs = prefs;
		
		pedometerSession = new SessionManager(applicationContext);
		//mSteps = getSteps();
		//mSteps = mPrefs.getInt("steps",0);
		//mStepsLastInsert = mPrefs.getInt("stepslastinsert", 0);

		//mDB = new Database(context, DBNAME, null, 3);
		
		mLastStepTime = System.currentTimeMillis();
	}

	//private Cursor getDataCursor(long start, long end) {
		//Cursor c = mDB.select(start, end);
		//return c;
	//}
	
	//public Cursor getAccDataCursor(long start, long end) {
		//return getDataCursor(start, end);
	//}

	// used to visualize samples.
	public synchronized void getData(ArrayList<TimeValue<Float>> output) {
		for (int i=Math.max(0,mSamples.size()-4096);i<mSamples.size();i++) {
			output.add(mSamples.get(i));
		}
	}

	private float mLastMax = 0;
	private float mLastMin = 0;
	private float mLastDiff = 0;
	private float mLastLength = 0;
	private long mLastTimeIncrease = 0;
	private long mLastTimeDecrease = 0;
	private long mLastTimeMaxima = 0;
	
	private ArrayList<TimeValue< Float >> mSamples = new ArrayList<TimeValue< Float >>();
	
	private int mLengthsIndex = 0;
	private float[] mLengths = new float[8];

	public synchronized void addAcc(float movement, float x, float y, float z) {
		long time = System.currentTimeMillis();

		// calc length and take median of last 8 sample lengths.
		float length = (float)Math.sqrt(x*x+y*y+z*z);
		mLengths[mLengthsIndex%mLengths.length] = length;
		mLengthsIndex++; mLengthsIndex %= mLengths.length;
		// calc mean
		float[] sorted = Arrays.copyOf(mLengths, mLengths.length);
		Arrays.sort(sorted);
		length = sorted[mLengths.length/2];

		final float STEP_THRESHOLD = 5f;
		float diff = length-mLastLength;
		float sample = length;
		if (diff<0.1&&diff>-0.1) {
			
		} else if (diff<0 && mLastDiff<0) {	// continue to decrease
			if (length<mLastMin) mLastMin = length;
			mLastTimeDecrease = time;
			sample = length;
		} else if (diff>0 && mLastDiff>0) {	// continue increase
			if (length>mLastMax) mLastMax = length;
			mLastTimeIncrease = time;
		} else if (diff<0) {	// we have reached a local maxima... (means mLastMax>0)
			// check validity of step
			if (mLastMax-mLastMin > STEP_THRESHOLD && time-mLastTimeMaxima>(1000/10)) { //mLastTimeIncrease-mLastTimeDecrease>(1000/100)) {
				mSteps++;
				
				mLastStepTime = time;
				if (mListener!=null) mListener.newSteps(1);
			}
			mLastTimeMaxima = time;
		} else {	// we have reached a local minima
			mLastMax = length;
			mLastMin = length;
		}
		mLastDiff = diff;
		mLastLength = length;

//		mSamples.add(new TimeValue<Float>(time,sample));

		data(time, movement);
		
		mCount++;
	}

	public synchronized void deleteAll() {
		//mDB.deleteAll();
//		mValues.clear();
	}

	public void setListener(StepListener listener) {
		mListener = listener;
	}

	public int getCounter() {
		return mCount;
	}

	private void data(long time, float acc) {
//		if (time-mCurrentSecond>1000L) {
//			if (mNumSamples>0) {
//				float v = mAccumulatedSamples / mNumSamples;
////				if (v>SEDENTARY_THRESHOLD)
////					mSteps += 1+(v-SEDENTARY_THRESHOLD)*STEP_FACTOR;
//			}
//			mCurrentSecond = time-time%1000;
//			mNumSamples = 0;
//			mAccumulatedSamples = 0;
//		}
//		mAccumulatedSamples += acc;
//		mNumSamples ++;

		maybeInsert(time);
	}

	private void maybeInsert(long time) {
		if (time-mLastInsertTime>INSERT_INTERVAL) {
			long steps = mSteps-mStepsLastInsert;
			Log.d(TAG, "Inserting steps... " + steps);
			//if (steps>0)
				//mDB.insert(time, (int)steps);
			pedometerSession.updateSteps((int) mSteps);
			mLastInsertTime = time;
			mStepsLastInsert = mSteps;
			
			//Editor edit = mPrefs.edit();
			//edit.putInt("steps", (int)mSteps);
			//edit.putInt("stepslastinsert", (int)mStepsLastInsert);
			//edit.apply();
		}
	}

	public synchronized int getSteps() {
		return (int)mSteps;
		//String stepString = pedometerSession.getUserDetails().get(3);
		//return Integer.parseInt(stepString);
	}
	
	public synchronized void setSteps() {
		mSteps = 0;
		//pedometerSession.updateSteps(0);
	}
	
	public synchronized void setmSteps() {
		mSteps = 0;
	}
	
	public void addSavedSteps() {
		String stepString = pedometerSession.getUserDetails().get(3);
		int noSteps = Integer.parseInt(stepString);
		mSteps = mSteps + noSteps;
		pedometerSession.updateSteps(0);
	}
	
	public long getTimeSinceLastStep() {
		return System.currentTimeMillis()-mLastStepTime;
	}
	
	public SensorEventListener getListener() {
		return mSensorListener;
	}

    private SensorEventListener mSensorListener = new SensorEventListener() {
    	float[] last = new float[3];
    	float[] tmp = new float[3];
    	float movement = 0;
    	long lasttime = 0;

		@Override
		public void onSensorChanged(SensorEvent event) {
			final float filter = 0.0f; //0.05f;
//			if (lasttime==0) {
			if (event.timestamp-lasttime>1000000000L) {
				last[0] = event.values[0];
				last[1] = event.values[1];
				last[2] = event.values[2];
				movement = 0;
				Log.d(TAG, "Restarting acc smoothing");
			} else {
				tmp[0] = last[0]*filter+event.values[0]*(1-filter);
				tmp[1] = last[1]*filter+event.values[1]*(1-filter);
				tmp[2] = last[2]*filter+event.values[2]*(1-filter);
				movement = movement*0.2f + 0.8f*(Math.abs(last[0]-tmp[0]) + Math.abs(last[1]-tmp[1]) + Math.abs(last[2]-tmp[2]));
				addAcc( movement, tmp[0], tmp[1], tmp[2] );
			}
			last[0] = tmp[0];
			last[1] = tmp[1];
			last[2] = tmp[2];
			lasttime = event.timestamp;
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};

}
