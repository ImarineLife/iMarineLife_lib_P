package nl.imarinelife.lib;

import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

public class MainDrawer {

	public enum SelectedNavigation {
		FIELDGUIDE, DIVINGLOG;
		public SelectedNavigation getValue(String selected) {
			if (FIELDGUIDE.toString().equals(selected)) {
				return FIELDGUIDE;
			} else if (DIVINGLOG.toString().equals(selected)) {
				return DIVINGLOG;
			}
			return null;
		}
	};

	private static String			TAG					= "MainDrawer";

	private MainActivity			mainActivity		= null;
	private SelectedNavigation		selectedNavigation	= null;
	private Button					fieldGuideButton;
	private Button					divingLogButton;
	private EditText				nameEditText;
	private EditText				emailEditText;
	private EditText				codeEditText;

	private View					drawer;
	private DrawerLayout			mDrawerLayout;
	private ActionBarDrawerToggle	mDrawerToggle;
	private boolean					openedBySwipe		= false;
	
	public boolean isOpenedBySwipe() {
		return openedBySwipe;
	}

	public void setOpenedBySwipe(boolean openedBySwipe) {
		this.openedBySwipe = openedBySwipe;
	}

	private OnClickListener			fieldGuideButtonOnClickListener	= new OnClickListener() {
																		public void onClick(View clickedButton) {
																			Log.d(TAG,
																				"FieldGuide button clicked");
																			selectedNavigation = SelectedNavigation.FIELDGUIDE;
																			mDrawerLayout.closeDrawer(drawer);
																			mainActivity.getFragmentManager().popBackStackImmediate(null,
																				FragmentManager.POP_BACK_STACK_INCLUSIVE);
																			mainActivity.activelyReset = true;
																			mainActivity.activate(selectedNavigation);
																		}
																	};

	private OnClickListener			divingLogButtonOnClickListener	= new OnClickListener() {
																		public void onClick(View clickedButton) {
																			Log.d(TAG,
																				"DivingLog button clicked");
																			selectedNavigation = SelectedNavigation.DIVINGLOG;
																			mDrawerLayout.closeDrawer(drawer);
																			mainActivity.getFragmentManager().popBackStackImmediate(null,
																				FragmentManager.POP_BACK_STACK_INCLUSIVE);
																			mainActivity.activelyReset = true;
																			mainActivity.activate(selectedNavigation);
																		}
																	};

	private OnFocusChangeListener	nameOnFocusChangeListener		= new OnFocusChangeListener() {
																		public void onFocusChange(View v,
																				boolean hasFocus) {
																			if (!hasFocus) {
																				Editable nameView = ((EditText) v).getText();
																				if (nameView != null) {
																					String name = nameView.toString();
																					Preferences.setString(Preferences.USER_NAME,
																						name);
																				}
																			}
																		}
																	};
	private OnFocusChangeListener	emailOnFocusChangeListener		= new OnFocusChangeListener() {
																		public void onFocusChange(View v,
																				boolean hasFocus) {
																			if (!hasFocus) {
																				Editable emailView = ((EditText) v).getText();
																				if (emailView != null) {
																					String email = emailView.toString();
																					Preferences.setString(Preferences.USER_EMAIL,
																						email);
																				}
																			}
																		}
																	};
	private OnFocusChangeListener	codeOnFocusChangeListener		= new OnFocusChangeListener() {
																		public void onFocusChange(View v,
																				boolean hasFocus) {
																			if (!hasFocus) {
																				Editable codeView = ((EditText) v).getText();
																				if (codeView != null) {
																					String code = codeView.toString();
																					Preferences.setString(Preferences.USER_CODE,
																						code);
																				}
																			}
																		}
																	};

	public MainDrawer(MainActivity activity) {
		mainActivity = activity;
		initializeDrawer();
	}

