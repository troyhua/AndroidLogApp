package cmu.troy.applogger;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import cmu.troy.applogger.JSONKeys.JSONValues;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class SmsReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    Bundle extras = intent.getExtras();
    Object[] pdus = (Object[]) extras.get("pdus");
    String origin = "";
    String body = "";
    for (Object pdu : pdus) {
      SmsMessage msg = SmsMessage.createFromPdu((byte[]) pdu);
      origin = msg.getOriginatingAddress();
      body = msg.getMessageBody();
    }
    JSONObject job = new JSONObject();
    try {
      job.put(JSONKeys.id, String.valueOf((new Date()).getTime()));

      if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
        job.put(JSONKeys.log_type, JSONValues.SMS_RECEIVED);
        job.put(JSONKeys.number, origin);
        // TODO log content of the SMS
        Tools.logJsonNewBlock(job);
        // SMS_SENT cannot be logged down
      } else if (intent.getAction().equals("android.provider.Telephony.SMS_SENT")) {
        job.put(JSONKeys.log_type, "SMS_sent");
        job.put(JSONKeys.number, origin);
        Tools.logJsonNewBlock(job);
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }
}