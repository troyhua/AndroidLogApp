package cmu.troy.myfirstapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar.OnNavigationListener;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;
import cmu.troy.androidlogger.R;
import cmu.troy.applogger.LogAnalyzer;
import cmu.troy.applogger.LogAnalyzer.Event;
import cmu.troy.applogger.LogAnalyzer.LocEvent;
import cmu.troy.applogger.Tools;

import com.haarman.listviewanimations.ArrayAdapter;
import com.haarman.listviewanimations.itemmanipulation.ExpandableListItemAdapter;
import com.haarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.haarman.listviewanimations.itemmanipulation.SwipeDismissAdapter;
import com.haarman.listviewanimations.itemmanipulation.contextualundo.ContextualUndoAdapter.DeleteItemCallback;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ValueAnimator;


/**
 * This class contains the main logic for rendering content in detail view of analyzer
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

  public ArrayAdapter<LocEvent> locAdapter;

  public int currentAdapter = 0;

  private View mRootView;

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon
   * screen orientation changes).
   */
  public ReportDetailFragment() {
  }

  private static class MyListAdapter extends ExpandableListItemAdapter<Event> {

    private Context mContext;

    private int mViewLayoutResId;

    private int mTitleParentResId;

    private int mContentParentResId;

    public List<Long> mVisibleIds;

    public MyListAdapter(Context context, List<Event> items) {
      super(context, R.layout.activity_expandablelistitem_card,
              R.id.activity_expandablelistitem_card_title,
              R.id.activity_expandablelistitem_card_content, items);
      // super(context, items);
      mViewLayoutResId = R.layout.activity_expandablelistitem_card;
      mTitleParentResId = R.id.activity_expandablelistitem_card_title;
      mContentParentResId = R.id.activity_expandablelistitem_card_content;
      mContext = context;
      mVisibleIds = new ArrayList<Long>();
    }

    @Override
    public long getItemId(int position) {
      return getItem(position).hashCode();
    }

    @Override
    public boolean hasStableIds() {
      return true;
    }

    private static class ViewHolder {
      ViewGroup titleParent;

      ViewGroup contentParent;

      View titleView;

      View contentView;
    }

    private ViewGroup createView(ViewGroup parent) {
      ViewGroup view;
      view = (ViewGroup) LayoutInflater.from(mContext).inflate(mViewLayoutResId, parent, false);
      return view;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
      ViewGroup view = (ViewGroup) convertView;
      ViewHolder viewHolder;

      if (view == null) {
        view = createView(parent);
        viewHolder = new ViewHolder();
        viewHolder.titleParent = (ViewGroup) view.findViewById(mTitleParentResId);
        viewHolder.contentParent = (ViewGroup) view.findViewById(mContentParentResId);

        view.setTag(viewHolder);
      } else {
        viewHolder = (ViewHolder) view.getTag();
      }

      View titleView = getTitleView(position, viewHolder.titleView, viewHolder.titleParent);
      if (!titleView.equals(viewHolder.titleView)) {
        viewHolder.titleParent.removeAllViews();
        viewHolder.titleParent.addView(titleView);

        View buttonView = (View) LayoutInflater.from(mContext).inflate(R.layout.my_toggle, parent,
                false);

        buttonView.findViewById(R.id.mytoggle).setOnClickListener(
                new TitleViewOnClickListener(viewHolder.contentParent));
        viewHolder.titleParent.addView(buttonView);
      }
      viewHolder.titleView = titleView;

      View contentView = getContentView(position, viewHolder.contentView, viewHolder.contentParent);
      if (!contentView.equals(viewHolder.contentView)) {
        viewHolder.contentParent.removeAllViews();
        viewHolder.contentParent.addView(contentView);
      }

      viewHolder.contentParent
              .setVisibility(mVisibleIds.contains(getItemId(position)) ? View.VISIBLE : View.GONE);
      viewHolder.contentParent.setTag(getItemId(position));
      return view;
    }

    private class TitleViewOnClickListener implements View.OnClickListener {

      private View mContentParent;

      private TitleViewOnClickListener(View contentParent) {
        this.mContentParent = contentParent;
      }

      @Override
      public void onClick(View view) {
        boolean isVisible = mContentParent.getVisibility() == View.VISIBLE;

        if (isVisible) {
          animateCollapsing();
          mVisibleIds.remove(mContentParent.getTag());
        } else {
          animateExpanding();
          mVisibleIds.add((Long) mContentParent.getTag());
        }
      }

      private void animateCollapsing() {
        int origHeight = mContentParent.getHeight();

        ValueAnimator animator = createHeightAnimator(origHeight, 0);
        animator.addListener(new Animator.AnimatorListener() {
          @Override
          public void onAnimationStart(Animator animator) {
          }

          @Override
          public void onAnimationEnd(Animator animator) {
            mContentParent.setVisibility(View.GONE);
          }

          @Override
          public void onAnimationCancel(Animator animator) {
          }

          @Override
          public void onAnimationRepeat(Animator animator) {
          }
        });
        animator.start();
      }

      private void animateExpanding() {
        mContentParent.setVisibility(View.VISIBLE);

        final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        mContentParent.measure(widthSpec, heightSpec);

        ValueAnimator animator = createHeightAnimator(0, mContentParent.getMeasuredHeight());
        animator.start();
      }

      private ValueAnimator createHeightAnimator(int start, int end) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
          @Override
          public void onAnimationUpdate(ValueAnimator valueAnimator) {
            int value = (Integer) valueAnimator.getAnimatedValue();

            ViewGroup.LayoutParams layoutParams = mContentParent.getLayoutParams();
            layoutParams.height = value;
            mContentParent.setLayoutParams(layoutParams);
          }
        });
        return animator;
      }
    }

    @Override
    public View getTitleView(int position, View convertView, ViewGroup parent) {
      TextView tv = (TextView) convertView;
      if (tv == null) {
        tv = (TextView) LayoutInflater.from(mContext).inflate(R.layout.list_row, parent, false);
      }
      tv.setText(getItem(position).title);
      return tv;
    }

    @Override
    public View getContentView(int position, View convertView, ViewGroup parent) {
      TextView tv = (TextView) convertView;
      if (tv == null) {
        tv = (TextView) LayoutInflater.from(mContext).inflate(R.layout.list_row, parent, false);
      }
      tv.setText(getItem(position).content);
      return tv;
    }
  }

  private static class MyLocationAdapter extends ExpandableListItemAdapter<LocEvent> {

    private Context mContext;

    private int mViewLayoutResId;

    private int mTitleParentResId;

    private int mContentParentResId;

    public List<Long> mVisibleIds;

    public MyLocationAdapter(Context context, List<LocEvent> items) {
      super(context, R.layout.activity_expandablelistitem_card,
              R.id.activity_expandablelistitem_card_title,
              R.id.activity_expandablelistitem_card_content, items);
      // super(context, items);
      mViewLayoutResId = R.layout.activity_expandablelistitem_card;
      mTitleParentResId = R.id.activity_expandablelistitem_card_title;
      mContentParentResId = R.id.activity_expandablelistitem_card_content;
      mContext = context;
      mVisibleIds = new ArrayList<Long>();
    }

    @Override
    public long getItemId(int position) {
      return getItem(position).hashCode();
    }

    @Override
    public boolean hasStableIds() {
      return true;
    }

    private static class ViewHolder {
      ViewGroup titleParent;

      ViewGroup contentParent;

      View titleView;

      View contentView;
    }

    private ViewGroup createView(ViewGroup parent) {
      ViewGroup view;
      view = (ViewGroup) LayoutInflater.from(mContext).inflate(mViewLayoutResId, parent, false);
      return view;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
      ViewGroup view = (ViewGroup) convertView;
      ViewHolder viewHolder;

      if (view == null) {
        view = createView(parent);
        viewHolder = new ViewHolder();
        viewHolder.titleParent = (ViewGroup) view.findViewById(mTitleParentResId);
        viewHolder.contentParent = (ViewGroup) view.findViewById(mContentParentResId);

        view.setTag(viewHolder);
      } else {
        viewHolder = (ViewHolder) view.getTag();
      }

      View titleView = getTitleView(position, viewHolder.titleView, viewHolder.titleParent);
      if (!titleView.equals(viewHolder.titleView)) {
        viewHolder.titleParent.removeAllViews();
        viewHolder.titleParent.addView(titleView);

        View buttonView = (View) LayoutInflater.from(mContext).inflate(R.layout.my_toggle, parent,
                false);

        buttonView.findViewById(R.id.mytoggle).setOnClickListener(
                new TitleViewOnClickListener(viewHolder.contentParent));
        viewHolder.titleParent.addView(buttonView);
      }
      viewHolder.titleView = titleView;

      View contentView = getContentView(position, viewHolder.contentView, viewHolder.contentParent);
      if (!contentView.equals(viewHolder.contentView)) {
        viewHolder.contentParent.removeAllViews();
        viewHolder.contentParent.addView(contentView);
      }

      viewHolder.contentParent
              .setVisibility(mVisibleIds.contains(getItemId(position)) ? View.VISIBLE : View.GONE);
      viewHolder.contentParent.setTag(getItemId(position));
      return view;
    }

    private class TitleViewOnClickListener implements View.OnClickListener {

      private View mContentParent;

      private TitleViewOnClickListener(View contentParent) {
        this.mContentParent = contentParent;
      }

      @Override
      public void onClick(View view) {
        boolean isVisible = mContentParent.getVisibility() == View.VISIBLE;

        if (isVisible) {
          animateCollapsing();
          mVisibleIds.remove(mContentParent.getTag());
        } else {
          animateExpanding();
          mVisibleIds.add((Long) mContentParent.getTag());
        }
      }

      private void animateCollapsing() {
        int origHeight = mContentParent.getHeight();

        ValueAnimator animator = createHeightAnimator(origHeight, 0);
        animator.addListener(new Animator.AnimatorListener() {
          @Override
          public void onAnimationStart(Animator animator) {
          }

          @Override
          public void onAnimationEnd(Animator animator) {
            mContentParent.setVisibility(View.GONE);
          }

          @Override
          public void onAnimationCancel(Animator animator) {
          }

          @Override
          public void onAnimationRepeat(Animator animator) {
          }
        });
        animator.start();
      }

      private void animateExpanding() {
        mContentParent.setVisibility(View.VISIBLE);

        final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        mContentParent.measure(widthSpec, heightSpec);

        ValueAnimator animator = createHeightAnimator(0, mContentParent.getMeasuredHeight());
        animator.start();
      }

      private ValueAnimator createHeightAnimator(int start, int end) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
          @Override
          public void onAnimationUpdate(ValueAnimator valueAnimator) {
            int value = (Integer) valueAnimator.getAnimatedValue();

            ViewGroup.LayoutParams layoutParams = mContentParent.getLayoutParams();
            layoutParams.height = value;
            mContentParent.setLayoutParams(layoutParams);
          }
        });
        return animator;
      }
    }

    @Override
    public View getTitleView(int position, View convertView, ViewGroup parent) {
      TextView tv = (TextView) convertView;
      if (tv == null) {
        tv = (TextView) LayoutInflater.from(mContext).inflate(R.layout.list_row, parent, false);
      }
      tv.setText(getItem(position).title);
      return tv;
    }

    @Override
    public View getContentView(int position, View convertView, ViewGroup parent) {
      TextView tv = (TextView) convertView;
      if (tv == null) {
        tv = (TextView) LayoutInflater.from(mContext).inflate(R.layout.list_row, parent, false);
      }
      tv.setText(getItem(position).content);
      return tv;
    }

  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case 0:
        // TODO
        makeEventAdapterFromLocAdapter();
        currentAdapter = 0;
        return true;
      case 1:
        makeLocAdapterFromEventAdapter();
        currentAdapter = 1;
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    menu.add(Menu.NONE, 0, 10, "Event View");
    menu.add(Menu.NONE, 1, 10, "Location View");
    // inflater.inflate(R.menu.activity_itemdetail, menu);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getArguments().containsKey(ARG_ITEM_ID)) {
      mItem = LogContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
    }

    setHasOptionsMenu(true);
  }

  protected ArrayAdapter<Event> createListAdapter(File logFile) {
    return new MyListAdapter(getActivity(), getItems(logFile));
  }

  private void makeLocAdapterFromEventAdapter() {
    List<Event> events = new ArrayList<Event>();
    for (int i = 0; i < mAdapter.getCount(); i++) {
      Event event = mAdapter.getItem(i);
      events.add(event);
    }
    locAdapter = new MyLocationAdapter(getActivity(), LogAnalyzer.ClusterLocFromEvents(events));
    SwipeDismissAdapter adapter = new SwipeDismissAdapter(locAdapter, this);
    try {
      adapter.setAbsListView(getListView(mRootView));
      getListView(mRootView).setAdapter(adapter);
    } catch (Exception e) {
      Log.e("view", e.toString());
    }
  }
  
  private void makeEventAdapterFromLocAdapter(){
    List<LocEvent> locs = new ArrayList<LocEvent>();
    for (int i = 0; i < locAdapter.getCount(); i++){
      locs.add(locAdapter.getItem(i));
    }
    mAdapter = new MyListAdapter(getActivity(), LogAnalyzer.GetEventFromLocEvents(locs));
    SwipeDismissAdapter adapter = new SwipeDismissAdapter(mAdapter, this);
    try {
      adapter.setAbsListView(getListView(mRootView));
      getListView(mRootView).setAdapter(adapter);
    } catch (Exception e) {
      Log.e("view", e.toString());
    }
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
      // read everything from log file
      logs = readLog(logFile);
    } catch (Exception e) {
      Log.e("json", e.toString());
    }
    // cluster log into events
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
    mRootView = rootView;
    if (mItem != null) {
      File logFile = mItem.logFile;
      if (mItem.updatedFileExisted){
        logFile = Tools.getUpdatedFile(logFile);
      }
      mAdapter = createListAdapter(logFile);
      setSwipeDismissAdapter(rootView);
    }
    return rootView;
  }

  @Override
  public void deleteItem(int position) {
    if (currentAdapter == 0) {
      mAdapter.remove(position);
      mAdapter.notifyDataSetChanged();
    } else {
      locAdapter.remove(position);
      locAdapter.notifyDataSetChanged();
    }
  }

  @Override
  public void onDismiss(AbsListView listView, int[] reverseSortedPositions) {
    if (currentAdapter == 0) {
      for (int position : reverseSortedPositions) {
        mAdapter.remove(position);
      }
    } else {
      for (int position : reverseSortedPositions) {
        locAdapter.remove(position);
      }
    }
  }

  @Override
  public boolean onNavigationItemSelected(int arg0, long arg1) {
    return false;
  }
}
