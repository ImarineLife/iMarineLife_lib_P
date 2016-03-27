package nl.imarinelife.lib;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionProvider;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import nl.imarinelife.lib.MainDrawer.SelectedNavigation;
import nl.imarinelife.lib.catalog.Catalog;
import nl.imarinelife.lib.divinglog.DivingLogIdentityEntryFragment;
import nl.imarinelife.lib.divinglog.DivingLogIdentityEntryFragment.OnDivingLogItemSelectedListener;
import nl.imarinelife.lib.divinglog.DivingLogListFragment;
import nl.imarinelife.lib.divinglog.DivingLogStayEntryFragment;
import nl.imarinelife.lib.divinglog.db.dive.Dive;
import nl.imarinelife.lib.divinglog.db.dive.DiveDbHelper;
import nl.imarinelife.lib.divinglog.db.dive.DiveProfilePartDbHelper;
import nl.imarinelife.lib.divinglog.sightings.DivingLogSightingsEntryFragment;
import nl.imarinelife.lib.divinglog.sightings.DivingLogSightingsListFragment;
import nl.imarinelife.lib.divinglog.sightings.Sighting;
import nl.imarinelife.lib.fieldguide.FieldGuideEntryFragment;
import nl.imarinelife.lib.fieldguide.FieldGuideEntryFragment.OnFieldGuideItemSelectedListener;
import nl.imarinelife.lib.fieldguide.FieldGuideListFragment;
import nl.imarinelife.lib.fieldguide.db.FieldGuideAndSightingsEntryDbHelper;
import nl.imarinelife.lib.fieldguide.db.FieldGuideEntry;
import nl.imarinelife.lib.utility.DataTextView;
import nl.imarinelife.lib.utility.ExpansionFileAccessHelper;

