package nl.imarinelife.lib;

import nl.imarinelife.lib.utility.NoContextException;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class Preferences {
	public static final String TAG = "preferences";

	public static final String BEHAVIOUR_PREFS = "behaviour";
	public static final String DEFAULT_SELECTED_NAV = "selectedNavigation";
	public static final String USER_NAME = "userName";
	public static final String USER_EMAIL = "userEmail";
	public static final String USER_CODE = "userCode";
	public static final String USES_SD = "usesSD";
	public static final String APP_STORE_EMPTY = "AppStorageEmpty";
	public static final String LAST_FIELDGUIDE_POSITION = "lastFieldguidePosition";
	public static final String FIELDGUIDE_GROUPS_HIDDEN = "fieldguideGroupsHidden";
	public static final String SIGHTINGS_GROUPS_HIDDEN = "sightingsGroupsHidden";

	// _Collapsed_last is niet overbodig omdat het mogelijk is dat de lijst daadwerkelijk ingeklapte
	// waarden anders is dan de door de app geleverde apps
	// (zeker bij sightings waar ook oude sightings in opgenomen kunnen zijn)
	public static final String SIGHTINGS_COLLAPSED_LAST = "sightingscollapsedLast";
	public static final String FIELDGUIDE_COLLAPSED_LAST = "fieldguidecollapsedLast";

	public static final String PERSONAL_DEFAULT_CHOICE = "personalDefaultChoice";
	public static final String SEND_ME = "SendToMeInCC";
	public static final String CURRENT_LANGUAGE = "currentLocale";
	public static final String IGNORED_LANGUAGE = "acceptedLocale";
	public static final String DATAVERSION_FIELDGUIDEANDSIGHTINGS = "dataVersionFieldGuideAndSightings";
	public static final String DATAVERSION_LOCATIONS = "dataVersionLocations";
	public static final String DATAVERSION_PROFILEPARTS = "dataVersionProfileParts";
	public static final String DATAVERSION_DIVES = "dataVersionDives";
	
	
	private static SharedPreferences prefs = null;

	static {
		prefs = LibApp.getInstance().getSharedPreferences(BEHAVIOUR_PREFS,
				MainActivity.MODE_PRIVATE);
	}

	public static String getString(String name, String defValue) {
		return prefs.getString(name, defValue);
	}

	public static void setString(String name, String value) {
		prefs.edit().putString(name, value).commit();
	}

	public static void addStringToList(String name, String value) {
		String oldValue = prefs.getString(name, "");
		if (!oldValue.contains("$" + value + "$")) {
			prefs.edit().putString(name, oldValue + "$" + value + "$").commit();
		}
	}

	public static void removeStringFromList(String name, String value) {
		String oldValue = prefs.getString(name, "");
		if (oldValue.contains("$" + value + "$")) {
			String newValue = oldValue.replace("$" + value + "$", "");
			prefs.edit().putString(name, newValue).commit();
		}
	}

	public static boolean listHasValue(String name, String value) {
		String oldValue = prefs.getString(name, "");
		if (oldValue.contains("$" + value + "$")) {
			return true;
		} else {
			return false;
		}
	}

	public static void toggleListValue(String name, String value) {
		String oldValue = prefs.getString(name, "");
		if (oldValue.contains("$" + value + "$")) {
			String newValue = oldValue.replace("$" + value + "$", "");
			prefs.edit().putString(name, newValue).commit();
		} else {
			prefs.edit().putString(name, oldValue + "$" + value + "$").commit();
		}
		String newValue = prefs.getString(name, "");
		Log.d(TAG, "toggleListValue [" + newValue + "]");
	}

	public static int getListLength(String name) {
		String oldValue = prefs.getString(name, "");
		if (oldValue.length() == 0)
			return 0;
		String[] values = oldValue.split("$$");
		return values.length;
	}

	public static Boolean getBoolean(String name, Boolean defValue) {
		return prefs.getBoolean(name, defValue);
	}

	public static void setBoolean(String name, Boolean value) {
		prefs.edit().putBoolean(name, value).commit();
	}

	public static int getInt(String name, int defValue) {
		return prefs.getInt(name, defValue);
	}

	public static void setInt(String name, int value) {
		prefs.edit().putInt(name, value).commit();
	}

	public static MainDrawer.SelectedNavigation getSelectedNavigation() {
		String selectedNav = prefs.getString(DEFAULT_SELECTED_NAV, null);
		if (selectedNav == null) {
			return null;
		} else {
			return MainDrawer.SelectedNavigation.valueOf(selectedNav);
		}
	}

	public static void setSelectedNavigation(
			MainDrawer.SelectedNavigation selected) {
		prefs.edit().putString(DEFAULT_SELECTED_NAV, selected.toString())
				.commit();

	}

	public static String getPersonalDefaultValue() {
		boolean useDefault2;
		try {
			useDefault2 = getBooleanFromDefaultSharedPreferences(PERSONAL_DEFAULT_CHOICE);
		} catch (NoContextException e) {
			useDefault2=false;
		}
		if (useDefault2) {
			return LibApp.getSecondDefaultableCatalogSightingChoice();
		} else {
			return LibApp.getFirstDefaultableCatalogSightingChoice();
		}
	}
	
	public static boolean getBooleanFromDefaultSharedPreferences(String key) throws NoContextException {
		Context context = null;
		if(MainActivity.me!=null) context = MainActivity.me;
		if(context==null && SettingsActivity.me!=null) context = SettingsActivity.me;
		if(context==null) throw new NoContextException();
		SharedPreferences prefs1 = PreferenceManager
				.getDefaultSharedPreferences(MainActivity.me.getBaseContext());
		boolean value = prefs1.getBoolean(key, false);
		Log.d(TAG, "getPersonalDefaultChoice [" + value + "]");
		return value;
	}
}
