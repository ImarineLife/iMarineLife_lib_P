package nl.imarinelife.lib.divinglog.sightings;

import java.util.ArrayList;
import java.util.List;

import nl.imarinelife.lib.MainActivity;
import nl.imarinelife.lib.Preferences;
import nl.imarinelife.lib.fieldguide.db.FieldGuideAndSightingsEntryDbHelper;
import nl.imarinelife.lib.fieldguide.db.FieldGuideEntry;
import nl.imarinelife.lib.utility.SingletonCursor;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

public class DivingLogSightingsEntryPagerAdapter extends FragmentStatePagerAdapter {

	private static String	TAG	= "DLogSghtngEntryPagerAd";

	private Cursor			cursor;
	public int				currentPosition	= -1;
	public int				count			= -1;
	public List<Integer>	hiddenPositions	= new ArrayList<Integer>();

	public DivingLogSightingsEntryPagerAdapter(FragmentManager fm, Cursor cursor) {
		super(fm);
		this.cursor = cursor;
		getCount();
	}

	
	@Override
	public Fragment getItem(int position) {
		if (cursor == null) // shouldn't happen
			return null;
		Log.d(TAG,
			"getitem[" + position + "]");

		cursor.moveToPosition(position);
		currentPosition = position;

		Bundle args = new Bundle();
		args.putLong(FieldGuideEntry.ID,
			cursor.getInt(FieldGuideAndSightingsEntryDbHelper.KEY_FIELDGUIDE_ID_CURSORLOC));
		args.putLong(MainActivity.KEY_INT_CURRENTID,
			SingletonCursor.getCursor().getInt(FieldGuideAndSightingsEntryDbHelper.KEY_DIVENR_CURSORLOC));

		Fragment fragment = new DivingLogSightingsEntryFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public float getPageWidth(int position) {
		Log.d(TAG,
			"getPageWidth[" + position + "] hidenPositions[" + hiddenPositions + "]");
		if (hiddenPositions.contains(position)) {
			return 0;
		}
		return super.getPageWidth(position);
	}


	@Override
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
		notifyDataSetChanged();
	}

	public Cursor getCursor() {
		return cursor;
	}

	
}
