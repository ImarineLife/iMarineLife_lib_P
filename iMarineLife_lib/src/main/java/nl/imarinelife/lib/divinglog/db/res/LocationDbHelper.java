package nl.imarinelife.lib.divinglog.db.res;

import java.util.List;

import nl.imarinelife.lib.DbHelper;
import nl.imarinelife.lib.LibApp;
import nl.imarinelife.lib.Preferences;
import nl.imarinelife.lib.catalog.Catalog;
import nl.imarinelife.lib.divinglog.db.dive.DiveDbHelper;
import nl.imarinelife.lib.utility.SQLUpdateObject;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class LocationDbHelper implements DbHelper {
	private static LocationDbHelper me = null;
	public static boolean usedInBackgroundThread = false;
	public static boolean closeAfterBackgroundThreadFinishes = false;
	public static boolean openedOnceAlready = false;

	public static final String KEY_ROWID = "_id";
	public static final String KEY_CATCODE = "cat_code";
	public static final String KEY_LOCATIONID = "name";
	public static final String KEY_CATNAME = "cat_name";
	public static final String KEY_SHOWLOCATIONNAME = "show_name";
	public static final int KEY_ROWID_CURSORLOC = 0;
	public static final int KEY_CATCODE_CURSORLOC = 1;
	public static final int KEY_LOCATIONID_CURSORLOC = 2;
	public static final int KEY_CATNAME_CURSORLOC = 3;
	public static final int KEY_SHOWLOCATIONNAME_CURSORLOC = 4;

	private static final String TAG = "LocationDbHelper";

	private DatabaseHelper mDbHelper;
	private static SQLiteDatabase mDb;

	private static final String DATABASE_NAME = "Locations";
	private static final int DATABASE_VERSION = 2;
	public static final int DATAVERSION = 2;
	private static final String SQLITE_TABLE = "Locations";

	private static final String DATABASE_CREATE = "CREATE TABLE if not exists "
			+ SQLITE_TABLE + " (" + KEY_ROWID
			+ " integer PRIMARY KEY autoincrement," + KEY_CATNAME + ","
			+ KEY_CATCODE + "," + KEY_LOCATIONID + "," + KEY_SHOWLOCATIONNAME
			+ "," + " UNIQUE (" + KEY_CATNAME + "," + KEY_CATCODE + ","
			+ KEY_LOCATIONID + "));";

	public static final String[] ALL = new String[] { KEY_ROWID, KEY_CATCODE,
			KEY_LOCATIONID, KEY_CATNAME, KEY_SHOWLOCATIONNAME };

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d(TAG, DATABASE_CREATE);
			db.beginTransaction();
			try {
				db.execSQL(DATABASE_CREATE);
				db.setTransactionSuccessful();
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
				Log.d(TAG, "Upgrading Locationsdatabase from appversion "
						+ oldVersion + " to " + newVersion);
				mDb = db;
				switch (oldVersion) {
				case 1:
					handleUpgradeFrom_001_to_002(db);
				}

			}
			// onCreate(db);
		}

		@Override
		public void onOpen(SQLiteDatabase db) {
			mDb = db;
			openedOnceAlready=true;
			super.onOpen(db);
		}

		private boolean handleUpgradeFrom_001_to_002(SQLiteDatabase db) {
			/*
			 * adding public static final String KEY_SHOWLOCATIONNAME =
			 * "show_name"; to FieldGuide
			 */
			boolean changeSucceeded = false;

			if (me.columnExists(KEY_SHOWLOCATIONNAME)) {
				changeSucceeded = true;
			} else {

				db.beginTransaction();
				try {
					db.execSQL("ALTER TABLE " + SQLITE_TABLE + " ADD COLUMN "
							+ KEY_SHOWLOCATIONNAME);
					db.setTransactionSuccessful();
					changeSucceeded = true;
				} catch (SQLException e) {
					Log.e(TAG,
							"adding fields for locations version 1 to 2 failed",
							e);
				} finally {
					db.endTransaction();
				}
			}
			Log.d(TAG,
					"adding fields for locations version 1 to 2 finished succesfully["
							+ changeSucceeded + "]");
			return changeSucceeded;
		}
	}

	@SuppressWarnings("deprecation")
	public static LocationDbHelper getInstance(Context ctx) {
		if (me == null) {
			me = new LocationDbHelper();
		}
		if (me.mDb == null || !me.mDb.isOpen()) {
			me.open(ctx);
		}
		while (me.mDb.isDbLockedByCurrentThread()
				|| me.mDb.isDbLockedByOtherThreads()) {
		}
		LibApp.getInstance().dbhelpers.put("LocationDbHelper", me);
		return me;
	}

	private LocationDbHelper() {
	}

	public LocationDbHelper open(Context ctx) throws SQLException {
		Log.d(TAG, "Opening db - onCreate should get called if necessary");

		mDbHelper = new DatabaseHelper(ctx);
		mDb = mDbHelper.getWritableDatabase();
		// just to get rid of the reference (which may have been necessary in an
		// onUpgrade)
		return this;
	}

	public void finalize() throws Throwable {
		close();
		super.finalize();
	}

	public void close() {
		if (!usedInBackgroundThread) {
			Log.d(TAG, "closing db");
			if (mDbHelper != null) {
				mDbHelper.close();
			}
			if (mDb != null) {
				mDb.close();
			}
		} else {
			Log.d(TAG,
					"close() - cannot close because connection is in use in background thread");
			closeAfterBackgroundThreadFinishes = true;
		}

	}

	public long insertPersonalLocation(String locationId, String showName) {
		return upsertLocation(
				Catalog.PERSONAL + LibApp.getCurrentCatalogName(), "",
				locationId, showName);
	}

	public int insertLocations(List<Location> insertList) {
		int inserted = 0;
		if (insertList != null) {
			mDb.beginTransaction();
			try {
				for (Location location : insertList) {
					ContentValues initialValues = new ContentValues();
					initialValues.put(KEY_CATNAME, location.getCatName());
					initialValues.put(KEY_CATCODE, location.getCatCode());
					initialValues.put(KEY_LOCATIONID, location.getLocationId());
					initialValues.put(KEY_SHOWLOCATIONNAME,
							location.getShowLocationName());

					mDb.insert(SQLITE_TABLE, null, initialValues);
					inserted++;

				}
				mDb.setTransactionSuccessful();
			} catch (Exception e) {
				Log.e(TAG, "error while inserting locations", e);
				inserted = -1;
			} finally {
				mDb.endTransaction();
			}

		}
		return inserted;
	}

	public long upsertLocation(String catName, String catCode,
			String locationId, String showName) {
		long result = 0;
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_CATNAME, catName);
		initialValues.put(KEY_CATCODE, catCode);
		initialValues.put(KEY_LOCATIONID, locationId);
		initialValues.put(KEY_SHOWLOCATIONNAME, showName);

		boolean alreadyExists = false;
		Location location = getLocationForCatalog(catName, locationId);
		if (location != null) {
			Log.d(TAG, "upsertLocation - exists location[" + location + "]");
			alreadyExists = true;
		}

		mDb.beginTransaction();
		try {
			if (alreadyExists) {
				Log.d(TAG, "upsertLocation updating[" + catName + "," + catCode
						+ "," + locationId + "," + showName + "]");
				result = mDb.update(SQLITE_TABLE, initialValues, KEY_CATNAME
						+ " = '" + catName + "' AND " + KEY_LOCATIONID + " = '"
						+ locationId + "'", null);
			} else {
				Log.d(TAG, "upsertLocation inserting[" + catName + ","
						+ catCode + "," + locationId + "," + showName + "]");
				result = mDb.insert(SQLITE_TABLE, null, initialValues);
			}
			mDb.setTransactionSuccessful();
		} finally {
			mDb.endTransaction();
		}

		return result;
	}

	public int deletePersonalLocation(String locationId) {

		int doneDelete = 0;

		mDb.beginTransaction();
		try {
			doneDelete = mDb
					.delete(SQLITE_TABLE, KEY_CATNAME + "='" + Catalog.PERSONAL
							+ LibApp.getCurrentCatalogName() + "' and "
							+ KEY_LOCATIONID + "='" + locationId + "'", null);
			mDb.setTransactionSuccessful();
		} finally {
			mDb.endTransaction();
		}

		Log.d(TAG, Integer.toString(doneDelete));
		return doneDelete;

	}

	public boolean deleteAllLocations() {

		int doneDelete = 0;

		mDb.beginTransaction();
		try {
			doneDelete = mDb.delete(SQLITE_TABLE, null, null);
			mDb.setTransactionSuccessful();
		} finally {
			mDb.endTransaction();
		}

		Log.d(TAG, Integer.toString(doneDelete));
		return doneDelete > 0;

	}

	public int deleteAllPersonalLocations() {
		return deleteAllLocationsForCatalog(Catalog.PERSONAL
				+ LibApp.getCurrentCatalogName());
	}

	public int deleteAllLocationsForCurrentCatalog() {
		return deleteAllLocationsForCatalog(LibApp.getCurrentCatalogName());
	}

	public int deleteAllLocationsForCatalog(String catName) {
		int doneDelete = 0;

		mDb.beginTransaction();
		try {
			doneDelete = mDb.delete(SQLITE_TABLE, KEY_CATNAME + "='" + catName
					+ "'", null);
			mDb.setTransactionSuccessful();
		} finally {
			mDb.endTransaction();
		}

		Log.d(TAG,
				"deleteAllLocationsForCatalog: deleted["
						+ Integer.toString(doneDelete) + "]");
		return doneDelete;

	}

	public Cursor fetchPersonalLocation(String locationId) {

		Cursor mCursor = null;

		mCursor = mDb.query(SQLITE_TABLE, ALL, KEY_CATNAME + "='"
				+ Catalog.PERSONAL + LibApp.getCurrentCatalogName() + "' and "
				+ KEY_LOCATIONID + "='" + locationId + "'", null, null, null,
				KEY_LOCATIONID);
		return mCursor;

	}

	public Cursor fetchLocationsCursorForCurrentCatalogAndPersonal()
			throws SQLException {
		return fetchLocationsCursorForCatalogAndPersonal(LibApp
				.getCurrentCatalogName());
	}

	public Cursor fetchLocationsCursorForCatalogAndPersonal(String catalog)
			throws SQLException {
		Log.d(TAG, catalog);
		Cursor mCursor = null;

		if (catalog == null || catalog.length() == 0) {
			mCursor = mDb.query(SQLITE_TABLE, ALL, null, null, null,
					KEY_LOCATIONID, null);

		} else {
			mCursor = mDb.query(true, SQLITE_TABLE, ALL, KEY_CATNAME + "= '"
					+ catalog + "' or " + KEY_CATNAME + "= '"
					+ Catalog.PERSONAL + LibApp.getCurrentCatalogName() + "'",
					null, null, null, KEY_LOCATIONID, null);
		}

		return mCursor;

	}

	public Location getLocationForCatalog(String catalog, String locationId)
			throws SQLException {
		Log.d(TAG, "getLocationForCatalog cat[" + catalog + "]locationId["
				+ locationId + "]");
		Cursor mCursor = null;

		mCursor = mDb.query(true, SQLITE_TABLE, ALL, "(" + KEY_CATNAME + " = '"
				+ catalog + "' or " + KEY_CATNAME + "= '" + Catalog.PERSONAL
				+ LibApp.getCurrentCatalogName() + "') and " + KEY_LOCATIONID
				+ " = '" + locationId + "'", null, null, null, KEY_LOCATIONID,
				null);
		Location location = null;
		if (mCursor != null) {
			mCursor.moveToFirst();
			if (!mCursor.isAfterLast()) {
				location = new Location(
						mCursor.getString(KEY_CATNAME_CURSORLOC),
						mCursor.getString(KEY_CATCODE_CURSORLOC),
						mCursor.getString(KEY_LOCATIONID_CURSORLOC),
						mCursor.getString(KEY_SHOWLOCATIONNAME_CURSORLOC));
			}
		}
		mCursor.close();
		Log.d(TAG, "getLocationForCatalog found[" + location + "]");

		return location;

	}

	public Location getLocationForCatalogAndName(String catalog,
			String locationName) throws SQLException {
		Log.d(TAG, "getLocationForCatalogAndName cat[" + catalog
				+ "]locationName[" + locationName + "]");
		Cursor mCursor = null;

		mCursor = mDb.query(true, SQLITE_TABLE, ALL, "(" + KEY_CATNAME + " = '"
				+ catalog + "' or " + KEY_CATNAME + "= '" + Catalog.PERSONAL
				+ LibApp.getCurrentCatalogName() + "') and "
				+ KEY_SHOWLOCATIONNAME + " = '" + locationName + "'", null,
				null, null, KEY_SHOWLOCATIONNAME, null);
		Location location = null;
		if (mCursor != null) {
			mCursor.moveToFirst();
			if (!mCursor.isAfterLast()) {
				location = new Location(
						mCursor.getString(KEY_CATNAME_CURSORLOC),
						mCursor.getString(KEY_CATCODE_CURSORLOC),
						mCursor.getString(KEY_LOCATIONID_CURSORLOC),
						mCursor.getString(KEY_SHOWLOCATIONNAME_CURSORLOC));
			}
		}
		mCursor.close();
		Log.d(TAG, "getLocationForCatalogAndName found[" + location + "]");

		return location;

	}

	public Cursor fetchLocationsCursorForPersonal() throws SQLException {
		return fetchLocationsCursorForCatalog(Catalog.PERSONAL
				+ LibApp.getCurrentCatalogName());
	}

	public Cursor fetchLocationsCursorForCurrentCatalog() throws SQLException {
		return fetchLocationsCursorForCatalog(LibApp.getCurrentCatalogName());
	}

	public Cursor fetchLocationsCursorForCatalog(String catalog)
			throws SQLException {
		Log.d(TAG, catalog);
		Cursor mCursor = null;

		if (catalog == null || catalog.length() == 0) {
			mCursor = mDb.query(SQLITE_TABLE, new String[] { KEY_ROWID,
					KEY_CATCODE, KEY_LOCATIONID, KEY_CATNAME,
					KEY_SHOWLOCATIONNAME }, null, null, null, KEY_LOCATIONID,
					null);

		} else {
			mCursor = mDb.query(true, SQLITE_TABLE, new String[] { KEY_ROWID,
					KEY_CATCODE, KEY_LOCATIONID, KEY_CATNAME,
					KEY_SHOWLOCATIONNAME },
					KEY_CATNAME + "= '" + catalog + "'", null, null, null,
					KEY_LOCATIONID, null);
		}

		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;

	}

	public boolean columnExists(String columnName) {
		Cursor mCursor = mDb.query(SQLITE_TABLE, null, null, null, null, null,
				null);
		if (mCursor != null) {
			Log.d(TAG, "columnExists: found [" + mCursor.getCount() + "]");
			mCursor.moveToFirst();
		}
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

	public Cursor fetchAllCursor() {
		Log.d(TAG, "fetchAllCursor");
		Cursor mCursor = mDb.query(true, SQLITE_TABLE, ALL, "1=1", null, null,
				null, KEY_LOCATIONID, null);

		if (mCursor != null) {
			Log.d(TAG, "fetchAllCursor: found [" + mCursor.getCount() + "]");
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	public static void logAllDatabaseContent(Context ctx) {
		LocationDbHelper me = LocationDbHelper.getInstance(ctx);
		Cursor cursor = me.fetchAllCursor();
		if (cursor != null) {
			cursor.moveToFirst();
			Log.d(TAG, "logAllDatabaseContent - Locations");
			while (!cursor.isAfterLast()) {
				Location location = new Location(
						cursor.getString(KEY_CATNAME_CURSORLOC),
						cursor.getString(KEY_CATCODE_CURSORLOC),
						cursor.getString(KEY_LOCATIONID_CURSORLOC),
						cursor.getString(KEY_SHOWLOCATIONNAME_CURSORLOC));
				Log.d(TAG,
						"logAllDatabaseContent - Locations: "
								+ location.toString());
				cursor.moveToNext();
			}
		} else {
			Log.d(TAG, "logAllDatabaseContent - Locations: nothing to log");
		}
		cursor.close();
	}

	public boolean personalLocationAlreadyExists(String locationId) {
		boolean result = false;
		Cursor cursor = fetchPersonalLocation(locationId);
		if (cursor != null && cursor.getCount() > 0) {
			result = true;
		}
		cursor.close();
		return result;
	}
}
