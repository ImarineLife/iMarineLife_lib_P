package nl.imarinelife.lib.fieldguide.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.imarinelife.lib.DbHelper;
import nl.imarinelife.lib.LibApp;
import nl.imarinelife.lib.MainActivity;
import nl.imarinelife.lib.Preferences;
import nl.imarinelife.lib.R;
import nl.imarinelife.lib.catalog.Catalog;
import nl.imarinelife.lib.divinglog.db.dive.DiveDbHelper;
import nl.imarinelife.lib.divinglog.sightings.DivingLogSightingsListFragment;
import nl.imarinelife.lib.divinglog.sightings.Sighting;
import nl.imarinelife.lib.divinglog.sightings.SightingsFieldGuideSimpleCursorAdapter;
import nl.imarinelife.lib.fieldguide.FieldGuideListFragment;
import nl.imarinelife.lib.utility.SDCardSQLiteOpenHelper;
import nl.imarinelife.lib.utility.SQLUpdateObject;
import nl.imarinelife.lib.utility.SerializableSparseArray;
import nl.imarinelife.lib.utility.dialogs.ThreeChoiceDialogFragment;
import nl.imarinelife.lib.utility.events.LanguageChangeEvent;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MergeCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class FieldGuideAndSightingsEntryDbHelper implements DbHelper {

	private static FieldGuideAndSightingsEntryDbHelper me = null;
	public boolean fieldguideConsistent = true;
	private static boolean openedOnceAlready = false;

	// primary and secondary key
	public static final String KEY_ROWID = "_id";
	public static final String KEY_CATNAME = "cat_name";
	// fieldguide rest
	public static final String KEY_GROUPNAME = "group_name";
	public static final String KEY_COMMONNAME = "common_name";
	public static final String KEY_LATINNAME = "latin_name";
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_CS_CHECKVALUES = "cs_checkvalues"; // commaseparated
	// fieldguide show values
	public static final String KEY_SHOWGROUPNAME = "show_group_name";
	public static final String KEY_SHOWCOMMONNAME = "show_common_name";
	public static final String KEY_CS_SHOWCHECKVALUES = "cs_showcheckvalues"; // commaseparated
	// ordernr
	public static final String KEY_ORDERNR = "ordernr";

	// sightings rest
	public static final String KEY_FIELDGUIDE_ID = "fg_id"; // foreign
															// key:
															// KEY_ROWID
	public static final String KEY_DIVENR = "divenr";
	public static final String KEY_SIGHTING_VALUE = "sighting_value";
	public static final String KEY_CS_CHECKEDVALUES = "cs_checkdValues";
	public static final String KEY_REMARKS = "remarks";
	public static final String KEY_ACTIVE = "active"; // Y or N
	public static final String KEY_SHOW_SIGHTING_VALUE = "show_sightingvalue";
	public static final String KEY_SHOW_CS_CHECKEDVALUES = "show_checkedvalues";

	// FieldGuide set
	public static final int KEY_ROWID_CURSORLOC = 0;
	public static final int KEY_CATNAME_CURSORLOC = 1;
	public static final int KEY_GROUPNAME_CURSORLOC = 2;
	public static final int KEY_COMMONNAME_CURSORLOC = 3;
	public static final int KEY_LATINNAME_CURSORLOC = 4;
	public static final int KEY_DESCRIPTION_CURSORLOC = 5;
	public static final int KEY_CS_CHECKVALUES_CURSORLOC = 6;
	public static final int KEY_ORDERNR_CURSORLOC = 7;
	public static final int KEY_SHOWGROUPNAME_CURSORLOC = 8;
	public static final int KEY_SHOWCOMMONNAME_CURSORLOC = 9;
	public static final int KEY_CS_SHOWCHECKVALUES_CURSORLOC = 10;
	// added sightings set
	public static final int KEY_FIELDGUIDE_ID_CURSORLOC = 11;
	public static final int KEY_DIVENR_CURSORLOC = 12;
	public static final int KEY_SIGHTING_VALUE_CURSORLOC = 13;
	public static final int KEY_CS_CHECKEDVALUES_CURSORLOC = 14;
	public static final int KEY_REMARKS_CURSORLOC = 15;
	public static final int KEY_ACTIVE_CURSORLOC = 16;
	public static final int KEY_SHOW_SIGHTING_VALUE_CURSORLOC = 17;
	public static final int KEY_SHOW_CS_CHECKEDVALUES_CURSORLOC = 18;

	public static final Map<Integer, Integer> CODE_TO_SHOWVALUE_COLUMNMAPPING = new HashMap<Integer, Integer>();
	public static final List<String> FIELDGUIDE_COLUMNS = new ArrayList<String>();
	public static final List<String> SIGHTINGS_COLUMNS = new ArrayList<String>();

	private static final String TAG = "FldGdSghtngEDbHlpr";
	private static boolean initializing = false;

	private SDCardDatabaseHelper dbHelperExternal;
	private static SQLiteDatabase dbExternal;

	public static final String DATABASE_NAME = "FieldGuideAndSightings";
	public static final int DATABASE_VERSION = 4;
	
	private static final String FIELDGUIDE = "FieldGuide";
	private static final String SIGHTINGS = "Sightings";
	private static final String FIELDGUIDE_AS_F = FIELDGUIDE + " as F";
	private static final String SIGHTINGS_AS_S = SIGHTINGS + " as S";

	static {
		CODE_TO_SHOWVALUE_COLUMNMAPPING.put(KEY_COMMONNAME_CURSORLOC,
				KEY_SHOWCOMMONNAME_CURSORLOC);
		CODE_TO_SHOWVALUE_COLUMNMAPPING.put(KEY_GROUPNAME_CURSORLOC,
				KEY_SHOWGROUPNAME_CURSORLOC);
		CODE_TO_SHOWVALUE_COLUMNMAPPING.put(KEY_CS_CHECKVALUES_CURSORLOC,
				KEY_CS_SHOWCHECKVALUES_CURSORLOC);

		FIELDGUIDE_COLUMNS.add(KEY_ROWID);
		FIELDGUIDE_COLUMNS.add(KEY_CATNAME);
		FIELDGUIDE_COLUMNS.add(KEY_GROUPNAME);
		FIELDGUIDE_COLUMNS.add(KEY_COMMONNAME);
		FIELDGUIDE_COLUMNS.add(KEY_LATINNAME);
		FIELDGUIDE_COLUMNS.add(KEY_DESCRIPTION);
		FIELDGUIDE_COLUMNS.add(KEY_CS_CHECKVALUES);
		FIELDGUIDE_COLUMNS.add(KEY_SHOWGROUPNAME);
		FIELDGUIDE_COLUMNS.add(KEY_SHOWCOMMONNAME);
		FIELDGUIDE_COLUMNS.add(KEY_CS_SHOWCHECKVALUES);
		FIELDGUIDE_COLUMNS.add(KEY_ORDERNR);

		SIGHTINGS_COLUMNS.add(KEY_ROWID);
		SIGHTINGS_COLUMNS.add(KEY_CATNAME);
		SIGHTINGS_COLUMNS.add(KEY_GROUPNAME);
		SIGHTINGS_COLUMNS.add(KEY_COMMONNAME);
		SIGHTINGS_COLUMNS.add(KEY_ORDERNR);
		SIGHTINGS_COLUMNS.add(KEY_FIELDGUIDE_ID);
		SIGHTINGS_COLUMNS.add(KEY_DIVENR);
		SIGHTINGS_COLUMNS.add(KEY_SIGHTING_VALUE);
		SIGHTINGS_COLUMNS.add(KEY_CS_CHECKEDVALUES);
		SIGHTINGS_COLUMNS.add(KEY_REMARKS);
		SIGHTINGS_COLUMNS.add(KEY_ACTIVE);
		SIGHTINGS_COLUMNS.add(KEY_SHOW_SIGHTING_VALUE);
		SIGHTINGS_COLUMNS.add(KEY_SHOW_CS_CHECKEDVALUES);

	}

	public static final String[] ALL_FIELDGUIDE = new String[] { KEY_ROWID,
			KEY_CATNAME, KEY_GROUPNAME, KEY_COMMONNAME, KEY_LATINNAME,
			KEY_DESCRIPTION, KEY_CS_CHECKVALUES, KEY_ORDERNR,
			KEY_SHOWGROUPNAME, KEY_SHOWCOMMONNAME, KEY_CS_SHOWCHECKVALUES };
	// "F." + KEY_ROWID is used instead of "S." + KEY_FIELDGUIDE_ID to make sure
	// we get the fieldguideId also for fieldguidIds there is no entry for in
	// Sightings
	public static final String[] ALL = new String[] { "F." + KEY_ROWID,
			"F." + KEY_CATNAME, "F." + KEY_GROUPNAME, "F." + KEY_COMMONNAME,
			"F." + KEY_LATINNAME, "F." + KEY_DESCRIPTION,
			"F." + KEY_CS_CHECKVALUES, "F." + KEY_ORDERNR,
			"F." + KEY_SHOWGROUPNAME, "F." + KEY_SHOWCOMMONNAME,
			"F." + KEY_CS_SHOWCHECKVALUES, "F." + KEY_ROWID, "S." + KEY_DIVENR,
			"S." + KEY_SIGHTING_VALUE, "S." + KEY_CS_CHECKEDVALUES,
			"S." + KEY_REMARKS, "S." + KEY_ACTIVE,
			"S." + KEY_SHOW_SIGHTING_VALUE, "S." + KEY_SHOW_CS_CHECKEDVALUES };
	public static final String[] ALL_SIGHTINGS = new String[] {
			"S." + KEY_ROWID, "S." + KEY_CATNAME, "S." + KEY_GROUPNAME,
			"S." + KEY_COMMONNAME, null, null, null, "S." + KEY_ORDERNR, null,
			null, null, "S." + KEY_FIELDGUIDE_ID, "S." + KEY_DIVENR,
			"S." + KEY_SIGHTING_VALUE, "S." + KEY_CS_CHECKEDVALUES,
			"S." + KEY_REMARKS, "S." + KEY_ACTIVE,
			"S." + KEY_SHOW_SIGHTING_VALUE, "S." + KEY_SHOW_CS_CHECKEDVALUES };

	private static final String FIELDGUIDE_CREATE = "CREATE TABLE if not exists "
			+ FIELDGUIDE
			+ " ("
			+ KEY_ROWID
			+ ", "
			+ KEY_CATNAME
			+ ","
			+ KEY_GROUPNAME
			+ ","
			+ KEY_COMMONNAME
			+ ","
			+ KEY_LATINNAME
			+ ","
			+ KEY_DESCRIPTION
			+ ","
			+ KEY_CS_CHECKVALUES
			+ ","
			+ KEY_ORDERNR
			+ ","
			+ KEY_SHOWGROUPNAME
			+ ","
			+ KEY_SHOWCOMMONNAME
			+ ","
			+ KEY_CS_SHOWCHECKVALUES
			+ ", PRIMARY KEY ("
			+ KEY_CATNAME
			+ ","
			+ KEY_ROWID + "));";

	private static final String SIGHTINGS_CREATE = "CREATE TABLE if not exists "
			+ SIGHTINGS
			+ " ("
			+ KEY_ROWID
			+ " integer PRIMARY KEY autoincrement,"
			+ KEY_CATNAME
			+ ","
			+ KEY_FIELDGUIDE_ID
			+ ","
			+ KEY_GROUPNAME
			+ ","
			+ KEY_COMMONNAME
			+ ","
			+ KEY_ORDERNR
			+ ","
			+ KEY_DIVENR
			+ ","
			+ KEY_SIGHTING_VALUE
			+ ","
			+ KEY_CS_CHECKEDVALUES
			+ ","
			+ KEY_REMARKS
			+ ","
			+ KEY_ACTIVE
			+ ","
			+ KEY_SHOW_SIGHTING_VALUE
			+ ","
			+ KEY_SHOW_CS_CHECKEDVALUES
			+ ","
			+ " UNIQUE ("
			+ KEY_CATNAME
			+ ","
			+ KEY_FIELDGUIDE_ID
			+ ","
			+ KEY_DIVENR + "));";

	@SuppressWarnings("deprecation")
	public static FieldGuideAndSightingsEntryDbHelper getInstance(Context ctx) {
		if (me == null) {
			me = new FieldGuideAndSightingsEntryDbHelper(ctx);
		}
		Boolean isSDPresent = SDCardSQLiteOpenHelper
				.isSDCardiMarineLifeDirectoryActive();

		while ((dbExternal != null && (dbExternal.isDbLockedByCurrentThread() || dbExternal
				.isDbLockedByOtherThreads()))) {
		}
		if (isSDPresent && (dbExternal == null || !dbExternal.isOpen())) {
			me.open(ctx);
		}

		LibApp.getInstance().dbhelpers.put(
				"FieldGuideAndSightingsEntryDbHelper", me);
		return me;
	}

	private FieldGuideAndSightingsEntryDbHelper(Context ctx) {
	}

	public FieldGuideAndSightingsEntryDbHelper open(Context mCtx)
			throws SQLException {
		Boolean isSDPresent = SDCardSQLiteOpenHelper
				.isSDCardiMarineLifeDirectoryActive();

		Log.d(TAG,
				"Opening db - onCreate should get called if necessary - SDPresent["
						+ isSDPresent + "]");
		if (!isSDPresent) {
			Toast.makeText(
					MainActivity.me,
					LibApp.getCurrentResources().getString(
							R.string.externalMemoryNeeded), Toast.LENGTH_LONG)
					.show();
			MainActivity.me.finish(); // ??
		}
		if (isSDPresent) {
			if (me.dbExternal == null || !me.dbExternal.isOpen())
				dbHelperExternal = new SDCardDatabaseHelper(mCtx,
						SDCardSQLiteOpenHelper.getDataBasesDirectory());
			dbExternal = dbHelperExternal.getWritableDatabase();
			Log.d(TAG, "version: " + dbExternal.getVersion());

		}

		boolean dropping = false;
		if (dropping) {
			Log.d(TAG, "dropping");
			dbExternal.beginTransaction();
			dbExternal.rawQuery("drop table if exists " + DATABASE_NAME + "."
					+ FIELDGUIDE, null);
			dbExternal.rawQuery("drop table if exists " + DATABASE_NAME + "."
					+ SIGHTINGS, null);
			dbExternal.setTransactionSuccessful();
			dbExternal.endTransaction();
			dbExternal.close();
			Log.d(TAG, "dropped");
			throw new RuntimeException();
		}

		return this;
	}

	public static void logCursorEntry(Cursor cursor, String[] type) {
		if (type == ALL_FIELDGUIDE) {
			if (cursor != null && cursor.getCount() > 0
					&& !cursor.isAfterLast() && !cursor.isBeforeFirst()) {
				Log.d(TAG, "logCursorEntry fieldguide: "
						+ getFieldGuideEntryFromCursor(cursor).toString());
			}
		} else {
			if (cursor != null && cursor.getCount() > 0
					&& !cursor.isAfterLast() && !cursor.isBeforeFirst()) {
				Log.d(TAG, "logCursorEntry sighting: "
						+ getSightingFromCursor(cursor));
			}
		}
	}

	@SuppressWarnings("unused")
	public static void logAllDatabaseContent(Context ctx) {
		FieldGuideAndSightingsEntryDbHelper helper = FieldGuideAndSightingsEntryDbHelper
				.getInstance(ctx);
		Cursor cursor = helper.queryAllFieldGuide();
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				Log.d(TAG, "logAllDatabaseContent fieldguide: "
						+ getFieldGuideEntryFromCursor(cursor).toString());
				cursor.moveToNext();
			}
		}
		Cursor cursor1 = helper.querySightings(ALL, null, null, KEY_GROUPNAME);
		if (cursor1 != null && cursor1.getCount() > 0) {
			cursor1.moveToFirst();
			while (!cursor1.isAfterLast()) {
				Log.d(TAG, "logAllDatabaseContent sightings: "
						+ getStringFromCursorEntry(cursor1));
				cursor1.moveToNext();
			}
		}
		cursor.close();
		cursor1.close();
	}

	public void finalize() throws Throwable {
		close();
		super.finalize();
	}

	public void close() {
		Log.d(TAG, "closing db");
		if (!initializing) {
			if (dbHelperExternal != null) {
				dbHelperExternal.close();
				dbHelperExternal = null;
			}
			if (dbExternal != null) {
				dbExternal.close();
				dbExternal = null;
			}
		}
	}

	public Cursor queryAllFieldGuide() {
		if (dbExternal != null) {
			String orderBy = KEY_ORDERNR;

			Cursor mCursor = dbExternal.query(FIELDGUIDE, ALL_FIELDGUIDE, null,
					null, null, null, KEY_ORDERNR);

			Log.d(TAG, "queryAllFieldguide result[" + mCursor.getCount() + "]");
			return mCursor;
		}
		return null;
	}

	public Cursor queryFieldGuide(String[] columns, String selection,
			String[] selArgs, String orderBy) {
		if (dbExternal != null) {
			String restriction = KEY_CATNAME + " = '"
					+ LibApp.getCurrentCatalogName() + "'";
			if (selection == null || selection.trim().length() == 0) {
				selection = restriction;
			} else {
				selection = "(" + selection + ") and (" + restriction + ")";
			}

			if (orderBy == null) {
				orderBy = KEY_ORDERNR;
			}

			Cursor mCursor = dbExternal.query(FIELDGUIDE,
					columns == null ? ALL_FIELDGUIDE : columns, selection,
					selArgs, null, null, orderBy);

			Log.d(TAG, "queryFieldguide with restriction[" + selection
					+ "] result[" + mCursor.getCount() + "]");
			return mCursor;
		}
		return null;
	}

	public boolean deleteAllFieldGuide() {
		int doneDelete = 0;
		if (dbExternal != null) {
			String selection = KEY_CATNAME + " = '"
					+ LibApp.getCurrentCatalogName() + "'";

			dbExternal.beginTransaction();
			try {
				doneDelete += dbExternal.delete(FIELDGUIDE, selection, null);
				dbExternal.setTransactionSuccessful();
			} finally {
				dbExternal.endTransaction();
			}
		}
		Log.d(TAG, Integer.toString(doneDelete));
		return doneDelete > 0;
	}

	private long insertFieldGuide(FieldGuideEntry entry) {
		long result = 0;
		if (dbExternal != null) {
			ContentValues initialValues = new ContentValues();
			initialValues.put(KEY_ROWID, entry.id);
			initialValues.put(KEY_CATNAME, entry.catalog);
			initialValues.put(KEY_GROUPNAME, entry.getGroupName());
			initialValues.put(KEY_COMMONNAME, entry.getCommonName());
			initialValues.put(KEY_LATINNAME, entry.latinName);
			initialValues.put(KEY_DESCRIPTION, entry.getDescription());
			initialValues.put(KEY_ORDERNR, entry.ordernr);
			initialValues.put(KEY_CS_CHECKVALUES, entry.getCheckValues());
			initialValues.put(KEY_SHOWGROUPNAME, entry.getShowGroupName());
			initialValues.put(KEY_SHOWCOMMONNAME, entry.getShowCommonName());
			initialValues.put(KEY_CS_SHOWCHECKVALUES,
					entry.getShowCheckValues());

			dbExternal.beginTransaction();

			try {
				result = dbExternal.insert(FIELDGUIDE, null, initialValues);
				Log.d(TAG, "inserting into FieldGuide");

				dbExternal.setTransactionSuccessful();
			} finally {
				dbExternal.endTransaction();
			}
		}
		return result;

	}

	public long upsertFieldGuide(FieldGuideEntry entry) {
		long result = 0;
		if (dbExternal != null) {
			Cursor cursor = queryFieldGuide(null,
					KEY_ROWID + "=" + entry.id + " and " + KEY_CATNAME + " = '"
							+ LibApp.getCurrentCatalogName() + "'", null, null);

			ContentValues initialValues = new ContentValues();
			initialValues.put(KEY_ROWID, entry.id);
			initialValues.put(KEY_CATNAME, entry.catalog);
			initialValues.put(KEY_GROUPNAME, entry.getGroupName());
			initialValues.put(KEY_COMMONNAME, entry.getCommonName());
			initialValues.put(KEY_LATINNAME, entry.latinName);
			initialValues.put(KEY_DESCRIPTION, entry.getDescription());
			initialValues.put(KEY_ORDERNR, entry.ordernr);
			initialValues.put(KEY_CS_CHECKVALUES, entry.getCheckValues());
			initialValues.put(KEY_SHOWGROUPNAME, entry.getShowGroupName());
			initialValues.put(KEY_SHOWCOMMONNAME, entry.getShowCommonName());
			initialValues.put(KEY_CS_SHOWCHECKVALUES,
					entry.getShowCheckValues());

			dbExternal.beginTransaction();

			try {
				if (cursor == null || cursor.getCount() == 0) {
					result = dbExternal.insert(FIELDGUIDE, null, initialValues);
					Log.d(TAG, "inserting into FieldGuide");
				} else {
					result = dbExternal.update(FIELDGUIDE, initialValues,
							KEY_ROWID + "=" + entry.id + " and " + KEY_CATNAME
									+ " = '" + LibApp.getCurrentCatalogName()
									+ "'", null);
					Log.d(TAG, "update into FieldGuide");

				}
				dbExternal.setTransactionSuccessful();
			} finally {
				dbExternal.endTransaction();
			}

			if (cursor != null)
				cursor.close();
		}
		return result;

	}

	public static FieldGuideEntry getFieldGuideEntryFromCursor(Cursor cursor) {
		FieldGuideEntry entry = new FieldGuideEntry();
		entry.id = cursor.getInt(KEY_ROWID_CURSORLOC);
		entry.catalog = cursor.getString(KEY_CATNAME_CURSORLOC);
		entry.setGroupName(cursor.getString(KEY_GROUPNAME_CURSORLOC));
		entry.setCommonName(cursor.getString(KEY_COMMONNAME_CURSORLOC));
		entry.latinName = cursor.getString(KEY_LATINNAME_CURSORLOC);
		entry.setDescription(cursor.getString(KEY_DESCRIPTION_CURSORLOC));
		entry.ordernr = cursor.getInt(KEY_ORDERNR_CURSORLOC);
		entry.setCheckValues(cursor.getString(KEY_CS_CHECKVALUES_CURSORLOC));
		entry.setShowGroupName(cursor.getString(KEY_SHOWGROUPNAME_CURSORLOC));
		entry.setShowCommonName(cursor.getString(KEY_SHOWCOMMONNAME_CURSORLOC));
		entry.setShowCheckValues(cursor
				.getString(KEY_CS_SHOWCHECKVALUES_CURSORLOC));
		return entry;
	}

	public static Sighting getSightingFromCursor(Cursor cursor) {
		if (cursor.getCount() > 0) {

			int fieldguide_id = cursor.getInt(KEY_FIELDGUIDE_ID_CURSORLOC);
			int diveNr = cursor.getInt(KEY_DIVENR_CURSORLOC);
			if (diveNr == 0) {
				if (MainActivity.me != null
						&& MainActivity.me.currentDive != null) {
					diveNr = MainActivity.me.currentDive.getDiveNr();
				}
			}
			String catalog = cursor.getString(KEY_CATNAME_CURSORLOC);
			String group_name = cursor.getString(KEY_GROUPNAME_CURSORLOC);
			String common_name = cursor.getString(KEY_COMMONNAME_CURSORLOC);
			int orderNr = cursor.getInt(KEY_ORDERNR_CURSORLOC);
			String sightingValue = cursor
					.getString(KEY_SIGHTING_VALUE_CURSORLOC);
			if (sightingValue == null) {
				sightingValue = LibApp
						.getDiveOrPersonalOrCatalogDefaultChoice();
			}

			Catalog cat = LibApp.getInstance().getCurrentCatalog();
			String showSightingValue = cat.getResourcedValue(catalog,
					cat.getValuesMapping(), sightingValue,
					cursor.getString(KEY_SHOW_SIGHTING_VALUE_CURSORLOC));

			String checkedValues = cursor
					.getString(KEY_CS_CHECKEDVALUES_CURSORLOC);
			ArrayList<String> csCheckedValues = checkedValues != null ? Sighting
					.getCheckedValuesFromCs(checkedValues) : null;

			String showCsCheckedValues = cursor
					.getString(KEY_SHOW_CS_CHECKEDVALUES_CURSORLOC);
			ArrayList<String> showCheckedValues = showCsCheckedValues != null ? Sighting
					.getCheckedValuesFromCs(showCsCheckedValues) : null;

			String remarks = cursor.getString(KEY_REMARKS_CURSORLOC);
			FieldGuideEntry entry = getFieldGuideEntryFromCursor(cursor);

			Sighting sighting = new Sighting(fieldguide_id, diveNr, catalog,
					group_name, common_name, orderNr, sightingValue,
					csCheckedValues, showSightingValue, showCheckedValues,
					remarks, entry);
			Log.d(TAG, "getSightingFromCursor sighting[" + sighting + "]");
			return sighting;
		}
		return null;

	}

	public static String getStringFromCursorEntry(Cursor cursor) {
		StringBuilder builder = new StringBuilder();
		if (cursor.getCount() > 0) {
			for (String columnName : FIELDGUIDE_COLUMNS) {
				int index = cursor.getColumnIndex(columnName);
				if (index != -1) {
					String columnValue = null;
					try {
						columnValue = cursor.getString(index);
					} catch (Exception e) {
						columnValue = Integer.toString(cursor.getInt(index));
					}
					builder.append(columnName + "[" + columnValue + "]");
				}
			}
			for (String columnName : SIGHTINGS_COLUMNS) {
				int index = cursor.getColumnIndex(columnName);
				if (index != -1) {
					String columnValue = null;
					try {
						columnValue = cursor.getString(index);
					} catch (Exception e) {
						columnValue = Integer.toString(cursor.getInt(index));
					}
					builder.append(columnName + "[" + columnValue + "]");
				}
			}
		}
		return builder.toString();
	}

	public long upsertSightings(SerializableSparseArray<Sighting> sightings) {
		long result = 0;
		if (sightings != null) {
			if (dbExternal != null) {
				dbExternal.beginTransaction();
				try {
					for (int i = 0; i < sightings.size(); i++) {
						Sighting sighting = sightings.get(sightings.keyAt(i));
						result += upsertSighting(sighting, false);
					}
					dbExternal.setTransactionSuccessful();
				} finally {
					dbExternal.endTransaction();

				}
			}
		}

		return result;

	}

	public long upsertSighting(Sighting sighting) {
		return upsertSighting(sighting, true);
	}

	public long upsertSighting(Sighting sighting, boolean transactional) {
		long result = 0;
		if (sighting != null) {
			if (dbExternal != null) {
				if (transactional) {
					dbExternal.beginTransaction();
				}
				try {
					Log.d(TAG, "upserting [" + sighting + "]");
					// called within a transaction already
					ContentValues initialValues = new ContentValues();
					initialValues.put(KEY_CATNAME, sighting.catalog);
					initialValues
							.put(KEY_FIELDGUIDE_ID, sighting.fieldguide_id);
					initialValues.put(KEY_ORDERNR, sighting.orderNr);
					initialValues.put(KEY_DIVENR, sighting.diveNr);
					initialValues.put(KEY_SIGHTING_VALUE,
							sighting.data.sightingValue);
					initialValues.put(KEY_CS_CHECKEDVALUES,
							sighting.getCsCheckedValues());
					initialValues.put(KEY_SHOW_SIGHTING_VALUE,
							sighting.data.showSightingValue);
					initialValues.put(KEY_SHOW_CS_CHECKEDVALUES,
							sighting.getCsShowCheckedValues());
					initialValues.put(KEY_REMARKS, sighting.data.remarks);

					String[] columns = { KEY_ROWID };
					String selection = KEY_CATNAME + "='" + sighting.catalog
							+ "' and " + KEY_DIVENR + "=" + sighting.diveNr
							+ " and " + KEY_FIELDGUIDE_ID + "="
							+ sighting.fieldguide_id;
					Cursor cursor = querySightings(columns, selection, null,
							null);
					Log.d(TAG, "" + cursor.getCount());

					if (cursor != null && cursor.getCount() != 0) {
						result = dbExternal.update(SIGHTINGS, initialValues,
								selection, null);
					} else {
						dbExternal.insert(SIGHTINGS, null, initialValues);
						result = 1;
					}
					if (transactional) {
						dbExternal.setTransactionSuccessful();
					}
					cursor.close();
				} finally {
					if (transactional) {
						dbExternal.endTransaction();
					}
				}
			}
		}
		Log.d(TAG, "result: " + result);
		return result;

	}

	public boolean deleteAllSightings() {
		int doneDelete = 0;
		if (dbExternal != null) {

			String selection = KEY_CATNAME + " = '"
					+ LibApp.getCurrentCatalogName() + "'";

			dbExternal.beginTransaction();
			try {
				doneDelete += dbExternal.delete(SIGHTINGS, selection, null);
				dbExternal.setTransactionSuccessful();
			} finally {
				dbExternal.endTransaction();
			}
		}
		Log.d(TAG, Integer.toString(doneDelete));
		return doneDelete > 0;
	}

	public boolean cleanUpDiveDataInWrongCatalog(int divenr,
			String preserveCatalogName) {
		int doneDelete = 0;
		if (dbExternal != null) {
			dbExternal.beginTransaction();
			try {
				doneDelete += dbExternal.delete(SIGHTINGS, KEY_DIVENR + " = "
						+ divenr + " and " + KEY_CATNAME + " != '"
						+ preserveCatalogName + "'", null);
				dbExternal.setTransactionSuccessful();
			} finally {
				dbExternal.endTransaction();
			}
		}
		Log.d(TAG,
				"cleanUpDiveDataInWrongCatalog deleted["
						+ Integer.toString(doneDelete) + "]");
		return doneDelete > 0;
	}

	public boolean deleteSightingsForDive(int divenr) {
		int doneDelete = 0;
		if (dbExternal != null) {
			dbExternal.beginTransaction();
			try {
				doneDelete += dbExternal.delete(SIGHTINGS, KEY_DIVENR + " = "
						+ divenr, null);
				dbExternal.setTransactionSuccessful();
			} finally {
				dbExternal.endTransaction();
			}
		}
		Log.d(TAG,
				"deleteSightingsForDive [" + divenr + "]["
						+ Integer.toString(doneDelete) + "]");
		return doneDelete > 0;
	}

	public int deleteSightingsForGroupInDive(int divenr, String groupName) {
		// first get fieldguide entries for group, to get groupIds
		String selection = KEY_GROUPNAME + " = '" + groupName + "'";
		Cursor cursor = queryFieldGuide(null, selection, null, null);
		SerializableSparseArray<FieldGuideEntry> entries = getFieldeGuideMapFrom(cursor);
		cursor.close();
		StringBuilder in = new StringBuilder();
		for (int i = 0; i < entries.size(); i++) {
			int fieldguide_id = entries.keyAt(i);
			if (in.length() > 0)
				in.append(",");
			in.append(fieldguide_id);
		}
		Log.d(TAG,
				"deleteSightingsForGroupInDive fieldguideIds[" + in.toString()
						+ "]");

		selection = KEY_DIVENR + " = " + divenr + " and " + KEY_FIELDGUIDE_ID
				+ " in (" + in.toString() + ")";

		int doneDelete = 0;
		if (dbExternal != null) {
			dbExternal.beginTransaction();
			try {
				doneDelete += dbExternal.delete(SIGHTINGS, selection, null);
				Log.d(TAG, "deleted [" + doneDelete + "] for[" + selection
						+ "]");
				dbExternal.setTransactionSuccessful();
			} finally {
				dbExternal.endTransaction();
			}
		}
		Log.d(TAG, Integer.toString(doneDelete));

		return doneDelete;
	}

	
	public int updateSightingsWithNewDiveNr(int diveNrAtStart, int diveNr) {
		Log.d(TAG, "updateSightingsWithNewDiveNr[" + diveNrAtStart + "][" + diveNr + "]");
		int doneUpdate = 0;
		if (dbExternal != null) {

			ContentValues initialValues = new ContentValues();
			initialValues.put(KEY_DIVENR, diveNr);

			dbExternal.beginTransaction();
			try {
				doneUpdate += dbExternal
						.update(SIGHTINGS, initialValues, KEY_DIVENR + " = "
								+ diveNrAtStart + " and " + KEY_CATNAME + " = '"
								+ LibApp.getCurrentCatalogName() + "'", null);

				dbExternal.setTransactionSuccessful();
			} finally {
				dbExternal.endTransaction();
			}
		}
		Log.d(TAG,
				"updateSightingsForGroupInDive updated["
						+ Integer.toString(doneUpdate) + "]");
		return doneUpdate;
	}
	
	public int updateSightingsForGroupInDive(int divenr, String groupName,
			String value) {
		int doneUpdate = 0;
		if (dbExternal != null) {

			ContentValues initialValues = new ContentValues();
			initialValues.put(KEY_SIGHTING_VALUE, value);

			dbExternal.beginTransaction();
			try {
				doneUpdate += dbExternal
						.update(SIGHTINGS, initialValues, KEY_DIVENR + " = "
								+ divenr + " and " + KEY_GROUPNAME + " = '"
								+ groupName + "' and " + KEY_CATNAME + " = '"
								+ LibApp.getCurrentCatalogName() + "'", null);

				dbExternal.setTransactionSuccessful();
			} finally {
				dbExternal.endTransaction();
			}
		}
		Log.d(TAG,
				"updateSightingsForGroupInDive updated["
						+ Integer.toString(doneUpdate) + "]");
		return doneUpdate;
	}

	public long insertSightingsForGroupInDive(int diveNr, String groupName,
			String sightingValue) {
		Cursor cursor = querySightingAnyWayYouCanForGroup(diveNr, groupName);
		SerializableSparseArray<Sighting> sightings = getSightingsMapFrom(cursor);
		cursor.close();

		SerializableSparseArray<Sighting> sightingsToCreate = new SerializableSparseArray<Sighting>();

		if (sightings != null && sightings.size() > 0) {
			for (int i = 0; i < sightings.size(); i++) {
				Sighting sighting = sightings.get(sightings.keyAt(i));
				if (sighting != null) {
					Log.d(TAG, "insertSightingsForGroupInDive [" + sighting
							+ "] new diveNr[" + diveNr + "]");
					sighting.diveNr = diveNr;
					if (sighting.data == null
							|| !sightingValue
									.equals(sighting.data.sightingValue)) {
						if (sighting.data == null) {
							sighting.data = new Sighting.SightingData(
									sightingValue, null, null);
						} else {
							sighting.data.setSightingValue(sightingValue);

						}

						sightingsToCreate.append(i, sighting);
					}
					SightingsFieldGuideSimpleCursorAdapter.setChangedSighting(
							sighting.fieldguide_id, sighting.data);
				}
			}
		}
		return upsertSightings(sightingsToCreate);
	}

	public String getGroupsFromSightingsAsIsForDive(int diveNr, String catalog) {
		String toReturn = "";
		Cursor cursor = querySightingAsIsForDive(diveNr, catalog);
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			do {
				toReturn = toReturn + "$"
						+ cursor.getString(KEY_GROUPNAME_CURSORLOC) + "$";
			} while (cursor.moveToNext());
		}
		return toReturn;
	}

	public SerializableSparseArray<Sighting> getSightingsMapFrom(Cursor mCursor) {
		Log.d(TAG, "getSightingsMapFromCursor count["
				+ (mCursor != null ? mCursor.getCount() : "null") + "]");
		SerializableSparseArray<Sighting> sightings = new SerializableSparseArray<Sighting>();

		if (mCursor != null && mCursor.getCount() > 0) {
			mCursor.moveToFirst();
			do {
				String checkedValuesString = mCursor
						.getString(KEY_CS_CHECKEDVALUES_CURSORLOC);
				ArrayList<String> checkedValues = null;
				if (checkedValuesString != null) {
					checkedValues = new ArrayList<String>();
					checkedValues.addAll(Arrays.asList(checkedValuesString
							.split(",")));
				}

				FieldGuideEntry fieldGuideEntry = new FieldGuideEntry(
						mCursor.getInt(KEY_ROWID_CURSORLOC),
						mCursor.getString(KEY_CATNAME_CURSORLOC),
						mCursor.getString(KEY_LATINNAME_CURSORLOC),
						mCursor.getString(KEY_GROUPNAME_CURSORLOC),
						mCursor.getString(KEY_COMMONNAME_CURSORLOC),
						mCursor.getString(KEY_DESCRIPTION_CURSORLOC),
						mCursor.getString(KEY_CS_CHECKVALUES_CURSORLOC),
						mCursor.getInt(KEY_ORDERNR_CURSORLOC),
						mCursor.getString(KEY_SHOWGROUPNAME_CURSORLOC),
						mCursor.getString(KEY_SHOWCOMMONNAME_CURSORLOC),
						mCursor.getString(KEY_CS_SHOWCHECKVALUES_CURSORLOC));

				String sightingValue = mCursor
						.getString(KEY_SIGHTING_VALUE_CURSORLOC);
				if (sightingValue == null) {
					sightingValue = LibApp
							.getDiveOrPersonalOrCatalogDefaultChoice();
				}
				Catalog cat = LibApp.getInstance().getCurrentCatalog();
				String showSightingValue = cat.getResourcedValue(
						mCursor.getString(KEY_CATNAME_CURSORLOC),
						cat.getValuesMapping(), sightingValue,
						mCursor.getString(KEY_SHOW_SIGHTING_VALUE_CURSORLOC));

				String showCsCheckedValues = mCursor
						.getString(KEY_SHOW_CS_CHECKEDVALUES_CURSORLOC);
				ArrayList<String> showCheckedValues = showCsCheckedValues != null ? Sighting
						.getCheckedValuesFromCs(showCsCheckedValues) : null;

				Sighting sighting = new Sighting(
						mCursor.getInt(KEY_FIELDGUIDE_ID_CURSORLOC),
						mCursor.getInt(KEY_DIVENR_CURSORLOC),
						mCursor.getString(KEY_CATNAME_CURSORLOC),
						mCursor.getString(KEY_GROUPNAME_CURSORLOC),
						mCursor.getString(KEY_COMMONNAME_CURSORLOC),
						mCursor.getInt(KEY_ORDERNR_CURSORLOC),
						mCursor.getString(KEY_SIGHTING_VALUE_CURSORLOC),
						checkedValues, showSightingValue, showCheckedValues,
						mCursor.getString(KEY_REMARKS_CURSORLOC),
						fieldGuideEntry);
				sightings.put(Integer.valueOf(mCursor
						.getInt(KEY_FIELDGUIDE_ID_CURSORLOC)), sighting);
			} while (mCursor.moveToNext());
		}
		Log.d(TAG, "getSightingsMapFromCursor returning[" + sightings + "]");

		return sightings;
	}

	public SerializableSparseArray<FieldGuideEntry> getFieldeGuideMapFrom(
			Cursor mCursor) {
		Log.d(TAG,
				"getFieldeGuideMapFrom count["
						+ (mCursor != null ? mCursor.getCount() : "null") + "]");
		SerializableSparseArray<FieldGuideEntry> fieldguideEntries = new SerializableSparseArray<FieldGuideEntry>();

		if (mCursor != null && mCursor.getCount() > 0) {
			mCursor.moveToFirst();
			do {

				FieldGuideEntry fieldGuideEntry = new FieldGuideEntry(
						mCursor.getInt(KEY_ROWID_CURSORLOC),
						mCursor.getString(KEY_CATNAME_CURSORLOC),
						mCursor.getString(KEY_LATINNAME_CURSORLOC),
						mCursor.getString(KEY_GROUPNAME_CURSORLOC),
						mCursor.getString(KEY_COMMONNAME_CURSORLOC),
						mCursor.getString(KEY_DESCRIPTION_CURSORLOC),
						mCursor.getString(KEY_CS_CHECKVALUES_CURSORLOC),
						mCursor.getInt(KEY_ORDERNR_CURSORLOC),
						mCursor.getString(KEY_SHOWGROUPNAME_CURSORLOC),
						mCursor.getString(KEY_SHOWCOMMONNAME_CURSORLOC),
						mCursor.getString(KEY_CS_SHOWCHECKVALUES_CURSORLOC));
				fieldguideEntries.append(mCursor.getInt(KEY_ROWID_CURSORLOC),
						fieldGuideEntry);

			} while (mCursor.moveToNext());
		}
		Log.d(TAG, "getFieldeGuideMapFrom partcount[" + fieldguideEntries + "]");

		return fieldguideEntries;
	}

	public Cursor querySightings(String[] columns, String selection,
			String[] selArgs, String orderBy) {
		Cursor mCursor = null;
		if (columns == null)
			columns = ALL;
		String columnsAsString = getCommaSeparatedString(columns);
		if (columns != ALL && columns != ALL_SIGHTINGS) {
			columnsAsString = addTablePrefixes(columnsAsString, "S");
		}
		selection = addTablePrefixes(selection, "S");
		orderBy = addTablePrefixes(orderBy, "S");

		if (columnsAsString.contains("F.")) {
			String sql = "select " + columnsAsString + " from "
					+ SIGHTINGS_AS_S + "," + FIELDGUIDE_AS_F + " where S."
					+ KEY_FIELDGUIDE_ID + "=F." + KEY_ROWID
					+ (selection != null ? (" and (" + selection + ")") : "")
					+ (orderBy != null ? (" order by " + orderBy) : "");
			Log.d(TAG, sql);
			mCursor = dbExternal.rawQuery(sql, null);
		} else {
			mCursor = dbExternal.query(SIGHTINGS_AS_S, columns, selection,
					selArgs, null, null, orderBy);
		}
		return mCursor;
	}

	private static String addTablePrefixes(String input, String defaultPrefix) {
		if (input == null) {
			return null;
		}
		if (defaultPrefix == null) {
			defaultPrefix = "S";
		}

		if (defaultPrefix.equals("S")) {
			input = handleTableColumns(input, SIGHTINGS_COLUMNS, "S.");
			input = handleTableColumns(input, FIELDGUIDE_COLUMNS, "F.");
		} else {
			input = handleTableColumns(input, FIELDGUIDE_COLUMNS, "F.");
			input = handleTableColumns(input, SIGHTINGS_COLUMNS, "S.");
		}
		return input;
	}

	private static String handleTableColumns(String input,
			List<String> columnNames, String prefix) {
		List<String> divider = new ArrayList<String>();
		divider.add(" ");
		divider.add(",");
		divider.add("=");
		divider.add("<");
		divider.add(">");
		divider.add("(");
		divider.add(")");
		divider.add(".");
		for (String column : columnNames) {
			int index = 0;
			do {
				index = input.indexOf(column, index);
				if (index != -1
						&& (index == 0 || divider.contains(input.substring(
								index - 1, index)))) {
					String before = input.substring(0, index);
					String after = input.substring(index + column.length());
					if (index != 0
							&& (input.substring(index - 1, index)).equals(".")) {
						input = before + column + after;
						index = index + column.length();
					} else {
						input = before + prefix + column + after;
						index = (before + prefix + column).length();
					}
				} else {
					if (index != -1) {
						index = index + column.length();
					}
				}

			} while (index != -1);
		}
		return input;
	}

	public Cursor querySightingAsIsForDive(int diveNr, String catalog) {
		if (catalog == null) {
			DiveDbHelper helper = DiveDbHelper.getInstance(MainActivity.me);
			Cursor cursor = helper.fetchCursorForDive(diveNr);
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				catalog = cursor.getString(DiveDbHelper.KEY_CATNAME_CURSORLOC);
			} else {
				catalog = LibApp.getCurrentCatalogName();
			}
			cursor.close();
		}
		String sql = "select " + getCommaSeparatedString(ALL) + " from "
				+ SIGHTINGS_AS_S + "," + FIELDGUIDE_AS_F + " where S."
				+ KEY_FIELDGUIDE_ID + "=F." + KEY_ROWID + " and S."
				+ KEY_CATNAME + "=F." + KEY_CATNAME + " and S." + KEY_DIVENR
				+ "=" + diveNr + " and F." + KEY_CATNAME + "='" + catalog + "'"
				+ " order by S." + KEY_ORDERNR;
		Log.d(TAG, sql);
		if (dbExternal == null) {
			Log.d(TAG,
					"db was closed. this was possible called in background and should now be ignored");
			return null;
		}
		Cursor mCursor = dbExternal.rawQuery(sql, null);
		mCursor.moveToFirst();
		return mCursor;
	}

	public Cursor querySightingAsIsForSighting(int rownr) {
		String sql = "select " + getCommaSeparatedString(ALL) + " from "
				+ SIGHTINGS_AS_S + "," + FIELDGUIDE_AS_F + " where S."
				+ KEY_FIELDGUIDE_ID + "=F." + KEY_ROWID + " and S." + KEY_ROWID
				+ "=" + rownr;
		Cursor mCursor = dbExternal.rawQuery(sql, null);
		return mCursor;
	}

	public Cursor querySightingAnyWayYouCan(int diveNr, int fieldguideId) {
		String catalog = LibApp.getCurrentCatalogName();
		String sql = "select " + getCommaSeparatedString(ALL) + " from "
				+ FIELDGUIDE_AS_F + " LEFT JOIN " + SIGHTINGS_AS_S + " on S."
				+ KEY_FIELDGUIDE_ID + "=F." + KEY_ROWID + " and S."
				+ KEY_CATNAME + "=F." + KEY_CATNAME + " and S." + KEY_DIVENR
				+ " = " + diveNr + " where F." + KEY_ROWID + "=" + fieldguideId
				+ " and F." + KEY_CATNAME + " = '" + catalog + "'"
				+ " order by F." + KEY_ORDERNR;
		Log.d(TAG, sql);
		Cursor cursor1 = dbExternal.rawQuery(sql, null);
		Log.d(TAG, (cursor1 != null ? "" + cursor1.getCount() : "null"));
		Cursor cursor2 = queryInActiveSightingsForDive(diveNr);
		Log.d(TAG, (cursor2 != null ? "" + cursor2.getCount() : "null"));
		Cursor[] cursors = { cursor1, cursor2 };
		MergeCursor mCursor = new MergeCursor(cursors);
		Log.d(TAG, (mCursor != null ? "" + mCursor.getCount() : "null"));
		return mCursor;
	}

	public Cursor querySightingAnyWayYouCanForGroup(int diveNr, String groupName) {
		String catalog = LibApp.getCurrentCatalogName();
		String sql = "select " + getCommaSeparatedString(ALL) + " from "
				+ FIELDGUIDE_AS_F + " LEFT JOIN " + SIGHTINGS_AS_S + " on S."
				+ KEY_FIELDGUIDE_ID + "=F." + KEY_ROWID + " and S."
				+ KEY_CATNAME + "=F." + KEY_CATNAME + " and S." + KEY_DIVENR
				+ " = " + diveNr + " where F." + KEY_GROUPNAME + "= '"
				+ groupName + "' " + " and F." + KEY_CATNAME + " = '" + catalog
				+ "' " + "order by F." + KEY_ORDERNR;
		Log.d(TAG, sql);
		Cursor cursor1 = dbExternal.rawQuery(sql, null);
		Log.d(TAG, (cursor1 != null ? "" + cursor1.getCount() : "null"));
		logCursor(cursor1);
		Cursor cursor2 = queryInActiveSightingsForDive(diveNr);
		Log.d(TAG, (cursor2 != null ? "" + cursor2.getCount() : "null"));
		Cursor[] cursors = { cursor1, cursor2 };
		MergeCursor mCursor = new MergeCursor(cursors);
		Log.d(TAG, (mCursor != null ? "" + mCursor.getCount() : "null"));
		return mCursor;
	}

	private void logCursor(Cursor cursor) {
		if (cursor != null && cursor.getCount() > 0) {
			int position = cursor.getPosition();
			cursor.moveToFirst();
			do {
				StringBuilder builder = new StringBuilder();
				int count = cursor.getColumnCount();
				for (int i = 0; i < count; i++) {
					builder.append("[");
					builder.append(cursor.getColumnName(i)).append(", ");
					builder.append(cursor.getString(i));
					builder.append("]");
				}
				Log.d(TAG, "logCursor [" + builder.toString() + "]");
			} while (cursor.moveToNext());
			cursor.moveToPosition(position);
		} else {
			Log.d(TAG, "logCursor: cursor is null or empty");
		}

	}

	public boolean deleteSighting(int diveNr, int fieldguide_id) {
		int doneDelete = 0;
		if (dbExternal != null) {
			dbExternal.beginTransaction();
			try {
				doneDelete = dbExternal.delete(SIGHTINGS, KEY_DIVENR + " = "
						+ diveNr + " and " + KEY_FIELDGUIDE_ID + " = "
						+ fieldguide_id, null);
				dbExternal.setTransactionSuccessful();
			} finally {
				dbExternal.endTransaction();
			}
		}
		Log.d(TAG, "deleteSighting diveNr[" + diveNr + "] fieldguideId["
				+ fieldguide_id + "] deleted[" + Integer.toString(doneDelete)
				+ "]");
		return doneDelete > 0;
	}

	private Cursor queryDistinct(String tableName, String[] columns,
			String selection, String groupBy) {

		Cursor mCursor = dbExternal.query(true, tableName,
				columns == null ? ALL : columns, selection, null, groupBy,
				null, null, null);

		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public int updateValues(String tableName, List<SQLUpdateObject> updateList) {
		int updated = 0;
		if (updateList != null) {
			dbExternal.beginTransaction();
			try {
				for (SQLUpdateObject update : updateList) {
					ContentValues initialValues = update.getUpdateValues();
					String selection = update.getSelection();
					if (initialValues != null && initialValues.size() > 0) {
						updated += dbExternal.update(tableName, initialValues,
								selection, null);
					}
				}
				dbExternal.setTransactionSuccessful();
			} catch (Exception e) {
				Log.e(TAG, "error while updating dives", e);
				updated = -1;
			} finally {
				dbExternal.endTransaction();
			}

		}
		return updated;
	}

	public Cursor queryFieldGuideFilledForDive(int diveNr) {
		String catalog = LibApp.getCurrentCatalogName();
		String sql = "select " + getCommaSeparatedString(ALL) + " from "
				+ FIELDGUIDE_AS_F + " LEFT JOIN " + SIGHTINGS_AS_S + " on S."
				+ KEY_FIELDGUIDE_ID + "=F." + KEY_ROWID + " and S."
				+ KEY_CATNAME + "=F." + KEY_CATNAME + " and S." + KEY_DIVENR
				+ "=" + diveNr + " where F." + KEY_CATNAME + "= '" + catalog
				+ "'" + " order by F." + KEY_ORDERNR;
		Log.d(TAG, sql);
		Cursor cursor1 = dbExternal.rawQuery(sql, null);
		Log.d(TAG, (cursor1 != null ? "" + cursor1.getCount() : "null"));
		Cursor cursor2 = queryInActiveSightingsForDive(diveNr);
		Log.d(TAG, (cursor2 != null ? "" + cursor2.getCount() : "null"));
		Cursor[] cursors = { cursor1, cursor2 };
		MergeCursor mCursor = new MergeCursor(cursors);
		Log.d(TAG, (mCursor != null ? "" + mCursor.getCount() : "null"));
		return mCursor;
	}

	public Cursor queryInActiveSightingsForDive(int diveNr) {
		String sql = "select " + getCommaSeparatedString(ALL_SIGHTINGS)
				+ " from " + SIGHTINGS_AS_S + " where S." + KEY_DIVENR + "="
				+ diveNr + " and S." + KEY_CATNAME + "= '"
				+ LibApp.getCurrentCatalogName() + "' and S." + KEY_ACTIVE
				+ "='N' order by S." + KEY_ORDERNR;
		Cursor mCursor = dbExternal.rawQuery(sql, null);
		return mCursor;
	}

	private String getCommaSeparatedString(String[] array) {
		if (array != null) {
			StringBuilder builder = new StringBuilder();
			boolean first = true;
			for (String x : array) {
				if (!first) {
					builder.append(",");
				}
				builder.append(x);
				first = false;
			}
			return builder.toString();
		} else {
			return null;
		}
	}

	public boolean shouldShowStatus() {

		if (initializing)
			return true;
		else
			return shouldInitialize();
	}

	public boolean shouldInitialize() {
		if (initializing)
			return false;
		Cursor cursor = me.queryFieldGuide(ALL_FIELDGUIDE, "1=1", null, null);
		try {
			if (cursor == null || cursor.getCount() == 0)
				return true;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return false;
	}

	public void initialize(Resources resources) {
		AsyncTask<Resources, Integer, Integer> task = new AsyncTask<Resources, Integer, Integer>() {
			@Override
			protected Integer doInBackground(Resources... params) {
				Log.d(TAG, "initialize FieldGuide");
				if (!initializing) {
					initializing = true;
					Catalog catalog = LibApp.getInstance().getCurrentCatalog();

					Resources resources = params[0];
					int done = 0;
					deleteAllFieldGuide();
					boolean first = true;
					done = 0;
					int id = 0;
					for (int i = 0; i < LibApp.getInstance()
							.getCurrentCatalog().getLatinIds().length; i++) {
						fieldguideConsistent = false;
						id = catalog.getIds()[i];
						FieldGuideEntry entry = new FieldGuideEntry(id,
								LibApp.getCurrentCatalogName(),
								resources.getString(LibApp.getInstance()
										.getCurrentCatalog().getLatinIds()[i]),
								catalog.getCommonToGroupMapping().get(
										"common" + id), "common" + id, "descr"
										+ id,
								LibApp.getInstance().getCurrentCatalog()
										.getCheckValues()[i], i);
						insertFieldGuide(entry);
						done++;
						if (first && (done % 5 == 0 || done < 10)) {
							publishProgress(done, LibApp.getInstance()
									.getCurrentCatalog().getLatinIds().length);
						}
					}
					first = false;

					return done;
				}
				return 0;

			}

			protected void onPostExecute(Integer result) {
				initializing = false;
				fieldguideConsistent = true;
				Toast.makeText(
						MainActivity.me,
						LibApp.getCurrentResources().getString(
								R.string.fieldguide_uptotdate),
						Toast.LENGTH_SHORT).show();
				FieldGuideListFragment fragment = (FieldGuideListFragment) MainActivity.me
						.getFragment("FieldGuideListFragment");
				if (fragment != null) {
					fragment.removeHeader();
				}
				DivingLogSightingsListFragment dlfragment = (DivingLogSightingsListFragment) MainActivity.me
						.getFragment("DivingLogSightingsListFragment");
				if (dlfragment != null) {
					dlfragment.removeHeader();
				}
			}

			@Override
			protected void onProgressUpdate(Integer... values) {
				// runs on UI thread on publishProgress
				Log.d(TAG, values[0] + " " + values[1]);
				int perc = values[0] * 100 / values[1];

				FieldGuideListFragment fragment = (FieldGuideListFragment) MainActivity.me
						.getFragment("FieldGuideListFragment");
				if (fragment != null) {
					/*
					 * Cursor cursor = queryFieldGuide(null, null, null, null);
					 */
					fragment.refreshProgress(perc, fragment.getActualCursor());
				}
				DivingLogSightingsListFragment dlfragment = (DivingLogSightingsListFragment) MainActivity.me
						.getFragment("DivingLogSightingsListFragment");
				if (dlfragment != null) {
					dlfragment.refreshProgress(perc,
							dlfragment.getActualCursor());
				}
				super.onProgressUpdate(values);
			}

		};

		if (!initializing) {
			task.execute(resources);
		}
	}

	public void fillFieldsForVersion004() {
		resetLocaleDependentContent();
	}

	public void resetLocaleDependentContent() {
		resetLocaleDependentContent(false);
	}
	
	public void resetLocaleDependentContent(boolean fieldGuideOnly) {
		AsyncTask<Boolean, Integer, Integer> task = new AsyncTask<Boolean, Integer, Integer>() {
			@Override
			protected Integer doInBackground(Boolean... booleans) {
				boolean fieldGuideOnly = booleans[0];
				Log.d(TAG, "resetLocaleDependentContent fieldGuideOnly["+fieldGuideOnly+"]");
				if (!initializing) {
					initializing = true;
					Catalog catalog = LibApp.getInstance().getCurrentCatalog();

					// FieldGuide
					Set<String> groupIds = catalog.getGroupIdMapping().keySet();
					Set<String> commonIds = catalog.getCommonIdMapping()
							.keySet();
					Set<String> checkValueIds = new HashSet<String>(
							Arrays.asList(catalog.getCheckValues()));

					// Sightings
					Set<String> sightingsSet = catalog.getValuesMapping()
							.keySet();
					Set<String> checkedValuesSet = getCheckedValuesSet();

					// times 2 is to keep track of the getting the data (1) and
					// writing the date (2)
					int todo = (
							  groupIds.size() 
							+ commonIds.size()
							+ checkValueIds.size() 
							+ (fieldGuideOnly ? 0 : 
								(sightingsSet.size() + checkedValuesSet.size())
							   )
							) * 2;

					int fieldGuideDone = resetLocaleDependentContentInFieldGuide(
							groupIds, commonIds, checkValueIds, todo);
					int sightingsDone = 0;
					if(!fieldGuideOnly){
						sightingsDone = fieldGuideDone == -1 ? -1
							: resetLocaleDependentContentInSightings(
									sightingsSet, checkedValuesSet,
									fieldGuideDone, todo);
					}
					
					return (fieldGuideDone == -1 || sightingsDone == -1) ? -1
							: fieldGuideDone + sightingsDone;
				}
				return 0;

			}

			private Set<String> getCheckedValuesSet() {
				String[] columns = { KEY_CS_CHECKEDVALUES };
				String selection = KEY_CATNAME + " = '"
						+ LibApp.getCurrentCatalogName() + "'";
				Cursor checkedValuesCursor = queryDistinct(SIGHTINGS, columns,
						selection, KEY_CS_CHECKEDVALUES);
				Set<String> checkedValuesSet = new HashSet<String>();

				if (checkedValuesCursor != null
						&& checkedValuesCursor.getCount() > 0) {
					do {
						String checkedValues = checkedValuesCursor.getString(0);
						if (checkedValues != null && checkedValues.length() > 0) {
							checkedValuesSet.add(checkedValues);
						}
					} while (checkedValuesCursor.moveToNext());
					checkedValuesCursor.close();
				}

				return checkedValuesSet;
			}

			private int resetLocaleDependentContentInSightings(
					Set<String> sightingValues, Set<String> checkedValuesSet,
					int done, int todo) {
				Catalog catalog = LibApp.getInstance().getCurrentCatalog();

				List<SQLUpdateObject> list = new ArrayList<SQLUpdateObject>();
				int doneHere = 0;

				for (String sightingValue : sightingValues) {
					String showSightingValue = Sighting
							.getResourcedSightingValue(catalog.getName(),
									sightingValue);
					ContentValues values = new ContentValues();
					values.put(KEY_SHOW_SIGHTING_VALUE, showSightingValue);
					String where = KEY_CATNAME + " = '"
							+ LibApp.getCurrentCatalogName() + "' AND "
							+ KEY_SIGHTING_VALUE + " = '" + sightingValue + "'";
					SQLUpdateObject update = new SQLUpdateObject(values, where);
					list.add(update);
					done++;
					doneHere++;

					if ((done % 5 == 0 || done < 10)) {
						publishProgress(done, todo);
					}

				}
				for (String checkedValues : checkedValuesSet) {
					List<String> showCheckedValuesList = Sighting
							.getResourcedCheckedValues(
									catalog.getName(),
									Sighting.getCheckedValuesFromCs(checkedValues));
					String showCheckedValues = Sighting
							.getCsStringFromList(showCheckedValuesList);

					ContentValues values = new ContentValues();
					values.put(KEY_SHOW_CS_CHECKEDVALUES, showCheckedValues);
					String where = KEY_CATNAME + " = '"
							+ LibApp.getCurrentCatalogName() + "' AND "
							+ KEY_CS_CHECKEDVALUES + " = '" + checkedValues
							+ "'";
					SQLUpdateObject update = new SQLUpdateObject(values, where);
					list.add(update);
					done++;
					doneHere++;
					if ((done % 5 == 0 || done < 10)) {
						publishProgress(done, todo);
					}

				}

				int updated = updateValues(SIGHTINGS, list);
				// doneHere added because the writing of the data (doneHere)
				// should be counted twice in the progressbar
				if ((done % 5 == 0 || done < 10)) {
					publishProgress(done + doneHere, todo);
				}
				return updated == -1 ? -1 : done + doneHere;

			}

			private int resetLocaleDependentContentInFieldGuide(
					Set<String> groupIds, Set<String> commonIds,
					Set<String> checkValueIds, int todo) {
				Catalog catalog = LibApp.getInstance().getCurrentCatalog();

				int done = 0;
				int doneHere = 0;

				List<SQLUpdateObject> list = new ArrayList<SQLUpdateObject>();

				for (String group : groupIds) {
					String showGroupName = FieldGuideEntry
							.getResourcedGroupName(catalog.getName(), group);
					ContentValues values = new ContentValues();
					values.put(KEY_SHOWGROUPNAME, showGroupName);
					String selection = KEY_CATNAME + " = '"
							+ LibApp.getCurrentCatalogName() + "' AND "
							+ KEY_GROUPNAME + " = '" + group + "'";
					SQLUpdateObject update = new SQLUpdateObject(values,
							selection);
					list.add(update);
					done++;
					doneHere++;
					if ((done % 5 == 0 || done < 10)) {
						publishProgress(done, todo);
					}
				}

				for (String commonName : commonIds) {
					String showCommonName = FieldGuideEntry
							.getResourcedCommonName(catalog.getName(),
									commonName);
					ContentValues values = new ContentValues();
					values.put(KEY_SHOWCOMMONNAME, showCommonName);
					String selection = KEY_CATNAME + " = '"
							+ LibApp.getCurrentCatalogName() + "' AND "
							+ KEY_COMMONNAME + " = '" + commonName + "'";
					SQLUpdateObject update = new SQLUpdateObject(values,
							selection);
					list.add(update);
					done++;
					doneHere++;
					if ((done % 5 == 0 || done < 10)) {
						publishProgress(done, todo);
					}
				}
				for (String checkValues : checkValueIds) {
					String showCheckValues = FieldGuideEntry
							.getResourcedCheckValuesForCurrentCatalog(
									catalog.getName(), checkValues);
					ContentValues values = new ContentValues();
					values.put(KEY_CS_SHOWCHECKVALUES, showCheckValues);
					String selection = KEY_CATNAME + " = '"
							+ LibApp.getCurrentCatalogName() + "' AND "
							+ KEY_CS_CHECKVALUES + " = '" + checkValues + "'";
					SQLUpdateObject update = new SQLUpdateObject(values,
							selection);
					list.add(update);
					done++;
					doneHere++;
					if ((done % 5 == 0 || done < 10)) {
						publishProgress(done, todo);
					}
				}

				int updated = updateValues(FIELDGUIDE, list);
				if ((done % 5 == 0 || done < 10)) {
					publishProgress(done + doneHere, todo);
				}
				return updated == -1 ? -1 : done + doneHere;
			}

			protected void onPostExecute(Integer result) {
				initializing = false;
				fieldguideConsistent = true;
				if (MainActivity.me != null) {
					FieldGuideListFragment fragment = (FieldGuideListFragment) MainActivity.me
							.getFragment("FieldGuideListFragment");
					if (fragment != null) {
						fragment.removeHeader();
					}
					DivingLogSightingsListFragment dlfragment = (DivingLogSightingsListFragment) MainActivity.me
							.getFragment("DivingLogSightingsListFragment");
					if (dlfragment != null) {
						dlfragment.removeHeader();
					}
				}
				Preferences.setInt(Preferences.DATAVERSION_FIELDGUIDEANDSIGHTINGS, Catalog.DATAVERSION_FIELDGUIDEANDSIGHTINGS);
				
				if (LanguageChangeEvent.dismissDialogFragment()) {
					String possibleLanguage = LibApp.getInstance()
							.getCurrentCatalog().getPossibleLanguage();
					Preferences.setString(Preferences.CURRENT_LANGUAGE,
							possibleLanguage);
					Preferences.setString(Preferences.IGNORED_LANGUAGE,
							possibleLanguage);
				}
				// Catalog.logAllDatabaseContent();
			}

			@Override
			protected void onProgressUpdate(Integer... values) {
				// runs on UI thread on publishProgress
				Log.d(TAG, values[0] + " " + values[1]);
				int perc = values[0] * 100 / values[1];

				if (MainActivity.me != null) {
					FieldGuideListFragment fragment = (FieldGuideListFragment) MainActivity.me
							.getFragment("FieldGuideListFragment");
					if (fragment != null) {
						fragment.refreshProgress(perc,
								fragment.getActualCursor());
					}

					DivingLogSightingsListFragment dlfragment = (DivingLogSightingsListFragment) MainActivity.me
							.getFragment("DivingLogSightingsListFragment");
					if (dlfragment != null) {
						dlfragment.refreshProgress(perc,
								dlfragment.getActualCursor());
					}
				}
				ThreeChoiceDialogFragment tcdfragment = LanguageChangeEvent
						.getDialogFragment();
				if (tcdfragment != null) {
					tcdfragment.refreshProgressFieldGuide(perc);
				}

				super.onProgressUpdate(values);
			}

		};

		if (!initializing) {
			task.execute(fieldGuideOnly);
		}

	}

	private static class SDCardDatabaseHelper extends SDCardSQLiteOpenHelper {

		public SDCardDatabaseHelper(Context context, String dir) {
			super(context, dir, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d(TAG, FIELDGUIDE_CREATE + " | " + SIGHTINGS_CREATE);
			db.beginTransaction();
			try {
				db.execSQL(FIELDGUIDE_CREATE);
				db.execSQL(SIGHTINGS_CREATE);
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
		}

		@Override
		// with a new appversion needing a new dbstructure (DBVERSION > than
		// DBVERSION on the device of the user)
		// handle the change
		// every change will be done on fallthrough (so no breaks please)
		// thus handling users that have skipped some upgrades
		//
		// that DOES mean you have to put every version in the case list, even
		// if nothing really happens there - it's the point the app will enter
		// the fallthrough
		//
		// There is a problem with testing and trying to go back to a previous
		// dbversion (you can't)
		// this may lead to extra versions (p.e 2 and 3) that are not actually
		// necessary for the app users
		// because they never will get the intermediate versions
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.d(TAG, "Upgrading database from appversion " + oldVersion
					+ " to " + newVersion);
			dbExternal = db;
			switch (oldVersion) {
			case 1:
			case 2:
			case 3:
				handleUpgradeFrom_003_to_004(db);
			}

		}

		@Override
		public void onOpen(SQLiteDatabase db) {
			dbExternal = db;
			Log.d(TAG, "onOpen "+openedOnceAlready);
			openedOnceAlready=true;
			super.onOpen(db);
		}

		private boolean handleUpgradeFrom_003_to_004(SQLiteDatabase db) {
			/*
			 * adding public static final String KEY_SHOWGROUPNAME =
			 * "show_group_name"; public static final String KEY_SHOWCOMMONNAME
			 * = "show_common_name"; public static final String
			 * KEY_CS_SHOWCHECKVALUES = "cs_showcheckvalues"; to FieldGuide
			 */
			boolean changeSucceeded = false;
			if (me.columnExists(FIELDGUIDE, KEY_SHOWGROUPNAME)) {
				changeSucceeded = true;
			} else {

				db.beginTransaction();
				try {
					db.execSQL("ALTER TABLE " + FIELDGUIDE + " ADD COLUMN "
							+ KEY_SHOWGROUPNAME);
					db.execSQL("ALTER TABLE " + FIELDGUIDE + " ADD COLUMN "
							+ KEY_SHOWCOMMONNAME);
					db.execSQL("ALTER TABLE " + FIELDGUIDE + " ADD COLUMN "
							+ KEY_CS_SHOWCHECKVALUES);
					db.execSQL("ALTER TABLE " + SIGHTINGS + " ADD COLUMN "
							+ KEY_SHOWCOMMONNAME);
					db.execSQL("ALTER TABLE " + SIGHTINGS + " ADD COLUMN "
							+ KEY_SHOWGROUPNAME);
					db.execSQL("ALTER TABLE " + SIGHTINGS + " ADD COLUMN "
							+ KEY_SHOW_SIGHTING_VALUE);
					db.execSQL("ALTER TABLE " + SIGHTINGS + " ADD COLUMN "
							+ KEY_SHOW_CS_CHECKEDVALUES);
					db.setTransactionSuccessful();
					changeSucceeded = true;
				} catch (SQLException e) {
					Log.e(TAG,
							"adding fields for fieldguide version 1 to 2 failed",
							e);
				} finally {
					db.endTransaction();
				}
			}
			Log.d(TAG,
					"adding fields for fieldguide version 1 to 2 finished succesfully["
							+ changeSucceeded + "]");

			return changeSucceeded;
		}

		public String toString() {
			return "DiveDbHelper.SDCardDatabaseHelper";
		};

	}

	public void updateOrCreateEntriesForGroupInDive(int diveId,
			String groupName, String value) {
		Log.d(TAG,
				"value[" + value + "] default["
						+ LibApp.getDiveOrPersonalOrCatalogDefaultChoice()
						+ "]");
		if (value.equals(LibApp.getDiveOrPersonalOrCatalogDefaultChoice())) {
			Log.d(TAG, "delete anything filled for group");
			int done = deleteSightingsForGroupInDive(diveId, groupName);
			Log.d(TAG, "deleted[" + done + "] for group[" + groupName + "]");
		} else {
			Log.d(TAG, "fill all with value for group");
			/*
			 * int done = updateSightingsForGroupInDive(diveId, groupName,
			 * value); Log.d(TAG, "updated[" + done + "] for group[" + groupName
			 * + "]");
			 */
			int done = (int) insertSightingsForGroupInDive(diveId, groupName,
					value);
			Log.d(TAG, "upserted[" + done + "] for group[" + groupName + "]");
		}
	}

	public boolean columnExists(String tableName, String columnName) {

		Cursor mCursor = dbExternal.query(tableName, null, null, null, null,
				null, null);
		if (mCursor != null) {
			if (mCursor.getColumnIndex(columnName) != -1) {
				mCursor.close();
				return true;
			} else {
				mCursor.close();
				return false;
			}
		}
		return false;

	}

}
