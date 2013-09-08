package cmu.troy.applogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.List;

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
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {

	public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
	public final static int PERIOD = 1000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Tools.setAlarm(this, PERIOD);
		registBroadcastReceiver();
		TelephonyManager telephony = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
		telephony.listen(new MyPhoneStateListener(),PhoneStateListener.LISTEN_CALL_STATE);
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

	// The function called by the button "send", used to show some debug information
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

	public void onDestroy() {
		super.onDestroy();
		android.os.Debug.stopMethodTracing();
	}

}
