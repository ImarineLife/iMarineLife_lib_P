package nl.imarinelife.lib.divinglog.db.dive;

import java.io.Serializable;
import java.util.HashMap;

import nl.imarinelife.lib.LibApp;
import nl.imarinelife.lib.MainActivity;
import nl.imarinelife.lib.MarineLifeContentProvider;
import nl.imarinelife.lib.divinglog.db.res.Buddy;
import nl.imarinelife.lib.divinglog.db.res.Location;
import nl.imarinelife.lib.divinglog.db.res.ProfilePart;
import nl.imarinelife.lib.divinglog.db.res.ProfilePartDbHelper;
import nl.imarinelife.lib.divinglog.db.res.ProfilePartDbHelper.AddType;
import nl.imarinelife.lib.divinglog.sightings.Sighting;
import nl.imarinelife.lib.utility.SerializableSparseArray;
import android.net.Uri;
import android.util.Log;

public class Dive implements Serializable {
	private static final String																TAG					= "Dive";
	private static final long																serialVersionUID	= 1L;
	private boolean																			changed				= false;

	private String																			catalog				= null;
	private int																				diveNr				= 0;
	private long																			date				= 0;
	private long																			time				= 0;
	private Location																		location			= null;
	private Buddy																			buddy				= null;
	private HashMap<ProfilePartDbHelper.AddType, SerializableSparseArray<DiveProfilePart>>	profile				= new HashMap<ProfilePartDbHelper.AddType, SerializableSparseArray<DiveProfilePart>>();
	private int																				visibilityInMeters	= 0;
	private boolean																			sentAlready			= false;
	private String																			remarks				= null;
	private String																			diveDefaultChoice	= null;

	public static final Uri																	CONTENT_URI			= Uri.parse("content://"
																														+ MarineLifeContentProvider.getAuthority()
																														+ "/"
																														+ MarineLifeContentProvider.NAME_DIVE);
	public static final Uri																	CONTENT_URI_FILTER	= Uri.parse("content://"
																														+ MarineLifeContentProvider.getAuthority()
																														+ "/"
																														+ MarineLifeContentProvider.NAME_DIVE_FILTERED);

	public Dive(int diveNr) {
		this.setDiveNr(diveNr);
		this.setCatalog(LibApp.getCurrentCatalogName());
		this.setDiveDefaultChoice(null);
	}

	public Dive(String catalog, int diveNr, long date, long time, Location location, Buddy buddy,
			HashMap<ProfilePartDbHelper.AddType, SerializableSparseArray<DiveProfilePart>> profile,
			SerializableSparseArray<Sighting> sightings, int visibilityInMeters, boolean sentAlready, String remarks) {
		this(catalog, diveNr, date, time, location, buddy, profile, sightings, visibilityInMeters, sentAlready,
				remarks, null);
	}

	public Dive(String catalog, int diveNr, long date, long time, Location location, Buddy buddy,
			HashMap<ProfilePartDbHelper.AddType, SerializableSparseArray<DiveProfilePart>> profile,
			SerializableSparseArray<Sighting> sightings, int visibilityInMeters, boolean sentAlready, String remarks,
			String defaultChoice) {
		super();
		this.setCatalog(catalog);
		this.setDiveNr(diveNr);
		this.setDate(date);
		this.setTime(time);
		this.setLocation(location);
		this.setBuddy(buddy);
		this.setProfile(profile);
		// this.sightings = sightings;
		this.setVisibilityInMeters(visibilityInMeters);
		this.setSentAlready(sentAlready);
		this.setRemarks(remarks);
		this.setDiveDefaultChoice(defaultChoice);
	}

