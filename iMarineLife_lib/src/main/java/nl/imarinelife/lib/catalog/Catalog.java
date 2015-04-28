package nl.imarinelife.lib.catalog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import nl.imarinelife.lib.DbHelper;
import nl.imarinelife.lib.LibApp;
import nl.imarinelife.lib.MainActivity;
import nl.imarinelife.lib.Preferences;
import nl.imarinelife.lib.divinglog.db.dive.DiveDbHelper;
import nl.imarinelife.lib.divinglog.db.res.Location;
import nl.imarinelife.lib.divinglog.db.res.LocationDbHelper;
import nl.imarinelife.lib.divinglog.db.res.ProfilePart;
import nl.imarinelife.lib.divinglog.db.res.ProfilePartDbHelper;
import nl.imarinelife.lib.fieldguide.db.FieldGuideAndSightingsEntryDbHelper;
import nl.imarinelife.lib.utility.SerializableSparseArray;
import nl.imarinelife.lib.utility.dialogs.ThreeChoiceDialogFragment;
import nl.imarinelife.lib.utility.events.LanguageChangeEvent;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.util.Log;

public abstract class Catalog implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static String TAG = "Catalog";
	
	public static int DATAVERSION_FIELDGUIDEANDSIGHTINGS = 0;
	public static int DATAVERSION_LOCATIONS = 0;
	public static int DATAVERSION_PROFILEPARTS = 0;

	public final static String PERSONAL = "P";
	public final static String TYPE_INTRO = "intro";
	public final static String TYPE_HELP = "help";
	public final static String TYPE_DANK = "dank";
	public final static String TYPE_ACK = "ack";
	public ArrayList<String> sightingChoices = new ArrayList<String>();
	public ArrayList<String> defaultableSightingChoices = new ArrayList<String>(); // can
																					// only
																					// be
																					// 1
																					// or
																					// 2
	public ArrayList<String> checkboxChoices = new ArrayList<String>();
	public String defaultChoice = null;
	public String allgroups = null;
	public boolean hideCode = false;

	public String name;
	protected String appName;
	public int version;
	public String versionName;
	protected int expansionFileMainVersion;
	protected int expansionFilePatchVersion;

	protected String introduction;
	protected String help;
	protected String thanks;
	protected String acknowledgements;
	protected String project_name;
	protected String mailBody;
	protected String mailTo;
	protected String mailFrom;
	protected String mailFromPwd;

	protected String possibleLanguage;

	protected Integer[] ids;
	protected Integer[] latinIds;
	protected Map<String, String> commonToGroup;
	protected Map<String, Integer> commonIds;
	protected Map<String, Integer> groupIds;
	protected Map<String, Integer> descrIds;
	// if checkValues change in order or number, they should all be changed in
	// the FieldGuide
	// otherwise the changeLocaleEvent will not work
	protected String[] checkValues;
	protected Map<String, Integer> locationNames; // locationNumbers are by
													// convention derived from
													// the resourceIdname
													// (R.string.loc00001)
	protected String[][] profileParts;
	protected Map<String, Integer> profilePartsMap;
	protected Map<String, Integer> valuesMap;
	protected byte[] salt;
	protected String base64PublicKey;

	public static boolean initializingLocations = false;

	public Catalog(Context ctx) {
	}

	public static android.location.Location getLocation() {
		// Get the location manager
		LocationManager locationManager = (LocationManager) LibApp
				.getInstance().getSystemService(Context.LOCATION_SERVICE);
		// Define the criteria how to select the location provider -> use
		// default
		Criteria criteria = new Criteria();
		String provider = locationManager.getBestProvider(criteria, false);
		android.location.Location location = locationManager
				.getLastKnownLocation(provider);
		return location;
	}

	public String getDefaultSightingChoice() {
		if (MainActivity.me == null || MainActivity.me.currentDive == null
				|| MainActivity.me.currentDive.getDiveDefaultChoice() == null) {
			Log.d(TAG,
					"getDefaultSightingChoice - no current dive or diveDefaultChoice - getting Pref["
							+ Preferences.getPersonalDefaultValue() + "]");
			return Preferences.getPersonalDefaultValue();
		} else {
			Log.d(TAG, "getDefaultSightingChoice - current dive["
					+ MainActivity.me.currentDive.getDiveNr()
					+ "] getting diveDefaultChoice["
					+ MainActivity.me.currentDive.getDiveDefaultChoice() + "]");
			return MainActivity.me.currentDive.getDiveDefaultChoice();
		}
	}

	public String getResourcedValue(Map<String, Integer> resourceIds,
			String id, String showValue) {
		return getResourcedValue(getName(), resourceIds, id, showValue);
	}

	public String getResourcedValue(String catalog,
			Map<String, Integer> resourceIds, String id, String showValue) {
		String toReturn;
		if (showValue != null && showValue.trim().length() > 0) {
			toReturn = showValue;
		} else if (id == null) {
			return null;
		} else {
			if (catalog.equals(LibApp.getCurrentCatalogName())) {
				if (resourceIds != null) {
					Integer resourceId = resourceIds.get(id);
					if (resourceId == null) {
						// should be personal (location)
						// just return the value itself
						toReturn = id;
					} else {
						toReturn = LibApp.getCurrentResources()
								.getString(resourceId);
					}
				} else {
					toReturn = id;
				}
			} else {
				String packagename = "nl.imarinelife." + catalog.toLowerCase();
				try {
					Resources res = MainActivity.me.getPackageManager()
							.getResourcesForApplication(packagename);
					Integer resourceId = res.getIdentifier(packagename
							+ ":string/" + id, "string", packagename);
					toReturn = res.getString(resourceId);
				} catch (Exception e) {
					Log.e(TAG, "Could not get resourceId from [" + packagename
							+ "] defaulting to id");
					toReturn = id; // not nice but it will give an indication
				}
			}
		}
		Log.d(TAG, "getResourcedValue: returning[" + toReturn + "] for code["
				+ id + "]");
		return toReturn;
	}

	public abstract Integer[] getIds();

	public abstract Integer[] getLatinIds();

	public abstract Map<String, Integer> getCommonIdMapping();

	public abstract Map<String, Integer> getGroupIdMapping();

	public abstract Map<String, Integer> getDescriptionIdMapping();

	public abstract Map<String, String> getCommonToGroupMapping();

	public abstract String getPossibleLanguage();

	public abstract void setPossibleLanguage(String language);

	public abstract String[] getCheckValues();

	public abstract Map<String, Integer> getValuesMapping();

	public abstract Map<String, Integer> getProfilePartsMapping();

	public abstract Map<String, Integer> getLocationNamesMapping();

	public abstract String getAppName();

	public abstract String getName();

	public abstract int getVersion();

	public abstract String getAllGroups();

	public abstract byte[] getSalt();

	public abstract String getBase64PublicKey();

	public abstract ArrayList<String> getSightingChoices();

	public abstract ArrayList<String> getDefaultableSightingChoices();

	public abstract ArrayList<String> getCheckBoxChoices();
	
	public abstract void onDataVersionUpgrade_FieldGuideAndSightings(
			int oldVersion, int newVersion);
	public abstract void onDataVersionUpgrade_Locations(
			int oldVersion, int newVersion);
	public abstract void onDataVersionUpgrade_ProfileParts(
			int oldVersion, int newVersion);
	
	public String getPrefText(String type) {
		if (type.equals(TYPE_INTRO)) {
			return getIntroduction();
		} else if (type.equals(TYPE_DANK)) {
			return getThanks();
		} else if (type.equals(TYPE_HELP)) {
			return getHelp();
		} else if (type.equals(TYPE_ACK)) {
			return getAck();
		} else {
			return "";
		}
	}

	public abstract String getIntroduction();

	public abstract String getHelp();

	public abstract String getThanks();

	public String getAck() {
		return acknowledgements;
	}

	public abstract String getProject();

	public abstract int getExpansionFileMainVersion();

	public abstract int getExpansionFilePatchVersion();

	public abstract String getMailBody();

	public abstract String getMailTo();

	public abstract String getMailFrom();

	public abstract String getMailFromPwd();
	
	protected void initializeFieldGuide(Context ctx){
		Log.d(TAG,"initializeFieldGuide");
		int oldDataVersion = Preferences.getInt(
				Preferences.DATAVERSION_FIELDGUIDEANDSIGHTINGS, 0);
		if (DATAVERSION_FIELDGUIDEANDSIGHTINGS > oldDataVersion) {
			Catalog currentCatalog = LibApp.getInstance().getCurrentCatalog();
			if(currentCatalog!=null){
				currentCatalog.onDataVersionUpgrade_FieldGuideAndSightings(oldDataVersion, DATAVERSION_FIELDGUIDEANDSIGHTINGS);
			}
		}
	}

	protected void initializeProfileParts(Context ctx) {
		Log.d(TAG, "initializeProfileParts");
		ProfilePartDbHelper profilepartsAdapter = ProfilePartDbHelper
				.getInstance(ctx);
		HashMap<ProfilePartDbHelper.AddType, SerializableSparseArray<ProfilePart>> list = profilepartsAdapter
				.fetchAll();
		boolean isEmpty = list.get(ProfilePartDbHelper.AddType.ADD).size() == 0
				&& list.get(ProfilePartDbHelper.AddType.NON_ADD).size() == 0;
		if (isEmpty) {
			fillProfileParts(profilepartsAdapter);
		}else{
			int oldDataVersion = Preferences.getInt(
					Preferences.DATAVERSION_PROFILEPARTS, 0);
			if (DATAVERSION_PROFILEPARTS > oldDataVersion) {
				Catalog currentCatalog = LibApp.getInstance().getCurrentCatalog();
				if(currentCatalog!=null){
					currentCatalog.onDataVersionUpgrade_ProfileParts(oldDataVersion, DATAVERSION_PROFILEPARTS);
				}
			}
		}
		
		

	}

	public void initializeLocations(Context ctx) {
		LocationDbHelper locationsAdapter = LocationDbHelper.getInstance(ctx);
		Cursor locationsCursor = locationsAdapter
				.fetchLocationsCursorForCatalog(name);
		boolean isEmpty = locationsCursor == null
				|| locationsCursor.getCount() == 0;
		if (isEmpty) {
			Log.d(TAG, "initializeLocations - empty so initializing");
			fillLocations(locationsAdapter, null, isEmpty);
		}else{
			int oldDataVersion = Preferences.getInt(
					Preferences.DATAVERSION_LOCATIONS, 0);
			if (DATAVERSION_LOCATIONS > oldDataVersion) {
				Catalog currentCatalog = LibApp.getInstance().getCurrentCatalog();
				if(currentCatalog!=null){
					currentCatalog.onDataVersionUpgrade_Locations(oldDataVersion, DATAVERSION_LOCATIONS);
				}
			}
		}
		
		locationsCursor.close();
		
	}

	public void fillLocations(LocationDbHelper locationDbHelper,
			DiveDbHelper diveDbHelper) {
		fillLocations(locationDbHelper, diveDbHelper, false);
	}

	public void fillLocations(LocationDbHelper locationDbHelper,
			DiveDbHelper diveDbHelper, final boolean isEmpty) {

		AsyncTask<DbHelper, Integer, Integer> task = new AsyncTask<DbHelper, Integer, Integer>() {
			@Override
			protected Integer doInBackground(DbHelper... params) {
				Log.d(TAG, "fillLocations (background)");
				int todo = 0;
				int done = 0;
				LocationDbHelper locationDbHelper = (LocationDbHelper) params[0];
				DiveDbHelper diveDbHelper = (DiveDbHelper) params[1];

                // How much is there to do
				Map<String, String> locationCatCodes = null;
				Set<String> locationCodesInDives = null;
				if (diveDbHelper != null && !isEmpty) {
					Log.d(TAG, "calling fillLocationCatCodes");
					DiveDbHelper.usedInBackgroundThread = true;
					locationCatCodes = diveDbHelper.getLocationCatCodesForDistinctLocationNames();
					todo = locationCatCodes != null ? locationCatCodes.size()
							: 0;
					locationCodesInDives = diveDbHelper.getDistinctLocationCodesInDives();
					todo += locationCodesInDives!=null ? locationCodesInDives.size() : 0;
				}
				todo += (locationNames != null ? locationNames.size() : 0);
				

				// just in case - there are certain states that may this
				// necessary
				// it will make sure every dive (for this catalog) has a catCode
				// ("loc01")
				if (locationCatCodes != null) {
					diveDbHelper.fillLocationCatCodes(locationCatCodes);
					done+=locationCatCodes.size();
					publishProgress(done, todo);
				}

				if (!isEmpty) {
					done = locationDbHelper.deleteAllLocationsForCatalog(name);
					Log.d(TAG, "deletedAllLocationsForCatalog[" + done + "]");
				}
				int size = 0;
				int doneHere = 0;
				if (locationNames != null && locationNames.keySet() != null
						&& locationNames.keySet().size() > 0) {
					size = locationNames.keySet().size();
					LocationDbHelper.usedInBackgroundThread = true;
					List<Location> locations = new ArrayList<Location>();
					for (String key : locationNames.keySet()) {
						Location location = new Location(name, key,
								key.substring(3), getResourcedValue(
										locationNames, key, null));
						locations.add(location);
					}
					doneHere = locationDbHelper.insertLocations(locations);
					done += doneHere;
					publishProgress(done, todo);
				}
				Log.d(TAG, "inserted locations[" + doneHere + "] of [" + size + "]");
				// LocationDbHelper.logAllDatabaseContent(LibApp.getContext());
				// usedInBackgroundThread: used in Destroy of MainActivity to
				// prevent closing of cursors and dbHelpers
				LocationDbHelper.usedInBackgroundThread = false;
				if (LocationDbHelper.closeAfterBackgroundThreadFinishes) {
					LocationDbHelper.closeAfterBackgroundThreadFinishes = false;
					locationDbHelper.close();
				}

				
				
				if (diveDbHelper != null && !isEmpty) {
					Log.d(TAG, "calling fillLocationNames");
					diveDbHelper.fillLocationShowNames(locationCodesInDives);
					done += locationCodesInDives.size();
					publishProgress(done, todo);
					DiveDbHelper.usedInBackgroundThread = false;
					if (DiveDbHelper.closeAfterBackgroundThreadFinishes) {
						DiveDbHelper.closeAfterBackgroundThreadFinishes = false;
						diveDbHelper.close();
					}
				}

				return done;
			}

			@Override
			protected void onProgressUpdate(Integer... values) {
				// runs on UI thread on publishProgress
				Log.d(TAG, values[0] + " " + values[1]);
				int perc = values[0] * 100 / values[1];

				ThreeChoiceDialogFragment fragment = LanguageChangeEvent.getDialogFragment();
				if (fragment != null) {
					fragment.refreshProgressLocations(perc);
				}
			
				super.onProgressUpdate(values);
			}
			
			@Override
			protected void onPostExecute(Integer result) {
				Preferences.setInt(Preferences.DATAVERSION_LOCATIONS, DATAVERSION_LOCATIONS);
				if (LanguageChangeEvent.dismissDialogFragment()) {
					String possibleLanguage = LibApp.getInstance()
							.getCurrentCatalog().getPossibleLanguage();
					Preferences.setString(Preferences.CURRENT_LANGUAGE,
							possibleLanguage);
					Preferences.setString(Preferences.IGNORED_LANGUAGE,
							possibleLanguage);
				}
				super.onPostExecute(result);
			}
		};
		Log.d(TAG, "fillLocations - starting task");
		task.execute(locationDbHelper, diveDbHelper);
	}

	public void fillProfileParts(ProfilePartDbHelper profilepartsDbHelper) {
		Log.d(TAG, "fillProfileParts");
		SerializableSparseArray<ProfilePart> parts = new SerializableSparseArray<ProfilePart>();
		int counter = 0;
		profilepartsDbHelper.deleteAllProfilePartsForCurrentCatalog();
		for (String[] profilePart : profileParts) {

			parts.put(
					counter,
					new ProfilePart(name, profilePart[0], getResourcedValue(
							getName(), getProfilePartsMapping(),
							profilePart[0], null), Boolean
							.parseBoolean(profilePart[1]), Integer
							.valueOf(profilePart[2])));
			counter++;
		}
		profilepartsDbHelper.insertProfileParts(parts);
	}

	public void cleanup(){
		FieldGuideAndSightingsEntryDbHelper sightingsHelper = FieldGuideAndSightingsEntryDbHelper.getInstance(LibApp.getContext());
		sightingsHelper.deleteSightingsForDive(0);
		
		Cursor allDives = DiveDbHelper.getInstance(LibApp.getContext()).fetchAllCursor();
		if(allDives!=null && allDives.getCount()>0)
		{
			allDives.moveToFirst();
			do{
				int diveNr = allDives.getInt(DiveDbHelper.KEY_DIVENR_CURSORLOC);
				String catalogToPreserve = allDives.getString(DiveDbHelper.KEY_CATNAME_CURSORLOC);
				sightingsHelper.cleanUpDiveDataInWrongCatalog(diveNr, catalogToPreserve);
			}while(allDives.moveToNext());
			allDives.close();
		}
		
		
	}

	public static void logAllDatabaseContent() {
		ProfilePartDbHelper.logAllDatabaseContent(LibApp.getContext());
		LocationDbHelper.logAllDatabaseContent(LibApp.getContext());
		FieldGuideAndSightingsEntryDbHelper.logAllDatabaseContent(LibApp.getContext());
		DiveDbHelper.logAllDatabaseContent(LibApp.getContext());
	}
	
	public boolean isCodeHidden(){
		return hideCode;
	}
}
