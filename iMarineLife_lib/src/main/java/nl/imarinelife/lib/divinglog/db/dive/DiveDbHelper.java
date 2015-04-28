package nl.imarinelife.lib.divinglog.db.dive;

import java.util.ArrayList;
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
import nl.imarinelife.lib.divinglog.db.res.Buddy;
import nl.imarinelife.lib.divinglog.db.res.Location;
import nl.imarinelife.lib.divinglog.db.res.LocationDbHelper;
import nl.imarinelife.lib.divinglog.db.res.ProfilePartDbHelper;
import nl.imarinelife.lib.divinglog.sightings.Sighting;
import nl.imarinelife.lib.fieldguide.db.FieldGuideAndSightingsEntryDbHelper;
import nl.imarinelife.lib.utility.SDCardSQLiteOpenHelper;
import nl.imarinelife.lib.utility.SQLUpdateObject;
import nl.imarinelife.lib.utility.SerializableSparseArray;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

public class DiveDbHelper implements DbHelper {
	public static boolean usedInBackgroundThread = false;
	public static boolean closeAfterBackgroundThreadFinishes = false;
	private static boolean openedOnceAlready = false;

	private static DiveDbHelper me = null;

	public static final String KEY_ROWID = "_id";
	public static final String KEY_CATNAME = "cat_name";

	public static final String KEY_DIVENR = "divenr";
	public static final String KEY_DATE = "date";
	public static final String KEY_TIME = "time";
	public static final String KEY_VISIBILITY = "visibility";

	public static final String KEY_LOCATIONCATCODE = "cat_code";
	public static final String KEY_LOCATIONNAME = "location_name";

	public static final String KEY_BUDDYNAME = "buddy_name";
	public static final String KEY_BUDDYEMAIL = "buddy_email";
	public static final String KEY_BUDDYCODE = "buddy_code";

	public static final String KEY_SENT_ALREADY = "sentAlready";
	public static final String KEY_REMARKS = "remarks";
	public static final String KEY_DEFAULT_CHOICE = "default_choice";

	// ALL
	public static final int KEY_ROWID_CURSORLOC = 0;
	public static final int KEY_CATNAME_CURSORLOC = 1;

	public static final int KEY_DIVENR_CURSORLOC = 2;
	public static final int KEY_DATE_CURSORLOC = 3;
	public static final int KEY_TIME_CURSORLOC = 4;
	public static final int KEY_VISIBILITY_CURSORLOC = 5;

	public static final int KEY_LOCATIONCATCODE_CURSORLOC = 6;
	public static final int KEY_LOCATIONNAME_CURSORLOC = 7;

	public static final int KEY_BUDDYNAME_CURSORLOC = 8;
	public static final int KEY_BUDDYEMAIL_CURSORLOC = 9;
	public static final int KEY_BUDDYCODE_CURSORLOC = 10;
	public static final int KEY_SENT_ALREADY_CURSORLOC = 11;
	public static final int KEY_REMARKS_CURSORLOC = 12;

	public static final int KEY_DEFAULT_CHOICE_CURSORLOC = 13;

	public static final Map<Integer, Integer> CODE_TO_SHOWVALUE_COLUMNMAPPING = null;

	// BUDDYS
	public static final int KEY_BUDDYNAME_CURSORLOC_BUDDYLIST = 1;
	public static final int KEY_DIVECOUNTFORBUDDY_CURSORLOC = 2;

	private static final String TAG = "DiveDbHelper";

	private SDCardDatabaseHelper dbHelperExternal;
	private static SQLiteDatabase dbExternal;

	public static final String DATABASE_NAME = "Dives";
	public static final int DATABASE_VERSION = 1;
	public static final int DATAVERSION = 1;
	private static final String SQLITE_TABLE = "Dives";

	private DiveProfilePartDbHelper diveProfileHelper;
	private FieldGuideAndSightingsEntryDbHelper sightingsHelper;

	public static final String[] ALL = new String[] { KEY_ROWID, KEY_CATNAME,
			KEY_DIVENR, KEY_DATE, KEY_TIME, KEY_VISIBILITY,
			KEY_LOCATIONCATCODE, KEY_LOCATIONNAME, KEY_BUDDYNAME,
			KEY_BUDDYEMAIL, KEY_BUDDYCODE, KEY_SENT_ALREADY, KEY_REMARKS,
			KEY_DEFAULT_CHOICE };
	public static final String[] BUDDYS = new String[] { KEY_ROWID,
			KEY_BUDDYNAME, "count()" };