	public HashMap<ProfilePartDbHelper.AddType, SerializableSparseArray<ProfilePart>> getProfilePartSetUpFromDive() {
		HashMap<ProfilePartDbHelper.AddType, SerializableSparseArray<ProfilePart>> pparts = new HashMap<ProfilePartDbHelper.AddType, SerializableSparseArray<ProfilePart>>();
		Log.d(TAG,
			"getProfilePartSetUpFromDive[" + this + "]");

		SerializableSparseArray<ProfilePart> addList = new SerializableSparseArray<ProfilePart>();
		if (getProfile() != null && getProfile().get(ProfilePartDbHelper.AddType.ADD) != null) {
			SerializableSparseArray<DiveProfilePart> addListFromDive = getProfile().get(ProfilePartDbHelper.AddType.ADD);
			for (int i = 0; i < addListFromDive.size(); i++) {
				DiveProfilePart part = addListFromDive.get(addListFromDive.keyAt(i));
				addList.put(part.profilePart.orderNumber,
					part.profilePart);
				Log.d(TAG,
					"added to addList[" + part.profilePart + "]");
			}
		}

		SerializableSparseArray<ProfilePart> nonAddList = new SerializableSparseArray<ProfilePart>();
		if (getProfile() != null && getProfile().get(ProfilePartDbHelper.AddType.NON_ADD) != null) {
			SerializableSparseArray<DiveProfilePart> nonaddListFromDive = getProfile().get(ProfilePartDbHelper.AddType.NON_ADD);
			for (int i = 0; i < nonaddListFromDive.size(); i++) {
				DiveProfilePart part = nonaddListFromDive.get(nonaddListFromDive.keyAt(i));
				nonAddList.put(part.profilePart.orderNumber,
					part.profilePart);
				Log.d(TAG,
					"added to nonAddList[" + part.profilePart + "]");
			}
		}
		pparts.put(ProfilePartDbHelper.AddType.ADD,
			addList);
		pparts.put(ProfilePartDbHelper.AddType.NON_ADD,
			nonAddList);
		return pparts;
	}

	public void fillFromProfilePartsSetup(HashMap<AddType, SerializableSparseArray<ProfilePart>> pparts) {
		Log.d(TAG,"fillFromProfilePartsSetup ["+pparts+"]");
		if (pparts != null) {
			for (ProfilePartDbHelper.AddType type : pparts.keySet()) {
				SerializableSparseArray<ProfilePart> array = pparts.get(type);
				SerializableSparseArray<DiveProfilePart> divearray = new SerializableSparseArray<DiveProfilePart>();
				for (int i = 0; i < array.size(); i++) {
					ProfilePart ppart = array.get(array.keyAt(i));
					DiveProfilePart part = new DiveProfilePart(getDiveNr(), 0, ppart);
					divearray.put(ppart.orderNumber,
						part);
				}
				if (getProfile() == null) {
					setProfile(new HashMap<ProfilePartDbHelper.AddType, SerializableSparseArray<DiveProfilePart>>());
				}
				getProfile().put(type,
					divearray);
			}
		}

	}

	public DiveProfilePart getDiveProfilePart(ProfilePart part) {
		DiveProfilePart toReturn = null;
		ProfilePartDbHelper.AddType type = part.addForTotalDiveTime
				? ProfilePartDbHelper.AddType.ADD
				: ProfilePartDbHelper.AddType.NON_ADD;
		int orderNr = part.orderNumber;
		if (getProfile() != null) {
			SerializableSparseArray<DiveProfilePart> array = getProfile().get(type);
			if (array != null) {
				toReturn = array.get(orderNr);
			}
		}
		return toReturn;
	}

