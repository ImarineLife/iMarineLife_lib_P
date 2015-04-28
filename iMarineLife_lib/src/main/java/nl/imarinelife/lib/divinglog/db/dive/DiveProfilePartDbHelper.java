package nl.imarinelife.lib.divinglog.db.dive;

import java.util.HashMap;

import nl.imarinelife.lib.DbHelper;
import nl.imarinelife.lib.LibApp;
import nl.imarinelife.lib.MainActivity;
import nl.imarinelife.lib.R;
import nl.imarinelife.lib.divinglog.db.res.ProfilePart;
import nl.imarinelife.lib.divinglog.db.res.ProfilePartDbHelper;
import nl.imarinelife.lib.utility.SDCardSQLiteOpenHelper;
import nl.imarinelife.lib.utility.SerializableSparseArray;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

public class DiveProfilePartDbHelper implements DbHelper {
	private static DiveProfilePartDbHelper	me						= null;

	public static final String				KEY_ROWID				= "_id";
	public static final String				KEY_DIVENR				= "divenr";
	public static final String				KEY_CATNAME				= "cat_name";
	public static final String				KEY_STAYID				= "description";
	public static final String				KEY_ADD					= "sumup";
	public static final String				KEY_STAYVALUE			= "value";
	public static final String				KEY_ORDERNR				= "ordernr";

	public static final int					KEY_ROWID_CURSORLOC		= 0;
	public static final int					KEY_DIVENR_CURSORLOC	= 1;
	public static final int					KEY_CATNAME_CURSORLOC	= 2;
	public static final int					KEY_STAYID_CURSORLOC	= 3;
	public static final int					KEY_ADD_CURSORLOC		= 4;
	public static final int					KEY_STAYVALUE_CURSORLOC	= 5;
	public static final int					KEY_ORDERNR_CURSORLOC	= 6;

	private static final String				TAG						= "DiveProfilePartDbHelper";

	private SDCardDatabaseHelper			dbHelperExternal;
	private SQLiteDatabase					dbExternal;

	private static final String				DATABASE_NAME			= "DiveProfileParts";
	private static final int				DATABASE_VERSION		= 1;
	private static final String				SQLITE_TABLE			= "DiveProfileParts";

	private static final String				DATABASE_CREATE			= "CREATE TABLE if not exists " + SQLITE_TABLE
																			+ " (" + KEY_ROWID
																			+ " integer PRIMARY KEY autoincrement,"
																			+ KEY_DIVENR + "," + KEY_CATNAME + ","
																			+ KEY_STAYID + "," + KEY_ADD + ","
																			+ KEY_STAYVALUE + "," + KEY_ORDERNR + ","
																			+ " UNIQUE (" + KEY_CATNAME + ","
																			+ KEY_DIVENR + "," + KEY_STAYID + "));";

	private static class SDCardDatabaseHelper extends SDCardSQLiteOpenHelper {

		public SDCardDatabaseHelper(Context context, String dir) {
			super(context, dir, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d(TAG,
				DATABASE_CREATE);
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
		// new list of data can be triggered by deleting all entries for that
		// catalog here, it will be filled from CurrentCatalog
		// don't forget to update the android:versionCode in the manifest with
		// every app distribution
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.d(TAG,
				"Upgrading database from appversion " + oldVersion + " to " + newVersion);
			switch (oldVersion) {
			/*
			 * case 1: handleUpgradeForVersion_001();
			 */}
			//onCreate(db);
		}

		/*
		 * private void handleUpgradeForVersion_001() { }
		 */
	}

	@SuppressWarnings("deprecation")
	public static DiveProfilePartDbHelper getInstance(Context ctx) {
		if (me == null) {
			me = new DiveProfilePartDbHelper();
		}
		Boolean isSDPresent = SDCardSQLiteOpenHelper.isSDCardiMarineLifeDirectoryActive();

		while ((me.dbExternal != null && (me.dbExternal.isDbLockedByCurrentThread() || me.dbExternal.isDbLockedByOtherThreads()))) {
		}
		if (isSDPresent && (me.dbExternal == null || !me.dbExternal.isOpen())) {
			me.open(ctx);
		}

		LibApp.getInstance().dbhelpers.put("DiveProfilePartDbHelper",
			me);
		return me;
	}

	private DiveProfilePartDbHelper() {

	}

