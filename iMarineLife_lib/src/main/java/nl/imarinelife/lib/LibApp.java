package nl.imarinelife.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import nl.imarinelife.lib.catalog.Catalog;
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;

public class LibApp extends Application {

	private static final String TAG = "LibApp";

	private static LibApp me = null;
	private static Catalog catalog = null;
	private static Resources currentResources = null;

	public static Resources getCurrentResources() {
		String language = Preferences.getString(Preferences.CURRENT_LANGUAGE, null);
		Resources resources = LibApp.getContext().getResources();
		Configuration config = resources.getConfiguration();
		config.locale = new Locale(language);
		Resources nwResources = new Resources(resources.getAssets(), resources.getDisplayMetrics(), config);
		return nwResources;
	}
	
	public static void setCurrentResources(Resources currentResources) {
		LibApp.currentResources = currentResources;
	}

	public HashMap<String, DbHelper> dbhelpers = new HashMap<String, DbHelper>();

	public LibApp() {
		me = this;
	};

	public static LibApp getInstance() {
		if (me == null) {
			Log.d(TAG, "Zorg dat LibApp eerst geinstantieerd wordt");
		}
		return me;
	}

	public void setCatalog(Catalog catalog) {
		LibApp.catalog = catalog;
	};

	public Catalog getCurrentCatalog() {
		return catalog;
	}

	public static String getCurrentCatalogName() {
		if (getInstance().getCurrentCatalog() == null)
			return "";
		return getInstance().getCurrentCatalog().name;
	}

	public static String getCurrentCatalogVersionName() {
		if (getInstance().getCurrentCatalog() == null)
			return "";
		return getInstance().getCurrentCatalog().versionName;
	}

	public static String getCurrentCatalogPreferenceText(String type) {
		if (getInstance().getCurrentCatalog() == null || type == null)
			return "";
		return getInstance().getCurrentCatalog().getPrefText(type);
	}

	public static int getCurrentCatalogVersion() {
		if (getInstance().getCurrentCatalog() == null)
			return 0;
		return getInstance().getCurrentCatalog().version;
	}

	public static String getDiveOrPersonalOrCatalogDefaultChoice() {
		if (getInstance().getCurrentCatalog() == null) {
			return "?";
		} else {
			return getInstance().getCurrentCatalog().getDefaultSightingChoice();
		}

	}

	public static String getCurrentCatalogDefaultChoice() {
		if (getInstance().getCurrentCatalog() == null)
			return "?";
		return getInstance().getCurrentCatalog().defaultChoice;
	}

	public static String getCurrentCatalogGroups() {
		if (getInstance().getCurrentCatalog() == null)
			return "";
		return getInstance().getCurrentCatalog().allgroups;
	}

	public static ArrayList<String> getCurrentCatalogSightingChoices() {
		if (getInstance().getCurrentCatalog() == null)
			return new ArrayList<String>();
		return getInstance().getCurrentCatalog().sightingChoices;
	}

	public static String getFirstDefaultableCatalogSightingChoice() {
		if (getInstance().getCurrentCatalog() == null
				|| getInstance().getCurrentCatalog().defaultableSightingChoices
						.size() < 1)
			return "?";
		Log.d(TAG,
				"getFirstDefaultable["
						+ getInstance().getCurrentCatalog().defaultableSightingChoices
						+ "]["
						+ getInstance().getCurrentCatalog().defaultableSightingChoices
								.get(0) + "]");
		return getInstance().getCurrentCatalog().defaultableSightingChoices
				.get(0);
	}

	public static String getSecondDefaultableCatalogSightingChoice() {
		if (getInstance().getCurrentCatalog() == null
				|| getInstance().getCurrentCatalog().defaultableSightingChoices
						.size() < 2)
			return "";
		Log.d(TAG,
				"getSecondDefaultable["
						+ getInstance().getCurrentCatalog().defaultableSightingChoices
						+ "]["
						+ getInstance().getCurrentCatalog().defaultableSightingChoices
								.get(1) + "]");
		return getInstance().getCurrentCatalog().defaultableSightingChoices
				.get(1);
	}

	public static ArrayList<String> getCurrentCatalogCheckBoxChoices() {
		if (getInstance().getCurrentCatalog() == null)
			return new ArrayList<String>();
		return getInstance().getCurrentCatalog().checkboxChoices;
	}

	public static byte[] getSalt() {

		if (getInstance().getCurrentCatalog() == null)
			return null;
		return getInstance().getCurrentCatalog().getSalt();
	}

	public static String getBase64PublicKey() {

		if (getInstance().getCurrentCatalog() == null)
			return null;
		return getInstance().getCurrentCatalog().getBase64PublicKey();
	}

	public static String getProject() {

		if (getInstance().getCurrentCatalog() == null)
			return "";
		return getInstance().getCurrentCatalog().getProject();
	}

	public static String getMailBody() {
		if (getInstance().getCurrentCatalog() == null)
			return "";
		return getInstance().getCurrentCatalog().getMailBody();
	}

	public static String getMailTo() {
		if (getInstance().getCurrentCatalog() == null)
			return "";
		return getInstance().getCurrentCatalog().getMailTo();
	}

	public static String getMailFrom() {
		if (getInstance().getCurrentCatalog() == null)
			return "";
		return getInstance().getCurrentCatalog().getMailFrom();
	}

	public static String getMailFromPassword() {
		if (getInstance().getCurrentCatalog() == null)
			return "";
		return getInstance().getCurrentCatalog().getMailFromPwd();
	}

	public static int getExpansionFileMainVersion() {
		if (getInstance().getCurrentCatalog() == null)
			return 0;
		return getInstance().getCurrentCatalog().getExpansionFileMainVersion();
	}

	public static int getExpansionFilePatchVersion() {
		if (getInstance().getCurrentCatalog() == null)
			return 0;
		return getInstance().getCurrentCatalog().getExpansionFilePatchVersion();
	}

	public static Context getContext() {
		return me.getApplicationContext();
	}

}
