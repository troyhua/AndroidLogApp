package cmu.troy.applogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;

import cmu.troy.myfirstapp.LogContent;
import cmu.troy.myfirstapp.R;
import cmu.troy.myfirstapp.R.id;
import cmu.troy.myfirstapp.R.layout;
import cmu.troy.myfirstapp.R.menu;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends FragmentActivity implements 
  GooglePlayServicesClient.ConnectionCallbacks,
  GooglePlayServicesClient.OnConnectionFailedListener{

	public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
	public final static int PERIOD = 2000;
	
	public LocationClient mLocationClient;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Tools.setAlarm(this, PERIOD);
		registBroadcastReceiver();
		TelephonyManager telephony = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
		telephony.listen(new MyPhoneStateListener(this),PhoneStateListener.LISTEN_CALL_STATE);
		mLocationClient = new LocationClient(this, this, this);
	}

	@Override
	protected void onStart(){
	  super.onStart();
	  mLocationClient.connect();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private void registBroadcastReceiver() {
	    final IntentFilter theFilter = new IntentFilter();
	    /** System Defined Broadcast */
	    theFilter.addAction(Intent.ACTION_SCREEN_ON);
	    theFilter.addAction(Intent.ACTION_SCREEN_OFF);

	    ScreenReceiver mScreenReceiver = new ScreenReceiver();
	    getApplicationContext().registerReceiver(mScreenReceiver, theFilter);
	}

	public void checkCurrentLocation(View view){
	  TextView mainView = (TextView) findViewById(R.id.main_view);
	  
	  int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
	  if (ConnectionResult.SUCCESS != resultCode){
	    mainView.setText("Failed to access Google Play Service");
	    return;
	  }
	  Location mCurrentLocation = mLocationClient.getLastLocation();
	  mainView.setText(Tools.getLocationString(mCurrentLocation));
	}
	
	public void goToAnalyzer(View view){
	  LogContent.refreshItem();
	  Intent intent = new Intent(this, cmu.troy.myfirstapp.ReportListActivity.class);
	  startActivity(intent);
	}
	
	public void seeDebug(View view){
		TextView mainView = (TextView) findViewById(R.id.main_view);
		StringBuilder sb = new StringBuilder();
		File file = Tools.getLogFile();
		if (file.exists()){
			sb.append("Log file exists! " + file.getAbsolutePath() + "\n");
		}else{
			sb.append("Log file does not exist in " + file.getAbsolutePath() + "\n");
		}
		file = Tools.getLastAppFile();
		if (file.exists()){
			sb.append("Last app file exits! " + file.getAbsolutePath() + "\n");
		}else{
			sb.append("Last app file does not exist in " + file.getAbsolutePath() + "\n");
		}
		mainView.setMovementMethod(new ScrollingMovementMethod());
		mainView.setText(sb.toString());
	}

	// This function is called by the button "check last app", used to show the content of last app
	public void seelast(View view) throws IOException {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(Tools.getLastAppFile()));
			StringBuilder sb = new StringBuilder();
			String ts = reader.readLine();
			while (ts != null){
				sb.append(ts + "\n");
				ts = reader.readLine();
			}
			TextView mainView = (TextView) findViewById(R.id.main_view);
			mainView.setMovementMethod(new ScrollingMovementMethod());
			mainView.setText(sb.toString());
		} finally {
			if (reader != null) 
				reader.close();
		}
	}
	
	// This function is called by the button "check log", used to show the content of the log, maximum 500 lines
	public void seelog(View view) throws IOException {
		int maxLine = 500;
		TextView mainView = (TextView) findViewById(R.id.main_view);
		mainView.setMovementMethod(new ScrollingMovementMethod());
		mainView.setText(Tools.getLastFewLines(Tools.getLogFile(), maxLine));
	}

	@Override
	protected void onStop(){
	  mLocationClient.disconnect();
	  super.onStop();
	}
	
	public void onDestroy() {
		super.onDestroy();
		android.os.Debug.stopMethodTracing();
	}


  @Override
  public void onConnectionFailed(ConnectionResult arg0) {
    // TODO Auto-generated method stub
    
  }


  @Override
  public void onConnected(Bundle arg0) {
    // TODO Auto-generated method stub
    
  }


  @Override
  public void onDisconnected() {
    // TODO Auto-generated method stub
    
  }

}
