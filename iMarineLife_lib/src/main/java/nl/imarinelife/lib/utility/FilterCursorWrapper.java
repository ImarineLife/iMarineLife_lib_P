package nl.imarinelife.lib.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.imarinelife.lib.LibApp;
import nl.imarinelife.lib.Preferences;
import nl.imarinelife.lib.catalog.Catalog;
import nl.imarinelife.lib.fieldguide.db.FieldGuideAndSightingsEntryDbHelper;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.util.Log;

public class FilterCursorWrapper extends CursorWrapper {
	private static final String TAG = "FilterCursorWrapper";
	private static HashMap<String, List<Integer>> handledGroups = null;
	private String filter;
	private int[] columns;
	private int[] index;
	private int count = 0;
	private int pos = 0;

	public FilterCursorWrapper(Cursor cursor, String hiddenlistName,
			int groupColumnIndex) {
		this(cursor, "", hiddenlistName, groupColumnIndex, null, null, null);
	}

	public FilterCursorWrapper(Cursor cursor, String filter, int[] columns,
			Map<Integer, Map<String, Integer>> columnsToLocalize, Map<Integer,Integer> codeToShowValueColumnMapping) {
		this(cursor, filter, null, -1, columns, columnsToLocalize, codeToShowValueColumnMapping);
	}

	public FilterCursorWrapper(Cursor cursor, String filter,
			String hiddenlistName, int groupColumnIndex, int[] codeColumns,
			Map<Integer, Map<String, Integer>> columnsToLocalize, Map<Integer,Integer> codeToShowValueColumnMapping) {
		super(cursor);
		Log.d(TAG, "super FilterCursorWrapper["
				+ (cursor instanceof FilterCursorWrapper) + "]");
		handledGroups = new HashMap<String, List<Integer>>();
		this.filter = filter.toLowerCase();
		this.columns = codeColumns;
		if (this.filter.length() != 0
				|| Preferences.getListLength(hiddenlistName) > 0) {
			Log.d(TAG, "filter something");
			this.count = super.getCount();
			this.index = new int[this.count];
			for (int i = 0; i < this.count; i++) {
				Log.d(TAG, "filter [" + i + "]");
				super.moveToPosition(i);
				// an entry is found if it should be a part of the cursor
				// returned
				// entries should be returned by the cursor if
				// - they would be part of the query (filtered or not)
				// - and
				// - the group is NOT in the hiddenList (Preferences)
				// - or it is the first entry in the group (the first entry is
				// necessary to create the expandbar for that group)
				boolean found = false;
				if (this.filter.length() == 0
						&& (groupColumnIndex == -1 || hiddenlistName == null)) {
					found = true;
				}
				Log.d(TAG, "found so far: [" + found + "]");

				if (this.filter.length() > 0) {
					Log.d(TAG, "filter[" + this.filter + "]");
					if (codeColumns != null) {
						for (int j = 0; j < codeColumns.length; j++) {
							Log.d(TAG,
									"filtering["
											+ this.getString(this.columns[j])
													.toLowerCase() + "]");
							String showValue = null;
							if(codeToShowValueColumnMapping!=null && !codeToShowValueColumnMapping.isEmpty()){
								Integer showValueColumnNr = codeToShowValueColumnMapping.get(codeColumns[j]);
								if(showValueColumnNr!=null){
									showValue = cursor.getString(showValueColumnNr);
								}
							}
							
							String columnValue = null;
							if (columnsToLocalize.containsKey(this.columns[j])) {
								Catalog catalog = LibApp.getInstance()
										.getCurrentCatalog();
								columnValue = catalog.getResourcedValue(
										columnsToLocalize.get(this.columns[j]),
										this.getString(this.columns[j]), showValue);
							} else {
								columnValue = this.getString(this.columns[j]);
							}
							if (columnValue.toLowerCase().contains(this.filter)) {
								Log.d(TAG, this.getString(this.columns[j])
										+ "contains[" + this.filter + "] ");
								found = true;
								break;
							}
						}
					}else{
						found=true;
					}
				} else {
					found = true;
				}
				Log.d(TAG, "found after filtering: [" + found + "]");

				String groupName = groupColumnIndex!=-1 ? this.getString(groupColumnIndex) : "all" ;
				countAmounts(found, groupName);
				Log.d(TAG, Preferences.listHasValue(hiddenlistName, groupName)
						+ " [" + Preferences.getString(hiddenlistName, "")
						+ "][" + handledGroups.get(groupName).get(0) + "]");
				if (handledGroups.get(groupName).get(0) > 1
						&& Preferences.listHasValue(hiddenlistName, groupName)) {
					found = false;
				}

				Log.d(TAG,
						"found after grouping: ["
								+ groupName
								+ "]["
								+ this.getString(FieldGuideAndSightingsEntryDbHelper.KEY_COMMONNAME_CURSORLOC)
								+ "][" + found + "]");
				if (found) {
					this.index[this.pos++] = i;
				}
			}
			this.count = this.pos;
			this.pos = 0;
			super.moveToFirst();
		} else {
			this.count = super.getCount();
			this.index = new int[this.count];
			for (int i = 0; i < this.count; i++) {
				this.index[i] = i;
			}
		}
		Log.d(TAG, "parentcursor[" + super.getCount() + "] wrappedCursor["
				+ this.count + "]");
		logIndex("current", this.index);
		Log.d(TAG, "countAmounts[" + handledGroups + "]");

	}

