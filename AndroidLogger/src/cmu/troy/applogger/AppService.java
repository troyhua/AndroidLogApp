/***
  Copyright (c) 2008-2012 CommonsWare, LLC
  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
  by applicable law or agreed to in writing, software distributed under the
  License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
  OF ANY KIND, either express or implied. See the License for the specific
  language governing permissions and limitations under the License.
  
  From _The Busy Coder's Guide to Advanced Android Development_
    http://commonsware.com/AdvAndroid
 */

package cmu.troy.applogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import cmu.troy.applogger.JSONKeys.JSONValues;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class AppService extends WakefulIntentService implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {
  private static List<String> lastApps = new ArrayList<String>();

  private static Location lastLocation;

  private static Address lastAddress;

  private LocationClient mLocationClient;

  public AppService() {
    super("AppService");
  }

  @Override
  public void onCreate() {
    super.onCreate();
    mLocationClient = new LocationClient(this, this, this);
    mLocationClient.connect();
  }

  @Override
  public void onDestroy() {
    mLocationClient.disconnect();
    super.onDestroy();
  }

  private List<String> getLastApps() throws IOException {
    return lastApps;
  }

  private List<String> getCurrentApps() {
    Context context = this.getApplicationContext();
    ActivityManager mgr = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
    List<RunningTaskInfo> tasks = mgr.getRunningTasks(100);
    ArrayList<String> res = new ArrayList<String>();
    for (Iterator<RunningTaskInfo> i = tasks.iterator(); i.hasNext();) {
      RunningTaskInfo p = (RunningTaskInfo) i.next();
      res.add(p.baseActivity.flattenToString());
    }
    return res;
  }

  private List<String> getNewApps(List<String> oldApps, List<String> currentApps) {
    if (oldApps.size() == currentApps.size()) {
      for (int i = 0; i < oldApps.size(); i++)
        if (!oldApps.get(i).equals(currentApps.get(i)))
          return currentApps;
      return new ArrayList<String>();
    } else
      return currentApps;
  }

  private void logApps(List<String> newApps, List<String> currentApps) throws IOException {
    /*
     * Empty newApps means current apps are the same with the last apps. So there is no need to
     * update last apps file or log file.
     */
    if (newApps == null || newApps.size() == 0)
      return;

    lastApps = currentApps;
    Date now = new Date();

    /* Append new Apps into log file */
    JSONObject job = new JSONObject();
    String id = String.valueOf(now.getTime());
    try {
      job.put(JSONKeys.id, id);
      job.put(JSONKeys.first, newApps.get(0));
      job.put(JSONKeys.log_type, JSONValues.OPEN_AN_APP);
      /* Log Location block */
      Location mLocation = null;
      if (mLocationClient.isConnected())
        mLocation = mLocationClient.getLastLocation();
      if (mLocation != null) {
        job.put(JSONKeys.loc_available, true);
        job.put(JSONKeys.latitude, mLocation.getLatitude());
        job.put(JSONKeys.longitude, mLocation.getLongitude());
        job.put(JSONKeys.location_accuracy, mLocation.getAccuracy());
        job.put(JSONKeys.location_updated_time, (new Date(mLocation.getTime())).toString());
        
        Date updateTime = new Date(mLocation.getTime());
        if ((updateTime.getTime() - now.getTime()) / 60000 > 5){
          Tools.runningLog("Last location is too old, a location update is triggered.");
          LocationRequest request = new LocationRequest();
          request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
          mLocationClient.requestLocationUpdates(request, new LocationListener(){
            @Override
            public void onLocationChanged(Location arg0) {
            }
          });
        }
                
        if (lastLocation == null || lastAddress == null
                || lastLocation.distanceTo(mLocation) > Tools.SMALL_DISTANCE) {
          /* Log Address if location is available */
          Geocoder geocoder = new Geocoder(this, Locale.getDefault());
          List<Address> addresses = null;
          addresses = geocoder
                  .getFromLocation(mLocation.getLatitude(), mLocation.getLongitude(), 1);
          if (addresses != null && addresses.size() > 0) {
            job.put(JSONKeys.addr_available, true);
            Address address = addresses.get(0);
            lastAddress = address;
            job.put(JSONKeys.address,
                    address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "");
            job.put(JSONKeys.city, address.getLocality());
            job.put(JSONKeys.country, address.getCountryName());
          } else {
            job.put(JSONKeys.addr_available, false);
          }
        } else {
          job.put(JSONKeys.addr_available, true);
          job.put(JSONKeys.address,
                  lastAddress.getMaxAddressLineIndex() > 0 ? lastAddress.getAddressLine(0) : "");
          job.put(JSONKeys.city, lastAddress.getLocality());
          job.put(JSONKeys.country, lastAddress.getCountryName());
        }
        lastLocation = mLocation;
      } else {
        if (!mLocationClient.isConnecting())
          mLocationClient.connect();
        job.put(JSONKeys.loc_available, false);
      }
    } catch (JSONException e) {
      Log.e("JSON", e.toString());
    }
    Tools.logJsonNewBlock(job);
  }

  private boolean checkExternalStorageAvailable() {
    boolean mExternalStorageAvailable = false;
    boolean mExternalStorageWriteable = false;
    String state = Environment.getExternalStorageState();
    if (Environment.MEDIA_MOUNTED.equals(state)) {
      mExternalStorageAvailable = mExternalStorageWriteable = true;
    } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
      mExternalStorageAvailable = true;
      mExternalStorageWriteable = false;
    } else {
      mExternalStorageAvailable = mExternalStorageWriteable = false;
    }
    if (!(mExternalStorageAvailable && mExternalStorageWriteable)) {
      return false;
    } else
      return true;
  }

  @Override
  protected void doWakefulWork(Intent intent) {
    try {
      if (checkExternalStorageAvailable()) {
        List<String> lastApps = getLastApps();
        List<String> currentApps = getCurrentApps();
        List<String> newApps = getNewApps(lastApps, currentApps);
        logApps(newApps, currentApps);
      } else
        Tools.toastFromHandler(handler, "Cannot access external storage!", getApplicationContext());
    } catch (Exception e) {
      Tools.toastFromHandler(handler, e.toString(), getApplicationContext());
    }

  }

  @Override
  public void onConnectionFailed(ConnectionResult arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onConnected(Bundle arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onDisconnected() {
    // TODO Auto-generated method stub

  }
}