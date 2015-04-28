package nl.imarinelife.lib.divinglog.sightings;

import nl.imarinelife.lib.LibApp;
import nl.imarinelife.lib.MainActivity;
import nl.imarinelife.lib.Preferences;
import nl.imarinelife.lib.R;
import nl.imarinelife.lib.catalog.Catalog;
import nl.imarinelife.lib.fieldguide.db.FieldGuideAndSightingsEntryDbHelper;
import nl.imarinelife.lib.fieldguide.db.FieldGuideEntry;
import nl.imarinelife.lib.utility.DataTextView;
import nl.imarinelife.lib.utility.FilterCursorWrapper;
import nl.imarinelife.lib.utility.SingletonCursor;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SightingsAsIsSimpleCursorAdapter extends SimpleCursorAdapter {

	private final static String TAG = "SghtngAsIsSCAdapter";
	public static String ID = "id";

	public SightingsAsIsSimpleCursorAdapter(Context context, int layout,
			Cursor c, String[] from, int[] to, int flag) {
		super(context, layout, c, from, to, flag);

		ViewBinder binder = new ViewBinder() {
			@Override
			public boolean setViewValue(android.view.View view, Cursor cursor,
					int columnIndex) {
				Log.d(TAG, "setViewValue for columnIndex[" + columnIndex + "]");
				String entryCatalog = cursor
						.getString(FieldGuideAndSightingsEntryDbHelper.KEY_CATNAME_CURSORLOC);
				Catalog currentCatalog = LibApp.getInstance()
						.getCurrentCatalog();
				String group_code_from_db = cursor
						.getString(FieldGuideAndSightingsEntryDbHelper.KEY_GROUPNAME_CURSORLOC);
				Log.d(TAG, "currentCatalog[" + currentCatalog + "]");
				Log.d(TAG, "group_name_from_db[" + group_code_from_db + "]");
				
				switch (columnIndex) {
				case FieldGuideAndSightingsEntryDbHelper.KEY_SIGHTING_VALUE_CURSORLOC:
					Log.d(TAG,
							"setViewValue for columnIndex["
									+ columnIndex
									+ "] "
									+ FieldGuideAndSightingsEntryDbHelper.KEY_SIGHTING_VALUE);

					String showSightingValue = cursor
							.getString(FieldGuideAndSightingsEntryDbHelper.KEY_SHOW_SIGHTING_VALUE_CURSORLOC);
					Log.d(TAG, "showSightingValue: "+showSightingValue);
					FieldGuideAndSightingsEntryDbHelper.logCursorEntry(cursor, FieldGuideAndSightingsEntryDbHelper.ALL_SIGHTINGS);
					String value = currentCatalog.getResourcedValue(
							cursor.getString(FieldGuideAndSightingsEntryDbHelper.KEY_CATNAME_CURSORLOC),
							currentCatalog.getValuesMapping(), cursor.getString(columnIndex), showSightingValue);
					
					TextView textView = (TextView) view;
					textView.setText("Aanwezig: "+value);
					return true;
				case FieldGuideAndSightingsEntryDbHelper.KEY_FIELDGUIDE_ID_CURSORLOC:
					int id = cursor.getInt(columnIndex);
					ImageView spicView = (ImageView) view;
					Bitmap bm = null;
					if (!Preferences.listHasValue(
							Preferences.SIGHTINGS_GROUPS_HIDDEN,
							group_code_from_db)) {
						if (entryCatalog.equals(LibApp.getCurrentCatalogName())) {
							bm = FieldGuideEntry
									.getSpicBitmapAsAssetFromFieldGuideId(id);
						} else {
							String packagename = "nl.imarinelife."
									+ entryCatalog.toLowerCase();
							try {
								Context otherContext = mContext
										.createPackageContext(packagename, 0);
								AssetManager manager = otherContext.getAssets();
								bm = FieldGuideEntry
										.getSpicBitmapAsAssetFromFieldGuideId(
												id, manager);
							} catch (NameNotFoundException e) {
								Log.d(TAG, packagename + ":" + id
										+ " not found", e);
								bm = FieldGuideEntry
										.getSpicBitmapAsAssetFromFileName("no-picture-icon.png");
							}
						}
					}
					spicView.setImageBitmap(bm);
					return true;
				case FieldGuideAndSightingsEntryDbHelper.KEY_CS_CHECKEDVALUES_CURSORLOC:
					Log.d(TAG,
							"setViewValue for columnIndex["
									+ columnIndex
									+ "] "
									+ FieldGuideAndSightingsEntryDbHelper.KEY_CS_CHECKEDVALUES);
					String checkedValues = FieldGuideEntry
							.getResourcedCheckValuesForCurrentCatalog(entryCatalog,
									cursor.getString(columnIndex));

					TextView checkValuesView = (TextView) view;
					checkValuesView.setText(checkedValues);

					return true;
				case FieldGuideAndSightingsEntryDbHelper.KEY_COMMONNAME_CURSORLOC:
					TextView commonName = (TextView) view;
					String showCommonName = cursor
							.getString(FieldGuideAndSightingsEntryDbHelper.KEY_SHOWCOMMONNAME_CURSORLOC);
					String common_name = currentCatalog
							.getResourcedValue(
									cursor.getString(FieldGuideAndSightingsEntryDbHelper.KEY_CATNAME_CURSORLOC),
									currentCatalog.getCommonIdMapping(), cursor
											.getString(columnIndex), showCommonName);
					commonName.setText(common_name);
					return true;
				case FieldGuideAndSightingsEntryDbHelper.KEY_GROUPNAME_CURSORLOC:
					TextView groupName = (TextView) view;
					String showGroupName = cursor
							.getString(FieldGuideAndSightingsEntryDbHelper.KEY_SHOWGROUPNAME_CURSORLOC);
					String group_name = currentCatalog
							.getResourcedValue(
									cursor.getString(FieldGuideAndSightingsEntryDbHelper.KEY_CATNAME_CURSORLOC),
									currentCatalog.getGroupIdMapping(),
									group_code_from_db, showGroupName);
					groupName.setText(group_name);
					Log.d(TAG, groupName + " " + groupName.getText().toString()
							+ groupName.getTextSize());

					if (view instanceof DataTextView) {
						((DataTextView) view).setData(MainActivity.DB_VALUE,
								group_code_from_db);
						Log.d(TAG, "groupName - data Set with ["
								+ group_code_from_db + "]");
					}

					ViewParent parent = ((ViewParent) groupName.getParent());
					LinearLayout headerLayout = (LinearLayout) parent;
					ViewParent row = headerLayout.getParent();
					View body = ((View) row)
							.findViewById(R.id.body_sightings_asis_layout);

					// show header or not
					boolean group_first = true;
					String group_name_previous = "";
					if (!cursor.isFirst()) {
						cursor.move(-1);
						group_name_previous = cursor.getString(columnIndex);
						cursor.move(1);
						if (group_name_previous.equals(group_code_from_db)) {
							group_first = false;
						}
					}
					Log.d(TAG, group_name + " show header[" + group_first + "]");
					if (group_first) {
						headerLayout.setVisibility(View.VISIBLE);
					} else {
						Log.d(TAG, "should NOT show header");
						headerLayout.setVisibility(View.GONE);
					}

					// show body or not
					Log.d(TAG,
							""
									+ Preferences
											.getString(
													Preferences.SIGHTINGS_GROUPS_HIDDEN,
													""));
					Log.d(TAG,
							group_code_from_db
									+ " "
									+ Preferences
											.listHasValue(
													Preferences.SIGHTINGS_GROUPS_HIDDEN,
													group_code_from_db));
					if (!Preferences.listHasValue(
							Preferences.SIGHTINGS_GROUPS_HIDDEN,
							group_code_from_db)) {
						Log.d(TAG, "should show content row");
						body.setVisibility(View.VISIBLE);
					} else {
						Log.d(TAG, "should NOT show content row");
						body.setVisibility(View.GONE);
					}
					return true;
				case FieldGuideAndSightingsEntryDbHelper.KEY_CATNAME_CURSORLOC:
					// CATNAME is a place holder to get the right view
					TextView amounts = (TextView) view;
					String group_name1 = cursor
							.getString(FieldGuideAndSightingsEntryDbHelper.KEY_GROUPNAME_CURSORLOC);

					amounts.setText(FilterCursorWrapper.getAmounts(group_name1));

					return true;
				}
				return false;
			};
		};
		setViewBinder(binder);

	}

	@Override
	public Cursor swapCursor(Cursor c) {
		super.swapCursor(c);
		Cursor toReturn = SingletonCursor.swapCursor(c);
		notifyDataSetChanged();
		return toReturn;

	}

}
