package cmu.troy.applogger;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import cmu.troy.applogger.JSONKeys.JSONValues;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class MyPhoneStateListener extends PhoneStateListener {
  private boolean isPhoneCalling = false;
  private Context mContext;
  
  public MyPhoneStateListener(Context context){
    super();
    mContext = context;
  }
  
  @Override
  public void onCallStateChanged(int state, String incomingNumber) {

    if (TelephonyManager.CALL_STATE_RINGING == state) {
      // phone ringing
      JSONObject job = new JSONObject();
      try {
        job.put(JSONKeys.id, String.valueOf((new Date()).getTime()));
        job.put(JSONKeys.log_type, JSONValues.INCOMING_CALL);
        job.put(JSONKeys.number, incomingNumber);
        Tools.logJsonNewBlock(job);
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }

    if (TelephonyManager.CALL_STATE_OFFHOOK == state) {
      // active
      JSONObject job = new JSONObject();
      try{
        job.put(JSONKeys.id, String.valueOf((new Date()).getTime()));
        job.put(JSONKeys.log_type, JSONValues.CALL_BEGIN);
      } catch (JSONException e){
        e.printStackTrace();
      }
      Tools.logJsonNewBlock(job);
    }

    if (TelephonyManager.CALL_STATE_IDLE == state) {
      if (isPhoneCalling) {
        JSONObject job = new JSONObject();
        try{
          job.put(JSONKeys.id,  String.valueOf((new Date()).getTime()));
          job.put(JSONKeys.log_type, JSONValues.CALL_END);
        } catch (JSONException e){
          e.printStackTrace();
        }
        Tools.logJsonNewBlock(job);
        isPhoneCalling = false;
      }
    }
  }
}