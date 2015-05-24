package nl.imarinelife.lib.fieldguide;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import nl.imarinelife.lib.LibApp;
import nl.imarinelife.lib.MainActivity;
import nl.imarinelife.lib.R;
import nl.imarinelife.lib.fieldguide.db.FieldGuideAndSightingsEntryDbHelper;
import nl.imarinelife.lib.fieldguide.db.FieldGuideEntry;
import nl.imarinelife.lib.utility.DivingLogGestureListener;

public class FieldGuideEntryFragment extends Fragment {

	private static String	TAG				= "FieldGuideEntryFragment";
	private View			entry			= null;
	
	private TextView		commonView		= null;
	private TextView		latinView		= null;
	private TextView		descriptionView	= null;
	private ImageView		imageView		= null;

	private long			shownId			= 0L;
	private int				position		= 0;
	FieldGuideEntry 		fldgEntry 		= null;
	FieldGuideEntryPagerAdapter	pagerAdapter;

	private GestureDetector gesturedetector = null;
	Cursor cursor=null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		//setRetainInstance(true);
		super.onCreate(savedInstanceState);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState == null) {
			savedInstanceState = getArguments();
		}
		if (savedInstanceState == null) {
			savedInstanceState = new Bundle();
		}
		shownId = savedInstanceState.getLong(FieldGuideEntry.ID, 0L);
		position = savedInstanceState.getInt(FieldGuideListFragment.CHECKED_POSITION);

		initializePagerAdapter(savedInstanceState);

		Log.d("FieldGuideEntryFragment",
				"onCreateView: Id found 1: " + shownId);

		gesturedetector = new GestureDetector(getActivity(),
				new FieldGuideEntryFragmentGestureListener());

		container.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				gesturedetector.onTouchEvent(event);
				return true;
			}

		});

		// Inflate the layout for this fragment
		entry = inflater.inflate(R.layout.entry_field_guide,
			container,
			false);
		// and set the onTouchListener
		entry.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				gesturedetector.onTouchEvent(event);
				return true;
			}
		});

		// set the onTouchListener separately on the scrollView because that will gobble up the swipe
		ScrollView view = (ScrollView) entry.findViewById(R.id.scrollview_descr_fieldguide_entry);
		view.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return gesturedetector.onTouchEvent(event);
			}
		});

		commonView = (TextView) entry.findViewById(R.id.common_fieldguide_entry);
		latinView = (TextView) entry.findViewById(R.id.latin_fieldguide_entry);
		descriptionView = (TextView) entry.findViewById(R.id.description_fieldguide_entry);
		imageView = (ImageView) entry.findViewById(R.id.image_fieldguide_entry);

		setData(shownId, position);
		return entry;
	}

	private void initializePagerAdapter(Bundle savedInstanceState) {
		String constraint = savedInstanceState.getString(FieldGuideListFragment.CONSTRAINT);
		Log.d(TAG, "onCreate query[" + shownId + "]["+position+"][" + constraint + "]");
		Uri uri = null;
		if (constraint == null || constraint.length() == 0) {
			uri = FieldGuideEntry.CONTENT_URI;
		} else {
			uri = Uri.withAppendedPath(FieldGuideEntry.CONTENT_URI_FILTER,
					constraint);
		}

		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		cursor=null;
		Log.d(TAG, "going to get cursor: " + uri);
		int counter = 0;
		while (cursor == null && counter < 5) {
			cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
			counter++;
		}
		Log.d(TAG, "cursor count[" + (cursor != null ? cursor.getCount() : 0)
				+ "]");

		pagerAdapter = new FieldGuideEntryPagerAdapter(cursor, position, this);

	}

	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// super.onCreateOptionsMenu(menu,
		// inflater);

		inflater.inflate(R.menu.field_guide_entry,
				menu);
        ActionBar bar=null;
        if(MainActivity.me!=null){
            bar = MainActivity.me.getActionBar();
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
		outState.putInt(FieldGuideListFragment.CHECKED_POSITION, position);
		Log.d(TAG, "shownId secured as [" + shownId + "] position["+position+"]");
	}

	public long getShownId() {
		return shownId;
	}

	public void setFieldGuideEntry(FieldGuideEntry entry, int position){
		fldgEntry = entry;
		setData(entry.getId(), position);
	}

	public void setData(long id, int position) {
		if (id != 0) {
			shownId = id;
			this.position=position;
			FieldGuideListFragment.setTopPosition(position);
		} else {
			id = shownId;
		}
		Log.d(TAG,"shownId set to ["+shownId+"]");

		if(fldgEntry==null) {
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
			int counter = 0;
			while (cursor == null && counter < 5) {
				cursor = MainActivity.me.getContentResolver().query(uri,
						null,
						null,
						null,
						null);
				counter++;
			}
			if (cursor != null) {
				cursor.moveToFirst();
				fldgEntry = FieldGuideAndSightingsEntryDbHelper.getFieldGuideEntryFromCursor(cursor);
				cursor.close();
			}
		}

		if (commonView != null && fldgEntry != null) {
			latinView.setText(fldgEntry.latinName);
			commonView.setText(fldgEntry.getShowCommonName());
			descriptionView.setText(fldgEntry.getResourcedDescription());

			int screenSize = LibApp.getCurrentResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
			int orientation = LibApp.getCurrentResources().getConfiguration().orientation;

			Bitmap bm = null;
			switch (screenSize) {
				case Configuration.SCREENLAYOUT_SIZE_LARGE:
				case Configuration.SCREENLAYOUT_SIZE_XLARGE:
					bm = fldgEntry.getLpicBitmapAsExpansion();
					if (bm == null)
						bm = fldgEntry.getSpicBitmapAsAsset();
					imageView.setImageBitmap(bm);
					break;
				default:
					switch (orientation) {
						case Configuration.ORIENTATION_LANDSCAPE:
							bm = fldgEntry.getLpicBitmapAsExpansion();
							Log.d(TAG,
								"setData bm found for Lpic[" + bm + "]");
							if (bm == null)
								bm = fldgEntry.getSpicBitmapAsAsset();
							imageView.setImageBitmap(bm);
							break;
						default:
							bm = fldgEntry.getSpicBitmapAsAsset();
							imageView.setImageBitmap(bm);
							break;
					}
			}

		}

	}

	public void onBackStackChanged() {
        ActionBar bar=null;
        if(MainActivity.me!=null){
            bar = MainActivity.me.getActionBar();
        }

        if(bar!=null) {
            bar.setHomeButtonEnabled(true);
            int backStackEntryCount = getActivity().getFragmentManager().getBackStackEntryCount();
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
	}

	private class FieldGuideEntryFragmentGestureListener extends DivingLogGestureListener {

		@Override
		protected void onLeftSwipe() {
			Log.d(TAG, "onLeftSwipe: back");
			if(getView()!=null) {
				View withFocus = getView().findFocus();
				if (withFocus != null) {
					withFocus.clearFocus();
				}
			}
			pagerAdapter.fillBeforeEntry(FieldGuideEntryFragment.this);

		}

		@Override
		protected void onRightSwipe() {
			Log.d(TAG, "onRightSwipe: next");
			if(getView()!=null) {
				View withFocus = getView().findFocus();
				if (withFocus != null) {
					withFocus.clearFocus();
				}
			}
			pagerAdapter.fillNextEntry(FieldGuideEntryFragment.this);
		}

	}

}
