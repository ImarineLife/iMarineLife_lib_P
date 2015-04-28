package nl.imarinelife.lib.divinglog.sightings;

//import  javax.mail.internet.InternetAddress;
import javax.mail.internet.InternetAddress;

import nl.imarinelife.lib.LibApp;
import nl.imarinelife.lib.MainActivity;
import nl.imarinelife.lib.MarineLifeContentProvider;
import nl.imarinelife.lib.Preferences;
import nl.imarinelife.lib.R;
import nl.imarinelife.lib.divinglog.db.dive.Dive;
import nl.imarinelife.lib.divinglog.sightings.DivingLogSightingsEntryPagerFragment.OnDivingLogSightingsItemSelectedListener;
import nl.imarinelife.lib.fieldguide.db.FieldGuideAndSightingsEntryDbHelper;
import nl.imarinelife.lib.utility.ConnectionDetector;
import nl.imarinelife.lib.utility.FilterCursorWrapper;
import nl.imarinelife.lib.utility.NoContextException;
import nl.imarinelife.lib.utility.SingletonCursor;
import nl.imarinelife.lib.utility.Utils;
import nl.imarinelife.lib.utility.dialogs.AreYouSureDialogFragment;
import nl.imarinelife.lib.utility.dialogs.AreYouSureDialogFragment.OnYesListener;
import nl.imarinelife.lib.utility.mail.MailSightingsSender;
import android.annotation.SuppressLint;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.support.v7.widget.SearchView;
import android.widget.Toast;


