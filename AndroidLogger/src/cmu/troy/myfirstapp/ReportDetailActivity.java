package cmu.troy.myfirstapp;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import cmu.troy.androidlogger.R;
import cmu.troy.applogger.LogAnalyzer.Event;
import cmu.troy.applogger.Tools;

/**
 * An activity representing a single Report detail screen. This activity is only used on handset
 * devices. On tablet-size devices, item details are presented side-by-side with a list of items in
 * a {@link ReportListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing more than a
 * {@link ReportDetailFragment}.
 */
public class ReportDetailActivity extends FragmentActivity {

  private ReportDetailFragment mfragment; 
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_report_detail);

    // Show the Up button in the action bar.
    getActionBar().setDisplayHomeAsUpEnabled(true);

    // savedInstanceState is non-null when there is fragment state
    // saved from previous configurations of this activity
    // (e.g. when rotating the screen from portrait to landscape).
    // In this case, the fragment will automatically be re-added
    // to its container so we don't need to manually add it.
    // For more information, see the Fragments API guide at:
    //
    // http://developer.android.com/guide/components/fragments.html
    //
    if (savedInstanceState == null) {
      // Create the detail fragment and add it to the activity
      // using a fragment transaction.
      Bundle arguments = new Bundle();
      arguments.putString(ReportDetailFragment.ARG_ITEM_ID,
              getIntent().getStringExtra(ReportDetailFragment.ARG_ITEM_ID));
      ReportDetailFragment fragment = new ReportDetailFragment();
      mfragment = fragment;
      fragment.setArguments(arguments);
      getSupportFragmentManager().beginTransaction().add(R.id.report_detail_container, fragment)
              .commit();
    }
  }

  public void generate_new_log_file(View view){
    List<JSONObject> jobs = new ArrayList<JSONObject>();
    for (int i = 0; i < mfragment.mAdapter.getCount(); i++){
      Event event = mfragment.mAdapter.getItem(i);
      for (JSONObject job : event.allUnitEvents)
        jobs.add(job);
    }
    Tools.newLogFile(jobs, mfragment.mItem.logFile);
    Context context = getApplicationContext();
    CharSequence text = "Log File updated!";
    int duration = Toast.LENGTH_SHORT;
    Toast toast = Toast.makeText(context, text, duration);
    toast.show();
  }


  
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        // This ID represents the Home or Up button. In the case of this
        // activity, the Up button is shown. Use NavUtils to allow users
        // to navigate up one level in the application structure. For
        // more details, see the Navigation pattern on Android Design:
        //
        // http://developer.android.com/design/patterns/navigation.html#up-vs-back
        //
        NavUtils.navigateUpTo(this, new Intent(this, ReportListActivity.class));
        return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
