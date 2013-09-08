package cmu.troy.applogger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class MyPhoneStateListener extends PhoneStateListener {
	private boolean isPhoneCalling = false;

	@Override
	public void onCallStateChanged(int state, String incomingNumber) {

		if (TelephonyManager.CALL_STATE_RINGING == state) {
			// phone ringing
			Tools.logNewBlock("Receive incoming call, ringing, number: " + incomingNumber);
		}

		if (TelephonyManager.CALL_STATE_OFFHOOK == state) {
			// active
			isPhoneCalling = true;
			Tools.logNewBlock("Begin the phone call");
		}

		if (TelephonyManager.CALL_STATE_IDLE == state) {
          // run when class initial and phone call ended, need detect flag
          // from CALL_STATE_OFFHOOK

          if (isPhoneCalling) {

        	  Tools.logNewBlock("Call ended");
//              Handler handler = new Handler();
//
//              //Put in delay because call log is not updated immediately when state changed
//              // The dialler takes a little bit of time to write to it 500ms seems to be enough
//              handler.postDelayed(new Runnable() {
//                  @Override
//                  public void run() {
//                      // get start of cursor
//                        Log.i("CallLogDetailsActivity", "Getting Log activity...");
//                          String[] projection = new String[]{Calls.NUMBER};
//                          Cursor cur = getContentResolver().query(Calls.CONTENT_URI, projection, null, null, Calls.DATE +" desc");
//                          cur.moveToFirst();
//                          String lastCallnumber = cur.getString(0);
//      	  }
//              	},500);

              isPhoneCalling = false;
          }
      }
  }
}