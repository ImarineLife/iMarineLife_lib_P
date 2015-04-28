package nl.imarinelife.lib.divinglog.db.res;

import nl.imarinelife.lib.LibApp;
import nl.imarinelife.lib.catalog.Catalog;
import nl.imarinelife.lib.divinglog.db.dive.DiveDbHelper;
import nl.imarinelife.lib.utility.CursorProvider;
import android.content.Context;
import android.database.Cursor;

public class LocationCursorProvider implements CursorProvider {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	private Cursor				cursor				= null;

	public LocationCursorProvider(Context ctx) {
		setCursor(ctx);
	}

	private void setCursor(Context ctx) {
		if (cursor != null)
			cursor.close();
		LocationDbHelper helper = LocationDbHelper.getInstance(ctx);
		cursor = helper.fetchLocationsCursorForCurrentCatalogAndPersonal();
	}

	@Override
	public String getValue(int index) {
		if (cursor != null && cursor.getCount() > index) {
			cursor.moveToPosition(index);
			StringBuilder buffer = new StringBuilder();
			if (cursor.getString(LocationDbHelper.KEY_CATCODE_CURSORLOC) != null
					&& cursor.getString(LocationDbHelper.KEY_CATCODE_CURSORLOC).trim().length() > 0) {
				buffer.append(cursor.getString(LocationDbHelper.KEY_CATCODE_CURSORLOC));
				buffer.append(" ");
			}
			String locationId = cursor.getString(LocationDbHelper.KEY_LOCATIONID_CURSORLOC);
			String showLocation = cursor.getString(LocationDbHelper.KEY_SHOWLOCATIONNAME_CURSORLOC);
			Catalog catalog = LibApp.getInstance().getCurrentCatalog();
			String locationName = catalog.getResourcedValue(catalog.getName(), catalog.getLocationNamesMapping(), locationId, showLocation);
			buffer.append(locationName);
			return buffer.toString();
		} else {
			return null;
		}
	}

	@Override
	public Cursor getCursor() {
		return cursor;
	}

	@Override
	public int getCount() {
		if (cursor != null) {
			return cursor.getCount();
		} else {
			return 0;
		}
	}

	@Override
	public int getIndex(String value) {
		if (value == null)
			return 0;

		cursor.moveToFirst();
		int i = 0;
		do {
			String cursorValue = cursor.getString(LocationDbHelper.KEY_LOCATIONID_CURSORLOC);
			String longCursorValue = getValue(i);
			if (cursorValue != null && cursorValue.equals(value)) {
				return i;
			} else if (longCursorValue != null && longCursorValue.equals(value)) {
				return i;
			}
			i++;
		} while (cursor.moveToNext());

		return 0;
	}

	@Override
	public void refresh(Context ctx) {
		setCursor(ctx);
	}

	@Override
	public boolean alreadyExists(Context ctx, Object... values) {
		LocationDbHelper helper = LocationDbHelper.getInstance(ctx);
		boolean toReturn = helper.personalLocationAlreadyExists((String) values[0]);
		return toReturn;

	}

	@Override
	public void insert(Context ctx, Object... values) {
		LocationDbHelper helper = LocationDbHelper.getInstance(ctx);
		helper.insertPersonalLocation((String) values[0],(String) values[0]);
	}

	@Override
	public int remove(Context ctx, Object... values) {
		LocationDbHelper helper = LocationDbHelper.getInstance(ctx);
		DiveDbHelper dbhelper = DiveDbHelper.getInstance(ctx);
		Cursor dives = dbhelper.fetchCursorForLocation((String) values[0]);
		if (dives.getCount() > 0) {
			return -1;
		} else {
			int toReturn = helper.deletePersonalLocation((String) values[0]);
			return toReturn;
		}
	}

	@Override
	public int checkRemoval(Context ctx, Object... values) {
		LocationDbHelper helper = LocationDbHelper.getInstance(ctx);
		Cursor locations = helper.fetchPersonalLocation((String) values[0]);
		DiveDbHelper dbhelper = DiveDbHelper.getInstance(ctx);
		Cursor dives = dbhelper.fetchCursorForLocation((String) values[0]);
		int toReturn;
		if (dives.getCount() > 0) {
			toReturn =  -1;
		} else {
			toReturn = locations.getCount();
		}
		locations.close();
		dives.close();
		return toReturn;
	}

	@Override
	public Object getObject(int index) {
		if (cursor != null && cursor.getCount() > index) {
			cursor.moveToPosition(index);
			String catName = cursor.getString(LocationDbHelper.KEY_CATNAME_CURSORLOC);
			String catCode = cursor.getString(LocationDbHelper.KEY_CATCODE_CURSORLOC);
			String locationId = cursor.getString(LocationDbHelper.KEY_LOCATIONID_CURSORLOC);
			String showLocationName = cursor.getString(LocationDbHelper.KEY_SHOWLOCATIONNAME_CURSORLOC);
			Location location = new Location(catName, catCode, locationId, showLocationName);
			return location;
		} else {
			return null;
		}
	}

	@Override
	public Object getMinimalObject(Context ctx, Object... values) {
		return null;
	}

	public void closeCursor() {
		if (cursor != null)
			cursor.close();
	}
}