	public DiveProfilePartDbHelper open(Context mCtx) throws SQLException {
		Boolean isSDPresent = SDCardSQLiteOpenHelper.isSDCardiMarineLifeDirectoryActive();
		if (!isSDPresent && MainActivity.me!=null) {
			Toast.makeText(MainActivity.me,
				LibApp.getCurrentResources().getString(R.string.externalMemoryNeeded),
				Toast.LENGTH_LONG).show();
			MainActivity.me.finish(); // ??
		}

		Log.d(TAG,
			"Opening db - onCreate should get called if necessary - SDPresent[" + isSDPresent + "]");
		if (isSDPresent) {
			dbHelperExternal = new SDCardDatabaseHelper(mCtx, SDCardSQLiteOpenHelper.getDataBasesDirectory());
			dbExternal = dbHelperExternal.getWritableDatabase();
		}
		return this;
	}

	public void finalize() throws Throwable {
		close();
		super.finalize();
	}

	public void close() {
		Log.d(TAG,
			"closing db");

		if (dbHelperExternal != null) {
			dbHelperExternal.close();
			dbHelperExternal = null;
		}
		if (dbExternal != null) {
			dbExternal.close();
			dbExternal = null;
		}

	}

	public long updateProfilePartsWithNewDiveNr(int diveNrAtStart, int diveNr) {
		Log.d(TAG,
			"updateProfilePartsWithNewDiveNr [" + diveNrAtStart + "," + diveNr + "]");
		long result = 0;
		if (dbExternal != null) {
			ContentValues initialValues = new ContentValues();
			initialValues.put(KEY_DIVENR,
				diveNr);

			result += dbExternal.update(SQLITE_TABLE,
				initialValues,
				KEY_DIVENR + "=" + diveNrAtStart,
				null);
		}
		return result;

	}

	public long upsertProfileParts(
			HashMap<ProfilePartDbHelper.AddType, SerializableSparseArray<DiveProfilePart>> profileParts) {
		long result = 0;
		if (profileParts != null) {
			dbExternal.beginTransaction();
			try {
				boolean first = true;
				for (SerializableSparseArray<DiveProfilePart> addOrNot : profileParts.values()) {
					for (int i = 0; i < addOrNot.size(); i++) {
						DiveProfilePart part = addOrNot.get(addOrNot.keyAt(i));
						if (first) {
							Cursor cursor = fetchAllCursorForDive(part.diveNr);
							if (cursor != null && cursor.getCount() > 0) {
								deleteProfilePartsForDive(part.diveNr,
									part.profilePart.catName);
								Log.d(TAG,
									"upsertProfileParts deleting them []");
							}
							first = false;
							cursor.close();
						}

						result += upsertProfilePart(part.diveNr,
							LibApp.getCurrentCatalogName(),
							part.profilePart.stayId,
							part.profilePart.addForTotalDiveTime,
							part.stayValueInMeters,
							part.profilePart.orderNumber);
					}
				}
				dbExternal.setTransactionSuccessful();
			} finally {
				dbExternal.endTransaction();

			}
		}
		return result;

	}

	private long upsertProfilePart(int divenr, String catname, String descr, boolean add, int stayValue, int ordernr) {

		Log.d(TAG,
			"inserting [" + divenr + "," + catname + "," + descr + "," + add + "," + stayValue + "]");
		// called within a transaction already
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_DIVENR,
			divenr);
		initialValues.put(KEY_CATNAME,
			catname);
		initialValues.put(KEY_STAYID,
			descr);
		initialValues.put(KEY_ADD,
			String.valueOf(add));
		initialValues.put(KEY_STAYVALUE,
			stayValue);
		initialValues.put(KEY_ORDERNR,
			ordernr);

		return dbExternal.insert(SQLITE_TABLE,
			null,
			initialValues);
	}

	public boolean deleteAllProfileParts() {
		int doneDelete = 0;

		dbExternal.beginTransaction();
		try {
			String whereClause = KEY_CATNAME + " = '" + LibApp.getCurrentCatalogName() + "'";
			doneDelete += dbExternal.delete(SQLITE_TABLE,
				whereClause,
				null);
			dbExternal.setTransactionSuccessful();
		} finally {
			dbExternal.endTransaction();

		}
		Log.d(TAG,
			Integer.toString(doneDelete));
		return doneDelete > 0;
	}

	public boolean deleteProfilePartsForDive(int divenr, String catalog) {
		int doneDelete = 0;

		dbExternal.beginTransaction();
		try {
			doneDelete += dbExternal.delete(SQLITE_TABLE,
				KEY_DIVENR + " = " + divenr + " and " + KEY_CATNAME + " = '" + catalog + "'",
				null);
			dbExternal.setTransactionSuccessful();
		} finally {
			dbExternal.endTransaction();

		}
		Log.d(TAG,
			Integer.toString(doneDelete));
		return doneDelete > 0;
	}

