package cmu.troy.applogger;

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
		if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
			Tools.logNewBlock("Received message from " + origin + " with content: " + body);

		}else  if(intent.getAction().equals("android.provider.Telephony.SMS_SENT")){
			Tools.logNewBlock("Sent message to " + origin + " with content: " + body);
		}
	}
}