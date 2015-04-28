package nl.imarinelife.lib.fieldguide;

import nl.imarinelife.lib.LibApp;
import nl.imarinelife.lib.MainActivity;
import nl.imarinelife.lib.Preferences;
import nl.imarinelife.lib.R;
import nl.imarinelife.lib.fieldguide.db.FieldGuideEntry;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.ListView;

public class FieldGuideEntryPagerActivity extends ActionBarActivity
		implements OnPageChangeListener {

	private static String TAG = "FieldGuidEntryPagerActivity";
	FieldGuideEntryPagerAdapter pagerAdapter;
	ViewPager mViewPager;
	Cursor cursor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_field_guide_entry);
		setTitle(LibApp.getCurrentResources().getString(R.string.fieldguide));
		
		Intent sender = getIntent();
		int position = sender.getIntExtra(
				FieldGuideListFragment.CHECKED_POSITION, 0);
		long id = sender.getExtras().getLong(FieldGuideEntry.ID);
		String constraint = sender.getExtras().getString(
				FieldGuideListFragment.CONSTRAINT);
		Log.d(TAG, "onCreate query[" + id + "][" + constraint + "]");
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
			cursor = getContentResolver().query(uri, null, null, null, null);
			counter++;
		}
		Log.d(TAG, "cursor count[" + (cursor != null ? cursor.getCount() : 0)
				+ "]");

		pagerAdapter = new FieldGuideEntryPagerAdapter(
				getSupportFragmentManager(), cursor);
		mViewPager = (ViewPager) findViewById(R.id.fragment_container_entry_pager);
		mViewPager.setOnPageChangeListener(this);
		mViewPager.setAdapter(pagerAdapter);
		mViewPager.setCurrentItem(position);
		mViewPager.setId(R.id.fragment_container_entry_pager);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			/*
			 * Intent upIntent = NavUtils.getParentActivityIntent(this); if
			 * (NavUtils.shouldUpRecreateTask(this, upIntent)) { // This
			 * activity is NOT part of this app's task, so create a // new task
			 * // when navigating up, with a synthesized back stack.
			 * TaskStackBuilder.create(this) // Add all of this activity's
			 * parents to the back stack .addParentStack(this) // Navigate up to
			 * the closest parent .startActivities(); } else { // This activity
			 * is part of this app's task, so simply // navigate up to the
			 * logical parent activity. Log.d(TAG,
			 * "onOptionsItemSelected navigate up");
			 * NavUtils.navigateUpFromSameTask(this); Log.d(TAG,
			 * "onOptionsItemSelected: poppingBackStack");
			 * getSupportFragmentManager().popBackStackImmediate(); return true;
			 * } return true;
			 */
			return goUp();
		}

		return super.onOptionsItemSelected(item);
	}

	private boolean goUp() {
		Intent upIntent = NavUtils.getParentActivityIntent(this);
		if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
			// This activity is NOT part of this app's task, so create a
			// new task
			// when navigating up, with a synthesized back stack.
			TaskStackBuilder.create(this)
			// Add all of this activity's parents to the back stack
					.addParentStack(this)
					// Navigate up to the closest parent
					.startActivities();
		} else {
			// This activity is part of this app's task, so simply
			// navigate up to the logical parent activity.
			Log.d(TAG, "onOptionsItemSelected navigate up");
			NavUtils.navigateUpFromSameTask(this);
			Log.d(TAG, "onOptionsItemSelected: poppingBackStack");
			getSupportFragmentManager().popBackStackImmediate();
			return true;
		}
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		int backstackEntryCount = getSupportFragmentManager()
				.getBackStackEntryCount();
		int repeatCount = event.getRepeatCount();
		Log.d(TAG, "onKeyDown repeatCount[" + repeatCount
				+ "] backstackEntryCount[" + backstackEntryCount + "]");
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return goUp();
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onPageScrollStateChanged(int state) {
		// do nothing
	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {
		// do nothing
	}

	@Override
	public void onPageSelected(int position) {
		FieldGuideListFragment fieldGuideListFragment = (FieldGuideListFragment) MainActivity.me
				.getFragment("FieldGuideListFragment");
		Log.d(TAG, "onPageSelected listView[" + fieldGuideListFragment + "]");
		if (fieldGuideListFragment != null) {
			ListView listView = fieldGuideListFragment.getListView();
			listView.setSelection(position);
			FieldGuideListFragment.setTopPosition(position);
		}
		Preferences.setInt(Preferences.LAST_FIELDGUIDE_POSITION, position);
	}

}
