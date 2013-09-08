package cmu.troy.applogger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.widget.Toast;

public class Tools {

  private static final String logfile = "log/log_v0.2";

  private static final String lastApp = "log/lastApp.txt";

  public static final String STAR_SPLIT = "****************";

  public static int max(int a, int b) {
    if (a > b)
      return a;
    else
      return b;
  }

  public static int min(int a, int b) {
    if (a < b)
      return a;
    else
      return b;
  }

  public static void setAlarm(Context context, int period) {
    AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    Intent i = new Intent(context, OnAlarmReceiver.class);
    PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
    mgr.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), period, pi);
  }

  public static void toastFromHandler(Handler handler, final String msg, final Context context) {
    handler.post(new Runnable() {
      public void run() {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
      }
    });
  }

  public static String showStringList(List<String> list, String spliter) {
    StringBuilder sb = new StringBuilder();
    for (String s : list) {
      sb.append(s);
      sb.append(spliter);
    }
    return sb.toString();
  }

  public static File getLogFile() {
    DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    Date now = new Date();
    String filepath = logfile + "-" + dateFormat.format(now) + ".txt";
    File file = new File(Environment.getExternalStorageDirectory(), filepath);
    return file;
  }

  public static File getLastAppFile() {
    File file = new File(Environment.getExternalStorageDirectory(), lastApp);
    return file;
  }

  public static String getLastFewLines(File file, int lineNum) throws IOException {
    RandomAccessFile raf = new RandomAccessFile(file, "r");
    StringBuilder sb = new StringBuilder();
    long length = raf.length();
    long bufferLength = 40 * 8 * lineNum;
    if (length > bufferLength)
      raf.seek(length - bufferLength);
    String line = raf.readLine();
    ArrayList<String> lines = new ArrayList<String>();
    while (line != null) {
      line = raf.readLine();
      lines.add(line);
    }
    for (int i = max((lines.size() - lineNum), 0); i < lines.size(); i++) {
      sb.append(lines.get(i) + "\n");
    }
    raf.close();
    return sb.toString();
  }

  public static void logNewBlock(String content) {
    try {
      File file = Tools.getLogFile();
      PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(
              file.getAbsoluteFile(), true)));
      writer.println(Tools.STAR_SPLIT);
      writer.println((new Date()).toString());
      writer.println(content);
      writer.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void saveCreateFile(File file) throws IOException {
    if (!file.exists()) {
      File theDir = file.getParentFile();
      if (!theDir.exists())
        theDir.mkdir();
      file.createNewFile();
    }
  }
}
