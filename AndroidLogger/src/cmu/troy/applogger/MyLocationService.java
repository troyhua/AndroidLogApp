package cmu.troy.applogger;

import cmu.troy.applogger.Tools;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;

import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/**
 * An {@link Service} subclass for handling asynchronous task requests in a service on a
 * separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static helper methods.
 */
public class MyLocationService extends Service implements 
  GooglePlayServicesClient.ConnectionCallbacks,
  GooglePlayServicesClient.OnConnectionFailedListener{
  // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
  private static final String ACTION_LOG_LOCATION = "cmu.troy.myfirstapp.action.LOC_LOCATION";

  private static final String CALLER_ID = "cmu.troy.myfirstapp.extra.CALLER_ID";

  private LocationClient mLocationClient;
  
  /**
   * Starts this service to perform action LOG_LOCATION with the given parameters. If the service is already
   * performing a task this action will be queued.
   * 
   * @see IntentService
   */
  public static void startActionLogLocation(Context context, String caller_id) {
    Intent intent = new Intent(context, MyLocationService.class);
    intent.setAction(ACTION_LOG_LOCATION);
    intent.putExtra(CALLER_ID, caller_id);
    context.startService(intent);
    Log.d("Loc", "In Static Called");
  }

  @Override
  public void onCreate(){
    Log.d("Loc", "In onCreat");
    super.onCreate();
    mLocationClient.connect();
  }
  
  @Override
  public void onDestroy(){
    mLocationClient.disconnect();
    super.onDestroy();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.d("Loc", "In Handle");
    Toast.makeText(getApplicationContext(), "In Location Serivce", Toast.LENGTH_SHORT).show();
    if (intent != null) {
      final String action = intent.getAction();
      if (ACTION_LOG_LOCATION.equals(action)) {
        final String param1 = intent.getStringExtra(CALLER_ID);
        handleActionLogLocation(param1);
      } 
    }
    return super.onStartCommand(intent, flags, startId);
  }

  /**
   * Handle action Log_Location in the provided background thread with the provided parameters.
   */
  private void handleActionLogLocation(String caller_id) {
    StringBuilder message = new StringBuilder();
    message.append("Location update requested by: " + caller_id + "\n");
    int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
    if (ConnectionResult.SUCCESS != resultCode){
      message.append("Cannot connect to Google Play Service.\n");
    }else{
      Location mLocation = mLocationClient.getLastLocation();
      if (mLocation == null)
        message.append("Cannot get loast location.\n");
      else{
        message.append(Tools.getLocationString(mLocation));
      }
    }
    
    Tools.logNewBlock(message.toString());
  }

  @Override
  public void onConnectionFailed(ConnectionResult arg0) {
  }

  @Override
  public void onConnected(Bundle arg0) {
  }

  @Override
  public void onDisconnected() {
  }

  @Override
  public IBinder onBind(Intent intent) {
    // TODO Auto-generated method stub
    return null;
  }
}
