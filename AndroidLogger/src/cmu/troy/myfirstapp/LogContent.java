package cmu.troy.myfirstapp;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cmu.troy.applogger.Tools;

public class LogContent {
  
  public static List<LogItem> ITEMS = new ArrayList<LogItem>();
  public static Map<String, LogItem> ITEM_MAP = new HashMap<String, LogItem>();
  
  static {
    File logDir = Tools.getLogFile().getParentFile();
    File[] files = logDir.listFiles();
    for (File mf :files){
      String date_str = Tools.getDateFromLogFile(mf);
      if (date_str != null){
        addItem(new LogItem(date_str, mf.getName(), mf, Tools.updatedFileExist(mf)));
      }
    }
  }
  
  public static void refreshItem(){
    ITEMS = new ArrayList<LogItem>();
    ITEM_MAP = new HashMap<String, LogItem>();
    File logDir = Tools.getLogFile().getParentFile();
    File[] files = logDir.listFiles();
    for (File mf :files){
      String date_str = Tools.getDateFromLogFile(mf);
      if (date_str != null){
        addItem(new LogItem(date_str, mf.getName(), mf, Tools.updatedFileExist(mf)));
      }
    }
  }
  
  private static void addItem(LogItem item) {
    ITEMS.add(item);
    ITEM_MAP.put(item.id, item);
  }
  
  public static class LogItem{
    public String id;
    public String content;
    public File logFile;
    public boolean updatedFileExisted = false;

    public LogItem(String id, String content, File file, boolean updatedFileExisted) {
      this.id = id;
      this.content = content;
      this.logFile = file;
      this.updatedFileExisted = updatedFileExisted;
    }

    @Override
    public String toString() {
      String rendered = id.substring(4, 6) + "-" + id.substring(6, 8) + "-" + id.substring(0, 4);
      if (updatedFileExisted)
        return rendered + "    (updated)";
      else
        return rendered;
    }
  }
}