	private static final String DATABASE_CREATE = "CREATE TABLE if not exists "
			+ SQLITE_TABLE + " (" + KEY_ROWID
			+ " integer PRIMARY KEY autoincrement," + KEY_CATNAME + ","
			+ KEY_DIVENR + "," + KEY_DATE + "," + KEY_TIME + ","
			+ KEY_VISIBILITY + "," + KEY_LOCATIONCATCODE + ","
			+ KEY_LOCATIONNAME + "," + KEY_BUDDYNAME + "," + KEY_BUDDYEMAIL
			+ "," + KEY_BUDDYCODE + "," + KEY_SENT_ALREADY + "," + KEY_REMARKS
			+ "," + KEY_DEFAULT_CHOICE + "," + " UNIQUE (" + KEY_DIVENR + "));";

	private static class SDCardDatabaseHelper extends SDCardSQLiteOpenHelper {

		public SDCardDatabaseHelper(Context context, String dir) {
			super(context, dir, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d(TAG, DATABASE_CREATE);
			db.beginTransaction();
			try {
				db.execSQL(DATABASE_CREATE);
				db.setTransactionSuccessful();
			} catch (SQLException e) {
				Log.d(TAG, "database (Dives) creation on SD failed", e);
			} finally {
				db.endTransaction();
			}
		}

		@Override
		// with a new appversion needing a new dbstructure
		// handle the change
		// every change will be done on fallthrough (so no breaks please)
		// thus handling users that have skipped some upgrades
		//
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if (oldVersion < newVersion) {
				Log.d(TAG, "Upgrading Divedatabase from appversion "
						+ oldVersion + " to " + newVersion);
				dbExternal = db;
				switch (oldVersion) {
				case 1:

				}

			}
			// onCreate(db);
		}

		@Override
		public void onOpen(SQLiteDatabase db) {
			dbExternal = db;
			int oldDataVersion = Preferences.getInt(
					Preferences.DATAVERSION_DIVES, 0);
			if (DATAVERSION > oldDataVersion && !(openedOnceAlready)) {
				onDataVersionUpgrade(db, oldDataVersion, DATAVERSION);
			}
			openedOnceAlready = true;
			super.onOpen(db);
		}

		private void onDataVersionUpgrade(SQLiteDatabase db, int oldVersion,
				int newVersion) {
			switch (oldVersion) {
			case 0: Preferences.setInt(Preferences.DATAVERSION_DIVES, DATAVERSION);
				
			}

		}

		
		public String toString() {
			return "DiveDbHelper.SDCardDatabaseHelper";
		};

	}

	@SuppressWarnings("deprecation")
	public static DiveDbHelper getInstance(Context ctx) {
		if (me == null) {
			me = new DiveDbHelper(ctx);
		}

		Boolean isSDPresent = SDCardSQLiteOpenHelper
				.isSDCardiMarineLifeDirectoryActive();

		while ((dbExternal != null && (dbExternal.isDbLockedByCurrentThread() || dbExternal
				.isDbLockedByOtherThreads()))) {
		}

		if ((isSDPresent && (dbExternal == null || !dbExternal.isOpen()))) {
			me.open(ctx);
		}

		LibApp.getInstance().dbhelpers.put("DiveDbHelper", me);
		return me;
	}

	public Map<String, String> getLocationCatCodesForDistinctLocationNames() {
		// let's make sure the Dives database has all the secondary keys for
		// Locations and not only the showLocationNames
		String[] columns = { KEY_LOCATIONCATCODE, KEY_LOCATIONNAME };
		String selection = KEY_CATNAME + " = '"
				+ LibApp.getCurrentCatalogName() + "'";
		String groupBy = KEY_LOCATIONNAME;

		Cursor distinct = queryDistinct(columns, selection, groupBy);
		Map<String, String> locationCatCodes = new HashMap<String, String>();
		if (distinct != null && distinct.getCount() > 0) {

			distinct.moveToFirst();
			do {
				locationCatCodes.put(distinct.getString(0),
						distinct.getString(1));
			} while (distinct.moveToNext());

		}

		if (distinct != null) {
			distinct.close();
		}
		return locationCatCodes;
	}