public class MainActivity extends Activity implements
		OnFieldGuideItemSelectedListener, OnDivingLogItemSelectedListener,
		DivingLogSightingsEntryFragment.OnDivingLogSightingsItemSelectedListener, FragmentManager.OnBackStackChangedListener {
	private static final String TAG = "MainActivity";

	public static final String FRAME1 = "contentframe_1";
	public static final String FRAME2 = "contentframe_2";

	public static final String KEY_BOOL_IDFROMDB = "IDSetfromDB";
	public static final String KEY_INT_CURRENTID = "currentDiveId";
	public static final String KEY_SER_DIVE = "dive";
	public static final String KEY_SELECTED = "selectedNavigation";
	public static final String KEY_CURRENTCATALOG = "currentCatalog";

	public static String DB_VALUE = "databaseValue";

	public static MainActivity me = null;

	public Dive currentDive = null;
	public int diveNrAtStart = 0;
	MainDrawer drawer = null;
	HashMap<String, Fragment> currentFragments = new HashMap<String, Fragment>();
	@SuppressWarnings("rawtypes")
	HashMap<String, Fragment> fragments = new HashMap<String, Fragment>();
	public HashMap<String, Cursor> cursors = new HashMap<String, Cursor>();
	boolean activelyReset = false;
	public Catalog currentCatalog = null;

	int lastDiveSightingsFilled = 0;

	@SuppressWarnings("rawtypes")
	public Fragment getFragment(String clazz) {
		return fragments.get(clazz);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		me = this;
		Log.d(TAG, "OnCreate");

		ExpansionFileAccessHelper.expansionFilesDeliveredCorrectly(); // this
																		// may
																		// initiate
																		// download
																		// in
																		// background
		setContentView(R.layout.navigation_drawer);

		// if we're being restored from a previous state,
		// or else we could end up with overlapping fragments.
		if (savedInstanceState != null) {
			currentDive = (Dive) savedInstanceState
					.getSerializable(KEY_SER_DIVE);
			diveNrAtStart = savedInstanceState.getInt(KEY_INT_CURRENTID);
			currentCatalog = (Catalog) savedInstanceState
					.getSerializable(KEY_CURRENTCATALOG);
			if (LibApp.getInstance().getCurrentCatalog() == null) {
				LibApp.getInstance().setCatalog(currentCatalog);
			}
			drawer = new MainDrawer(this);
			currentFragments.put(FRAME1, getFragmentManager()
					.findFragmentByTag(FRAME1));
			currentFragments.put(FRAME2, getFragmentManager()
					.findFragmentByTag(FRAME2));
			checkBackStackEntryCount();

			Log.d(TAG, currentFragments.toString());
			return;
		} else {
			activelyReset = true;
			activate((drawer != null ? drawer.getSelected() : null));
		}

		getFragmentManager().addOnBackStackChangedListener(this);

		setHomeButtonEnabled(true);

	}

	@Override
	public void onAttachFragment(Fragment fragment) {
		fragments.put(fragment.getClass().getSimpleName(), fragment);
	}

	public void activate(MainDrawer.SelectedNavigation selected) {
		if (drawer == null)
			drawer = new MainDrawer(this);
		if (selected == null) {
			selected = Preferences.getSelectedNavigation();
		}
		drawer.setTitle(selected);
		if (selected != null) {
			switch (selected) {
			case DIVINGLOG:
				handleDivingLog(selected);
				break;
			case FIELDGUIDE:
				handleFieldGuide(MainDrawer.SelectedNavigation.FIELDGUIDE);
				break;
			}
			Preferences.setSelectedNavigation(selected);
		} else {
			drawer.openDrawer();
			drawer.setOpenedBySwipe(false);
		}

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(KEY_SER_DIVE, currentDive);
		outState.putInt(KEY_INT_CURRENTID, diveNrAtStart);
		outState.putSerializable(KEY_SELECTED, drawer.getSelected());
		outState.putSerializable(KEY_CURRENTCATALOG, LibApp.getInstance()
				.getCurrentCatalog());

	}

	private void handleFieldGuide(SelectedNavigation selected) {
		Log.d(TAG, "activate[" + selected + "][" + getNumberOfPanes()
				+ "] handling FieldGuideFragment");
		// Create an instance of FieldGuideListFragment
		FieldGuideListFragment listFragment = (FieldGuideListFragment) fragments
				.get("FieldGuideListFragment");
		if (listFragment == null) {
			listFragment = new FieldGuideListFragment();
			fragments.put(listFragment.getClass().getSimpleName(), listFragment);
			listFragment.setArguments(getIntent().getExtras());
		}

		switch (getNumberOfPanes()) {
		case 1:
			FragmentTransaction transaction = getFragmentManager()
					.beginTransaction();
			transaction = addOrReplaceFragment(R.id.content_frame_1,
					transaction, fragments.get("FieldGuideListFragment"),
					FRAME1);
			Log.d(TAG, "handleFieldGuide - backstackcount ["
					+ getFragmentManager().getBackStackEntryCount()
					+ "]");
			if (activelyReset) {
				Log.d(TAG, "adding top to backstack");
				transaction.addToBackStack("top");
				activelyReset = false;
			}
			transaction.commit();
			checkBackStackEntryCount();
			break;
		case 2:
			FrameLayout listLayout = (FrameLayout) findViewById(R.id.content_frame_1);
			@SuppressWarnings("deprecation")
			RelativeLayout.LayoutParams listParams = new RelativeLayout.LayoutParams(
					300, RelativeLayout.LayoutParams.FILL_PARENT);
			listParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
					RelativeLayout.TRUE);
			listLayout.setLayoutParams(listParams);

			FrameLayout entryLayout = (FrameLayout) findViewById(R.id.content_frame_2);
			@SuppressWarnings("deprecation")
			RelativeLayout.LayoutParams entryParams = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.FILL_PARENT);
			entryParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,
					RelativeLayout.TRUE);
			entryParams.addRule(RelativeLayout.ALIGN_PARENT_TOP,
					RelativeLayout.TRUE);
			entryParams.addRule(RelativeLayout.RIGHT_OF, R.id.content_frame_1);
			entryLayout.setLayoutParams(entryParams);
			entryLayout.setVisibility(View.VISIBLE);

			FieldGuideEntryFragment entryFragment = new FieldGuideEntryFragment();
			entryFragment.setArguments(getIntent().getExtras());

			FragmentTransaction dualTransaction = getFragmentManager()
					.beginTransaction();
			dualTransaction = addOrReplaceFragment(R.id.content_frame_1,
					dualTransaction,
					fragments.get("FieldGuideListFragment"), FRAME1);
			dualTransaction = addOrReplaceFragment(R.id.content_frame_2,
					dualTransaction, entryFragment, FRAME2);
			dualTransaction.addToBackStack(null);
			dualTransaction.commit();
			checkBackStackEntryCount();
			break;
		}
	}

	private void handleDivingLog(SelectedNavigation selected) {
		Log.d(TAG, "activate[" + selected + "][" + getNumberOfPanes()
				+ "] handling DivingLogFragment");

		DivingLogListFragment divelistFragment = (DivingLogListFragment) fragments
				.get("DivingLogListFragment");
		if (divelistFragment == null) {
			divelistFragment = new DivingLogListFragment();
			fragments.put(divelistFragment.getClass().getSimpleName(), divelistFragment);
			divelistFragment.setArguments(getIntent().getExtras());
		}
		switch (getNumberOfPanes()) {
		case 1:
			FragmentTransaction transaction = getFragmentManager()
					.beginTransaction();
			transaction = addOrReplaceFragment(R.id.content_frame_1,
					transaction, divelistFragment, FRAME1);
			if (activelyReset) {
				Log.d(TAG, "adding top to backstack");
				transaction.addToBackStack("top");
				activelyReset = false;
			}
			transaction.commit();
			checkBackStackEntryCount();
			break;
		case 2:
			DivingLogIdentityEntryFragment logentryFragment = new DivingLogIdentityEntryFragment();
			logentryFragment.setArguments(getIntent().getExtras());
			FragmentTransaction dualTransaction = getFragmentManager()
					.beginTransaction();
			dualTransaction = addOrReplaceFragment(R.id.content_frame_1,
					dualTransaction, divelistFragment, FRAME1);
			dualTransaction = addOrReplaceFragment(R.id.content_frame_2,
					dualTransaction, logentryFragment, FRAME2);
			if (activelyReset) {
				Log.d(TAG, "adding top to backstack");
				dualTransaction.addToBackStack("top");
				activelyReset = false;
			}
			dualTransaction.commit();
			checkBackStackEntryCount();
			break;
		}
	}

	public FragmentTransaction addOrReplaceFragment(int contentFrame,
			FragmentTransaction transaction, Fragment fragment,
			String contentFrameName) {
		Log.d(TAG, contentFrameName + " " + contentFrame + " "
				+ fragment.getClass().getName());
		Fragment alreadyThere = getFragmentManager().findFragmentByTag(
				contentFrameName);
		if (fragment != null) {
			if (alreadyThere != null && alreadyThere.getView() != null) {
				Log.d(TAG, "replacing contentFrame[" + contentFrameName + "]");
				transaction = transaction.remove(alreadyThere);
				transaction = transaction.add(contentFrame, fragment,
						contentFrameName);
			} else {
				Log.d(TAG, "adding contentFrame[" + contentFrameName + "]");
				transaction = transaction.add(contentFrame, fragment,
						contentFrameName);
			}
			currentFragments.put(contentFrameName, fragment);
		}
		return transaction;
	}

	@Override
	public void activateFieldGuideEntryFragment(FieldGuideEntryFragment entry,
			int position, long fieldguideId, String constraint) {
		Log.d(TAG, "activateFieldGuideEntryFragment position[" + position
				+ "] id[" + fieldguideId + "] constraint[" + constraint + "]");
		fragments.put(entry.getClass().getSimpleName(), entry);
		int frameId = 0;
		String frameName = null;
		switch (getNumberOfPanes()) {
			case 1:
				frameId = R.id.content_frame_1;
				frameName = FRAME1;
				break;
			case 2:
				frameId = R.id.content_frame_2;
				frameName = FRAME2;
				break;
		}
		Bundle bundle = new Bundle();
		bundle.putLong(FieldGuideEntry.ID, fieldguideId);
		bundle.putString(FieldGuideListFragment.CONSTRAINT, constraint);
		bundle.putInt(FieldGuideListFragment.CHECKED_POSITION, position);

		entry.setArguments(bundle);
		FragmentTransaction transaction = getFragmentManager()
				.beginTransaction();
		transaction = addOrReplaceFragment(frameId, transaction, entry,
				frameName);
		transaction.addToBackStack(null);
		transaction.commit();
	}

	@Override
	public void activateDivingLogEntryFragment(long id, Dive dive) {
		Log.d(TAG, "activateDivingLogEntryFragment");
		Log.d(TAG, "activateDivingLogEntryFragment[" + id + "][" + dive + "]["
				+ getNumberOfPanes() + "]");
		currentDive = dive;

		boolean frame2ContainsDivingLogIdentityFragment = frame2Contains(DivingLogIdentityEntryFragment.class);

		if (fragments.get("DivingLogIdentityEntryFragment") == null
				|| !frame2ContainsDivingLogIdentityFragment) {
			DivingLogIdentityEntryFragment entry = new DivingLogIdentityEntryFragment();
			fragments.put(entry.getClass().getSimpleName(), entry);
			diveNrAtStart = (int) id;
			int frameId = 0;
			String frameName = null;
			switch (getNumberOfPanes()) {
			case 1:
				frameId = R.id.content_frame_1;
				frameName = FRAME1;
				break;
			case 2:
				frameId = R.id.content_frame_2;
				frameName = FRAME2;
				break;
			}
			Bundle bundle = new Bundle();
			bundle.putBoolean(KEY_BOOL_IDFROMDB, true);
			bundle.putInt(KEY_INT_CURRENTID, (int) id);
			bundle.putSerializable(KEY_SER_DIVE, dive);
			Log.d(TAG, "dive put in bundle: " + dive);
			entry.setArguments(bundle);
			FragmentTransaction transaction = getFragmentManager()
					.beginTransaction();
			transaction = addOrReplaceFragment(frameId, transaction, entry,
					frameName);
			transaction.addToBackStack(null);
			transaction.commit();
		} else {
			DivingLogIdentityEntryFragment entry = (DivingLogIdentityEntryFragment) currentFragments
					.get(FRAME2);
			entry.setData(dive, true);

		}
	}

	private boolean frame2Contains(@SuppressWarnings("rawtypes") Class class1) {
		if (getNumberOfPanes() == 1)
			return false;

		if (currentFragments.get(FRAME2) == null)
			return false;

		Fragment fragmentInFrame2 = currentFragments.get(FRAME2);
		if (fragmentInFrame2.getClass().equals(class1))
			return true;

		return false;
	}

	public static boolean areDiveSightingsEditable() {
		String catalog = me.currentDive.getCatalog();

		if (catalog.equals(LibApp.getCurrentCatalogName()))
			return true;
		else {
			String text = (LibApp.getCurrentResources()
					.getString(R.string.editing_sightings_notavailable_for_other_app))
					+ "[" + MainActivity.me.currentDive.getCatalog() + "]";
			Toast.makeText(MainActivity.me, text, Toast.LENGTH_LONG).show();
			return false;
		}
	}

	@Override
	public void activateDivingLogSightingsEntryFragment(
			DivingLogSightingsEntryFragment entry, int position,
			long fieldguideId, String constraint) {
		Log.d(TAG, "activateDivingLogSightingsEntryFragment [" + fieldguideId
				+ "][" + position + "][" + constraint + "]");
		fragments.put(entry.getClass().getSimpleName(), entry);
		int frameId = 0;
		String frameName = null;
		switch (getNumberOfPanes()) {
		case 1:
			frameId = R.id.content_frame_1;
			frameName = FRAME1;
			break;
		case 2:
			frameId = R.id.content_frame_2;
			frameName = FRAME2;
			break;
		}
		Bundle bundle = new Bundle();
		bundle.putLong(FieldGuideEntry.ID, fieldguideId);
		bundle.putLong(KEY_INT_CURRENTID, currentDive.getDiveNr());
		bundle.putString(DivingLogSightingsListFragment.CONSTRAINT, constraint);
		bundle.putInt(DivingLogSightingsListFragment.CHECKED_POSITION, position);
		bundle.putInt(DivingLogSightingsListFragment.FIELDGUIDE_ID,
				(int) fieldguideId);
		entry.setArguments(bundle);
		FragmentTransaction transaction = getFragmentManager()
				.beginTransaction();
		transaction = addOrReplaceFragment(frameId, transaction, entry,
				frameName);
		transaction.addToBackStack(null);
		transaction.commit();
	}

	public int getNumberOfPanes() {
		int screenSize = LibApp.getCurrentResources().getConfiguration().screenLayout
				& Configuration.SCREENLAYOUT_SIZE_MASK;
		int orientation = getScreenOrientation();
		int panes = 1;

		Log.d(TAG, "orientation: " + orientation);
		Log.d(TAG, "types: " + Configuration.ORIENTATION_LANDSCAPE + " "
				+ Configuration.ORIENTATION_PORTRAIT + " "
				+ Configuration.ORIENTATION_UNDEFINED);
		switch (screenSize) {
		case Configuration.SCREENLAYOUT_SIZE_XLARGE:
		case Configuration.SCREENLAYOUT_SIZE_LARGE:
			panes = 1;
			break;
		default:
			switch (orientation) {
			case Configuration.ORIENTATION_LANDSCAPE:
				panes = 1;
				break;
			default:
				panes = 1;
			}
			/*
			 * switch (orientation) { case Configuration.ORIENTATION_LANDSCAPE:
			 * break; default:
			 * 
			 * }
			 */
		}
		Log.d(TAG, "panes: " + panes);
		return panes;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		if (drawer != null)
			drawer.hideOrShowContentRelatedActionItems(menu);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		if (drawer != null && drawer.getActionBarDrawerToggle() != null) {
			drawer.getActionBarDrawerToggle().syncState();
		}
	}

	/*
	 * @Override public void onConfigurationChanged(Configuration newConfig) {
	 * super.onConfigurationChanged(newConfig);
	 * 
	 * if (drawer != null && drawer.getActionBarDrawerToggle() != null) {
	 * drawer.getActionBarDrawerToggle().onConfigurationChanged(newConfig); }
	 * 
	 * Log.d(TAG, "OnConfigurationChanged");
	 * setContentView(R.layout.navigation_drawer); boolean drawerOpen = (drawer
	 * != null && drawer.isOpen()) ? true : false; drawer = new
	 * MainDrawer(this); if (drawerOpen) { drawer.openDrawer(); }
	 * 
	 * // Checks the orientation of the screen if (newConfig.orientation ==
	 * Configuration.ORIENTATION_LANDSCAPE) { Toast.makeText(this,
	 * "landscape drawer[" + drawer.isOpen() + "]", Toast.LENGTH_SHORT).show();
	 * } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
	 * Toast.makeText(this, "portrait drawer[" + drawer.isOpen() + "]",
	 * Toast.LENGTH_SHORT).show(); } }
	 */

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Pass the event to ActionBarDrawerToggle, if it returns
		// true, then it has handled the app icon touch event
		int backstackEntryCount = getFragmentManager()
				.getBackStackEntryCount();
		Log.d(TAG, "onOptionsItemSelected - start: backstackEntryCount ["
				+ backstackEntryCount + "]");
		boolean toReturn = true;
		if (item.getItemId() == R.id.action_settings) {
			Intent settingsIntent = new Intent(this, SettingsActivity.class);
			startActivityForResult(settingsIntent, 0);
			return true;
		} else if (item.getItemId() == android.R.id.home) {
			boolean success = true;
			if (currentFragmentIsDiveRelated()) {
				success = saveDive(false);
			}
			if (success) {
				if (backstackEntryCount == 0) {
					// de reeds geselecteerde activiteit opnieuw activeren
					if (drawer != null) {
						activate(drawer.getSelected());
					} else {
						activate(MainDrawer.SelectedNavigation.DIVINGLOG);
					}
				} else if (backstackEntryCount > 0) {
					if (drawer == null || !drawer.isOpen()) {
						Log.d(TAG, "onOptionsItemSelected: poppingBackStack");
						getFragmentManager().popBackStackImmediate();
					}
					if (backstackEntryCount == 1 && drawer != null
							&& drawer.getActionBarDrawerToggle() != null) {
						drawer.getActionBarDrawerToggle()
								.onOptionsItemSelected(getMenuItem(item));
						if (drawer.isOpen()) {
							drawer.setOpenedBySwipe(false);
						}
					}
				}
			} else {
				toReturn = false;
			}
			backstackEntryCount = getFragmentManager()
					.getBackStackEntryCount();
			Log.d(TAG, "onOptionsItemSelected - end: backstackEntryCount ["
					+ backstackEntryCount + "]");
			checkBackStackEntryCount();

			return toReturn;

		}

		return super.onOptionsItemSelected(item);
	}

	/*
	 * @Override public void onBackPressed() { int backstackEntryCount =
	 * getFragmentManager().getBackStackEntryCount(); Log.d(TAG,
	 * "onBackPressed: backstackEntryCount [" + backstackEntryCount + "]"); if
	 * (backstackEntryCount == 1) drawer.openDrawer(); if (backstackEntryCount
	 * == 0) System.exit(0); super.onBackPressed(); }
	 */

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		int backstackEntryCount = getFragmentManager()
				.getBackStackEntryCount();
		int repeatCount = event.getRepeatCount();
		Log.d(TAG, "onKeyDown repeatCount[" + repeatCount
				+ "] backstackEntryCount[" + backstackEntryCount + "]");
		getFragmentManager().executePendingTransactions();
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (backstackEntryCount == 1
					&& (drawer == null || !drawer.isOpen())) {
				Log.d(TAG, "onKeyDown opening Drawer");
				getFragmentManager().popBackStackImmediate();
				if (drawer == null)
					drawer = new MainDrawer(this);
				drawer.openDrawer();
				drawer.setOpenedBySwipe(false);
				return false;
			} else if (backstackEntryCount == 0
					|| (backstackEntryCount == 1 && drawer.isOpen())) {
				System.exit(0);
				return true;
			} else {
				getFragmentManager().popBackStackImmediate();
				return true;
			}

		}

		return super.onKeyDown(keyCode, event);
	}

	private android.view.MenuItem getMenuItem(final MenuItem item) {
		return new android.view.MenuItem() {
			@Override
			public int getItemId() {
				return item.getItemId();
			}

			public boolean isEnabled() {
				return true;
			}

			@Override
			public boolean collapseActionView() {
				return false;
			}

			@Override
			public boolean expandActionView() {
				return false;
			}

			@Override
			public ActionProvider getActionProvider() {
				return null;
			}

			@Override
			public View getActionView() {
				return null;
			}

			@Override
			public char getAlphabeticShortcut() {
				return 0;
			}

			@Override
			public int getGroupId() {
				return 0;
			}

			@Override
			public Drawable getIcon() {
				return null;
			}

			@Override
			public Intent getIntent() {
				return null;
			}

			@Override
			public ContextMenuInfo getMenuInfo() {
				return null;
			}

			@Override
			public char getNumericShortcut() {
				return 0;
			}

			@Override
			public int getOrder() {
				return 0;
			}

			@Override
			public SubMenu getSubMenu() {
				return null;
			}

			@Override
			public CharSequence getTitle() {
				return null;
			}

			@Override
			public CharSequence getTitleCondensed() {
				return null;
			}

			@Override
			public boolean hasSubMenu() {
				return false;
			}

			@Override
			public boolean isActionViewExpanded() {
				return false;
			}

			@Override
			public boolean isCheckable() {
				return false;
			}

			@Override
			public boolean isChecked() {
				return false;
			}

			@Override
			public boolean isVisible() {
				return false;
			}

			@Override
			public android.view.MenuItem setActionProvider(
					ActionProvider actionProvider) {
				return null;
			}

			@Override
			public android.view.MenuItem setActionView(View view) {
				return null;
			}

			@Override
			public android.view.MenuItem setActionView(int resId) {
				return null;
			}

			@Override
			public android.view.MenuItem setAlphabeticShortcut(char alphaChar) {
				return null;
			}

			@Override
			public android.view.MenuItem setCheckable(boolean checkable) {
				return null;
			}

			@Override
			public android.view.MenuItem setChecked(boolean checked) {
				return null;
			}

			@Override
			public android.view.MenuItem setEnabled(boolean enabled) {
				return null;
			}

			@Override
			public android.view.MenuItem setIcon(Drawable icon) {
				return null;
			}

			@Override
			public android.view.MenuItem setIcon(int iconRes) {
				return null;
			}

			@Override
			public android.view.MenuItem setIntent(Intent intent) {
				return null;
			}

			@Override
			public android.view.MenuItem setNumericShortcut(char numericChar) {
				return null;
			}

			@Override
			public android.view.MenuItem setOnActionExpandListener(
					OnActionExpandListener listener) {
				return null;
			}

			@Override
			public android.view.MenuItem setOnMenuItemClickListener(
					OnMenuItemClickListener menuItemClickListener) {
				return null;
			}

			@Override
			public android.view.MenuItem setShortcut(char numericChar,
					char alphaChar) {
				return null;
			}

			@Override
			public void setShowAsAction(int actionEnum) {

			}

			@Override
			public android.view.MenuItem setShowAsActionFlags(int actionEnum) {
				return null;
			}

			@Override
			public android.view.MenuItem setTitle(CharSequence title) {
				return null;
			}

			@Override
			public android.view.MenuItem setTitle(int title) {
				return null;
			}

			@Override
			public android.view.MenuItem setTitleCondensed(CharSequence title) {
				return null;
			}

			@Override
			public android.view.MenuItem setVisible(boolean visible) {
				return null;
			}
		};
	}

	@Override
	public void onBackStackChanged() {
		checkBackStackEntryCount();
	}

	public void checkBackStackEntryCount() {
		int backStackEntryCount = getFragmentManager()
				.getBackStackEntryCount();
		Log.d(TAG,
				"onBackStackChanged/checkBackStackEntryCount - backstackEntryCount["
						+ backStackEntryCount + "]");
		if (backStackEntryCount > 0) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
			setHomeButtonEnabled(true);
		} else {
			getActionBar().setDisplayHomeAsUpEnabled(false);
			setHomeButtonEnabled(false);
		}
	}

	@TargetApi(14)
	public void setHomeButtonEnabled(boolean enable){
		getActionBar().setHomeButtonEnabled(enable);
	}

	private boolean currentFragmentIsDiveRelated() {
		Log.d(TAG, "currentFragmentIsDiveRelated");
		Fragment inFrame1 = currentFragments.get(FRAME1);
		Fragment inFrame2 = currentFragments.get(FRAME2);
		Log.d(TAG, "currentFragmentIsDiveRelated " + FRAME1 + "[" + inFrame1
				+ "] FRAME2[" + inFrame2 + "]");
		if (inFrame1 != null
				&& (inFrame1 instanceof DivingLogIdentityEntryFragment || inFrame1 instanceof DivingLogStayEntryFragment))
			return true;
		if (inFrame2 != null
				&& (inFrame2 instanceof DivingLogIdentityEntryFragment || inFrame2 instanceof DivingLogStayEntryFragment))
			return true;

		return false;
	}

	public boolean saveDive(boolean in) {
		if (currentDive.isChanged() && in) {
			Log.d(TAG, "saveDive  diveNrAtStart[" + diveNrAtStart + "] dive["
					+ currentDive + "]");
			if (currentDive != null
					&& currentDive.getDiveNr() != diveNrAtStart
					&& existsAlready(currentDive.getDiveNr(),
							currentDive.getCatalog())) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(
						LibApp.getCurrentResources().getString(
								R.string.exists_already_title))
						.setMessage(
								LibApp.getCurrentResources().getString(
										R.string.exists_already_tekst))
						.setCancelable(false)
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.dismiss();
									}
								});
				AlertDialog alert = builder.create();
				alert.show();
				return false;
			} else if (currentDive != null
					&& currentDive.getDiveNr() != diveNrAtStart) {
				deleteDiveSensuStricto(diveNrAtStart);
				updateProfilePartsWithNewDiveNr(diveNrAtStart,
						currentDive.getDiveNr());
				updateSightingsWithNewDiveNr(diveNrAtStart,
						currentDive.getDiveNr());
			}
			return saveDive(null);
		} else {
			Log.d(TAG, "saveDive  diveNrAtStart[" + diveNrAtStart + "] dive["
					+ currentDive.getDiveNr()
					+ "] not changed or going up, not saving");
			return true; // next action should be taken
		}
	}

	private void updateProfilePartsWithNewDiveNr(int diveNrAtStart, int diveNr) {
		Log.d(TAG, "updateProfilePartsWithNewDiveNr[" + diveNrAtStart + "][" + diveNr + "]");
		DiveProfilePartDbHelper helper = DiveProfilePartDbHelper
				.getInstance(this);
		helper.updateProfilePartsWithNewDiveNr(diveNrAtStart, diveNr);
	}

	private void updateSightingsWithNewDiveNr(int diveNrAtStart, int diveNr) {
		Log.d(TAG, "updateSightingsWithNewDiveNr[" + diveNrAtStart + "][" + diveNr + "]");
		FieldGuideAndSightingsEntryDbHelper helper = FieldGuideAndSightingsEntryDbHelper
				.getInstance(this);
		helper.updateSightingsWithNewDiveNr(diveNrAtStart, diveNr);
	}

	private void deleteDiveSensuStricto(int diveNrAtStart) {
		Log.d(TAG, "deleteDive[" + diveNrAtStart + "]");
		DiveDbHelper helper = DiveDbHelper.getInstance(this);
		helper.deleteDiveSensuStricto(diveNrAtStart, currentDive.getCatalog(),
				this);
		helper.close();
	}

	public void deleteSighting(Sighting sighting) {
		Log.d(TAG,"deleteSighting ["+sighting+"]");
		FieldGuideAndSightingsEntryDbHelper helper = FieldGuideAndSightingsEntryDbHelper
				.getInstance(this);
		helper.deleteSighting(sighting.diveNr, sighting.fieldguide_id);
	}

	private boolean existsAlready(int diveNr, String catalog) {
		if (catalog == null)
			catalog = LibApp.getCurrentCatalogName();
		DiveDbHelper helper = DiveDbHelper.getInstance(this);
		Cursor cursor = helper.fetchCursorForDive(diveNr);
		if (cursor != null && cursor.getCount() != 0) {
			cursor.moveToFirst();
			do {
				Log.d(TAG,
						"existsAlready diveNr["
								+ diveNr
								+ "] catalog["
								+ catalog
								+ "]  cursor diveNr["
								+ cursor.getString(DiveDbHelper.KEY_DIVENR_CURSORLOC)
								+ "] catalog["
								+ cursor.getString(DiveDbHelper.KEY_CATNAME_CURSORLOC)
								+ "]");
				return true;
			} while (cursor.moveToNext());
		}
		cursor.close();
		return false;
	}

	public boolean saveDive(Sighting sighting) {
		Log.d(TAG, "saveDive [" + currentDive.isChanged() + "][" + currentDive
				+ "] fieldguideEntry["
				+ (sighting == null ? "null" : sighting.fieldguide_id) + "]");
		if (sighting == null) {
			Calendar endOfToday = Calendar.getInstance();
			endOfToday.set(Calendar.HOUR_OF_DAY, 23);
			endOfToday.set(Calendar.MINUTE, 59);
			endOfToday.set(Calendar.SECOND, 59);
			if (currentDive.isChanged()) {
				if (currentDive.getLocation() == null
						|| currentDive.getLocation().getShowLocationName()
								.trim().equals("")
						|| currentDive.getDate() == 0
						|| currentDive.getTime() == 0) {
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle(
							LibApp.getCurrentResources().getString(
									R.string.required_fields_title))
							.setMessage(
									LibApp.getCurrentResources().getString(
											R.string.required_fields_tekst))
							.setCancelable(false)
							.setPositiveButton("OK",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											dialog.dismiss();
										}
									});
					AlertDialog alert = builder.create();
					alert.show();
					return false;
				}
				if (currentDive.getDate() > endOfToday.getTimeInMillis()) {
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle(LibApp.getCurrentResources().getString(R.string.not_after_today_title))
							.setMessage(
									LibApp.getCurrentResources().getString(
											R.string.not_after_today_tekst))
							.setCancelable(false)
							.setPositiveButton("OK",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											dialog.dismiss();
										}
									});
					AlertDialog alert = builder.create();
					alert.show();
					return false;
				} else {
					currentDive.save();
					currentDive.setChanged(false);
					diveNrAtStart = currentDive.getDiveNr();
					return true;
				}
			} else {
				return true;
			}
		} else {
			FieldGuideAndSightingsEntryDbHelper sightingsHelper = FieldGuideAndSightingsEntryDbHelper
					.getInstance(this);
			sightingsHelper.upsertSighting(sighting);
			return true;
		}

	}

	@Override
	protected void onResume() {
		ExpansionFileAccessHelper.getInstance().connect();
		super.onResume();
	};

	@Override
	protected void onPause() {
		ExpansionFileAccessHelper.getInstance().disconnect();
		super.onPause();
	}

	@Override
	protected void onStop() {
		ExpansionFileAccessHelper.getInstance().disconnect();
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "disconnecting ExpansionFileAccessHelper");
		ExpansionFileAccessHelper.getInstance().disconnect();
		// release all resources

		Log.d(TAG, "closing all cursors");
		for (String key : cursors.keySet()) {
			Cursor cursor = cursors.get(key);
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
				Log.d(TAG, "closed old Cursor[" + key + "]");
			}
		}
		Log.d(TAG, "closing alll dbhelpers");
		for (DbHelper helper : LibApp.getInstance().dbhelpers.values()) {
			helper.close();
		}
		super.onDestroy();
	}

	@SuppressWarnings("deprecation")
	public int getScreenOrientation() {
		Display getOrient = getWindowManager().getDefaultDisplay();
		int orientation = Configuration.ORIENTATION_UNDEFINED;
		if (getOrient.getWidth() == getOrient.getHeight()) {
			orientation = Configuration.ORIENTATION_SQUARE;
		} else {
			if (getOrient.getWidth() < getOrient.getHeight()) {
				orientation = Configuration.ORIENTATION_PORTRAIT;
			} else {
				orientation = Configuration.ORIENTATION_LANDSCAPE;
			}
		}
		return orientation;
	}

	public void onFieldGuideListHeaderClick(View view) {
		Log.d(TAG, "onFieldGuideListHeaderClick");
		int count = ((ViewGroup) view).getChildCount();
		for (int i = 0; i < count; i++) {
			View childView = ((ViewGroup) view).getChildAt(i);
			if (childView instanceof TextView && childView.getId()==R.id.header_fieldguide_list_groupname) {
				String groupAsShown = ((TextView) childView).getText()
						.toString();
				String group = ((childView instanceof DataTextView) && ((DataTextView) childView)
						.getData(DB_VALUE) != null) ? ((DataTextView) childView)
						.getData(DB_VALUE) : groupAsShown;
				if(group!=null && !group.isEmpty()) {
					Preferences.toggleListValue(
							Preferences.FIELDGUIDE_GROUPS_HIDDEN, group);
					Log.d(TAG, "Hidden groups [" + Preferences.getString(Preferences.FIELDGUIDE_GROUPS_HIDDEN, "") + "]");
					invalidateOptionsMenu();
				}
			}
		}
		((FieldGuideListFragment) fragments.get("FieldGuideListFragment"))
				.refresh();

	}

	public void onSightingsListHeaderClick(View view) {
		Log.d(TAG, "onSightingsListHeaderClick");
		if (view instanceof TextView) {
			String groupAsShown = ((TextView) view).getText().toString();
			String group = ((view instanceof DataTextView) && ((DataTextView) view)
					.getData(DB_VALUE) != null) ? ((DataTextView) view)
					.getData(DB_VALUE) : groupAsShown;
			if(group!=null && !group.isEmpty()) {
				Preferences.toggleListValue(Preferences.SIGHTINGS_GROUPS_HIDDEN,
						group);
			}
		}
		((DivingLogSightingsListFragment) fragments
				.get("DivingLogSightingsListFragment")).refresh();
	}

	public void onSightingsListHeaderDefaultClick(View view) {
		String value = ((TextView) view).getText().toString();
		Log.d(TAG, "onSightingsListHeaderDefaultClick[" + value + "]");
		TextView groupNameView = ((TextView) ((View) view.getParent())
				.findViewById(R.id.header_sightings_all_list_groupname));
		String groupAsShown = groupNameView.getText().toString();
		boolean isDataTextView = (groupNameView instanceof DataTextView);
		Map<String, String> map = (isDataTextView) ? ((DataTextView) groupNameView)
				.getData() : null;
		String groupName = (isDataTextView && ((DataTextView) groupNameView)
				.getData(DB_VALUE) != null) ? ((DataTextView) groupNameView)
				.getData(DB_VALUE) : groupAsShown;
		Log.d(TAG, "onSightingsListHeaderDefaultClick[" + groupAsShown + "]["
				+ groupName + "][" + isDataTextView + "][" + map + "]");
		((DivingLogSightingsListFragment) fragments
				.get("DivingLogSightingsListFragment")).changeAllValues(
				groupName, value);
	}

}
