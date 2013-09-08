package cmu.troy.applogger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OutGoingCallReceiver extends BroadcastReceiver {
	public OutGoingCallReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String phonenumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
		Tools.logNewBlock("Outgoing call: " + phonenumber);
	}
}