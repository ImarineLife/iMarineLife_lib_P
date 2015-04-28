package nl.imarinelife.lib.fieldguide;

import nl.imarinelife.lib.LibApp;
import nl.imarinelife.lib.MainActivity;
import nl.imarinelife.lib.R;
import nl.imarinelife.lib.divinglog.sightings.DivingLogSightingsEntryPagerFragment;
import nl.imarinelife.lib.fieldguide.db.FieldGuideAndSightingsEntryDbHelper;
import nl.imarinelife.lib.fieldguide.db.FieldGuideEntry;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class FieldGuideEntryFragment extends Fragment {

	private static String	TAG				= "FieldGuideEntryFragment";
	private View			entry			= null;
	
	private TextView		commonView		= null;
	private TextView		latinView		= null;
	private TextView		descriptionView	= null;
	private ImageView		imageView		= null;

	private long			shownId			= 0L;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setRetainInstance(true);
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			// Restore last state for checked position.
			shownId = savedInstanceState.getLong(FieldGuideEntry.ID,
				0L);
		} else {
			Bundle arguments = getArguments();
			if (arguments != null) {
				shownId = arguments.getLong(FieldGuideEntry.ID,
					0L);
			} else {
				shownId = 0L;
			}
		}

		Log.d("FieldGuideEntryFragment",
			"onCreateView: Id found 1: " + shownId);

		// Inflate the layout for this fragment
		entry = inflater.inflate(R.layout.entry_field_guide,
			container,
			false);
		commonView = (TextView) entry.findViewById(R.id.common_fieldguide_entry);
		latinView = (TextView) entry.findViewById(R.id.latin_fieldguide_entry);
		descriptionView = (TextView) entry.findViewById(R.id.description_fieldguide_entry);
		imageView = (ImageView) entry.findViewById(R.id.image_fieldguide_entry);

		setData(shownId);
		return entry;
	}

	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// super.onCreateOptionsMenu(menu,
		// inflater);

		inflater.inflate(R.menu.field_guide_entry,
			menu);
        ActionBar bar=null;
        if(MainActivity.me!=null){
            bar = MainActivity.me.getSupportActionBar();
        }

        if(bar!=null) {
            bar.setHomeButtonEnabled(true);
        }else{
            Log.d(TAG, "no actionbar to set Homebutton enabled for");
        }
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(TAG,
			"onOptionsItemSelected: [" + item.getItemId() + "]");
		switch (item.getItemId()) {
			case android.R.id.home:
				Log.d(TAG,
					"onOptionsItemSelected: poppingBackStack");
				getFragmentManager().popBackStackImmediate();
				return true;
		}
		return super.onOptionsItemSelected(item);

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(FieldGuideEntry.ID,
			shownId);
	}

	public long getShownId() {
		return shownId;
	}

	public void setData(long id) {
		if (id != 0) {
			shownId = id;
		} else {
			id = shownId;
		}

		Uri uri = null;
		if (id != 0) {
			uri = Uri.withAppendedPath(FieldGuideEntry.CONTENT_URI,
				Long.toString(id));
		} else {
			uri = FieldGuideEntry.CONTENT_URI;
		}

		Log.d(TAG,
			uri.toString());
		Log.d(TAG,
			MainActivity.me == null
					? "null"
					: "not null");
		Log.d(TAG,
			MainActivity.me == null || MainActivity.me.getContentResolver() == null
					? "null"
					: "not null");

		Cursor cursor = null;
		int counter=0;
		while(cursor==null && counter<5){
			cursor = MainActivity.me.getContentResolver().query(uri,
					null,
					null,
					null,
					null);
			counter++;
		}
		FieldGuideEntry entry = null;
		if(cursor!=null){
			cursor.moveToFirst();
			entry = FieldGuideAndSightingsEntryDbHelper.getFieldGuideEntryFromCursor(cursor);
			cursor.close();
		}

		if (commonView != null && entry != null) {
			latinView.setText(entry.latinName);
			commonView.setText(entry.getShowCommonName());
			descriptionView.setText(entry.getResourcedDescription());

			int screenSize = LibApp.getCurrentResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
			int orientation = LibApp.getCurrentResources().getConfiguration().orientation;

			Bitmap bm = null;
			switch (screenSize) {
				case Configuration.SCREENLAYOUT_SIZE_LARGE:
				case Configuration.SCREENLAYOUT_SIZE_XLARGE:
					bm = entry.getLpicBitmapAsExpansion();
					if (bm == null)
						bm = entry.getSpicBitmapAsAsset();
					imageView.setImageBitmap(bm);
					break;
				default:
					switch (orientation) {
						case Configuration.ORIENTATION_LANDSCAPE:
							bm = entry.getLpicBitmapAsExpansion();
							Log.d(TAG,
								"setData bm found for Lpic[" + bm + "]");
							if (bm == null)
								bm = entry.getSpicBitmapAsAsset();
							imageView.setImageBitmap(bm);
							break;
						default:
							bm = entry.getSpicBitmapAsAsset();
							imageView.setImageBitmap(bm);
							break;
					}
			}

		}

	}

	public void onBackStackChanged() {
        ActionBar bar=null;
        if(MainActivity.me!=null){
            bar = MainActivity.me.getSupportActionBar();
        }

        if(bar!=null) {
            bar.setHomeButtonEnabled(true);
            int backStackEntryCount = getActivity().getSupportFragmentManager().getBackStackEntryCount();
            Log.d(TAG,
                    "backstackEntryCount[" + backStackEntryCount + "]");
            if (backStackEntryCount > 0) {
                bar.setDisplayHomeAsUpEnabled(true);
            } else {
                bar.setDisplayHomeAsUpEnabled(false);
            }}else{
            Log.d(TAG, "no actionbar to set Homebutton enabled for");
        }


	}

	@Override
	public void onStop() {
		// getActivity().getSupportFragmentManager().removeOnBackStackChangedListener(this);
		super.onStop();
	}

	@Override
	public void onDestroy() {
		// getActivity().getSupportFragmentManager().removeOnBackStackChangedListener(this);
		super.onDestroy();
	}

	@Override
	public void onStart() {
		// getActivity().getSupportFragmentManager().addOnBackStackChangedListener(this);
		super.onStart();
	}

	public interface OnFieldGuideItemSelectedListener {
		public void activateFieldGuideEntryFragment(FieldGuideEntryFragment entry, int position, long id,
				String constraint);

		void activateDivingLogSightingsEntryFragment(DivingLogSightingsEntryPagerFragment entry, int position, long id,
				String constraint);
	}

}
