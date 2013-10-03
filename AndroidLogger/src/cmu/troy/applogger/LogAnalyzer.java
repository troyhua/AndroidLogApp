package cmu.troy.applogger;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import cmu.troy.applogger.JSONKeys.JSONValues;

public class LogAnalyzer {
  @SuppressWarnings("deprecation")
  public static ArrayList<Event> clusterEvent(List<JSONObject> unitList) {
    ArrayList<Event> ret = new ArrayList<Event>();
    int lastIndex = 0;
    Date lastDate = null;
    for (int i = 0; i < unitList.size(); i++) {
      Date thisDate;
      try {
        thisDate = new Date(Date.parse(unitList.get(i).getString(JSONKeys.time)));
        if (lastDate == null) {
          lastDate = thisDate;
        } else {
          long totalSeconds = (thisDate.getTime() - lastDate.getTime()) / 1000;
          if (totalSeconds > 120) {
            Event event = createEvent(unitList.subList(lastIndex, i));
            if (!event.ommited)
              ret.add(event);
            lastIndex = i;
            lastDate = thisDate;
          } else {
            lastDate = thisDate;
          }
        }
      } catch (JSONException e) {
        Log.e("json", e.toString());
      }
    }
    try {
      Event event = createEvent(unitList.subList(lastIndex, unitList.size()));
      if (!event.ommited)
        ret.add(event);
    } catch (Exception e) {
      Log.e("json", e.toString());
    }
    return ret;
  }

  public static Event createEvent(List<JSONObject> unitList) throws JSONException {
    
    //MainActivity.initializePackageNameDictionary();
    
    Event event = new Event();
    event.allUnitEvents = unitList;
    for (JSONObject job : unitList) {
      String type = job.getString(JSONKeys.log_type);
      if (type.equals(JSONValues.INCOMING_CALL) || type.equals(JSONValues.OUTGOING_CALL)) {
        event.callEvent = job;
      }
      if (type.equals(JSONValues.CALL_END)) {
        event.callEndEvent = job;
      }
      if (type.equals(JSONValues.OPEN_AN_APP)) {
        String appPath = job.getString(JSONKeys.first);
        if (!appPath.startsWith("com.android.launcher/") && !appPath.startsWith("com.android.systemui")) {
          String packageName = Tools.getAppName(appPath);
          if (event.appMap.containsKey(packageName))
            event.appMap.put(packageName, event.appMap.get(packageName));
          else
            event.appMap.put(packageName, 1);
        }
      }
    }

    renderEvent(event);

    return event;
  }

  @SuppressWarnings("deprecation")
  public static void renderEvent(Event event) throws JSONException {
    
    if (event.callEvent != null) {

      SpannableString ns = new SpannableString("Contacted "
              + event.callEvent.getString(JSONKeys.number));
      ns.setSpan(new ForegroundColorSpan(Color.BLUE), 0, 10, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
      event.title = ns;
      String startTime = Tools.getSimpleTime(new Date(Date.parse(event.callEvent
              .getString(JSONKeys.time))));
      ns = new SpannableString("From " + startTime);
      ns.setSpan(new ForegroundColorSpan(Color.GRAY), 5, 10, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
      event.content = ns;
      if (event.callEndEvent != null) {
        ns = new SpannableString(" to "
                + Tools.getSimpleTime(new Date(Date.parse(event.callEndEvent
                        .getString(JSONKeys.time)))));
        ns.setSpan(new ForegroundColorSpan(Color.GRAY), 4, 9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        event.content = TextUtils.concat(event.content, ns);
      }
      return;
    }
    if (!event.appMap.isEmpty()) {
      Iterator<Entry<String, Integer>> it = event.appMap.entrySet().iterator();
      int max = -1;
      String keyApp = "";
      List<String> apps = new ArrayList<String>();
      while (it.hasNext()) {
        Entry<String, Integer> pairs = (Entry<String, Integer>) it.next();
        if (pairs.getValue() > max) {
          max = pairs.getValue();
          keyApp = pairs.getKey();
        }
        apps.add(pairs.getKey());
      }
      SpannableString ns = new SpannableString("Used " + keyApp);
      ns.setSpan(new ForegroundColorSpan(Color.GREEN), 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
      event.title = ns;
      event.content = "";
      ns = new SpannableString("From "
              + Tools.getSimpleTime(new Date(Date.parse(event.allUnitEvents.get(0).getString(
                      JSONKeys.time)))));
      ns.setSpan(new ForegroundColorSpan(Color.GRAY), 5, 10, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
      event.content = TextUtils.concat(event.content, ns);
      ns = new SpannableString(" to "
              + Tools.getSimpleTime(new Date(Date.parse(event.allUnitEvents.get(
                      event.allUnitEvents.size() - 1).getString(JSONKeys.time)))));
      ns.setSpan(new ForegroundColorSpan(Color.GRAY), 4, 9, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
      event.content = TextUtils.concat(event.content, ns);
      if (apps.size() > 1){
        ns = new SpannableString("\nWith the following apps:");
        ns.setSpan(new ForegroundColorSpan(Color.CYAN), 1, ns.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        event.content = TextUtils.concat(event.content, ns);
        StringBuilder sb = new StringBuilder();
        for (String app : apps){
          if (!app.equals(keyApp))
            sb.append("\n" + app);
        }
        event.content = TextUtils.concat(event.content, sb.toString());
      }
      return;
    }
    event.ommited = true;
    return;
  }

  public static class Event {
    public List<JSONObject> allUnitEvents;

    public CharSequence content = "";

    public CharSequence title = "";

    public JSONObject keyEvent;

    public JSONObject callEvent = null;

    public JSONObject callEndEvent = null;

    public boolean ommited = false;

    public List<JSONObject> noDesktopApp = new ArrayList<JSONObject>();

    public HashMap<String, Integer> appMap = new HashMap<String, Integer>();

    public Event() {
    }

    public Event(JSONObject single) {
      allUnitEvents = new ArrayList<JSONObject>();
      allUnitEvents.add(single);
      keyEvent = single;
    }
  }
}
