package nl.imarinelife.lib.fieldguide;

import java.util.ArrayList;
import java.util.List;

import nl.imarinelife.lib.Preferences;
import nl.imarinelife.lib.divinglog.sightings.DivingLogSightingsEntryFragment;
import nl.imarinelife.lib.divinglog.sightings.Sighting;
import nl.imarinelife.lib.fieldguide.db.FieldGuideAndSightingsEntryDbHelper;
import nl.imarinelife.lib.fieldguide.db.FieldGuideEntry;

import android.app.Fragment;
import android.app.FragmentManager;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

public class FieldGuideEntryPagerAdapter {

	private static String	TAG				= "FieldGuideEntryPagerAdapter";

	private Cursor cursor;
	public int count = -1;
	public List<Integer> hiddenPositions = new ArrayList<Integer>();
	private int beforePosition = -1;
	public int currentPosition = -1;
	private int nextPosition = -1;

	public FieldGuideEntryPagerAdapter(Cursor cursor, int currentPosition, FieldGuideEntryFragment fragment) {
		this.cursor = cursor;
		getCount();
		this.currentPosition=currentPosition;
		cursor.moveToPosition(currentPosition);
		FieldGuideEntry currentEntry = FieldGuideAndSightingsEntryDbHelper
				.getFieldGuideEntryFromCursor(cursor);
		fragment.setFieldGuideEntry(currentEntry);
		beforePosition = getBeforePosition(currentPosition);
		nextPosition = getNextPosition(currentPosition);
		Log.d(TAG, "constructor: bef["+beforePosition+"]cur["+currentPosition+"]next["+nextPosition+"]");
	}

	public void fillNextEntry(FieldGuideEntryFragment fragment) {
		if (cursor == null) {
			FragmentManager manager = fragment.getFragmentManager();
			if (manager != null)
				manager.popBackStackImmediate();
		} else {
			if(nextPosition!=-1){
				beforePosition = currentPosition;
				currentPosition = nextPosition;
				nextPosition = getNextPosition(currentPosition);
				cursor.moveToPosition(currentPosition);
				FieldGuideEntry entry = FieldGuideAndSightingsEntryDbHelper
						.getFieldGuideEntryFromCursor(cursor);
				fragment.setFieldGuideEntry(entry);
				nextPosition = getNextPosition(currentPosition);
			}else{
				// do nothing - keep showing old situation
			}
			Log.d(TAG, "fillnext: bef["+beforePosition+"]cur["+currentPosition+"]next["+nextPosition+"]");
		}
	}

	public void fillBeforeEntry(FieldGuideEntryFragment fragment) {
		if (cursor == null) {
			FragmentManager manager = fragment.getFragmentManager();
			if (manager != null)
				manager.popBackStackImmediate();
		} else {
			if(beforePosition!=-1){
				nextPosition = currentPosition;
				currentPosition = beforePosition;
				cursor.moveToPosition(currentPosition);
				FieldGuideEntry entry = FieldGuideAndSightingsEntryDbHelper
						.getFieldGuideEntryFromCursor(cursor);
				fragment.setFieldGuideEntry(entry);
				beforePosition = getBeforePosition(currentPosition);
			}else{
				// do nothing - keep showing old situation
			}
			Log.d(TAG, "fillbef: bef["+beforePosition+"]cur["+currentPosition+"]next["+nextPosition+"]");
		}
	}

	private int getBeforePosition(int position) {
		if (cursor == null) {
			return -1;
		} else {
			int current = position;
			if (current == 0) {
				return -1;
			} else {
				current--;
				while (current != -1 && hiddenPositions.contains(current)) {
					current--;
				}
				return current;
			}
		}
	}

	private int getNextPosition(int position) {
		if (cursor == null) {
			return -1;
		} else {
			int current = position;
			if (current == cursor.getCount()-1) {
				return -1;
			} else {
				current++;
				while (current < cursor.getCount() && hiddenPositions.contains(current)) {
					current++;
				}
				if (current > cursor.getCount()-1) {
					return -1;
				}
				return current;
			}
		}
	}


	public int getCount() {
		if (count == -1) {
			Log.d(TAG,
					"getCount[" + (cursor != null
							? cursor.getCount()
							: null) + "]");
			if (cursor == null)
				count = 0;
			else {
				count = cursor.getCount();
				int current = cursor.getPosition();
				cursor.moveToFirst();
				do {
					String group_name = cursor.getString(FieldGuideAndSightingsEntryDbHelper.KEY_GROUPNAME_CURSORLOC);
					if (Preferences.listHasValue(Preferences.SIGHTINGS_GROUPS_HIDDEN,
							group_name)) {
						hiddenPositions.add(cursor.getPosition());
					}

				} while (cursor.moveToNext());
				cursor.moveToPosition(current);
			}
		}
		return count;

	}

	public void swapCursor(Cursor c) {
		if (cursor == c)
			return;
		if (this.cursor != null) {
			this.cursor.close();
		}
		this.cursor = c;
		count = -1;
		getCount();
	}

	public Cursor getCursor() {
		return cursor;
	}


}