	public Cursor fetchAllCursor() {

		Cursor mCursor = dbExternal.query(SQLITE_TABLE,
			new String[] { KEY_ROWID, KEY_DIVENR, KEY_CATNAME, KEY_STAYID, KEY_ADD, KEY_STAYVALUE, KEY_ORDERNR },
			null,
			null,
			null,
			null,
			KEY_ROWID);

		return mCursor;
	}

	public Cursor fetchAllCursorForDive(int divenr) {

		Cursor mCursor = dbExternal.query(SQLITE_TABLE,
			new String[] { KEY_ROWID, KEY_DIVENR, KEY_CATNAME, KEY_STAYID, KEY_ADD, KEY_STAYVALUE, KEY_ORDERNR },
			KEY_DIVENR + " = " + divenr,
			null,
			null,
			null,
			KEY_ROWID);

		return mCursor;
	}

	public HashMap<ProfilePartDbHelper.AddType, SerializableSparseArray<DiveProfilePart>> fetchAll(Context ctx) {
		Cursor mCursor = fetchAllCursor();
		HashMap<ProfilePartDbHelper.AddType, SerializableSparseArray<DiveProfilePart>> toReturn = fetchAll(mCursor, ctx);
		mCursor.close();
		return toReturn;
	}

	public HashMap<ProfilePartDbHelper.AddType, SerializableSparseArray<DiveProfilePart>> fetchAll(int divenr, Context ctx) {
		Cursor mCursor = fetchAllCursorForDive(divenr);
		Log.d(TAG,
			"fetchAll(forDive) [" + (mCursor != null
					? mCursor.getCount()
					: "null") + "]");
		HashMap<ProfilePartDbHelper.AddType, SerializableSparseArray<DiveProfilePart>> toReturn = fetchAll(mCursor, ctx );
		mCursor.close();
		return toReturn;
	}

	public HashMap<ProfilePartDbHelper.AddType, SerializableSparseArray<DiveProfilePart>> fetchAll(Cursor mCursor, Context ctx) {
		Log.d(TAG,
			"fetchAllFromCursor count[" + (mCursor != null
					? mCursor.getCount()
					: "null") + "]");
		HashMap<ProfilePartDbHelper.AddType, SerializableSparseArray<DiveProfilePart>> parts = new HashMap<ProfilePartDbHelper.AddType, SerializableSparseArray<DiveProfilePart>>();

		SerializableSparseArray<DiveProfilePart> addParts = new SerializableSparseArray<DiveProfilePart>();
		SerializableSparseArray<DiveProfilePart> nonAddParts = new SerializableSparseArray<DiveProfilePart>();
		
		if (mCursor != null && mCursor.getCount() > 0) {
			mCursor.moveToFirst();
			ProfilePartDbHelper pphelper = ProfilePartDbHelper.getInstance(ctx);
			do {
				String catalog = mCursor.getString(KEY_CATNAME_CURSORLOC);
				String stayId = mCursor.getString(KEY_STAYID_CURSORLOC); 
				String showName = pphelper.getShowNameForCatalogAndStayId(catalog, stayId);
				
				ProfilePart profilePart = new ProfilePart(catalog,
						stayId, showName, 
						Boolean.valueOf(mCursor.getString(KEY_ADD_CURSORLOC)), mCursor.getInt(KEY_ORDERNR_CURSORLOC));
				DiveProfilePart diveProfilePart = new DiveProfilePart(mCursor.getInt(KEY_DIVENR_CURSORLOC),
						mCursor.getInt(KEY_STAYVALUE_CURSORLOC), profilePart);
				if (Boolean.valueOf(mCursor.getString(KEY_ADD_CURSORLOC))) {
					addParts.put(diveProfilePart.profilePart.orderNumber,
						diveProfilePart);
				} else {
					nonAddParts.put(diveProfilePart.profilePart.orderNumber,
						diveProfilePart);
				}
			} while (mCursor.moveToNext());
		}
		Log.d(TAG,
			"fetchAllFromCursor partcount[" + (addParts.size() + nonAddParts.size()) + "]");

		parts.put(ProfilePartDbHelper.AddType.ADD,
			addParts);
		parts.put(ProfilePartDbHelper.AddType.NON_ADD,
			nonAddParts);
		return parts;
	}

}
