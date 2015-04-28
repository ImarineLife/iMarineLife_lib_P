package nl.imarinelife.lib.divinglog.sightings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.imarinelife.lib.LibApp;
import nl.imarinelife.lib.MainActivity;
import nl.imarinelife.lib.Preferences;
import nl.imarinelife.lib.R;
import nl.imarinelife.lib.catalog.Catalog;
import nl.imarinelife.lib.divinglog.db.dive.Dive;
import nl.imarinelife.lib.divinglog.sightings.Sighting.SightingData;
import nl.imarinelife.lib.fieldguide.db.FieldGuideAndSightingsEntryDbHelper;
import nl.imarinelife.lib.fieldguide.db.FieldGuideEntry;
import nl.imarinelife.lib.utility.DataTextView;
import nl.imarinelife.lib.utility.FilterCursorWrapper;
import nl.imarinelife.lib.utility.SerializableSparseArray;
import nl.imarinelife.lib.utility.SingletonCursor;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class SightingsFieldGuideSimpleCursorAdapter extends SimpleCursorAdapter {

	private final static String TAG = "SghtngFldGdSimpleCAdapt";
	public static String ID = "id";
	private final int uncheckedColor;
	private final int checkedColor;
	private Dive dive = null;

	private static SerializableSparseArray<Sighting.SightingData> changedSightings1 = new SerializableSparseArray<Sighting.SightingData>();
	private static SerializableSparseArray<Sighting.SightingData> changedSightings2 = new SerializableSparseArray<Sighting.SightingData>();
	private static boolean dubbel = false;
	private final static int CHANGES_BEFORE_REQUERY = 15;
	private int changesToGo = CHANGES_BEFORE_REQUERY;

	// should always be filled with the actual resourcedvalues for the current locale for the currentcatalog
	// however... if turning from one locale to another fails.. so will this
	public static Map<String, String> valuesResourcedToDb = new HashMap<String, String>();

	static {
		Catalog currentCatalog = LibApp.getInstance().getCurrentCatalog();
		Map<String, Integer> namesAndResourceIds = currentCatalog
				.getValuesMapping();
		if (namesAndResourceIds != null) {
			for (String dbValue : namesAndResourceIds.keySet()) {
				String resourceValue = currentCatalog.getResourcedValue(
						currentCatalog.getValuesMapping(), dbValue, null);
				if (resourceValue != null) {
					valuesResourcedToDb.put(resourceValue, dbValue);
				}
			}
		}
	}

	public SightingsFieldGuideSimpleCursorAdapter(Context context, int layout,
			Cursor c, String[] from, int[] to, int flag, Dive dive) {
		super(context, layout, c, from, to, flag);
		checkedColor = LibApp.getCurrentResources().getColor(R.color.orange);
		uncheckedColor = LibApp.getCurrentResources().getColor(R.color.dark);
		this.dive = dive;
	}

	@Override
	public void bindView(View row, Context context, Cursor cursor) {
		SightingHolder holder = (SightingHolder) row.getTag();
		Sighting sighting = FieldGuideAndSightingsEntryDbHelper
				.getSightingFromCursor(cursor);

		String group_name_from_db = cursor
				.getString(FieldGuideAndSightingsEntryDbHelper.KEY_GROUPNAME_CURSORLOC);

		Sighting.SightingData changedSightingData = changedSightings1
				.get(sighting.fieldguide_id);
		Log.d(TAG, "bindView ["+sighting+"][" + changedSightingData + "]");

		if (changedSightingData != null) {
			if (changedSightingData.sightingValue == null) {
				sighting.data.setSightingValue(LibApp.getDiveOrPersonalOrCatalogDefaultChoice());
				sighting.data.setCsCheckedValues(null);
			} else {
				sighting.data.setSightingValue(changedSightingData.sightingValue);
				sighting.data.setCsCheckedValues(changedSightingData.csCheckedValues);
			}
		}

		if (holder == null || holder.common_name == null) {
			holder = initializeHolder(row);
		}

		String entryCatalog = cursor
				.getString(FieldGuideAndSightingsEntryDbHelper.KEY_CATNAME_CURSORLOC);
		Catalog currentCatalog = LibApp.getInstance().getCurrentCatalog();

		holder.fieldguideId = cursor
				.getInt(FieldGuideAndSightingsEntryDbHelper.KEY_FIELDGUIDE_ID_CURSORLOC);

		Bitmap smallPic = null;
		if (!Preferences.listHasValue(Preferences.SIGHTINGS_GROUPS_HIDDEN,
				group_name_from_db)) {
			smallPic = FieldGuideEntry
					.getSpicBitmapAsAssetFromFieldGuideId(holder.fieldguideId);
		}

		Log.d(TAG, "bindView setting holder from cursor");
		holder.spicBitMap = smallPic;
		holder.orderId = cursor
				.getInt(FieldGuideAndSightingsEntryDbHelper.KEY_ORDERNR_CURSORLOC);
		holder.remarks = cursor
				.getString(FieldGuideAndSightingsEntryDbHelper.KEY_REMARKS_CURSORLOC);
		holder.checkValues = FieldGuideEntry
				.getResourcedCheckValuesForCurrentCatalog(
						entryCatalog,
						cursor.getString(FieldGuideAndSightingsEntryDbHelper.KEY_CS_CHECKVALUES_CURSORLOC));
		
		holder.sighting = sighting;
		holder.spic.setImageBitmap(holder.spicBitMap);
		
		String showCommonName = cursor
				.getString(FieldGuideAndSightingsEntryDbHelper.KEY_SHOWCOMMONNAME_CURSORLOC);
		String commonName = currentCatalog
				.getResourcedValue(
						cursor.getString(FieldGuideAndSightingsEntryDbHelper.KEY_CATNAME_CURSORLOC),
						currentCatalog.getCommonIdMapping(),
						cursor.getString(FieldGuideAndSightingsEntryDbHelper.KEY_COMMONNAME_CURSORLOC),
						showCommonName);
		holder.common_name.setText(commonName);

		String showGroupName = cursor
				.getString(FieldGuideAndSightingsEntryDbHelper.KEY_SHOWGROUPNAME_CURSORLOC);
		String groupName = currentCatalog
				.getResourcedValue(
						cursor.getString(FieldGuideAndSightingsEntryDbHelper.KEY_CATNAME_CURSORLOC),
						currentCatalog.getGroupIdMapping(), group_name_from_db, showGroupName);
		holder.group_name.setText(groupName);
		holder.group_name.setData(MainActivity.DB_VALUE, group_name_from_db);
		
		holder.amounts.setText(FilterCursorWrapper.getAmounts(groupName));
		
		String showButtonValue = (sighting != null && sighting.data != null) ? sighting.data.showSightingValue
				: LibApp.getCurrentCatalogDefaultChoice();
		holder.setButtonValue(showButtonValue);
	
		List<String> checkedValues = (sighting != null && sighting.data != null) ? sighting.data.csCheckedValues
				: null;
		holder.setCheckedValues(cursor.getString(FieldGuideAndSightingsEntryDbHelper.KEY_CATNAME_CURSORLOC), checkedValues);

		holder.setVisibility(cursor, row);

		Log.d(TAG, "setViewValue for holder["+holder+"]");

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		Log.d(TAG, "newView, cursor["
				+ (cursor != null ? cursor.getPosition() : "null") + "]");
		LayoutInflater inflater = ((Activity) super.mContext)
				.getLayoutInflater();
		View row = inflater.inflate(R.layout.listview_sightings_fieldguide_row,
				parent, false);
		// a holder is created for every visible listview row.
		// listview rows, and therefore holders are reused.
		SightingHolder holder = initializeHolder(row);
		row.setTag(holder);
		return row;
	}

	private SightingHolder initializeHolder(final View row) {
		Log.d(TAG, "initializeHolder");
		final SightingHolder holder = new SightingHolder(false);
		holder.spic = (ImageView) row.findViewById(R.id.sighting_image_list);
		holder.group_name = (DataTextView) row
				.findViewById(R.id.header_sightings_all_list_groupname);
		holder.defaultChoice1 = (TextView) row
				.findViewById(R.id.header_sightings_all_list_default1);
		holder.defaultChoice1.setText(LibApp
				.getFirstDefaultableCatalogSightingChoice());
		holder.defaultChoice2 = (TextView) row
				.findViewById(R.id.header_sightings_all_list_default2);
		String second = LibApp.getSecondDefaultableCatalogSightingChoice();
		holder.defaultChoice2.setText(second);
		if (second.length() == 0) {
			holder.defaultChoice2.setClickable(false);
		} else {
			holder.defaultChoice2.setClickable(true);
		}
		holder.amounts = (TextView) row
				.findViewById(R.id.header_sightings_all_list_amounts);
		holder.common_name = (TextView) row
				.findViewById(R.id.sighting_name_list);
		holder.common_name.setText("");
		holder.showButtonValue = null;
		holder.showCheckedValues = null;
		holder.values = (LinearLayout) row
				.findViewById(R.id.sighting_values_layout);

		holder.buttons = new HashMap<String, RadioButton>();
		RadioGroup group = new RadioGroup(super.mContext);
		group.setOrientation(RadioGroup.HORIZONTAL);
		ArrayList<String> sightingChoices = LibApp
				.getCurrentCatalogSightingChoices(); // is values as they will
														// be stored
		LayoutInflater inflater = ((Activity) super.mContext)
				.getLayoutInflater();
		Catalog catalog = LibApp.getInstance().getCurrentCatalog();
		if (sightingChoices != null) {
			int buttonId = 1;
			for (String sightingChoice : sightingChoices) {
				RadioButton button = (RadioButton) inflater.inflate(
						R.layout.radiobutton, null); // and all that just to get
														// the correct style
				String resourcedSightingChoice = catalog.getResourcedValue(
						catalog.getValuesMapping(), sightingChoice, null);
				button.setText(resourcedSightingChoice);
				button.setId(buttonId);
				buttonId++;
				button.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View view) {
						((RadioGroup) view.getParent()).check(view.getId());
						saveSightingInDive(row);
					}
				});
				holder.buttons.put(resourcedSightingChoice, button);
				group.addView(button);

			}
		}
		group.setOnCheckedChangeListener(holder);
		holder.buttongroup = group;
		holder.values.addView(holder.buttongroup);

		LinearLayout checkboxgroup = (LinearLayout) inflater.inflate(
				R.layout.checkbox_linearlayout, null);
		holder.values.addView(checkboxgroup);
		holder.allCheckBoxes = new HashMap<String, CheckBox>();
		ArrayList<String> checkChoices = LibApp
				.getCurrentCatalogCheckBoxChoices(); // as stored
		if (checkChoices != null) {
			for (String check : checkChoices) {
				CheckBox box = (CheckBox) inflater.inflate(R.layout.checkbox,
						null); // and all that just to get the correct style
				String resourcedCheck = catalog.getResourcedValue(
						catalog.getValuesMapping(), check, null);
				box.setText(resourcedCheck);
				holder.allCheckBoxes.put(resourcedCheck, box);
				checkboxgroup.addView(box);
				box.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View view) {
						Log.d(TAG, "checkbox onClickListener");
						CheckBox box = (CheckBox) view;
						String text = box.getText().toString();
						boolean checked = box.isChecked();

						if (holder.showCheckedValues == null)
							holder.showCheckedValues = new ArrayList<String>();
						int index = holder.showCheckedValues.indexOf(text);
						if (checked && index == -1) {
							holder.showCheckedValues.add(text);
						} else {
							holder.showCheckedValues.remove(index);
						}

						saveSightingInDive(row);
					}
				});
			}
		}

		return holder;
	}

	protected void saveSightingInDive(View row) {
		SightingHolder holder = (SightingHolder) row.getTag();
		Log.d(TAG, "saveSightingInDive[" + holder + "]");

		if (holder != null) {
			StringBuilder builder = new StringBuilder();
			List<String> storeValues = new ArrayList<String>();
			if (holder != null && holder.showCheckedValues != null) {
				boolean first = false;
				for (String value : holder.showCheckedValues) {
					if (!first)
						builder.append(",");
					String dbValue = valuesResourcedToDb.get(value);
					if (dbValue == null)
						dbValue = value;
					builder.append(dbValue);
					storeValues.add(dbValue);
				}
			}
			String storeButtonValue = valuesResourcedToDb
					.get(holder.showButtonValue);
			if (storeButtonValue == null)
				storeButtonValue = holder.showButtonValue;

			boolean worthSaving = ((storeButtonValue != null && !storeButtonValue.equals(LibApp
					.getDiveOrPersonalOrCatalogDefaultChoice())) || (storeValues != null
					&& storeValues.size() > 0 && builder.toString().length() > 0));
			String common_name = holder.common_name.getText().toString();
			String group_name = holder.group_name.getText().toString();
			if (holder.sighting == null && worthSaving) {
				holder.sighting = new Sighting(holder.fieldguideId,
						dive.getDiveNr(), LibApp.getCurrentCatalogName(),
						group_name, common_name, holder.orderId,
						storeButtonValue, storeValues,
						holder.remarks);
				holder.sighting.data.showSightingValue = holder.showButtonValue;
				holder.sighting.data.showCsCheckedValues = holder.showCheckedValues;
			} else {
				if (holder.sighting != null) {
					holder.sighting.group_name = group_name;
					holder.sighting.common_name = common_name;
					holder.sighting.fieldguide_id = holder.fieldguideId;
					holder.sighting.orderNr = holder.orderId;
					holder.sighting.data.remarks = holder.remarks;
					holder.sighting.data.setSightingValue(storeButtonValue);
					holder.sighting.data.setCsCheckedValues(storeValues);
				}
			}

			Sighting sighting = holder.sighting;
			
			Log.d(TAG, "saveSightingInDive worthSaving[" + worthSaving + "]["
					+ storeButtonValue + "][" + builder.toString().length()
					+ "]");

			if (worthSaving || sighting != null) {
				if (worthSaving) {
					Sighting.SightingData changedSighting = new Sighting.SightingData(
							holder.sighting.data.sightingValue, holder.sighting.data.csCheckedValues,
							holder.remarks);
					setChangedSighting(holder.sighting.fieldguide_id,
							changedSighting);
				} else {
					Sighting.SightingData removedSighting = new Sighting.SightingData(
							null, null, null);
					setChangedSighting(holder.sighting.fieldguide_id,
							removedSighting);
				}

				AsyncTask<Object, Void, Cursor> task = new AsyncTask<Object, Void, Cursor>() {
					@Override
					protected Cursor doInBackground(Object... params) {
						Sighting sighting = (Sighting) params[0];
						Boolean worthSaving = (Boolean) params[1];
						if (worthSaving) {
							((MainActivity)mContext).saveDive(sighting);
						} else {
							((MainActivity)mContext).deleteSighting(sighting);
						}
						synchronized (SingletonCursor.getCursor()) {
							changesToGo--;
							if (changesToGo == 0) {
								dubbel = true;
								FieldGuideAndSightingsEntryDbHelper dbHelper = FieldGuideAndSightingsEntryDbHelper
										.getInstance(mContext);
								Cursor newCursor = dbHelper
										.queryFieldGuideFilledForDive(sighting.diveNr);
								changesToGo = CHANGES_BEFORE_REQUERY;
								return newCursor;
							} else {
								return null;
							}
						}

					}

					protected void onPostExecute(Cursor newCursor) {
						Log.d(TAG,"changedSightings1["+changedSightings1+"] changedSightings2["+changedSightings2+"]");
						if (newCursor != null) {
							swapCursor(newCursor);
							changedSightings1 = changedSightings2;
							dubbel = false;
							changedSightings2 = new SerializableSparseArray<Sighting.SightingData>();
						}
					}

				};

				task.execute(holder.sighting, worthSaving);

			}
		}
	}

	@Override
	public Cursor swapCursor(Cursor c) {
		super.swapCursor(c);
		Cursor toReturn = SingletonCursor.swapCursor(c);
		Log.d(TAG, "swapCursor ");
		notifyDataSetChanged();
		return toReturn;

	}

	private class SightingHolder implements RadioGroup.OnCheckedChangeListener {
		public boolean isPlaceHolder = false;
		public DataTextView group_name;
		public TextView defaultChoice1;
		public TextView defaultChoice2;
		public TextView amounts;
		public String showButtonValue; // dbvalue
		public List<String> showCheckedValues; // dbvalues
		public ImageView spic;
		public TextView common_name;

		public LinearLayout values;
		public RadioGroup buttongroup;
		public HashMap<String, RadioButton> buttons;
		public HashMap<String, CheckBox> allCheckBoxes;

		public Sighting sighting;
		public int fieldguideId;
		public int orderId;
		public Bitmap spicBitMap;
		public String remarks;
		public String checkValues;

		public SightingHolder(boolean isPlaceHolder) {
			this.isPlaceHolder = isPlaceHolder;
		}

		public void setVisibility(Cursor cursor, View row) {
			LinearLayout headerLayout = (LinearLayout) ((View) row)
					.findViewById(R.id.header_sightings_all_list);
			View body = ((View) row)
					.findViewById(R.id.body_sightings_all_layout);
			String groupName = group_name.getText().toString();
			String groupNameFromDb = group_name.getData(MainActivity.DB_VALUE);

			// show header or not
			boolean group_first = true;
			String group_name_previous = "";
			if (!cursor.isFirst()) {
				cursor.move(-1);
				group_name_previous = cursor
						.getString(FieldGuideAndSightingsEntryDbHelper.KEY_GROUPNAME_CURSORLOC);
				cursor.move(1);
				if (group_name_previous.equals(groupNameFromDb)) {
					group_first = false;
				}
			}
			Log.d(TAG, groupName + " show header[" + group_first + "]");
			if (group_first) {
				headerLayout.setVisibility(View.VISIBLE);
			} else {
				Log.d(TAG, "should NOT show header");
				headerLayout.setVisibility(View.GONE);
			}

			// show body or not
			Log.d(TAG,
					""
							+ Preferences.getString(
									Preferences.SIGHTINGS_GROUPS_HIDDEN, ""));
			Log.d(TAG,
					groupNameFromDb
							+ " "
							+ Preferences.listHasValue(
									Preferences.SIGHTINGS_GROUPS_HIDDEN,
									groupNameFromDb));
			if (!Preferences.listHasValue(Preferences.SIGHTINGS_GROUPS_HIDDEN,
					groupNameFromDb)) {
				Log.d(TAG, "should show content row");
				body.setVisibility(View.VISIBLE);
			} else {
				Log.d(TAG, "should NOT show content row");
				body.setVisibility(View.GONE);
			}

		}

		@Override
		protected SightingHolder clone() throws CloneNotSupportedException {
			SightingHolder holder = new SightingHolder(false);
			holder.group_name = group_name;
			holder.defaultChoice1 = defaultChoice1;
			holder.defaultChoice2 = defaultChoice2;
			holder.amounts = amounts;
			holder.showButtonValue = showButtonValue;
			holder.showCheckedValues = showCheckedValues;
			holder.spic = spic;
			holder.common_name = common_name;
			holder.values = values;
			holder.buttongroup = buttongroup;
			holder.buttons = buttons;
			holder.allCheckBoxes = allCheckBoxes;
			holder.sighting = sighting;
			holder.fieldguideId = fieldguideId;
			holder.orderId = orderId;
			holder.remarks = remarks;
			holder.checkValues = checkValues;
			// spicBitmap not copied to conserve space;
			return holder;
		}

		public void clearCheckboxes(String checkValues) {
			String[] checkers = null;
			if (checkValues != null) {
				String resourcedCheckValues = FieldGuideEntry
						.getResourcedCheckValuesForCurrentCatalog(
								LibApp.getCurrentCatalogName(), checkValues);
				checkers = resourcedCheckValues.split(",");
			}
			if (allCheckBoxes != null) {
				for (CheckBox box : allCheckBoxes.values()) {
					box.setChecked(false);
					if (checkers != null) {
						// if one of the checkers equals the boxtext it should
						// be visible(not checked), otherwize not.
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

		public void setCheckedValues(String catalog, List<String> checkedValues2) {
			Log.d(TAG, "checkedValues[" + checkedValues2 + "]");
			List<String> showCheckedValues = FieldGuideEntry.getResourcedCheckValuesForCurrentCatalog(
					catalog, checkedValues2);
			this.showCheckedValues = showCheckedValues;

			// first make sure that only the checkboxes that are valid for this
			// species are visible
			Log.d(TAG, "checkValues[" + checkValues + "]");
			clearCheckboxes(checkValues); // Visibility and all unchecked
			if (showCheckedValues != null && allCheckBoxes != null) {
				for (String check : showCheckedValues) {
					CheckBox box = allCheckBoxes.get(check);
					if (box != null)
						box.setChecked(true);
				}
			}
		}

		public void setButtonValue(String showButtonValue) {
			Log.d(TAG, "setButtonValue[" + showButtonValue + "]");
			this.showButtonValue = showButtonValue;
			RadioButton button = buttons.get(showButtonValue);
			if (button != null) {
				Log.d(TAG, "setButtonValue[" + showButtonValue + "] - checked");
				button.setChecked(true);
			}
		}

		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("holder[");
			if (isPlaceHolder) {
				builder.append("isPlaceHolder");
			} else {
				builder.append(group_name.getText().toString() + ",");
				builder.append(amounts.getText().toString() + ",");
				builder.append(common_name.getText().toString() + ",");
				builder.append(showButtonValue + ",");
				builder.append(showCheckedValues + ",");
				builder.append("[" + allCheckBoxes.keySet() + "]" + ",");
				builder.append(sighting);
			}
			builder.append("]");
			return builder.toString();

		}

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			for (int j = 0; j < buttongroup.getChildCount(); j++) {
				RadioButton view = (RadioButton) buttongroup.getChildAt(j);
				int viewId = view.getId();
				Log.d(TAG, "onCheckedChanged[" + viewId + "][" + checkedId
						+ "]");
				if (viewId == checkedId) {
					showButtonValue = view.getText().toString();
					view.setChecked(true);
					view.setTextColor(checkedColor);
				} else {
					view.setChecked(false);
					view.setTextColor(uncheckedColor);
				}
			}
		}
	}

	public static Map<String, List<String>> getValuesForChangedSighting(
			int fieldguideId) {
		if (changedSightings1 == null)
			return null;

		Sighting.SightingData data = changedSightings1.get(fieldguideId);
		if (data == null)
			return null;
		else {
			Map<String, List<String>> map = new HashMap<String, List<String>>();
			map.put(data.sightingValue, data.csCheckedValues);
			return map;
		}
	}

	public static void setChangedSighting(int fieldguide_id,
			SightingData changedSighting) {
		changedSightings1.put(fieldguide_id, changedSighting);
		if (dubbel) {
			changedSightings2.put(fieldguide_id, changedSighting);
		}
	}
	

}
