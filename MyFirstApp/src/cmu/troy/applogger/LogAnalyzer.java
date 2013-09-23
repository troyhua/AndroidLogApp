package cmu.troy.applogger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
            lastIndex = i + 1;
            lastDate = thisDate;
          } else {
            lastDate = thisDate;
          }
        }
      } catch (JSONException e) {
        Log.e("json", e.toString());
      }
    }
    return ret;
  }

  public static Event createEvent(List<JSONObject> unitList) throws JSONException {
    Event event = new Event();
    event.allUnitEvents = unitList;
    for (JSONObject job : unitList) {
      String type = job.getString(JSONKeys.log_type);
      if (type.equals(JSONValues.INCOMING_CALL) || type.equals(JSONValues.OUTGOING_CALL)){
        event.callEvent = job;
        break;
      }
      if (type.equals(JSONValues.OPEN_AN_APP)){
        String appPath = job.getString(JSONKeys.first);
        if (!appPath.startsWith("com.android.launcher/")){
          event.noDesktopApp.add(job);
        }
      }
    }
    
    renderEvent(event);
    
    return event;
  }
  
  public static void renderEvent(Event event) throws JSONException{
    if (event.callEvent != null){
      Date time = new Date(Date.parse(event.callEvent.getString(JSONKeys.time)));
      SpannableString ns = new SpannableString("Contacted " + event.callEvent.getString(JSONKeys.number)
              + " at " + time.getHours() + ":" + time.getMinutes());
      ns.setSpan(new ForegroundColorSpan(Color.BLUE), 0, 10, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
      event.content = ns;
      return;
    }
    if (event.noDesktopApp.size() != 0){
      for (JSONObject job : event.noDesktopApp){
        Date time = new Date(Date.parse(job.getString(JSONKeys.time)));
        SpannableString ns = new SpannableString("Opened " + job.getString(JSONKeys.first)
          + " at " + time.getHours() + ":" + time.getMinutes() + "\n");
        ns.setSpan(new ForegroundColorSpan(Color.GREEN), 0, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        event.content = TextUtils.concat(event.content, ns);
      }
      event.content = event.content.subSequence(0, event.content.length() - 1);
      return;
    }
    event.ommited = true;
    return;
  }

  public static class Event {
    public List<JSONObject> allUnitEvents;
    public CharSequence content = "";
    public JSONObject keyEvent;
    public JSONObject callEvent = null;
    public boolean ommited = false;
    public List<JSONObject> noDesktopApp = new ArrayList<JSONObject>();

    public Event() {
    }

    public Event(JSONObject single) {
      allUnitEvents = new ArrayList<JSONObject>();
      allUnitEvents.add(single);
      keyEvent = single;
    }
  }
}