	private void countAmounts(boolean found, String groupName) {

		if (!handledGroups.keySet().contains(groupName)) {
			List<Integer> nr = new ArrayList<Integer>();
			nr.add(1);
			if (found) {
				nr.add(1);
			} else {
				nr.add(0);
			}
			handledGroups.put(groupName, nr);
		} else {
			List<Integer> nr = handledGroups.get(groupName);
			Integer total = nr.get(0);
			total++;
			nr.remove(0);
			nr.add(0, total);
			if (found) {
				Integer nrFound = nr.get(1);
				nrFound++;
				nr.remove(1);
				nr.add(nrFound);
			}
		}
		Log.d(TAG, "FilterCursorWrapper countAmounts: done[" + groupName + "] "
				+ handledGroups);

	}

	private static void logIndex(String which, int[] array) {
		StringBuffer buffer = new StringBuffer();
		if (array != null) {
			for (int i = 0; i < array.length; i++) {
				if (buffer.length() > 0)
					buffer.append(",");
				buffer.append(array[i]);
			}
		}
		Log.d(TAG, which + " logIndex[" + buffer.toString() + "]");
	}

	@Override
	public boolean move(int offset) {
		return this.moveToPosition(this.pos + offset);
	}

	@Override
	public boolean moveToNext() {
		return this.moveToPosition(this.pos + 1);
	}

	@Override
	public boolean moveToPrevious() {
		return this.moveToPosition(this.pos - 1);
	}

	@Override
	public boolean moveToFirst() {
		return this.moveToPosition(0);
	}

	@Override
	public boolean moveToLast() {
		return this.moveToPosition(this.count - 1);
	}

	@Override
	public boolean moveToPosition(int position) {
		if (position >= this.count || position < 0)
			return false;
		this.pos = position;
		return super.moveToPosition(this.index[position]);
	}

	@Override
	public int getCount() {
		return this.count;
	}

	@Override
	public int getPosition() {
		return this.pos;
	}

	@Override
	public boolean isFirst() {
		return this.pos == 0;
	}

	@Override
	public boolean isLast() {
		return (this.pos - 1) == this.count;
	}

	public static String getAmounts(String groupName) {
		List<Integer> nrs = handledGroups.get(groupName);
		if (nrs != null && (nrs.size() == 1 || nrs.get(0) == nrs.get(1))) {
			return "" + nrs.get(0);
		} else if (nrs != null && nrs.size() == 2) {
			return nrs.get(1) + "/" + nrs.get(0);
		} else {
			return "";
		}
	}
}
