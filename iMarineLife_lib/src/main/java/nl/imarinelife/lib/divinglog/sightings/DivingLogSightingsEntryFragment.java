package nl.imarinelife.lib.divinglog.sightings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.imarinelife.lib.LibApp;
import nl.imarinelife.lib.MainActivity;
import nl.imarinelife.lib.MarineLifeContentProvider;
import nl.imarinelife.lib.R;
import nl.imarinelife.lib.catalog.Catalog;
import nl.imarinelife.lib.fieldguide.db.FieldGuideAndSightingsEntryDbHelper;
import nl.imarinelife.lib.fieldguide.db.FieldGuideEntry;

import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

public class DivingLogSightingsEntryFragment extends Fragment implements
		OnCheckedChangeListener, OnClickListener {

	private static String TAG = "DivingLogSightingsEntryFragment";
	private View entry = null;
	private Sighting sighting = null;

	private TextView commonView = null;
	private TextView latinView = null;
	private TextView descriptionView = null;
	private ImageView imageView = null;
	@SuppressWarnings("unused")
	private LinearLayout sightingbuttons = null;
	private RadioGroup group = null;
	RadioGroup buttongroup = null;
	Map<String, RadioButton> buttons = null;
	private LinearLayout checkboxgroup = null;
	Map<String, CheckBox> allCheckBoxes = null;
	public String sightingValue = null;
	public String showSightingValue = null;
	public List<String> checkedValues = null;
	public List<String> showCheckedValues = null;
	public String checkValues;

	private long shownId = 0L;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setRetainInstance(false);
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			// Restore last state for checked position.
			shownId = savedInstanceState.getLong(FieldGuideEntry.ID, 0L);
		} else {
			Bundle arguments = getArguments();
			if (arguments != null) {
				shownId = arguments.getLong(FieldGuideEntry.ID, 0L);
			} else {
				shownId = 0L;
			}
		}

		Log.d(TAG, "onCreateView: Id found in savedInstanceState: " + shownId);

		// Inflate the layout for this fragment
		entry = inflater.inflate(R.layout.entry_sighting, container, false);
		commonView = (TextView) entry
				.findViewById(R.id.common_fieldguide_entry);
		latinView = (TextView) entry.findViewById(R.id.latin_fieldguide_entry);
		descriptionView = (TextView) entry
				.findViewById(R.id.description_fieldguide_entry);
		imageView = (ImageView) entry.findViewById(R.id.image_fieldguide_entry);
		sightingbuttons = (LinearLayout) entry
				.findViewById(R.id.sighting_buttons_layout);
		group = (RadioGroup) entry.findViewById(R.id.sighting_radiogroup);
		checkboxgroup = (LinearLayout) entry
				.findViewById(R.id.sighting_checkboxes);

		buttons = new HashMap<String, RadioButton>();

		group.setOrientation(RadioGroup.HORIZONTAL);

		ArrayList<String> sightingChoices = LibApp
				.getCurrentCatalogSightingChoices();
		Catalog currentCatalog = LibApp.getInstance().getCurrentCatalog();
		LayoutInflater inflater1 = (getActivity()).getLayoutInflater();
		if (sightingChoices != null) {
			int buttonId = 1;
			for (String sightingChoice : sightingChoices) {
				RadioButton button = (RadioButton) inflater1.inflate(
						R.layout.radiobutton_big, null); // and all that just to
															// get the correct
															// style
				String showSightingChoice = currentCatalog
						.getResourcedValue(currentCatalog.getValuesMapping(),
								sightingChoice, null);
				button.setText(showSightingChoice);
				button.setId(buttonId);
				buttonId++;
				button.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View view) {
						((RadioGroup) view.getParent()).check(view.getId());
						RadioButton button = (RadioButton) view;
						showSightingValue = button.getText().toString();
						sightingValue = SightingsFieldGuideSimpleCursorAdapter.valuesResourcedToDb
								.get(showSightingValue);
						if (sightingValue == null)
							sightingValue = showSightingValue;
						saveSightingInDive();
					}
				});
				buttons.put(sightingChoice, button);
				group.addView(button);

			}
		}
		group.setOnCheckedChangeListener(this);
		buttongroup = group;

		allCheckBoxes = new HashMap<String, CheckBox>();
		ArrayList<String> checkChoices = LibApp
				.getCurrentCatalogCheckBoxChoices();
		List<String> showCheckChoices = Sighting.getResourcedCheckedValues(
				currentCatalog.getName(), checkChoices);
		if (checkChoices != null) {
			for (String check : checkChoices) {
				CheckBox box = (CheckBox) inflater.inflate(R.layout.checkbox,
						null); // and all that just to get the correct style
				box.setText(check);
				allCheckBoxes.put(check, box);
				checkboxgroup.addView(box);

				box.setOnClickListener(this);
			}
		}

		if (shownId != 0) {
			setData(shownId);
		}
		return entry;
	}

	protected void saveSightingInDive() {
		Log.d(TAG, "saveSightingInDive");

		// just to check there is at least one not empty string in the
		// checkedValues
		StringBuilder builder = new StringBuilder();
		if (checkedValues != null) {
			for (String value : checkedValues) {
				builder.append(value);
			}
		}

		String defaultChoice = LibApp.getDiveOrPersonalOrCatalogDefaultChoice();
		boolean worthSaving = ((sightingValue != null && !sightingValue
				.equals(defaultChoice)) || (checkedValues != null
				&& checkedValues.size() > 0 && builder.toString().length() > 0));

		if (sighting != null) {
			sighting.data.setSightingValue(sightingValue);
			sighting.data.setCsCheckedValues(checkedValues);
		}

		Log.d(TAG, "saveSightingInDive worthSaving[" + worthSaving + "]["
				+ sightingValue + "][default: " + defaultChoice + "]["
				+ builder.toString().length() + "]");
		if (worthSaving) {
			((MainActivity) getActivity()).saveDive(sighting);
			SightingsFieldGuideSimpleCursorAdapter.setChangedSighting(
					sighting.fieldguide_id, sighting.data);
		} else {
			if (sighting != null) {
				Log.d(TAG, "saveSightingInDive delete[" + sighting + "]");
				((MainActivity) getActivity()).deleteSighting(sighting);
				SightingsFieldGuideSimpleCursorAdapter.setChangedSighting(
						sighting.fieldguide_id, new Sighting.SightingData(null,
								null, null));
			}
		}
	}

	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// super.onCreateOptionsMenu(menu,
		// inflater);

		inflater.inflate(R.menu.sightings_entry, menu);
        ActionBarActivity act = MainActivity.me;
        if(act!= null && act.getSupportActionBar()!=null) {
            act.getSupportActionBar().setHomeButtonEnabled(true);
        }else{
            Log.d(TAG, "no Activity to set the HomeButton enabled for");
        }
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(TAG, "onOptionsItemSelected: [" + item.getItemId() + "]");
		switch (item.getItemId()) {
		case android.R.id.home:
			Log.d(TAG, "onOptionsItemSelected: poppingBackStack");
			getFragmentManager().popBackStackImmediate();
			return true;
		}
		return super.onOptionsItemSelected(item);

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(FieldGuideEntry.ID, shownId);
	}

	public long getShownId() {
		return shownId;
	}

	public void setData(long fieldguideId) {
		Log.d(TAG, "setData fieldguideId[" + fieldguideId + "]");
		if (fieldguideId != 0) {
			shownId = fieldguideId;
		} else {
			fieldguideId = shownId;
		}

		if (sighting == null) {
			Uri uri = null;
			if (fieldguideId != 0) {
				uri = Uri.withAppendedPath(
						Sighting.CONTENT_URI_DIVE_FLESHEDOUT,
						MainActivity.me.currentDive.getDiveNr()
								+ MarineLifeContentProvider.SEPARATOR
								+ Long.toString(fieldguideId));
			} else {
				uri = Uri.withAppendedPath(
						Sighting.CONTENT_URI_DIVE_FLESHEDOUT,
						MainActivity.me.currentDive.getDiveNr() + "");
				// dan heb je dus altijd de eerste entry van de list cursor -
				// mag niet voorkomen
			}
			Cursor cursor = null;
			int counter = 0;
			while (cursor == null && counter < 5) {
				cursor = getActivity().getContentResolver().query(uri,
						null, null, null, null);
				counter++;
			}
			if (cursor != null) {
				cursor.moveToFirst();
				sighting = FieldGuideAndSightingsEntryDbHelper
						.getSightingFromCursor(cursor);
				cursor.close();
			}
		}

		Log.d(TAG, "setData sighting: " + sighting);

		if (commonView != null && sighting != null) {
			latinView.setText(sighting.fg_entry.latinName);
			commonView.setText(sighting.fg_entry.getShowCommonName());
			descriptionView
					.setText(sighting.fg_entry.getResourcedDescription());
			checkValues = sighting.fg_entry.getShowCheckValues();
			Map<String, List<String>> changedValues = SightingsFieldGuideSimpleCursorAdapter
					.getValuesForChangedSighting(sighting.fieldguide_id);
			if (changedValues != null && changedValues.size() > 0) {
				sightingValue = changedValues.keySet().iterator().next();
				checkedValues = changedValues.values().iterator().next();
			} else {
				sightingValue = sighting.data.sightingValue;
				checkedValues = sighting.data.csCheckedValues;
			}

			String result = sightingValue != null
					&& sightingValue.length() != 0 ? sightingValue : LibApp
					.getDiveOrPersonalOrCatalogDefaultChoice();
			setButtonValue(result);
			setCheckedValues(checkedValues);

			int screenSize = LibApp.getCurrentResources().getConfiguration().screenLayout
					& Configuration.SCREENLAYOUT_SIZE_MASK;
			int orientation = LibApp.getCurrentResources().getConfiguration().orientation;

			Bitmap bm = null;
			switch (screenSize) {
			case Configuration.SCREENLAYOUT_SIZE_LARGE:
			case Configuration.SCREENLAYOUT_SIZE_XLARGE:
				bm = sighting.fg_entry.getLpicBitmapAsExpansion();
				if (bm == null)
					bm = sighting.fg_entry.getSpicBitmapAsAsset();
				imageView.setImageBitmap(bm);
				break;
			default:
				switch (orientation) {
				case Configuration.ORIENTATION_LANDSCAPE:
					bm = sighting.fg_entry.getLpicBitmapAsExpansion();
					Log.d(TAG, "setData bm found for Lpic[" + bm + "]");
					if (bm == null)
						bm = sighting.fg_entry.getSpicBitmapAsAsset();
					imageView.setImageBitmap(bm);
					break;
				default:
					bm = sighting.fg_entry.getSpicBitmapAsAsset();
					imageView.setImageBitmap(bm);
					break;
				}
			}

		}

	}

	public void clearCheckboxes(String checkValues) {
		String[] checkers = null;
		if (checkValues != null) {
			checkers = checkValues.split(",");
		}
		if (allCheckBoxes != null) {
			for (CheckBox box : allCheckBoxes.values()) {
				box.setChecked(false);
				if (checkers != null) {
					// if one of the checkers equals the boxtext it should
					// be visible, otherwize not.
					boolean show = false;
					for (String checker : checkers) {
						if (box.getText().equals(checker)) {
							show = true;
						}
					}
					if (show) {
						box.setVisibility(View.VISIBLE);
					} else {
						box.setVisibility(View.GONE);
					}

				} else {
					// there are no checkers,so all boxes should be hidden
					box.setVisibility(View.GONE);
				}
			}
		}
	}

	public void setCheckedValues(List<String> csCheckedValues) {
		Log.d(TAG, "checkedValues[" + csCheckedValues + "]");
		Catalog currentCatalog = LibApp.getInstance().getCurrentCatalog();
		this.checkedValues = csCheckedValues;
		this.showCheckedValues = Sighting.getResourcedCheckedValues(
				currentCatalog.getName(), csCheckedValues);

		// first make sure that only the checkboxes that are valid for this
		// species are visible
		Log.d(TAG, "checkValues[" + showCheckedValues + "]");
		clearCheckboxes(checkValues); // Visibility and all unchecked
		if (csCheckedValues != null && allCheckBoxes != null) {
			for (String check : showCheckedValues) {
				CheckBox box = allCheckBoxes.get(check);
				if (box != null)
					box.setChecked(true);
			}
		}
	}

	public void setButtonValue(String buttonValue) {
		Log.d(TAG, "setButtonValue[" + buttonValue + "]");
		Catalog currentCatalog = LibApp.getInstance().getCurrentCatalog();
		sightingValue = buttonValue;
		showSightingValue = currentCatalog.getResourcedValue(
				currentCatalog.getValuesMapping(), sightingValue, null);
		RadioButton button = buttons.get(showSightingValue);
		if (button != null) {
			Log.d(TAG, "setButtonValue[" + showSightingValue + "] - checked");
			button.setChecked(true);
		}

	}

	public void onBackStackChanged() {
        ActionBarActivity act = MainActivity.me;
        ActionBar bar = act.getSupportActionBar();
		act.getSupportActionBar().setHomeButtonEnabled(true);
		int backStackEntryCount = getActivity()
				.getSupportFragmentManager().getBackStackEntryCount();
		Log.d(TAG, "backstackEntryCount[" + backStackEntryCount + "]");
		if (backStackEntryCount > 0) {
			getActivity().getActionBar()
					.setDisplayHomeAsUpEnabled(true);
		} else {
			getActivity().getActionBar()
					.setDisplayHomeAsUpEnabled(false);
		}

	}

	@Override
	public void onStop() {
		// getActivity().getSupportFragmentManager().removeOnBackStackChangedListener(this);
		super.onStop();
	}

	@Override
	public void onDestroy() {
		// getActivity().getSupportFragmentManager().removeOnBackStackChangedListener(this);
		super.onDestroy();
	}

	@Override
	public void onStart() {
		// getActivity().getSupportFragmentManager().addOnBackStackChangedListener(this);
		super.onStart();
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		int checkedColor = LibApp.getCurrentResources()
				.getColor(R.color.orange);
		int uncheckedColor = LibApp.getCurrentResources()
				.getColor(R.color.dark);
		for (int j = 0; j < buttongroup.getChildCount(); j++) {
			RadioButton view = (RadioButton) buttongroup.getChildAt(j);
			int viewId = view.getId();
			Log.d(TAG, "onCheckedChanged[" + viewId + "][" + checkedId + "]");
			if (viewId == checkedId) {
				showSightingValue = view.getText().toString();
				view.setChecked(true);
				view.setTextColor(checkedColor);
			} else {
				view.setChecked(false);
				view.setTextColor(uncheckedColor);
			}
		}
		saveSightingInDive();
	}

	@Override
	// creating an anonymous inner class created problems
	// this seems to work
	public void onClick(View view) {
		Log.d(TAG, "checkbox onClickListener");
		CheckBox box = (CheckBox) view;
		String text = box.getText().toString();
		boolean checked = box.isChecked();

		if (checkedValues == null)
			checkedValues = new ArrayList<String>();
		if (showCheckedValues == null)
			showCheckedValues = new ArrayList<String>();
		int index = showCheckedValues.indexOf(text);
		if (checked && index == -1) {
			showCheckedValues.add(text);
		} else {
			showCheckedValues.remove(index);
		}
		checkedValues.clear();
		for (String showCheckedValue : showCheckedValues) {
			String checkedValue = SightingsFieldGuideSimpleCursorAdapter.valuesResourcedToDb
					.get(showCheckedValue);
			checkedValues.add(checkedValue);
		}

		saveSightingInDive();

	}
}