	public void organizeView() {
		drawer = (RelativeLayout) mainActivity.findViewById(R.id.drawer);

		fieldGuideButton = (Button) mainActivity.findViewById(R.id.nav_fieldguide_button);
		fieldGuideButton.setText(LibApp.getCurrentResources().getString(R.string.fieldguide));
		if (fieldGuideButton != null) {
			fieldGuideButton.setOnClickListener(fieldGuideButtonOnClickListener);
		}

		divingLogButton = (Button) mainActivity.findViewById(R.id.nav_divinglog_button);
		divingLogButton.setText(LibApp.getCurrentResources().getString(R.string.divinglog));
		if (divingLogButton != null) {
			divingLogButton.setOnClickListener(divingLogButtonOnClickListener);
		}

		int orange = LibApp.getCurrentResources().getColor(R.color.orange);
		
		nameEditText = (EditText) mainActivity.findViewById(R.id.nav_prefs_name_et);
		if (nameEditText != null) {
			nameEditText.setText(Preferences.getString(Preferences.USER_NAME,
				null));
			nameEditText.setOnFocusChangeListener(nameOnFocusChangeListener);
            nameEditText.setTextColor(orange);
		}

		emailEditText = (EditText) mainActivity.findViewById(R.id.nav_prefs_email_et);
		if (emailEditText != null) {
			emailEditText.setText(Preferences.getString(Preferences.USER_EMAIL,
				null));
			emailEditText.setOnFocusChangeListener(emailOnFocusChangeListener);
            emailEditText.setTextColor(orange);
		}

		codeEditText = (EditText) mainActivity.findViewById(R.id.nav_prefs_code_et);
		if (codeEditText != null) {
			if(LibApp.getInstance().getCurrentCatalog()!=null && LibApp.getInstance().getCurrentCatalog().isCodeHidden()){
				((View)codeEditText.getParent()).setVisibility(View.GONE);
			}else{
				codeEditText.setText(Preferences.getString(Preferences.USER_CODE,
					null));
				codeEditText.setOnFocusChangeListener(codeOnFocusChangeListener);
				codeEditText.setTextColor(orange);
			}
		}

	}

	public void setUpDrawerListener() {
		mDrawerLayout = (DrawerLayout) mainActivity.findViewById(R.id.drawer_layout);
		mDrawerToggle = new ActionBarDrawerToggle(mainActivity, mDrawerLayout, R.drawable.ic_drawer,
				R.string.drawer_open, R.string.drawer_close) {

			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {
				mainActivity.getActionBar().setTitle(mainActivity.getTitle());
				mainActivity.invalidateOptionsMenu(); // creates call to
				// onPrepareOptionsMenu()
				openedBySwipe = true; // is default for next action
			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
				if(mainActivity!=null){
					if(mainActivity.getActionBar()!=null && LibApp.getInstance()!=null && LibApp.getInstance().getCurrentCatalog()!=null){
						mainActivity.getActionBar().setTitle(LibApp.getInstance().getCurrentCatalog().getAppName()+" "+LibApp.getCurrentCatalogVersionName());
					}
					mainActivity.invalidateOptionsMenu(); // creates call to
				}
				// onPrepareOptionsMenu()

			}
		};

		mDrawerToggle.syncState();
		// Set the drawer toggle as the DrawerListener
		mDrawerLayout.setDrawerListener(mDrawerToggle);

	}

	/*
	 * Called through mainActivity.onPrepareOptionsMenu() whenever we call
	 * invalidateOptionsMenu()
	 */
	public void hideOrShowContentRelatedActionItems(Menu menu) {
		// If the nav drawer is open, hide action items related to the content
		// view
		if (mDrawerLayout != null) {
			boolean drawerOpen = mDrawerLayout.isDrawerOpen(drawer);
			MenuItem item = menu.findItem(R.id.fieldguide_search);
			if (item != null)
				item.setVisible(!drawerOpen);
		}
	}

	public boolean isOpen() {
		if (mDrawerLayout != null) {
			Log.d(TAG,
				"" + mDrawerLayout.isDrawerVisible(drawer));
			return mDrawerLayout.isDrawerVisible(drawer);
		} else {
			Log.d(TAG,
				"null");
			return false;
		}
	}

	public void setTitle(SelectedNavigation selected) {
		if (selected != null) {
			switch (selected) {
				case DIVINGLOG:
					mainActivity.setTitle(LibApp.getCurrentResources().getString(R.string.divinglog));
					break;
				case FIELDGUIDE:
					mainActivity.setTitle(LibApp.getCurrentResources().getString(R.string.fieldguide));
					break;
			}
		}
	}

	public SelectedNavigation getSelected() {
		if (selectedNavigation == null)
			selectedNavigation = SelectedNavigation.FIELDGUIDE;
		return selectedNavigation;
	}

	public ActionBarDrawerToggle getActionBarDrawerToggle() {
		return mDrawerToggle;
	}

	public void openDrawer() {
		if (mDrawerLayout != null && drawer != null) {
			mDrawerLayout.openDrawer(drawer);
		}
	}

	private void initializeDrawer() {
		organizeView();
		setUpDrawerListener();
		mDrawerLayout.setFocusableInTouchMode(false);
	}

}
