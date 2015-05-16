package nl.imarinelife.lib.divinglog;

import nl.imarinelife.lib.LibApp;
import nl.imarinelife.lib.MainActivity;
import nl.imarinelife.lib.R;
import nl.imarinelife.lib.divinglog.DivingLogIdentityEntryFragment.OnDivingLogItemSelectedListener;
import nl.imarinelife.lib.divinglog.db.dive.Dive;
import nl.imarinelife.lib.divinglog.db.dive.DiveDbHelper;
import nl.imarinelife.lib.divinglog.db.dive.DiveSimpleCursorAdapter;
import nl.imarinelife.lib.divinglog.sightings.DivingLogSightingsListFragment;
import nl.imarinelife.lib.utility.Utils;

import android.annotation.SuppressLint;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;


public class DivingLogListFragment extends ListFragment implements
        SearchView.OnQueryTextListener, SearchView.OnCloseListener,
		LoaderManager.LoaderCallbacks<Cursor> {

	public static final String TAG = "DivingLogListFragment";
	private static final String KEY_DISPLAY_OPT = "displayOptions";
	private static final String KEY_CURRENTPOSITION = "currentPosition";
	private static final String KEY_TOP_POSITION = "DivingLogSightingsTopPosition";
	

	boolean mDualPane;
	long mCurId = 0L;
	int checkedPosition = 0;
	int topPosition = 0;

	private SearchView searchView;
	private DiveDbHelper dbHelper;
	private DiveSimpleCursorAdapter dataAdapter;
	private String constraint;
	private Cursor cursor;
	EditText editText;
	View header;

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
			checkedPosition = savedInstanceState.getInt(KEY_CURRENTPOSITION, 0);
			topPosition = savedInstanceState.getInt(KEY_TOP_POSITION,0);
			Log.d(TAG,"top position gotten["+topPosition+"]");
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// Inflate the layout for this fragment
		// Getting adapter
		Log.d(TAG, "onActivityCreated ");

		/*
		 * header = getActivity().findViewById(R.layout.
		 * listview_diving_log_headerifnul);
		 */
		header = (View) getActivity().getLayoutInflater().inflate(
				R.layout.listview_diving_log_headerifnul, null);
		setListAdapter(null);

		displayListView();
		MainActivity.me.setTitle(LibApp.getCurrentResources().getString(R.string.divinglog));
		setHasOptionsMenu(true);
		mDualPane = ((MainActivity) getActivity()).getNumberOfPanes() == 2;

		if (savedInstanceState != null) {
			// Restore last state for checked position.
			mCurId = savedInstanceState.getLong(DiveSimpleCursorAdapter.ID);
			// restore actionbar displayoptions (like up icon)
			int savedDisplayOpt = savedInstanceState.getInt(KEY_DISPLAY_OPT);
			if (savedDisplayOpt != 0) {
				MainActivity.me.getActionBar().setDisplayOptions(
						savedDisplayOpt);
			}
			if (android.os.Build.VERSION.SDK_INT >= 11) {
				getListView().smoothScrollToPositionFromTop(topPosition, 0);
			}else{
				getListView().setSelection(topPosition);
			}
		
		} else {
			getListView().setSelection(getListView().getBottom());
		}

		if (mDualPane) {
			// In dual-pane mode, the list view highlights the selected item.
			getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			// Make sure our UI is in the correct state.
			showDetails(null);
		}

	}

	private void displayListView() {
		Log.d(TAG, "displayListView ");

		dbHelper = DiveDbHelper.getInstance(this.getActivity());

		// dbHelper.addOneEntry(this.getActivity());

		cursor = dbHelper.fetchAllCursor();
		MainActivity.me.cursors.put(TAG, cursor);
		//DiveDbHelper.logAllDatabaseContent(LibApp.getContext());

		if (cursor == null || cursor.getCount() == 0) {
			if (getListView().getHeaderViewsCount() == 0) {
				getListView().addHeaderView(header);
			}
		} else {
			if (getListView().getHeaderViewsCount() > 0) {
				getListView().removeHeaderView(header);
			}
		}

		// The desired columns to be bound
		String[] columns = new String[] { DiveDbHelper.KEY_DIVENR,
				DiveDbHelper.KEY_LOCATIONNAME, DiveDbHelper.KEY_DATE,
				DiveDbHelper.KEY_TIME };

		// the XML defined views which the data will be bound to
		int[] to = new int[] { R.id.text_diving_log_divennr,
				R.id.text_diving_log_divelocationname,
				R.id.text_diving_log_divingdate,
				R.id.text_diving_log_divingtime };

		// create the adapter using the cursor pointing to the desired data
		// as well as the layout information
		dataAdapter = new DiveSimpleCursorAdapter(this.getActivity(),
				R.layout.listview_diving_log_row, cursor, columns, to, 0);

		setListAdapter(dataAdapter);

		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.
		Loader<Object> loader = getLoaderManager().getLoader(0);
		if (loader != null && !loader.isReset()) {
			getLoaderManager().restartLoader(0, getArguments(), this);
		} else {
			getLoaderManager().initLoader(0, getArguments(), this);
		}

	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		Log.d(TAG, "onPrepareOptionsMenu");
		menu.clear();
		getActivity().getMenuInflater().inflate(
				R.menu.diving_log_list, menu);
		MenuItem searchItem = menu.findItem(R.id.diving_log_search);
		searchView = new SearchView(getActivity());
		searchItem.setActionView(searchView);
		if(searchView!=null) {
            searchView.setSubmitButtonEnabled(true);
            searchView.setOnQueryTextListener(this);
			Utils.setSearchTextColour(searchView, getActivity().getResources());
		}

		super.onPrepareOptionsMenu(menu);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(TAG, "onOptionsItemSelected: [" + item.getItemId() + "]");
		if (item.getItemId() == R.id.diving_log_add) {
			MainActivity.me.currentDive = null;
			DivingLogIdentityEntryFragment fragment = new DivingLogIdentityEntryFragment();
			fragment.setArguments(getActivity().getIntent().getExtras());
			switch (((MainActivity) getActivity()).getNumberOfPanes()) {
			case 1:
				FragmentTransaction transaction = getActivity()
						.getFragmentManager().beginTransaction();
				transaction = ((MainActivity) getActivity())
						.addOrReplaceFragment(R.id.content_frame_1,
								transaction, fragment, MainActivity.FRAME1);
				transaction.addToBackStack(null);
				transaction.commit();
				return true;
			case 2:
				FragmentTransaction transaction2 = getActivity()
						.getFragmentManager().beginTransaction();
				transaction = ((MainActivity) getActivity())
						.addOrReplaceFragment(R.id.content_frame_2,
								transaction2, fragment, MainActivity.FRAME2);
				// transaction2.addToBackStack(null);
				transaction2.commit();
				return true;
			}

		}
		/*
		 * if (item.getItemId() == R.id.diving_log_remove) {
		 * 
		 * }
		 */
		return super.onOptionsItemSelected(item);

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(DiveSimpleCursorAdapter.ID, mCurId);
		outState.putInt(KEY_DISPLAY_OPT, MainActivity.me
				.getActionBar().getDisplayOptions());
		outState.putInt(KEY_CURRENTPOSITION, checkedPosition);
		Log.d(TAG, "storing top position["+topPosition+"]");
		outState.putInt(KEY_TOP_POSITION, topPosition);
		
	}

	@Override
	public void onListItemClick(ListView listView, View v, int position, long id) {
		// Get the cursor, positioned to the corresponding row in the
		// result set
		Cursor cursor = (Cursor) listView.getItemAtPosition(position);
		if (cursor != null) {
			Log.d(TAG, "position: " + position);
			Log.d(TAG,
					"divenr: "
							+ cursor.getInt(DiveDbHelper.KEY_DIVENR_CURSORLOC));
			setSelection(position);
			DivingLogSightingsListFragment.setCurrentPosition(0);
			DivingLogSightingsListFragment.setTopPosition(0);
			checkedPosition = position;
			topPosition = getListView().getFirstVisiblePosition();
			Utils.hideKeyboard(this);
			if (cursor != null) {
				Cursor oldCursor = MainActivity.me.cursors.get(TAG);
				if (oldCursor != cursor) {
					oldCursor.close();
					oldCursor = null;
					MainActivity.me.cursors.put(TAG, cursor);
				}
				showDetails(cursor);
				// cursor.close();
			}
		}
	}

	/**
	 * Helper function to show the details of a selected item, either by
	 * displaying a fragment in-place in the current UI, or starting a whole new
	 * activity in which it is displayed.
	 */
	void showDetails(Cursor cursor) {
		// We can display everything in-place with fragments, so update
		// the list to highlight the selected item and show the data.

		int position = 0;
		if (cursor != null) {
			position = cursor.getPosition();
			Log.d(TAG, "selected item " + position);
			Log.d(TAG,
					"divenr: "
							+ cursor.getInt(DiveDbHelper.KEY_DIVENR_CURSORLOC));
		}

		Dive dive = dbHelper.getDiveFromCursor(cursor,
				this.getActivity());
		if (dive != null) {
			MainActivity.me.currentDive = dive;
			Log.d(TAG, "showDetails dive[" + dive + "]");
			if (dive != null) {
				mCurId = dive.getDiveNr();
				Log.d(TAG, "showDetails [" + mCurId + "]");
				((OnDivingLogItemSelectedListener) getActivity())
						.activateDivingLogEntryFragment(mCurId, dive);
			}

		}

	}

	@Override
	public void onPause() {
		Log.d(TAG,"setting top position ["+topPosition +"]");
		topPosition = getListView().getFirstVisiblePosition();
		super.onPause();
	}

	@Override
	public void onStop() {
		if (dbHelper != null)
			dbHelper.close();
		dbHelper = null;

		super.onStop();
	}

	@Override
	public void onDestroy() {
		if (dbHelper != null)
			dbHelper.close();
		dbHelper = null;

		if (cursor != null) {
			Log.d(TAG, "onDestroy cursor closing");
			cursor.close();
		}

		super.onDestroy();
	}

	public void onStart() {
		dbHelper = DiveDbHelper.getInstance(getActivity());
		super.onStart();
	}

	public void onResume() {
		getListView().setSelection(topPosition);
		super.onResume();
	}

	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// This is called when a new Loader needs to be created. This
		// sample only has one Loader, so we don't care about the ID.
		// First, pick the base URI to use depending on whether we are
		// currently filtering.
		Uri baseUri;
		if (constraint != null) {
			baseUri = Uri.withAppendedPath(Dive.CONTENT_URI_FILTER,
					Uri.encode(constraint));
		} else {
			baseUri = Dive.CONTENT_URI;
		}

		// Now create and return a CursorLoader that will take care of
		// creating a Cursor for the data being displayed.
		return new CursorLoader(getActivity(), baseUri, null, null, null, null);
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		Log.d(TAG, "onLoadFinished");
		// Swap the new cursor in. (The framework will take care of closing the
		// old cursor once we return.)
		dataAdapter.swapCursor(data);
	}

	public void onLoaderReset(Loader<Cursor> loader) {
		Log.d(TAG, "onLoaderReset");
		// This is called when the last Cursor provided to onLoadFinished()
		// above is about to be closed. We need to make sure we are no
		// longer using it.
		Log.d(TAG, "onLoaderReset cursor null");
		dataAdapter.swapCursor(null);
	}

	@Override
	public boolean onClose() {
		Log.d(TAG, "fragment closing");
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
		getLoaderManager().restartLoader(0, null, this);
		return true;
	}

}
