package cmu.troy.applogger;

import java.util.Date;

import org.json.JSONObject;

import cmu.troy.applogger.JSONKeys.JSONValues;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ScreenReceiver extends BroadcastReceiver {
	public ScreenReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			String strAction = intent.getAction();
			if (strAction.equals(Intent.ACTION_SCREEN_OFF)
					|| strAction.equals(Intent.ACTION_SCREEN_ON)) {
				if (strAction.equals(Intent.ACTION_SCREEN_OFF)){
				  JSONObject job = new JSONObject();
				  job.put(JSONKeys.id, String.valueOf((new Date()).getTime()));
				  job.put(JSONKeys.log_type, JSONValues.SCREEN_OFF);
				  Tools.logJsonNewBlock(job);
				}
				else{
				  JSONObject job = new JSONObject();
          job.put(JSONKeys.id, String.valueOf((new Date()).getTime()));
          job.put(JSONKeys.log_type, JSONValues.SCREEN_ON);
          Tools.logJsonNewBlock(job);
				}
			}
		} catch (Exception e) {
		}
	}
}
