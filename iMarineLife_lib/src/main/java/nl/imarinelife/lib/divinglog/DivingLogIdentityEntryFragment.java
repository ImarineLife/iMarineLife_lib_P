package nl.imarinelife.lib.divinglog;

import java.util.Calendar;

import nl.imarinelife.lib.LibApp;
import nl.imarinelife.lib.MainActivity;
import nl.imarinelife.lib.R;
import nl.imarinelife.lib.divinglog.db.dive.BuddyCursorAdapter;
import nl.imarinelife.lib.divinglog.db.dive.Dive;
import nl.imarinelife.lib.divinglog.db.dive.DiveDbHelper;
import nl.imarinelife.lib.divinglog.db.dive.DiveSimpleCursorAdapter;
import nl.imarinelife.lib.divinglog.db.res.Buddy;
import nl.imarinelife.lib.divinglog.db.res.Location;
import nl.imarinelife.lib.divinglog.db.res.LocationCursorProvider;
import nl.imarinelife.lib.utility.DivingLogGestureListener;
import nl.imarinelife.lib.utility.dialogs.EditTextDialogFragment.OnOkListener;
import nl.imarinelife.lib.utility.dialogs.AreYouSureDialogFragment;
import nl.imarinelife.lib.utility.dialogs.AreYouSureDialogFragment.OnYesListener;
import nl.imarinelife.lib.utility.dialogs.DateWheelDialogFragment;
import nl.imarinelife.lib.utility.dialogs.DateWheelDialogFragment.OnDateCompleteListener;
import nl.imarinelife.lib.utility.dialogs.EditTextDialogFragment;
import nl.imarinelife.lib.utility.dialogs.NumberWheelDialogFragment;
import nl.imarinelife.lib.utility.dialogs.NumberWheelDialogFragment.OnCompleteListener;
import nl.imarinelife.lib.utility.dialogs.SearchTextWheelDialogFragment;

import nl.imarinelife.lib.utility.dialogs.SearchTextWheelDialogFragment.OnTextCompleteListener;
import nl.imarinelife.lib.utility.dialogs.TimeWheelDialogFragment;
import nl.imarinelife.lib.utility.dialogs.TimeWheelDialogFragment.OnTimeCompleteListener;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;


public class DivingLogIdentityEntryFragment extends Fragment {

	public boolean shownIdFromDb = false;
	public int currentId = 0;
	public int autoId = 0;
	public static final String TAG = "DivingLogIdEntryFr";

	private DiveDbHelper helper = null;
	private View entry = null;
	private EditText diveNumber = null;
	private EditText datum = null;
	private EditText tijdTeWater = null;
	private EditText locatie = null;
	private AutoCompleteTextView buddyNaam = null;
	private EditText buddyEmail = null;
	private EditText buddyCode = null;

	public static MenuItem remarksMenuItem;

