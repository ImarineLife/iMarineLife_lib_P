package nl.imarinelife.lib.divinglog.sightings;

import java.util.HashMap;
import java.util.Map;

import nl.imarinelife.lib.LibApp;
import nl.imarinelife.lib.MainActivity;
import nl.imarinelife.lib.Preferences;
import nl.imarinelife.lib.R;
import nl.imarinelife.lib.fieldguide.db.FieldGuideAndSightingsEntryDbHelper;
import nl.imarinelife.lib.utility.FilterCursorWrapper;
import nl.imarinelife.lib.utility.SingletonCursor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

//public class DivingLogSightingsEntryPagerFragment extends Fragment implements OnBackStackChangedListener {
public class DivingLogSightingsEntryPagerFragment extends Fragment implements OnPageChangeListener {


	public static String				TAG	= "DivingLogSightingsEntryPagerFragment";

	DivingLogSightingsEntryPagerAdapter	pagerAdapter;
	ViewPager							mViewPager;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setRetainInstance(true);
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		View fragment = inflater.inflate(R.layout.activity_sightings_entry,
			null);

		if (savedInstanceState == null) {
			savedInstanceState = getArguments();
		}
		if (savedInstanceState == null) {
			savedInstanceState = new Bundle();
		}
		int position = savedInstanceState.getInt(DivingLogSightingsListFragment.CHECKED_POSITION,
			0);
		String constraint = savedInstanceState.getString(DivingLogSightingsListFragment.CONSTRAINT);

		Log.d(TAG,
			"onCreateView query[" + position + "][" + constraint + "]");

		int diveNr = MainActivity.me.currentDive.getDiveNr();
		if (SingletonCursor.getCursor() == null || SingletonCursor.getCursor().isClosed()) {
			FieldGuideAndSightingsEntryDbHelper dbHelper = FieldGuideAndSightingsEntryDbHelper.getInstance(this.getActivity());
			SingletonCursor.swapCursor(dbHelper.queryFieldGuideFilledForDive(diveNr));
			if (constraint != null && constraint.length() != 0) {
				int[] columnsToSearch_s2 = { FieldGuideAndSightingsEntryDbHelper.KEY_COMMONNAME_CURSORLOC,
						FieldGuideAndSightingsEntryDbHelper.KEY_LATINNAME_CURSORLOC };
				Map<Integer, Map<String, Integer>> columnsToLocalize_s2 = new HashMap<Integer, Map<String, Integer>>();
				columnsToLocalize_s2
						.put(FieldGuideAndSightingsEntryDbHelper.KEY_COMMONNAME_CURSORLOC,
								LibApp.getInstance().getCurrentCatalog()
										.getCommonIdMapping());
				SingletonCursor.swapCursor(new FilterCursorWrapper(SingletonCursor.getCursor(), constraint, Preferences.SIGHTINGS_GROUPS_HIDDEN, FieldGuideAndSightingsEntryDbHelper.KEY_GROUPNAME_CURSORLOC, columnsToSearch_s2, columnsToLocalize_s2, FieldGuideAndSightingsEntryDbHelper.CODE_TO_SHOWVALUE_COLUMNMAPPING));
			}
		}

		pagerAdapter = new DivingLogSightingsEntryPagerAdapter(getChildFragmentManager(),SingletonCursor.getCursor());
		mViewPager = (ViewPager) fragment.findViewById(R.id.fragment_container_sightings_pager);
		mViewPager.setOnPageChangeListener(this);
		mViewPager.setAdapter(pagerAdapter);
		mViewPager.setCurrentItem(position);
		mViewPager.setId(R.id.fragment_container_sightings_pager);

        ActionBar bar=null;
        if(MainActivity.me!=null){
            bar = MainActivity.me.getSupportActionBar();
        }

        if(bar!=null) {
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setHomeButtonEnabled(true);
        }else{
            Log.d(TAG, "no actionbar to set Homebutton enabled for");
        }
		return fragment;
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
            }
        }else{
            Log.d(TAG, "no actionbar to set Homebutton enabled for");
        }



	}

	@Override
	public void onStop() {
		//getActivity().getSupportFragmentManager().removeOnBackStackChangedListener(this);
		super.onStop();
	}

	@Override
	public void onDestroy() {
		//getActivity().getSupportFragmentManager().removeOnBackStackChangedListener(this);
		super.onDestroy();
	}

	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// super.onCreateOptionsMenu(menu,
		// inflater);

		inflater.inflate(R.menu.sightings_entry,
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

    public interface OnDivingLogSightingsItemSelectedListener {
		public void activateDivingLogSightingsEntryFragment(DivingLogSightingsEntryPagerFragment entry, int position,
				long id, String constraint);
	}

	@Override
	public void onPageScrollStateChanged(int state) {
		// do nothing
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		// do nothing
	}

	@Override
	public void onPageSelected(int position) {
		Log.d(TAG,
			"onPageSelected ["+position+"]");
		DivingLogSightingsListFragment.setCurrentPosition(position);
		DivingLogSightingsListFragment.setTopPosition(position);	
	}

}
