package cmu.troy.myfirstapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar.OnNavigationListener;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;
import cmu.troy.applogger.JSONKeys;
import cmu.troy.applogger.LogAnalyzer;
import cmu.troy.applogger.Tools;
import cmu.troy.applogger.LogAnalyzer.Event;

import com.haarman.listviewanimations.ArrayAdapter;
import com.haarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.haarman.listviewanimations.itemmanipulation.SwipeDismissAdapter;
import com.haarman.listviewanimations.itemmanipulation.contextualundo.ContextualUndoAdapter.DeleteItemCallback;

/**
 * A fragment representing a single Report detail screen. This fragment is either contained in a
 * {@link ReportListActivity} in two-pane mode (on tablets) or a {@link ReportDetailActivity} on
 * handsets.
 */
public class ReportDetailFragment extends Fragment implements OnNavigationListener,
        OnDismissCallback, DeleteItemCallback {
  /**
   * The fragment argument representing the item ID that this fragment represents.
   */
  public static final String ARG_ITEM_ID = "item_id";

  /**
   * The dummy content this fragment is presenting.
   */
  public LogContent.LogItem mItem;

  public ArrayAdapter<Event> mAdapter;

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon
   * screen orientation changes).
   */
  public ReportDetailFragment() {
  }

  private static class MyListAdapter extends ArrayAdapter<Event> {

    private Context mContext;

    public MyListAdapter(Context context, List<Event> items) {
      super(items);
      mContext = context;
    }

    @Override
    public long getItemId(int position) {
      return getItem(position).hashCode();
    }

    @Override
    public boolean hasStableIds() {
      return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      TextView tv = (TextView) convertView;
      if (tv == null) {
        tv = (TextView) LayoutInflater.from(mContext).inflate(R.layout.list_row, parent, false);
      }
      tv.setText(getItem(position).content);
      return tv;
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getArguments().containsKey(ARG_ITEM_ID)) {
      mItem = LogContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
    }
  }

  protected ArrayAdapter<Event> createListAdapter(File logFile) {
    return new MyListAdapter(getActivity(), getItems(logFile));
  }

  private static List<JSONObject> readLog(File file) throws IOException, JSONException {
    List<JSONObject> ret = new ArrayList<JSONObject>();
    BufferedReader reader = new BufferedReader(new FileReader(file));
    String line = reader.readLine();
    while (line != null) {
      JSONObject job = new JSONObject(line);
      ret.add(job);
      line = reader.readLine();
    }
    reader.close();
    return ret;
  }

  public static List<Event> getItems(File logFile) {
    List<JSONObject> logs = new ArrayList<JSONObject>();
    try {
      logs = readLog(logFile);
    } catch (Exception e) {
      Log.e("json", e.toString());
    }

    return LogAnalyzer.clusterEvent(logs);
  }

  private ListView getListView(View rootView) {
    return (ListView) (rootView.findViewById(R.id.report_detail_list));
  }

  private void setSwipeDismissAdapter(View rootView) {
    SwipeDismissAdapter adapter = new SwipeDismissAdapter(mAdapter, this);
    try {
      adapter.setAbsListView(getListView(rootView));
      getListView(rootView).setAdapter(adapter);
    } catch (Exception e) {
      Log.e("view", e.toString());
    }
  }
  
   @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_report_detail, container, false);
    if (mItem != null) {
      File logFile = mItem.logFile;
      mAdapter = createListAdapter(logFile);
      setSwipeDismissAdapter(rootView);

      // ((TextView) rootView.findViewById(R.id.report_detail)).setText(ss);

    }

    return rootView;
  }

  @Override
  public void deleteItem(int position) {
    mAdapter.remove(position);
    mAdapter.notifyDataSetChanged();
  }

  @Override
  public void onDismiss(AbsListView listView, int[] reverseSortedPositions) {
    for (int position : reverseSortedPositions) {
      mAdapter.remove(position);
    }
    // Toast.makeText(this, "Removed positions: " + Arrays.toString(reverseSortedPositions),
    // Toast.LENGTH_SHORT).show();

  }

  @Override
  public boolean onNavigationItemSelected(int arg0, long arg1) {
    // TODO Auto-generated method stub
    return false;
  }
}