	/*
	 * public Sighting getSighting(int fieldGuideId) { if (sightings != null) {
	 * return sightings.get(fieldGuideId); } return null; }
	 * 
	 * public void upsertSighting(Context context, Sighting sighting) { if
	 * (sighting != null) { int fieldGuideId = sighting.fieldguide_id;
	 * sightings.put(Integer.valueOf(fieldGuideId), sighting);
	 * ((MainActivity)context).saveDive(sighting); } }
	 */

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append("Catalog[" + getCatalog() + "] ");
		builder.append("diveNr[" + getDiveNr() + "] ");
		builder.append("date[" + getDate() + "] ");
		builder.append("time[" + getTime() + "] ");
		builder.append("locationCode["+getLocationCode()+"]");
		builder.append("locationName["+getLocationName()+"]");
		builder.append("location["+getLocation()+"]");
		builder.append("buddy[" + getBuddy() + "] ");
		builder.append("visibility[" + getVisibilityInMeters() + "] ");
		builder.append("profile[");
		if (getProfile() != null) {
			builder.append("profile[");
			for (ProfilePartDbHelper.AddType type : getProfile().keySet()) {
				SerializableSparseArray<DiveProfilePart> array = getProfile().get(type);
				for (int i = 0; i < array.size(); i++) {
					DiveProfilePart ppart = array.get(array.keyAt(i));
					builder.append("diveprofilepart[" + ppart + "] ");
				}
			}

		} else {
			builder.append("null");
		}
		builder.append("]");
		/*
		 * if (sightings != null) { builder.append("sightings["); for (int i =
		 * 0; i < sightings.size(); i++) { Sighting sighting =
		 * sightings.get(sightings.keyAt(i)); builder.append("sighting[" +
		 * sighting + "] "); } } else { builder.append("null"); }
		 * builder.append("]");
		 */
		builder.append("sentAlready[" + isSentAlready() + "] ");
		builder.append("remarks[" + getRemarks() + "] ");
		builder.append("diveDefaultChoice[" + getDiveDefaultChoice() + "] ");
		return builder.toString();

	}

	public String getCatalog() {
		return catalog;
	}

	public void setCatalog(String catalog) {
		Log.d(TAG,
			"setCatalog: " + catalog);
		this.catalog = catalog;
	}

	public int getDiveNr() {
		return diveNr;
	}

	public void setDiveNr(int diveNr) {
		Log.d(TAG,
			"setDiveNr: " + diveNr);
		this.diveNr = diveNr;
	}

	public long getDate() {
		return date;
	}

	public String getFormattedDate() {
		if (this.date == 0) {
			return null;
		} else {
			return DiveSimpleCursorAdapter.dateformat.format(this.date);
		}

	}

	public void setDate(long date) {
		Log.d(TAG,
			"setDate: " + date);
		this.date = date;
	}

	public long getTime() {
		return time;
	}

	public String getFormattedTime() {
		if (this.time == 0) {
			return null;
		} else {
			return DiveSimpleCursorAdapter.timeformat.format(this.time);
		}

	}

	public void setTime(long time) {
		Log.d(TAG,
			"setTime: " + time);
		this.time = time;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		Log.d(TAG,
			"setLocation: " + location);
		this.location = location;
	}

	public String getLocationName() {
		if (this.location == null) {
			return null;
		} else {
			return location.getShowLocationName();
		}
	}

	public String getLocationCode() {
		if (this.location == null) {
			return null;
		} else {
			return location.getCatCode();
		}
	}

	public Buddy getBuddy() {
		return buddy;
	}

	public void setBuddy(Buddy buddy) {
		Log.d(TAG,
			"setBuddy: " + buddy);
		this.buddy = buddy;
	}

	public String getBuddyName() {
		if (buddy != null) {
			return buddy.getName();
		} else {
			return null;
		}

	}

	public void setBuddyName(String name) {
		if ("".equals(name)) {
			name = null;
		}
		if (buddy == null && name != null) {
			buddy = new Buddy();
		}
		if (buddy != null) {
			buddy.setName(name);
		}
	}

	public String getBuddyEmail() {
		if (buddy != null) {
			return buddy.getEmail();
		} else {
			return null;
		}
	}

	public void setBuddyEmail(String email) {
		if ("".equals(email)) {
			email = null;
		}
		if (buddy == null && email != null) {
			buddy = new Buddy();
		}
		if (buddy != null) {
			buddy.setEmail(email);
		}
	}

	public String getBuddyCode() {
		if (buddy != null) {
			return buddy.getCodeForCatalog(getCatalog());
		} else {
			return null;
		}
	}

	public void setBuddyCode(String code) {
		if ("".equals(code)) {
			code = null;
		}
		if (buddy == null && code != null) {
			buddy = new Buddy();
		}
		if (buddy != null) {
			if (code != null) {
				buddy.setCodeForCatalog(getCatalog(),
					code);
			} else {
				buddy.removeCodeForCatalog(getCatalog());
			}
		}

	}

	public boolean isBuddyCodeChanged() {
		if (buddy != null) {
			return buddy.isBuddyCodeChanged();
		} else {
			return false;
		}
	}

	public void setBuddyCodeChanged(boolean buddyCodeChanged) {
		if (buddy != null) {
			buddy.setBuddyCodeChanged(buddyCodeChanged);
		}

	}

	public boolean isBuddyEmailChanged() {
		if (buddy != null) {
			return buddy.isBuddyEmailChanged();
		} else {
			return false;
		}
	}

	public void setBuddyEmailChanged(boolean buddyEmailChanged) {
		if (buddy != null) {

			buddy.setBuddyEmailChanged(buddyEmailChanged);
		}
	}

	public boolean isBuddyNameMustbeChangedEveryWhere() {
		if (buddy != null) {
			return buddy.isBuddyNameMustbeChangedEveryWhere();
		} else {
			return false;
		}
	}

	public void setBuddyNameMustbeChangedEveryWhere(boolean buddyNameMustbeChangedEveryWhere) {
		if (buddy != null) {

			buddy.setBuddyNameMustbeChangedEveryWhere(buddyNameMustbeChangedEveryWhere);
		}
	}

	public String getBuddyNameSelected() {
		if (buddy == null) {
			return null;
		} else {
			return buddy.getBuddyNameSelected();
		}
	}

	public void setBuddyNameSelected(String buddyNameSelected) {
		if (buddy != null) {

			buddy.setBuddyNameSelected(buddyNameSelected);
		}
	}

	public HashMap<ProfilePartDbHelper.AddType, SerializableSparseArray<DiveProfilePart>> getProfile() {
		return profile;
	}

	public void setProfile(HashMap<ProfilePartDbHelper.AddType, SerializableSparseArray<DiveProfilePart>> profile) {
		Log.d(TAG,
			"setProfile: " + profile);
		this.profile = profile;
	}

	public void insertProfilePart(DiveProfilePart part) {
		Log.d(TAG,
			part.stayValueInMeters + " " + part.profilePart.orderNumber + " " + part.profilePart.addForTotalDiveTime);
		int orderNr = part.profilePart.orderNumber;
		boolean add = part.profilePart.addForTotalDiveTime;
		if (getProfile() == null) {
			Log.d(TAG,
				"insertProfilePart profile created");
			setProfile(new HashMap<ProfilePartDbHelper.AddType, SerializableSparseArray<DiveProfilePart>>());
		}
		ProfilePartDbHelper.AddType type = add
				? ProfilePartDbHelper.AddType.ADD
				: ProfilePartDbHelper.AddType.NON_ADD;
		SerializableSparseArray<DiveProfilePart> list = getProfile().get(type);
		if (list == null) {
			Log.d(TAG,
				"insertProfilePart list created for " + (add
						? ProfilePartDbHelper.AddType.ADD
						: ProfilePartDbHelper.AddType.NON_ADD));
			list = new SerializableSparseArray<DiveProfilePart>();
			getProfile().put(type,
				list);
		}
		list.put(orderNr,
			part);
	}

	public int getVisibilityInMeters() {
		return visibilityInMeters;
	}

	public void setVisibilityInMeters(int visibilityInMeters) {
		Log.d(TAG,
			"setVisibilityInMeters: " + visibilityInMeters);
		this.visibilityInMeters = visibilityInMeters;
	}

	public boolean isSentAlready() {
		return sentAlready;
	}

	public void setSentAlready(boolean sentAlready) {
		Log.d(TAG,
			"setSentAlready: " + sentAlready);
		this.sentAlready = sentAlready;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		Log.d(TAG,
			"setRemarks: " + remarks);
		this.remarks = remarks;
	}

	public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		Log.d(TAG,
			"setChanged: " + changed);
		this.changed = changed;
	}

	public String getDiveDefaultChoice() {
		return diveDefaultChoice;
	}

	public void setDiveDefaultChoice(String diveDefaultChoice) {
		if (diveDefaultChoice == null) {
			diveDefaultChoice = LibApp.getDiveOrPersonalOrCatalogDefaultChoice();
		}
		this.diveDefaultChoice = diveDefaultChoice;
	}

	public void save() {
		DiveDbHelper helper = DiveDbHelper.getInstance(MainActivity.me);
		helper.upsertDive(this,
			MainActivity.me);
		helper.close();
	}
}
