package nl.imarinelife.lib.utility.events;

import nl.imarinelife.lib.LibApp;
import nl.imarinelife.lib.Preferences;
import nl.imarinelife.lib.R;
import nl.imarinelife.lib.catalog.Catalog;
import nl.imarinelife.lib.divinglog.db.dive.DiveDbHelper;
import nl.imarinelife.lib.divinglog.db.res.LocationDbHelper;
import nl.imarinelife.lib.divinglog.db.res.ProfilePartDbHelper;
import nl.imarinelife.lib.fieldguide.db.FieldGuideAndSightingsEntryDbHelper;
import nl.imarinelife.lib.utility.dialogs.ThreeChoiceDialogFragment;
import nl.imarinelife.lib.utility.dialogs.ThreeChoiceDialogFragment.OnOneListener;
import nl.imarinelife.lib.utility.dialogs.ThreeChoiceDialogFragment.OnTwoListener;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.util.Log;

public class LanguageChangeEvent {
	private static String TAG = "LanguageChangeEvent";

	private static ThreeChoiceDialogFragment fragment = null;
	private static int finishedBackendProcesses = 0;

	public static ThreeChoiceDialogFragment getDialogFragment() {
		return fragment;
	}

	public static boolean dismissDialogFragment() {
		finishedBackendProcesses++;
		if (fragment != null && finishedBackendProcesses == 2) {
			//fragment.dismiss();
			fragment = null;
			finishedBackendProcesses = 0;
			return true;
		} else {
			return false;
		}
	}

	public void decideLanguageChange(Activity activity) {
		Catalog currentCatalog = LibApp.getInstance().getCurrentCatalog();
		// the language decided on earlier
		String currentLanguage = Preferences.getString(
				Preferences.CURRENT_LANGUAGE, null);
		// the language we could turn to based on the current Locale (as
		// defaulted to available values/string.xml)
		String possibleLanguage = currentCatalog.getPossibleLanguage();
		// the language that was already accepted as NOT using (so if it is
		// equal to the possibleLanguage we ignore it)
		String ignoredLanguage = Preferences.getString(
				Preferences.IGNORED_LANGUAGE, null);

		if (!currentLanguage.equals(possibleLanguage)) {
			if (!possibleLanguage.equals(ignoredLanguage)) {
				Log.d(TAG,
						"decideLanguageChange - changing - locale changed or LATER given earlier");
				// at some previous time the answer was LATER or a locale change
				// has occurred
				fragment = getYesNoLaterLanguageChangedFragment(activity,
						currentCatalog);
				FragmentTransaction ft = (activity).getFragmentManager()
						.beginTransaction();
				ft.addToBackStack(null);
				ft.commit();
				//fragment.show(ft, "adddialog");

			} else {
				Log.d(TAG,
						"decideLanguageChange - no change necessary - no locale change or NO given earlier");
			}
		} else {
			Log.d(TAG,
					"decideLanguageChange - no change necessary - already using correct language");
		}

	}

	private ThreeChoiceDialogFragment getYesNoLaterLanguageChangedFragment(
			final Activity activity, final Catalog currentCatalog) {
		final int layoutId;
		final int textViewId;
		final int yesbuttonId;
		final int nobuttonId;
		final int laterbuttonId;
		layoutId = R.layout.yesnolater_localechange_dialog;
		textViewId = R.id.general_textview_id;
		yesbuttonId = R.id.general_yes_button_id;
		nobuttonId = R.id.general_no_button_id;
		laterbuttonId = R.id.general_later_button_id;

		@SuppressWarnings("serial")
		OnOneListener yesListener = new OnOneListener() {

			@Override
			public void onOne() {
				// setting the currentLanguage will switch the resources values gotten
				String possibleLanguage = currentCatalog.getPossibleLanguage();
				Preferences.setString(Preferences.CURRENT_LANGUAGE,
						possibleLanguage);
				
				// Locations
				Log.d(TAG, "onYes - fillLocations starting");
				LocationDbHelper locationsAdapter = LocationDbHelper
						.getInstance(activity);
				DiveDbHelper diveAdapter = DiveDbHelper.getInstance(activity);
				currentCatalog.fillLocations(locationsAdapter, diveAdapter);
				// ProfileParts
				Log.d(TAG, "onYes - fillProfileParts starting");
				ProfilePartDbHelper profilepartsAdapter = ProfilePartDbHelper
						.getInstance(activity);
				currentCatalog.fillProfileParts(profilepartsAdapter);
				// FieldGuide and Sightings
				Log.d(TAG, "onYes - resetting locale dependent content");
				FieldGuideAndSightingsEntryDbHelper fieldguideAdapter = FieldGuideAndSightingsEntryDbHelper
						.getInstance(activity);
				fieldguideAdapter.resetLocaleDependentContent();
			}
		};

		@SuppressWarnings("serial")
		OnTwoListener noListener = new OnTwoListener() {

			@Override
			public void onTwo() {
				String possibleLanguage = currentCatalog.getPossibleLanguage();
				Preferences.setString(Preferences.IGNORED_LANGUAGE,
						possibleLanguage);
			}
		};

		String dialogtxt = LibApp.getCurrentResources().getString(
				R.string.yesnolater_changeLanguage);
		ThreeChoiceDialogFragment frag = ThreeChoiceDialogFragment.newInstance(
				dialogtxt, layoutId, textViewId, yesbuttonId, nobuttonId,
				laterbuttonId, DialogFragment.STYLE_NO_TITLE,
				R.style.iMarineLifeDialogTheme);
		frag.setOnOneListener(yesListener);
		frag.setOnTwoListener(noListener);
		return frag;

	}

}
