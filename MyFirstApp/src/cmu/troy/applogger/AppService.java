/***
  Copyright (c) 2008-2012 CommonsWare, LLC
  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
  by applicable law or agreed to in writing, software distributed under the
  License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
  OF ANY KIND, either express or implied. See the License for the specific
  language governing permissions and limitations under the License.
  
  From _The Busy Coder's Guide to Advanced Android Development_
    http://commonsware.com/AdvAndroid
*/

package cmu.troy.applogger;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class AppService extends WakefulIntentService {
	public AppService() {
		super("AppService");
	}

	private List<String> getLastApps() throws IOException{
		File file = Tools.getLastAppFile();
		ArrayList<String> res = new ArrayList<String>();
		if (!file.exists()){
		  Tools.saveCreateFile(file);
			return res;
		}
		
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String ts = reader.readLine(); // the first line is the date
		ts = reader.readLine();
		while (ts != null){
			res.add(ts);
			ts = reader.readLine();
		}
		reader.close();
		return res;
	}
	
	private List<String> getCurrentApps(){
		Context context = this.getApplicationContext();
	    ActivityManager mgr = (ActivityManager)context.getSystemService(ACTIVITY_SERVICE);
	    List<RunningTaskInfo> tasks = mgr.getRunningTasks(100);
	    ArrayList<String> res = new ArrayList<String>();
	    for(Iterator<RunningTaskInfo> i = tasks.iterator(); i.hasNext(); )
	    {
	        RunningTaskInfo p = (RunningTaskInfo)i.next();
	        res.add(p.baseActivity.flattenToString());
	    }
	    return res;
	}
	
	private List<String> getNewApps(List<String> oldApps, List<String> currentApps){
		if (oldApps.size() == currentApps.size()){
			for (int i = 0; i < oldApps.size(); i++)
				if (!oldApps.get(i).equals(currentApps.get(i)))
					return currentApps;
			return new ArrayList<String>();
		}else
			return currentApps;
	}
	
	private void logApps(List<String> newApps, List<String> currentApps) throws IOException{
		/* Empty newApps means current apps are the same with the last apps.
		 * So there is no need to update last apps file or log file.
		 */
		if (newApps == null  || newApps.size() == 0)
			return;
		
		/* Log currentApps to last apps file */
		File file = Tools.getLastAppFile();
		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file.getAbsolutePath(), false)));
		Date now = new Date();
		writer.println(now.toString());
		for (String s : currentApps)
			writer.println(s);
		writer.close();
		
		/* Append new Apps into log file */
		file = Tools.getLogFile();
		Tools.saveCreateFile(file);
		writer = new PrintWriter(new BufferedWriter(new FileWriter(file.getAbsoluteFile(), true)));
		writer.println(Tools.STAR_SPLIT);
		writer.println(now.toString());
		for (String s : newApps){
			writer.println(s);
		}
		writer.close();
	}
	
	private boolean checkExternalStorageAvailable(){
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    mExternalStorageAvailable = true;
		    mExternalStorageWriteable = false;
		} else {
		    mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
		if ( !(mExternalStorageAvailable && mExternalStorageWriteable) ){
			return false;
		} else
			return true;
	}
	
	@Override
	protected void doWakefulWork(Intent intent) {
		try{
			if (checkExternalStorageAvailable()){
				List<String> lastApps = getLastApps();
				List<String> currentApps = getCurrentApps();
				List<String> newApps = getNewApps(lastApps, currentApps);
				logApps(newApps, currentApps);
			} else 
				Tools.toastFromHandler(handler, "Cannot access external storage!", getApplicationContext());
		}
		catch (Exception e) {
	    	Tools.toastFromHandler(handler, e.toString(), getApplicationContext());
	    }
		
    }
}