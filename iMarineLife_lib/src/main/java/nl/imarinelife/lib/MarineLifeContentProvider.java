package nl.imarinelife.lib;

import java.util.HashMap;
import java.util.Map;

import nl.imarinelife.lib.divinglog.db.dive.DiveDbHelper;
import nl.imarinelife.lib.fieldguide.db.FieldGuideAndSightingsEntryDbHelper;
import nl.imarinelife.lib.utility.FilterCursorWrapper;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class MarineLifeContentProvider extends ContentProvider {
	private static final String TAG = "MLContentProvider";
	public static final String SEPARATOR = "--";

	public static String authorityUsedOnThisDevice = null;

	private static final String M = "vnd.android.cursor.dir";
	private static final String S = "vnd.android.cursor.item";

	public static final String NAME_DIVE = "dive";
	public static final String NAME_DIVE_FILTERED = "dive_filter";

	public static final String NAME_FIELDGUIDE = "fieldguide";
	public static final String NAME_FIELDGUIDE_FILTERED = "fieldguide_filter";

	public static final String NAME_SIGHTING = "sighting";

	public static final String NAME_SIGHTINGS_ASIS = "sightings_asis";
	public static final String NAME_SIGHTINGS_ASIS_FILTERED = "sightings_asis_filter";

	public static final String NAME_SIGHTINGS_FLESHEDOUT = "sightings_fleshedout";
	public static final String NAME_SIGHTINGS_FLESHEDOUT_FILTERED = "sightings_fleshedout_filter";

	private static final int ALL_DIVES = 1;
	private static final int DIVE_FOR_ID = 2;
	private static final int DIVES_FILTERED = 3;

	private static final int FIELDGUIDE = 4;
	private static final int FIELDGUIDE_ID = 5;
	private static final int FIELDGUIDE_FILTERED = 6;

	private static final int ALL_SIGHTINGS = 7;
	private static final int SIGHTING_ID = 8;

	private static final int SIGHTINGS_ASIS_FOR_DIVE = 9;
	private static final int SIGHTINGS_ASIS_FOR_DIVE_FILTERED = 10;

	private static final int SIGHTINGS_FLESHEDOUT_FOR_DIVE = 11;
	private static final int SIGHTINGS_FLESHEDOUT_FOR_DIVE_AND_FGID = 12;
	private static final int SIGHTINGS_FLESHEDOUT_FOR_DIVE_FILTERED = 13;

	private static final UriMatcher sUriMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);

	public static void initialize() {
		sUriMatcher.addURI(getAuthority(), NAME_DIVE, ALL_DIVES);
		sUriMatcher.addURI(getAuthority(), NAME_DIVE_FILTERED + "/*",
				DIVES_FILTERED);
		sUriMatcher.addURI(getAuthority(), NAME_DIVE + "/#", DIVE_FOR_ID);

		sUriMatcher.addURI(getAuthority(), NAME_FIELDGUIDE, FIELDGUIDE);
		sUriMatcher.addURI(getAuthority(), NAME_FIELDGUIDE_FILTERED + "/*",
				FIELDGUIDE_FILTERED);
		sUriMatcher.addURI(getAuthority(), NAME_FIELDGUIDE + "/#",
				FIELDGUIDE_ID);

		sUriMatcher.addURI(getAuthority(), NAME_SIGHTING, ALL_SIGHTINGS);
		sUriMatcher.addURI(getAuthority(), NAME_SIGHTING + "/#", SIGHTING_ID);

		sUriMatcher.addURI(getAuthority(), NAME_SIGHTINGS_ASIS + "/#",
				SIGHTINGS_ASIS_FOR_DIVE);
		sUriMatcher.addURI(getAuthority(), NAME_SIGHTINGS_ASIS_FILTERED + "/*",
				SIGHTINGS_ASIS_FOR_DIVE_FILTERED);

		sUriMatcher.addURI(getAuthority(), NAME_SIGHTINGS_FLESHEDOUT + "/#",
				SIGHTINGS_FLESHEDOUT_FOR_DIVE);
		sUriMatcher.addURI(getAuthority(), NAME_SIGHTINGS_FLESHEDOUT + "/*",
				SIGHTINGS_FLESHEDOUT_FOR_DIVE_AND_FGID);
		sUriMatcher.addURI(getAuthority(), NAME_SIGHTINGS_FLESHEDOUT_FILTERED
				+ "/*", SIGHTINGS_FLESHEDOUT_FOR_DIVE_FILTERED);

		Log.d(TAG, sUriMatcher + " authority: [" + getAuthority() + "]");
	}

	@Override
	public boolean onCreate() {
		return false;
	}

	@Override
	public String getType(Uri url) {
		Log.d(TAG, "url to match: " + url);
		int match = sUriMatcher.match(url);
		Log.d(TAG, "match found: " + match);
		switch (match) {
		case ALL_DIVES:
			return M + "/" + getAuthority() + "." + NAME_DIVE;
		case DIVE_FOR_ID:
			return S + "/" + getAuthority() + "." + NAME_DIVE;
		case DIVES_FILTERED:
			return M + "/" + getAuthority() + "." + NAME_DIVE_FILTERED;
		case FIELDGUIDE:
			return M + "/" + getAuthority() + "." + NAME_FIELDGUIDE;
		case FIELDGUIDE_ID:
			return S + "/" + getAuthority() + "." + NAME_FIELDGUIDE;
		case FIELDGUIDE_FILTERED:
			return M + "/" + getAuthority() + "." + NAME_FIELDGUIDE_FILTERED;
		case ALL_SIGHTINGS:
			return M + "/" + getAuthority() + "." + NAME_SIGHTING;
		case SIGHTING_ID:
			return S + "/" + getAuthority() + "." + NAME_SIGHTING;
		case SIGHTINGS_ASIS_FOR_DIVE:
			return M + "/" + getAuthority() + "." + NAME_SIGHTINGS_ASIS;
		case SIGHTINGS_ASIS_FOR_DIVE_FILTERED:
			return M + "/" + getAuthority() + "."
					+ NAME_SIGHTINGS_ASIS_FILTERED;
		case SIGHTINGS_FLESHEDOUT_FOR_DIVE:
			return M + "/" + getAuthority() + "." + NAME_SIGHTINGS_FLESHEDOUT;
		case SIGHTINGS_FLESHEDOUT_FOR_DIVE_FILTERED:
			return M + "/" + getAuthority() + "."
					+ NAME_SIGHTINGS_FLESHEDOUT_FILTERED;

		}
		return null;
	}

	public Cursor query(Uri url) {
		return query(url, null, null, null, null);
	}

	@Override
	public Cursor query(Uri url, String[] columns, String selection,
			String[] selArgs, String orderBy) {
		int match = sUriMatcher.match(url);
		Log.d(TAG, "query [" + url.getPath() + "] match[" + match + "]");
		DiveDbHelper divehelper = null;
		FieldGuideAndSightingsEntryDbHelper fghelper = null;
		Cursor cursor;
		String constraint;
		int diveNr;
		int fieldguideId;
		Cursor toReturn = null;
		switch (match) {
		case ALL_DIVES:
			Log.d(TAG, "ALL_DIVES");
			divehelper = DiveDbHelper.getInstance(getContext());
			toReturn = divehelper.query(columns, selection, selArgs, orderBy,
					getContext());
			break;
		case DIVE_FOR_ID:
			Log.d(TAG, "DIVE_FOR_ID [" + url.getPath() + "]");
			divehelper = DiveDbHelper.getInstance(getContext());
			selection = DiveDbHelper.KEY_ROWID + " = "
					+ ContentUris.parseId(url);
			toReturn = divehelper.query(columns, selection, selArgs, orderBy,
					getContext());
			break;
		case DIVES_FILTERED:
			Log.d(TAG, "DIVES_FILTERED [" + url.getPath() + "]");
			divehelper = DiveDbHelper.getInstance(getContext());
			cursor = divehelper.query(columns, selection, selArgs, orderBy,
					getContext());
			constraint = url.getLastPathSegment();
			int[] columnsToSearch = { DiveDbHelper.KEY_LOCATIONNAME_CURSORLOC };
			Map<Integer, Map<String, Integer>> columnsToLocalize = new HashMap<Integer, Map<String, Integer>>();
			columnsToLocalize
					.put(DiveDbHelper.KEY_LOCATIONNAME_CURSORLOC,
							LibApp.getInstance().getCurrentCatalog()
									.getLocationNamesMapping());
			toReturn = new FilterCursorWrapper(cursor, constraint,
					columnsToSearch, columnsToLocalize, DiveDbHelper.CODE_TO_SHOWVALUE_COLUMNMAPPING);
			break;
		case FIELDGUIDE:
			Log.d(TAG, "FIELDGUIDE [" + url.getPath() + "]");
			fghelper = FieldGuideAndSightingsEntryDbHelper
					.getInstance(getContext());
			cursor = fghelper.queryFieldGuide(columns, selection, selArgs,
					orderBy);
			toReturn = new FilterCursorWrapper(cursor,
					Preferences.FIELDGUIDE_GROUPS_HIDDEN,
					FieldGuideAndSightingsEntryDbHelper.KEY_GROUPNAME_CURSORLOC);
			break;
		case FIELDGUIDE_ID:
			Log.d(TAG, "FIELDGUIDE_ID [" + url.getPath() + "]");
			fghelper = FieldGuideAndSightingsEntryDbHelper
					.getInstance(getContext());
			selection = FieldGuideAndSightingsEntryDbHelper.KEY_ROWID + " = "
					+ ContentUris.parseId(url);
			cursor = fghelper.queryFieldGuide(columns, selection, selArgs,
					orderBy);
			cursor.moveToFirst();
			Log.d(TAG, "query result ["
					+ (cursor != null ? cursor.getCount() : null) + "]");
			toReturn = cursor;
			break;
		case FIELDGUIDE_FILTERED:
			constraint = url.getLastPathSegment();
			Log.d(TAG, "FIELDGUIDE_FILTERED constraint [" + url.getPath()
					+ "][" + constraint + "]");
			fghelper = FieldGuideAndSightingsEntryDbHelper
					.getInstance(getContext());
			cursor = fghelper.queryFieldGuide(columns, selection, selArgs,
					orderBy);
			int[] columnsToSearch1 = {
					FieldGuideAndSightingsEntryDbHelper.KEY_COMMONNAME_CURSORLOC,
					FieldGuideAndSightingsEntryDbHelper.KEY_LATINNAME_CURSORLOC };
			Map<Integer, Map<String, Integer>> columnsToLocalize1 = new HashMap<Integer, Map<String, Integer>>();
			columnsToLocalize1
					.put(FieldGuideAndSightingsEntryDbHelper.KEY_COMMONNAME_CURSORLOC,
							LibApp.getInstance().getCurrentCatalog()
									.getCommonIdMapping());
			toReturn = new FilterCursorWrapper(
					cursor,
					constraint,
					Preferences.FIELDGUIDE_GROUPS_HIDDEN,
					FieldGuideAndSightingsEntryDbHelper.KEY_GROUPNAME_CURSORLOC,
					columnsToSearch1, columnsToLocalize1, 
					FieldGuideAndSightingsEntryDbHelper.CODE_TO_SHOWVALUE_COLUMNMAPPING);
			break;
		case ALL_SIGHTINGS:
			Log.d(TAG, "ALL_SIGHTINGS [" + url.getPath() + "]");
			fghelper = FieldGuideAndSightingsEntryDbHelper
					.getInstance(getContext());
			cursor = fghelper.querySightings(FieldGuideAndSightingsEntryDbHelper.ALL, selection, selArgs,
					orderBy);
			toReturn = new FilterCursorWrapper(cursor,
					Preferences.FIELDGUIDE_GROUPS_HIDDEN,
					FieldGuideAndSightingsEntryDbHelper.KEY_GROUPNAME_CURSORLOC);
			break;
		case SIGHTING_ID:
			Log.d(TAG, "SIGHTING_ID [" + url.getPath() + "]");
			fghelper = FieldGuideAndSightingsEntryDbHelper
					.getInstance(getContext());
			int rownr = (int) ContentUris.parseId(url);
			cursor = fghelper.querySightingAsIsForSighting(rownr);
			if (cursor != null) {
				cursor.moveToFirst();
			}
			Log.d(TAG, "query result ["
					+ (cursor != null ? cursor.getCount() : null) + "]");
			toReturn = cursor;
			break;
		case SIGHTINGS_ASIS_FOR_DIVE:
			Log.d(TAG, "SIGHTINGS_ASIS_FOR_DIVE [" + url.getPath() + "]");
			fghelper = FieldGuideAndSightingsEntryDbHelper
					.getInstance(getContext());
			diveNr = (int) ContentUris.parseId(url);
			selection = FieldGuideAndSightingsEntryDbHelper.KEY_DIVENR + " = "
					+ ContentUris.parseId(url);
			cursor = fghelper.querySightingAsIsForDive(diveNr,null);
			if (cursor != null) {
				cursor.moveToFirst();
			}
			Log.d(TAG, "query result ["
					+ (cursor != null ? cursor.getCount() : null) + "]");
			toReturn = new FilterCursorWrapper(cursor,
					Preferences.SIGHTINGS_GROUPS_HIDDEN,
					FieldGuideAndSightingsEntryDbHelper.KEY_GROUPNAME_CURSORLOC);
			break;
		case SIGHTINGS_ASIS_FOR_DIVE_FILTERED:
			String[] diveAndconstraint1 = url.getLastPathSegment().split(
					SEPARATOR);
			diveNr = Integer.parseInt(diveAndconstraint1[0]);
			constraint = diveAndconstraint1[1];
			Log.d(TAG, "SIGHTINGS_ASIS_FOR_DIVE_FILTERED [" + url.getPath()
					+ "][" + constraint + "]");
			fghelper = FieldGuideAndSightingsEntryDbHelper
					.getInstance(getContext());
			cursor = fghelper.querySightingAsIsForDive(diveNr,null);

			int[] columnsToSearch_s1 = {
					FieldGuideAndSightingsEntryDbHelper.KEY_COMMONNAME_CURSORLOC,
					FieldGuideAndSightingsEntryDbHelper.KEY_LATINNAME_CURSORLOC };
			Map<Integer, Map<String, Integer>> columnsToLocalize_s1 = new HashMap<Integer, Map<String, Integer>>();
			columnsToLocalize_s1
					.put(FieldGuideAndSightingsEntryDbHelper.KEY_COMMONNAME_CURSORLOC,
							LibApp.getInstance().getCurrentCatalog()
									.getCommonIdMapping());

			toReturn = new FilterCursorWrapper(
					cursor,
					constraint,
					Preferences.SIGHTINGS_GROUPS_HIDDEN,
					FieldGuideAndSightingsEntryDbHelper.KEY_GROUPNAME_CURSORLOC,
					columnsToSearch_s1,
					columnsToLocalize_s1,
					FieldGuideAndSightingsEntryDbHelper.CODE_TO_SHOWVALUE_COLUMNMAPPING);
			break;
		case SIGHTINGS_FLESHEDOUT_FOR_DIVE:
			Log.d(TAG, "SIGHTINGS_FLESHEDOUT_FOR_DIVE [" + url.getPath() + "]");
			fghelper = FieldGuideAndSightingsEntryDbHelper
					.getInstance(getContext());
			diveNr = (int) ContentUris.parseId(url);
			selection = FieldGuideAndSightingsEntryDbHelper.KEY_DIVENR + " = "
					+ ContentUris.parseId(url);
			cursor = fghelper.queryFieldGuideFilledForDive(diveNr);
			if (cursor != null) {
				cursor.moveToFirst();
			}
			Log.d(TAG, "query result ["
					+ (cursor != null ? cursor.getCount() : null) + "]");
			toReturn = new FilterCursorWrapper(cursor,
					Preferences.SIGHTINGS_GROUPS_HIDDEN,
					FieldGuideAndSightingsEntryDbHelper.KEY_GROUPNAME_CURSORLOC);
			break;
		case SIGHTINGS_FLESHEDOUT_FOR_DIVE_AND_FGID:
			String[] diveAndfgid = url.getLastPathSegment().split(SEPARATOR);
			diveNr = Integer.parseInt(diveAndfgid[0]);
			fieldguideId = Integer.parseInt(diveAndfgid[1]);
			Log.d(TAG, "query dive [" + diveNr + "][" + fieldguideId + "]");
			fghelper = FieldGuideAndSightingsEntryDbHelper
					.getInstance(getContext());
			cursor = fghelper.querySightingAnyWayYouCan(diveNr, fieldguideId);
			if (cursor != null) {
				cursor.moveToFirst();
			}
			Log.d(TAG, "query result ["
					+ (cursor != null ? cursor.getCount() : null) + "]");
			toReturn = new FilterCursorWrapper(cursor,
					Preferences.SIGHTINGS_GROUPS_HIDDEN,
					FieldGuideAndSightingsEntryDbHelper.KEY_GROUPNAME_CURSORLOC);
			break;
		case SIGHTINGS_FLESHEDOUT_FOR_DIVE_FILTERED:
			String[] diveAndconstraint = url.getLastPathSegment().split(
					SEPARATOR);
			diveNr = Integer.parseInt(diveAndconstraint[0]);
			constraint = diveAndconstraint[1];
			Log.d(TAG, "SIGHTINGS_ASIS_FOR_DIVE_FILTERED [" + url.getPath()
					+ "][" + constraint + "]");
			fghelper = FieldGuideAndSightingsEntryDbHelper
					.getInstance(getContext());
			cursor = fghelper.queryFieldGuideFilledForDive(diveNr);

			int[] columnsToSearch_s2 = {
					FieldGuideAndSightingsEntryDbHelper.KEY_COMMONNAME_CURSORLOC,
					FieldGuideAndSightingsEntryDbHelper.KEY_LATINNAME_CURSORLOC };
			Map<Integer, Map<String, Integer>> columnsToLocalize_s2 = new HashMap<Integer, Map<String, Integer>>();
			columnsToLocalize_s2
					.put(FieldGuideAndSightingsEntryDbHelper.KEY_COMMONNAME_CURSORLOC,
							LibApp.getInstance().getCurrentCatalog()
									.getCommonIdMapping());

			toReturn = new FilterCursorWrapper(
					cursor,
					constraint,
					Preferences.SIGHTINGS_GROUPS_HIDDEN,
					FieldGuideAndSightingsEntryDbHelper.KEY_GROUPNAME_CURSORLOC,
					columnsToSearch_s2,
					columnsToLocalize_s2, 
					FieldGuideAndSightingsEntryDbHelper.CODE_TO_SHOWVALUE_COLUMNMAPPING);
		}
		return toReturn;

	}

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		return 0;
	}

	@Override
	public Uri insert(Uri arg0, ContentValues arg1) {
		return null;
	}

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		return 0;
	}

	public static String getAuthority() {
		if (authorityUsedOnThisDevice == null) {
			authorityUsedOnThisDevice = "nl.imarinelife."
					+ LibApp.getCurrentCatalogName().toLowerCase()
					+ ".provider";
			initialize();
		}
		return authorityUsedOnThisDevice;
	}

}
