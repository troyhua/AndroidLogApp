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

public class ScreenReceiver extends BroadcastReceiver {
	public ScreenReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			String strAction = intent.getAction();
			if (strAction.equals(Intent.ACTION_SCREEN_OFF)
					|| strAction.equals(Intent.ACTION_SCREEN_ON)) {
				File file = Tools.getLogFile();
				PrintWriter writer = new PrintWriter(new BufferedWriter(
						new FileWriter(file.getAbsoluteFile(), true)));
				writer.println(Tools.STAR_SPLIT);
				writer.println((new Date()).toString());
				if (strAction.equals(Intent.ACTION_SCREEN_OFF))
					writer.println("Screen Off");
				else
					writer.println("Screen On");
				writer.close();
			}
		} catch (IOException e) {

		}
	}
}
