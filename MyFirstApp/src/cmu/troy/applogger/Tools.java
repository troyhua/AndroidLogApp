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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Environment;
import android.os.Handler;
import android.widget.Toast;

public class Tools {

  private static final String version = "v0.3";

  private static final String fileNamePattern = "log_v0\\.3-(\\d{8})\\.txt";

  private static final String logfile = "log/log_" + version;

  private static final String lastApp = "log/lastApp.txt";

  private static final String musicFile = "log/music.txt";

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

  public static void LogMusic(String message) throws IOException {
    File file = new File(Environment.getExternalStorageDirectory(), musicFile);
    Tools.safeCreateFile(file);
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd, hh:mm:ss");
    Date now = new Date();
    PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file.getAbsoluteFile(),
            true)));
    writer.println(dateFormat.format(now) + "\t" + message);
    writer.close();
  }

  public static String getPackage(String fullAppPath) {
    return fullAppPath.substring(0, fullAppPath.indexOf("/"));
  }

  public static String getSimpleTime(Date time) {
    SimpleDateFormat format = new SimpleDateFormat("hh:mm");
    return format.format(time);
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

  public static String getTimeSpanString(Date early, Date late) {
    long totalSeconds = (late.getTime() - early.getTime()) / 1000;
    StringBuilder sb = new StringBuilder();
    sb.append((totalSeconds / 3600) + " h " + (totalSeconds % 3600) / 60 + " m " + totalSeconds
            % 60 + " s");
    return sb.toString();
  }

  public static String getDateFromLogFile(File file) {
    String name = file.getName();
    Pattern p = Pattern.compile(fileNamePattern);
    Matcher matcher = p.matcher(name);
    if (matcher.find()) {
      if (name.startsWith("updated"))
        return null;
      return matcher.group(1);
    } else
      return null;
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
      lines.add(line);
      line = raf.readLine();
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
      Tools.safeCreateFile(file);
      PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(
              file.getAbsoluteFile(), true)));
      writer.println(Tools.STAR_SPLIT);
      writer.println((new Date()).toString());
      if (!content.endsWith("\n"))
        writer.println(content);
      else
        writer.print(content);
      writer.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void safeCreateFile(File file) throws IOException {
    if (!file.exists()) {
      File theDir = file.getParentFile();
      if (!theDir.exists())
        theDir.mkdir();
      file.createNewFile();
    }
  }

  public static String getLocationString(Location location) {
    return "Longitude: " + location.getLongitude() + "\n" + "Latitude: " + location.getLatitude()
            + "\n" + "Accuracy: " + location.getAccuracy() + "\n" + "Updated Time: "
            + (new Date(location.getTime())).toString() + "\n" + "Raw Form: " + location.toString();
  }

  public static void logJsonNewBlock(JSONObject object) {
    try {
      File file = Tools.getLogFile();
      Tools.safeCreateFile(file);
      PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(
              file.getAbsoluteFile(), true)));
      object.put(JSONKeys.time, (new Date()).toString());
      writer.println(object);
      writer.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static File createUpdatedFile(File oldFile) {
    return new File(oldFile.getParent(), "updated-" + oldFile.getName());
  }

  public static boolean updatedFileExist(File oldFile) {
    File file = createUpdatedFile(oldFile);
    return file.exists();
  }

  public static void newLogFile(List<JSONObject> jobs, File oldFile) {
    try {
      File file = createUpdatedFile(oldFile);
      Tools.safeCreateFile(file);
      PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(
              file.getAbsoluteFile(), false)));

      for (JSONObject job : jobs)
        writer.println(job);
      writer.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

  }
}