public class DivingLogSightingsListFragment extends ListFragment
		implements SearchView.OnQueryTextListener, SearchView.OnCloseListener,
		LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener {

	public boolean shownIdFromDb = false;

	private static final String TAG = "DivingLogSightsLstFrag";
	private static final String KEY_DISPLAY_OPT = "displayOptions";
	private static final String KEY_FIELDGUIDEID = "fieldguideId";
	private static final String KEY_DIVEID = "diveId";

	public static final String CONSTRAINT = "DivingLogSightingsConstraint";
	public static final String CHECKED_POSITION = "DivingLogSightingsCheckedPosition";
	public static final String TOP_POSITION = "DivingLogSightingsTopPosition";
	public static final String FIELDGUIDE_ID = "DivingLogSightingsFieldguideId";

	private boolean mDualPane;
	private long diveId = 0L;
	private long fieldguideId = 0L;
	private static Integer checkedPosition = null;
	private boolean editMode = false;
	public static Integer topPosition = null;

	private android.support.v7.widget.SearchView searchView;
	private String constraint;
	private ProgressBar progressbar;
	private View header;

	private DivingLogSightingsEntryPagerFragment details = null;

	private enum ActionBarMode {
		EDIT, SAVE
	}

	private ActionBarMode actionbarMode = ActionBarMode.EDIT;

	private FieldGuideAndSightingsEntryDbHelper dbHelper;
	private SimpleCursorAdapter adapter;

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
			shownIdFromDb = savedInstanceState.getBoolean(
					MainActivity.KEY_BOOL_IDFROMDB, false);
			fieldguideId = savedInstanceState.getLong(KEY_FIELDGUIDEID);
			diveId = savedInstanceState.getLong(KEY_DIVEID);
			if (diveId == 0L && MainActivity.me != null
					&& MainActivity.me.currentDive != null) {
				diveId = MainActivity.me.currentDive.getDiveNr();
			}
			checkedPosition = savedInstanceState.getInt(CHECKED_POSITION, 0);
			topPosition = savedInstanceState.getInt(TOP_POSITION, 0);
			Log.d(TAG, "top position gotten[" + topPosition + "]");
		}
	}

	@SuppressLint("NewApi")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView: checked position:[" + checkedPosition
				+ "], top position:[" + topPosition + "]");
		View layout = super.onCreateView(inflater, container,
				savedInstanceState);
		ListView lv = (ListView) layout.findViewById(android.R.id.list);
		ViewGroup parent = (ViewGroup) lv.getParent();

		// Remove ListView and add CustomView in its place
		int lvIndex = parent.indexOfChild(lv);
		parent.removeViewAt(lvIndex);
		LinearLayout mLinearLayout = (LinearLayout) inflater.inflate(
				R.layout.listview_sightings, container, false);
		parent.addView(mLinearLayout, lvIndex, lv.getLayoutParams());
		return layout;
	}

	@SuppressLint("NewApi")
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.d(TAG, "onActivityCreated ");
		super.onActivityCreated(savedInstanceState);

		if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
			initializeBundle(savedInstanceState);
		}
		MainActivity.me.setTitle(LibApp.getCurrentResources().getString(R.string.divinglog_page3));
		setHasOptionsMenu(true);

		int diveNr = MainActivity.me.currentDive.getDiveNr();
		header = getView().findViewById(R.id.fieldguide_header);
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

		Cursor newCursor = dbHelper.querySightingAsIsForDive(diveNr,
				MainActivity.me.currentDive.getCatalog());
		Cursor cursor = new FilterCursorWrapper(newCursor,
				Preferences.SIGHTINGS_GROUPS_HIDDEN,
				FieldGuideAndSightingsEntryDbHelper.KEY_GROUPNAME_CURSORLOC);

		setCursor(cursor);
		actionbarMode = ActionBarMode.EDIT;
		Log.d(TAG, "onActivityCreated cursorAsIs["
				+ SingletonCursor.getCursor().getCount() + "] diveNr[" + diveNr
				+ "]");
		OnItemClickListener listener = null;
		if (SingletonCursor.getCursor() == null
				|| SingletonCursor.getCursor().getCount() == 0 || editMode) {
			editMode = true;
			newCursor = dbHelper.queryFieldGuideFilledForDive(diveNr);
			cursor = new FilterCursorWrapper(newCursor,
					Preferences.SIGHTINGS_GROUPS_HIDDEN,
					FieldGuideAndSightingsEntryDbHelper.KEY_GROUPNAME_CURSORLOC);
			setCursor(cursor);
			adapter = getAdapterFieldGuideForDive(SingletonCursor.getCursor());
			actionbarMode = ActionBarMode.SAVE;
			getActivity().supportInvalidateOptionsMenu();
			listener = this;
		} else {
			adapter = getAdapterAsIsForDive(SingletonCursor.getCursor());

		}
		setListAdapter(adapter);
		getListView().setOnItemClickListener(listener);

		Log.d(TAG, "onActivityCreated - adapter set, actionbar modus ["
				+ actionbarMode + "]");
		Log.d(TAG,
				"onActivityCreated - cursor ["
						+ (SingletonCursor.getCursor() != null ? SingletonCursor
								.getCursor().getCount() : "null") + "]");

		mDualPane = ((MainActivity) getActivity()).getNumberOfPanes() == 2;
		/*
		 * if(checkedPosition!=null){
		 * getListView().setSelection(checkedPosition); }
		 */
		if (android.os.Build.VERSION.SDK_INT >= 11) {
			getListView().smoothScrollToPositionFromTop(topPosition, 0);
		} else {
			getListView().setSelection(topPosition);
		}

		Log.d(TAG, "onActivityCreated - checkedPosition[" + checkedPosition
				+ "] topPosition[" + topPosition + "]");

		if (mDualPane) {
			// In dual-pane mode, the list view highlights the selected item.
			getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			// Make sure our UI is in the correct state.
			showDetails(checkedPosition);
		}

	}

	private SimpleCursorAdapter getAdapterAsIsForDive(Cursor cursor) {
		// The desired columns to be bound
		String[] columns = new String[] {
				FieldGuideAndSightingsEntryDbHelper.KEY_CATNAME,
				FieldGuideAndSightingsEntryDbHelper.KEY_GROUPNAME,
				FieldGuideAndSightingsEntryDbHelper.KEY_COMMONNAME,
				FieldGuideAndSightingsEntryDbHelper.KEY_SIGHTING_VALUE,
				FieldGuideAndSightingsEntryDbHelper.KEY_CS_CHECKEDVALUES,
				FieldGuideAndSightingsEntryDbHelper.KEY_ROWID };

		// the XML defined views which the data will be bound to
		int[] to = new int[] { R.id.header_sightings_asis_list_amounts,
				R.id.header_sightings_asis_list_groupname,
				R.id.sighting_name_list, R.id.sighting_value_list,
				R.id.sighting_checkedvalue_list, R.id.sighting_image_list };

		adapter = new SightingsAsIsSimpleCursorAdapter(
				this.getActivity(),
				R.layout.listview_sightings_as_is_row, cursor, columns, to, 0);

		return adapter;
	}

	private SimpleCursorAdapter getAdapterFieldGuideForDive(Cursor cursor) {
		// The desired columns to be bound
		String[] columns = new String[] {
				FieldGuideAndSightingsEntryDbHelper.KEY_CATNAME,
				FieldGuideAndSightingsEntryDbHelper.KEY_GROUPNAME,
				FieldGuideAndSightingsEntryDbHelper.KEY_COMMONNAME,
				FieldGuideAndSightingsEntryDbHelper.KEY_SIGHTING_VALUE,
				FieldGuideAndSightingsEntryDbHelper.KEY_CS_CHECKEDVALUES,
				FieldGuideAndSightingsEntryDbHelper.KEY_ROWID };

		// the XML defined views which the data will be bound to
		int[] to = new int[] { R.id.header_sightings_all_list_amounts,
				R.id.header_sightings_all_list_groupname,
				R.id.sighting_name_list, R.id.sighting_value_list,
				R.id.sighting_checkedvalue_list, R.id.sighting_image_list };

		adapter = new SightingsFieldGuideSimpleCursorAdapter(
				this.getActivity(),
				R.layout.listview_sightings_fieldguide_row, cursor, columns,
				to, 0, MainActivity.me.currentDive);

		return adapter;
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		Log.d(TAG, "onPrepareOptionsMenu");

		menu.clear();
		getActivity().getMenuInflater().inflate(
				R.menu.sightings_list, menu);

		MenuItem editItem = menu.findItem(R.id.sightings_edit);
		MenuItem saveItem = menu.findItem(R.id.sightings_send);
		if (actionbarMode == ActionBarMode.SAVE) {
			saveItem.setVisible(true);
			editItem.setVisible(false);
		} else {
			saveItem.setVisible(false);
			editItem.setVisible(true);
		}

		MenuItem collapseItem = menu.findItem(R.id.sightings_collapse);
		MenuItem expandItem = menu.findItem(R.id.sightings_expand);
		if (Preferences.getBoolean(Preferences.SIGHTINGS_COLLAPSED_LAST, false)) {
			collapseItem.setVisible(false);
			expandItem.setVisible(true);
		} else {
			collapseItem.setVisible(true);
			expandItem.setVisible(false);
		}

		MenuItem searchItem = menu.findItem(R.id.sightings_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        if(searchView!=null) {
            searchView.setSubmitButtonEnabled(true);
            searchView.setOnQueryTextListener(this);
        }
		super.onPrepareOptionsMenu(menu);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(TAG, "onOptionsItemSelected: [" + item.getItemId() + "]");

		if (item.getItemId() == R.id.sightings_edit) {
			if (MainActivity.areDiveSightingsEditable()) {
				Cursor newCursor = dbHelper
						.queryFieldGuideFilledForDive((int) diveId);
				Cursor cursor = new FilterCursorWrapper(
						newCursor,
						Preferences.SIGHTINGS_GROUPS_HIDDEN,
						FieldGuideAndSightingsEntryDbHelper.KEY_GROUPNAME_CURSORLOC);
				setCursor(cursor);

				adapter = getAdapterFieldGuideForDive(SingletonCursor
						.getCursor());
				setListAdapter(adapter);
				getListView().setOnItemClickListener(this);
				actionbarMode = ActionBarMode.SAVE;
				getActivity().supportInvalidateOptionsMenu();
				editMode = true;
				return true;
			}
		} else if (item.getItemId() == R.id.sightings_send) {
			String text = null;
			String name = Preferences.getString(Preferences.USER_NAME, "");
			String code = Preferences.getString(Preferences.USER_CODE, "");
			if (MainActivity.me.currentDive.isSentAlready()
					|| SightingsSender
							.isNowBeingSent(MainActivity.me.currentDive
									.getDiveNr())) {
				text = (LibApp.getCurrentResources()
						.getString(R.string.send_prerequisite_notsent));
			} else if (name.trim().length() == 0 && code.trim().length() == 0  && !LibApp.getInstance().getCurrentCatalog().isCodeHidden()) {
				text = (LibApp.getCurrentResources()
						.getString(R.string.send_prerequisite_name_and_code));
			} else if (name.trim().length() == 0) {
				text = (LibApp.getCurrentResources()
						.getString(R.string.send_prerequisite_name));
			} else if (code.trim().length() == 0 && !LibApp.getInstance().getCurrentCatalog().isCodeHidden()) {
				text = (LibApp.getCurrentResources()
						.getString(R.string.send_prerequisite_code));
			} else {
				boolean sendToMeInCC;
				try {
					sendToMeInCC = Preferences
							.getBooleanFromDefaultSharedPreferences(Preferences.SEND_ME);
				} catch (NoContextException e1) {
					sendToMeInCC = true;
				}
				String cc;
				if (sendToMeInCC) {
					cc = Preferences.getString(Preferences.USER_EMAIL, null);
					if (cc != null && cc.trim().length() > 0) {
						try {
							new InternetAddress(cc);
						} catch (Exception e) {
							sendToMeInCC = false;
							text = (LibApp.getCurrentResources()
									.getString(R.string.send_mailadres_not_ok))
									+ "["
									+ MainActivity.me.currentDive.getCatalog()
									+ "]";
						}
					} else {
						text = (LibApp.getCurrentResources()
								.getString(R.string.send_mailadres_null))
								+ "["
								+ MainActivity.me.currentDive.getCatalog()
								+ "]";
						sendToMeInCC = false;
					}
				}
			}

			if (text == null
					&& !ConnectionDetector
							.isConnectingToInternet(getActivity())) {
				text = (LibApp.getCurrentResources()
						.getString(R.string.send_prerequisite_internet));
			}
			if (text != null) {
				Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
			} else {
				Log.d(TAG, "sightings send");
				AreYouSureDialogFragment fragment = getAreYouSureToSendFragment();
				FragmentTransaction ft = (getActivity())
						.getSupportFragmentManager().beginTransaction();
				ft.addToBackStack(null);
				fragment.show(ft, "adddialog");
			}
		} else if (item.getItemId() == R.id.sightings_expand) {
			Preferences.setString(Preferences.SIGHTINGS_GROUPS_HIDDEN, "");
			Preferences.setBoolean(Preferences.SIGHTINGS_COLLAPSED_LAST, false);
			getActivity().supportInvalidateOptionsMenu();
			refresh();
			return true;

		} else if (item.getItemId() == R.id.sightings_collapse) {
			if (LibApp.getCurrentCatalogName().equals(
					MainActivity.me.currentDive.getCatalog())) {
				Preferences.setString(Preferences.SIGHTINGS_GROUPS_HIDDEN,
						LibApp.getCurrentCatalogGroups());
			} else {
				if (dbHelper == null) {
					dbHelper = FieldGuideAndSightingsEntryDbHelper
							.getInstance(this.getActivity());
				}
				if (MainActivity.me != null
						&& MainActivity.me.currentDive != null) {
					Preferences.setString(Preferences.SIGHTINGS_GROUPS_HIDDEN,
							dbHelper.getGroupsFromSightingsAsIsForDive(
									MainActivity.me.currentDive.getDiveNr(),
									MainActivity.me.currentDive.getCatalog()));
				}
			}
			Preferences.setBoolean(Preferences.SIGHTINGS_COLLAPSED_LAST, true);
			getActivity().supportInvalidateOptionsMenu();
			refresh();
			return true;
		}
		return super.onOptionsItemSelected(item);

	}

	private AreYouSureDialogFragment getAreYouSureToSendFragment() {
		final int layoutId;
		final int textViewId;
		final int yesbuttonId;
		final int nobuttonId;
		layoutId = R.layout.general_yoursuretext_dialog;
		textViewId = R.id.general_textview_id;
		yesbuttonId = R.id.general_yes_button_id;
		nobuttonId = R.id.general_no_button_id;

		@SuppressWarnings("serial")
		OnYesListener yesListener = new OnYesListener() { 

			@Override
			public void onYes() {
				SightingsSender sender = new MailSightingsSender();
				sender.sendSightingsAsynchronously(MainActivity.me.currentDive,
						getActivity());
			}
		};

		String dialogtxt = LibApp.getCurrentResources().getString(
				R.string.send_are_you_sure);
		AreYouSureDialogFragment frag = AreYouSureDialogFragment.newInstance(
				dialogtxt, layoutId, textViewId, yesbuttonId, nobuttonId,
				DialogFragment.STYLE_NO_TITLE, R.style.iMarineLifeDialogTheme);
		frag.setOnYesListener(yesListener);
		return frag;

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.d(TAG, "onSaveInstanceState");
		outState.putLong(KEY_FIELDGUIDEID, fieldguideId);
		outState.putInt(KEY_DISPLAY_OPT,MainActivity.me
                .getSupportActionBar().getDisplayOptions());
		outState.putInt(CHECKED_POSITION, checkedPosition);
		Log.d(TAG, "storing top position[" + topPosition + "]");
		outState.putInt(TOP_POSITION, topPosition);
		outState.putLong(KEY_DIVEID, diveId);
	}

	public static void setTopPosition(int position) {
		Log.d(TAG, "topPosition[" + position + "]");
		topPosition = position;
	}

	public static void setCurrentPosition(int position) {
		Log.d(TAG, "currentPosition[" + position + "]");
		checkedPosition = position;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
		Log.d(TAG, "OnItemClick [" + position + "] hiding keyboard");
		Utils.hideKeyboard(this);
		checkedPosition = position;
		topPosition = getListView().getFirstVisiblePosition();
		setSelection(position);
		// setCursor(adapter.getCursor());
		if (SingletonCursor.getCursor() != null
				&& SingletonCursor.getCursor().getCount() >= position
				&& !SingletonCursor.getCursor().isClosed()) {
			SingletonCursor.getCursor().moveToPosition(checkedPosition);
			fieldguideId = SingletonCursor
					.getCursor()
					.getInt(FieldGuideAndSightingsEntryDbHelper.KEY_FIELDGUIDE_ID_CURSORLOC);
		}
		showDetails(fieldguideId);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		setSelection(position);
	}

	/**
	 * Helper function to show the details of a selected item, either by
	 * displaying a fragment in-place in the current UI, or starting a whole new
	 * activity in which it is displayed.
	 */
	void showDetails(long fieldguideId) {
		// We can display everything in-place with fragments, so update
		// the list to highlight the selected item and show the data.
		if (fieldguideId != 0L) {
			Log.d(TAG, "query selected item " + checkedPosition + " "
					+ fieldguideId);
			getListView().setItemChecked(checkedPosition, true);
		}

		details = new DivingLogSightingsEntryPagerFragment();

		((OnDivingLogSightingsItemSelectedListener) getActivity())
				.activateDivingLogSightingsEntryFragment(details,
						checkedPosition, fieldguideId, constraint);
	}

	public Uri getCursorUri() {
		Uri cursorUri;
		if (editMode) {
			if (constraint != null) {
				cursorUri = Uri.withAppendedPath(
						Sighting.CONTENT_URI_DIVE_FLESHEDOUT_FILTERED, ""
								+ diveId + MarineLifeContentProvider.SEPARATOR
								+ Uri.encode(constraint));
			} else {
				cursorUri = Uri.withAppendedPath(
						Sighting.CONTENT_URI_DIVE_FLESHEDOUT, "" + diveId);
			}
		} else {
			if (constraint != null) {
				cursorUri = Uri.withAppendedPath(
						Sighting.CONTENT_URI_DIVE_ASIS_FILTERED,
						"" + diveId + MarineLifeContentProvider.SEPARATOR
								+ Uri.encode(constraint));
			} else {
				cursorUri = Uri.withAppendedPath(
						Sighting.CONTENT_URI_DIVE_ASIS, "" + diveId);
			}
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
		// Swap the new cursor in. (The framework will take care of closing the
		// old cursor once we return.)
		Log.d(TAG, "onLoadFinished");
		Log.d(TAG,
				"onLoadFinished SingletonCursor["
						+ (SingletonCursor.getCursor() == null ? "null"
								: SingletonCursor.getCursor().getCount()) + "]");
		if (SingletonCursor.getCursor() != null
				&& !SingletonCursor.getCursor().isClosed()) {
			adapter.swapCursor(SingletonCursor.getCursor()); // necessary to
																// tell
																// the adapter
																// that
																// something
																// changed
		}

		Log.d(TAG,
				"onLoadFinished data["
						+ (data == null ? "null" : data.getCount()) + "]");
		if (data != null && data.getCount() != 0 && !data.isClosed()) {
			Log.d(TAG, "onLoadFinished setting Cursor");
			setCursor(data, false); // necessary to actually use the new cursor
		}
		Log.d(TAG,
				"onLoadFinished SingletonCursor["
						+ (SingletonCursor.getCursor() == null ? "null"
								: SingletonCursor.getCursor().getCount()) + "]");

		if (isResumed()) {
			setListShown(true);
		} else {
			if (!isDetached())
				setListShownNoAnimation(true);
		}

	}

	public void onLoaderReset(Loader<Cursor> loader) {
		// This is called when the last Cursor provided to onLoadFinished()
		// above is about to be closed. We need to make sure we are no
		// longer using it.
		adapter.swapCursor(null);
		SingletonCursor.swapCursor(null, false);

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

	public void setCursor(Cursor newCursor) {
		setCursor(newCursor, true);
	}

	public void setCursor(Cursor newCursor, boolean closeOldCursor) {
		SingletonCursor.swapCursor(newCursor, closeOldCursor);

	}

	public Cursor getActualCursor() {
		Uri cursorUri = getCursorUri();
		Cursor cursor = (new MarineLifeContentProvider()).query(cursorUri);
		return cursor;
	}

	public void refreshProgress(int percentage, Cursor cursor) {
		Log.d(TAG, "Progress [" + percentage + "]");
		if (!isDetached()) {
			SingletonCursor.swapCursor(cursor, false);
			progressbar.setProgress(percentage);
			progressbar.refreshDrawableState();
		}
	}

	public void removeHeader() {
		if (!isDetached())
			header.setVisibility(View.GONE);
	}

	@Override
	public void onPause() {
		Log.d(TAG, "setting top position [" + topPosition + "]");
		topPosition = getListView().getFirstVisiblePosition();
		super.onPause();
	}

	@Override
	public void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestoy");
		super.onDestroy();
	}

	@Override
	public void onDestroyView() {
		Log.d(TAG, "onDestroyView");
		super.onDestroyView();
	}

	@Override
	public void onStart() {
		Log.d(TAG, "onStart");
		super.onStart();
	}

	@Override
	public void onResume() {
		Log.d(TAG, "onResume: selected position: ["
				+ getListView().getSelectedItemPosition()
				+ "], checked position:[" + checkedPosition
				+ "] top position: [" + topPosition + "]");
		adapter.notifyDataSetChanged();
		getListView().setSelection(topPosition);
		Log.d(TAG, "onResume [" + getListView().getSelectedItemPosition() + "]");
		super.onResume();
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
		getLoaderManager().restartLoader(0, null, this);
		getListView().invalidateViews();

	}

	public void changeAllValues(String groupName, String value) {
		dbHelper.updateOrCreateEntriesForGroupInDive((int) diveId, groupName,
				value);
		refresh();
	}

}
