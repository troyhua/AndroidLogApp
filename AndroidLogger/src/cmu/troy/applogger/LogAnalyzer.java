package cmu.troy.applogger;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
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

    // MainActivity.initializePackageNameDictionary();

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
        if (!appPath.startsWith("com.android.launcher/")
                && !appPath.startsWith("com.android.systemui")) {
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
      String location = getAddress(event);
      if (!location.equals("")){
        ns = new SpannableString("\nLocation:" + location);
        ns.setSpan(new ForegroundColorSpan(Color.DKGRAY), 1, 9,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        event.content = TextUtils.concat(event.content, ns);
      }
      if (apps.size() > 1) {
        ns = new SpannableString("\nYou also use the following apps:");
        ns.setSpan(new ForegroundColorSpan(Color.CYAN), 1, ns.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        event.content = TextUtils.concat(event.content, ns);
        StringBuilder sb = new StringBuilder();
        for (String app : apps) {
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

  public static class LocEvent {
    public List<Event> allEvents;

    public String address;

    public CharSequence content = "";

    public CharSequence title = "";

    public LocEvent(String address) {
      allEvents = new ArrayList<Event>();
      this.address = address;
    }
  }

  private static void renderLocEvent(LocEvent locEvent) {
    if (!locEvent.address.equals("")) {
      locEvent.title = locEvent.address;
    }else{
      locEvent.title = "Events without address information";
    }
    HashSet<CharSequence> eventSet = new HashSet<CharSequence>();
    for (Event event : locEvent.allEvents) {
      eventSet.add(event.title);
    }
    if (!locEvent.address.equals(""))
      locEvent.content = "At this place, you made the following interaction with your cell phone";
    for (CharSequence str : eventSet) {
      locEvent.content = TextUtils.concat(locEvent.content, "\n", str);
    }
  }

  private static String getAddress(Event event) {
    Hashtable<String, Integer> addressTable = new Hashtable<String, Integer>();
    for (JSONObject job : event.allUnitEvents) {
      try {
        if (job.has(JSONKeys.addr_available)) {
          if (job.getBoolean(JSONKeys.addr_available)) {
            String address = job.getString(JSONKeys.address);
            if (addressTable.containsKey(address))
              addressTable.put(address, addressTable.get(address) + 1);
            else
              addressTable.put(address, 1);
          }
        }
      } catch (Exception e) {
        Log.e("Addr", e.toString());
      }
    }
    if (addressTable.size() == 0)
      return "";
    String maxAddr = "";
    int max = -1;
    for (String key : addressTable.keySet()) {
      if (addressTable.get(key) > max) {
        max = addressTable.get(key);
        maxAddr = key;
      }
    }
    return maxAddr;
  }

  public static List<LocEvent> ClusterLocFromEvents(List<Event> events) {
    Hashtable<String, LocEvent> locTable = new Hashtable<String, LocEvent>();
    for (Event event : events) {
      String addr = getAddress(event);
      if (locTable.containsKey(addr)) {
        locTable.get(addr).allEvents.add(event);
      } else {
        LocEvent locEvent = new LocEvent(addr);
        locEvent.allEvents.add(event);
        locTable.put(addr, locEvent);
      }
    }
    List<LocEvent> ret = new ArrayList<LocEvent>();
    for (String addr : locTable.keySet()) {
      LocEvent loc = locTable.get(addr);
      renderLocEvent(loc);
      ret.add(loc);
    }
    return ret;
  }

  public static List<Event> GetEventFromLocEvents(List<LocEvent> locs) {
    List<Event> ret = new ArrayList<Event>();
    for (LocEvent loc : locs){
      ret.addAll(loc.allEvents);
    }
    return ret;
  }
}
