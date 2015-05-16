package nl.imarinelife.lib.fieldguide;

import nl.imarinelife.lib.LibApp;
import nl.imarinelife.lib.MainActivity;
import nl.imarinelife.lib.MarineLifeContentProvider;
import nl.imarinelife.lib.Preferences;
import nl.imarinelife.lib.R;
import nl.imarinelife.lib.catalog.Catalog;
import nl.imarinelife.lib.fieldguide.FieldGuideEntryFragment.OnFieldGuideItemSelectedListener;
import nl.imarinelife.lib.fieldguide.db.FieldGuideAndSightingsEntryDbHelper;
import nl.imarinelife.lib.fieldguide.db.FieldGuideEntry;
import nl.imarinelife.lib.utility.FilterCursorWrapper;
import nl.imarinelife.lib.utility.Utils;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;


public class FieldGuideListFragment extends ListFragment implements
		SearchView.OnQueryTextListener, SearchView.OnCloseListener,
		LoaderManager.LoaderCallbacks<Cursor> {
    public static String TAG = "FieldGuideListFragment";
	private static final String KEY_DISPLAY_OPT = "displayOptions";

	public static final String CONSTRAINT = "FieldGuideConstraint";
	public static final String CHECKED_POSITION = "FieldGuidePosition";
	public static final String TOP_POSITION = "DivingLogSightingsTopPosition";
	
	private boolean mDualPane;
	private long mCurId = 0L;
	private int checkedPosition = 0;
	private static int topPosition = 0;

	private SearchView searchView;
	private String constraint;
	private ProgressBar progressbar;
	private View header;

	private FieldGuideAndSightingsEntryDbHelper dbHelper;
	private FieldGuideSimpleCursorAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		// initialize
		setRetainInstance(true);

		if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
			initializeBundle(savedInstanceState);
		} else {
			initializeBundle(getArguments());
		}

		super.onCreate(savedInstanceState);
	}
	
	private void initializeBundle(Bundle savedInstanceState) {
		if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
			Log.d(TAG, "initializeBundle [" + savedInstanceState.size() + "]");
			checkedPosition = savedInstanceState.getInt(CHECKED_POSITION, 0);
			topPosition = savedInstanceState.getInt(TOP_POSITION,0);
			Log.d(TAG,"top position gotten["+topPosition+"]");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View layout = super.onCreateView(inflater, container,
				savedInstanceState);
		ListView lv = (ListView) layout.findViewById(android.R.id.list);
		ViewGroup parent = (ViewGroup) lv.getParent();

		// Remove ListView and add CustomView in its place
		int lvIndex = parent.indexOfChild(lv);
		parent.removeViewAt(lvIndex);
		LinearLayout mLinearLayout = (LinearLayout) inflater.inflate(
				R.layout.listview_field_guide, container, false);
		parent.addView(mLinearLayout, lvIndex, lv.getLayoutParams());
		return layout;
	}

	@SuppressLint("NewApi")
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Inflate the layout for this fragment
        // Getting adapter
        Log.d(TAG, "onActivityCreated ");

        header = (View) getView().findViewById(R.id.fieldguide_header);
        progressbar = (ProgressBar) header
                .findViewById(R.id.fieldguide_progressbar);

        dbHelper = FieldGuideAndSightingsEntryDbHelper.getInstance(this
                .getActivity());
        if (dbHelper.shouldShowStatus()) {
            header.setVisibility(View.VISIBLE);
        } else {
            header.setVisibility(View.GONE);
        }
        if (dbHelper.shouldInitialize())
            dbHelper.initialize(LibApp.getCurrentResources());

        Cursor simple = dbHelper.queryFieldGuide(null, null, null, null);
        Cursor cursor = new FilterCursorWrapper(simple,
                Preferences.FIELDGUIDE_GROUPS_HIDDEN,
                FieldGuideAndSightingsEntryDbHelper.KEY_GROUPNAME_CURSORLOC);

        // The desired columns to be bound
        String[] columns = new String[]{
                FieldGuideAndSightingsEntryDbHelper.KEY_CATNAME,
                FieldGuideAndSightingsEntryDbHelper.KEY_GROUPNAME,
                FieldGuideAndSightingsEntryDbHelper.KEY_COMMONNAME,
                FieldGuideAndSightingsEntryDbHelper.KEY_LATINNAME,
                FieldGuideAndSightingsEntryDbHelper.KEY_ROWID};

        // the XML defined views which the data will be bound to
        int[] to = new int[]{R.id.header_fieldguide_list_amounts,
                R.id.header_fieldguide_list_groupname,
                R.id.text_fieldguide_list, R.id.latintext_fieldguide_list,
                R.id.image_fieldguide_list};

        adapter = new FieldGuideSimpleCursorAdapter(this.getActivity(),
                R.layout.listview_field_guide_row, cursor, columns, to, 0);
        setListAdapter(adapter);

        setHasOptionsMenu(true);
        mDualPane = ((MainActivity) getActivity()).getNumberOfPanes() == 2;

        if (savedInstanceState != null) {
            // Restore last state for checked position.
            mCurId = savedInstanceState.getLong(FieldGuideEntry.ID);
            // restore actionbar displayoptions (like up icon)
            int savedDisplayOpt = savedInstanceState.getInt(KEY_DISPLAY_OPT);
            if (savedDisplayOpt != 0) {
                ActionBar abar = MainActivity.me.getActionBar();
                if (abar != null) {
                    abar.setDisplayOptions(savedDisplayOpt);
                }
            }
            if (android.os.Build.VERSION.SDK_INT >= 11) {
                getListView().smoothScrollToPositionFromTop(topPosition, 0);
            } else {
                getListView().setSelection(topPosition);
            }

            if (mDualPane) {
                // In dual-pane mode, the list view highlights the selected item.
                getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                // Make sure our UI is in the correct state.
                showDetails(0);
            }

        }
    }



    @Override
    public void onPrepareOptionsMenu(Menu menu){
        Log.d(TAG, "onPrepareOptionsMenu");
        menu.clear();
        getActivity().getMenuInflater().inflate(
				R.menu.field_guide_list, menu);

		MenuItem item = menu.findItem(R.id.fieldguide_search);
		searchView = new SearchView(getActivity());
		item.setActionView(searchView);
        if(searchView!=null) {
			searchView.setSubmitButtonEnabled(true);
            searchView.setOnQueryTextListener(this);
			Utils.setSearchTextColour(searchView, getActivity().getResources());
        }

        MenuItem collapseItem = menu.findItem(R.id.fieldguide_collapse);
        MenuItem expandItem = menu.findItem(R.id.fieldguide_expand);
		Catalog catalog = LibApp.getInstance().getCurrentCatalog();
		Log.d(TAG, "Hidden groups [" + Preferences.getString(Preferences.FIELDGUIDE_GROUPS_HIDDEN, "")+"]");
        if (Preferences
                .getBoolean(Preferences.FIELDGUIDE_COLLAPSED_LAST, false)
				|| catalog==null
				|| Preferences.getString(Preferences.FIELDGUIDE_GROUPS_HIDDEN, "").equals(catalog.getAllGroups())) {
            collapseItem.setVisible(false);
            expandItem.setVisible(true);
        } else {
            collapseItem.setVisible(true);
            expandItem.setVisible(false);
        }

        super.onPrepareOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: [" + item.getItemId() + "]");
        if (item.getItemId() == R.id.fieldguide_expand) {
            Preferences.setString(Preferences.FIELDGUIDE_GROUPS_HIDDEN, "");
            Preferences
                    .setBoolean(Preferences.FIELDGUIDE_COLLAPSED_LAST, false);
			getActivity().invalidateOptionsMenu();
            refresh();
            return true;

        } else if (item.getItemId() == R.id.fieldguide_collapse) {
            Preferences.setString(Preferences.FIELDGUIDE_GROUPS_HIDDEN,
					LibApp.getCurrentCatalogGroups());
            Preferences.setBoolean(Preferences.FIELDGUIDE_COLLAPSED_LAST, true);
			getActivity().invalidateOptionsMenu();
            refresh();
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putLong(FieldGuideEntry.ID, mCurId);
        outState.putInt(KEY_DISPLAY_OPT, MainActivity.me
                .getActionBar().getDisplayOptions());
        outState.putInt(CHECKED_POSITION, checkedPosition);
        Log.d(TAG, "storing top position["+topPosition+"]");
        outState.putInt(TOP_POSITION, topPosition);
    }


    public static void setTopPosition(int position) {
		Log.d(TAG, "topPosition[" + position + "]");
		topPosition = position;
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.d(TAG, "ListItemClicked hiding keyboard");
		Utils.hideKeyboard(this);
		checkedPosition = position;
		topPosition = getListView().getFirstVisiblePosition();
		Preferences.setInt(Preferences.LAST_FIELDGUIDE_POSITION, position);
		// setSelection(position);
		showDetails(id);
	}

	/**
	 * Helper function to show the details of a selected item, either by
	 * displaying a fragment in-place in the current UI, or starting a whole new
	 * activity in which it is displayed.
	 */
	void showDetails(long id) {
		// We can display everything in-place with fragments, so update
		// the list to highlight the selected item and show the data.
		if (id != 0L) {
			Log.d(TAG, "query selected item position[" + checkedPosition
					+ "] id[" + id + "]");
			getListView().setItemChecked(checkedPosition, true);
		}

		mCurId = id;

		// FieldGuideEntryFragment details = (FieldGuideEntryFragment)
		// getFragmentManager().findFragmentByTag(FieldGuideEntryFragment.class.getName());
		FieldGuideEntryFragment details = new FieldGuideEntryFragment();
		if (mDualPane) {
			// Check what fragment is currently shown, replace if needed.
			if (details != null && details.getShownId() != id) {
				// fill fragment with correct data.
				details.setData(id);
			}
		}
		((OnFieldGuideItemSelectedListener) getActivity())
				.activateFieldGuideEntryFragment(details, checkedPosition, id,
						constraint);
	}

	public Uri getCursorUri() {
		Uri cursorUri;
		if (constraint != null) {
			cursorUri = Uri.withAppendedPath(
					FieldGuideEntry.CONTENT_URI_FILTER, Uri.encode(constraint));
		} else {
			cursorUri = FieldGuideEntry.CONTENT_URI;
		}
		return cursorUri;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		// This is called when a new Loader needs to be created. This
		// sample only has one Loader, so we don't care about the ID.
		// First, pick the base URI to use depending on whether we are
		// currently filtering.
		Uri cursorUri = getCursorUri();

		// Now create and return a CursorLoader that will take care of
		// creating a Cursor for the data being displayed.
		return new CursorLoader(getActivity(), cursorUri, null, null, null,
				null);
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		Log.d(TAG, "onLoadFinished");
		// Swap the new cursor in. (The framework will take care of closing the
		// old cursor once we return.)
		adapter.swapCursor(data);
		// getListView().setSelection(checkedPosition);
		// The list should now be shown.
		if (isResumed()) {
			setListShown(true);
		} else if (!isDetached()) {
			setListShownNoAnimation(true);
		}

	}
	
	

	public void onLoaderReset(Loader<Cursor> loader) {
		// This is called when the last Cursor provided to onLoadFinished()
		// above is about to be closed. We need to make sure we are no
		// longer using it.
		adapter.swapCursor(null);
	}

	
	@Override
	public void onPause() {
		Log.d(TAG,"setting top position ["+topPosition +"]");
		topPosition = getListView().getFirstVisiblePosition();
		super.onPause();
	}
	@Override
	public void onResume() {
		Log.d(TAG, "onResume: selected position: [" + getListView().getSelectedItemPosition()
				+ "], checked position:[" + checkedPosition + "] top position: ["+topPosition+"]");
		adapter.notifyDataSetChanged();
		getListView().setSelection(topPosition);
		Log.d(TAG, "onResume [" + getListView().getSelectedItemPosition() + "]");
		super.onResume();
	}
	@Override
	public boolean onClose() {
		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		Log.d(TAG, "onQueryTextSubmit [" + query + "]");
		Utils.hideKeyboard(this);
		return true;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		// Called when the action bar search text has changed. Update
		// the search filter, and restart the loader to do a new query
		// with this filter.
		Log.d(TAG, "onQueryTextChange [" + constraint + "]");
		constraint = !TextUtils.isEmpty(newText) ? newText : null;
		// checkedPosition=0;
		getLoaderManager().restartLoader(0, null, this);
		return true;
	}

	public int getPosition(View view) {
		int children = getListView().getChildCount();
		int position = 0;
		for (int i = 0; i < children; i++) {
			View child = getListView().getChildAt(i);
			if (child == view) {
				position = i;
				break;
			}
		}
		return position;
	}

	public void refresh() {
		// checkedPosition = getPosition((View)view.getParent());
		// Preferences.setInt(Preferences.LAST_FIELDGUIDE_POSITION,
		// checkedPosition);
		getLoaderManager().restartLoader(0, null, this);
		getListView().invalidateViews();
	}

	public Cursor getActualCursor() {
		Uri cursorUri = getCursorUri();
		Cursor cursor = (new MarineLifeContentProvider()).query(cursorUri);
		return cursor;
	}

	public void refreshProgress(int percentage, Cursor cursor) {
		Log.d(TAG, "Progress [" + percentage + "]");
		if (!isDetached()) {
			adapter.swapCursor(cursor);
			progressbar.setProgress(percentage);
			progressbar.refreshDrawableState();
		}
	}

	public void removeHeader() {
		if (!isDetached())
			header.setVisibility(View.GONE);
	}


}