	public void fillLocationCatCodes(Map<String, String> locationCatCodes) {
		// let's make sure the Dives database has all the secondary keys for
		// Locations and not only the showLocationNames
		List<SQLUpdateObject> list = new ArrayList<SQLUpdateObject>();
		if (locationCatCodes != null) {
			LocationDbHelper helper = LocationDbHelper.getInstance(LibApp
					.getContext());

			for (String code : locationCatCodes.keySet()) {
				String name = locationCatCodes.get(code);
				if ((code == null || code.length() == 0)
						&& (name != null && name.length() > 0)) {
					Location location = helper.getLocationForCatalogAndName(
							LibApp.getCurrentCatalogName(), name);
					if (location != null) {
						Log.d(TAG, location.toString());

						ContentValues values = new ContentValues();
						values.put(KEY_LOCATIONCATCODE, code);
						String where = KEY_CATNAME + " = '"
								+ LibApp.getCurrentCatalogName() + "' AND "
								+ KEY_LOCATIONNAME + " = '" + name + "'";

						list.add(new SQLUpdateObject(values, where));

					}
				}
			}
		}
		updateValues(list);

	}

	private Cursor queryDistinct(String[] columns, String selection,
			String groupBy) {

		Cursor mCursor = dbExternal.query(true, SQLITE_TABLE,
				columns == null ? ALL : columns, selection, null, groupBy,
				null, null, null);

		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public Set<String> getDistinctLocationCodesInDives() {
		String[] columns = { KEY_LOCATIONCATCODE };
		String selection = KEY_CATNAME + " = '"
				+ LibApp.getCurrentCatalogName() + "'";
		String groupBy = KEY_LOCATIONCATCODE;

		Cursor distinct = queryDistinct(columns, selection, groupBy);
		Catalog currentCatalog = LibApp.getInstance().getCurrentCatalog();
		Set<String> locationCodes = new HashSet<String>();

		if (distinct != null && distinct.getCount() > 0
				&& currentCatalog != null) {
			distinct.moveToFirst();
			do {
				String code = distinct.getString(0);
				if ((code != null && code.length() > 0)) {
					if (!code.startsWith("loc")) {
						code = "loc" + code;
					}
					locationCodes.add(code);
				}
			} while (distinct.moveToNext());

		} else {
			Log.d(TAG, "no entries found for catalog");
			//logAllDatabaseContent(LibApp.getContext());
		}
		if (distinct != null) {
			distinct.close();
		}
		return locationCodes;

	}

	public void fillLocationShowNames(Set<String> locationCodes) {
		// let's make sure the Dives database has all the correct
		// showLocationNames
		Catalog currentCatalog = LibApp.getInstance().getCurrentCatalog();
		if (locationCodes != null && locationCodes.size() > 0) {
			List<SQLUpdateObject> list = new ArrayList<SQLUpdateObject>();

			for (String code : locationCodes) {
				String name = currentCatalog.getResourcedValue(
						currentCatalog.getLocationNamesMapping(), code, null);
				Log.d(TAG, "fillLocationShowNames - handling code[" + code
						+ "] getting name[" + name + "]");
				if (name != null && name.length() > 0) {

					ContentValues values = new ContentValues();
					values.put(KEY_LOCATIONNAME, name);
					String where = KEY_CATNAME + " = '"
							+ LibApp.getCurrentCatalogName() + "' AND "
							+ KEY_LOCATIONCATCODE + " = '" + code.substring(3) + "'";

					list.add(new SQLUpdateObject(values, where));
				}
			}

			updateValues(list);
		}

	}

	public int updateValues(List<SQLUpdateObject> updateList) {
		int updated = 0;
		if (updateList != null) {
			dbExternal.beginTransaction();
			try {
				for (SQLUpdateObject update : updateList) {
					ContentValues initialValues = update.getUpdateValues();
					String selection = update.getSelection();
					if (initialValues != null && initialValues.size() > 0) {
						updated += dbExternal.update(SQLITE_TABLE,
								initialValues, selection, null);
						Log.d(TAG,"updateValues - setting["+update.getUpdateValues().toString()+"] selection["+selection+"] updated["+updated+"]");
						
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

	/*
	 * public void replaceLocationCodesForName(String name, String catCode) {
	 * ContentValues initialValues = new ContentValues();
	 * initialValues.put(KEY_LOCATIONCATCODE, catCode); int update =
	 * dbExternal.update(SQLITE_TABLE, initialValues, KEY_CATNAME + " = '" +
	 * LibApp.getCurrentCatalogName() + "' AND " + KEY_LOCATIONNAME + " = '" +
	 * name + "'", null); Log.d(TAG, "replaceLocationCodesForName: [" + update +
	 * " updates done]");
	 * 
	 * }
	 * 
	 * public void replaceLocationNamesForCode(String catCode, String name) {
	 * ContentValues initialValues = new ContentValues();
	 * initialValues.put(KEY_LOCATIONNAME, name); int update =
	 * dbExternal.update(SQLITE_TABLE, initialValues, KEY_CATNAME + " = '" +
	 * LibApp.getCurrentCatalogName() + "' AND " + KEY_LOCATIONCATCODE + " = '"
	 * + catCode + "'", null); Log.d(TAG, "replaceLocationNamesForCodes: [" +
	 * update + " updates done]");
	 * 
	 * }
	 */
	private DiveDbHelper(Context context) {
	}

	/*
	 * Strategy is to keep both local and SD card up to date with all dives from
	 * this catalog Dives will be shown from SD if possible (showing everything,
	 * also from other catalogs) if not, only cataloglocal dives are shown, from
	 * local storage
	 * 
	 * If appstorage is removed (or the app is removed and installed again) and
	 * external (SD for short, but it may not be an actual SD card) storage has
	 * data for this Catalog the user will be asked if he wants to restore the
	 * data
	 * 
	 * If there is an SDCard but it was not available on last usage
	 * (usesSD=false) the data on SD will be removed and replaced with what is
	 * available on appstorage
	 * 
	 * If data on the SD card has been tampered with, the user may be able to
	 * restore by resetting usesSD in the app settings
	 */
	@Override
	public DiveDbHelper open(Context mCtx) throws SQLException {
		/*
		 * boolean usesSD = Preferences.getBoolean(Preferences.USES_SD, false);
		 */
		Boolean isSDPresent = SDCardSQLiteOpenHelper
				.isSDCardiMarineLifeDirectoryActive();
		if (!isSDPresent) {
			Toast.makeText(
					MainActivity.me,
					LibApp.getCurrentResources().getString(
							R.string.externalMemoryNeeded), Toast.LENGTH_LONG)
					.show();
			MainActivity.me.finish(); // ??
		}

		Log.d(TAG,
				"Opening db - onCreate should get called if necessary - SDPresent["
						+ isSDPresent + "]");
		dbHelperExternal = new SDCardDatabaseHelper(mCtx,
				SDCardSQLiteOpenHelper.getDataBasesDirectory());
		dbExternal = dbHelperExternal.getWritableDatabase();
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.imarinelife.lib.divinglog.db.dive.DbHelper#finalize()
	 */
	@Override
	public void finalize() throws Throwable {
		close();
		super.finalize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.imarinelife.lib.divinglog.db.dive.DbHelper#close()
	 */
	@Override
	public void close() {

		Log.d(TAG, "closing db");
		if (!usedInBackgroundThread) {
			if (dbHelperExternal != null) {
				dbHelperExternal.close();
				dbHelperExternal = null;
			}
			if (dbExternal != null) {
				dbExternal.close();
				dbExternal = null;
			}
		} else {
			Log.d(TAG,
					"close() - cannot close because connection is in use in background thread");
			closeAfterBackgroundThreadFinishes = true;
		}
	}

	public long upsertDive(Dive dive, Context mCtx) {
		String catalog = dive.getCatalog();
		Log.d(TAG, "dive upsert [" + dive + "]");
		Log.d("dive upsert", dive.getBuddy() + " catalog: " + catalog);

		long result = 0;
		Boolean isSDPresent = SDCardSQLiteOpenHelper
				.isSDCardiMarineLifeDirectoryActive();

		if (isSDPresent) {
			boolean alreadyExists = false;
			Cursor cursor = fetchCursorForDive(dive.getDiveNr());
			if (cursor != null && cursor.getCount() != 0) {
				alreadyExists = true;
			}
			cursor.close();
			ContentValues initialValues = new ContentValues();
			initialValues.put(KEY_CATNAME, catalog);
			initialValues.put(KEY_DIVENR, dive.getDiveNr());
			initialValues.put(KEY_DATE, dive.getDate());
			initialValues.put(KEY_TIME, dive.getTime());
			initialValues.put(KEY_VISIBILITY, dive.getVisibilityInMeters());
			initialValues.put(KEY_LOCATIONCATCODE, dive.getLocationCode());
			initialValues.put(KEY_LOCATIONNAME, dive.getLocationName());
			initialValues.put(KEY_BUDDYNAME, dive.getBuddyName());
			initialValues.put(KEY_BUDDYEMAIL, dive.getBuddyEmail());
			initialValues.put(KEY_BUDDYCODE, dive.getBuddyCode());
			initialValues.put(KEY_BUDDYCODE, dive.getBuddyCode());
			initialValues.put(KEY_DEFAULT_CHOICE, dive.getDiveDefaultChoice());
			initialValues.put(KEY_REMARKS, dive.getRemarks());

			dbExternal.beginTransaction();

			diveProfileHelper = DiveProfilePartDbHelper.getInstance(mCtx);
			diveProfileHelper.upsertProfileParts(dive.getProfile());

			try {
				if (alreadyExists) {
					int update = dbExternal.update(SQLITE_TABLE, initialValues,
							KEY_DIVENR + " = " + dive.getDiveNr(), null);
					update += updateBuddyDataFromDiveOverAllRelevantDives(dive);
					Log.d(TAG, dbExternal + " updated[" + update + "]");
					result += update;
				} else {
					long insert = dbExternal.insert(SQLITE_TABLE, null,
							initialValues);
					Log.d(TAG, dbExternal + " inserted[" + insert + "]");
					result += insert;
				}

				dbExternal.setTransactionSuccessful();
			} finally {
				dbExternal.endTransaction();
			}
		}
		return result;
	}

	private int updateBuddyDataFromDiveOverAllRelevantDives(Dive dive) {
		String formerBuddyName = dive.getBuddyNameSelected();
		int counter = 0;
		// update all buddy emailadresses (and name if wanted) in the dives with
		// the same buddyName
		Log.d(TAG,
				"updateBuddyDataFromDiveOverAllRelevantDives: "
						+ dive.getBuddyNameSelected());
		Log.d(TAG,
				"updateBuddyDataFromDiveOverAllRelevantDives: "
						+ dive.isBuddyEmailChanged());
		Log.d(TAG,
				"updateBuddyDataFromDiveOverAllRelevantDives: "
						+ dive.isBuddyNameMustbeChangedEveryWhere());
		if (dive.getBuddyNameSelected() != null
				&& (dive.isBuddyEmailChanged() || dive
						.isBuddyNameMustbeChangedEveryWhere())) {
			if (formerBuddyName != null) {
				ContentValues nameAndEmailValues = new ContentValues();
				Log.d(TAG, "updateBuddyDataFromDiveOverAllRelevantDives: "
						+ dive.getBuddyName() + " / " + formerBuddyName);
				boolean onlyChangeCurrentive = false;
				if (!formerBuddyName.equals(dive.getBuddyName())
						&& dive.isBuddyNameMustbeChangedEveryWhere()) {
					onlyChangeCurrentive = true;
					nameAndEmailValues.put(KEY_BUDDYNAME, dive.getBuddyName());
					Log.d(TAG, "updateBuddyDataFromDiveOverAllRelevantDives: "
							+ nameAndEmailValues.getAsString(KEY_BUDDYNAME));
				}
				if (dive.isBuddyEmailChanged()
						&& (onlyChangeCurrentive || formerBuddyName.equals(dive
								.getBuddyName()))) {
					nameAndEmailValues
							.put(KEY_BUDDYEMAIL, dive.getBuddyEmail());
					Log.d(TAG, "updateBuddyDataFromDiveOverAllRelevantDives: "
							+ nameAndEmailValues.getAsString(KEY_BUDDYEMAIL));
				}
				if (nameAndEmailValues.size() > 0) {
					counter += dbExternal.update(SQLITE_TABLE,
							nameAndEmailValues, KEY_BUDDYNAME + " = '"
									+ formerBuddyName + "'", null);
				}
				Log.d(TAG,
						"updateBuddyDataFromDiveOverAllRelevantDives: updated (name/email)["
								+ counter + "]");
				dive.setBuddyNameMustbeChangedEveryWhere(false);
				dive.setBuddyEmailChanged(false);
			}
		}
		// including code for dives with the same catalog as the saved dive
		Log.d(TAG,
				"updateBuddyDataFromDiveOverAllRelevantDives: "
						+ dive.isBuddyCodeChanged());
		if (dive.isBuddyCodeChanged()) {
			if (formerBuddyName != null) {
				ContentValues codeValue = new ContentValues();
				codeValue.put(KEY_BUDDYCODE, dive.getBuddyCode());
				counter += dbExternal.update(SQLITE_TABLE, codeValue,
						KEY_BUDDYNAME + " = '" + formerBuddyName + "' and "
								+ KEY_CATNAME + " = '" + dive.getCatalog()
								+ "'", null);
				Log.d(TAG,
						"updateBuddyDataFromDiveOverAllRelevantDives: updated (code)["
								+ counter + "]");
				dive.setBuddyCodeChanged(false);

			}
		}
		if (dive.getBuddy() != null) {
			dive.getBuddy().setBuddyNameSelected(dive.getBuddyName());
		}
		return counter;
	}

	public boolean deleteDive(int diveNr, String catalog, Context mCtx) {

		int doneDelete = 0;
		Boolean isSDPresent = SDCardSQLiteOpenHelper
				.isSDCardiMarineLifeDirectoryActive();

		if (isSDPresent) {

			dbExternal.beginTransaction();
			diveProfileHelper = DiveProfilePartDbHelper.getInstance(mCtx);
			diveProfileHelper.deleteProfilePartsForDive(diveNr, catalog);
			sightingsHelper = FieldGuideAndSightingsEntryDbHelper
					.getInstance(mCtx);
			sightingsHelper.deleteSightingsForDive(diveNr);

			try {
				doneDelete += dbExternal.delete(SQLITE_TABLE, KEY_DIVENR
						+ " = " + diveNr + " and " + KEY_CATNAME + " = '"
						+ catalog + "'", null);
				dbExternal.setTransactionSuccessful();
			} finally {
				dbExternal.endTransaction();
			}
		}
		Log.d(TAG, Integer.toString(doneDelete));
		return doneDelete > 0;

	}

	public boolean deleteDiveSensuStricto(int diveNr, String catalog,
			Context mCtx) {
		int doneDelete = 0;
		Boolean isSDPresent = SDCardSQLiteOpenHelper
				.isSDCardiMarineLifeDirectoryActive();

		if (isSDPresent) {

			dbExternal.beginTransaction();

			try {
				doneDelete += dbExternal.delete(SQLITE_TABLE, KEY_DIVENR
						+ " = " + diveNr + " and " + KEY_CATNAME + " = '"
						+ catalog + "'", null);
				dbExternal.setTransactionSuccessful();
			} finally {
				dbExternal.endTransaction();
			}
		}
		Log.d(TAG, Integer.toString(doneDelete));
		return doneDelete > 0;

	}

	public boolean deleteAllDives(Context mCtx) {
		int doneDelete = 0;
		Boolean isSDPresent = SDCardSQLiteOpenHelper
				.isSDCardiMarineLifeDirectoryActive();

		if (isSDPresent) {
			dbExternal.beginTransaction();
			diveProfileHelper = DiveProfilePartDbHelper.getInstance(mCtx);
			diveProfileHelper.deleteAllProfileParts();
			sightingsHelper = FieldGuideAndSightingsEntryDbHelper
					.getInstance(mCtx);
			sightingsHelper.deleteAllSightings();

			String whereClause = KEY_CATNAME + " = '"
					+ LibApp.getCurrentCatalogName() + "'";

			try {
				doneDelete += dbExternal
						.delete(SQLITE_TABLE, whereClause, null);
				dbExternal.setTransactionSuccessful();
			} finally {
				dbExternal.endTransaction();
			}
			Log.d(TAG, Integer.toString(doneDelete));
		}
		return doneDelete > 0;

	}

	public boolean deleteProfilePartsForDive(int divenr, String catalog,
			Context mCtx) {
		int doneDelete = 0;
		Boolean isSDPresent = SDCardSQLiteOpenHelper
				.isSDCardiMarineLifeDirectoryActive();

		if (isSDPresent) {
			dbExternal.beginTransaction();
			diveProfileHelper = DiveProfilePartDbHelper.getInstance(mCtx);
			diveProfileHelper.deleteProfilePartsForDive(divenr, catalog);
			/*
			 * diveProfileHelper.close();
			 */try {
				doneDelete += dbExternal.delete(SQLITE_TABLE, KEY_DIVENR
						+ " = " + divenr + " and " + KEY_CATNAME + " = '"
						+ catalog + "'", null);
				dbExternal.setTransactionSuccessful();
			} finally {
				dbExternal.endTransaction();
			}
		}
		Log.d(TAG, Integer.toString(doneDelete));
		return doneDelete > 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * nl.imarinelife.lib.divinglog.db.dive.DbHelper#query(java.lang.String[],
	 * java.lang.String, java.lang.String[], java.lang.String,
	 * android.content.Context)
	 */
	public Cursor query(String[] columns, String selection, String[] selArgs,
			String orderBy, Context mCtx) {

		Cursor mCursor = dbExternal.query(SQLITE_TABLE, columns == null ? ALL
				: columns, selection, selArgs, null, null,
				orderBy == null ? KEY_DIVENR : orderBy);

		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public Cursor fetchAllCursor() {

		Cursor mCursor = dbExternal.query(SQLITE_TABLE, ALL, null, null, null,
				null, KEY_DIVENR);

		return mCursor;
	}

	public Cursor fetchAllCursorWithNoLocationCode() {

		String selection = KEY_LOCATIONCATCODE + " is null OR "
				+ KEY_LOCATIONCATCODE + " = ''";
		Log.d(TAG, selection);
		Cursor mCursor = dbExternal.query(SQLITE_TABLE, ALL, selection, null,
				null, null, KEY_DIVENR);

		return mCursor;
	}

	public Cursor fetchCursorForDive(int divenr) {
		Cursor mCursor = dbExternal.query(SQLITE_TABLE, ALL, KEY_DIVENR + " = "
				+ divenr, null, null, null, KEY_ROWID);

		return mCursor;
	}

	public Cursor fetchCursorForCatalog() {
		Cursor mCursor = dbExternal.query(SQLITE_TABLE, ALL, KEY_CATNAME
				+ " = '" + LibApp.getCurrentCatalogName() + "'", null, null,
				null, KEY_ROWID);

		return mCursor;
	}

	// use only for your 'own' locations
	public Cursor fetchCursorForLocation(String locationName) {
		Cursor mCursor = dbExternal.query(SQLITE_TABLE, ALL, KEY_LOCATIONNAME
				+ " = '" + locationName + "'", null, null, null, KEY_DIVENR);

		return mCursor;
	}

	public Cursor fetchCursorDistinctBuddyList() {

		Cursor mCursor = dbExternal.query(SQLITE_TABLE, BUDDYS, null, null,
				KEY_BUDDYNAME, null, KEY_BUDDYNAME);
		return mCursor;
	}

	public Buddy getUpToDateBuddy(String buddyName) {

		Buddy buddy = null;
		Cursor mCursor = dbExternal.query(SQLITE_TABLE, ALL, KEY_BUDDYNAME
				+ " = '" + buddyName + "'", null, null, null, KEY_DATE + ","
				+ KEY_TIME + " DESC");
		if (mCursor != null && mCursor.getCount() > 0) {
			HashMap<String, String> codesMap = getCodesMap(mCursor);
			mCursor.moveToFirst();
			buddy = new Buddy(mCursor.getString(KEY_BUDDYNAME_CURSORLOC),
					mCursor.getString(KEY_BUDDYEMAIL_CURSORLOC), codesMap);
		}
		mCursor.close();
		return buddy;
	}

	private HashMap<String, String> getCodesMap(Cursor mCursor) {
		HashMap<String, String> codesMap = new HashMap<String, String>();

		mCursor.moveToFirst();
		do {
			String catalog = mCursor.getString(KEY_CATNAME_CURSORLOC);
			String code = mCursor.getString(KEY_BUDDYCODE_CURSORLOC);
			if (code != null && code.length() > 0 && catalog != null
					&& catalog.length() > 0 && !codesMap.containsKey(catalog)) {
				codesMap.put(catalog, code);
			}
		} while (mCursor.moveToNext());
		return codesMap;
	}

	public Cursor fetchCursorForMatchingBuddies(String constraint) {

		String queryString = "SELECT " + KEY_ROWID + "," + KEY_BUDDYNAME
				+ " FROM " + SQLITE_TABLE;
		if (constraint != null) {
			// Query for any rows where the buddyName name contains the
			// string specified in constraint.
			//
			// NOTE:
			// If wildcards are to be used in a rawQuery, they must appear
			// in the query parameters, and not in the query string proper.
			// See http://code.google.com/p/android/issues/detail?id=3153
			constraint = "%" + constraint.toString().trim() + "%";
			queryString += " WHERE " + KEY_BUDDYNAME + " LIKE ? GROUP BY "
					+ KEY_BUDDYNAME;
		}
		String params[] = { constraint };
		if (constraint == null) {
			params = null;
		}

		try {
			Cursor cursor = dbExternal.rawQuery(queryString, params);
			if (cursor != null) {
				cursor.moveToFirst();
				return cursor;
			}
		} catch (SQLException e) {
			Log.d(TAG, e.toString());
			throw e;
		}

		return null;

	}

	public Dive getDiveFromCursor(Cursor cursor, Context mCtx) {
		// cursor should be set to correct value
		if (cursor != null && cursor.getCount() > 0) {
			String catalog = cursor.getString(KEY_CATNAME_CURSORLOC);
			int diveNr = cursor.getInt(KEY_DIVENR_CURSORLOC);
			long date = cursor.getLong(KEY_DATE_CURSORLOC);
			long time = cursor.getLong(KEY_TIME_CURSORLOC);
			int visibility = cursor.getInt(KEY_VISIBILITY_CURSORLOC);
			boolean sentAlready = Boolean.valueOf(cursor
					.getString(KEY_SENT_ALREADY_CURSORLOC));
			String remarks = cursor.getString(KEY_REMARKS_CURSORLOC);
			String defaultChoice = cursor
					.getString(KEY_DEFAULT_CHOICE_CURSORLOC);

			Location location = null;
			String locationId = "loc"
					+ cursor.getString(KEY_LOCATIONCATCODE_CURSORLOC);
			location = new Location(catalog, locationId,
					cursor.getString(KEY_LOCATIONCATCODE_CURSORLOC),
					cursor.getString(KEY_LOCATIONNAME_CURSORLOC));

			String buddyName = cursor.getString(KEY_BUDDYNAME_CURSORLOC);
			String buddyEmail = cursor.getString(KEY_BUDDYEMAIL_CURSORLOC);
			String buddyCode = cursor.getString(KEY_BUDDYCODE_CURSORLOC);
			Log.d(TAG, "code from cursor: " + buddyCode);
			Buddy buddy = new Buddy(buddyName, buddyEmail, buddyCode, catalog);
			buddy.setBuddyNameSelected(buddyName);
			Log.d(TAG, "buddy: " + buddy);

			diveProfileHelper = DiveProfilePartDbHelper.getInstance(mCtx);
			HashMap<ProfilePartDbHelper.AddType, SerializableSparseArray<DiveProfilePart>> profiles = diveProfileHelper
					.fetchAll(diveNr, mCtx);

			sightingsHelper = FieldGuideAndSightingsEntryDbHelper
					.getInstance(mCtx);
			Cursor sightingsCursor = sightingsHelper.querySightingAsIsForDive(
					diveNr, catalog);
			SerializableSparseArray<Sighting> sightings = sightingsHelper
					.getSightingsMapFrom(sightingsCursor);
			Log.d(TAG, "sightings[" + sightings + "]");
			sightingsCursor.close();
			Dive dive = new Dive(catalog, diveNr, date, time, location, buddy,
					profiles, sightings, visibility, sentAlready, remarks,
					defaultChoice);
			Log.d(TAG, "dive[" + dive + "]");
			return dive;
		} else {
			return null;
		}
	}

	public Buddy getBuddyFromCursor(Cursor cursor) {
		// cursor should be set to correct value
		if (cursor != null && cursor.getCount() > 0) {
			String buddyName = cursor.getString(KEY_BUDDYNAME_CURSORLOC);
			String buddyEmail = cursor.getString(KEY_BUDDYEMAIL_CURSORLOC);
			String buddyCode = cursor.getString(KEY_BUDDYCODE_CURSORLOC);
			String catName = cursor.getString(KEY_CATNAME_CURSORLOC);
			Buddy buddy = new Buddy(buddyName, buddyEmail, buddyCode, catName);
			return buddy;
		} else {
			return null;
		}

	}

	public int getHighestDiveNumber() {
		int value = 0;

		Cursor mCursor = dbExternal.query(SQLITE_TABLE,
				new String[] { KEY_DIVENR }, null, null, null, null, KEY_DIVENR
						+ " desc");
		if (mCursor != null && mCursor.getCount() > 0) {
			mCursor.moveToFirst();
			value = mCursor.getInt(0);
		}

		return value;
	}

	public static void logAllDatabaseContent(Context ctx) {
		DiveDbHelper me = DiveDbHelper.getInstance(ctx);
		Cursor cursor = me.fetchAllCursor();
		if (cursor != null) {
			cursor.moveToFirst();
			Log.d(TAG, "logAllDatabaseContent - Dives");
			while (!cursor.isAfterLast()) {
				Dive dive = me.getDiveFromCursor(cursor, ctx);
				Log.d(TAG, "logAllDatabaseContent - Dives: " + dive.toString());
				cursor.moveToNext();
			}
		} else {
			Log.d(TAG, "logAllDatabaseContent - Dives:  nothing to log");
		}
		cursor.close();
	}
}