	private GestureDetector gesturedetector = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		//setRetainInstance(true);
		/*
		 * if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
		 * initializeBundle(savedInstanceState); } else {
		 * initializeBundle(getArguments()); }
		 */
		super.onCreate(savedInstanceState);
	}

	private void initializeBundle(Bundle savedInstanceState) {
		if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
			shownIdFromDb = savedInstanceState.getBoolean(
					MainActivity.KEY_BOOL_IDFROMDB, false);
			currentId = savedInstanceState
					.getInt(MainActivity.KEY_INT_CURRENTID);
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
			initializeBundle(savedInstanceState);
		}
		MainActivity.me.setTitle(LibApp.getCurrentResources().getString(R.string.divinglog_page1));
	}

	public boolean dispatchTouchEvent(MotionEvent ev) {
		Log.d(TAG, "dispatchTouchEvent");
		getActivity().dispatchTouchEvent(ev);
		return gesturedetector.onTouchEvent(ev);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
			initializeBundle(savedInstanceState);
		} else {
			initializeBundle(getArguments());
		}

		gesturedetector = new GestureDetector(getActivity(),
				new MyDivingLogGestureListener());
		container.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				gesturedetector.onTouchEvent(event);
				return true;
			}
		});

		// Inflate the layout for this fragment
		entry = inflater.inflate(R.layout.divinglog_identification, container,
				false);
		entry.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				gesturedetector.onTouchEvent(event);
				return true;
			}
		});

		setHasOptionsMenu(true);

		diveNumber = (EditText) entry.findViewById(R.id.dl_id_et_divenr_id);
		datum = (EditText) entry.findViewById(R.id.dl_id_et_datum_id);
		tijdTeWater = (EditText) entry.findViewById(R.id.dl_id_et_tijdtw_id);
		locatie = (EditText) entry.findViewById(R.id.dl_id_et_locatie_id);
		buddyNaam = (AutoCompleteTextView) entry
				.findViewById(R.id.dl_id_et_naam_id);
		buddyEmail = (EditText) entry.findViewById(R.id.dl_id_et_email_id);
		buddyCode = (EditText) entry.findViewById(R.id.dl_id_et_code_id);
		if(buddyCode!=null){
			if(LibApp.getInstance().getCurrentCatalog()!=null && LibApp.getInstance().getCurrentCatalog().isCodeHidden()){
				((View)buddyCode.getParent()).setVisibility(View.GONE);
			}else{
				buddyCode.setRawInputType(InputType.TYPE_CLASS_TEXT);

				buddyCode.setOnFocusChangeListener(new OnFocusChangeListener() {

					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						Log.d(TAG, "focuschange " + hasFocus);
						if (!hasFocus) {
							Editable codeView = ((EditText) v).getText();
							Dive dive = MainActivity.me.currentDive;
							if (codeView != null) {
								String code = codeView.toString();
								dive.setBuddyCode(code);
							} else {
								dive.setBuddyCode(null);
							}
							MainActivity.me.currentDive.setChanged(true);
							MainActivity.me.currentDive.setBuddyCodeChanged(true);
						}
					}
				});
			}
		}

		diveNumber.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				NumberWheelDialogFragment fragment = getNumberWheelDialogFragmentForDiveNr();
				showDialog(fragment);
				MainActivity.me.currentDive.setChanged(true);
			}
		});
		datum.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				DateWheelDialogFragment fragment = getDateWheelDialogFragmentForDate();
				showDialog(fragment);
				MainActivity.me.currentDive.setChanged(true);
			}
		});
		tijdTeWater.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				TimeWheelDialogFragment fragment = getTimeWheelDialogFragmentForTijdTeWater();
				showDialog(fragment);
				MainActivity.me.currentDive.setChanged(true);
			}
		});
		locatie.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String diveCatalog = MainActivity.me.currentDive.getCatalog();
				String currentCatalog = (LibApp.getCurrentCatalogName());
				Log.d(TAG, diveCatalog + "    " + currentCatalog);
				if (diveCatalog.equals(currentCatalog)) {
					SearchTextWheelDialogFragment fragment = getTextWheelDialogFragmentForLocation();
					showDialog(fragment);
					MainActivity.me.currentDive.setChanged(true);
				} else {
					String text = (LibApp.getCurrentResources()
							.getString(R.string.editing_location_notavailable_for_other_app))
							+ "[" + diveCatalog + "]";
					Toast.makeText(MainActivity.me, text, Toast.LENGTH_LONG)
							.show();
				}
			}
		});
		setUpBuddyNaamAutoCompleteTextView(buddyNaam);
		buddyEmail.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					Log.d(TAG, "focusend on email");
					Editable emailView = ((EditText) v).getText();
					if (emailView != null) {
						String email = emailView.toString();
						MainActivity.me.currentDive.setBuddyEmail(email);
					} else {
						MainActivity.me.currentDive.setBuddyEmail(null);
					}
					MainActivity.me.currentDive.setChanged(true);
					MainActivity.me.currentDive.setBuddyEmailChanged(true);
				}else{
					if(LibApp.getInstance().getCurrentCatalog()!=null && LibApp.getInstance().getCurrentCatalog().isCodeHidden()){
						((EditText)v).setImeOptions(EditorInfo.IME_ACTION_DONE);
					}
				}
			}
		});

		// after selection in listfragment
		// shownIdFromDb=true, dive!=null, currentId=dive.diveNr
		// after a user changes content
		// shownIdFromDb=true, contentChanged=true, currentId=dive.diveNr
		// when a user made a new one, nothing has changed yet
		// shownIdFromDb=false && dive=null
		if (!shownIdFromDb && MainActivity.me.currentDive == null) {
			helper = DiveDbHelper.getInstance(getActivity());
			currentId = helper.getHighestDiveNumber() + 1;
			diveNumber.setText(currentId + "");
			((MainActivity) getActivity()).diveNrAtStart = currentId;
			MainActivity.me.currentDive = new Dive(currentId);
			MainActivity.me.currentDive.setChanged(true);
		}
		if (MainActivity.me.currentDive != null) {
			setData(MainActivity.me.currentDive, shownIdFromDb);
		}
		return entry;
	}

	protected NumberWheelDialogFragment getNumberWheelDialogFragmentForDiveNr() {
		final int currentNumber = currentId;

		final int layoutId;
		final int numberWheelId;
		final int choosebuttonId;
		final int cancelbuttonId;
		layoutId = R.layout.divinglog_identification_divenr_dialog;
		numberWheelId = R.id.dl_id_divenrdialog_wheel_id;
		choosebuttonId = R.id.dl_id_divenrdialog_choose_button_id;
		cancelbuttonId = R.id.dl_id_divenrdialog_cancel_button_id;

		@SuppressWarnings("serial")
		OnCompleteListener listener = new OnCompleteListener() {

			@Override
			public void onCompleteNumberWheel(int value) {
				diveNumber.setText(value + "");
				currentId = value;
				MainActivity.me.currentDive.setDiveNr(value);
			}
		};

		NumberWheelDialogFragment frag = NumberWheelDialogFragment.newInstance(
				currentNumber, 0, 10000, 5, null, layoutId, numberWheelId,
				choosebuttonId, cancelbuttonId, DialogFragment.STYLE_NO_TITLE,
				R.style.iMarineLifeDialogTheme);
		frag.setOnCompleteListener(listener);
		return frag;

	}

	protected DateWheelDialogFragment getDateWheelDialogFragmentForDate() {
		Calendar currentDate = Calendar.getInstance();
		if (MainActivity.me.currentDive != null
				&& MainActivity.me.currentDive.getDate() != 0) {
			currentDate.setTimeInMillis(MainActivity.me.currentDive.getDate());
		}
		final int layoutId;
		final int datepickerId;
		final int choosebuttonId;
		final int cancelbuttonId;
		final int currentbuttonId;
		layoutId = R.layout.divinglog_identification_date_dialog;
		datepickerId = R.id.dl_id_datedialog_datepicker_id;
		choosebuttonId = R.id.dl_id_datedialog_choose_button_id;
		cancelbuttonId = R.id.dl_id_datedialog_cancel_button_id;
		currentbuttonId = R.id.dl_id_datedialog_current_button_id;

		OnDateCompleteListener listener = new OnDateCompleteListener() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void onCompleteDatePicker(int year, int month, int day) {
				Calendar calendar = Calendar.getInstance();
				calendar.set(year, month, day);
				datum.setText(DiveSimpleCursorAdapter.dateformat
						.format(calendar.getTime()));
				MainActivity.me.currentDive.setDate(calendar.getTimeInMillis());
			}

		};

		DateWheelDialogFragment frag = DateWheelDialogFragment.newInstance(
				currentDate, 1900, currentDate.get(Calendar.YEAR), 5, layoutId,
				datepickerId, choosebuttonId, cancelbuttonId, currentbuttonId,
				DialogFragment.STYLE_NO_TITLE, R.style.iMarineLifeDialogTheme);
		frag.setOnDateCompleteListener(listener);
		return frag;
	}

	protected TimeWheelDialogFragment getTimeWheelDialogFragmentForTijdTeWater() {
		Calendar currentTime = Calendar.getInstance();
		if (MainActivity.me.currentDive != null
				&& MainActivity.me.currentDive.getTime() != 0) {
			currentTime.setTimeInMillis(MainActivity.me.currentDive.getTime());
		}
		final int layoutId;
		final int timepickerId;
		final int choosebuttonId;
		final int cancelbuttonId;
		layoutId = R.layout.divinglog_identification_tijdtewater_dialog;
		timepickerId = R.id.dl_id_ttwdialog_timepicker_id;
		choosebuttonId = R.id.dl_id_ttwdialog_choose_button_id;
		cancelbuttonId = R.id.dl_id_ttwdialog_cancel_button_id;

		OnTimeCompleteListener listener = new OnTimeCompleteListener() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void onCompleteTimePicker(int hour, int minute) {
				Calendar calendar = Calendar.getInstance();
				calendar.set(Calendar.HOUR_OF_DAY, hour);
				calendar.set(Calendar.MINUTE, minute);
				tijdTeWater.setText(DiveSimpleCursorAdapter.timeformat
						.format(calendar.getTime()));
				MainActivity.me.currentDive.setTime(calendar.getTimeInMillis());
			}

		};

		TimeWheelDialogFragment frag = TimeWheelDialogFragment.newInstance(
				currentTime, 5, layoutId, timepickerId, choosebuttonId,
				cancelbuttonId, DialogFragment.STYLE_NO_TITLE,
				R.style.iMarineLifeDialogTheme);
		frag.setOnTimeCompleteListener(listener);
		return frag;
	}

	protected SearchTextWheelDialogFragment getTextWheelDialogFragmentForLocation() {
		String currentLocation = null;
		if (MainActivity.me.currentDive != null
				&& MainActivity.me.currentDive.getLocation() != null) {
			currentLocation = MainActivity.me.currentDive.getLocation()
					.getShowLocationName();
		}

		final int layoutId;
		final int numberWheelId;
		final int choosebuttonId;
		final int cancelbuttonId;
		final int addbuttonId;
		final int minusbuttonId;
		final int searchtextId;
		final int nextbuttonId;
		final int previousbuttonId;
		layoutId = R.layout.divinglog_identification_location_dialog;
		numberWheelId = R.id.dl_id_locationdialog_wheel_id;
		choosebuttonId = R.id.dl_id_locationdialog_choose_button_id;
		cancelbuttonId = R.id.dl_id_locationdialog_cancel_button_id;
		addbuttonId = R.id.dl_id_locationdialog_add_button_id;
		minusbuttonId = R.id.dl_id_locationdialog_remove_button_id;
		searchtextId = R.id.dl_id_locationdialog_searchtext_id;
		nextbuttonId = R.id.dl_id_locationdialog_next_button_id;


		@SuppressWarnings("serial")
		OnTextCompleteListener listener = new OnTextCompleteListener() {

			@Override
			public void onCompleteTextWheel(Object location, String displayName) {
				locatie.setText(displayName);
				MainActivity.me.currentDive.setLocation((Location) location);
			}
		};

		SearchTextWheelDialogFragment frag = SearchTextWheelDialogFragment.newInstance(
				currentLocation, new LocationCursorProvider(getActivity()), 5,
				layoutId, numberWheelId,
				searchtextId, nextbuttonId,
				0, choosebuttonId, cancelbuttonId,
				addbuttonId, minusbuttonId,
				DialogFragment.STYLE_NO_TITLE,
				R.style.iMarineLifeDialogTheme);
		frag.setOnCompleteListener(listener);
		return frag;

	}

	private void setUpBuddyNaamAutoCompleteTextView(
			AutoCompleteTextView buddyNaam) {
		helper = DiveDbHelper.getInstance(this.getActivity());
		BuddyCursorAdapter buddyAdapter = new BuddyCursorAdapter(
				this.getActivity(), helper, buddyNaam);

		buddyNaam.setAdapter(buddyAdapter);
		buddyNaam.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> listView, View view,
					int position, long id) {
				// Get the cursor, positioned to the corresponding row in the
				// result set
				Cursor cursor = (Cursor) listView.getItemAtPosition(position);
				String buddyName = cursor
						.getString(DiveDbHelper.KEY_BUDDYNAME_CURSORLOC_BUDDYLIST);
				setBuddyData(buddyName);
				MainActivity.me.currentDive.setBuddyNameSelected(buddyName);
				cursor.close();
			}
		});

		buddyNaam.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				Log.d(TAG, "focuschange name: " + hasFocus);
				Dive dive = MainActivity.me.currentDive;
				if (!hasFocus) {
					Editable nameView = ((EditText) v).getText();
					if (nameView != null
							&& nameView.toString().trim().length() > 0) {
						String name = nameView.toString();
						dive.setBuddyName(name);
						Log.d(TAG, name + "    " + dive.getBuddyNameSelected());
						if (dive.getBuddyNameSelected() != null
								&& dive.getBuddyNameSelected().trim().length() > 0
								&& !name.equals(dive.getBuddyNameSelected())) {
							AreYouSureDialogFragment fragment = getAreYouSureBuddyChangedFragment(getActivity());
							FragmentTransaction ft = (getActivity())
									.getFragmentManager()
									.beginTransaction();
							ft.addToBackStack(null);
							fragment.show(ft, "adddialog");
						}

					} else {
						if (dive.getBuddy() != null) {
							dive.setBuddy(null);
						}
					}
				}
				Log.d(TAG,
						(MainActivity.me.currentDive != null && MainActivity.me.currentDive
								.getBuddy() != null) ? MainActivity.me.currentDive
								.getBuddy().toString() : "null");
			}

			private AreYouSureDialogFragment getAreYouSureBuddyChangedFragment(
					Activity activity) {
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
						MainActivity.me.currentDive
								.setBuddyNameMustbeChangedEveryWhere(true);
					}
				};

				String dialogtxt = LibApp.getCurrentResources().getString(
						R.string.areyousure_buddychanged);
				AreYouSureDialogFragment frag = AreYouSureDialogFragment
						.newInstance(dialogtxt, layoutId, textViewId,
								yesbuttonId, nobuttonId,
								DialogFragment.STYLE_NO_TITLE,
								R.style.iMarineLifeDialogTheme);
				frag.setOnYesListener(yesListener);
				return frag;

			}
		});

	}

	protected void setBuddyData(String buddyName) {
		buddyNaam.setText(buddyName);
		if (buddyName != null && buddyName.length() > 0) {
			String catalog = MainActivity.me.currentDive.getCatalog();
			helper = DiveDbHelper.getInstance(getActivity());
			Buddy buddy = helper.getUpToDateBuddy(buddyName);
			if (buddy == null) {
				buddy = new Buddy();
				buddy.setName(buddyName);
				buddy.setBuddyNameSelected(buddyName);
			}
			buddyEmail.setText(buddy.getEmail());
			buddyCode.setText(buddy.getCodeForCatalog(catalog));
			MainActivity.me.currentDive.setBuddy(buddy);
		}
	}

	public static EditTextDialogFragment getRemarksDialogFragment() {
		String currentText = null;
		if (MainActivity.me.currentDive != null
				&& MainActivity.me.currentDive.getLocation() != null) {
			currentText = MainActivity.me.currentDive.getRemarks();
			Log.d(TAG, "remarks from dive: " + currentText);
		}
		String labelText = LibApp.getCurrentResources().getString(
				R.string.dive_remarks);

		final int layoutId;
		final int labelId;
		final int editTextId;
		final int okbuttonId;
		final int cancelbuttonId;
		layoutId = R.layout.general_edittext_dialog;
		labelId = R.id.general_labeltext_id;
		editTextId = R.id.general_edittext_id;
		okbuttonId = R.id.general_ok_button_id;
		cancelbuttonId = R.id.general_cancel_button_id;

		@SuppressWarnings("serial")
		OnOkListener listener = new OnOkListener() {

			@Override
			public void onOk(String remarks) {
				MainActivity.me.currentDive.setRemarks(remarks);
				MainActivity.me.saveDive(true);
				if (remarksMenuItem != null) {
					if (remarks != null && remarks.trim().length() > 0) {
						DivingLogIdentityEntryFragment.remarksMenuItem
								.setIcon(LibApp.getCurrentResources()
										.getDrawable(
												R.drawable.ic_action_labels));
					} else {
						DivingLogIdentityEntryFragment.remarksMenuItem
								.setIcon(LibApp.getCurrentResources()
										.getDrawable(
												R.drawable.ic_action_new_label));
					}
				} else {
					Log.d(TAG, "remarksMenuItem does not exist");
				}
				Log.d(TAG, "remarks written in currentDive["
						+ MainActivity.me.currentDive.getDiveNr() + "]: "
						+ remarks);

			}
		};
		EditTextDialogFragment frag = EditTextDialogFragment.newInstance(
				labelText, currentText, layoutId, labelId, editTextId,
				okbuttonId, cancelbuttonId, 200, DialogFragment.STYLE_NO_TITLE,
				R.style.iMarineLifeDialogTheme, true);
		frag.setOnOkListener(listener);
		return frag;

	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		Log.d(TAG, "onPrepareOptionsMenu");
		menu.clear();
		getActivity().getMenuInflater().inflate(
				R.menu.diving_log_identity, menu);

		remarksMenuItem = menu.findItem(R.id.diving_log_remarks);
		if (MainActivity.me != null && MainActivity.me.currentDive != null
				&& MainActivity.me.currentDive.getRemarks() != null
				&& MainActivity.me.currentDive.getRemarks().length() > 0) {
			remarksMenuItem.setIcon(LibApp.getCurrentResources().getDrawable(
					R.drawable.ic_action_labels));
		}
		super.onPrepareOptionsMenu(menu);

	}

	@Override
	public boolean onOptionsItemSelected(
			MenuItem item) {
		Log.d(TAG, "onOptionsItemSelected: [" + item.getItemId() + "]");

		if (item.getItemId() == R.id.diving_log_next) {
            if(getView()!=null) {
                View withFocus = this.getView().findFocus();
                if (withFocus != null) {
                    withFocus.clearFocus();
                }

                boolean succes = ((MainActivity) getActivity())
                        .saveDive(true);
                if (succes) {
                    return activateDivingLogStayEntryFragment();
                }
            }
		} else if (item.getItemId() == R.id.diving_log_remarks) {
			EditTextDialogFragment fragment = getRemarksDialogFragment();
			showDialog(fragment);
			MainActivity.me.currentDive.setChanged(true);
		}

		return super.onOptionsItemSelected(item);

	}

	private boolean activateDivingLogStayEntryFragment() {
		DivingLogStayEntryFragment fragment = new DivingLogStayEntryFragment();
		Bundle bundle = getActivity().getIntent().getExtras();
		bundle = saveState(bundle);
		Log.d(TAG, "bundle[" + bundle + "]");
		fragment.setArguments(bundle);
		switch (((MainActivity) getActivity()).getNumberOfPanes()) {
		case 1:
			FragmentTransaction transaction = getActivity()
					.getFragmentManager().beginTransaction();
			transaction = ((MainActivity) getActivity()).addOrReplaceFragment(
					R.id.content_frame_1, transaction, fragment,
					MainActivity.FRAME1);
			transaction.addToBackStack(null);
			transaction.commit();
			break;
		case 2:
			FragmentTransaction transaction2 = getActivity()
					.getFragmentManager().beginTransaction();
			transaction = ((MainActivity) getActivity()).addOrReplaceFragment(
					R.id.content_frame_2, transaction2, fragment,
					MainActivity.FRAME2);
			// transaction2.addToBackStack(null);
			transaction2.commit();
			break;
		}
		return true;

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		saveState(outState);
	}

	private Bundle saveState(Bundle outState) {
		if (outState == null) {
			outState = new Bundle();
		}
		outState.putBoolean(MainActivity.KEY_BOOL_IDFROMDB, shownIdFromDb);
		outState.putInt(MainActivity.KEY_INT_CURRENTID, currentId);
		return outState;
	}

	public long getShownId() {
		return currentId;
	}

	public void setData(Dive dive, boolean shownIdFromDb) {
		Log.d(TAG, "setData [" + dive.getDiveNr() + "]");
		if (entry != null && diveNumber != null && dive != null) {
			this.shownIdFromDb = shownIdFromDb;
			currentId = dive.getDiveNr();
			diveNumber.setText(dive.getDiveNr() + "");
			datum.setText(dive.getFormattedDate());
			tijdTeWater.setText(dive.getFormattedTime());
			locatie.setText(dive.getLocationName());
			buddyNaam.setText(dive.getBuddyName());
			buddyEmail.setText(dive.getBuddyEmail());
			buddyCode.setText(dive.getBuddyCode());
		}
	}

	public interface OnDivingLogItemSelectedListener {
		public void activateDivingLogEntryFragment(long id, Dive dive);
	}

	void showDialog(DialogFragment newFragment) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.addToBackStack(null);
		newFragment.show(ft, "dialog");

	}

	@Override
	public void onStop() {
		if (helper != null)
			helper.close();
		super.onStop();
	}

	public void onDestroy() {
		if (helper != null)
			helper.close();
		super.onStop();
	}

	public void onStart() {
		super.onStart();
		helper = DiveDbHelper.getInstance(getActivity());
	}

	private class MyDivingLogGestureListener extends DivingLogGestureListener {

		@Override
		protected void onLeftSwipe() {
			Log.d(TAG, "onLeftSwipe");
            if(getView()!=null) {
                View withFocus = getView().findFocus();
                if (withFocus != null) {
                    withFocus.clearFocus();
                }
                boolean succes = ((MainActivity) getActivity())
                        .saveDive(false);
                if (succes) {
                    FragmentManager manager = getFragmentManager();
                    if (manager != null)
                        manager.popBackStackImmediate();

                }
            }
		}

		@Override
		protected void onRightSwipe() {
			Log.d(TAG, "onLeftSwipe: activate stay page");
            if(getView()!=null) {
                View withFocus = getView().findFocus();
                if (withFocus != null) {
                    withFocus.clearFocus();
                }
                boolean succes = ((MainActivity) getActivity())
                        .saveDive(true);
                if (succes) {
                    activateDivingLogStayEntryFragment();
                }
            }
		}

	}

}
