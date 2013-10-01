package cmu.troy.applogger;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import cmu.troy.applogger.JSONKeys.JSONValues;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OutGoingCallReceiver extends BroadcastReceiver {
	public OutGoingCallReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
	  String phonenumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
	  JSONObject job = new JSONObject();
    try {
      job.put(JSONKeys.id, String.valueOf((new Date()).getTime()));
      job.put(JSONKeys.log_type, JSONValues.OUTGOING_CALL);
      job.put(JSONKeys.number, phonenumber);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    Tools.logJsonNewBlock(job);
	}
}