package nl.imarinelife.lib.divinglog;

import java.util.HashMap;

import nl.imarinelife.lib.LibApp;
import nl.imarinelife.lib.MainActivity;
import nl.imarinelife.lib.R;
import nl.imarinelife.lib.divinglog.db.dive.Dive;
import nl.imarinelife.lib.divinglog.db.dive.DiveDbHelper;
import nl.imarinelife.lib.divinglog.db.dive.DiveProfilePart;
import nl.imarinelife.lib.divinglog.db.res.ProfilePart;
import nl.imarinelife.lib.divinglog.db.res.ProfilePartDbHelper;
import nl.imarinelife.lib.divinglog.db.res.ProfilePartDbHelper.AddType;
import nl.imarinelife.lib.divinglog.db.res.WheelDrivenProfilePartEditText;
import nl.imarinelife.lib.divinglog.sightings.DivingLogSightingsListFragment;
import nl.imarinelife.lib.fieldguide.db.FieldGuideAndSightingsEntryDbHelper;
import nl.imarinelife.lib.utility.DivingLogGestureListener;
import nl.imarinelife.lib.utility.RobotoEditText;
import nl.imarinelife.lib.utility.RobotoTextView;
import nl.imarinelife.lib.utility.SerializableSparseArray;
import nl.imarinelife.lib.utility.Utils;
import nl.imarinelife.lib.utility.dialogs.EditTextDialogFragment;
import nl.imarinelife.lib.utility.dialogs.NumberWheelDialogFragment;
import nl.imarinelife.lib.utility.dialogs.NumberWheelDialogFragment.OnCompleteListener;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.InputType;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class DivingLogStayEntryFragment extends Fragment {

	public boolean shownIdFromDb = false;
	public int currentId = 0;
	public int autoId = 0;
	public static final String TAG = "DivingLogIdStEntryFr";

	private DiveDbHelper helper = null;
	private View entry = null;

	private SerializableSparseArray<WheelDrivenProfilePartEditText> addEditTexts = new SerializableSparseArray<WheelDrivenProfilePartEditText>();
	private SerializableSparseArray<WheelDrivenProfilePartEditText> nonaddEditTexts = new SerializableSparseArray<WheelDrivenProfilePartEditText>();
	private TextView totalsummed = null;
	private EditText zichtInMeters = null;

	private GestureDetector gesturedetector = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setRetainInstance(true);
		if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
			initializeBundle(savedInstanceState);
		} else {
			initializeBundle(getArguments());
		}
		super.onCreate(savedInstanceState);
	}

	private void initializeBundle(Bundle savedInstanceState) {
		Log.d(TAG, "initializeBundle [" + savedInstanceState.size() + "]");
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
		MainActivity.me.setTitle(LibApp.getCurrentResources().getString(R.string.divinglog_page2));
		setHasOptionsMenu(true);
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
				Log.d(TAG, "onTouch");
				gesturedetector.onTouchEvent(event);

				return true;

			}

		});

		addEditTexts.clear();
		nonaddEditTexts.clear();
		entry = initializeLayOut();

		zichtInMeters.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				NumberWheelDialogFragment fragment = getNumberWheelDialogFragmentForZichtInMeters();
				showDialog(fragment);
				MainActivity.me.currentDive.setChanged(true);
			}
		});

		Dive dive = MainActivity.me.currentDive;
		if (dive != null) {
			setData(dive, shownIdFromDb);
		}

		return entry;
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		Log.d(TAG, "onPrepareOptionsMenu");
		menu.clear();
		getActivity().getMenuInflater().inflate(
				R.menu.diving_log_identity, menu);
		if (MainActivity.me != null && MainActivity.me.currentDive != null
				&& MainActivity.me.currentDive.getRemarks() != null
				&& MainActivity.me.currentDive.getRemarks().length() > 0) {
			DivingLogIdentityEntryFragment.remarksMenuItem = menu
					.findItem(R.id.diving_log_remarks);
			DivingLogIdentityEntryFragment.remarksMenuItem
					.setIcon(LibApp.getCurrentResources().getDrawable(
							R.drawable.ic_action_labels));
		}
		super.onPrepareOptionsMenu(menu);

	}

	@Override
	public boolean onOptionsItemSelected(
			MenuItem item) {
		Log.d(TAG, "onOptionsItemSelected: [" + item.getItemId() + "]");
		if (item.getItemId() == R.id.diving_log_next) {
			View withFocus = getView().findFocus();
			if (withFocus != null) {
				withFocus.clearFocus();
			}

			boolean succes = ((MainActivity) getActivity())
					.saveDive(true);
			if (succes) {
				return activateSightingsFragment();
			}
		} else if (item.getItemId() == R.id.diving_log_remarks) {
			EditTextDialogFragment fragment = DivingLogIdentityEntryFragment
					.getRemarksDialogFragment();
			showDialog(fragment);
			MainActivity.me.currentDive.setChanged(true);
		}
		return super.onOptionsItemSelected(item);

	}

	private Bundle saveState(Bundle outState) {
		if (outState == null) {
			outState = new Bundle();
		}
		outState.putBoolean(MainActivity.KEY_BOOL_IDFROMDB, shownIdFromDb);
		outState.putInt(MainActivity.KEY_INT_CURRENTID, currentId);
		outState.putSerializable(MainActivity.KEY_SER_DIVE,
				MainActivity.me.currentDive);
		return outState;
	}

	private boolean activateSightingsFragment() {
		FieldGuideAndSightingsEntryDbHelper dbHelper = FieldGuideAndSightingsEntryDbHelper
				.getInstance(this.getActivity());
		Cursor cursor = dbHelper.querySightingAsIsForDive(
				MainActivity.me.currentDive.getDiveNr(),
				MainActivity.me.currentDive.getCatalog());
		int count = cursor.getCount();
		cursor.close();

		if (count != 0 || MainActivity.areDiveSightingsEditable()) {
			DivingLogSightingsListFragment fragment = new DivingLogSightingsListFragment();
			Bundle bundle = getActivity().getIntent().getExtras();
			bundle = saveState(bundle);
			Log.d(TAG, "bundle[" + bundle + "]");
			fragment.setArguments(bundle);
			switch (((MainActivity) getActivity()).getNumberOfPanes()) {
			case 1:
				FragmentTransaction transaction = getActivity()
						.getSupportFragmentManager().beginTransaction();
				transaction = ((MainActivity) getActivity())
						.addOrReplaceFragment(R.id.content_frame_1,
								transaction, fragment, MainActivity.FRAME1);
				transaction.addToBackStack(null);
				transaction.commit();
				break;
			case 2:
				FragmentTransaction transaction2 = getActivity()
						.getSupportFragmentManager().beginTransaction();
				transaction = ((MainActivity) getActivity())
						.addOrReplaceFragment(R.id.content_frame_2,
								transaction2, fragment, MainActivity.FRAME2);
				// transaction2.addToBackStack(null);
				transaction2.commit();
				break;
			}
		}
		return true; // option was consumed, no need for checking other ids
	}

	protected NumberWheelDialogFragment getNumberWheelDialogFragmentForZichtInMeters() {

		final int currentNumber = MainActivity.me.currentDive
				.getVisibilityInMeters();

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
				zichtInMeters.setText(value + "");
				MainActivity.me.currentDive.setVisibilityInMeters(value);
				MainActivity.me.currentDive.setChanged(true);
			}
		};

		NumberWheelDialogFragment frag = NumberWheelDialogFragment.newInstance(
				currentNumber, 0, 100, 5, null, layoutId, numberWheelId,
				choosebuttonId, cancelbuttonId, DialogFragment.STYLE_NO_TITLE,
				R.style.iMarineLifeDialogTheme);
		frag.setOnCompleteListener(listener);
		return frag;

	}

	private View initializeLayOut() {
		Log.d(TAG, "initializeLayOut");
		ScrollView scrollView = new ScrollView(getActivity());
		scrollView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		// this is to make sure the scrollview does not override the onTouch so
		// that the Swipe doesn't work
		scrollView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				gesturedetector.onTouchEvent(event);
				return false;
			}
		});
		TableLayout table = new TableLayout(getActivity());
		table.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		table.setId(R.id.divinglog_stay);
		table.setColumnStretchable(1, true);
		int px1 = Utils.getPixels(LibApp.getCurrentResources(), 15);
		int px2 = Utils.getPixels(LibApp.getCurrentResources(), 25);
		table.setPadding(px2, px1, px2, px1);

		// hier wordt de setup bepaald, vanuit de duik als die er is en anders
		// uit de configuratie.
		// de opzet van een duik kán dus anders zijn dan die vanuit de
		// configuratie (als die tussentijds gewijzigd is bijvoorbeeld)
		HashMap<ProfilePartDbHelper.AddType, SerializableSparseArray<ProfilePart>> pparts = null;

		boolean useDive = false;
		Dive currentDive = MainActivity.me.currentDive;
        if (currentDive != null) {
            useDive=true;
		    SerializableSparseArray<DiveProfilePart> addlist = currentDive
					.getProfile().get(ProfilePartDbHelper.AddType.ADD);
			SerializableSparseArray<DiveProfilePart> noaddlist = currentDive
					.getProfile().get(ProfilePartDbHelper.AddType.NON_ADD);

			if (addlist == null || noaddlist == null
					|| (addlist.size() == 0 && noaddlist.size() == 0)) {
				useDive = false;
			}

		}
		if (useDive) {
			Log.d(TAG, "setting up for dive");
			pparts = MainActivity.me.currentDive.getProfilePartSetUpFromDive();
		} else {

			Log.d(TAG, "setting up from configuration");
			ProfilePartDbHelper pphelper = ProfilePartDbHelper
					.getInstance(getActivity());
			pparts = pphelper.fetchAll();
			MainActivity.me.currentDive.fillFromProfilePartsSetup(pparts);
		}

		// setting up the views
		SerializableSparseArray<ProfilePart> partlist = pparts
				.get(ProfilePartDbHelper.AddType.ADD);
		if (partlist != null) {
			for (int i = 0; i < partlist.size(); i++) {
				ProfilePart part = partlist.get(partlist.keyAt(i));
				addTableRowForPart(table, part, addEditTexts);
			}
		}
		addTableRowsForVisibility(table);
		partlist = pparts.get(ProfilePartDbHelper.AddType.NON_ADD);
		if (partlist != null) {
			for (int i = 0; i < partlist.size(); i++) {
				ProfilePart part = partlist.get(partlist.keyAt(i));
				addTableRowForPart(table, part, nonaddEditTexts);
			}
		}
		scrollView.addView(table);
		return scrollView;

	}

	private void addTableRowsForVisibility(TableLayout table) {
		// TotalRow
		TableRow totalrow = new TableRow(getActivity());
		totalrow.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				Utils.getPixels(LibApp.getCurrentResources(), 35)));
		int px = Utils.getPixels(LibApp.getCurrentResources(), 7);

		// Total Label
		int tv_idtotallabel = R.id.divinglog_stay_tv_total_label;
		TextView totallabel = new RobotoTextView(getActivity());
		totallabel.setId(tv_idtotallabel);
		// totallabel.setLayoutParams(new
		// LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		totallabel.setPadding(px, px, px, px);
		totallabel.setText(LibApp.getCurrentResources().getString(R.string.dl_stay_totalTime));
		totallabel.setTextColor(LibApp.getCurrentResources().getColor(R.color.dontknow));
		totallabel.setTextSize(18);
		totallabel.setTypeface(Typeface.SANS_SERIF, Typeface.NORMAL);

		// Total Summed
		int tv_idtotalsummed = R.id.divinglog_stay_tv_total_summed;
		totalsummed = new RobotoTextView(getActivity());
		totalsummed.setId(tv_idtotalsummed);
		// totalsummed.setLayoutParams(new
		// LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		totalsummed.setPadding(px, px, px, px);
		totalsummed.setTextColor(LibApp.getCurrentResources().getColor(R.color.dontknow));
		totalsummed.setTextSize(18);
		totalsummed.setTypeface(Typeface.SANS_SERIF, Typeface.NORMAL);
		int nrOfSummable = resetTotalSummed();

		if (nrOfSummable > 0) {
			totalrow.addView(totallabel);
			totalrow.addView(totalsummed);
			table.addView(totalrow);

			// Empty Row
			TableRow emptyrow = new TableRow(getActivity());
			emptyrow.setLayoutParams(new LayoutParams(
					LayoutParams.MATCH_PARENT, Utils.getPixels(LibApp.getCurrentResources(),
							10)));

			// Label
			TextView emptylabel = new RobotoTextView(getActivity());
			// emptylabel.setLayoutParams(new
			// LayoutParams(LayoutParams.WRAP_CONTENT,
			// LayoutParams.WRAP_CONTENT));
			emptylabel.setText("");
			emptylabel.setTextColor(LibApp.getCurrentResources().getColor(R.color.dontknow));
			emptylabel.setTextSize(18);
			emptylabel.setTypeface(Typeface.SANS_SERIF, Typeface.NORMAL);

			emptyrow.addView(emptylabel);
			table.addView(emptyrow);
		}
		// Visibility Row
		TableRow row = new TableRow(getActivity());
		row.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, Utils
				.getPixels(LibApp.getCurrentResources(), 40)));

		// Label
		int tv_id = R.id.divinglog_stay_tv_maxvis;
		TextView label = new RobotoTextView(getActivity());
		label.setId(tv_id);
		// label.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
		// LayoutParams.WRAP_CONTENT));
		label.setPadding(px, px, px, px);
		label.setText(LibApp.getCurrentResources().getString(R.string.dl_stay_maxdepth));
		label.setTextColor(LibApp.getCurrentResources().getColor(R.color.dontknow));
		label.setTextSize(18);
		label.setTypeface(Typeface.SANS_SERIF, Typeface.NORMAL);

		// EditText
		int et_id = R.id.divinglog_stay_et_maxvis;
		zichtInMeters = new RobotoEditText(getActivity());
		zichtInMeters.setId(et_id);
		// zichtInMeters.setLayoutParams(new
		// LayoutParams(Utils.getPixels(LibApp.getCurrentResources(),
		// 120), Utils.getPixels(LibApp.getCurrentResources(),
		// 40)));
		zichtInMeters.setFocusable(false);
		zichtInMeters.setInputType(InputType.TYPE_CLASS_NUMBER);
		zichtInMeters.setPadding(px, px, px, px);
		zichtInMeters.setTextColor(LibApp.getCurrentResources().getColor(R.color.dontknow));
		zichtInMeters.setTextSize(18);
		zichtInMeters.setTypeface(Typeface.SANS_SERIF, Typeface.NORMAL);

		row.addView(label);
		row.addView(zichtInMeters);
		table.addView(row);

	}

	private void addTableRowForPart(TableLayout table, ProfilePart part,
			SerializableSparseArray<WheelDrivenProfilePartEditText> list) {
		Log.d(TAG, "addTableRowForPart: profilePart to add[" + part + "]");

		TableRow row = new TableRow(getActivity());
		row.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, Utils
				.getPixels(LibApp.getCurrentResources(), 40)));

		// Label
		int tv_id = getTVId(part.orderNumber);
		TextView label = new RobotoTextView(getActivity());
		label.setId(tv_id);
		// label.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
		// LayoutParams.WRAP_CONTENT));
		int px = Utils.getPixels(LibApp.getCurrentResources(), 7);
		label.setPadding(px, px, px, px);
		label.setText(part.getShowName());
		label.setTextColor(LibApp.getCurrentResources().getColor(R.color.dontknow));
		label.setTextSize(18);
		label.setTypeface(Typeface.SANS_SERIF, Typeface.NORMAL);

		// EditText
		Dive dive = MainActivity.me.currentDive;
		DiveProfilePart dpart = dive != null ? dive.getDiveProfilePart(part)
				: null;
		;
		int et_id = getETId(part.orderNumber);
		EditText editText = new RobotoEditText(getActivity());
		editText.setId(et_id);
		// editText.setLayoutParams(new
		// LayoutParams(Utils.getPixels(LibApp.getCurrentResources(),
		// 120), Utils.getPixels(LibApp.getCurrentResources(),
		// 40)));
		editText.setFocusable(false);
		editText.setInputType(InputType.TYPE_CLASS_NUMBER);
		editText.setPadding(px, px, px, px);
		if (dpart == null) {
			editText.setText(0 + "");
		} else {
			editText.setText("" + dpart.stayValueInMeters);
		}
		editText.setTextColor(LibApp.getCurrentResources().getColor(R.color.dontknow));
		editText.setTextSize(18);
		editText.setTypeface(Typeface.SANS_SERIF, Typeface.NORMAL);

		WheelDrivenProfilePartEditText wheelDrivenEditText = new WheelDrivenProfilePartEditText(
				editText, dpart, MainActivity.me.currentDive, this);
		list.put(part.orderNumber, wheelDrivenEditText);
		row.addView(label);
		row.addView(editText);
		table.addView(row);

	}

	private int getETId(int orderNumber) {
		switch (orderNumber) {
		case 1:
			return R.id.divinglog_stay_et_1;
		case 2:
			return R.id.divinglog_stay_et_2;
		case 3:
			return R.id.divinglog_stay_et_3;
		case 4:
			return R.id.divinglog_stay_et_4;
		case 5:
			return R.id.divinglog_stay_et_5;
		case 6:
			return R.id.divinglog_stay_et_6;
		case 7:
			return R.id.divinglog_stay_et_7;
		case 8:
			return R.id.divinglog_stay_et_8;
		case 9:
			return R.id.divinglog_stay_et_9;
		case 10:
			return R.id.divinglog_stay_et_10;
		default:
			throw new IllegalArgumentException(
					"a ProfilePart must have an orderNumber in the range 1-10 not ["
							+ orderNumber + "]");

		}
	}

	private int getTVId(int orderNumber) {
		switch (orderNumber) {
		case 1:
			return R.id.divinglog_stay_tv_1;
		case 2:
			return R.id.divinglog_stay_tv_2;
		case 3:
			return R.id.divinglog_stay_tv_3;
		case 4:
			return R.id.divinglog_stay_tv_4;
		case 5:
			return R.id.divinglog_stay_tv_5;
		case 6:
			return R.id.divinglog_stay_tv_6;
		case 7:
			return R.id.divinglog_stay_tv_7;
		case 8:
			return R.id.divinglog_stay_tv_8;
		case 9:
			return R.id.divinglog_stay_tv_9;
		case 10:
			return R.id.divinglog_stay_tv_10;
		default:
			throw new IllegalArgumentException(
					"a ProfilePart must have an orderNumber in the range 1-10 not ["
							+ orderNumber + "]");

		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(MainActivity.KEY_BOOL_IDFROMDB, shownIdFromDb);
		outState.putInt(MainActivity.KEY_INT_CURRENTID, currentId);
		outState.putSerializable(MainActivity.KEY_SER_DIVE,
				MainActivity.me.currentDive);
	}

	public long getShownId() {
		return currentId;
	}

	public void setData(Dive dive, boolean shownIdFromDb) {

		if (entry != null && dive != null) {
			Log.d(TAG, "setData divep[" + dive + "]");
			this.shownIdFromDb = shownIdFromDb;
			currentId = dive.getDiveNr();

			SerializableSparseArray<DiveProfilePart> addParts = getPartsList(
					ProfilePartDbHelper.AddType.ADD, dive.getProfile(),
					addEditTexts);
			if (addParts != null && addEditTexts != null) {
				for (int i = 0; i < addParts.size(); i++) {
					DiveProfilePart part = addParts.get(addParts.keyAt(i));
					if (part.profilePart != null) {
						int orderNr = part.profilePart.orderNumber;
						WheelDrivenProfilePartEditText wheelDriven = addEditTexts
								.get(orderNr);
						Log.d(TAG, "setData wheelDrive[" + i + "]["
								+ wheelDriven + "]");

						wheelDriven.numberEditText.setText(""
								+ part.stayValueInMeters);
						wheelDriven.currentPart = part;
					}
				}
			} else {
				Log.d(TAG, "add: " + addParts + " " + addEditTexts);
			}

			if (zichtInMeters != null)
				zichtInMeters.setText(dive.getVisibilityInMeters() + "");

			SerializableSparseArray<DiveProfilePart> nonaddParts = getPartsList(
					ProfilePartDbHelper.AddType.NON_ADD, dive.getProfile(),
					nonaddEditTexts);
			if (nonaddParts != null && nonaddEditTexts != null) {
				for (int i = 0; i < nonaddParts.size(); i++) {
					DiveProfilePart part = nonaddParts
							.get(nonaddParts.keyAt(i));
					if (part.profilePart != null) {
						int orderNr = part.profilePart.orderNumber;
						WheelDrivenProfilePartEditText wheelDriven = nonaddEditTexts
								.get(orderNr);
						wheelDriven.numberEditText.setText(""
								+ part.stayValueInMeters);
						wheelDriven.currentPart = part;
					}
				}
			} else {
				Log.d(TAG, "nonadd: " + nonaddParts + " " + nonaddEditTexts);
			}

		}
	}

	private SerializableSparseArray<DiveProfilePart> getPartsList(
			AddType addType,
			HashMap<AddType, SerializableSparseArray<DiveProfilePart>> profile,
			SerializableSparseArray<WheelDrivenProfilePartEditText> wheelDrivenEditTexts) {
		Log.d(TAG, "getPartsList[" + profile + "][" + addType + "]["
				+ wheelDrivenEditTexts + "]");
		SerializableSparseArray<DiveProfilePart> addParts = null;
		if (profile != null && profile.get(addType) != null
				&& profile.get(addType).size() != 0) {
			addParts = profile.get(addType);
			for (int i = 0; i < addParts.size(); i++) {
				DiveProfilePart part = addParts.get(i);
				Log.d(TAG, "getPartsList[" + part + "]");
			}
			return addParts;
		}
		if (addParts == null) {
			addParts = new SerializableSparseArray<DiveProfilePart>();
			if (wheelDrivenEditTexts != null) {
				for (int i = 0; i < wheelDrivenEditTexts.size(); i++) {
					WheelDrivenProfilePartEditText wheelDriven = wheelDrivenEditTexts
							.get(wheelDrivenEditTexts.keyAt(i));
					addParts.put(
							wheelDriven.currentPart.profilePart.orderNumber,
							wheelDriven.currentPart);
				}
				return addParts;
			}

		}

		return null;
	}

	public int resetTotalSummed() {
		int total = 0;
		int counter = 0;
		if (totalsummed != null && MainActivity.me.currentDive != null
				&& MainActivity.me.currentDive.getProfile() != null) {
			SerializableSparseArray<DiveProfilePart> addParts = MainActivity.me.currentDive
					.getProfile().get(ProfilePartDbHelper.AddType.ADD);
			if (addParts != null) {
				Log.d(TAG, "addParts[" + addParts.size() + "]");
				for (int i = 1; i < addEditTexts.size() + 1; i++) {
					DiveProfilePart part = addParts.get(i);
					if (part != null) {
						total += part.stayValueInMeters;
					}
					counter++;
				}
			}
			Log.d(TAG, "resetTotalSummed total[" + total + "]");
			totalsummed.setText(total + "");

		}
		return counter;
	}

	public interface OnDivingLogItemSelectedListener {
		public void activateDivingLogEntryFragment(
				DivingLogStayEntryFragment entry, long id, Dive dive);
	}

	public void showDialog(DialogFragment newFragment) {
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
				super.onLeftSwipe();
			}
		}

		@Override
		protected void onRightSwipe() {
			Log.d(TAG, "onLeftSwipe: activate sightings page");
			View withFocus = getView().findFocus();
			if (withFocus != null) {
				withFocus.clearFocus();
			}

			boolean succes = ((MainActivity) getActivity())
					.saveDive(true);
			if (succes) {
				activateSightingsFragment();
				super.onRightSwipe();
			}
		}

	}
}
