package nl.imarinelife.lib.divinglog.db.res;

import java.util.HashMap;

import nl.imarinelife.lib.DbHelper;
import nl.imarinelife.lib.LibApp;
import nl.imarinelife.lib.utility.SerializableSparseArray;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ProfilePartDbHelper implements DbHelper {
	private static ProfilePartDbHelper me = null;
	private static boolean openedOnceAlready = false;

	public static final String KEY_ROWID = "_id";
	public static final String KEY_CATNAME = "cat_name";
	public static final String KEY_STAYID = "description";
	public static final String KEY_SHOWNAME = "showName";
	public static final String KEY_ADD = "sumup";
	public static final String KEY_ORDERNR = "ordernr";
	public static final int KEY_ROWID_CURSORLOC = 0;
	public static final int KEY_CATNAME_CURSORLOC = 1;
	public static final int KEY_STAYID_CURSORLOC = 2;
	public static final int KEY_ADD_CURSORLOC = 3;
	public static final int KEY_ORDERNR_CURSORLOC = 4;
	public static final int KEY_SHOWNAME_CURSORLOC = 5;

	private static final String TAG = "ProfilePartDbHelper";

	public enum AddType {
		ADD("a"), NON_ADD("n");
		private final String name;

		AddType(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	private DatabaseHelper mDbHelper;
	private static SQLiteDatabase mDb;

	private static final String DATABASE_NAME = "ProfileParts";
	private static final int DATABASE_VERSION = 2;
	private static final int DATAVERSION = 2;
	private static final String SQLITE_TABLE = "ProfileParts";

	private static final String[] ALL = { KEY_ROWID, KEY_CATNAME, KEY_STAYID,
			KEY_ADD, KEY_ORDERNR, KEY_SHOWNAME };

	private static final String DATABASE_CREATE = "CREATE TABLE if not exists "
			+ SQLITE_TABLE + " (" + KEY_ROWID
			+ " integer PRIMARY KEY autoincrement," + KEY_CATNAME + ","
			+ KEY_STAYID + "," + KEY_SHOWNAME + "," + KEY_ADD + ","
			+ KEY_ORDERNR + "," + " UNIQUE (" + KEY_CATNAME + "," + KEY_STAYID
			+ "));";

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
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if (newVersion > oldVersion) {
				Log.d(TAG, "Upgrading database from appversion " + oldVersion
						+ " to " + newVersion);
				mDb = db;
				boolean somethingChanged = false;

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
			openedOnceAlready = true;
			super.onOpen(db);
		}

		private boolean handleUpgradeFrom_001_to_002(SQLiteDatabase db) {
			boolean changeSucceeded = false;
			if (me.columnExists(KEY_SHOWNAME)) {
				changeSucceeded = true;
			} else {

				try {
					db.beginTransaction();
					db.execSQL("ALTER TABLE " + SQLITE_TABLE + " ADD COLUMN "
							+ KEY_SHOWNAME);
					db.setTransactionSuccessful();
					changeSucceeded = true;
				} catch (SQLException e) {
					Log.e(TAG,
							"adding fields for profileparts version 1 to 2 failed",
							e);
				} finally {
					db.endTransaction();
				}
			}
			Log.d(TAG,
					"adding fields for profileparts version 1 to 2 finished succesfully["
							+ changeSucceeded + "]");

			return changeSucceeded;
		}

	}

	@SuppressWarnings("deprecation")
	public static ProfilePartDbHelper getInstance(Context ctx) {
		if (me == null) {
			me = new ProfilePartDbHelper();
		}
		if (me.mDb == null || !me.mDb.isOpen()) {
			me.open(ctx);
		}
		while (me.mDb.isDbLockedByCurrentThread()
				|| me.mDb.isDbLockedByOtherThreads()) {
		}
		LibApp.getInstance().dbhelpers.put("ProfilePartDbHelper", me);
		return me;
	}

	private ProfilePartDbHelper() {
	}

	public ProfilePartDbHelper open(Context mCtx) throws SQLException {
		Log.d(TAG, "Opening db - onCreate should get called if necessary");

		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void finalize() throws Throwable {
		close();
		super.finalize();
	}

	public void close() {
		Log.d(TAG, "closing db");
		if (mDbHelper != null) {
			mDbHelper.close();
		}
		if (mDb != null) {
			mDb.close();
		}

	}

	public long insertProfileParts(
			SerializableSparseArray<ProfilePart> profileParts) {
		long result = 0;
		deleteAllProfilePartsForCurrentCatalog();
		mDb.beginTransaction();
		try {
			for (int i = 0; i < profileParts.size(); i++) {
				ProfilePart part = profileParts.get(profileParts.keyAt(i));
				result += insertProfilePart(LibApp.getCurrentCatalogName(),
						part.stayId, part.showName, part.addForTotalDiveTime,
						part.orderNumber);
				Log.d(TAG, "part: " + part);
			}
			Log.d(TAG, "parts stored " + result);
			mDb.setTransactionSuccessful();
		} finally {
			mDb.endTransaction();
		}

		return result;

	}

	private long insertProfilePart(String catname, String descr,
			String showName, boolean add, int orderNumber) {
		long result = 0;

		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_CATNAME, catname);
		initialValues.put(KEY_STAYID, descr);
		initialValues.put(KEY_SHOWNAME, showName);
		initialValues.put(KEY_ADD, String.valueOf(add));
		initialValues.put(KEY_ORDERNR, orderNumber);

		if (mDb.insert(SQLITE_TABLE, null, initialValues) != -1) {
			result++;
		}

		return result;
	}

	public boolean deleteAllProfilePartsForCurrentCatalog() {
		int doneDelete = 0;

		mDb.beginTransaction();
		try {
			doneDelete = mDb.delete(SQLITE_TABLE,
					KEY_CATNAME + "='" + LibApp.getCurrentCatalogName() + "'",
					null);
			mDb.setTransactionSuccessful();
		} finally {
			mDb.endTransaction();
		}

		Log.d(TAG, Integer.toString(doneDelete));
		return doneDelete > 0;
	}

	public Cursor fetchAllCursorForCurrentCatalog() {

		Cursor mCursor = mDb.query(SQLITE_TABLE, ALL, KEY_CATNAME + "='"
				+ LibApp.getCurrentCatalogName() + "'", null, null, null,
				KEY_ROWID);
		Log.d(TAG, "fetchAllCursorForCurrentCatalog: cursor count["
				+ (mCursor != null ? mCursor.getCount() : "null") + "]");
		return mCursor;
	}

	public Cursor fetchAllCursor() {

		Cursor mCursor = mDb.query(SQLITE_TABLE, ALL, null, null, null, null,
				KEY_ROWID);
		Log.d(TAG,
				"fetchAllCursor: cursor count["
						+ (mCursor != null ? mCursor.getCount() : "null") + "]");
		return mCursor;
	}

	public static void logAllDatabaseContent(Context ctx) {
		ProfilePartDbHelper me = ProfilePartDbHelper.getInstance(ctx);
		Cursor cursor = me.fetchAllCursor();
		boolean something = false;
		if (cursor != null) {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				ProfilePart part = new ProfilePart(
						cursor.getString(KEY_CATNAME_CURSORLOC),
						cursor.getString(KEY_STAYID_CURSORLOC),
						cursor.getString(KEY_SHOWNAME_CURSORLOC),
						Boolean.valueOf(cursor.getString(KEY_ADD_CURSORLOC)),
						cursor.getInt(KEY_ORDERNR_CURSORLOC));
				something = true;
				Log.d(TAG,
						"logAllDatabaseContent ProfileParts " + part.toString());
				cursor.moveToNext();
			}
		}
		if (!something) {
			Log.d(TAG, "logAllDatabaseContent - nothing to log");
		}
		cursor.close();
	}

	public String getShowNameForCatalogAndStayId(String catalog, String stayId) {
		getInstance(LibApp.getContext()); // apparentely it may have been closed
		String toReturn = null;
		Cursor cursor = mDb.query(SQLITE_TABLE, ALL, KEY_CATNAME + "='"
				+ catalog + "' and " + KEY_STAYID + " = '" + stayId + "'",
				null, null, null, KEY_ROWID);
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			toReturn = cursor != null ? cursor
					.getString(KEY_SHOWNAME_CURSORLOC) : stayId;
		}
		cursor.close();
		return toReturn;
	}

	public HashMap<ProfilePartDbHelper.AddType, SerializableSparseArray<ProfilePart>> fetchAll() {
		HashMap<ProfilePartDbHelper.AddType, SerializableSparseArray<ProfilePart>> parts = null;
		Cursor mCursor = fetchAllCursorForCurrentCatalog();
		Log.d(TAG, "cursorsize: " + mCursor.getCount());
		parts = new HashMap<ProfilePartDbHelper.AddType, SerializableSparseArray<ProfilePart>>();
		SerializableSparseArray<ProfilePart> addParts = new SerializableSparseArray<ProfilePart>();
		SerializableSparseArray<ProfilePart> nonAddParts = new SerializableSparseArray<ProfilePart>();
		while (mCursor.moveToNext()) {
			if (Boolean.valueOf(mCursor.getString(KEY_ADD_CURSORLOC))) {
				addParts.put(
						mCursor.getInt(KEY_ORDERNR_CURSORLOC),
						new ProfilePart(mCursor
								.getString(KEY_CATNAME_CURSORLOC), mCursor
								.getString(KEY_STAYID_CURSORLOC), mCursor
								.getString(KEY_SHOWNAME_CURSORLOC), Boolean
								.valueOf(mCursor.getString(KEY_ADD_CURSORLOC)),
								mCursor.getInt(KEY_ORDERNR_CURSORLOC)));
			} else {
				nonAddParts.put(
						mCursor.getInt(KEY_ORDERNR_CURSORLOC),
						new ProfilePart(mCursor
								.getString(KEY_CATNAME_CURSORLOC), mCursor
								.getString(KEY_STAYID_CURSORLOC), mCursor
								.getString(KEY_SHOWNAME_CURSORLOC), Boolean
								.valueOf(mCursor.getString(KEY_ADD_CURSORLOC)),
								mCursor.getInt(KEY_ORDERNR_CURSORLOC)));
			}
		}
		Log.d(TAG, "addlist: " + addParts.size());
		Log.d(TAG, "nonaddlist: " + nonAddParts.size());
		parts.put(AddType.ADD, addParts);
		parts.put(AddType.NON_ADD, nonAddParts);
		mCursor.close();
		return parts;
	}

	public boolean columnExists(String columnName) {
		Cursor mCursor = mDb.query(SQLITE_TABLE, null, null, null, null, null,
				null);
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
