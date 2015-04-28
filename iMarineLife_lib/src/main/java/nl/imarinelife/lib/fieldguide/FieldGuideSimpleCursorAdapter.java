package nl.imarinelife.lib.fieldguide;

import java.util.Map;

import nl.imarinelife.lib.LibApp;
import nl.imarinelife.lib.MainActivity;
import nl.imarinelife.lib.Preferences;
import nl.imarinelife.lib.R;
import nl.imarinelife.lib.catalog.Catalog;
import nl.imarinelife.lib.fieldguide.db.FieldGuideAndSightingsEntryDbHelper;
import nl.imarinelife.lib.fieldguide.db.FieldGuideEntry;
import nl.imarinelife.lib.utility.DataTextView;
import nl.imarinelife.lib.utility.FilterCursorWrapper;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FieldGuideSimpleCursorAdapter extends SimpleCursorAdapter {

	private final static String TAG = "FldGdSimpleCAdapter";
	public static String ID = "id";

	public FieldGuideSimpleCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flag) {
		super(context, layout, c, from, to, flag);

		ViewBinder binder = new ViewBinder() {
			@Override
			public boolean setViewValue(android.view.View view, Cursor cursor,
					int columnIndex) {
				Log.d(TAG, "setViewValue for columnIndex[" + columnIndex + "]["
						+ cursor.getString(columnIndex) + "]");
				Catalog cat = LibApp.getInstance().getCurrentCatalog();
				String group_code_from_db = cursor
						.getString(FieldGuideAndSightingsEntryDbHelper.KEY_GROUPNAME_CURSORLOC);

				switch (columnIndex) {
				case FieldGuideAndSightingsEntryDbHelper.KEY_ROWID_CURSORLOC:
					ImageView spicView = (ImageView) view;
					if (!Preferences.listHasValue(
							Preferences.FIELDGUIDE_GROUPS_HIDDEN,
							group_code_from_db)) {
						Log.d(TAG, "should show content row");
						Bitmap bm = FieldGuideEntry
								.getSpicBitmapAsAssetFromFieldGuideId(cursor
										.getInt(columnIndex));
						spicView.setImageBitmap(bm);
						return true;
					} else {
						Log.d(TAG, "should NOT show content row");
						spicView.setImageBitmap(null);
						return true;
					}

				case FieldGuideAndSightingsEntryDbHelper.KEY_COMMONNAME_CURSORLOC:
					TextView commonName = (TextView) view;
					String catName = cursor
							.getString(FieldGuideAndSightingsEntryDbHelper.KEY_CATNAME_CURSORLOC);
					Map<String, Integer> commonMapping = cat
							.getCommonIdMapping();
					String codedDbValue = cursor.getString(columnIndex);
					String showDbValue = cursor
							.getString(FieldGuideAndSightingsEntryDbHelper.KEY_SHOWCOMMONNAME_CURSORLOC);
					String common_name = cat.getResourcedValue(catName,
							commonMapping, codedDbValue, showDbValue);
					commonName.setText(common_name);
					return true;
				case FieldGuideAndSightingsEntryDbHelper.KEY_GROUPNAME_CURSORLOC:
					TextView groupName = (TextView) view;
					String showGroupValue = cursor
							.getString(FieldGuideAndSightingsEntryDbHelper.KEY_SHOWGROUPNAME_CURSORLOC);
					String group_name = cat
							.getResourcedValue(
									cursor.getString(FieldGuideAndSightingsEntryDbHelper.KEY_CATNAME_CURSORLOC),
									cat.getGroupIdMapping(), group_code_from_db, showGroupValue);
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
							.findViewById(R.id.body_fieldguide_layout);

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
													Preferences.FIELDGUIDE_GROUPS_HIDDEN,
													""));
					Log.d(TAG,
							group_code_from_db
									+ " "
									+ Preferences
											.listHasValue(
													Preferences.FIELDGUIDE_GROUPS_HIDDEN,
													group_code_from_db));
					if (!Preferences.listHasValue(
							Preferences.FIELDGUIDE_GROUPS_HIDDEN,
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

}
